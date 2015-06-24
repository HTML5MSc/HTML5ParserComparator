package com.HTML5.ParserComparer.model;

public class WebConfig {
	String bashScriptName;
	String bashScriptPath;
	String reportPath;

	public String getBashScriptName() {
		return bashScriptName;
	}

	public void setBashScriptName(String bashScriptName) {
		this.bashScriptName = bashScriptName;
	}

	public String getBashScriptPath() {
		return bashScriptPath;
	}

	public void setBashScriptPath(String bashScriptPath) {
		this.bashScriptPath = bashScriptPath;
	}

	public String getReportPath() {
		return reportPath;
	}

	public void setReportPath(String reportPath) {
		this.reportPath = reportPath;
	}
	
	public String getBashScriptFullPath() {
		return bashScriptPath.concat(bashScriptName);
	}
}