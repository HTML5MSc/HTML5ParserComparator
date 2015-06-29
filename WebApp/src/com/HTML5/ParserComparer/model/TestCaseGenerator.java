package com.HTML5.ParserComparer.model;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.HTML5.ParserComparer.model.TestCase.TestOutput;
import com.HTML5.ParserComparer.model.utils.FormattingUtils;
import com.HTML5.ParserComparer.model.utils.XMLUtils;

public class TestCaseGenerator {

	public static final String START_CODE_INS = "StartCodeIns";
	public static final String END_CODE_INS = "EndCodeIns";
	public static final String START_CODE_DEL = "StartCodeDel";
	public static final String END_CODE_DEL = "EndCodeDel";

	public static TestCase getTestCase(String filePath, String name,
			FormatOptions formatOptions) {
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

				ArrayList<Element> elements = XMLUtils.getElementsByTagName(
						testNode, "input");
				if (!elements.isEmpty())
					testCase.setInput(elements.get(0).getTextContent());

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
					} else {
						// testOutput.setEditDistance(StringUtils
						// .getLevenshteinDistance(majorityTree, tree));

						testOutput = getFormattedTreeFromDiffsNode(
								majorityTree,
								XMLUtils.getFirstElementByTagName(treeNode,
										"diffs"), formatOptions);
					}
					testOutput.setParsers(parsers);
					outputs.add(testOutput);

				}
				outputs = formatOutputs(outputs, formatOptions);
				testCase.setOutputs(outputs);
			}
		}
		return testCase;
	}

	private static TestOutput getFormattedTreeFromDiffsNode(
			String majorityTree, Node diffsNode, FormatOptions formatOptions) {
		StringBuilder reconstructedTree = new StringBuilder(majorityTree);
		TestOutput testOutput = new TestCase().new TestOutput();

		int index = 0;
		int indexOffset = 0;
		int deletions = 0;
		int insertions = 0;
		int charDeletions = 0;
		int charInsertions = 0;

		for (Element diff : XMLUtils.getElementsByTagName(diffsNode, "diff")) {

			index = Integer.parseInt(diff.getAttribute("index"));
			String type = diff.getAttribute("type");
			String content = diff.getTextContent();
			String newContent = null;

			if (type.equals("I")) {
				insertions++;
				charInsertions += content.length();
				newContent = START_CODE_INS + content + END_CODE_INS;

			} else if (type.equals("D")) {
				reconstructedTree.delete(index + indexOffset, index
						+ indexOffset + content.length());
				deletions++;
				charDeletions += content.length();
				newContent = START_CODE_DEL + content + END_CODE_DEL;
			}

			reconstructedTree.insert(index + indexOffset, newContent);
			indexOffset += newContent.length() - content.length();
		}

		String tree;
		if (formatOptions.isRemoveTextAfterLastDiff())
			tree = reconstructedTree.substring(0,
					reconstructedTree.indexOf("\n", index + indexOffset));
		else
			tree = reconstructedTree.toString();

		tree = escapeStringToHTML(tree);
		tree = unEscapeDiffTags(tree);
		tree = FormattingUtils.formatTreeLines(tree);
		testOutput.setTree(tree);
		testOutput.setCharDeletions(charDeletions);
		testOutput.setCharInsertions(charInsertions);
		testOutput.setDeletions(deletions);
		testOutput.setInsertions(insertions);

		return testOutput;
	}

	private static ArrayList<TestCase.TestOutput> formatOutputs(
			ArrayList<TestCase.TestOutput> outputs, FormatOptions formatOptions) {
		List<String> textsToRemove = getTextsToRemove(outputs.get(0).getTree(),
				formatOptions);
		for (String s : textsToRemove) {
			boolean all = true;
			for (TestCase.TestOutput output : outputs) {
				all &= output.getTree().contains(s);
			}

			if (all) {
				for (TestCase.TestOutput output : outputs) {
					output.setTree(output.getTree().replace(s, ""));
				}
			}
		}
		return outputs;
	}

	private static List<String> getTextsToRemove(String majorityTree,
			FormatOptions formatOptions) {
		List<String> textsToRemove = new ArrayList<String>();
		if (formatOptions.isRemoveComments()) {
			textsToRemove.addAll(FormattingUtils.getSubStringsOf(majorityTree,
					"&lt;!-- ", " --&gt;", true));
		}
		if (formatOptions.isRemoveStyleContent()) {
			textsToRemove.addAll(FormattingUtils.getSubStringsOf(majorityTree,
					"&lt;style&gt;"));
		}
		if (formatOptions.isRemoveScriptContent()) {
			textsToRemove.addAll(FormattingUtils.getSubStringsOf(majorityTree,
					"&lt;script&gt;"));
		}
		return textsToRemove;
	}

	private static String escapeStringToHTML(String string) {
		return string.replace("&", "&amp;").replace("<", "&lt;")
				.replace(">", "&gt;");
		// .replace("\n", "<br>");
	}

	private static String unEscapeDiffTags(String string) {
		string = string.replace(START_CODE_INS, "<ins>")
				.replace(END_CODE_INS, "</ins>")
				.replace(START_CODE_DEL, "<del>")
				.replace(END_CODE_DEL, "</del>");
		return string;
	}
}