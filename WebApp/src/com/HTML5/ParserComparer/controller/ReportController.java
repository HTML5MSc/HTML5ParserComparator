package com.HTML5.ParserComparer.controller;

import java.io.IOException;
import java.io.InputStream;
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
import com.HTML5.ParserComparer.model.TracerInput;
import com.HTML5.ParserComparer.model.WebConfig;
import com.HTML5.ParserComparer.model.utils.FormattingUtils;
import com.HTML5.ParserComparer.model.utils.ProcessRunner;
import com.HTML5.ParserComparer.model.utils.RequestURL;
import com.html5dom.Constants;
import com.html5dom.Document;
import com.html5parser.parser.Parser;
import com.html5parser.tracer.Event;
import com.html5parser.tracer.Event.EventType;
import com.html5parser.tracer.Tracer;
import com.html5tools.Utils.IOUtils;

@Controller
public class ReportController {
	@Autowired
	WebConfig webConfig;

	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String hello(
			Model model,
			@RequestParam(value = "name", required = false, defaultValue = "World") String name) {
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

		return getTestCase(model, formatOptions, reportName, testName);
	}

	@RequestMapping(value = "/testdetails", method = RequestMethod.POST)
	public String formatTestOutputs(Model model,
			@ModelAttribute("formatOptions") FormatOptions formatOptions,
			@RequestParam(value = "reportName") String reportName,
			@RequestParam(value = "testName") String testName) {

		return getTestCase(model, formatOptions, reportName, testName);
	}

	private String getTestCase(Model model, FormatOptions formatOptions,
			String reportName, String testName) {
		TestCase testCase = TestCaseGenerator.getTestCase(webConfig
				.getReportPath().concat(reportName), testName, formatOptions);

		model.addAttribute("formatOptions", formatOptions);
		model.addAttribute("reportName", reportName);
		model.addAttribute("testName", testName);
		model.addAttribute("test", testCase);

		return "testdetails";
	}

	@RequestMapping(value = "/inputform", method = RequestMethod.GET)
	public String viewParserForm(Model model) {
		model.addAttribute("parserInput", new ParserInput());
		model.addAttribute("inputTypeList", getInputTypeList());

		return "inputform";
	}

	@RequestMapping(value = "/inputform", method = RequestMethod.POST)
	public String processParserForm(Model model,
			@ModelAttribute("parserInput") ParserInput parserInput) {
		// Map<String, Object> model) {
		// model.put("inputTypeList", inputTypeList);
		// // implement your own registration logic here...
		// // for testing purpose:
		// System.out.println("value: " + parserInput.getValue());
		return parse(parserInput, model);
	}

	@RequestMapping(value = "/traceform", method = RequestMethod.GET)
	public String getTracer(Model model) {
		Tracer tracer = new Tracer();
		TracerInput tracerInput = new TracerInput();
		tracerInput.setSections(getInitialSectionsToExclude());

		model.addAttribute("eventTypes", tracer.getEventTypes());
		model.addAttribute("sections", tracer.getSections());
		model.addAttribute("inputTypeList", getInputTypeList());
		model.addAttribute("output");
		model.addAttribute("tracer");
		model.addAttribute("tracerInput", tracerInput);

		return "traceform";
	}

	@RequestMapping(value = "/traceform", method = RequestMethod.POST)
	public String postTracer(Model model,
			@ModelAttribute("tracerInput") TracerInput tracerInput,
			@ModelAttribute("input") String input) {

		return trace(model, tracerInput);
	}

	private String parse(ParserInput parserInput, Model model) {
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
			e.printStackTrace();
			model.addAttribute("error", "The url could not be accessed.");
			return viewParserForm(model);
		}

		return testDetails(model,
				java.nio.file.Paths.get(reportName, "report.xml").toString(),
				null);
	}

	private String trace(Model model, TracerInput tracerInput) {

		Parser parser = new Parser(true, true);
		Document document = null;

		if (tracerInput.getType().equals("String"))
			document = parser.parse(tracerInput.getInput());
		else {
			try {
				InputStream is = RequestURL.getResponse(tracerInput.getInput());
				document = parser.parse(is);
			} catch (IOException e) {
				e.printStackTrace();
				model.addAttribute("error", "The url could not be accessed.");
				return getTracer(model);
			}
		}

		String html = document.getOuterHtml(true);
		Tracer tracer = parser.getParserContext().getTracer();

		ArrayList<EventType> excludeTypes = getTypesToExclude(tracerInput);
		ArrayList<String> excludeSections = getSectionsToExclude(tracerInput);

		tracer.setParseEvents(tracer.getParseEvents(excludeTypes,
				excludeSections));

		model.addAttribute("inputTypeList", getInputTypeList());
		model.addAttribute("tracerInput", tracerInput);
		model.addAttribute("output", FormattingUtils.escapeStringToHTML(html));
		model.addAttribute("tracer", tracer);
		model.addAttribute("eventTypes", tracer.getEventTypes());
		model.addAttribute("sections", tracer.getSections());
		model.addAttribute("algorithms", tracer.getAlgorithms());
		model.addAttribute("insertionModes", tracer.getInsertionModes());
		model.addAttribute("tokenizerStates", tracer.getTokenizerStates());

		addElementsDetailsToModel(model);

		return "traceform";
	}

	private List<String> getInputTypeList() {
		List<String> inputTypeList = new ArrayList<String>();
		inputTypeList.add("String");
		inputTypeList.add("URL");
		return inputTypeList;
	}

	private ArrayList<EventType> getTypesToExclude(TracerInput tracerInput) {
		ArrayList<EventType> excludeTypes = new ArrayList<Event.EventType>();

		if (tracerInput.getEventTypes() != null)
			for (String s : tracerInput.getEventTypes()) {
				switch (s) {
				case "Algorithm":
					excludeTypes.add(EventType.Algorithm);
					break;
				case "InsertionMode":
					excludeTypes.add(EventType.InsertionMode);
					break;
				case "ParseError":
					excludeTypes.add(EventType.ParseError);
					break;
				case "TokenizerState":
					excludeTypes.add(EventType.TokenizerState);
					break;
				}
			}

		return excludeTypes;
	}

	private ArrayList<String> getSectionsToExclude(TracerInput tracerInput) {
		ArrayList<String> excludeSections = new ArrayList<String>();
		if (tracerInput.getSections() != null)
			for (String s : tracerInput.getSections())
				excludeSections.add(s.split(" ")[0]);

		return excludeSections;
	}

	private String getReportName() {
		return Long.toString(System.currentTimeMillis());
	}

	private void addElementsDetailsToModel(Model model) {
		String specialElements = "HTML's "
				+ FormattingUtils.stringArrayToString(
						Constants.SPECIAL_HTML_ELEMENTS, ", ")
				+ " SVG's "
				+ FormattingUtils.stringArrayToString(
						Constants.SPECIAL_SVG_ELEMENTS, ", ")
				+ " MathML's "
				+ FormattingUtils.stringArrayToString(
						Constants.SPECIAL_MATHML_ELEMENTS, ", ");

		model.addAttribute("specialElements", specialElements);
		model.addAttribute("formattingElements", FormattingUtils
				.stringArrayToString(Constants.FORMATTING_ELEMENTS, ", "));
		model.addAttribute("html5FormElements", FormattingUtils
				.stringArrayToString(Constants.HTML5_FORM_ELEMENTS, ", "));
		model.addAttribute("html5GraphicElements", FormattingUtils
				.stringArrayToString(Constants.HTML5_GRAPHIC_ELEMENTS, ", "));
		model.addAttribute("html5MediaElements", FormattingUtils
				.stringArrayToString(Constants.HTML5_MEDIA_ELEMENTS, ", "));
		model.addAttribute("html5SemanticElements", FormattingUtils
				.stringArrayToString(
						Constants.HTML5_SEMANTIC_STRUCTURAL_ELEMENTS, ", "));
	}

	private ArrayList<String> getInitialSectionsToExclude() {
		ArrayList<String> excludeSections = new ArrayList<String>();
		excludeSections.add("8.2.3.2 - The stack of open elements");
		excludeSections
				.add("8.2.3.2_1 - Have an element target node in a specific scope");
		excludeSections.add("8.2.3.2_2 - Have a particular element in scope");
		excludeSections
				.add("8.2.3.2_3 - Have a particular element in list item scope");
		excludeSections
				.add("8.2.3.2_4 - Have a particular element in button scope");
		excludeSections
				.add("8.2.3.2_5 - Have a particular element in table scope");
		excludeSections
				.add("8.2.3.2_6 - Have a particular element in select scope");
		excludeSections.add("8.2.5 - Tree construction");
		excludeSections.add("8.2.5_1 - Tree construction dispatcher");
		excludeSections.add("8.2.5.1 - Creating and inserting nodes");
		excludeSections
				.add("8.2.5.1_1 - Appropriate place for inserting a node");
		excludeSections.add("8.2.5.1_4 - Insert an HTML element");
		excludeSections.add("8.2.5.1_5 - Adjust MathML attributes");
		excludeSections.add("8.2.5.1_6 - Adjust SVG attributes");
		excludeSections.add("8.2.5.1_7 - Adjust foreign attributes");
		excludeSections
				.add("8.2.5.2 - Parsing elements that contain only text");
		excludeSections
				.add("8.2.5.3 - Closing elements that have implied end tags");
		excludeSections.add("8.2.5.3_1 - Generate implied end tags");

		return excludeSections;
	}
}