package com.html5tools.adapters.validatorNU;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class HtmlLibSerializer implements ContentHandler, LexicalHandler {

	private final Writer writer;
	private int ancestors = 0;
	private String tempChars = null;

	private final LinkedList<StackNode> stack = new LinkedList<StackNode>();

	private final static Writer wrap(OutputStream out) {
		Charset charset = Charset.forName("utf-8");
		CharsetEncoder encoder = charset.newEncoder();
		encoder.onMalformedInput(CodingErrorAction.REPLACE);
		encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		try {
			encoder.replaceWith("\uFFFD".getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return new OutputStreamWriter(out, encoder);
	}

	public HtmlLibSerializer(OutputStream out) {
		this(wrap(out));
	}

	public HtmlLibSerializer(Writer out) {
		this.writer = out;
	}

	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		try {
			writeChars();
			writer.write("| "
					+ indent(ancestors + templateAncestors(stack.peekLast(), 0)));
			writer.write("<!-- ");
			writer.write(ch, start, length);
			writer.write(" -->");
			writer.write("\n");
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void endCDATA() throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endDTD() throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endEntity(String arg0) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startCDATA() throws SAXException {
	}

	@Override
	public void startDTD(String name, String publicId, String systemId)
			throws SAXException {
		try {
			writeChars();
			writer.write("| "
					+ indent(ancestors + templateAncestors(stack.peekLast(), 0)));
			if (!("".equals(publicId) && "".equals(systemId))) {
				publicId = " \"" + (publicId == "" ? "" : publicId) + "\"";
				systemId = " \"" + (systemId == "" ? "" : systemId) + "\"";
			}
			// publicId = (publicId == "" ? "" : " \"" + publicId + "\"");
			// systemId = (systemId == "" ? "" : " \"" + systemId + "\"");

			writer.write("<!DOCTYPE " + name + publicId + systemId + '>');
			writer.write("\n");
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void startEntity(String arg0) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (tempChars == null)
			tempChars = new String(ch);
		else
			tempChars = tempChars + String.copyValueOf(ch);
	}

	private void writeChars() throws SAXException {
		try {
			if (tempChars != null) {
				writer.write("| "
						+ indent(ancestors
								+ templateAncestors(stack.peekLast(), 0)));
				writer.write('"');
				writer.write(tempChars);
				writer.write('"');
				writer.write("\n");
				tempChars = null;
			}
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void endDocument() throws SAXException {
		try {
			writeChars();
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
		writeChars();
		stack.removeFirst();
		ancestors--;
	}

	@Override
	public void endPrefixMapping(String arg0) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDocumentLocator(Locator arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void skippedEntity(String arg0) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startElement(String uri, String localName, String q,
			Attributes atts) throws SAXException {

		try {
			writeChars();
			writer.write("| "
					+ indent(ancestors + templateAncestors(stack.peekLast(), 0)));
			writer.write('<');
			if (uri.length() != 0) {
				switch (uri) {
				case "http://www.w3.org/2000/svg":
					writer.write("svg ");
					break;
				case "http://www.w3.org/1998/Math/MathML":
					writer.write("math ");
					break;
				}

			}
			writer.write(localName);
			writer.write('>');

			Map<String, String> attrNames = new HashMap<String, String>();
			int attLen = atts.getLength();
			for (int i = 0; i < attLen; i++) {
				String attUri = atts.getURI(i);
				String name = "";
				if (attUri.length() != 0) {
					switch (attUri) {
					case "http://www.w3.org/XML/1998/namespace":
						name += "xml ";
						break;
					case "http://www.w3.org/2000/xmlns/":
						name += "xmlns ";
						break;
					case "http://www.w3.org/1999/xlink":
						name += "xlink ";
						break;
					}
				}
				name += atts.getLocalName(i);
				attrNames.put(name, atts.getValue(i));
			}

			if (attrNames.size() > 0) {
				TreeMap<String, String> sorted_map = new TreeMap<String, String>(
						attrNames);
				for (Map.Entry<String, String> entry : sorted_map.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					// str += "\n| " + indent(1 + ancestors) + key;
					writer.write("\n| "
							+ indent(1 + ancestors
									+ templateAncestors(stack.peekLast(), 0))
							+ key);
					writer.write("=\"" + value + "\"");
				}

			}

			/*
			 * Template elements have 'content' child This is the required
			 * format of html5lib
			 */
			if (localName.equals("template")
					&& ("http://www.w3.org/1999/xhtml".equals(uri))) {
				writer.write("\n| "
						+ indent(1 + ancestors
								+ templateAncestors(stack.peekLast(), 0))
						+ "content");
			}

			writer.write("\n");

		} catch (IOException e) {
			throw new SAXException(e);
		}
		ancestors++;
		push(uri, localName);

	}

	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	private static String indent(int ancestors) {
		String str = "";
		if (ancestors > 0) {
			while (0 <= --ancestors)
				str += "  ";
		}
		return str;
	}

	private final int templateAncestors(StackNode current, int ancestors) {
		try {
			current = stack.get(stack.lastIndexOf(current) - 1);
		} catch (IndexOutOfBoundsException e) {
			current = null;
		}
		if (current != null) {
			if (current.name.equals("template")
					&& current.uri.equals("http://www.w3.org/1999/xhtml"))
				ancestors++;
			return templateAncestors(current, ancestors);
		} else
			return ancestors;

	}

	private final void push(String uri, String local) {
		stack.addFirst(new StackNode(uri, local, null));
	}

	private final class StackNode {
		public final String uri;

		//public final String prefix;

		public final String name;

		/**
		 * @param uri
		 * @param qName
		 */
		public StackNode(String uri, String name, String prefix) {
			this.uri = uri;
			this.name = name;
			//this.prefix = prefix;
		}
	}

}
