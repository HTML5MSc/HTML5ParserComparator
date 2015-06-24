package com.HTML5.ParserComparer.model;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.HTML5.ParserComparer.model.TestCase.TestOutput;

public class TestCaseGenerator {

	// static String filePath = "A:\\GitHub\\HTML5ParserComparator\\report.xml";

	public static TestCase getTestCase(String filePath, String name) {
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

				String majorityTree = null;
				xPathExpression += "/output";
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
					String tree = treeNode.getTextContent();
					if (isMajority) {
						testOutput.setTree(escapeStringToHTML(tree));
						majorityTree = tree;
						// testOutput.setEditDistance(0);
					} else {
						// testOutput.setEditDistance(StringUtils
						// .getLevenshteinDistance(majorityTree, tree));
						// testOutput.setEditDistance(0);

						testOutput = getFormattedTreeFromDiffsNode(
								majorityTree,
								XMLUtils.getFirstElementByTagName(treeNode,
										"diffs"));
					}
					testOutput.setParsers(parsers);
					outputs.add(testOutput);

				}

				testCase.setOutputs(outputs);
			}
		}
		return testCase;
	}

	private static TestOutput getFormattedTreeFromDiffsNode(
			String majorityTree, Node diffsNode) {
		StringBuilder reconstructedTree = new StringBuilder(majorityTree);
		TestOutput testOutput = new TestCase().new TestOutput();

		int indexOffset = 0;
		int deletions = 0;
		int insertions = 0;
		int charDeletions = 0;
		int charInsertions = 0;

		boolean first = true;
		for (Element diff : XMLUtils.getElementsByTagName(diffsNode, "diff")) {
		
			int index = Integer.parseInt(diff.getAttribute("index"));
			String type = diff.getAttribute("type");
			String content = diff.getTextContent();
			String newContent = null;
			
//			if(first)
//				first = false;
//			else
//			{
//				reconstructedTree.insert(reconstructedTree.indexOf("|", index), "</span>");
//				indexOffset += 7;
//			}

			if (type.equals("I")) {
				insertions++;
				charInsertions += content.length();
				newContent = "<ins>" + content + "</ins>";

			} else if (type.equals("D")) {
				reconstructedTree.delete(index + indexOffset, index
						+ indexOffset + content.length());
				deletions++;
				charDeletions += content.length();
				newContent = "<del>" + content + "</del>";

			}
			//reconstructedTree.insert(reconstructedTree.lastIndexOf("|", index - 10), "<span>");
			reconstructedTree.insert(index + indexOffset, newContent);
			//indexOffset += 6;
			indexOffset += newContent.length() - content.length();
		}
		String tree = escapeStringToHTML(reconstructedTree.toString());
		tree = unEscapeDiffTags(tree);
		testOutput.setTree(tree);
		testOutput.setCharDeletions(charDeletions);
		testOutput.setCharInsertions(charInsertions);
		testOutput.setDeletions(deletions);
		testOutput.setInsertions(insertions);

		return testOutput;
	}

	private static String escapeStringToHTML(String string) {
		return string.replace("&", "&amp;").replace("<", "&lt;")
				.replace(">", "&gt;");
		// .replace("\n", "<br>");
	}

	private static String unEscapeDiffTags(String string) {
		return string.replace("&lt;ins&gt;", "<ins>")
				.replace("&lt;/ins&gt;", "</ins>")
				.replace("&lt;del&gt;", "<del>")
				.replace("&lt;/del&gt;", "</del>");
	}
}