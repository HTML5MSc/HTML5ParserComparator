package com.HTML5.ParserComparer.model;

public class FormatOptions {
	private boolean prettify = false;
	private boolean removeTextAfterLastDiff = true;
	private boolean removeScriptContent = true;
	private boolean removeStyleContent = true;
	private boolean removeComments = true;
	
	public boolean isPrettify() {
		return prettify;
	}
	public void setPrettify(boolean prettify) {
		this.prettify = prettify;
	}
	public boolean isRemoveTextAfterLastDiff() {
		return removeTextAfterLastDiff;
	}
	public void setRemoveTextAfterLastDiff(boolean removeTextAfterLastDiff) {
		this.removeTextAfterLastDiff = removeTextAfterLastDiff;
	}
	public boolean isRemoveScriptContent() {
		return removeScriptContent;
	}
	public void setRemoveScriptContent(boolean removeScriptContent) {
		this.removeScriptContent = removeScriptContent;
	}
	public boolean isRemoveStyleContent() {
		return removeStyleContent;
	}
	public void setRemoveStyleContent(boolean removeStyleContent) {
		this.removeStyleContent = removeStyleContent;
	}
	public boolean isRemoveComments() {
		return removeComments;
	}
	public void setRemoveComments(boolean removeComments) {
		this.removeComments = removeComments;
	}	
}