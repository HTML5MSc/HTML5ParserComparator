package com.html5tools.commoncrawl;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class ParserRunner {

	private final static Logger LOG = Logger.getLogger(ParserRunner.class
			.getName());
	// private static final String WORKING_PATH =
	// "/home/jose/HTML5ParserComparator/";
	private static final long TIMEOUT = 60000;

	private String name;
	private String cmd;

	// private final List<ParserRun> parsers = new ArrayList<ParserRun>();

	public ParserRunner(String name, String cmd) {
		this.name = name;
		this.cmd = cmd;
	}

	public String parseFile(String htmlDocPath) throws IOException {

		ParserRun html5Lib = new ParserRun(name,  cmd + " " + 
				htmlDocPath );

		return parse(html5Lib);
	}

	private String parse(ParserRun parser) {
		String result;
		parser.start();
		try {

			// Gives a specified TIMEOUT to wait for the execution of the
			// parsers
			parser.join(TIMEOUT);
			// After the timeout, destroy subprocess and interrupt thread of the
			// unfinished parsers
			parser.destroyProcess();// Force termination of the
									// subprocess(linux command)
			// Lets wait the thread to finish after the interrupt. Shouldn't
			// took to much.
			parser.interrupt();
			parser.join();

			if (parser.resCmd == 0) {
				result = new String(parser.result);
			} else if (parser.resCmd < 0) {
				result = "ERROR: Timeout reached" + parser.parserName;
			} else {
				result = "ERROR: Running Parser" + parser.parserName;
			}

			// Log any other warning or error
			String error = new String(parser.error);
			if (!error.isEmpty())
				LOG.error("Error output (" + parser.parserName + ") :" + "\n"
						+ error);

		} catch (InterruptedException e) {
			result = "ERROR: Interrupted Exception";
			e.printStackTrace();
		}

		return result;
	}

	private class ParserRun extends Thread {

		private int resCmd;
		private byte[] result;
		private byte[] error;
		private String parserName;
		private String cmd;
		private Process proc = null;

		public ParserRun(String parserName, String cmd) {
			this.cmd = cmd;
			this.parserName = parserName;
			
		}

		public void destroyProcess() {
			if (proc != null)
				proc.destroy();
		}

		@Override
		public void run() {
			try {
				LOG.info("Running: " + cmd );
				proc = Runtime.getRuntime().exec(cmd);

				// result
				result = IOUtils.toByteArray(proc.getInputStream());

				// error
				error = IOUtils.toByteArray(proc.getErrorStream());
				resCmd = proc.waitFor();
			} catch (InterruptedException e) {
				resCmd = -1;
				LOG.info("Exception running: " + parserName);
				e.printStackTrace();
			} catch (IOException e) {
				LOG.info("Exception running: " + parserName);
				resCmd = -2;
				if (error == null)
					error = "null".getBytes();
				e.printStackTrace();
			} finally {
				LOG.info("Done: " + cmd);
				if (proc != null)
					proc.destroy();
			}
		}
	}
}
