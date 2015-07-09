package com.HTML5.ParserComparer.model.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class ProcessRunner {

	public static void run(List<String> args, String directory) throws Exception {
		run(args, directory, true);
	}

	public static void run(List<String> args, String directory, boolean waitForCompletion)
			throws Exception {
		System.out.println("Process started");
		Process process = new ProcessBuilder(args).directory(new File(directory)).start();
		if (waitForCompletion) {
			int errCode = process.waitFor();
			System.out
					.println("Process completed, any errors? "
							+ (errCode == 0 ? "No" : "Yes - "
									+ String.valueOf(errCode)));
			if(errCode != 0)
				throw new Exception("Could not complete the process.");
		}
		System.out.println(inputStreamToString(process.getErrorStream()));
		System.out.println("Process output:\n"
				+ inputStreamToString(process.getInputStream()));
	}

	private static String inputStreamToString(InputStream inputStream)
			throws IOException {

		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + System.lineSeparator());
			}
		} finally {
			br.close();
		}
		return sb.toString();
	}

}