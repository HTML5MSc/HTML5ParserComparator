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



import com.html5tools.Utils.IOUtils;
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

		if (args.length != 2)
			throw new Exception(
					"Missing argument. Argument must be the path to a directory and type of comparison");

		Comparator comparator = new Comparator();
		if (args[1].equals("s"))
			comparator.runSingle(args[0]);
		else if (args[1].equals("m"))
			comparator.runMulti(args[0]);

	}

	public void runMulti(String path) throws Exception {

		Boolean singleReport = true;
		// path = "A:\\GitHub\\HTML5ParserComparator\\Reports";

		if (Files.notExists(Paths.get(path)))
			throw new Exception("Could not find the directory");

		Path reportFileNameP = Paths.get(path, "report.xml");
		Path pathP = Paths.get(path);

		try (DirectoryStream<Path> rootStream = Files.newDirectoryStream(pathP)) {
			for (Path folderName : rootStream) {
				String testName = folderName.getFileName().toString();

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
						testName, trees, successfulParsers, singleReport,
						folderName.toAbsolutePath().toString());
			}
		} catch (IOException | DirectoryIteratorException e) {
			e.printStackTrace();
		}
	}

	public void runSingle(String path) throws IOException {
		// path = "A:\\GitHub\\HTML5ParserComparator\\Reports\\test1";
		String reportFileName = Paths.get(path, "report.xml").toString();
		List<OutputTree> trees = new ArrayList<OutputTree>();
		List<String> successfulParsers;
		parserNames = new ArrayList<String>();

		for (String fileName : IOUtils.listFilesInFolder(path, false)) {
			String outputName = fileName
					.substring(fileName.lastIndexOf("\\") + 1);
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
		double halfNoOfPasers = (parserNames.size() - parserWithErrors) / 2;
		if (majority.size() <= halfNoOfPasers) {
			return new ArrayList<String>();// No parser passed
		}

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

}