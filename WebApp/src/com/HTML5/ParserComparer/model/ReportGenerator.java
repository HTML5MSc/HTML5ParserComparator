package com.HTML5.ParserComparer.model;

import java.util.ArrayList;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.html5tools.Utils.XMLUtils;

public class ReportGenerator {

	public static Report getReport(String filePath) {
		Report report = null;
		Document document = null;
		try {
			document = XMLUtils.readXMLFromFile(filePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		Node node = (Node) XMLUtils.executeXPath(document, xPathExpression,
				XPathConstants.NODE);

		String testDate = XMLUtils.getAttributeValue(node, "date");
		String numberOfTests = XMLUtils
				.getAttributeValue(node, "numberOfTests");
		String testsEqual = XMLUtils.getAttributeValue(node, "equals");
		String testsDifferent = XMLUtils.getAttributeValue(node, "different");

		report.setNumberOfTests(Integer.parseInt(numberOfTests));
		report.setTestsEqual(Integer.parseInt(testsEqual));
		report.setTestsDifferent(Integer.parseInt(testsDifferent));
		report.setTestDate(testDate);

		return report;
	}

	private static ArrayList<TestResult> getTestResults(Document document) {
		ArrayList<TestResult> testResults = new ArrayList<TestResult>();
		String xPathExpression = "/report/testResult/parser";
		NodeList nodes = (NodeList) XMLUtils.executeXPath(document,
				xPathExpression, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String parserName = XMLUtils.getAttributeValue(node, "name");
			String passed = XMLUtils.getAttributeValue(node, "passed");
			String failed = XMLUtils.getAttributeValue(node, "failed");
			testResults.add(new TestResult(parserName,
					Integer.parseInt(passed), Integer.parseInt(failed)));
		}
		return testResults;
	}

	private static ArrayList<TestCase> getTestCases(Document document) {
		ArrayList<TestCase> testCases = new ArrayList<TestCase>();
		String xPathExpression = "/report/test";
		NodeList nodes = (NodeList) XMLUtils.executeXPath(document,
				xPathExpression, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String name = XMLUtils.getAttributeValue(node, "name");
			String numberOfTrees = XMLUtils.getAttributeValue(node,
					"numberOfTrees");
			TestCase testCase = new TestCase();
			testCase.setName(name);
			testCase.setNumberOfTrees(Integer.parseInt(numberOfTrees));
			testCases.add(testCase);
		}
		return testCases;
	}
}
