package com.HTML5.ParserComparer.model;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TestCaseGenerator {

	static String filePath = "A:\\GitHub\\HTML5ParserComparator\\report.xml";

	public static TestCase getTestCase(String name) {
		TestCase testCase = new TestCase();
		String xPathExpression = "/report/test[@name=\"" + name + "\"]";
		Document document = XMLUtils.readXMLFromFile(filePath);
		if (document != null) {
			NodeList nodes = XMLUtils.executeXPathExpression(document,
					xPathExpression);
			if (nodes != null && nodes.getLength() > 0) {
				Node testNode = nodes.item(0);
				String numberOfTrees = testNode.getAttributes()
						.getNamedItem("numberOfTrees").getNodeValue();
				testCase.setNumberOfTrees(Integer.parseInt(numberOfTrees));
				testCase.setName(name);

				ArrayList<TestCase.TestOutput> outputs = new ArrayList<TestCase.TestOutput>();

				xPathExpression += "/output";
				String majorityTree = getMajorityTree(xPathExpression, document);
				NodeList outputNodes = XMLUtils.executeXPathExpression(
						document, xPathExpression);
				for (int i = 0; i < outputNodes.getLength(); i++) {
					Node outputNode = outputNodes.item(i);
					boolean isMajority = outputNode.getAttributes()
							.getNamedItem("majority").getNodeValue()
							.equals("true");

					Element treeNode = XMLUtils.getFirstElementByTagName(
							outputNode, "tree");
					Element parsersNode = XMLUtils.getFirstElementByTagName(
							outputNode, "parsers");

					ArrayList<String> parsers = new ArrayList<String>();

					for (Element parser : XMLUtils.getElementsByTagName(
							parsersNode, "parser")) {
						String parserName = parser.getAttributes()
								.getNamedItem("name").getNodeValue();
						parsers.add(parserName);
					}

					TestCase.TestOutput testOutput = testCase.new TestOutput();
					testOutput.setParsers(parsers);
					String tree = treeNode.getTextContent();
					if (isMajority) {
						testOutput.setTree(tree.replace("&", "&amp;")
								.replace("<", "&lt;").replace(">", "&gt;")
								.replace("\n", "<br>"));
						testOutput.setEditDistance(0);
					} else {
						testOutput.setEditDistance(StringUtils
								.getLevenshteinDistance(majorityTree, tree));

						testOutput.setTree(getTreeWithDiffFormat(majorityTree,
								tree));
					}
					outputs.add(testOutput);

				}

				testCase.setOutputs(outputs);
			}
		}
		return testCase;
	}

	private static String getMajorityTree(String baseXPathExpression,
			Document document) {
		String majorityTree = null;
		baseXPathExpression += "[@majority=\"true\"]/tree";
		NodeList outputNodes = XMLUtils.executeXPathExpression(document,
				baseXPathExpression);
		for (int i = 0; i < outputNodes.getLength(); i++) {
			Node outputNode = outputNodes.item(i);
			majorityTree = outputNode.getTextContent();
		}
		return majorityTree;
	}

	private static String getTreeWithDiffFormat(String string1, String string2) {
		diff_match_patch dmp = new diff_match_patch();
		LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(string1,
				string2);
		dmp.diff_cleanupMerge(diffs);
		return dmp.diff_prettyHtml_Bootstrap(diffs);
	}
}
