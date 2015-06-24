package com.HTML5.ParserComparer.model;

import java.util.ArrayList;

public class TestCase {
	private String name;
	private int numberOfTrees;
	private ArrayList<TestOutput> outputs;

	public TestCase() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumberOfTrees() {
		return numberOfTrees;
	}

	public void setNumberOfTrees(int numberOfTrees) {
		this.numberOfTrees = numberOfTrees;
	}

	public ArrayList<TestOutput> getOutputs() {
		return outputs;
	}

	public void setOutputs(ArrayList<TestOutput> outputs) {
		this.outputs = outputs;
	}

	public class TestOutput {

		private boolean majority;
		private int editDistance;
		private int deletions = 0;
		private int insertions;
		private int charDeletions;
		private int charInsertions;
		private String tree;
		private ArrayList<String> parsers;
		private ArrayList<Diff> diffs;

		public TestOutput() {
			super();
		}

		public TestOutput(String tree, ArrayList<String> parsers) {
			super();
			this.tree = tree;
			this.parsers = parsers;
		}

		public boolean isMajority() {
			return majority;
		}

		public void setMajority(boolean majority) {
			this.majority = majority;
		}

		public int getEditDistance() {
			return editDistance;
		}

		public void setEditDistance(int editDistance) {
			this.editDistance = editDistance;
		}

		public int getDeletions() {
			return deletions;
		}

		public void setDeletions(int deletions) {
			this.deletions = deletions;
		}

		public int getInsertions() {
			return insertions;
		}

		public void setInsertions(int insertions) {
			this.insertions = insertions;
		}

		public int getCharDeletions() {
			return charDeletions;
		}

		public void setCharDeletions(int charDeletions) {
			this.charDeletions = charDeletions;
		}

		public int getCharInsertions() {
			return charInsertions;
		}

		public void setCharInsertions(int charInsertions) {
			this.charInsertions = charInsertions;
		}

		public String getTree() {
			return tree;
		}

		public void setTree(String tree) {
			this.tree = tree;
		}

		public ArrayList<String> getParsers() {
			return parsers;
		}

		public void setParsers(ArrayList<String> parsers) {
			this.parsers = parsers;
		}

		public ArrayList<Diff> getDiffs() {
			return diffs;
		}

		public void setDiffs(ArrayList<Diff> diffs) {
			this.diffs = diffs;
		}
	}

	public class Diff {
		private int index;
		private String type;
		private String content;

		public Diff(int index, String type, String content) {
			super();
			this.index = index;
			this.type = type;
			this.content = content;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
	}
}