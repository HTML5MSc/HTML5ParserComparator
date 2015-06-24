package com.html5tools.parserComparison.Test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.html5tools.parserComparison.OutputTree;

public class OutputTreeOrderTest {

	@Test
	public void testMajorityFirst1() {
		String[] args = new String[] { "test", "test.xml", "a", "x", "b", "y",
				"c", "z", "b", "w" };
		List<OutputTree> outputs = new ArrayList<OutputTree>();
		for (int i = 2; i < args.length; i = i + 2) {
			OutputTree tree = new OutputTree(args[i], args[i + 1]);
			if (!outputs.contains(tree))
				outputs.add(tree);
			else
				outputs.get(outputs.indexOf(tree)).addParser(args[i + 1]);
		}

		List<String> expectedParsers = new ArrayList<String>();
		expectedParsers.add("y");
		expectedParsers.add("w");

		Collections.sort(outputs);
		assertTrue(outputs.get(0).getParsers().equals(expectedParsers));
	}

	@Test
	public void testMajorityFirst2() {
		String[] args = new String[] { "test", "test.xml", "a", "x", "b", "y",
				"c", "z" };
		List<OutputTree> outputs = new ArrayList<OutputTree>();
		for (int i = 2; i < args.length; i = i + 2) {
			OutputTree tree = new OutputTree(args[i], args[i + 1]);
			if (!outputs.contains(tree))
				outputs.add(tree);
			else
				outputs.get(outputs.indexOf(tree)).addParser(args[i + 1]);
		}

		List<String> expectedParsers = new ArrayList<String>();
		expectedParsers.add("x");

		Collections.sort(outputs);
		assertTrue(outputs.get(0).getParsers().equals(expectedParsers));
	}

	@Test
	public void testMajorityFirst3() {
		String[] args = new String[] { "test", "test.xml", "a", "x", "b", "y",
				"c", "z", "b", "w", "c", "r", "c", "s" };
		List<OutputTree> outputs = new ArrayList<OutputTree>();
		for (int i = 2; i < args.length; i = i + 2) {
			OutputTree tree = new OutputTree(args[i], args[i + 1]);
			if (!outputs.contains(tree))
				outputs.add(tree);
			else
				outputs.get(outputs.indexOf(tree)).addParser(args[i + 1]);
		}

		List<String> expectedParsers = new ArrayList<String>();
		expectedParsers.add("z");
		expectedParsers.add("r");
		expectedParsers.add("s");

		Collections.sort(outputs);
		assertTrue(outputs.get(0).getParsers().equals(expectedParsers));
	}
}
