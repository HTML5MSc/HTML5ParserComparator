package com.html5tools.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.html5tools.Utils.DiffEntry.DiffOperation;
import com.html5tools.Utils.diff_match_patch.Diff;
import com.html5tools.Utils.diff_match_patch.Operation;

public class DiffUtils {
	
	private static final String SEPARATOR = ",";
	
	public static String getFormattedDiffs(String string1, String string2) {
		StringBuilder sb = new StringBuilder();

		diff_match_patch dmp = new diff_match_patch();
		LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(string1,
				string2, false);
		dmp.Diff_EditCost = 1;
		dmp.diff_cleanupEfficiency(diffs);

		int index = 0;
		for (Diff diff : diffs) {
			if (diff.operation != Operation.EQUAL) {
				sb.append(diff.operation == Operation.INSERT ? "+" : "-");
				sb.append(index);
				sb.append(SEPARATOR);
				sb.append(diff.text.length());
				sb.append(SEPARATOR);
				sb.append(diff.text);
				sb.append(";");
			}
			index += diff.text.length();
		}

		return sb.toString();
	}

	public static String getOriginalFromDiffs(String baseString,
			String diffsString) {
		StringBuilder reconstructedString = new StringBuilder(baseString);
		List<DiffEntry> diffs = new ArrayList<DiffEntry>();
		try {
			diffs.addAll(getDiffs(diffsString));
		} catch (Exception e) {
		}

		int index = 0;
		int indexOffset = 0;
		for (DiffEntry diff : diffs) {
			DiffOperation type = diff.getDiffOperation();
			String content = diff.getContent();
			String newContent = null;
			index = diff.getIndex();

			if (type == DiffOperation.INSERT) {
				newContent = content;
			} else if (type == DiffOperation.DELETE) {
				reconstructedString.delete(index + indexOffset, index
						+ indexOffset + content.length());
				newContent = "";
			}
			reconstructedString.insert(index + indexOffset, newContent);
			indexOffset += newContent.length() - content.length();
		}

		return reconstructedString.toString();
	}

	public static List<DiffEntry> getDiffs(String diffsString) throws Exception {
		List<DiffEntry> diffs = new ArrayList<DiffEntry>();
		if (diffsString == null || diffsString.isEmpty())
			return diffs;

		int pointer = 0;
		int separatorPointer = 0;
		while (pointer < diffsString.length()) {
			DiffOperation diffOperation;
			int index;
			int charsToRead;
			String content;

			separatorPointer = pointer + 1;
			content = diffsString.substring(pointer, separatorPointer);

			if (content.equals("+"))
				diffOperation = DiffOperation.INSERT;
			else if (content.equals("-"))
				diffOperation = DiffOperation.DELETE;
			else
				throw new Exception("Unexpected character");

			pointer = separatorPointer;
			separatorPointer = diffsString.indexOf(SEPARATOR, pointer);
			if (separatorPointer == -1)
				throw new Exception("Character separator not found");
			index = Integer.valueOf(diffsString.substring(pointer,
					separatorPointer));

			pointer = separatorPointer + 1;
			separatorPointer = diffsString.indexOf(SEPARATOR, pointer);
			if (separatorPointer == -1)
				throw new Exception("Character separator not found");
			charsToRead = Integer.valueOf(diffsString.substring(pointer,
					separatorPointer));

			pointer = separatorPointer + 1;
			content = diffsString.substring(pointer, pointer + charsToRead);
			pointer += charsToRead + 1;

			diffs.add(new DiffEntry(index, diffOperation, content));
		}
		return diffs;
	}
}