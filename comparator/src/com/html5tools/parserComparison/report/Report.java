package com.html5tools.parserComparison.report;

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
	
	public abstract void updateReport(String testName, List<OutputTree> trees,
			List<String> successfulParsers, String inputValue);

	public abstract void saveReportToFile(String reportFileName);
	
	protected void addAttribute(Node node, String attName, String attValue) {
		Attr attr = report.createAttribute(attName);
		attr.setNodeValue(attValue);
		node.getAttributes().setNamedItem(attr);
	}
	
	protected void incrementAttributeValue(Node node, String attName) {
		Node attr = node.getAttributes().getNamedItem(attName);
		String attValue = String
				.valueOf(Integer.parseInt(attr.getNodeValue()) + 1);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return node;
	}
}
