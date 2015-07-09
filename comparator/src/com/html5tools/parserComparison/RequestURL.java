package com.html5tools.parserComparison;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

public class RequestURL {

	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0";
	public static final int CONNECT_TIMEOUT = 10000;

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.print("One parameter (URL) is required.");
			System.exit(-1);
		}

		String url = args[0].toLowerCase();
		if (!url.contains("http://"))
			url = "http://".concat(url);
		String response = null;
		try {
			response = getResponse(url);
			if (response.isEmpty())
				response = getResponse(url.replace("http://", "https://"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		saveFile("response.txt", response.trim());
		// System.out.print(response);
	}

	private static String getResponse(String string) throws IOException {
		StringBuilder sb = new StringBuilder();
		URL url = new URL(string);
		URLConnection urlConnection = url.openConnection();
		urlConnection.addRequestProperty("User-Agent", USER_AGENT);
		urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
		InputStream inputStream = urlConnection.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(
				inputStream, "UTF-8");
		BufferedReader br = new BufferedReader(inputStreamReader);
		String read = br.readLine();

		while (read != null) {
			// System.out.println(read);
			sb.append(read);
			read = br.readLine();
		}

		return sb.toString();
	}

	private static void saveFile(String fileName, String data) {
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
}
