package com.HTML5.ParserComparer.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class ProcessRunner {

	public static void run(List<String> args) throws IOException,
			InterruptedException {
		run(args, true);
	}

	public static void run(List<String> args, boolean waitForCompletion)
			throws IOException, InterruptedException {
		System.out.println("Process started");
		Process process = new ProcessBuilder(args).start();
		if (waitForCompletion) {
			int errCode = process.waitFor();
			System.out
					.println("Process completed, any errors? "
							+ (errCode == 0 ? "No" : "Yes - "
									+ String.valueOf(errCode)));
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