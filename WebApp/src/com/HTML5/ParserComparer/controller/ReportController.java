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

import com.HTML5.ParserComparer.model.FormatOptions;
import com.HTML5.ParserComparer.model.ParserInput;
import com.HTML5.ParserComparer.model.Report;
import com.HTML5.ParserComparer.model.ReportGenerator;
import com.HTML5.ParserComparer.model.TestCase;
import com.HTML5.ParserComparer.model.TestCaseGenerator;
import com.HTML5.ParserComparer.model.WebConfig;
import com.HTML5.ParserComparer.model.utils.ProcessRunner;
import com.html5tools.Utils.IOUtils;

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

	@RequestMapping(value = "/testdetails", method = RequestMethod.GET)
	public String testDetails(
			Model model,
			@RequestParam(value = "reportName", required = true) String reportName,
			@RequestParam(value = "testName", required = false) String testName) {
		// Test test = initializeTest();
		FormatOptions formatOptions = new FormatOptions();
		TestCase testCase = TestCaseGenerator.getTestCase(webConfig
				.getReportPath().concat(reportName), testName, formatOptions);
		model.addAttribute(formatOptions);
		model.addAttribute("reportName", reportName);
		model.addAttribute("testName", testName);
		model.addAttribute("test", testCase);
		return "testdetails";
	}

	@RequestMapping(value = "/testdetails", method = RequestMethod.POST)
	public String formatTestOutputs(
			@ModelAttribute("formatOptions") FormatOptions formatOptions,
			Model model, @RequestParam(value = "reportName") String reportName,
			@RequestParam(value = "testName") String testName) {

		TestCase testCase = TestCaseGenerator.getTestCase(webConfig
				.getReportPath().concat(reportName), testName, formatOptions);
		model.addAttribute(formatOptions);
		model.addAttribute("reportName", reportName);
		model.addAttribute("testName", testName);
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

		String fileName = null;
		String reportName = getReportName();
		String testName = "test".concat(Long.toString(System
				.currentTimeMillis()));
		List<String> args = new ArrayList<String>();
		args.add(webConfig.getBashScriptFullPath());
		args.add(testName);
		args.add(webConfig.getReportPath().concat(reportName));

		if (parserInput.getTypeParameter().equals("-s")) {
			args.add("-f");
			fileName = "input"
					.concat(Long.toString(System.currentTimeMillis())).concat(
							".txt");
			String inputText = parserInput.getValue().replace("]]>", "]] >");
			IOUtils.saveFile(webConfig.getBashScriptPath().concat(fileName),
					inputText);
			args.add(fileName);
		} else {
			testName = parserInput.getValue();
			args.add(parserInput.getTypeParameter());
			args.add(parserInput.getValue());
		}

		try {
			ProcessRunner.run(args, webConfig.getBashScriptPath());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			model.addAttribute("error", "The url could not be accessed.");
			return viewParserForm(model);
		} finally {
			if (fileName != null)
				try {
					IOUtils.deleteFile(webConfig.getBashScriptPath().concat(
							fileName));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		return testDetails(model, reportName, testName);
	}

	private String getReportName() {
		return "reportString.xml";
		// "report".concat(
		// Long.toString(System.currentTimeMillis())).concat(".xml");
	}
}