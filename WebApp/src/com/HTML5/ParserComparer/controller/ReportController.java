package com.HTML5.ParserComparer.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.HTML5.ParserComparer.model.ParserInput;
import com.HTML5.ParserComparer.model.ProcessRunner;
import com.HTML5.ParserComparer.model.Report;
import com.HTML5.ParserComparer.model.ReportGenerator;
import com.HTML5.ParserComparer.model.TestCase;
import com.HTML5.ParserComparer.model.TestCaseGenerator;
import com.HTML5.ParserComparer.model.WebConfig;

@Controller
public class ReportController {
	@Autowired
	WebConfig webConfig;

	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String hello(
			@RequestParam(value = "name", required = false, defaultValue = "World") String name,
			Model model) {

		model.addAttribute("name", name);
		// returns the view name
		return "helloworld";

	}

	@RequestMapping("/report")
	public String report(
			Model model,
			@RequestParam(value = "reportName", required = true) String reportName) {
		// Report report = initializeReport();
		Report report = ReportGenerator.getReport(webConfig.getReportPath()
				.concat(reportName));
		model.addAttribute("reportName", reportName);
		model.addAttribute("report", report);
		return "report";
	}


	@RequestMapping("/testdetails")
	public String testDetails(
			Model model,
			@RequestParam(value = "reportName", required = true) String reportName,
			@RequestParam(value = "testName", required = false) String testName) {
		// Test test = initializeTest();
		TestCase testCase = TestCaseGenerator.getTestCase(webConfig
				.getReportPath().concat(reportName), testName);
		model.addAttribute("reportName", reportName);
		model.addAttribute("test", testCase);
		return "testdetails";
	}

	@RequestMapping(value = "/inputform", method = RequestMethod.GET)
	public String viewParserForm(Model model) {
		ParserInput parserInput = new ParserInput();
		model.addAttribute("parserInput", parserInput);

		List<String> inputTypeList = new ArrayList<String>();
		inputTypeList.add("String");
		inputTypeList.add("URL");
		model.addAttribute("inputTypeList", inputTypeList);

		return "inputform";
	}

	@RequestMapping(value = "/inputform", method = RequestMethod.POST)
	public String processParserForm(
			@ModelAttribute("parserInput") ParserInput parserInput, Model model) {
		// Map<String, Object> model) {
		// model.put("inputTypeList", inputTypeList);
		// // implement your own registration logic here...
		// // for testing purpose:
		// System.out.println("value: " + parserInput.getValue());

		String reportName = "report".concat(
				Long.toString(System.currentTimeMillis())).concat(".xml");
		List<String> args = new ArrayList<String>();
		args.add(webConfig.getBashScriptFullPath());
		args.add("-n");
		args.add(webConfig.getReportPath().concat(reportName));
		args.add(parserInput.getTypeParameter());
		args.add(parserInput.getValue());
		try {
			ProcessRunner.run(args);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Report report = ReportGenerator.getReport(webConfig.getReportPath()
				.concat(reportName));
		model.addAttribute("reportName", reportName);
		model.addAttribute("report", report);
		return "report";
	}

	// private Report initializeReport() {
	// Report report = new Report();
	// report.setNumberOfTests(10);
	// report.setTestsEqual(8);
	// report.setTestsDifferent(2);
	// report.setTestDate(new Date().toString());
	//
	// ArrayList<TestResult> testResults = new ArrayList<>();
	// testResults.add(new TestResult("html5lib", 10, 0));
	// testResults.add(new TestResult("parse5", 9, 1));
	// testResults.add(new TestResult("jsoup", 6, 4));
	// report.setTestResults(testResults);
	// return report;
	// }
	//
	// private Test initializeTest() {
	// Test test = new Test();
	// test.setName("abcd-1");
	// test.setAllEqual(false);
	// ArrayList<TestOutput> outputs = new ArrayList<TestOutput>();
	// ArrayList<String> parser1 = new ArrayList<String>();
	// ArrayList<String> parser2 = new ArrayList<String>();
	// parser1.add("jsoup");
	// parser1.add("parse5");
	// parser2.add("html5lib");
	// TestOutput output1 = new TestOutput();
	// output1.setParsers(parser1);
	// output1.setTree("| tree \n|   one");
	// TestOutput output2 = new TestOutput();
	// output2.setParsers(parser2);
	// output2.setTree("| tree \n|   1one");
	// outputs.add(output1);
	// outputs.add(output2);
	// test.setOutputs(outputs);
	// return test;
	// }
}