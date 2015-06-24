package com.HTML5.ParserComparer.model;

import java.util.ArrayList;

public class Report {
	private int numberOfTests;
	private int testsEqual;
	private int testsDifferent;
	private String testDate;
	private ArrayList<TestCase> testCases;
	private ArrayList<TestResult> testResults;

	public int getNumberOfTests() {
		return numberOfTests;
	}

	public void setNumberOfTests(int numberOfTests) {
		this.numberOfTests = numberOfTests;
	}

	public int getTestsEqual() {
		return testsEqual;
	}

	public void setTestsEqual(int testsEqual) {
		this.testsEqual = testsEqual;
	}

	public int getTestsDifferent() {
		return testsDifferent;
	}

	public void setTestsDifferent(int testsDifferent) {
		this.testsDifferent = testsDifferent;
	}

	public String getTestDate() {
		return testDate;
	}

	public void setTestDate(String testDate) {
		this.testDate = testDate;
	}

	public ArrayList<TestCase> getTestCases() {
		return testCases;
	}

	public void setTestCases(ArrayList<TestCase> testCases) {
		this.testCases = testCases;
	}

	public ArrayList<TestResult> getTestResults() {
		return testResults;
	}

	public void setTestResults(ArrayList<TestResult> testResults) {
		this.testResults = testResults;
	}

	public Report() {
		super();
	}

}
