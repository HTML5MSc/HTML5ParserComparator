package com.html5tools.Utils;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.html5tools.Utils.DiffEntry.DiffOperation;

public class DiffUtilsTest {

	@Test
	public void testGetDiffsWhenNullInputThenEmptyList() throws Exception {
		String diffsString = null;

		List<DiffEntry> diffs = DiffUtils.getDiffs(diffsString);

		assertEquals(0, diffs.size());
	}

	@Test
	public void testGetDiffsWhenEmptyInputThenEmptyList() throws Exception {
		String diffsString = "";

		List<DiffEntry> diffs = DiffUtils.getDiffs(diffsString);

		assertEquals(0, diffs.size());
	}

	@Test(expected = Exception.class)
	public void testGetDiffsWhenMalformedDiffOperationThenException()
			throws Exception {
		String diffsString = "a";

		DiffUtils.getDiffs(diffsString);
	}

	@Test(expected = Exception.class)
	public void testGetDiffsWhenNoCommaSeparatorThenException()
			throws Exception {
		String diffsString = "+a";

		DiffUtils.getDiffs(diffsString);
	}

	@Test(expected = NumberFormatException.class)
	public void testGetDiffsWhenMalformedDiffIndexThenException()
			throws Exception {
		String diffsString = "+a,";

		DiffUtils.getDiffs(diffsString);
	}

	@Test(expected = Exception.class)
	public void testGetDiffsWhenNoNewLineSeparatorThenException()
			throws Exception {
		String diffsString = "+1,a";

		DiffUtils.getDiffs(diffsString);
	}

	@Test(expected = NumberFormatException.class)
	public void testGetDiffsWhenMalformedDiffCharsThenException()
			throws Exception {
		String diffsString = "+1,a,";

		DiffUtils.getDiffs(diffsString);
	}

	@Test
	public void testGetDiffsOneDiff() throws Exception {
		String diffsString = "+3,7,1234567;";

		List<DiffEntry> diffs = DiffUtils.getDiffs(diffsString);

		assertEquals("Incorrect diff size", 1, diffs.size());
		assertEquals("Incorrect diff operation", DiffOperation.INSERT, diffs
				.get(0).getDiffOperation());
		assertEquals("Incorrect diff index", 3, diffs.get(0).getIndex());
		assertEquals("Incorrect diff content", "1234567", diffs.get(0)
				.getContent());
	}

	@Test
	public void testGetDiffsManyDiffs() throws Exception {
		String diffsString = "-12,2,,\t;";
		diffsString += "+22,4,﨟〃々〻;";
		diffsString += "-32,3,123;";

		List<DiffEntry> diffs = DiffUtils.getDiffs(diffsString);

		assertEquals("Incorrect diff size", 3, diffs.size());
		assertEquals("Incorrect diff operation", DiffOperation.DELETE, diffs
				.get(0).getDiffOperation());
		assertEquals("Incorrect diff index", 32, diffs.get(2).getIndex());
		assertEquals("Incorrect diff content", "﨟〃々〻", diffs.get(1)
				.getContent());
	}

	@Test
	public void testGetOriginalFromOneDiff() {
		String base = "test string";
		String expected = "test new string";

		String diffs = DiffUtils.getFormattedDiffs(base, expected);
		String reconstructed = DiffUtils.getOriginalFromDiffs(base, diffs);

		assertEquals("Incorrect string", expected, reconstructed);
	}

	@Test
	public void testGetOriginalFromManyDiffs() {
		String base = "test string";
		String expected = "best strings";

		String diffs = DiffUtils.getFormattedDiffs(base, expected);
		String reconstructed = DiffUtils.getOriginalFromDiffs(base, diffs);

		assertEquals("Incorrect string", expected, reconstructed);
	}
}