package com.html5libtester.jsoup;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jsoup.nodes.Node;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class JsoupParser_test {

	private String testName;
	private String test;

	// parameters pass via this constructor
	public JsoupParser_test(String testName, String test) {
		this.testName = testName;
		this.test = test;
	}

	// Declares parameters here
	@Parameters(name = "Test name: {0}")
	public static Iterable<Object[]> test1() {
		List<Object[]> testList = new ArrayList<Object[]>();

		String[] resources = {

				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests1.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests2.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests3.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests4.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests5.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests6.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests7.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests8.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests9.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests10.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests11.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests12.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests14.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests15.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests16.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests17.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests18.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests19.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests20.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests21.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests22.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests23.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests24.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests25.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests26.dat",

				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/adoption01.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/adoption02.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/comments01.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/doctype01.dat",
				// "https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/domjs-unsafe.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/entities01.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/entities02.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/foreign-fragment.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/html5test-com.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/inbody01.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/isindex.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/main-element.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/pending-spec-changes-plain-text-unsafe.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/pending-spec-changes.dat",
				// "https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/plain-text-unsafe.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/ruby.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/scriptdata01.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tables01.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/template.dat",

				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tests_innerHTML_1.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/tricky01.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/webkit01.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/webkit02.dat",

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

			// String resource = "C:\\Users\\JoséArmando\\Desktop\\test.txt";
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
				if (test.contains("#script-off"))
					continue;

				String testName = i + " (" + resource + ") "
						+ test.split("\\n")[0]; // i + "";
				testList.add(new Object[] { testName, test });
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

		// remove new line character if is the last character
		if (expected.lastIndexOf('\n') == expected.length() - 1) {
			expected = expected.substring(0, expected.length() - 1);
		}

		// System.out.println("*************** " + input);
		// System.out.println("******Expected " + expected);
		if (contextElement != null) {
			process_result(input, new JsoupParser().parseFragment(input),
					expected);
		} else {
			process_result(input, new JsoupParser().parse(input), expected);
		}

	}

	private void process_result(String input, Node element, String expected) {
		String result = new Html5libSerializer().dom2string(element);
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
}
