package com.html5libtester.jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;

public class Html5libTestSaver {
	public static void main(String[] args) {

		String path = "C:\\Users\\hs012\\Desktop\\html5libTests";
		if (!IOFunctions.createDirectory(path))
			return;

		getTestFile(path);
	}

	private static void getTestFile(String path) {
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
				//
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/domjs-unsafe.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/entities01.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/entities02.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/foreign-fragment.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/html5test-com.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/inbody01.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/isindex.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/main-element.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/pending-spec-changes-plain-text-unsafe.dat",
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/pending-spec-changes.dat",
				//
				"https://raw.githubusercontent.com/html5lib/html5lib-tests/master/tree-construction/plain-text-unsafe.dat",
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
			String dataPath = path
					+ "\\data\\"
					+ resource.substring(resource.lastIndexOf("/") + 1)
							.replace(".dat", "");
			String expectedPath = path
					+ "\\expected\\"
					+ resource.substring(resource.lastIndexOf("/") + 1)
							.replace(".dat", "");
			IOFunctions.createDirectory(dataPath);
			IOFunctions.createDirectory(expectedPath);
			saveTestFile(dataPath, expectedPath, resource);

			System.out.println("tests saved in " + dataPath);
		}

		System.out.println("");
		System.out.println("process completed");
	}

	private static void saveTestFile(String dataPath, String expectedPath,
			String resource) {
		BufferedReader in = null;
		Scanner scanner = null;
		URL url;
		try {
			url = new URL(resource);
			in = new BufferedReader(new InputStreamReader(url.openStream()));

			scanner = new Scanner(in);
			String testFile = scanner.useDelimiter("\\A").next();
			String[] tests = testFile.split("(^|\n\n)#data\n");

			for (int i = 1; i < tests.length; i++) {
				String test = tests[i];
				/*
				 * Omit the tests that have script-off. Check tests16.dat
				 */
				if (test.contains("#script-off"))
					continue;

				/*
				 * TODO Omit the fragment tests now, change later
				 */
				if (test.contains("#document-fragment"))
					continue;

				String testName = i + ".txt";
				String data = test.substring(0, test.indexOf("\n#errors\n"));
				String expected = test
						.substring(test.indexOf("\n#document\n") + 11);
				if (expected.lastIndexOf('\n') == expected.length() - 1) {
					expected = expected.substring(0, expected.length() - 1);
				}

				// System.out.println(path + "\\input\\" + testName);
				// System.out.println(data);
				// System.out.println(expected);
				IOFunctions.saveFile(dataPath + "\\" + testName, data);
				IOFunctions.saveFile(expectedPath + "\\" + testName,
						expected);
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
	}

}
