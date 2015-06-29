package com.html5tools.commoncrawl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReaderFactory;
import org.jets3t.service.S3ServiceException;

import com.html5tools.parserComparison.Comparator;

public class Crawler {

	private final static Logger LOG = Logger.getLogger(Crawler.class.getName());

	private static final String REPORT_FILE = "report.xml";

	private Comparator comparator = new Comparator();

	private ParserRunner parserRunner = new ParserRunner();

	public void processRecord(String remoteFile, long offset)
			throws IOException, S3ServiceException {
		process(remoteFile, 1, offset);
	}

	public void processFile(String remoteFile, int maxRecords)
			throws IOException, S3ServiceException {
		if(maxRecords<=0) throw new IllegalArgumentException("Maximum or records should be greater than 0");
		process(remoteFile, maxRecords, -1);
	}

	private void process(String remoteFile, int maxRecords, long offset)
			throws IOException, S3ServiceException {

		URL url = new URL(remoteFile);
		// S3Object f = s3s.getObject("aws-publicdatasets", fn, null, null,
		// null, null, null, null);

		// GZIPMembersInputStream some = new
		// GZIPMembersInputStream(f.getDataInputStream());

		// The file name identifies the ArchiveReader and indicates if it should
		// be decompressed
		// WarcReader ar = WarcReaderFactory.getReader(f.getDataInputStream());
		// WarcReader ar = WarcReaderFactory.getReaderUncompressed();

		// 17181

		URLConnection connection = url.openConnection();
		if (connection instanceof HttpURLConnection) {
			// addUserAgent((HttpURLConnection)connection);
		}

		if (offset != -1) {
			connection.addRequestProperty("Range", "bytes=" + offset + "-");
		}

		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		InputStream is = connection.getInputStream();
		ArchiveReader ar = WARCReaderFactory.get(url.toString(), is, true);

		String body = "";
		String recordID = "";
		int numberHtmlDocsRetrieved = 0;
		for (ArchiveRecord r : ar) {

			// The header file contains information such as the type of record,
			// size, creation time, and URL
			System.out.println("Header: " + r.getHeader());
			System.out.println("URL: " + r.getHeader().getUrl());
			// recordID = r.getHeader().getHeaderValue("WARC-Record-ID").toString();
			recordID = r.getHeader().getUrl();
			System.out.println("ID: " + r.getHeader().getHeaderValue("WARC-Record-ID").toString());
			System.out.println("Offset: " + (offset==-1?r.getHeader().getOffset():offset));
			System.out.println();

			if (r.getHeader().getMimetype()
					.equals("application/http; msgtype=response")) {
				// Convenience function that reads the full message into a raw
				// byte array
				byte[] rawData = IOUtils.toByteArray(r, r.available());
				String content = new String(rawData);
				// System.out.println(content);
				System.out.println();
				System.out.println();
				// The HTTP header gives us valuable information about what was
				// received during the request
				String headerText = content.substring(0,
						content.indexOf("\r\n\r\n"));
				System.out.println(headerText);
				System.out.println();
				System.out.println();
				// In our task, we're only interested in text/html, so we can be
				// a little lax
				// TODO: Proper HTTP header parsing + don't trust headers
				if (headerText.contains("Content-Type: text/html")) {
					// Only extract the body of the HTTP response when necessary
					// Due to the way strings work in Java, we don't use any
					// more memory than before
					body = content.substring(content.indexOf("\r\n\r\n") + 4);
					// Process all the matched HTML tags found in the body of
					// the document
					//System.out.println(body);
					if (!body.isEmpty()) {
						Map<String, String> outputTrees = runParsers(body);
						compareOutputs(outputTrees, recordID);
					}

					numberHtmlDocsRetrieved++;
					if (numberHtmlDocsRetrieved >= maxRecords) {
						break;
					}
				}
			}
		}
		
		is.close();

	}

	private Map<String, String> runParsers(String htmlDoc) throws IOException {
		return parserRunner.runParsers(htmlDoc);
	}

	private void compareOutputs(Map<String, String> outputTrees, String recordID) {
		// TODO: Enhance comparator to process stream of bytes, instead of
		// String
		comparator.run(recordID, Crawler.REPORT_FILE, outputTrees);
	}

	public static void main(String[] args) throws IOException,
			S3ServiceException {
		// Let's grab a file out of the CommonCrawl S3 bucket
		String fn = "common-crawl/crawl-data/CC-MAIN-2013-48/segments/1386163035819/warc/CC-MAIN-20131204131715-00000-ip-10-33-133-15.ec2.internal.warc.gz";
		fn = "https://aws-publicdatasets.s3.amazonaws.com/common-crawl/crawl-data/CC-MAIN-2015-18/segments/1429246633512.41/warc/CC-MAIN-20150417045713-00000-ip-10-235-10-82.ec2.internal.warc.gz";

		long offset = 0;// Start from offset 0, the beginning
		//offset = 680252006;
		offset = 540967; //doesnt work TOO LONG

		long startTime = System.currentTimeMillis();
		
		Crawler crawler = new Crawler();
		//crawler.processRecord(fn, offset);
		crawler.processFile(fn, 100);
		
		long finalTime = System.currentTimeMillis()-startTime;
		LOG.info("\n\n DONE!!!!");
		LOG.info("Time elapsed: "+finalTime);
	}
}
