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
				private String tree;
		private ArrayList<String> parsers;

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
	}
}
