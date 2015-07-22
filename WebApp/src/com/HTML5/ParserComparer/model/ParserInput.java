package com.HTML5.ParserComparer.model;

public class ParserInput {

	private String type;
	private String value;

	public ParserInput() {
		super();
	}

	public ParserInput(String type, String value) {
		super();
		this.type = type;
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getTypeParameter() {
		switch (type) {
		case "URL":
			return "-u";
		case "File":
			return "-f";
		case "String":
		default:
			return "-s";
		}
	}
}