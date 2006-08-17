/**
 * 
 */
package org.owasp.webscarab.util;

import java.util.List;

import org.owasp.webscarab.util.Diff.Edit;

import junit.framework.TestCase;

/**
 * @author rdawes
 * 
 */
public class DiffTest extends TestCase {

	public static void main(String[] args) {
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// /*
	// * Test method for 'org.owasp.webscarab.util.Diff.getEditsByLine(String,
	// String)'
	// */
	// public void btestGetEditsByLine() {
	// String src;
	// String dst;
	// List<Edit> edits;
	// CharSequence result;
	// src = "abc\ndef\nghi";
	// dst = "def\nghi\njkl\n";
	// edits = Diff.getEditsByLine(src, dst);
	// for (int i=0; i<edits.size(); i++)
	// System.out.println("Edit: " + edits.get(i));
	//		
	// result = Diff.applyEdits(src, dst, edits);
	// if (!result.equals(dst)) {
	// System.err.println("Failed! '" + result + "' != '" + dst + "'");
	// } else {
	// System.err.println("Success!!");
	// }
	// }

	/*
	 * Test method for 'org.owasp.webscarab.util.Diff.getEdits(CharSequence,
	 * CharSequence)'
	 */
	public void testGetEdits() {
		String src;
		String dst;
		List<Edit> edits;
		CharSequence result;
		src = "quicklyquic";
		dst = "quincequick";
		edits = Diff.getEdits(src, dst);
		checkForOverlaps(edits);
		for (int i = 0; i < edits.size(); i++)
			System.out.println(edits.get(i));

		// assertEquals("Wrong distance", 5, Diff.getDistance(edits));

		result = Diff.apply(src, edits);
		if (!result.equals(dst)) {
			System.err.println("Failed! '" + result + "' != '" + dst + "'");
		} else {
			System.err.println("Success!!");
		}
	}

	/*
	 * Test method for 'org.owasp.webscarab.util.Diff.getEdit(CharSequence,
	 * int, int)'
	 */
	public void testGetEdit() {
		// TODO Auto-generated method stub

	}

	/*
	 * Test method for 'org.owasp.webscarab.util.Diff.printEdit(Edit,
	 * CharSequence, CharSequence)'
	 */
	public void testPrintEdit() {
		// TODO Auto-generated method stub

	}

	private void checkForOverlaps(List<Diff.Edit> edits) {
		Diff.Edit prev = null;
		for (int i = 0; i < edits.size(); i++) {
			Edit edit = edits.get(i);
			if (prev != null) {
				if (edit.getSrcLocation() < prev.getSrcLocation()
						+ prev.getSrc().length()
						|| edit.getDstLocation() < prev.getDstLocation()
								+ prev.getDst().length()) {
					throw new IllegalStateException("Edits may not overlap: ("
							+ prev + ") - (" + edit + ")");
				}
			}
			prev = edit;
		}
	}

	/*
	 * Test method for 'org.owasp.webscarab.util.Diff.applyEdits(CharSequence,
	 * CharSequence, List<Edit>)'
	 */
	public void testApply() {
		// TODO Auto-generated method stub

	}

}
