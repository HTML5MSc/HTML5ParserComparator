package com.html5libtester.jsoup;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Hello world!
 *
 */
public class JsoupParser {
	public static void main(String[] args) {
		System.out.println("Hello World!");

		String path = "C:\\Users\\hs012\\Desktop\\html5libTests";
		parseTest();
		//parseFiles(path);
	}

	public static Document parse(String html) {
		return Jsoup.parse(html);
	}
	
	public static Document parseURL(String url){
		try {
			return Jsoup.parse(new URL(url), 1000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Document parseFragment(String html) {
		return Jsoup.parseBodyFragment(html);
	}

	private static void parseTest() {
		//String html = "<!DOCTYPEhtml>Hello";
		//Document doc = Jsoup.parse(html);
		String html = "https://www.facebook.com/";
		Document doc = parseURL(html);
		System.out.println(doc.toString());
		System.out.println("\n");
		System.out.println(Html5libSerializer.dom2string(doc));
	}

	private static void parseFiles(String path) {
		if (!new File(path).exists() || !new File(path + "\\data").exists()) {
			System.out.print("Can't find tests directory");
			return;
		}

		/*
		 * Check if the path contains files/directories
		 */
		File[] dirs = new File(path + "\\data").listFiles();
		if (dirs.length < 1)
			return;

		/*
		 * Create a folder to save the jsoup parsed files
		 */
		path += "\\jsoup";
		if (!IOFunctions.createDirectory(path))
			return;

		for (File dir : dirs) {

			/*
			 * Check if the path is a directory and create a folder of the test
			 * name
			 */
			if (!dir.isDirectory())
				continue;
			String dirPath = path + "\\" + dir.getName();
			IOFunctions.createDirectory(dirPath);

			/*
			 * Loop through the files and parse them
			 */
			for (File file : dir.listFiles()) {
				String fileName = file.getName();
				String data = IOFunctions.readFile(file.getPath());

				// System.out.println(fileName + "\t" + data);
				Document doc = Jsoup.parse(data);
				IOFunctions.saveFile(dirPath + "\\" + fileName,
						Html5libSerializer.dom2string(doc));
			}
			System.out.println("tests parsed in " + dirPath);
		}
		System.out.println("");
		System.out.println("process completed");
	}
}
