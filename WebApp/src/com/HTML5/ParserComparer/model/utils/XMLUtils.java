package com.HTML5.ParserComparer.model.utils;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLUtils {
	public static Document readXMLFromFile(String filePath) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		Document document = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			document = docBuilder.parse(filePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return document;
	}

	public static NodeList executeXPathExpression(Document document,
			String expression) {
		NodeList nodeList = null;
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile(expression);
			nodeList = (NodeList) expr.evaluate(document,
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nodeList;
	}

	public static ArrayList<Element> getElementsByTagName(Node node,
			String tagName) {
		ArrayList<Element> elements = new ArrayList<Element>();
		if (node.hasChildNodes())
			for (int i = 0; i < node.getChildNodes().getLength(); i++) {
				Node n = node.getChildNodes().item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE
						&& n.getNodeName().equals(tagName)) {
					elements.add((Element) n);
				}
			}
		return elements;
	}
	
	public static Element getFirstElementByTagName(Node node, String tagName) {
		ArrayList<Element> elements = getElementsByTagName(node, tagName);
		if (!elements.isEmpty())
			return elements.get(0);
		return null;
	}
}
