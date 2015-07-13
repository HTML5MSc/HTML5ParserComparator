package com.html5tools.parserComparison.report.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.html5tools.Utils.DiffUtils;
import com.html5tools.Utils.IOUtils;
import com.html5tools.Utils.XMLUtils;
import com.html5tools.parserComparison.OutputTree;
import com.html5tools.parserComparison.report.Report;

public class SingleReport extends Report {

	public SingleReport(List<String> parserNames) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			report = db.newDocument();
			this.parserNames = parserNames;
			addNode(report, "report");
			appendTotalsTags();
			appendParserInfoTags();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public SingleReport(String fileName, List<String> parserNames)
			throws SAXException, IOException {
		DocumentBuilderFactory dbf;
		DocumentBuilder db;
		dbf = DocumentBuilderFactory.newInstance();
		try {
			db = dbf.newDocumentBuilder();
			// document = db.parse(new File(fileName));

			File file = new File(fileName);
			InputStream inputStream = new FileInputStream(file);
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			report = db.parse(is);
			this.parserNames = parserNames;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}

	public void updateReport(String folderPath, String testName,
			List<OutputTree> trees) {
		Node root = report.getElementsByTagName("report").item(0);
		Node totals = report.getElementsByTagName("generalData").item(0);
		incrementAttributeValue(totals, "numberOfTests");

		Node test = addNode(root, "test");
		addAttribute(test, "name", testName);
		addAttribute(test, "numberOfTrees", String.valueOf(trees.size()));

		if (trees.size() == 1)
			incrementAttributeValue(totals, "equals");
		else
			incrementAttributeValue(totals, "different");

		String majorityTree = null;
		for (int i = 0; i < trees.size(); i++) {
			OutputTree tree = trees.get(i);

			Node output = XMLUtils.addNode(report, test, "output");

			Node parsers = XMLUtils.addNode(report, output, "parsers");
			for (String parserName : tree.getParsers()) {
				Node parser = XMLUtils.addNode(report, parsers, "parser");
				XMLUtils.addAttribute(report, parser, "name", parserName);
			}

			String fileName;
			String content;
			// Because the list is ordered, the first tree represents the
			// majority
			if (i == 0) {
				majorityTree = tree.getTree();
				fileName = "majority";
				content = majorityTree;
				XMLUtils.addAttribute(report, output, "majority", "true");
			} else {
				fileName = "diff" + String.valueOf(i);
				content = DiffUtils.getFormattedDiffs(majorityTree,
						tree.getTree());
				XMLUtils.addAttribute(report, output, "majority", "false");
			}
			XMLUtils.addAttribute(report, output, "fileName", fileName);
			Path filePath = Paths.get(folderPath, fileName);
			IOUtils.saveFile(filePath.toString(), content);
		}

	}

	public void saveReportToFile(String reportFileName) {

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
			// t.transform(source, resultString);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

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

}
