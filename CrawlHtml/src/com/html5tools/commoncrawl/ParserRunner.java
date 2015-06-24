package com.html5tools.commoncrawl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.commons.io.IOUtils;

public class ParserRunner {

	private final static Logger LOG = Logger.getLogger(ParserRunner.class
			.getName());
	private static final String WORKING_PATH = "/home/jose/HTML5ParserComparator/";
	private static final long TIMEOUT = 5000;

	// private final List<ParserRun> parsers = new ArrayList<ParserRun>();

	public ParserRunner() {

	}

	public Map<String, String> runParsers(String htmlDoc) throws IOException {
		Map<String, String> outputTrees = new HashMap<String, String>();

		List<ParserRun> parsers = new ArrayList<ParserRun>();

		String[] cmd = new String[] { "node",
				WORKING_PATH + "parse5/parser5.js", "-s", htmlDoc };
		ParserRun parserJsoup = new ParserRun("parser5", cmd);
		parsers.add(parserJsoup);

		String[] cmdJsoup = { "java", "-jar",
				WORKING_PATH + "jsoup/JsoupParser.jar", "-s", htmlDoc };
		ParserRun jsoup = new ParserRun("jsoup", cmdJsoup);
		parsers.add(jsoup);

		String[] cmdhtm5lLib = { "python",
				WORKING_PATH + "html5lib/html5libAdapter.py", "-s", htmlDoc };
		ParserRun html5Lib = new ParserRun("html5lib", cmdhtm5lLib);
		parsers.add(html5Lib);

		ExecutorService parseExecutor = Executors.newFixedThreadPool(parsers
				.size());
		for (ParserRun parser : parsers) {
			parseExecutor.execute(parser);
		}

		// Force the execution of parsers
		parseExecutor.shutdown();
		try {
			// parser.join();
			// Gives a specified TIMEOUT to wait for the execution of the
			// parsers
			parseExecutor.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);
			// After the timeout, force and interrupt the process of the
			// unfinished parsers
			parseExecutor.shutdownNow();
			// Lets wait the thread to finish after the interrupt. Shouldn't
			// took to much.
			parseExecutor.awaitTermination(10000, TimeUnit.SECONDS);
			// while (!parseExecutor.isTerminated()){}
			for (ParserRun parser : parsers) {

				if (parser.resCmd == 0) {
					String result = new String(parser.result);
					outputTrees.put(parser.getParserName(), result);
				} else {
					outputTrees.put(parser.getParserName(), "");
					if (parser.resCmd == -1) { // Interruption
						String resultE = new String(parser.error);
						LOG.error(resultE);
					}
				}
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * LOG.info("Executing parser5.js..."); String[] cmdParse5 = {"node",
		 * WORKING_PATH+"parse5/parser5.js", "-s", htmlDoc}; String parse5Result
		 * = runParser(cmdParse5); outputTrees.put("parse5", parse5Result);
		 * 
		 * LOG.info("Executing JsoupParser.jar..."); String[] cmdHtml5Lib =
		 * {"java", "-jar",WORKING_PATH+"jsoup/JsoupParser.jar", "-s", htmlDoc};
		 * String html5LibResult = runParser(cmdHtml5Lib);
		 * outputTrees.put("jsoup", html5LibResult);
		 * 
		 * LOG.info("Executing html5libAdapter.py..."); String[] cmdJsoup =
		 * {"python", WORKING_PATH+"html5lib/html5libAdapter.py", "-s",
		 * htmlDoc}; String jsoupResult = runParser(cmdJsoup);
		 * outputTrees.put("html5lib", jsoupResult);
		 */
		return outputTrees;
	}

	private class ParserRun extends Thread {

		private String[] cmd;
		private int resCmd;
		private byte[] result;
		private byte[] error;
		private String parserName;
		private String cmdS;

		public ParserRun(String parserName, String[] cmd) {
			this.cmd = cmd;
			this.parserName = parserName;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < cmd.length - 1; i++) {
				sb.append(" " + cmd[i]);
			}
			cmdS = sb.toString();
		}

		@Override
		public void run() {
			try {

				LOG.info("Running: " + cmdS + "...");
				Process proc = Runtime.getRuntime().exec(cmd);

				// result
				result = IOUtils.toByteArray(proc.getInputStream());
				// sb.append(new String(rawData));

				// error
				error = IOUtils.toByteArray(proc.getErrorStream());
				// sbError.append(new String(rawDataE));
				resCmd = proc.waitFor();
			} catch (InterruptedException e) {
				resCmd = -1;
				e.printStackTrace();
			} catch (IOException e) {
				resCmd = -2;
				e.printStackTrace();
			} finally {
				LOG.info("Done: " + cmdS);
			}
		}

		public String getParserName() {
			return parserName;
		}

	}
}
