package com.html5tools.Utils;

public class DiffEntry {

	public enum DiffOperation {
		DELETE, INSERT
	}

	private int index;
	private DiffOperation diffOperation;
	private String content;

	public DiffEntry(int index, DiffOperation diffOperation, String content) {
		super();
		this.index = index;
		this.diffOperation = diffOperation;
		this.content = content;
	}

	public int getIndex() {
		return index;
	}

	public DiffOperation getDiffOperation() {
		return diffOperation;
	}

	public String getContent() {
		return content;
	}
}