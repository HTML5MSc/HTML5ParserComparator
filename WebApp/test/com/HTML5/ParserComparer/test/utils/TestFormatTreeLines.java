package com.HTML5.ParserComparer.test.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.HTML5.ParserComparer.model.utils.FormattingUtils;

public class TestFormatTreeLines {

	private static final String SPAN_INS = "<span class=\"line_ins\">";
	private static final String SPAN_DEL = "<span class=\"line_del\">";
	private static final String SPAN_CLOSE = "</span>";

	@Test
	public void testNull() {
		String input = null;
		String expected = null;

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected null", expected, output);
	}

	@Test
	public void testEmpty() {
		String input = "";
		String expected = "";

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected empty", expected, output);
	}

	@Test
	public void testNoFormat() {
		String input = "test";
		String expected = "test";

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected no format", expected, output);
	}

	@Test
	public void testOneIns() {
		String input = "<ins>test</ins>";
		String expected = SPAN_INS + "<ins>test</ins>" + SPAN_CLOSE;

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected one ins format", expected, output);
	}

	@Test
	public void testOneDel() {
		String input = "<del>test</del>";
		String expected = SPAN_DEL + "<del>test</del>" + SPAN_CLOSE;

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected one del format", expected, output);
	}

	@Test
	public void testMultiLineIns() {
		String input = "<ins>line1\n" + "line2</ins>";
		String expected = SPAN_INS + "<ins>line1\n" + "line2</ins>"
				+ SPAN_CLOSE;

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected multi line format", expected, output);
	}

	@Test
	public void testMultiLineDel() {
		String input = "<del>line1\n" + "line2</del>";
		String expected = SPAN_DEL + "<del>line1\n" + "line2</del>"
				+ SPAN_CLOSE;

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected multiple line format", expected, output);
	}

	@Test
	public void testSingleLineMultipleIns() {
		String input = "test<ins>data1</ins>data2"
				+ "<ins>added</ins>data3<ins>qwerty</ins>azerty";
		String expected = SPAN_INS + "test<ins>data1</ins>data2"
				+ "<ins>added</ins>data3<ins>qwerty</ins>azerty" + SPAN_CLOSE;

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected multiple ins single line format", expected,
				output);
	}

	@Test
	public void testMultiLineMultipleIns() {
		String input = "test\n" + "<ins>line1</ins>abcd\n"
				+ "line2<ins>added</ins>";
		String expected = "test\n" + SPAN_INS + "<ins>line1</ins>abcd"
				+ SPAN_CLOSE + "\n" + SPAN_INS + "line2<ins>added</ins>"
				+ SPAN_CLOSE;

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected multiple ins multi line format", expected,
				output);
	}

	@Test
	public void testMultiLineMultipleInsNewLine() {
		String input = "test\n" + "abc<ins>line1\n" + "line2</ins>added";
		String expected = "test\n" + SPAN_INS + "abc<ins>line1\n"
				+ "line2</ins>added" + SPAN_CLOSE;

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected multiple ins multi line format", expected,
				output);
	}

	@Test
	public void testMultiLineMultipleInsNewLineEmpty() {
		String input = "test\n" + "abc<ins>line1\n" + "</ins>";
		String expected = "test\n" + SPAN_INS + "abc<ins>line1\n" + "</ins>"
				+ SPAN_CLOSE;

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected multiple ins multi line format", expected,
				output);
	}

	@Test
	public void testSingleLineMultipleDel() {
		String input = "test<del>data1</del>data2"
				+ "<del>added</del>data3<del>qwerty</del>azerty";
		String expected = SPAN_DEL + "test<del>data1</del>data2"
				+ "<del>added</del>data3<del>qwerty</del>azerty" + SPAN_CLOSE;

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected multiple del single line format", expected,
				output);
	}

	@Test
	public void testMultiLineMultipleDel() {
		String input = "test\n" + "<del>line1</del>abcd\n"
				+ "line2<del>added</del>";
		String expected = "test\n" + SPAN_DEL + "<del>line1</del>abcd"
				+ SPAN_CLOSE + "\n" + SPAN_DEL + "line2<del>added</del>"
				+ SPAN_CLOSE;

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected multiple del multi line format", expected,
				output);
	}

	@Test
	public void testMultiLineMultipleDelNewLine() {
		String input = "test\n" + "abc<del>line1\n" + "line2</del>added";
		String expected = "test\n" + SPAN_DEL + "abc<del>line1\n"
				+ "line2</del>added" + SPAN_CLOSE;

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected multiple del multi line format", expected,
				output);
	}

	@Test
	public void testMultiLineMultipleDelNewLineEmpty() {
		String input = "test\n" + "abc<del>line1\n" + "</del>";
		String expected = "test\n" + SPAN_DEL + "abc<del>line1\n" + "</del>"
				+ SPAN_CLOSE;

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected multiple del multi line format", expected,
				output);
	}

	@Test
	public void testMultiLineCombinedInsDel() {
		String input = "test\n" + "<ins>line1</ins>abcd\n"
				+ "line2<del>added</del>";
		String expected = "test\n" + SPAN_INS + "<ins>line1</ins>abcd"
				+ SPAN_CLOSE + "\n" + SPAN_DEL + "line2<del>added</del>"
				+ SPAN_CLOSE;

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected combined multi line format", expected, output);
	}

	@Test
	public void testMultiLineCombinedInsDelNewLine() {
		/*
		 * Input:
		 * 
		 * test
		 * 
		 * <ins>line1</ins>ab<ins>cd
		 * 
		 * </ins>line2<del>added</del>
		 */
		String input = "test\n" + "<ins>line1</ins>ab<ins>cd\n"
				+ "</ins>line2<del>added</del>";
		String expected = "test\n" + SPAN_INS + "<ins>line1</ins>ab<ins>cd"
				+ "\n</ins>line2<del>added</del>" + SPAN_CLOSE;

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected combined multi line format", expected, output);
	}

	@Test
	public void testMultiLineCombinedInsDelNewLine2() {
		/*
		 * Input:
		 * 
		 * test
		 * 
		 * <ins>line1</ins>ab<ins>cd
		 * 
		 * </ins>line2<del>added</del>
		 */
		String input = "|     xxx<ins>\n" 
				+ "|     </ins> xxx <ins>\n"		
				+ "|     </ins>x";
		String expected = SPAN_INS + "|     xxx<ins>\n" 
				+ "|     </ins> xxx <ins>\n"		
				+ "|     </ins>x" + SPAN_CLOSE;

		String output = FormattingUtils.formatTreeLines(input);

		assertEquals("Expected combined multi line format", expected, output);
	}
}
