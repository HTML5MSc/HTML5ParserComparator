package com.html5tools.parserComparison;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.html5tools.Utils.DiffUtils;
import com.html5tools.Utils.IOUtils;
import com.html5tools.Utils.XMLUtils;

/**
 *
 * @author JoseArmando
 */
public class Comparator {

	Document report;
	private List<String> parserNames;

	public static void main(String[] args) throws Exception {
		Comparator comparator = new Comparator();
		comparator.run(args);
	}

	public void run(String[] args) throws Exception {

		if (args.length != 1)
			throw new Exception(
					"Missing argument. Argument must be the path to a directory.");
		
		String path = args[0];
		// String path = "A:\\GitHub\\HTML5ParserComparator\\testHtml5libTests";

		if (!IOUtils.directoryExists(path))
			throw new Exception("Could not find the directory");

		String reportFileName = path + "\\" + "report.xml";
		getReport(reportFileName);

		for (String folderName : IOUtils.listFoldersInFolder(path, false)) {
			folderName = path + "\\" + folderName;
			String testName = folderName;
			String inputValue = folderName;
			List<OutputTree> trees = new ArrayList<OutputTree>();
			List<String> successfulParsers;
			parserNames = new ArrayList<String>();

			for (String fileName : IOUtils.listFilesInFolder(folderName, false)) {
				if (fileName.contains("majority") || fileName.contains("diff")
						|| fileName.contains("input"))
					continue;

				parserNames.add(fileName);
				String tree = IOUtils.readFile(folderName + "\\" + fileName);
				trees.add(new OutputTree(tree, fileName));
				IOUtils.deleteFile(folderName + "\\" + fileName);
			}
			if (parserNames.isEmpty())
				continue;

			trees = groupByEquality(trees);
			successfulParsers = getParsersInMajority(trees);

			updateReport(folderName, testName, trees, inputValue);
			updateTestResults(successfulParsers);
		}
		XMLUtils.saveReportToFile(report, reportFileName);
	}

	private void getReport(String reportFileName) {
		try {
			report = XMLUtils.readXMLFromFile(reportFileName);
		} catch (Exception e) {
			report = XMLUtils.createDocument();
			XMLUtils.addNode(report, report, "report");
			appendTotalsTags();
			appendParserInfoTags();
		}
	}

	private void appendTotalsTags() {

		Node root = report.getFirstChild();
		Node totals = XMLUtils.addNode(report, root, "generalData");

		XMLUtils.addAttribute(report, totals, "numberOfTests", "0");
		XMLUtils.addAttribute(report, totals, "equals", "0");
		XMLUtils.addAttribute(report, totals, "different", "0");
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		XMLUtils.addAttribute(report, totals, "date",
				dateFormat.format(new Date()));
	}

	private void appendParserInfoTags() {
		Node root = report.getFirstChild();
		XMLUtils.addNode(report, root, "testResult");
	}

	private void appendParserInfoTag(String parserName, boolean passed) {
		Node root = report.getFirstChild();
		Node testResult = XMLUtils.getFirstElementByTagName(root, "testResult");
		Node parserNode = XMLUtils.addNode(report, testResult, "parser");

		XMLUtils.addAttribute(report, parserNode, "name", parserName);
		XMLUtils.addAttribute(report, parserNode, "passed", passed ? "1" : "0");
		XMLUtils.addAttribute(report, parserNode, "failed", passed ? "0" : "1");

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

	private void updateReport(String folderPath, String testName,
			List<OutputTree> trees, String inputValue) {

		Node root = report.getElementsByTagName("report").item(0);
		Node totals = report.getElementsByTagName("generalData").item(0);
		XMLUtils.incrementAttributeValue(totals, "numberOfTests");

		Node test = XMLUtils.addNode(report, root, "test");
		XMLUtils.addAttribute(report, test, "name", testName);
		XMLUtils.addAttribute(report, test, "numberOfTrees",
				String.valueOf(trees.size()));

		Node input = XMLUtils.addNode(report, test, "input");
		XMLUtils.addAttribute(report, input, "name", inputValue);

		if (trees.size() == 1)
			XMLUtils.incrementAttributeValue(totals, "equals");
		else
			XMLUtils.incrementAttributeValue(totals, "different");

		processOutputs(folderPath, test, trees);

	}

	private void processOutputs(String folderPath, Node test,
			List<OutputTree> trees) {
		String majorityTree = null;
		for (int i = 0; i < trees.size(); i++) {
			OutputTree tree = trees.get(i);

			Node output = XMLUtils.addNode(report, test, "output");

			Node parsers = XMLUtils.addNode(report, output, "parsers");
			for (String parserName : tree.getParsers()) {
				Node parser = XMLUtils.addNode(report, parsers, "parser");
				XMLUtils.addAttribute(report, parser, "name", parserName);
			}

			String fileName;
			String content;
			// Because the list is ordered, the first tree represents the
			// majority
			if (i == 0) {
				majorityTree = tree.getTree();
				fileName = "majority";
				content = majorityTree;
				XMLUtils.addAttribute(report, output, "majority", "true");
				XMLUtils.addAttribute(report, output, "fileName", fileName);
				IOUtils.saveFile(folderPath + "\\" + fileName, content);
			} else {
				fileName = "diff" + String.valueOf(i);
				content = DiffUtils.getFormattedDiffs(majorityTree,
						tree.getTree());
				XMLUtils.addAttribute(report, output, "majority", "false");
				XMLUtils.addAttribute(report, output, "fileName", fileName);
				IOUtils.saveFile(folderPath + "\\" + fileName, content);
			}
		}
	}

	private void updateTestResults(List<String> successfulParsers) {

		for (String parserName : successfulParsers) {

			String xPathExp = "/report/testResult/*[@name='" + parserName
					+ "']";
			Node parserNodeInTestResult = XMLUtils.executeXPathExpression(
					report, xPathExp);
			if (parserNodeInTestResult != null)
				XMLUtils.incrementAttributeValue(parserNodeInTestResult,
						"passed");
			else
				appendParserInfoTag(parserName, true);
		}
		List<String> failingParsers = new ArrayList<String>(parserNames);
		failingParsers.removeAll(successfulParsers);

		for (String parserName : failingParsers) {

			String xPathExp = "/report/testResult/*[@name='" + parserName
					+ "']";
			Node parserNodeInTestResult = XMLUtils.executeXPathExpression(
					report, xPathExp);
			if (parserNodeInTestResult != null)
				XMLUtils.incrementAttributeValue(parserNodeInTestResult,
						"failed");
			else
				appendParserInfoTag(parserName, false);
		}
	}

}