package com.html5tools.commoncrawl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.commons.io.IOUtils;

import com.html5parser.parser.HTML5Parser;

public class ParserRunner {

	private final static Logger LOG = Logger.getLogger(ParserRunner.class
			.getName());
	private static final String WORKING_PATH = "/home/jose/HTML5ParserComparator/";
	private static final long TIMEOUT = 30000;

	// private final List<ParserRun> parsers = new ArrayList<ParserRun>();

	public ParserRunner() {

	}

	public Map<String, String> parseFile(String htmlDocPath) throws IOException {
		List<ParserRun> parsers = new ArrayList<ParserRun>();

		 String[] cmdMScParser = { "java", "-jar",
		 WORKING_PATH + "MScParser/BestParserEver.jar", "-f",
		 htmlDocPath };
		//String[] cmdMScParser = { "-f", htmlDocPath };
		//ParserRun mscParser = new ParserRun("thePerron", cmdMScParser, "msc");
		ParserRun mscParser = new ParserRun("thePerron", cmdMScParser);
		parsers.add(mscParser);
		
		String[] cmdParse5 = new String[] { "node",
				WORKING_PATH + "parse5/parser5.js", "-f", htmlDocPath };
		ParserRun parser5 = new ParserRun("parser5", cmdParse5);
		parsers.add(parser5);

		String[] cmdJsoup = { "java", "-jar",
				WORKING_PATH + "jsoup/JsoupParser.jar", "-f", htmlDocPath };
		ParserRun jsoup = new ParserRun("jsoup", cmdJsoup);
		parsers.add(jsoup);

		String[] cmdhtm5lLib = { "python",
				WORKING_PATH + "html5lib/html5libAdapter.py", "-f", htmlDocPath };
		ParserRun html5Lib = new ParserRun("html5lib", cmdhtm5lLib);
		parsers.add(html5Lib);

		return parse(parsers);
	}

	public Map<String, String> parseString(String htmlDoc) throws IOException {
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

		return parse(parsers);
	}

	private Map<String, String> parse(List<ParserRun> parsers) {
		Map<String, String> outputTrees = new HashMap<String, String>();
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
			// After the timeout, destroy subprocess and interrupt thread of the
			// unfinished parsers
			for (ParserRun parser : parsers) {
				parser.destroyProcess();// Force termination of the
										// subprocess(linux command)
			}
			parseExecutor.shutdownNow();
			// Lets wait the thread to finish after the interrupt. Shouldn't
			// took to much.
			parseExecutor.awaitTermination(5, TimeUnit.SECONDS);
			// while (!parseExecutor.isTerminated()){}
			for (ParserRun parser : parsers) {

				if (parser.resCmd == 0) {
					String result = new String(parser.result);
					outputTrees.put(parser.getParserName(), result);
				} else if(parser.resCmd < 0){
					outputTrees.put(parser.getParserName(), "ERROR: Timeout reached" + parser.parserName);
				} else{
					outputTrees.put(parser.getParserName(), "ERROR: Running Parser" + parser.parserName);
				}
				
				//Log any other warning or error
				String error = new String(parser.error);
				if(!error.isEmpty())
					LOG.error("Error output (" + parser.parserName + ") :"
							 + "\n" + error);
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return outputTrees;
	}

	private class ParserRun extends Thread {

		private String[] cmd;
		private int resCmd;
		private byte[] result;
		private byte[] error;
		private String parserName;
		private String cmdS;
		private Process proc = null;
		private String parserRunner = null;

		public ParserRun(String parserName, String[] cmd) {
			this.cmd = cmd;
			this.parserName = parserName;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < cmd.length - 1; i++) {
				sb.append(" " + cmd[i]);
			}
			cmdS = sb.toString();
		}

		public void destroyProcess() {
			if (proc != null)
				proc.destroy();
		}

		/**
		 * This constructor is for Java Parsers to run it "inline"
		 * 
		 * @param parserName
		 * @param parser
		 */
		public ParserRun(String parserName, String[] cmd, String parserRunner) {
			this.cmd = cmd;
			this.parserRunner = parserRunner;
			this.parserName = parserName;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < cmd.length - 1; i++) {
				sb.append(" " + cmd[i]);
			}
			cmdS = sb.toString();
		}

		@Override
		public void run() {
			// Process proc=null;
			if (parserRunner != null) {
				LOG.info("Running java parser: " + parserName);
				PrintStream old = System.out;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				System.setOut(ps);

				if (parserRunner.equals("msc")) {
					HTML5Parser.main(cmd);
				} else {

				}

				System.out.flush();
				System.setOut(old);
				LOG.info("Done: " + parserName);
				result = baos.toByteArray();
			} else {
				try {

					LOG.info("Running: " + cmdS + "...");
					proc = Runtime.getRuntime().exec(cmd);

					// result
					result = IOUtils.toByteArray(proc.getInputStream());
					// sb.append(new String(rawData));

					// error
					error = IOUtils.toByteArray(proc.getErrorStream());
					// sbError.append(new String(rawDataE));
					resCmd = proc.waitFor();
				} catch (InterruptedException e) {
					resCmd = -1;
					LOG.info("Exception running: " + parserName);
					e.printStackTrace();
				} catch (IOException e) {
					LOG.info("Exception running: " + parserName);
					resCmd = -2;
					if(error==null) error="null".getBytes();
					e.printStackTrace();
				} finally {
					LOG.info("Done: " + cmdS);
					if (proc != null)
						proc.destroy();
				}
			}
		}

		public String getParserName() {
			return parserName;
		}

	}
}
