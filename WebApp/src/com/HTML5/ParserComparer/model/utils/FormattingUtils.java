package com.HTML5.ParserComparer.model.utils;

import java.util.ArrayList;
import java.util.List;

public class FormattingUtils {

	private static final String SPAN_INS = "<span class=\"line_ins\">";
	private static final String SPAN_DEL = "<span class=\"line_del\">";
	private static final String SPAN_CLOSE = "</span>";

	public static String formatTreeLines(String tree) {
		if (tree == null || tree.equals(""))
			return tree;

		StringBuilder reconstructedTree = new StringBuilder();
		boolean isOpen = false;
		for (String s : tree.split("\n")) {

			List<Integer> insStartIndexes = getIndexesOf(s, "<ins>");
			List<Integer> insEndIndexes = getIndexesOf(s, "</ins>");
			List<Integer> delStartIndexes = getIndexesOf(s, "<del>");
			List<Integer> delEndIndexes = getIndexesOf(s, "</del");
			int closingTags = insEndIndexes.size() + delEndIndexes.size();
			int startTags = insStartIndexes.size() + delStartIndexes.size();

			if (insStartIndexes.size() > 0 && delStartIndexes.size() > 0) {

				if (isOpen) {
					if (closingTags > startTags) {
						isOpen = false;
						s += SPAN_CLOSE;
					}
				} else {
					if (insStartIndexes.get(0) > delStartIndexes.get(0))
						s = SPAN_DEL + s;
					else
						s = SPAN_INS + s;

					if (closingTags == startTags)
						s += SPAN_CLOSE;
					else
						isOpen = true;
				}
			} else if (insStartIndexes.size() > 0
					&& delStartIndexes.size() == 0) {

				if (isOpen) {
					if (closingTags > startTags) {
						isOpen = false;
						s += SPAN_CLOSE;
					}
				} else {
					s = SPAN_INS + s;
					if (closingTags == startTags)
						s += SPAN_CLOSE;
					else
						isOpen = true;
				}

			} else if (insStartIndexes.size() == 0
					&& delStartIndexes.size() > 0) {

				if (isOpen) {
					if (closingTags > startTags) {
						isOpen = false;
						s += SPAN_CLOSE;
					}
				} else {
					s = SPAN_DEL + s;
					if (closingTags == startTags)
						s += SPAN_CLOSE;
					else
						isOpen = true;
				}
			} else if (closingTags > 0) {
				s += SPAN_CLOSE;
				isOpen = false;
			}

			reconstructedTree.append(s + "\n");
		}

		return reconstructedTree.toString().substring(0,
				reconstructedTree.length() - 1);
	}

	public static List<Integer> getIndexesOf(String word, String value) {
		List<Integer> indexes = new ArrayList<Integer>();
		for (int index = word.indexOf(value); index != -1; index = word
				.indexOf(value, index + 1)) {
			indexes.add(index);
		}
		return indexes;
	}

	public static List<String> getSubStringsOf(String word, String startValue,
			String endValue, boolean appendEndValue) {
		List<String> indexes = new ArrayList<String>();
		for (int index = word.indexOf(startValue); index != -1; index = word
				.indexOf(startValue, index + 1)) {
			int startIndex = word.substring(0, index).lastIndexOf("\n| ");
			int endIndex = word.indexOf(endValue, index);
			if (endIndex != -1) {
				endIndex += appendEndValue ? endValue.length() : 0;
				indexes.add(word.substring(startIndex, endIndex));
			}
		}
		return indexes;
	}
	
	public static List<String> getSubStringsOf(String word, String startValue) {
		List<String> indexes = new ArrayList<String>();
		for (int index = word.indexOf(startValue); index != -1; index = word
				.indexOf(startValue, index + 1)) {
			int startIndex = word.substring(0, index).lastIndexOf("\n| ");
			String endValue = word.substring(startIndex, index).concat("&lt;");
			int endIndex = word.indexOf(endValue, index);
			if (endIndex != -1) {				
				indexes.add(word.substring(startIndex, endIndex));
			}
		}
		return indexes;
	}
}
