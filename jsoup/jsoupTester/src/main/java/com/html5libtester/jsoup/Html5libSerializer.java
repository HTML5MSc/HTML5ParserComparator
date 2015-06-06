package com.html5libtester.jsoup;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

public class Html5libSerializer {
	public static String dom2string(Node node) {
		String str = "";
		int ancestors = 0;
		if (node.childNode(0) == null)
			return "| ";
		Node parent = node;
		Node current = node.childNode(0);
		Node next = null;

		for (;;) {
			str += "\n| " + indent(ancestors + templateAncestors(current, 0));

			switch (current.getClass().toString()
					.replace("class org.jsoup.nodes.", "")) {
			case "Comment":
				Comment c = (Comment) current;
				str += "<!-- " + c.attr("comment") + " -->";
				break;
			case "DataNode":
				DataNode dn = (DataNode) current;
				str += '"' + dn.getWholeData() + '"';
				break;
			case "DocumentType":
				// DocumentType d = (DocumentType) current;
				String dname = current.attr("name");
				String publicid = current.attr("publicid");
				String systemid = current.attr("systemid");

				if (!publicid.isEmpty() && systemid.isEmpty()) {
					publicid = " \"" + publicid + "\"";
					systemid = " \"\"";
				} else if (publicid.isEmpty() && !systemid.isEmpty()) {
					publicid = " \"\"";
					systemid = " \"" + systemid + "\"";
				} else if (!publicid.isEmpty() && !systemid.isEmpty()) {
					publicid = " \"" + publicid + "\"";
					systemid = " \"" + systemid + "\"";
				}

				str += "<!DOCTYPE " + dname + publicid + systemid + '>';
				break;
			case "Element":
			case "FormElement":
				str += "<";

				// if (current.baseUri() != null)
				// switch (current.baseUri()) {
				// case "http://www.w3.org/2000/svg":
				// str += "svg ";
				// break;
				// case "http://www.w3.org/1998/Math/MathML":
				// str += "math ";
				// break;
				// }

				str += current.nodeName();
				str += '>';
				if (current.attributes().size() > 0) {
					Map<String, String> attrNames = new HashMap<String, String>();
					for (Attribute att : current.attributes().asList()) {
						String name = "";
						name += att.getKey();
						attrNames.put(name, att.getValue());
					}

					if (attrNames.size() > 0) {
						TreeMap<String, String> sorted_map = new TreeMap<String, String>(
								attrNames);
						for (Map.Entry<String, String> entry : sorted_map
								.entrySet()) {
							String key = entry.getKey();
							String value = entry.getValue();

							str += "\n| " + indent(1 + ancestors + templateAncestors(current, 0)) + key;
							str += "=\"" + value + "\"";
						}
					}
				}
				/*
				 * Template elements have 'content' child 
				 * This is the required format of html5lib
				 */
				if(current.nodeName().equals("template")){
					str += "\n| " + indent(1 + ancestors + templateAncestors(current, 0)) + "content";
				}
				break;
			case "Entities":
				str += "<![CDATA[ " + current.nodeName() + " ]]>";
				break;
			case "TextNode":
				TextNode t = (TextNode) current;
				str += '"' + t.getWholeText() + '"';
				break;

			default:
				break;
			}

			next = current.childNodeSize() > 0 ? current.childNode(0) : null;
			if (null != next) {
				parent = current;
				current = next;
				ancestors++;
				continue;
			}

			for (;;) {
				next = current.nextSibling();
				if (next != null) {
					current = next;
					break;
				}
				current = current.parent();
				parent = parent.parent();
				ancestors--;
				if (current == node) {
					return str.substring(1);
				}
			}
		}
	}

	private static String indent(int ancestors) {
		String str = "";
		if (ancestors > 0) {
			while (0 <= --ancestors)
				str += "  ";
		}
		return str;
	}

	private static int templateAncestors(Node current, int ancestors){
		if(current.parent() != null)
		{
			current = current.parent();
			if(current.nodeName().equals("template"))
				ancestors++;
			return templateAncestors(current, ancestors);
		}
		else
			return ancestors;
		
	}
}
