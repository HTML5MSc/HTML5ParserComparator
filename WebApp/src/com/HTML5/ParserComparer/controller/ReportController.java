package com.HTML5.ParserComparer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.HTML5.ParserComparer.model.Report;
import com.HTML5.ParserComparer.model.ReportGenerator;
import com.HTML5.ParserComparer.model.TestCase;
import com.HTML5.ParserComparer.model.TestCaseGenerator;

@Controller
public class ReportController {

	@RequestMapping("/hello")
	public String hello(
			@RequestParam(value = "name", required = false, defaultValue = "World") String name,
			Model model) {

		model.addAttribute("name", name);
		// returns the view name
		return "helloworld";

	}

	// @RequestMapping("/report")
	// public ModelAndView report() {
	//
	// // Report report = initializeReport();
	// Report report = ReportGenerator.generate();
	// return new ModelAndView("report", "report", report);
	// }

	@RequestMapping("/report")
	public String report(Model model) {

		// Report report = initializeReport();
		Report report = ReportGenerator.getReport();
		model.addAttribute("report", report);
		return "report";
	}

	// @RequestMapping("/testdetails")
	// public ModelAndView testDetails(
	// @RequestParam(value = "testName", required = false) String testName) {
	// // Test test = initializeTest();
	// Test test = ReportGenerator.getTest(testName);
	// return new ModelAndView("testdetails", "test", test);
	// }

	@RequestMapping("/testdetails")
	public String testDetails(Model model,
			@RequestParam(value = "testName", required = false) String testName) {
		// Test test = initializeTest();
		TestCase testCase = TestCaseGenerator.getTestCase(testName);
		model.addAttribute("test", testCase);
		return "testdetails";
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