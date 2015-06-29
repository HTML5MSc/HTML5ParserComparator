package com.html5tools.parserComparison;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.html5tools.parserComparison.diff_match_patch.Diff;
import com.html5tools.parserComparison.diff_match_patch.Operation;

/**
 *
 * @author JoseArmando
 */
public class Comparator {


	Document report;
	List<String> parserNames;

	/**
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Comparator comparator = new Comparator();
		comparator.run(args);
		// System.out.println(comparator.run(args));

	}

	private final static Logger LOG = Logger.getLogger(Comparator.class
			.getName());

	public void run(String testName, String reportFileName,
			Map<String, String> outputTrees) {

		List<OutputTree> trees = new ArrayList<OutputTree>();
		parserNames = new ArrayList<String>();

		for (String parserName : outputTrees.keySet()) {
			trees.add(new OutputTree(outputTrees.get(parserName), parserName));
			parserNames.add(parserName);
		}

		trees = groupByEquality(trees);

		run(reportFileName, testName, trees);

	}

	public void run(String[] args) throws Exception {

		if (args.length != 1)
			throw new Exception(
					"Missing argument. Argument must be the path to an xml file.");

		Document inputData = getDocument(args[0]);
		Node root = inputData.getFirstChild();
		String testName = root.getAttributes().getNamedItem("name")
				.getNodeValue();
		String reportFileName = root.getAttributes().getNamedItem("report")
				.getNodeValue();

		String input = "";
		ArrayList<Element> elements = getElementsByTagName(root, "input");
		if (!elements.isEmpty())
			input = elements.get(0).getTextContent();

		parserNames = getParserNames(root);
		List<OutputTree> trees = groupByEquality(root);

		run(reportFileName, testName, trees, input);
	}

	private void run(String reportFileName, String testName,
			List<OutputTree> trees) {
		run(reportFileName, testName, trees, "");
	}

	private void run(String reportFileName, String testName,
			List<OutputTree> trees, String input) {
		try {
			report = getDocument(reportFileName);
		} catch (Exception e) {
			createReport();
		}

		updateReport(testName, trees, input);

		saveReportToFile(reportFileName);
	}

	private List<String> getParserNames(Node node) {

		List<String> parsers = new ArrayList<String>();
		for (Element element : getElementsByTagName(node, "tree"))
			parsers.add(element.getAttributes().getNamedItem("parser")
					.getNodeValue());

		return parsers;
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

	private List<OutputTree> groupByEquality(Node node) {

		List<OutputTree> outputs = new ArrayList<OutputTree>();
		for (Element element : getElementsByTagName(node, "tree")) {
			String parser = element.getAttributes().getNamedItem("parser")
					.getNodeValue();
			String tree = element.getTextContent();
			OutputTree outputTree = new OutputTree(tree, parser);
			if (!outputs.contains(outputTree))
				outputs.add(outputTree);
			else
				outputs.get(outputs.indexOf(outputTree)).addParser(parser);
		}

		Collections.sort(outputs);
		return outputs;
	}

	private Document getDocument(String fileName)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf;
		DocumentBuilder db;
		Document document;

		dbf = DocumentBuilderFactory.newInstance();
		db = dbf.newDocumentBuilder();
		// document = db.parse(new File(fileName));

		File file = new File(fileName);
		InputStream inputStream = new FileInputStream(file);
		Reader reader = new InputStreamReader(inputStream, "UTF-8");
		InputSource is = new InputSource(reader);
		is.setEncoding("UTF-8");

		document = db.parse(is);

		return document;
	}

	private void createReport() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			report = db.newDocument();
			addNode(report, "report");
			appendTotalsTags();
			appendParserInfoTags();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void appendTotalsTags() {

		Node root = report.getFirstChild();
		Node totals = addNode(root, "generalData");

		addAttribute(totals, "numberOfTests", "0");
		addAttribute(totals, "equals", "0");
		addAttribute(totals, "different", "0");
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		addAttribute(totals, "date", dateFormat.format(new Date()));
	}

	private void appendParserInfoTags() {

		Node root = report.getFirstChild();
		Node testResult = addNode(root, "testResult");

		for (String parser : parserNames) {
			Node parserNode = addNode(testResult, "parser");

			addAttribute(parserNode, "name", parser);
			addAttribute(parserNode, "passed", "0");
			addAttribute(parserNode, "failed", "0");
		}
	}

	private void updateReport(String testName, List<OutputTree> trees,
			String inputValue) {

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

		List<String> parsersInMajority = trees.get(0).getParsers();
		updateTestResults(parsersInMajority);
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

	private void updateTestResults(List<String> parsersInMajority) {
		// Update results due to majority

		// is mayority? more than the half of total trees
		// TODO: CHECK, IF ACCORDING to boyer-moore majority vote algorithm
		// then uncomment
		double halfNoOfPasers = parserNames.size() / 2;
		if (parsersInMajority.size() <= halfNoOfPasers) {
			parsersInMajority.clear();
		}

		for (String parserName : parsersInMajority) {

			String xPathExp = "/report/testResult/*[@name='" + parserName
					+ "']";
			Node parserNodeInTestResult = executeXPathExpression(xPathExp);
			// is mayority? more than the half of total trees
			if (parserNodeInTestResult != null)
				incrementAttributeValue(parserNodeInTestResult, "passed");
		}
		List<String> failingParsers = parserNames;
		failingParsers.removeAll(parsersInMajority);

		for (String parserName : failingParsers) {

			String xPathExp = "/report/testResult/*[@name='" + parserName
					+ "']";
			Node parserNodeInTestResult = executeXPathExpression(xPathExp);
			// is mayority? more than the half of total trees
			if (parserNodeInTestResult != null)
				incrementAttributeValue(parserNodeInTestResult, "failed");
		}
	}

	private void saveReportToFile(String reportFileName) {
		// StringWriter writer = new StringWriter();
		// StreamResult resultString = new StreamResult(writer);

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
		// return writer.toString();
	}

	private Node addNode(Node baseNode, String newNodeName) {
		Node newNode = report.createElement(newNodeName);
		baseNode.appendChild(newNode);
		return newNode;
	}

	private void addCDATA(Node baseNode, String value) {
		CDATASection cdataSection = report.createCDATASection(value);
		baseNode.appendChild(cdataSection);
	}

	private void addAttribute(Node node, String attName, String attValue) {
		Attr attr = report.createAttribute(attName);
		attr.setNodeValue(attValue);
		node.getAttributes().setNamedItem(attr);
	}

	private void incrementAttributeValue(Node node, String attName) {
		Node attr = node.getAttributes().getNamedItem(attName);
		String attValue = String
				.valueOf(Integer.parseInt(attr.getNodeValue()) + 1);
		attr.setNodeValue(attValue);
	}

	public static ArrayList<Element> getElementsByTagName(Node node,
			String tagName) {
		ArrayList<Element> elements = new ArrayList<Element>();
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			Node n = node.getChildNodes().item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE
					&& n.getNodeName().equals(tagName)) {
				elements.add((Element) n);
			}
		}
		return elements;
	}

	private Node executeXPathExpression(String expression) {
		Node node = null;
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile(expression);
			node = (Node) expr.evaluate(report, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return node;
	}
}
