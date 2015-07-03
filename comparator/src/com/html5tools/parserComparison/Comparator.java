package com.html5tools.parserComparison;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import com.html5tools.parserComparison.report.Report;
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
		List<String> successfulParsers = getParsersInMajority(trees);

		run(reportFileName, testName, trees, successfulParsers);

	}

	private void run(String reportFileName, String testName,
			List<OutputTree> trees, List<String> successfulParsers) {
		saveToReport(reportFileName, testName, trees, successfulParsers, "");
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
		List<String> successfulParsers = getParsersInMajority(trees);
		saveToReport(reportFileName, testName, trees, successfulParsers, input);
	}

	private List<String> getParsersInMajority(List<OutputTree> trees) {
		// The tree is ordered so the majority parsers are of those from the
		// first tree
		List<String> parsersInMajority = trees.get(0).getParsers();

		// Update results due to majority

		// is majority? more than the half of total trees
		// ACCORDING to boyer-moore majority vote algorithm
		double halfNoOfPasers = parserNames.size() / 2;
		if (parsersInMajority.size() <= halfNoOfPasers) {
			return new ArrayList<String>();//No parser passed
		}
		return parsersInMajority;
	}

	private void saveToReport(String reportFileName, String testName,
			List<OutputTree> trees, List<String> successfulParsers, String input) {
		try {
			report = new SingleReport(reportFileName,parserNames);
		} catch (FileNotFoundException e) {
			report = new SingleReport(parserNames);
		} catch (SAXException | IOException e) {
			e.printStackTrace();
			return;// Do not continue
		}

		report.updateReport(testName, trees, successfulParsers, input);

		report.saveReportToFile(reportFileName);
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

}
