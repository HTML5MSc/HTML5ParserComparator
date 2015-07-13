package com.HTML5.ParserComparer.controller;

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
		return "helloworld";

	}

	@RequestMapping("/report")
	public String report(
			Model model,
			@RequestParam(value = "reportName", required = true) String reportName) {
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

		String reportName = getReportName();
		String testName = webConfig.getReportPath() + reportName;
		String fileName = java.nio.file.Paths.get(testName, "input").toString();
		String inputText = parserInput.getValue();
		String inputType = parserInput.getTypeParameter();
		List<String> args = new ArrayList<String>();
		args.add(webConfig.getBashScriptFullPath());
		args.add(reportName);

		if (inputType.equals("-s")) {
			IOUtils.createDirectory(testName);
			IOUtils.saveFile(fileName, inputText);
			inputType = "-f";
			inputText = fileName;
		}
		args.add(inputType);
		args.add(inputText);

		try {
			ProcessRunner.run(args, webConfig.getBashScriptPath());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			model.addAttribute("error", "The url could not be accessed.");
			return viewParserForm(model);
		}

		return testDetails(model,
				java.nio.file.Paths.get(reportName, "/report.xml").toString(),
				null);
	}

	private String getReportName() {
		return Long.toString(System.currentTimeMillis());
	}
}