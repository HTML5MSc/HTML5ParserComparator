package com.html5tools.commoncrawl;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReaderFactory;
import org.jets3t.service.S3ServiceException;

import com.html5tools.parserComparison.Comparator;

public class Crawler {

	private final static Logger LOG = Logger.getLogger(Crawler.class.getName());

	private String reportFile;
	private String htmlPath = "tempdocsin/";
	private String workingPath;

	private static long startTime;

	private Comparator comparator = new Comparator();

	private ParserRunner parserRunner;

	private String ccArchiveName;

	private String parser;
	private String cmd;

	public Crawler(String parser, String cmd) throws IOException {
		this.parser = parser;
		this.cmd = cmd;
		parserRunner = new ParserRunner(parser, cmd);
		loadProperties();
		init();
	}

	private void init() throws IOException {
		Path tempDir = Paths.get(htmlPath);
		if (Files.notExists(tempDir)) {
			Files.createDirectory(tempDir);
		}

		Path workDir = Paths.get(workingPath);
		if (Files.notExists(workDir)) {
			Files.createDirectory(workDir);
		}
	}

	private void loadProperties() throws IOException {

		Properties defaultProps = new Properties();
		InputStream in = getClass().getResourceAsStream(
				"/defaultConfig.properties");
		defaultProps.load(in);
		in.close();

		Properties applicationProps = new Properties(defaultProps);
		try {
			in = new FileInputStream("config.properties");
			applicationProps.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			// Running with defaults
		}

		reportFile = applicationProps.getProperty("reportFile");
		workingPath = applicationProps.getProperty("workingPath");
	}

	public void processRecord(String remoteFile, long offset) {
		InputStream is = null;
		try {
			ccArchiveName = getArchiveName(remoteFile);
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
			ccArchiveName = getArchiveName(remoteFile);
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
			is.skip(offset);
			// TODO: update to get the record from the offset
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
	
	public void processLocalFile(String file) {
		FileInputStream is = null;
		try {
			ccArchiveName = getArchiveName(file);
			is = new FileInputStream(file);
			ArchiveReader ar = WARCReaderFactory.get(file, is, true);
			process(ar, -1, -1);
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
			ccArchiveName = getArchiveName(file);
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

	private String getArchiveName(String file) {
		int beginIndex = file.lastIndexOf('/') + 1;
		int endIndex = file.lastIndexOf("-ip-");
		return file.substring(beginIndex, endIndex);
	}

	private void process(ArchiveReader ar, int maxRecords, long offset)
			throws IOException, S3ServiceException {

		if (!Files.exists(Paths.get(workingPath + ccArchiveName))) {
			Files.createDirectory(Paths.get(workingPath + ccArchiveName));
		}

		String body = "";
		long numberHtmlDocsRetrieved = 0;
		for (ArchiveRecord r : ar) {

			// The header file contains information such as the type of record,
			// size, creation time, and URL
			//System.out.println("Header: " + r.getHeader());
			System.out.println("URL: " + r.getHeader().getUrl());
			System.out.println("Content Size: "
					+ r.getHeader().getContentLength());
			// recordID =
			// r.getHeader().getHeaderValue("WARC-Record-ID").toString();
			// String recordID = r.getHeader().getUrl() + "     Offset: "
			// + (offset == -1 ? r.getHeader().getOffset() : offset);
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
//				System.out.println(headerText);
//				System.out.println();
//				System.out.println();
				// TODO: Proper HTTP header parsing + don't trust headers
				if (headerText.contains("Content-Type: text/html")) {
					// Only extract the body of the HTTP response when necessary
					body = content.substring(content.indexOf("\r\n\r\n") + 4);

					// System.out.println(body);
					if (!body.isEmpty()) {
						numberHtmlDocsRetrieved++;
						Path htmlTempFile = Paths.get(htmlPath + "htmlDocTemp"
								+ System.currentTimeMillis());
						saveTempFile(htmlTempFile, body);
						String outputTree = runParsers(htmlTempFile
								.toAbsolutePath().toString());
						Files.deleteIfExists(htmlTempFile);

						saveTreeToFileSystem(outputTree,
								numberHtmlDocsRetrieved
										+ "_"
										+ (offset == -1 ? r.getHeader()
												.getOffset() : offset));

						// compareOutputs(outputTrees, recordID);

						if (maxRecords!=-1 && numberHtmlDocsRetrieved >= maxRecords) {
							break;
						}
					}
				}
			}
			LOG.info("Elapsed time: "
					+ (System.currentTimeMillis() - startTime) / 1000);
		}

	}

	private void saveTempFile(Path file, String content) throws IOException {

		byte data[] = content.getBytes(Charset.forName("UTF-8"));
		try (OutputStream out = new BufferedOutputStream(
				Files.newOutputStream(file))) {
			out.write(data, 0, data.length);
		}

	}

	private void saveTreeToFileSystem(String outputTree, String recordId)
			throws IOException {

		String recordDir = workingPath + "/" + ccArchiveName + "/" + recordId;
		Path recordDirPath = Paths.get(recordDir);
		if (!Files.exists(recordDirPath)) {
			Files.createDirectory(recordDirPath);
		}

		Path file = Paths.get(recordDir + "/" + parser);

		byte data[] = outputTree.getBytes(Charset.forName("UTF-8"));
		try (OutputStream out = new BufferedOutputStream(
				Files.newOutputStream(file))) {
			out.write(data, 0, data.length);
		} catch (IOException x) {
			System.err.println(x);
		}

	}

	public int countTotalRecords(String file) throws IOException {

		// URL url = new URL(remoteFile);
		//
		// URLConnection connection = url.openConnection();
		//
		// connection.setConnectTimeout(10000);
		// connection.setReadTimeout(10000);
		// InputStream is = connection.getInputStream();
		// ArchiveReader ar = WARCReaderFactory.get(url.toString(), is, true);

		InputStream is = new FileInputStream(file);
		ArchiveReader ar = WARCReaderFactory.get(file, is, true);

		int numberHtmlDocsRetrieved = 0;
		for (ArchiveRecord r : ar) {

			if (r.getHeader().getMimetype()
					.equals("application/http; msgtype=response")) {
				// Convenience function that reads the full message into a raw
				// byte array
				byte[] rawData = IOUtils.toByteArray(r, r.available());
				String content = new String(rawData);
				String headerText = content.substring(0,
						content.indexOf("\r\n\r\n"));
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

	// private Map<String, String> runParsers(String htmlDocPath)
	// throws IOException {
	// return parserRunner.parseFile(htmlDocPath);
	// }

	private String runParsers(String htmlDocPath) throws IOException {
		return parserRunner.parseFile(htmlDocPath);
	}

	private void compareOutputs(Map<String, String> outputTrees, String recordID) {
		// TODO: Enhance comparator to process stream of bytes, instead of
		// String
		comparator.run(recordID, reportFile, outputTrees);
	}

	public static void main(String[] args) throws IOException {

		if(args.length==0){
			System.err.println("Empty arguments");
		}
		
		String parseName = args[0];
		String cmd = args[1];
		String ccfile = args[2];
		
		// Let's grab a file out of the CommonCrawl S3 bucket
		String fn = "common-crawl/crawl-data/CC-MAIN-2013-48/segments/1386163035819/warc/CC-MAIN-20131204131715-00000-ip-10-33-133-15.ec2.internal.warc.gz";
		fn = "https://aws-publicdatasets.s3.amazonaws.com/common-crawl/crawl-data/CC-MAIN-2015-18/segments/1429246633512.41/warc/CC-MAIN-20150417045713-00000-ip-10-235-10-82.ec2.internal.warc.gz";

		long offset = 0;// Start from offset 0, the beginning
		// offset = 15172197; // CDATA
		offset = 540967;

		startTime = System.currentTimeMillis();
		
		Crawler crawler = new Crawler(parseName,
				 cmd);
		
//		 Crawler crawler = new Crawler("html5lib",
//		 "python /home/jose/HTML5ParserComparator/html5lib/html5libAdapter.py -f");

//		 Crawler crawler = new Crawler("html5lib",
//		 "python /home/jose/HTML5ParserComparator/html5lib/html5libAdapter.py -f");

//		Crawler crawler = new Crawler("uom",
//				"java -jar /home/jose/HTML5ParserComparator/MScParser/BestParserEver.jar -f");

//		 crawler.processRecord(fn, offset);
		// crawler.processRemoteFile(fn, 50);

		//String ccfile = "/home/jose/Downloads/CC-MAIN-20150417045713-00000-ip-10-235-10-82.ec2.internal.warc.gz";
		crawler.processLocalFile(ccfile);
		// crawler.processLocalRecord(local, offset);
		// int total = crawler.countTotalRecords(local);
		// LOG.info("Total: "+total);
		long finalTime = System.currentTimeMillis() - startTime;
		LOG.info("DONE!!!!");
		LOG.info("Elapsed time: " + finalTime / 1000);
	}
}
