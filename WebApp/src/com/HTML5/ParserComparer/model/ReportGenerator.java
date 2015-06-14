package com.HTML5.ParserComparer.model;

import java.util.ArrayList;

import org.w3c.dom.Document;
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
			report.setTestCases(getTestCases(document));
		}

		return report;
	}

	private static Report getGeneralData(Document document) {
		Report report = new Report();
		String xPathExpression = "/report/generalData";
		NodeList nodes = XMLUtils.executeXPathExpression(document,
				xPathExpression);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String testDate = node.getAttributes().getNamedItem("date")
					.getNodeValue();
			String numberOfTests = node.getAttributes()
					.getNamedItem("numberOfTests").getNodeValue();
			String testsEqual = node.getAttributes().getNamedItem("equals")
					.getNodeValue();
			String testsDifferent = node.getAttributes()
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
			Node node = nodes.item(i);
			String parserName = node.getAttributes().getNamedItem("name")
					.getNodeValue();
			String passed = node.getAttributes().getNamedItem("passed")
					.getNodeValue();
			String failed = node.getAttributes().getNamedItem("failed")
					.getNodeValue();
			testResults.add(new TestResult(parserName,
					Integer.parseInt(passed), Integer.parseInt(failed)));
		}
		return testResults;
	}

	private static ArrayList<TestCase> getTestCases(Document document) {
		ArrayList<TestCase> testCases = new ArrayList<TestCase>();
		String xPathExpression = "/report/test";
		NodeList nodes = XMLUtils.executeXPathExpression(document,
				xPathExpression);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String name = node.getAttributes().getNamedItem("name")
					.getNodeValue();
			String numberOfTrees = node.getAttributes()
					.getNamedItem("numberOfTrees").getNodeValue();
			TestCase testCase = new TestCase();
			testCase.setName(name);
			testCase.setNumberOfTrees(Integer.parseInt(numberOfTrees));
			testCases.add(testCase);
		}
		return testCases;
	}
}
