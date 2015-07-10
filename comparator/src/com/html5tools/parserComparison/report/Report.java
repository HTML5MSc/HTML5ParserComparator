package com.html5tools.parserComparison.report;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.html5tools.parserComparison.OutputTree;

public abstract class Report {
	
	protected Document report;
	protected List<String> parserNames;
	
	protected Report(){};
	
	public abstract void updateReport(String folderPath, String testName,
			List<OutputTree> trees);	

	public abstract void saveReportToFile(String reportFileName);
	
	public void updateTestResults(List<String> successfulParsers) {

		for (String parserName : successfulParsers) {

			String xPathExp = "/report/testResult/*[@name='" + parserName
					+ "']";
			Node parserNodeInTestResult = executeXPathExpression(xPathExp);
			if (parserNodeInTestResult != null)
				incrementAttributeValue(parserNodeInTestResult, "passed");
		}
		List<String> failingParsers = new ArrayList<String>(parserNames);
		failingParsers.removeAll(successfulParsers);

		for (String parserName : failingParsers) {

			String xPathExp = "/report/testResult/*[@name='" + parserName
					+ "']";
			Node parserNodeInTestResult = executeXPathExpression(xPathExp);
			if (parserNodeInTestResult != null)
				incrementAttributeValue(parserNodeInTestResult, "failed");
		}
	}
	
	
	protected void addAttribute(Node node, String attName, String attValue) {
		Attr attr = report.createAttribute(attName);
		attr.setNodeValue(attValue);
		node.getAttributes().setNamedItem(attr);
	}
	
	protected void incrementAttributeValue(Node node, String attName) {
		incrementAttributeValue(node, attName, 1);
	}
	
	protected void incrementAttributeValue(Node node, String attName, int value) {
		Node attr = node.getAttributes().getNamedItem(attName);
		String attValue = String
				.valueOf(Integer.parseInt(attr.getNodeValue()) + value);
		attr.setNodeValue(attValue);
	}
	
	protected Node addNode(Node baseNode, String newNodeName) {
		Node newNode = report.createElement(newNodeName);
		baseNode.appendChild(newNode);
		return newNode;
	}
	
	protected void addCDATA(Node baseNode, String value) {
		CDATASection cdataSection = report.createCDATASection(value);
		baseNode.appendChild(cdataSection);
	}
	
	protected Node executeXPathExpression(String expression) {
		Node node = null;
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile(expression);
			node = (Node) expr.evaluate(report, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return node;
	}
	
	protected void appendTotalsTags() {

		Node root = report.getFirstChild();
		Node totals = addNode(root, "generalData");

		addAttribute(totals, "numberOfTests", "0");
		addAttribute(totals, "equals", "0");
		addAttribute(totals, "different", "0");
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		addAttribute(totals, "date", dateFormat.format(new Date()));
	}

	protected void appendParserInfoTags() {

		Node root = report.getFirstChild();
		Node testResult = addNode(root, "testResult");

		for (String parser : parserNames) {
			Node parserNode = addNode(testResult, "parser");

			addAttribute(parserNode, "name", parser);
			addAttribute(parserNode, "passed", "0");
			addAttribute(parserNode, "failed", "0");
		}
	}
	
	public Document getReportDocument(){
		return report;
	}
}
