package com.html5tools.parserComparison;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.html5tools.Utils.DiffUtils;
import com.html5tools.Utils.IOUtils;
import com.html5tools.Utils.XMLUtils;
import com.html5tools.parserComparison.report.Report;
import com.html5tools.parserComparison.report.impl.PartitionedReport;
import com.html5tools.parserComparison.report.impl.SingleReport;

/**
 *
 * @author JoseArmando
 */
public class Comparator {

	Report report;
	private List<String> parserNames;

	/**
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		if (args.length == 0)
			throw new Exception(
					"Missing argument. Argument must be the path to a directory and type of comparison");
		Comparator comparator = new Comparator();
		String path = args[0];
		// path = "A:\\GitHub\\HTML5ParserComparator\\Reports\\adoption01";
		if (args.length > 1 && args[1].equals("s"))
			comparator.runSingle(path);
		else if (args.length > 1 && args[1].equals("u")) {
			comparator.restoreOutputsFromDiffs(path);
			comparator.runMulti(path);
		} else
			// if (args[1].equals("m"))
			comparator.runMulti(path);
	}

	public void runMulti(String path) throws Exception {

		Boolean singleReport = true;

		if (Files.notExists(Paths.get(path)))
			throw new Exception("Could not find the directory");

		Path reportFileNameP = Paths.get(path, "report.xml");
		Path pathP = Paths.get(path);

		try (DirectoryStream<Path> rootStream = Files.newDirectoryStream(pathP)) {
			for (Path folderName : rootStream) {
				// String testName = folderName.getFileName().toString();

				List<OutputTree> trees = new ArrayList<OutputTree>();
				List<String> successfulParsers;
				parserNames = new ArrayList<String>();

				if (!Files.isDirectory(folderName))
					continue;

				try (DirectoryStream<Path> treeFolderStream = Files
						.newDirectoryStream(folderName)) {
					for (Path treeFile : treeFolderStream) {
						String fileName = treeFile.getFileName().toString();
						if (fileName.contains("majority")
								|| fileName.contains("diff")
								|| fileName.contains("input"))
							continue;

						parserNames.add(fileName);

						String tree = new String(Files.readAllBytes(treeFile),
								Charset.forName("UTF-8"));
						tree = processEndOfLines(tree);
						trees.add(new OutputTree(tree, fileName));
						Files.delete(treeFile);
					}
				} catch (IOException | DirectoryIteratorException e) {
					e.printStackTrace();
				}

				if (parserNames.isEmpty())
					continue;

				trees = groupByEquality(trees);
				successfulParsers = getParsersInMajority(trees);
				saveToReport(reportFileNameP.toAbsolutePath().toString(),
						folderName.toAbsolutePath().toString(), trees,
						successfulParsers, singleReport, folderName
								.toAbsolutePath().toString());
			}
		} catch (IOException | DirectoryIteratorException e) {
			e.printStackTrace();
		}
	}

	public void runSingle(String path) throws IOException {

		String reportFileName = Paths.get(path, "report.xml").toString();
		List<OutputTree> trees = new ArrayList<OutputTree>();
		List<String> successfulParsers;
		parserNames = new ArrayList<String>();

		for (String fileName : IOUtils.listFilesInFolder(path, false)) {
			String outputName = Paths.get(fileName).getFileName().toString();
			// fileName.substring(fileName.lastIndexOf("\\") + 1);
			if (fileName.contains("input"))
				continue;

			parserNames.add(outputName);
			String tree = IOUtils.readFile(fileName);
			trees.add(new OutputTree(tree, outputName));
			IOUtils.deleteFile(fileName);
		}
		if (parserNames.isEmpty())
			return;

		trees = groupByEquality(trees);
		successfulParsers = getParsersInMajority(trees);

		saveToReport(reportFileName, path, trees, successfulParsers, true, path);
	}

	public void restoreOutputsFromDiffs(String path) throws Exception {
		String reportFileName = Paths.get(path, "report.xml").toString();
		Document document = XMLUtils.readXMLFromFile(reportFileName);
		String xPathExpression = "/report/test";
		NodeList testNodes = (NodeList) XMLUtils.executeXPath(document,
				xPathExpression, XPathConstants.NODESET);
		for (int i = 0; i < testNodes.getLength(); i++) {
			Node testNode = testNodes.item(i);
			String testName = XMLUtils.getAttributeValue(testNode, "name");
			for (Element output : XMLUtils.getElementsByTagName(testNode,
					"output")) {
				String parsers = XMLUtils.getAttributeValue(output, "parsers");
				boolean majority = XMLUtils.getAttributeValue(output,
						"majority").equals("true");
				String majorityTree = IOUtils.readFile(Paths.get(testName,
						"majority").toString());
				for (String parser : parsers.split("\\|")) {
					String originalTreeFile = Paths.get(testName, parser)
							.toString();
					String content;
					if (majority)
						content = majorityTree;
					else {
						String diffName = XMLUtils.getAttributeValue(output,
								"name");
						content = IOUtils.readFile(Paths
								.get(testName, diffName).toString());
						content = DiffUtils.getOriginalFromDiffs(majorityTree,
								content);
					}
					IOUtils.saveFile(originalTreeFile, content);
				}
			}
		}
		IOUtils.deleteFile(reportFileName);
	}

	private List<String> getParsersInMajority(List<OutputTree> trees) {

		// Don't count the parsers with error in their parsing
		// Outputs with error starts with ERROR: {details}
		int parserWithErrors = 0;
		List<List<String>> parsersList = new ArrayList<List<String>>();
		for (OutputTree tree : trees) {
			if (!tree.getTree().startsWith("ERROR")) {
				parsersList.add(tree.getParsers());
			} else {
				parserWithErrors++;
			}
		}

		// No trees were produced, thus no majority
		if (parsersList.isEmpty())
			return new ArrayList<String>();

		// if more the one tree was produced and the highest(they are ordered)
		// is equal to the next one, then there is no majority
		if (parsersList.size() != 1
				&& (parsersList.get(0).size() <= parsersList.get(1).size())) {
			return new ArrayList<String>();
		}

		List<String> majority = parsersList.get(0);

		// is majority if more than the half of total trees
		// ACCORDING to boyer-moore majority vote algorithm
		// double halfNoOfPasers = (parserNames.size() - parserWithErrors) / 2;
		// if (majority.size() <= halfNoOfPasers) {
		// return new ArrayList<String>();// No parser passed
		// }

		return majority;
	}

	private void saveToReport(String reportFileName, String testName,
			List<OutputTree> trees, List<String> successfulParsers,
			boolean singleReport, String folderPath) {
		try {
			if (singleReport)
				report = new SingleReport(reportFileName, parserNames);
			else
				report = new PartitionedReport(reportFileName, parserNames);
		} catch (FileNotFoundException e) {
			if (singleReport)
				report = new SingleReport(parserNames);
			else
				report = new PartitionedReport(parserNames);
		} catch (Exception e) {
			e.printStackTrace();
			return;// Do not continue
		}

		report.updateReport(folderPath, testName, trees);
		report.updateTestResults(successfulParsers);
		report.saveReportToFile(reportFileName);
	}

	private List<OutputTree> groupByEquality(List<OutputTree> trees) {

		List<OutputTree> outputs = new ArrayList<OutputTree>();

		for (OutputTree tree : trees) {
			if (!outputs.contains(tree))
				outputs.add(tree);
			else
				outputs.get(outputs.indexOf(tree)).addParser(
						tree.getParsers().get(0));
		}
		Collections.sort(outputs);
		return outputs;
	}

	private String processEndOfLines(String tree) {
		tree = tree.replace("\r\n", "\n").replace("\n",
				System.getProperty("line.separator"));
		if (tree.endsWith(System.getProperty("line.separator")))
			tree = tree.substring(0,
					tree.length()
							- System.getProperty("line.separator").length());
		return tree;
	}
}