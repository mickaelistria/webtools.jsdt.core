/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.core.tests.formatter.comment;

import java.util.Map;

import org.eclipse.wst.jsdt.core.formatter.CodeFormatter;
import org.eclipse.wst.jsdt.core.formatter.DefaultCodeFormatterConstants;

import junit.framework.Test;

import org.eclipse.wst.jsdt.internal.formatter.comment.MultiCommentLine;

public class MultiLineTestCase extends CommentTestCase {
	protected static final String INFIX= MultiCommentLine.MULTI_COMMENT_CONTENT_PREFIX;

	protected static final String POSTFIX= MultiCommentLine.MULTI_COMMENT_END_PREFIX;

	protected static final String PREFIX= MultiCommentLine.MULTI_COMMENT_START_PREFIX;

	public static Test suite() {
		return buildTestSuite(MultiLineTestCase.class);
	}

	public MultiLineTestCase(String name) {
		super(name);
	}

	protected int getCommentKind() {
		return CodeFormatter.K_MULTI_LINE_COMMENT;
	}

	public void testSingleLineComment1() {
		assertEquals("/*" + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX, testFormat("/*\t\t" + DELIMITER + "*\t test*/")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public void testSingleLineComment2() {
		assertEquals("/*" + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX, testFormat(PREFIX + "test" + DELIMITER + "\t" + POSTFIX)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public void testSingleLineComment3() {
		assertEquals("/*" + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX, testFormat(PREFIX + DELIMITER + "* test\t*/")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testSingleLineComment4() {
		assertEquals("/*" + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX, testFormat("/*test" + DELIMITER + POSTFIX)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testSingleLineCommentSpace1() {
		assertEquals(PREFIX + "test" + POSTFIX, testFormat("/*test*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentSpace2() {
		assertEquals(PREFIX + "test" + POSTFIX, testFormat("/*test" + POSTFIX)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentSpace3() {
		assertEquals(PREFIX + "test" + POSTFIX, testFormat(PREFIX + "test*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentSpace4() {
		assertEquals(PREFIX + "test test" + POSTFIX, testFormat("/* test   test*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentTabs1() {
		assertEquals(PREFIX + "test test" + POSTFIX, testFormat("/*\ttest\ttest" + POSTFIX)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentTabs2() {
		assertEquals(PREFIX + "test test" + POSTFIX, testFormat("/*\ttest\ttest*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * [formatting] formatter removes last line with block comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=51654
	 */
	public void testMultiLineCommentAsterisk1() {
		// test3 (currently) forces the comment formatter to actually do something, it wouldn't do anything otherwise.
		String input= PREFIX + INFIX + "test1" + DELIMITER + "test2" + INFIX + DELIMITER + "test3" + DELIMITER + "test4" + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		String result= testFormat(input);
		assertTrue(result.indexOf("test1") != -1); //$NON-NLS-1$
		assertTrue(result.indexOf("test2") != -1); //$NON-NLS-1$
		assertTrue(result.indexOf("test3") != -1); //$NON-NLS-1$
		assertTrue(result.indexOf("test4") != -1); //$NON-NLS-1$
	}
	
	public void testNoChange1() {
		String content= PREFIX + DELIMITER + POSTFIX;
		assertEquals(content, testFormat(content));
	}
	
	public void testNoFormat1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_BLOCK_COMMENT, DefaultCodeFormatterConstants.FALSE);
		String content= PREFIX + DELIMITER + INFIX + "test" + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX;
		assertEquals(content, testFormat(content));
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=145544
	 */
	public void testMultiLineCommentFor145544() {
		setUserOption(DefaultCodeFormatterConstants.getJavaConventionsSettings());
		String input= "/**\n" +  //$NON-NLS-1$
				" * Member comment\n" +//$NON-NLS-1$ 
				" */";//$NON-NLS-1$
		String result= testFormat(input, 0, input.length(), CodeFormatter.K_MULTI_LINE_COMMENT , 2);
		String expectedOutput = "/***********************************************************************\n" + 
				"	 * Member comment\n" + 
				"	 */";
		assertEquals("Different output", expectedOutput, result);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=75460
	public void test75460() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "200");
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_BLOCK_COMMENT, DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_SOURCE, DefaultCodeFormatterConstants.TRUE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_BLOCK_COMMENT, DefaultCodeFormatterConstants.FALSE);

		String input = "/*" + DELIMITER +
				"            var objects = new Array(3);" + DELIMITER +
				"            objects[0] = new String(\"Hallo Welt !!!\");" + DELIMITER +
				"            objects[1] = new String(\"Test !!!\");" + DELIMITER +
				"            objects[2] = new Number(\"1980\");" + DELIMITER +
				"            var objs = ObjectFile.read(pathname);" + DELIMITER +
				"            for(int i = 0; i < objs.length; i++)" + DELIMITER +
				"            {" + DELIMITER +
				"              alert(objs[i].toString());" + DELIMITER +
				"            }" + DELIMITER +
				"*/";
		
		String expected = input;
		String result=testFormat(input, options);
		assertEquals(expected, result);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=49412
	// check backward compatibility
	public void test49412() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "200");
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_BLOCK_COMMENT, DefaultCodeFormatterConstants.TRUE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_BLOCK_COMMENT, DefaultCodeFormatterConstants.TRUE);

		String input = "/*" + DELIMITER + DELIMITER +
				" test block comment with a blank line" + DELIMITER +
				"*/";
		
		String expected= "/*" + DELIMITER +
		" * test block comment with a blank line" + DELIMITER +
		" */";

		String result=testFormat(input, options);
		assertEquals(expected, result);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=49412
	// check backward compatibility
	public void test49412_2() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "200");
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_BLOCK_COMMENT, DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_BLOCK_COMMENT, DefaultCodeFormatterConstants.TRUE);

		String input = "/*" + DELIMITER + DELIMITER +
				" test block comment with a blank line" + DELIMITER +
				"*/";
		
		String expected= input;

		String result=testFormat(input, options);
		assertEquals(expected, result);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=49412
	// check backward compatibility
	public void test49412_3() {
		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "200");
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_BLOCK_COMMENT, DefaultCodeFormatterConstants.TRUE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_BLOCK_COMMENT, DefaultCodeFormatterConstants.FALSE);

		String input = "/*" + DELIMITER + DELIMITER +
				" test block comment with a blank line" + DELIMITER +
				"*/";
		
		String expected= "/*" + DELIMITER +
		" * "+ DELIMITER +
		" * test block comment with a blank line" + DELIMITER +
		" */";

		String result=testFormat(input, options);
		assertEquals(expected, result);
	}
}
