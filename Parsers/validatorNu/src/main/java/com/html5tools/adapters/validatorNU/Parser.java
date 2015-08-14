package com.html5tools.adapters.validatorNU;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

public class Parser {

	public static void main(String[] args) {

		if (args.length != 2) {
			System.out.println("Two parameters are required");
			return;
		}

		String file = null;
		String inputString = null;

		switch (args[0]) {
		case "-f":
			file = args[1];
			break;
		case "-s":
			inputString = args[1];
			break;
		default:
			System.out.println("Invalid option");
			return;
		}
		
		try {
			//OutputStream out = System.out;
			Writer out = new StringWriter();
			ContentHandler serializer = new HtmlLibSerializer(out);
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALLOW);

			parser.setContentHandler(serializer);
			parser.setProperty("http://xml.org/sax/properties/lexical-handler",
					serializer);
			//enable scripting to follow the testing standard
			parser.setScriptingEnabled(true);
			InputSource in = null;
			if (file != null) {
				//TODO: fin a way to read a file avoiding a new line at the end
//				try (InputStream is = Files.newInputStream(Paths.get(args[1]));
//						BufferedReader reader = new BufferedReader(
//								new InputStreamReader(is, Charset.forName("UTF-8")))) {
//					StringBuilder input = new StringBuilder();
//					String line;
//					if ((line = reader.readLine()) != null)
//						input.append(line);
//					while ((line = reader.readLine()) != null) {
//						input.append("\n");
//						input.append(line);
//					}
//					inputString=input.toString();
//					//inputString=input.deleteCharAt(input.length()-1).toString();
//				}
				
				in = new InputSource();
				String systemId = new File(args[1]).toURI().toASCIIString();
				in.setSystemId(systemId);
                in.setByteStream(new URL(systemId).openStream());
				
			}
			if (inputString != null) {
				in = new InputSource(new ByteArrayInputStream(
						inputString.getBytes()));
			}

			in.setEncoding("UTF-8");
			parser.parse(in);
			// Parse fragment
			// parser.parseFragment(new InputSource(in),"div");
			// serializer.endDocument();

			out.flush();
			String result = out.toString();
			//the serializer always add an extra line
			result = result.substring(0, result.length() - 1);
			System.out.print(result);
			out.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}

}
