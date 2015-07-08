package com.html5tools.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

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

public class XMLUtils {

	public static Document readXMLFromFile(String fileName)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf;
		DocumentBuilder db;
		Document document;

		dbf = DocumentBuilderFactory.newInstance();
		db = dbf.newDocumentBuilder();

		File file = new File(fileName);
		InputStream inputStream = new FileInputStream(file);
		Reader reader = new InputStreamReader(inputStream, "UTF-8");
		InputSource is = new InputSource(reader);
		is.setEncoding("UTF-8");

		document = db.parse(is);

		return document;
	}

	public static Document createDocument() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Document document = null;
		try {
			db = dbf.newDocumentBuilder();
			document = db.newDocument();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return document;
	}

	public static Node addNode(Document document, Node baseNode,
			String newNodeName) {
		Node newNode = document.createElement(newNodeName);
		baseNode.appendChild(newNode);
		return newNode;
	}

	public static void addCDATA(Document document, Node baseNode, String value) {
		CDATASection cdataSection = document.createCDATASection(value);
		baseNode.appendChild(cdataSection);
	}

	public static void addAttribute(Document document, Node node,
			String attName, String attValue) {
		Attr attr = document.createAttribute(attName);
		attr.setNodeValue(attValue);
		node.getAttributes().setNamedItem(attr);
	}

	public static String getAttributeValue(Node node, String attName) {
		Node attr = node.getAttributes().getNamedItem(attName);
		return attr.getNodeValue();
	}

	public static void setAttributeValue(Node node, String attName,
			String attValue) {
		Node attr = node.getAttributes().getNamedItem(attName);
		attr.setNodeValue(attValue);
	}

	public static void incrementAttributeValue(Node node, String attName) {
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

	public static Element getFirstElementByTagName(Node node, String tagName) {
		ArrayList<Element> elements = getElementsByTagName(node, tagName);
		if (!elements.isEmpty())
			return elements.get(0);
		return null;
	}

	public static Node executeXPathExpression(Document document,
			String expression) {
		Node node = null;
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile(expression);
			node = (Node) expr.evaluate(document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return node;
	}

	public static void saveReportToFile(Node node, String documentFileName) {
		// StringWriter writer = new StringWriter();
		// StreamResult resultString = new StreamResult(writer);

		File output = new File(documentFileName);
		StreamResult resultFile = new StreamResult(output);

		DOMSource source = new DOMSource(node);
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
}