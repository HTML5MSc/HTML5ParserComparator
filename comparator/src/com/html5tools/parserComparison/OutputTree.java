package com.html5tools.parserComparison;

import java.util.ArrayList;
import java.util.List;

public class OutputTree implements Comparable<OutputTree> {
	private String tree;
	private List<String> parsers;

	public OutputTree(String tree, String parser) {
		this.tree = tree;
		parsers = new ArrayList<String>();
		parsers.add(parser);
	}

	public void addParser(String parser) {
		parsers.add(parser);
	}

	public String getTree() {
		return tree;
	}

	public void setTree(String tree) {
		this.tree = tree;
	}

	public List<String> getParsers() {
		return parsers;
	}

	public void setParsers(List<String> parsers) {
		this.parsers = parsers;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(tree.toString() + "\n");
		for (String parser : parsers) {
			sb.append(parser.toString() + " ");
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tree == null) ? 0 : tree.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OutputTree other = (OutputTree) obj;
		if (tree == null) {
			if (other.tree != null)
				return false;
		} else if (!tree.equals(other.tree))
			return false;
		return true;
	}

	@Override
	public int compareTo(OutputTree o) {
		return Integer.compare(o.parsers.size(), parsers.size());
	}
}