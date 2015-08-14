package com.HTML5.ParserComparer.model;

import java.util.List;

public class TracerInput {
	private boolean prettify = false;
	private String input;
	private String type;
	private List<String> eventTypes;
	private List<String> sections;
	
	public boolean isPrettify() {
		return prettify;
	}
	public void setPrettify(boolean prettify) {
		this.prettify = prettify;
	}
	public String getInput() {
		return input;
	}
	public void setInput(String input) {
		this.input = input;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getEventTypes() {
		return eventTypes;
	}
	public void setEventTypes(List<String> eventTypes) {
		this.eventTypes = eventTypes;
	}
	public List<String> getSections() {
		return sections;
	}
	public void setSections(List<String> sections) {
		this.sections = sections;
	}

}