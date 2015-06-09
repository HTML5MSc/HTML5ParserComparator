package com.HTML5.ParserComparer.model;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ReportGenerator {

	static String filePath = "A:\\GitHub\\HTML5ParserComparator\\report.xml";

	public static Report getReport() {
		Report report = null;
		Document document = XMLUtils.readXMLFromFile(filePath);
		if (document != null) {
			report = getGeneralData(document);
			report.setTestResults(getTestResults(document));
			report.setTests(getTests(document));
		}

		return report;
	}

	public static TestCase getTest(String name) {
		TestCase testCase = new TestCase();
		String xPathExpression = "/report/test[@name=\"" + name + "\"]";
		Document document = XMLUtils.readXMLFromFile(filePath);
		if (document != null) {
			NodeList nodes = XMLUtils.executeXPathExpression(document,
					xPathExpression);
			if (nodes != null && nodes.getLength() > 0) {
				Node testNode = nodes.item(0);
				String allEqual = testNode.getAttributes()
						.getNamedItem("allEqual").getNodeValue();
				testCase.setAllEqual(allEqual.equals("true"));
				testCase.setName(name);

				ArrayList<TestCase.TestOutput> outputs = new ArrayList<TestCase.TestOutput>();

				xPathExpression += "/output";
				NodeList outputNodes = XMLUtils.executeXPathExpression(
						document, xPathExpression);
				for (int i = 0; i < outputNodes.getLength(); i++) {
					Node outputNode = outputNodes.item(i);

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
					testOutput.setTree(treeNode.getTextContent());
					outputs.add(testOutput);

				}

				testCase.setOutputs(outputs);
			}
		}
		return testCase;
	}

	private static Report getGeneralData(Document document) {
		Report report = new Report();
		String xPathExpression = "/report/generalData";
		NodeList nodes = XMLUtils.executeXPathExpression(document,
				xPathExpression);
		for (int i = 0; i < nodes.getLength(); i++) {
			String testDate = nodes.item(i).getAttributes()
					.getNamedItem("date").getNodeValue();
			String numberOfTests = nodes.item(i).getAttributes()
					.getNamedItem("numberOftests").getNodeValue();
			String testsEqual = nodes.item(i).getAttributes()
					.getNamedItem("equals").getNodeValue();
			String testsDifferent = nodes.item(i).getAttributes()
					.getNamedItem("different").getNodeValue();

			report.setNumberOfTests(Integer.parseInt(numberOfTests));
			report.setTestsEqual(Integer.parseInt(testsEqual));
			report.setTestsDifferent(Integer.parseInt(testsDifferent));
			report.setTestDate(testDate);
		}
		return report;
	}

	private static ArrayList<TestResult> getTestResults(Document document) {
		ArrayList<TestResult> testResults = new ArrayList<TestResult>();
		String xPathExpression = "/report/testResult/parser";
		NodeList nodes = XMLUtils.executeXPathExpression(document,
				xPathExpression);
		for (int i = 0; i < nodes.getLength(); i++) {
			String parserName = nodes.item(i).getAttributes()
					.getNamedItem("name").getNodeValue();
			String passed = nodes.item(i).getAttributes()
					.getNamedItem("passed").getNodeValue();
			String failed = nodes.item(i).getAttributes()
					.getNamedItem("failed").getNodeValue();

			testResults.add(new TestResult(parserName,
					Integer.parseInt(passed), Integer.parseInt(failed)));
		}
		return testResults;
	}

	private static ArrayList<TestCase> getTests(Document document) {
		ArrayList<TestCase> testCases = new ArrayList<TestCase>();
		String xPathExpression = "/report/test";
		NodeList nodes = XMLUtils.executeXPathExpression(document,
				xPathExpression);
		for (int i = 0; i < nodes.getLength(); i++) {
			String name = nodes.item(i).getAttributes().getNamedItem("name")
					.getNodeValue();
			String allEqual = nodes.item(i).getAttributes()
					.getNamedItem("allEqual").getNodeValue();
			TestCase testCase = new TestCase();
			testCase.setAllEqual(allEqual.equals("true"));
			testCase.setName(name);
			testCases.add(testCase);
		}
		return testCases;
	}
}
