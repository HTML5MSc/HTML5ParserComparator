package com.HTML5.ParserComparer.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.HTML5.ParserComparer.model.utils.FormattingUtils;
import com.html5tools.Utils.DiffEntry;
import com.html5tools.Utils.DiffEntry.DiffOperation;
import com.html5tools.Utils.DiffUtils;
import com.html5tools.Utils.IOUtils;
import com.html5tools.Utils.XMLUtils;

public class TestCaseGenerator {

	static TestCase testCase;

	private static int deletions;
	private static int insertions;
	private static int charDeletions;
	private static int charInsertions;

	public static TestCase getTestCase(String filePath, String name,
			FormatOptions formatOptions) {

		testCase = new TestCase();
		String xPathExpression = "/report/test[@name=\"" + name + "\"]";
		Document document = null;
		Node testNode = null;
		try {
			document = XMLUtils.readXMLFromFile(filePath);
			testNode = (Node) XMLUtils.executeXPath(document, xPathExpression,
					XPathConstants.NODE);
			getTestCaseData(testNode, name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (document == null || testNode == null)
				return testCase;
		}

		ArrayList<TestCase.TestOutput> outputs = new ArrayList<TestCase.TestOutput>();

		String majorityTree = null;
		xPathExpression += "/output";
		NodeList outputNodes = (NodeList) XMLUtils.executeXPath(document,
				xPathExpression, XPathConstants.NODESET);
		for (int i = 0; i < outputNodes.getLength(); i++) {
			Node outputNode = outputNodes.item(i);
			boolean isMajority = XMLUtils.getAttributeValue(outputNode,
					"majority").equals("true");

			TestCase.TestOutput testOutput = testCase.new TestOutput();
			testOutput.setParsers(getParserData(outputNode));

			String fileName = name + "\\"
					+ XMLUtils.getAttributeValue(outputNode, "fileName");
			try {
				String tree = IOUtils.readFile(fileName);
				if (isMajority) {
					testOutput.setTree(FormattingUtils.escapeStringToHTML(tree)
							.trim());
					majorityTree = tree;
				} else {
					tree = getFormattedTree(majorityTree, tree, formatOptions);
					testOutput.setTree(tree.trim());
					testOutput.setCharDeletions(charDeletions);
					testOutput.setCharInsertions(charInsertions);
					testOutput.setDeletions(deletions);
					testOutput.setInsertions(insertions);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			outputs.add(testOutput);
		}
		testCase.setOutputs(outputs);
		formatOutputs(formatOptions);

		return testCase;
	}

	private static void getTestCaseData(Node testNode, String name)
			throws IOException {
		String numberOfTrees = XMLUtils.getAttributeValue(testNode,
				"numberOfTrees");
		testCase.setNumberOfTrees(Integer.parseInt(numberOfTrees));
		testCase.setName(name);

		String fileName = name + "\\input";
		String tree = IOUtils.readFile(fileName);
		testCase.setInput(tree);

	}

	private static ArrayList<String> getParserData(Node outputNode) {
		ArrayList<String> parsers = new ArrayList<String>();
		Element parsersNode = XMLUtils.getFirstElementByTagName(outputNode,
				"parsers");

		for (Element parser : XMLUtils.getElementsByTagName(parsersNode,
				"parser")) {
			String parserName = XMLUtils.getAttributeValue(parser, "name");
			parsers.add(parserName);
		}
		return parsers;
	}

	private static String getFormattedTree(String majorityTree,
			String diffsString, FormatOptions formatOptions) throws Exception {

		StringBuilder reconstructedTree = new StringBuilder(majorityTree);
		boolean originalTree = formatOptions.isOriginalOutput();
		int index = 0;
		int indexOffset = 0;

		deletions = 0;
		insertions = 0;
		charDeletions = 0;
		charInsertions = 0;

		for (DiffEntry diff : DiffUtils.getDiffs(diffsString)) {

			index = diff.getIndex();
			DiffOperation diffOperation = diff.getDiffOperation();
			String content = diff.getContent();
			String newContent = null;

			if (diffOperation == DiffOperation.INSERT) {
				insertions++;
				charInsertions += content.length();
				newContent = originalTree ? content : FormattingUtils
						.escapeInsTag(content);

			} else if (diffOperation == DiffOperation.DELETE) {
				reconstructedTree.delete(index + indexOffset, index
						+ indexOffset + content.length());
				deletions++;
				charDeletions += content.length();
				newContent = originalTree ? "" : FormattingUtils
						.escapeDelTag(content);
			}

			reconstructedTree.insert(index + indexOffset, newContent);
			indexOffset += newContent.length() - content.length();
		}

		String tree;
		int lastLineIndex;
		if (formatOptions.isRemoveTextAfterLastDiff()) {
			lastLineIndex = reconstructedTree.indexOf(
					System.getProperty("line.separator"), index + indexOffset);
			if (lastLineIndex == -1)
				lastLineIndex = reconstructedTree.length();
		} else
			lastLineIndex = reconstructedTree.length();

		tree = reconstructedTree.substring(0, lastLineIndex);

		tree = FormattingUtils.escapeStringToHTML(tree);
		if (!originalTree) {
			tree = FormattingUtils.unEscapeDiffTags(tree);
			tree = FormattingUtils.formatTreeLines(tree);
		}

		return tree;
	}

	private static void formatOutputs(FormatOptions formatOptions) {

		ArrayList<TestCase.TestOutput> outputs = testCase.getOutputs();
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
		testCase.setOutputs(outputs);
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
		if (formatOptions.isRemoveMetaContent()) {
			textsToRemove.addAll(FormattingUtils.getSubStringsOf(majorityTree,
					"&lt;meta&gt;"));
		}
		if (formatOptions.isRemoveLinkContent()) {
			textsToRemove.addAll(FormattingUtils.getSubStringsOf(majorityTree,
					"&lt;link&gt;"));
		}
		return textsToRemove;
	}

}