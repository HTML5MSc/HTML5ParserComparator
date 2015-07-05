package com.html5tools.parserComparison.report.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.html5tools.parserComparison.OutputTree;
import com.html5tools.parserComparison.report.Report;

public class PartitionedReport extends Report {
	
	//50 mb
	final static long PARTITION_DESIRED_SIZE =  5120000;
	
	List<Report> children;
	Report part;
	
	public PartitionedReport(List<String> parserNames) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			report = db.newDocument();
			this.parserNames = parserNames;
			addNode(report, "report");
			appendTotalsTags();
			appendParserInfoTags();
			appendSubReports();
			part = new SingleReport(parserNames);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public PartitionedReport(String fileName, List<String> parserNames)
			throws SAXException, IOException {
		DocumentBuilderFactory dbf;
		DocumentBuilder db;
		dbf = DocumentBuilderFactory.newInstance();
		try {
			db = dbf.newDocumentBuilder();
			File file = new File(fileName);
			InputStream inputStream = new FileInputStream(file);
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			report = db.parse(is);
			this.parserNames = parserNames;
			part = getPart(fileName);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}

	private Report getPart(String filename) {
		String partFileName = getPartFileName(filename);
		try {
			return new SingleReport(partFileName,parserNames);
		} catch (FileNotFoundException e) {
			return new SingleReport(parserNames);
		} catch (SAXException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void updateReport(String testName, List<OutputTree> trees,
			List<String> successfulParsers, String inputValue) {
		part.updateReport(testName, trees, successfulParsers, inputValue);
		
		Node totals = report.getElementsByTagName("generalData").item(0);
		
		incrementAttributeValue(totals, "numberOfTests");

		if (trees.size() == 1)
			incrementAttributeValue(totals, "equals");
		else
			incrementAttributeValue(totals, "different");

		updateTestResults(successfulParsers);
		
	}

	private void updateTestResults(List<String> successfulParsers) {

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
	
	@Override
	public void saveReportToFile(String reportFileName) {
		//Save part first
		
		String partFileName  = getPartFileName(reportFileName);
		part.saveReportToFile(partFileName);
		
		//append part reference into parent
		appendSubReport(partFileName, part.getReportDocument());
		
		//Save parent
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

	private String getPartFileName(String reportFileName) {
		
		String xPathExp = "/report/subreports/subreport[last()]";
		Node node = executeXPathExpression(xPathExp);
		
		//No parts, create new one
		if(node==null){
			return reportFileName.replaceFirst(".xml", "_part1.xml");
		}
		
		String partFileName = node.getAttributes().getNamedItem("file").getNodeValue();
		
		//Create new part if the previous is too big
		File partFile = new File(partFileName);
		if(partFile.length() > PARTITION_DESIRED_SIZE){
			int currentCount = new Integer(partFileName.substring(partFileName.lastIndexOf("_part")+5, partFileName.lastIndexOf(".xml")));
			return reportFileName.replaceFirst(".xml", "_part"+(currentCount+1)+".xml");
		}
		
		return partFileName;
		
		
	}
	
	private void appendSubReports() {
		Node root = report.getFirstChild();
		addNode(root, "subreports");
	}
	
	private void appendSubReport(String filename, Document partDoc) {

		Node subreports = report.getElementsByTagName("subreports").item(0);
		
		String xPathExp = "report/subreports/subreport[@file='"+filename+"']";
		Node subreport = executeXPathExpression(xPathExp);
		
		//No existing node, create new one
		if(subreport==null){
			subreport = addNode(subreports, "subreport");
			addAttribute(subreport, "file", filename);
		}else{
			//remove old content
			while(subreport.getChildNodes().getLength()>0){
				subreport.removeChild(subreport.getChildNodes().item(0));
			}
		}
		
		//add updated content
		Node gd = partDoc.getElementsByTagName("generalData").item(0);
		Node tr =partDoc.getElementsByTagName("testResult").item(0);
		
		gd = report.importNode(gd, true);
		tr = report.importNode(tr, true);
		
		subreport.appendChild(gd);
		subreport.appendChild(tr);

	}
	

}
