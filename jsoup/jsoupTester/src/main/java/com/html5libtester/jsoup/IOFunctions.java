package com.html5libtester.jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class IOFunctions {
	/*
	 * Try to create a directory given a path
	 */
	public static boolean createDirectory(String path) {
		File files = new File(path);
		if (!files.exists()) {
			if (!files.mkdirs()) {
				System.out.println("Failed to create multiple directories!");
				return false;
			}
		}
		return true;
	}

	/*
	 * Save a file given a path
	 */
	public static void saveFile(String fileName, String data) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(fileName, "UTF-8");
			writer.print(data);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	/*
	 * Read a file given a path and return the content
	 */
	public static String readFile(String path) {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sb.toString();
	}
}