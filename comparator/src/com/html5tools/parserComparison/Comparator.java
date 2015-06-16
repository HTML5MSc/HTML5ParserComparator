package com.html5tools.parserComparison;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author JoseArmando
 */
public class Comparator {

	public String run(String[] args) throws Exception {

		if (args.length % 2 != 0)
			throw new Exception(
					"Missing argument. Arguments must be the name test plus a set of pairs of output and the parser");

		String[] parsers = getPasers(args);
		List<OutputTree> trees = groupByEquality(args);
		String reportFileName = args[1]; //"report.xml";

		Document report = createReport(args[0], parsers, trees,
				parseXmlFile(reportFileName));

		StringWriter writer = new StringWriter();
		StreamResult resultString = new StreamResult(writer);

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
			t.transform(source, resultString);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return writer.toString();
	}

	private Document parseXmlFile(String file) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			return db.parse(file);
		} catch (FileNotFoundException e) {
			System.out.println("File " + file
					+ " not found. Creating new file...");
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private String[] getPasers(String[] args) throws Exception {

		String[] parsers = new String[(args.length - 2) / 2];

		for (int i = 0; i < (args.length - 2) / 2; i = i + 1) {
			parsers[i] = args[(i * 2) + 3];
		}
		return parsers;

		// if (args.length % 2 != 0)
		// throw new Exception(
		// "Missing argument. Arguments must be a set of pairs of output and the parser");
		//
		// String[] parsers = new String[args.length / 2];
		//
		// for (int i = 0; i < args.length / 2; i = i + 1) {
		// parsers[i] = args[(i*2)+1];
		// }
		// return parsers;
	}

	private List<OutputTree> groupByEquality(String[] args) throws Exception {

		List<OutputTree> outputs = new ArrayList<OutputTree>();

		for (int i = 2; i < args.length; i = i + 2) {
			OutputTree tree = new OutputTree(args[i], args[i + 1]);

			if (!outputs.contains(tree))
				outputs.add(tree);
			else
				outputs.get(outputs.indexOf(tree)).addParser(args[i + 1]);

		}
		return outputs;

		// List<OutputTree> outputs = new ArrayList<OutputTree>();
		//
		// for (int i = 0; i < args.length; i = i + 2) {
		// OutputTree tree = new OutputTree(args[i], args[i + 1]);
		//
		// if(!outputs.contains(tree)) outputs.add(tree);
		// else outputs.get(outputs.indexOf(tree)).addParser(args[i + 1]);
		//
		// }
		// return outputs;
	}

	private Document createReport(String testName, String[] parserNames,
			List<OutputTree> data, Document report) {
		// Document report = null;

		try {

			Node root;

			if (report == null) {
				// Create new report node
				report = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder().newDocument();

				root = report.createElement("report");
				report.appendChild(root);

				appendTotalsTags(report, root);
				appendParserInfoTags(report, root, parserNames);

			} else {
				// Append to the existing report node
				root = report.getElementsByTagName("report").item(0);
			}

			Node totals = report.getElementsByTagName("generalData").item(0);

			// Add test to the counter
			Node totalTests = totals.getAttributes().getNamedItem(
					"numberOfTests");
			totalTests.setNodeValue(String.valueOf(Integer.parseInt(totalTests
					.getNodeValue()) + 1));

			Node test = report.createElement("test");
			root.appendChild(test);

			// test name attr
			Attr testNameNode = report.createAttribute("name");
			testNameNode.setNodeValue(testName);
			test.getAttributes().setNamedItem(testNameNode);

			// Number Of Trees attr
			Attr numberOfTrees = report.createAttribute("numberOfTrees");
			test.getAttributes().setNamedItem(numberOfTrees);

			numberOfTrees.setNodeValue(String.valueOf(data.size()));
			if (data.size() > 1) {
				// Add a difference to the counter
				Node counter = totals.getAttributes().getNamedItem("different");
				counter.setNodeValue(String.valueOf(Integer.parseInt(counter
						.getNodeValue()) + 1));
			} else {
				// Add an assert to the counter
				Node counter = totals.getAttributes().getNamedItem("equals");
				counter.setNodeValue(String.valueOf(Integer.parseInt(counter
						.getNodeValue()) + 1));
			}

			List<String> parsersInMajority = new ArrayList<String>();
			int majorityNumberOfAgreements = 0;
			Node majorityTree = null;
			for (OutputTree tree : data) {
				// System.out.println(tree);

				Node output = report.createElement("output");
				test.appendChild(output);

				Attr majority = report.createAttribute("majority");
				output.getAttributes().setNamedItem(majority);
				majority.setNodeValue("false");

				Node parsers = report.createElement("parsers");
				output.appendChild(parsers);

				if (tree.getParsers().size() > majorityNumberOfAgreements) {
					majorityNumberOfAgreements = tree.getParsers().size();
					parsersInMajority = tree.getParsers();
					majorityTree = output;
				} else if (tree.getParsers().size() == majorityNumberOfAgreements) {
					parsersInMajority = new ArrayList<String>(); // no majority
				}

				for (String parserName : tree.getParsers()) {
					Node parser = report.createElement("parser");
					parsers.appendChild(parser);

					Node parserNameNode = report.createAttribute("name");
					parserNameNode.setNodeValue(parserName);
					parser.getAttributes().setNamedItem(parserNameNode);

				}

				Node treeNode = report.createElement("tree");
				output.appendChild(treeNode);
				CDATASection outputTree = report.createCDATASection(tree
						.getTree());
				treeNode.appendChild(outputTree);
			}

			if (majorityTree != null)
				majorityTree.getAttributes().getNamedItem("majority")
						.setNodeValue("true");

			// Update results due to majority

			// is mayority? more than the half of total trees
			// TODO: CHECK, IF ACCORDING to boyer-moore majority vote algorithm
			// then uncomment
			// double halfNoOfPasers = parserNames.length / 2;
			// if(parsersInMajority.size() <= halfNoOfPasers){
			// parsersInMajority.clear();
			// }

			XPath xPath = XPathFactory.newInstance().newXPath();
			for (String parserName : parsersInMajority) {

				Node parserNodeInTestResult = (Node) xPath.compile(
						"testResult/*[@name='" + parserName + "']").evaluate(
						root, XPathConstants.NODE);
				// is mayority? more than the half of total trees
				Node passed = parserNodeInTestResult.getAttributes()
						.getNamedItem("passed");
				passed.setNodeValue(String.valueOf(Integer.parseInt(passed
						.getNodeValue()) + 1));
			}
			List<String> failingParsers = new ArrayList<String>(
					Arrays.asList(parserNames));
			failingParsers.removeAll(parsersInMajority);

			for (String parserName : failingParsers) {

				Node parserNodeInTestResult = (Node) xPath.compile(
						"testResult/*[@name='" + parserName + "']").evaluate(
						root, XPathConstants.NODE);
				// is mayority? more than the half of total trees
				Node failed = parserNodeInTestResult.getAttributes()
						.getNamedItem("failed");
				failed.setNodeValue(String.valueOf(Integer.parseInt(failed
						.getNodeValue()) + 1));
			}

		} catch (ParserConfigurationException | XPathExpressionException e) {
			e.printStackTrace();
		}
		return report;
	}

	private void appendTotalsTags(Document report, Node root) {

		Node totals;
		Attr totalTests, equals, diff;

		totals = report.createElement("generalData");
		root.appendChild(totals);

		totalTests = report.createAttribute("numberOfTests");
		totalTests.setNodeValue("0");
		equals = report.createAttribute("equals");
		equals.setNodeValue("0");
		diff = report.createAttribute("different");
		diff.setNodeValue("0");

		Attr date = report.createAttribute("date");
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date dateD = new Date();
		date.setNodeValue(dateFormat.format(dateD));

		totals.getAttributes().setNamedItem(totalTests);
		totals.getAttributes().setNamedItem(equals);
		totals.getAttributes().setNamedItem(diff);
		totals.getAttributes().setNamedItem(date);
	}

	private void appendParserInfoTags(Document report, Node root,
			String[] parsers) {

		Node testResult;

		testResult = report.createElement("testResult");
		root.appendChild(testResult);

		for (String parser : parsers) {
			Attr parseName, passed, failed;

			Node parserNode = report.createElement("parser");
			testResult.appendChild(parserNode);

			parseName = report.createAttribute("name");
			parseName.setNodeValue(parser);
			passed = report.createAttribute("passed");
			passed.setNodeValue("0");
			failed = report.createAttribute("failed");
			failed.setNodeValue("0");

			parserNode.getAttributes().setNamedItem(parseName);
			parserNode.getAttributes().setNamedItem(passed);
			parserNode.getAttributes().setNamedItem(failed);

		}
	}

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

}

class OutputTree {

	private String tree;
	private List<String> parsers;

	public OutputTree(String tree, String parser) {
		this.tree = tree;
		parsers = new ArrayList<String>();
		parsers.add(parser);
	}

	public void addParser(String parser) {
		parsers.add(parser);
	}

	public String getTree() {
		return tree;
	}

	public void setTree(String tree) {
		this.tree = tree;
	}

	public List<String> getParsers() {
		return parsers;
	}

	public void setParsers(List<String> parsers) {
		this.parsers = parsers;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(tree.toString() + "\n");
		for (String parser : parsers) {
			sb.append(parser.toString() + " ");
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tree == null) ? 0 : tree.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OutputTree other = (OutputTree) obj;
		if (tree == null) {
			if (other.tree != null)
				return false;
		} else if (!tree.equals(other.tree))
			return false;
		return true;
	}

}
