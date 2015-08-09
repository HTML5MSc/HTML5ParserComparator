package com.html5tools.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
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
			String attName, boolean attValue) {
		addAttribute(document, node, attName, attValue ? "true" : "false");
	}

	public static void addAttribute(Document document, Node node,
			String attName, int attValue) {
		addAttribute(document, node, attName, Integer.toString(attValue));
	}

	public static void addAttribute(Document document, Node node,
			String attName, String attValue) {
		Attr attr = document.createAttribute(attName);
		attr.setNodeValue(removeXMLInvalidChars(attValue));
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
		incrementAttributeValue(node, attName, 1);
	}

	public static void incrementAttributeValue(Node node, String attName,
			String attValue) {
		try {
			incrementAttributeValue(node, attName, Integer.decode(attValue));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void incrementAttributeValue(Node node, String attName,
			int value) {
		Node attr = node.getAttributes().getNamedItem(attName);
		String attValue = String.valueOf(Integer.parseInt(attr.getNodeValue())
				+ value);
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

	public static Element getElementByAttributeValue(List<Element> elements,
			String attName, String attValue) {
		for (Element e : elements) {
			String s = e.getAttribute(attName);
			if (attValue.equals(s))
				return e;
		}
		return null;
	}

	public static Object executeXPath(Document document, String expression,
			QName outputType) {
		Object result = null;
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile(expression);
			result = expr.evaluate(document, outputType);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
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

	public static String removeXMLInvalidChars(String input) {
		String xml10pattern = "[^" + "\u0009\r\n" + "\u0020-\uD7FF"
				+ "\uE000-\uFFFD" + "\ud800\udc00-\udbff\udfff" + "]";

		return input.replaceAll(xml10pattern, "");
	}
}