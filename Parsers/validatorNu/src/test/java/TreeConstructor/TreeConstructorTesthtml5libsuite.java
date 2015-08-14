package TreeConstructor;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.html5tools.adapters.validatorNU.HtmlLibSerializer;

/* HTML5LIB FORMAT example
 * 
 * 

 #test
 <p>One<p>Two
 #errors
 3: Missing document type declaration
 #document
 | <html>
 |   <head>
 |   <body>
 |     <p>
 |       "One"
 |     <p>
 |       "Two"

 */

@RunWith(value = Parameterized.class)
public class TreeConstructorTesthtml5libsuite {

	private static List<String> ignoredTests;
	private static boolean ignoreTests = true;

	private String testName;
	private String test;
	private boolean scriptFlag;

	// parameters pass via this constructor
	public TreeConstructorTesthtml5libsuite(String testName, String test,
			boolean scriptFlag) {
		this.testName = testName;
		this.test = test;
		this.scriptFlag = scriptFlag;
	}

	// Declares parameters here
	@Parameters(name = "Test name: {0}")
	public static Iterable<Object[]> test1() {

		if (ignoreTests)
			ignoreTests();

		List<Object[]> testList = new ArrayList<Object[]>();

		String[] resources = {

				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/entities01.dat",

		};

		for (String resource : resources) {
			testList = addTestFile(testList, resource);
		}

		return testList;
	}

	private static List<Object[]> addTestFile(List<Object[]> testList,
			String resource) {
		BufferedReader in = null;
		Scanner scanner = null;
		URL url;
		try {
			url = new URL(resource);
			in = new BufferedReader(new InputStreamReader(url.openStream()));

			// resource = "C:\\Users\\JoséArmando\\Desktop\\domjs-unsafe.dat";
			// in = new BufferedReader(new FileReader(new File(resource)));

			scanner = new Scanner(in);
			String testFile = scanner.useDelimiter("\\A").next();
			String[] tests = testFile.split("(^|\n\n)#data\n");

			resource = resource.split("/")[resource.split("/").length - 1];
			for (int i = 1; i < tests.length; i++) {
				String test = tests[i];
				/*
				 * Omit the tests that have script-off. Check tests16.dat
				 */
				boolean scriptFlag = false;
				if (test.contains("#script-on"))
					scriptFlag = true;

				int inputFinish = test.indexOf("\n#errors\n");
				String input = "";
				if (inputFinish != -1)
					input = test.substring(0, inputFinish);

				// The replacement of the return line char is because the Junit
				// Parameterized
				// does not work properly is the char is set in a parameter
				// (testName)
				String testName = i + " (" + resource + ") "
						+ input.replace("\n", "(EOL)").replace("\r", "(EOL)");

				if (ignoreTests && ignoredTests.contains(testName))
					continue;
				testList.add(new Object[] { testName, test, scriptFlag });
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
				if (scanner != null)
					scanner.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return testList;
	}

	@Test
	public final void tests() {
		String contextElement = null;

		System.out.println("*************** " + testName);

		int errorsStart = test.indexOf("\n#errors\n");
		if (errorsStart != -1) {
			String input = test.substring(0, errorsStart);
			int fragmentStart = test.indexOf("\n#document-fragment\n");
			int domStart = test.indexOf("\n#document\n");
			if (fragmentStart != -1) {
				contextElement = test.substring(fragmentStart + 20, domStart);
			}
			if (domStart != -1) {
				String dom = test.substring(domStart + 11);
				if (dom.substring(dom.length() - 1) == "\n") {
					dom = dom.substring(0, dom.length() - 1);
				}
				run_test(input, contextElement, dom);
				return;
			}
		}
		System.out.println("Invalid test: " + test);

	}

	private void run_test(String input, String contextElement, String expected) {
		String result = "";
		// remove new line character if is the last character
		if (expected.lastIndexOf('\n') == expected.length() - 1) {
			expected = expected.substring(0, expected.length() - 1);
		}

		Writer out = new StringWriter();
		InputStream is;
		try {
			is = new ByteArrayInputStream(input.getBytes());

			ContentHandler serializer = new HtmlLibSerializer(out);
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALLOW);

			parser.setContentHandler(serializer);
			parser.setProperty("http://xml.org/sax/properties/lexical-handler",
					serializer);
			parser.setScriptingEnabled(scriptFlag);
			InputSource in = new InputSource(is);
			in.setEncoding("UTF-8");
			if (contextElement != null) {
				System.out
						.println("*************** Parsing HTML Fragment with Context Element:"
								+ contextElement);
				contextElement = contextElement.trim();
				String namespace = "";
				String name = contextElement;
				if (!contextElement.contains(" "))
					namespace = "http://www.w3.org/1999/xhtml";
				else {
					String[] contextSplit = contextElement.split(" ");
					name = contextSplit[1];

					switch (contextSplit[0]) {
					case "math":
						namespace = "http://www.w3.org/1998/Math/MathML";
						break;
					case "svg":
						namespace = "http://www.w3.org/2000/svg";
						break;
					}

				}
				parser.parseFragment(in, name, namespace);
				serializer.endDocument();
			} else {

				parser.parse(in);
			}
			out.flush();
			result = out.toString();
			result = result.substring(0, result.length() - 1);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (SAXNotRecognizedException e) {
			e.printStackTrace();
		} catch (SAXNotSupportedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		process_result(input, result, expected);

	}

	private void process_result(String input, String result, String expected) {
		System.out.println();
		System.out.println("****************** Input: " + input
				+ "  ******************");
		System.out.println(input);
		System.out.println("*******************");
		System.out.println();
		System.out.println();
		System.out.println("****************** Expected: " + testName
				+ "  ******************");
		System.out.println(expected);
		System.out.println("*******************");
		System.out.println();
		System.out.println();
		System.out.println("****************** Result: " + testName
				+ "  ******************");
		System.out.println(result);
		System.out.println("*******************");
		System.out.println();
		assertEquals("TEST FAILED", expected, result);

	}

	private static void ignoreTests() {
		ignoredTests = new ArrayList<String>();

		// invalid element name (<)
		// ignoredTests.add("1 (html5test-com.dat) <div<div>");
		// invalid element name (:)
		// ignoredTests.add("14 (webkit01.dat) <rdar://problem/6869687>");
		// different specs - "main" element
		ignoredTests
				.add("3 (main-element.dat) <!DOCTYPE html>xxx<svg><x><g><a><main><b>");
		// different specs - "rtc" element
		ignoredTests.add("14 (ruby.dat) <html><ruby>a<rtc>b<rp></ruby></html>");
		// different specs - Special HTML elements "menu", "menuitem"
		ignoredTests.add("78 (template.dat) <body><template><i><menu>Foo</i>");
		// different specs - adoption agengy algorithm
		ignoredTests.add("54 (tests1.dat) <b id=a><p><b id=b></p></b>TEST");
	}
}
