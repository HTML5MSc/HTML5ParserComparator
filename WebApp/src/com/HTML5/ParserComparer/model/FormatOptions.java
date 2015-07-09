package com.HTML5.ParserComparer.model;

public class FormatOptions {
	private boolean prettify = false;
	private boolean originalOutput = false;
	private boolean removeTextAfterLastDiff = true;
	private boolean removeScriptContent = true;
	private boolean removeStyleContent = true;
	private boolean removeMetaContent = true;
	private boolean removeLinkContent = true;
	private boolean removeComments = true;
	
	public boolean isPrettify() {
		return prettify;
	}
	public void setPrettify(boolean prettify) {
		this.prettify = prettify;
	}
	public boolean isOriginalOutput() {
		return originalOutput;
	}
	public void setOriginalOutput(boolean originalOutput) {
		this.originalOutput = originalOutput;
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
	public boolean isRemoveMetaContent() {
		return removeMetaContent;
	}
	public void setRemoveMetaContent(boolean removeMetaContent) {
		this.removeMetaContent = removeMetaContent;
	}
	public boolean isRemoveLinkContent() {
		return removeLinkContent;
	}
	public void setRemoveLinkContent(boolean removeLinkContent) {
		this.removeLinkContent = removeLinkContent;
	}
	public boolean isRemoveComments() {
		return removeComments;
	}
	public void setRemoveComments(boolean removeComments) {
		this.removeComments = removeComments;
	}	
}