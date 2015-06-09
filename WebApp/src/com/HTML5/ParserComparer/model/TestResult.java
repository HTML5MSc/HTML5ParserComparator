package com.HTML5.ParserComparer.model;

public class TestResult {
	private String parserName;
	private int passed;
	private int failed;
	
	public TestResult(String parserName, int passed, int failed) {
		super();
		this.parserName = parserName;
		this.passed = passed;
		this.failed = failed;
	}
	
	public String getParserName() {
		return parserName;
	}
	public void setParserName(String parserName) {
		this.parserName = parserName;
	}
	public int getPassed() {
		return passed;
	}
	public void setPassed(int passed) {
		this.passed = passed;
	}
	public int getFailed() {
		return failed;
	}
	public void setFailed(int failed) {
		this.failed = failed;
	}
}
