package com.html5tools.parserComparison.report.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.html5tools.parserComparison.OutputTree;
import com.html5tools.parserComparison.diff_match_patch;
import com.html5tools.parserComparison.diff_match_patch.Diff;
import com.html5tools.parserComparison.diff_match_patch.Operation;
import com.html5tools.parserComparison.report.Report;

public class SingleReport extends Report {

	public SingleReport(List<String> parserNames) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			report = db.newDocument();
			this.parserNames = parserNames;
			addNode(report, "report");
			appendTotalsTags();
			appendParserInfoTags();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public SingleReport(String fileName, List<String> parserNames)
			throws SAXException, IOException {
		DocumentBuilderFactory dbf;
		DocumentBuilder db;
		dbf = DocumentBuilderFactory.newInstance();
		try {
			db = dbf.newDocumentBuilder();
			// document = db.parse(new File(fileName));

			File file = new File(fileName);
			InputStream inputStream = new FileInputStream(file);
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			report = db.parse(is);
			this.parserNames = parserNames;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}

	public void updateReport(String testName, List<OutputTree> trees,
			List<String> successfulParsers, String inputValue) {
		Node root = report.getElementsByTagName("report").item(0);
		Node totals = report.getElementsByTagName("generalData").item(0);
		incrementAttributeValue(totals, "numberOfTests");

		Node test = addNode(root, "test");
		addAttribute(test, "name", testName);
		addAttribute(test, "numberOfTrees", String.valueOf(trees.size()));

		Node input = addNode(test, "input");
		addCDATA(input, inputValue);

		if (trees.size() == 1)
			incrementAttributeValue(totals, "equals");
		else
			incrementAttributeValue(totals, "different");

		String majorityTree = null;
		for (int i = 0; i < trees.size(); i++) {
			OutputTree tree = trees.get(i);

			Node output = addNode(test, "output");

			Node parsers = addNode(output, "parsers");
			for (String parserName : tree.getParsers()) {
				Node parser = addNode(parsers, "parser");
				addAttribute(parser, "name", parserName);
			}

			Node treeNode = addNode(output, "tree");
			// Because the list is ordered, the first tree represents the
			// majority
			if (i == 0) {
				addAttribute(output, "majority", "true");
				majorityTree = tree.getTree();
				addCDATA(treeNode, tree.getTree());
			} else {
				addAttribute(output, "majority", "false");
				Node diffsNode = getDiffsNode(majorityTree, tree.getTree());
				treeNode.appendChild(diffsNode);
			}
		}

		updateTestResults(successfulParsers);
	}

	public void saveReportToFile(String reportFileName) {

		File output = new File(reportFileName);
		StreamResult resultFile = new StreamResult(output);

		DOMSource source = new DOMSource(report);
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.METHOD, "xml");
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
					"4");
			t.transform(source, resultFile);
			// t.transform(source, resultString);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	

	private Node getDiffsNode(String tree1, String tree2) {
		Node diffsNode = report.createElement("diffs");

		diff_match_patch dmp = new diff_match_patch();
		LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(tree1, tree2,
				false);
		dmp.Diff_EditCost = 1;
		dmp.diff_cleanupEfficiency(diffs);
		// dmp.diff_cleanupSemantic(diffs);
		// dmp.diff_cleanupMerge(diffs);

		int index = 0;
		for (Diff diff : diffs) {
			if (diff.operation != Operation.EQUAL) {
				Node diffNode = addNode(diffsNode, "diff");
				addCDATA(diffNode, diff.text);
				addAttribute(diffNode, "index", String.valueOf(index));

				if (diff.operation == Operation.INSERT)
					addAttribute(diffNode, "type", "I");
				else if (diff.operation == Operation.DELETE)
					addAttribute(diffNode, "type", "D");
			}
			index += diff.text.length();
		}

		return diffsNode;
	}

	private void updateTestResults(List<String> successfulParsers) {

		for (String parserName : successfulParsers) {

			String xPathExp = "/report/testResult/*[@name='" + parserName
					+ "']";
			Node parserNodeInTestResult = executeXPathExpression(xPathExp);
			if (parserNodeInTestResult != null)
				incrementAttributeValue(parserNodeInTestResult, "passed");
		}
		List<String> failingParsers = new ArrayList<String>(parserNames);
		failingParsers.removeAll(successfulParsers);

		for (String parserName : failingParsers) {

			String xPathExp = "/report/testResult/*[@name='" + parserName
					+ "']";
			Node parserNodeInTestResult = executeXPathExpression(xPathExp);
			if (parserNodeInTestResult != null)
				incrementAttributeValue(parserNodeInTestResult, "failed");
		}
	}

}
