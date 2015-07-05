package com.html5tools.commoncrawl;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReaderFactory;
import org.jets3t.service.S3ServiceException;

import com.html5tools.parserComparison.Comparator;

public class Crawler {

	private final static Logger LOG = Logger.getLogger(Crawler.class.getName());

	private static final String REPORT_FILE = "report_crawler.xml";
	private static final String HTML_FILE = "/home/jose/HTML5ParserComparator/htmlDocTemp";

	private static long startTime;

	private Comparator comparator = new Comparator();

	private ParserRunner parserRunner = new ParserRunner();

	public void processRecord(String remoteFile, long offset) {
		InputStream is = null;
		try {
			URLConnection connection = getConnection(remoteFile, offset);
			is = connection.getInputStream();
			ArchiveReader ar = WARCReaderFactory.get(remoteFile, is, true);
			process(ar, 1, offset);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (S3ServiceException e) {
			e.printStackTrace();
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public void processRemoteFile(String remoteFile, int maxRecords) {
		if (maxRecords <= 0)
			throw new IllegalArgumentException(
					"Maximum or records should be greater than 0");
		InputStream is = null;
		try {
			URLConnection connection = getConnection(remoteFile, -1);
			is = connection.getInputStream();
			ArchiveReader ar = WARCReaderFactory.get(remoteFile, is, true);
			process(ar, maxRecords, -1);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (S3ServiceException e) {
			e.printStackTrace();
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

	}

	/**
	 * 
	 * @param remoteFile
	 *            The URL of the remote file
	 * @param offset
	 *            -1 if no offset
	 * @return URLConnection
	 * @throws IOException
	 */
	private URLConnection getConnection(String remoteFile, long offset)
			throws IOException {
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
			connection.addRequestProperty("User-Agent", this.getClass()
					.getName());
		}

		if (offset != -1) {
			connection.addRequestProperty("Range", "bytes=" + offset + "-");
		}

		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		return connection;
	}

	public void processLocalRecord(String file, long offset) {
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			//TODO: update to get the record from the offset
			ArchiveReader ar = WARCReaderFactory.get(file, is, true);
			process(ar, 1, offset);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (S3ServiceException e) {
			e.printStackTrace();
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public void processLocalFile(String file, int maxRecords) {
		if (maxRecords <= 0)
			throw new IllegalArgumentException(
					"Maximum or records should be greater than 0");
		
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			ArchiveReader ar = WARCReaderFactory.get(file, is, true);
			process(ar, maxRecords, -1);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (S3ServiceException e) {
			e.printStackTrace();
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

	}

	private void process(ArchiveReader ar, int maxRecords, long offset)
			throws IOException, S3ServiceException {

		String body = "";
		String recordID = "";
		int numberHtmlDocsRetrieved = 0;
		for (ArchiveRecord r : ar) {

			// The header file contains information such as the type of record,
			// size, creation time, and URL
			System.out.println("Header: " + r.getHeader());
			System.out.println("URL: " + r.getHeader().getUrl());
			System.out.println("Content Size: "
					+ r.getHeader().getContentLength());
			// recordID =
			// r.getHeader().getHeaderValue("WARC-Record-ID").toString();
			recordID = r.getHeader().getUrl() + "     Offset: "
					+ (offset == -1 ? r.getHeader().getOffset() : offset);
			System.out
					.println("ID: "
							+ r.getHeader().getHeaderValue("WARC-Record-ID")
									.toString());
			System.out.println("Offset: "
					+ (offset == -1 ? r.getHeader().getOffset() : offset));
			System.out.println();

			// if (r.getHeader().getMimetype()
			// .equals("application/http; msgtype=response") &&
			// r.getHeader().getContentLength()<100000) {
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

					FileWriter fw = new FileWriter(HTML_FILE);
					BufferedWriter bf = new BufferedWriter(fw);
					bf.write(body);
					bf.close();
					fw.close();

					// System.out.println(body);
					if (!body.isEmpty()) {
						Map<String, String> outputTrees = runParsers(HTML_FILE);
						compareOutputs(outputTrees, recordID);
						numberHtmlDocsRetrieved++;
						if (numberHtmlDocsRetrieved >= maxRecords) {
							break;
						}
					}
				}
			}
			LOG.info("Elapsed time: "
					+ (System.currentTimeMillis() - startTime) / 1000);
		}

	}

	public int countTotalRecords(String remoteFile, long offset)
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
					numberHtmlDocsRetrieved++;
				}
			}
			LOG.info("Elapsed time: "
					+ (System.currentTimeMillis() - startTime) / 1000);
		}

		is.close();
		return numberHtmlDocsRetrieved;
	}

	private Map<String, String> runParsers(String htmlDocPath)
			throws IOException {
		return parserRunner.parseFile(htmlDocPath);
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
		offset = 11368;
		//offset = 540967;

		startTime = System.currentTimeMillis();

		Crawler crawler = new Crawler();
		//crawler.processRecord(fn, offset);
		// crawler.processRemoteFile(fn, 5);

		String local = "/home/jose/Downloads/CC-MAIN-20150417045713-00000-ip-10-235-10-82.ec2.internal.warc.gz";
		crawler.processLocalFile(local, 498);
		//crawler.processLocalRecord(local, offset);

		long finalTime = System.currentTimeMillis() - startTime;
		LOG.info("\n\n DONE!!!!");
		LOG.info("Elapsed time: " + finalTime / 1000);
	}
}
