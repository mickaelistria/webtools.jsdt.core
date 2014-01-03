/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.internal.compiler.impl.CompilerOptions;

/**
 * Test class for completion in text of a Javadoc comment.
 */
public class JavadocTextCompletionModelTest extends AbstractJavadocCompletionModelTest {

public JavadocTextCompletionModelTest(String name) {
	super(name);
}

static {
//	TESTS_NUMBERS = new int[] { 34 };
//	TESTS_RANGE = new int[] { 51, -1 };
}
public static Test suite() {
	return buildModelTestSuite(JavadocTextCompletionModelTest.class);
}

/* (non-Javadoc)
 * @see org.eclipse.wst.jsdt.core.tests.model.AbstractJavadocCompletionModelTest#setUp()
 */
protected void setUp() throws Exception {
	super.setUp();
	setUpProjectOptions(CompilerOptions.VERSION_1_4);
}

/**
 * @tests  Tests for tag names completion
 */
public void test001() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"/**\n" +
		" * Completion on @ inside text\n" +
		" */\n" +
		"public class BasicTestTextIns {}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "@");
	assertResults(
		"link[JSDOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"docRoot[JSDOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"linkplain[JSDOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"value[JSDOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test002() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"/**\n" +
		" * Completion on @s inside text\n" +
		" */\n" +
		"public class BasicTestTextIns {}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "@s");
	assertResults("");
}

public void test003() throws JavaScriptModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_4);
	String source =
		"package javadoc.text;\n" +
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion on @ inside text\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "@");
	assertResults(
		"link[JSDOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"docRoot[JSDOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"linkplain[JSDOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"value[JSDOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test004() throws JavaScriptModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_4);
	String source =
		"package javadoc.text;\n" +
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion on @d inside text\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "@d");
	assertResults(
		"docRoot[JSDOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test005() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion on empty tag name: @\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "@");
	assertResults(
		"link[JSDOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"docRoot[JSDOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"inheritDoc[JSDOC_INLINE_TAG]{{@inheritDoc}, null, null, inheritDoc, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"linkplain[JSDOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"value[JSDOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test006() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion on impossible tag name: @ret\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "@ret");
	assertResults("");
}

public void test007() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion on one letter: @l\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "@l");
	assertResults(
		"link[JSDOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"linkplain[JSDOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test008() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion on started inline tag: {@li\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "{@li");
	assertResults(
		"link[JSDOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"linkplain[JSDOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test009() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion with several letters:\n" +
		"	 *		@param str @inh\n" +
		"	 */\n" +
		"	public void foo(String str) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "@inh");
	assertResults(
		"inheritDoc[JSDOC_INLINE_TAG]{{@inheritDoc}, null, null, inheritDoc, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test010() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion with several letters:\n" +
		"	 *		@param str {@inh\n" +
		"	 */\n" +
		"	public void foo(String str) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "{@inh");
	assertResults(
		"inheritDoc[JSDOC_INLINE_TAG]{{@inheritDoc}, null, null, inheritDoc, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test011() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" +
		"	/**\n" +
		"	 * Completion on full tag name: {@docRoot}\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "{@docRoot");
	assertResults(
		"docRoot[JSDOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

/**
 * @tests  Tests for types completion
 */
public void test020() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"/**\n" + 
		" * Completion after: BasicTestTextIns\n" + 
		" */\n" + 
		"public class BasicTestTextIns {\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "BasicTestTextIns");
	assertSortedResults(
		"BasicTestTextIns[JSDOC_TYPE_REF]{{@link BasicTestTextIns}, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+R_DICENUNRIT+"}\n" + 
		"BasicTestTextIns[TYPE_REF]{BasicTestTextIns, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+R_DICENUNR+"}"
	);
}

public void test021() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"/**\n" + 
		" * Completion after: {@link BasicTestTextIns\n" + 
		" */\n" + 
		"public class BasicTestTextIns {\n" + 
		"}\n" + 
		"class BasicTestTextInsException extends Exception{\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "BasicTestTextIns");
	assertSortedResults(
		"BasicTestTextIns[TYPE_REF]{BasicTestTextIns, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+R_DICENUNR+"}\n" + 
		"BasicTestTextInsException[TYPE_REF]{BasicTestTextInsException, javadoc.text, Ljavadoc.text.BasicTestTextInsException;, null, null, "+this.positions+R_DICUNR+"}"
	);
}

public void test022() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"/**\n" + 
		" * Completion after: @link BasicTestTextIns\n" + 
		" */\n" + 
		"public class BasicTestTextIns {\n" + 
		"	public void foo() throws BasicTestTextInsException {}\n" + 
		"}\n" + 
		"class BasicTestTextInsException extends Exception{\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "BasicTestTextIns");
	assertSortedResults(
		"BasicTestTextIns[JSDOC_TYPE_REF]{{@link BasicTestTextIns}, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+(R_DICENUNR+R_INLINE_TAG)+"}\n" +
		"BasicTestTextInsException[JSDOC_TYPE_REF]{{@link BasicTestTextInsException}, javadoc.text, Ljavadoc.text.BasicTestTextInsException;, null, null, "+this.positions+R_DICUNRIT+"}\n" + 
		"BasicTestTextIns[TYPE_REF]{BasicTestTextIns, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+R_DICENUNR+"}\n" +
		"BasicTestTextInsException[TYPE_REF]{BasicTestTextInsException, javadoc.text, Ljavadoc.text.BasicTestTextInsException;, null, null, "+this.positions+R_DICUNR+"}"
	);
}

public void test023() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: @link BasicTestTextIns\n" + 
		"	 */\n" + 
		"	public void foo() throws BasicTestTextInsException {}\n" + 
		"}\n" + 
		"class BasicTestTextInsException extends Exception{\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "BasicTestTextIns", 2); // 2nd occurrence
	assertSortedResults(
		"BasicTestTextInsException[JSDOC_TYPE_REF]{{@link BasicTestTextInsException}, javadoc.text, Ljavadoc.text.BasicTestTextInsException;, null, null, "+this.positions+R_DICUNREETIT+"}\n" + 
		"BasicTestTextIns[JSDOC_TYPE_REF]{{@link BasicTestTextIns}, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+R_DICENUNRIT+"}\n" +
		"BasicTestTextInsException[TYPE_REF]{BasicTestTextInsException, javadoc.text, Ljavadoc.text.BasicTestTextInsException;, null, null, "+this.positions+R_DICUNREET+"}\n" + 
		"BasicTestTextIns[TYPE_REF]{BasicTestTextIns, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+R_DICENUNR+"}"
	);
}

public void test024() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"/**\n" + 
		" * Completion after: @see BasicTestTextIns\n" + 
		" */\n" + 
		"public class BasicTestTextIns {\n" + 
		"	public void foo() {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "BasicTestTextIns");
	assertSortedResults(
		"BasicTestTextIns[JSDOC_TYPE_REF]{{@link BasicTestTextIns}, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+R_DICENUNRIT+"}\n" + 
		"BasicTestTextIns[TYPE_REF]{BasicTestTextIns, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+R_DICENUNR+"}"
	);
}

public void test025() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: @see BasicTestTextIns\n" + 
		"	 */\n" + 
		"	public void foo() {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "BasicTestTextIns", 2); // 2nd occurrence
	assertSortedResults(
		"BasicTestTextIns[JSDOC_TYPE_REF]{{@link BasicTestTextIns}, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+R_DICENUNRIT+"}\n" + 
		"BasicTestTextIns[TYPE_REF]{BasicTestTextIns, javadoc.text, Ljavadoc.text.BasicTestTextIns;, null, null, "+this.positions+R_DICENUNR+"}"
	);
}

public void test026() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"/**\n" + 
		" * Completion after: java.la\n" + 
		" */\n" + 
		"public class BasicTestTextIns {\n" + 
		"	public void foo() throws InterruptedException {\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "java.la");
	assertSortedResults(
		"java.lang[PACKAGE_REF]{java.lang, java.lang, null, null, null, "+this.positions+R_DICQNR+"}"
	);
}

public void test027() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"/**\n" + 
		" * Completion after: java.lang.I\n" + 
		" */\n" + 
		"public class BasicTestTextIns {\n" + 
		"	public void foo() throws InterruptedException {\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "java.lang.I");
	assertSortedResults(
		"IllegalMonitorStateException[JSDOC_TYPE_REF]{{@link IllegalMonitorStateException}, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+R_DICNRIT+"}\n" + 
		"InterruptedException[JSDOC_TYPE_REF]{{@link InterruptedException}, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DICNRIT+"}\n" + 
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+R_DICNR+"}\n" +
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DICNR+"}"
	);
}

public void test028() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: java.lang.I\n" + 
		"	 */\n" + 
		"	public void foo() throws InterruptedException {\n" + 
		"	}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "java.lang.I");
	assertSortedResults(
		"InterruptedException[JSDOC_TYPE_REF]{{@link InterruptedException}, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DICNREETIT+"}\n" + 
		"IllegalMonitorStateException[JSDOC_TYPE_REF]{{@link IllegalMonitorStateException}, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+R_DICNRIT+"}\n" + 
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DICNREET+"}\n" + 
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+R_DICNR+"}"
	);
}

/**
 * @tests  Tests for fields completion
 */
public void test030() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #fo\n" + 
		"	 */\n" + 
		"	int foo;\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#fo");
	assertSortedResults(
		"foo[JSDOC_FIELD_REF]{{@link #foo}, Ljavadoc.text.BasicTestTextIns;, I, foo, null, "+this.positions+R_DICNRNSIT+"}"
	);
}

public void test031() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: BasicTestTextIns#fo\n" + 
		"	 */\n" + 
		"	static int foo;\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "BasicTestTextIns#fo");
	assertSortedResults(
		"foo[JSDOC_FIELD_REF]{{@link BasicTestTextIns#foo}, Ljavadoc.text.BasicTestTextIns;, I, foo, null, "+this.positions+R_DICNRIT+"}\n" + 
		"foo[JSDOC_VALUE_REF]{{@value BasicTestTextIns#foo}, Ljavadoc.text.BasicTestTextIns;, I, foo, null, "+this.positions+R_DICNRIT+"}"
	);
}

public void test032() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: javadoc.text.BasicTestTextIns#fo\n" + 
		"	 */\n" + 
		"	int foo;\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "javadoc.text.BasicTestTextIns#fo");
	assertSortedResults(
		"foo[JSDOC_FIELD_REF]{{@link javadoc.text.BasicTestTextIns#foo}, Ljavadoc.text.BasicTestTextIns;, I, foo, null, "+this.positions+R_DICNRNSIT+"}"
	);
}

public void test033() throws JavaScriptModelException {
	String[] sources = {
		"/Completion/src/javadoc/text/BasicTestTextIns.js",
			"package javadoc.text;\n" + 
			"public class BasicTestTextIns {\n" + 
			"	/**\n" + 
			"	 * Completion after: OtherFields#fo\n" + 
			"	 */\n" + 
			"	int foo;\n" +
			"}",
		"/Completion/src/javadoc/text/OtherFields.js",
			"package javadoc.text;\n" + 
			"public class OtherFields {\n" + 
			"	static int foo;\n" + 
			"}"
	};
	completeInJavadoc(sources, true, "OtherFields#fo");
	assertSortedResults(
		"foo[JSDOC_FIELD_REF]{{@link OtherFields#foo}, Ljavadoc.text.OtherFields;, I, foo, null, "+this.positions+R_DICNRIT+"}\n" + 
		"foo[JSDOC_VALUE_REF]{{@value OtherFields#foo}, Ljavadoc.text.OtherFields;, I, foo, null, "+this.positions+R_DICNRIT+"}"
	);
}

public void test034() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@value #fo\n" + 
		"	 *		Note: this test must be run with 1.4 compliance\n" + 
		"	 */\n" + 
		"	int foo;\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "fo");
	assertSortedResults(
		"foo[FIELD_REF]{foo, Ljavadoc.text.BasicTestTextIns;, I, foo, null, "+this.positions+R_DICNRNS+"}"
	);
}

public void test035() throws JavaScriptModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@value #fo\n" + 
		"	 *		Note: this test must be run with 1.5 compliance\n" + 
		"	 */\n" + 
		"	int foo;\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "fo");
	assertSortedResults(
		"foo[FIELD_REF]{foo, Ljavadoc.text.BasicTestTextIns;, I, foo, null, "+this.positions+R_DICNRNS+"}"
	);
}

public void test036() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link BasicTestTextIns#fo\n" + 
		"	 */\n" + 
		"	static int foo;\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "fo");
	assertSortedResults(
		"foo[FIELD_REF]{foo, Ljavadoc.text.BasicTestTextIns;, I, foo, null, "+this.positions+R_DICNR+"}"
	);
}

public void test037() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link javadoc.text.BasicTestTextIns#fo }\n" + 
		"	 */\n" + 
		"	int foo;\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "fo");
	assertSortedResults(
		"foo[FIELD_REF]{foo, Ljavadoc.text.BasicTestTextIns;, I, foo, null, "+this.positions+R_DICNRNS+"}"
	);
}

public void test038() throws JavaScriptModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String[] sources = {
		"/Completion/src/javadoc/text/BasicTestTextIns.js",
			"package javadoc.text;\n" + 
			"public class BasicTestTextIns {\n" + 
			"	/**\n" + 
			"	 * Completion after: {@value OtherFields#fo\n" + 
		"	 *		Note: this test must be run with 1.5 compliance\n" + 
			"	 */\n" + 
			"	int foo;\n" +
			"}",
		"/Completion/src/javadoc/text/OtherFields.js",
			"package javadoc.text;\n" + 
			"public class OtherFields {\n" + 
			"	static int foo;\n" + 
			"}"
	};
	completeInJavadoc(sources, true, "fo");
	assertSortedResults(
		"foo[FIELD_REF]{foo, Ljavadoc.text.OtherFields;, I, foo, null, "+this.positions+R_DICNR+"}"
	);
}

/**
 * @tests  Tests for methods completion
 */
public void test040() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: meth\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "meth");
	assertSortedResults("");
}

public void test041() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #meth\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#meth");
	assertSortedResults(
		"method[JSDOC_METHOD_REF]{{@link #method()}, Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+R_DICNRNSIT+"}"
	);
}

public void test042() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #meth with text after\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#meth");
	assertSortedResults(
		"method[JSDOC_METHOD_REF]{{@link #method()}, Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+R_DICNRNSIT+"}"
	);
}

public void test043() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #method\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#meth");
	assertSortedResults(
		"method[JSDOC_METHOD_REF]{{@link #method()}, Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+R_DICNRNSIT+"}"
	);
}

public void test044() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: BasicTestTextIns#param\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "BasicTestTextIns#param");
	assertSortedResults(
		"paramMethod[JSDOC_METHOD_REF]{{@link BasicTestTextIns#paramMethod(String, boolean, Object)}, Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+R_DICNRNSIT+"}"
	);
}

public void test045() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: javadoc.text.BasicTestTextIns#meth\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "javadoc.text.BasicTestTextIns#meth");
	assertSortedResults(
		"method[JSDOC_METHOD_REF]{{@link javadoc.text.BasicTestTextIns#method()}, Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+R_DICNRNSIT+"}"
	);
}

public void test046() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link #param\n" + 
		"	 */\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "param");
	assertSortedResults(
		"paramMethod[FUNCTION_REF]{paramMethod(String, boolean, Object), Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+R_DICNRNS+"}"
	);
}

public void test047() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link BasicTestTextIns#meth\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "meth");
	assertSortedResults(
		"method[FUNCTION_REF]{method(), Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+R_DICNRNS+"}"
	);
}

public void test048() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link javadoc.text.BasicTestTextIns#param }\n" + 
		"	 */\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "param");
	assertSortedResults(
		"paramMethod[FUNCTION_REF]{paramMethod(String, boolean, Object), Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+R_DICNRNS+"}"
	);
}

public void test049() throws JavaScriptModelException {
	String[] sources = {
		"/Completion/src/javadoc/text/BasicTestTextIns.js",
			"package javadoc.text;\n" + 
			"public class BasicTestTextIns {\n" + 
			"	/**\n" + 
			"	 * Completion after: OtherTypes#meth\n" + 
			"	 */\n" + 
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/text/OtherTypes.js",
			"package javadoc.text;\n" + 
			"public class OtherTypes {\n" + 
			"	void method() {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "OtherTypes#meth");
	assertSortedResults(
		"method[JSDOC_METHOD_REF]{{@link OtherTypes#method()}, Ljavadoc.text.OtherTypes;, ()V, method, null, "+this.positions+R_DICNRNSIT+"}"
	);
}

public void test050() throws JavaScriptModelException {
	String[] sources = {
		"/Completion/src/javadoc/text/BasicTestTextIns.js",
			"package javadoc.text;\n" + 
			"public class BasicTestTextIns {\n" + 
			"	/**\n" + 
			"	 * Completion after: {@link OtherTypes#method }\n" + 
			"	 */\n" + 
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/text/OtherTypes.js",
			"package javadoc.text;\n" + 
			"public class OtherTypes {\n" + 
			"	void method() {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "meth");
	assertSortedResults(
		"method[FUNCTION_REF]{method(), Ljavadoc.text.OtherTypes;, ()V, method, null, "+this.positions+R_DICNRNS+"}"
	);
}

public void test051() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#");
	assertSortedResults(
		"clone[JSDOC_METHOD_REF]{{@link #clone()}, Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"equals[JSDOC_METHOD_REF]{{@link #equals(Object)}, Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"finalize[JSDOC_METHOD_REF]{{@link #finalize()}, Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"getClass[JSDOC_METHOD_REF]{{@link #getClass()}, Ljava.lang.Object;, ()Ljava.lang.Class;, getClass, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"hashCode[JSDOC_METHOD_REF]{{@link #hashCode()}, Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"method[JSDOC_METHOD_REF]{{@link #method()}, Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"notify[JSDOC_METHOD_REF]{{@link #notify()}, Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"notifyAll[JSDOC_METHOD_REF]{{@link #notifyAll()}, Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"paramMethod[JSDOC_METHOD_REF]{{@link #paramMethod(String, boolean, Object)}, Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+R_DICNRNSIT+"}\n" + 
		"toString[JSDOC_METHOD_REF]{{@link #toString()}, Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"wait[JSDOC_METHOD_REF]{{@link #wait(long, int)}, Ljava.lang.Object;, (JI)V, wait, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"wait[JSDOC_METHOD_REF]{{@link #wait(long)}, Ljava.lang.Object;, (J)V, wait, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"wait[JSDOC_METHOD_REF]{{@link #wait()}, Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"BasicTestTextIns[JSDOC_METHOD_REF]{{@link #BasicTestTextIns()}, Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}"
	);
}

public void test052() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #method\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#");
	assertSortedResults(
		"clone[JSDOC_METHOD_REF]{{@link #clone()}, Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"equals[JSDOC_METHOD_REF]{{@link #equals(Object)}, Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"finalize[JSDOC_METHOD_REF]{{@link #finalize()}, Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"getClass[JSDOC_METHOD_REF]{{@link #getClass()}, Ljava.lang.Object;, ()Ljava.lang.Class;, getClass, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"hashCode[JSDOC_METHOD_REF]{{@link #hashCode()}, Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"method[JSDOC_METHOD_REF]{{@link #method()}, Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"notify[JSDOC_METHOD_REF]{{@link #notify()}, Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"notifyAll[JSDOC_METHOD_REF]{{@link #notifyAll()}, Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"paramMethod[JSDOC_METHOD_REF]{{@link #paramMethod(String, boolean, Object)}, Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+R_DICNRNSIT+"}\n" + 
		"toString[JSDOC_METHOD_REF]{{@link #toString()}, Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"wait[JSDOC_METHOD_REF]{{@link #wait(long, int)}, Ljava.lang.Object;, (JI)V, wait, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"wait[JSDOC_METHOD_REF]{{@link #wait(long)}, Ljava.lang.Object;, (J)V, wait, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"wait[JSDOC_METHOD_REF]{{@link #wait()}, Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"BasicTestTextIns[JSDOC_METHOD_REF]{{@link #BasicTestTextIns()}, Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+(JAVADOC_RELEVANCE+R_INLINE_TAG)+"}"
	);
}

public void test053() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link BasicTestTextIns#\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#", 0); //empty token
	assertSortedResults(
		"clone[FUNCTION_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DICNRNS+"}\n" + 
		"equals[FUNCTION_REF]{equals(Object), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+this.positions+R_DICNRNS+"}\n" + 
		"finalize[FUNCTION_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DICNRNS+"}\n" + 
		"getClass[FUNCTION_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class;, getClass, null, "+this.positions+R_DICNRNS+"}\n" + 
		"hashCode[FUNCTION_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DICNRNS+"}\n" + 
		"method[FUNCTION_REF]{method(), Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+R_DICNRNS+"}\n" + 
		"notify[FUNCTION_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DICNRNS+"}\n" + 
		"notifyAll[FUNCTION_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DICNRNS+"}\n" + 
		"paramMethod[FUNCTION_REF]{paramMethod(String, boolean, Object), Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+R_DICNRNS+"}\n" + 
		"toString[FUNCTION_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(long, int), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(long), Ljava.lang.Object;, (J)V, wait, (millis), "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DICNRNS+"}\n" + 
		"BasicTestTextIns[FUNCTION_REF<CONSTRUCTOR>]{BasicTestTextIns(), Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test054() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link javadoc.text.BasicTestTextIns# }\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#", 0); //empty token
	assertSortedResults(
		"clone[FUNCTION_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DICNRNS+"}\n" + 
		"equals[FUNCTION_REF]{equals(Object), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+this.positions+R_DICNRNS+"}\n" + 
		"finalize[FUNCTION_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DICNRNS+"}\n" + 
		"getClass[FUNCTION_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class;, getClass, null, "+this.positions+R_DICNRNS+"}\n" + 
		"hashCode[FUNCTION_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DICNRNS+"}\n" + 
		"method[FUNCTION_REF]{method(), Ljavadoc.text.BasicTestTextIns;, ()V, method, null, "+this.positions+R_DICNRNS+"}\n" + 
		"notify[FUNCTION_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DICNRNS+"}\n" + 
		"notifyAll[FUNCTION_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DICNRNS+"}\n" + 
		"paramMethod[FUNCTION_REF]{paramMethod(String, boolean, Object), Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+R_DICNRNS+"}\n" + 
		"toString[FUNCTION_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(long, int), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(long), Ljava.lang.Object;, (J)V, wait, (millis), "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DICNRNS+"}\n" + 
		"BasicTestTextIns[FUNCTION_REF<CONSTRUCTOR>]{BasicTestTextIns(), Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test055() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: #paramMethod(\n" + 
		"	 */\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#paramMethod(");
	assertSortedResults(
		"paramMethod[JSDOC_METHOD_REF]{{@link #paramMethod(String, boolean, Object)}, Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+R_DICENUNRIT+"}"
	);
}

public void test056() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link #paramMethod(Str\n" + 
		"	 */\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "Str");
	assertSortedResults(
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+R_DICUNR+"}"
	);
}

public void test057() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link #paramMethod(String s\n" + 
		"	 */\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "paramMethod(String s");
	assertSortedResults(
		"paramMethod[FUNCTION_REF]{paramMethod(String, boolean, Object), Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+R_DICENUNR+"}"
	);
}

public void test058() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: #paramMethod(String str, \n" + 
		"	 */\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#paramMethod(String str,");
	assertSortedResults(
		"paramMethod[JSDOC_METHOD_REF]{{@link #paramMethod(String, boolean, Object)}, Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+R_DICENUNRIT+"}"
	);
}

public void test059() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link #paramMethod(String,\n" + 
		"	 */\n" + 
		"	void paramMethod(String str, boolean flag, Object obj) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "paramMethod(String,");
	assertSortedResults(
		"paramMethod[FUNCTION_REF]{paramMethod(String, boolean, Object), Ljavadoc.text.BasicTestTextIns;, (Ljava.lang.String;ZLjava.lang.Object;)V, paramMethod, (str, flag, obj), "+this.positions+R_DICENUNR+"}"
	);
}

/**
 * @tests  Tests for constructors completion
 */
public void test070() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #BasicTest\n" + 
		"	 */\n" + 
		"	BasicTestTextIns() {}\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#BasicTest");
	assertSortedResults(
		"BasicTestTextIns[JSDOC_METHOD_REF]{{@link #BasicTestTextIns(int, float, Class)}, Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE_IT+"}\n" + 
		"BasicTestTextIns[JSDOC_METHOD_REF]{{@link #BasicTestTextIns()}, Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+JAVADOC_RELEVANCE_IT+"}"
	);
}

public void test071() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link BasicTestTextIns#BasicTest\n" + 
		"	 */\n" + 
		"	BasicTestTextIns() {}\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "BasicTest", 3); // 3rd occurence
	assertSortedResults(
		"BasicTestTextIns[FUNCTION_REF<CONSTRUCTOR>]{BasicTestTextIns(int, float, Class), Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"BasicTestTextIns[FUNCTION_REF<CONSTRUCTOR>]{BasicTestTextIns(), Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test072() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@linkplain javadoc.text.BasicTestTextIns#BasicTest }\n" + 
		"	 */\n" + 
		"	BasicTestTextIns() {}\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "BasicTest", 3); // 3rd occurence
	assertSortedResults(
		"BasicTestTextIns[FUNCTION_REF<CONSTRUCTOR>]{BasicTestTextIns(int, float, Class), Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"BasicTestTextIns[FUNCTION_REF<CONSTRUCTOR>]{BasicTestTextIns(), Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test073() throws JavaScriptModelException {
	String[] sources = {
		"/Completion/src/javadoc/text/BasicTestTextIns.js",
			"package javadoc.text;\n" + 
			"public class BasicTestTextIns {\n" + 
			"	/**\n" + 
			"	 * Completion after: OtherTypes#Other\n" + 
			"	 */\n" + 
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/text/OtherTypes.js",
			"package javadoc.text;\n" + 
			"public class OtherTypes {\n" + 
			"	OtherTypes() {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "OtherTypes#O");
	assertSortedResults(
		"OtherTypes[JSDOC_METHOD_REF]{{@link OtherTypes#OtherTypes()}, Ljavadoc.text.OtherTypes;, ()V, OtherTypes, null, "+this.positions+JAVADOC_RELEVANCE_IT+"}"
	);
}

public void test074() throws JavaScriptModelException {
	String[] sources = {
		"/Completion/src/javadoc/text/BasicTestTextIns.js",
			"package javadoc.text;\n" + 
			"public class BasicTestTextIns {\n" + 
			"	/**\n" + 
			"	 * Completion after: {@link OtherTypes#O implicit default constructor\n" + 
			"	 */\n" + 
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/text/OtherTypes.js",
			"package javadoc.text;\n" + 
			"public class OtherTypes {\n" + 
			"}"
	};
	completeInJavadoc(sources, true, "O", 2); // 2nd occurence
	assertSortedResults(
		"OtherTypes[FUNCTION_REF<CONSTRUCTOR>]{OtherTypes(), Ljavadoc.text.OtherTypes;, ()V, OtherTypes, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test075() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #\n" + 
		"	 */\n" + 
		"	BasicTestTextIns() {}\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#");
	assertSortedResults(
		"clone[JSDOC_METHOD_REF]{{@link #clone()}, Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"equals[JSDOC_METHOD_REF]{{@link #equals(Object)}, Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"finalize[JSDOC_METHOD_REF]{{@link #finalize()}, Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"getClass[JSDOC_METHOD_REF]{{@link #getClass()}, Ljava.lang.Object;, ()Ljava.lang.Class;, getClass, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"hashCode[JSDOC_METHOD_REF]{{@link #hashCode()}, Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"notify[JSDOC_METHOD_REF]{{@link #notify()}, Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"notifyAll[JSDOC_METHOD_REF]{{@link #notifyAll()}, Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"toString[JSDOC_METHOD_REF]{{@link #toString()}, Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"wait[JSDOC_METHOD_REF]{{@link #wait(long, int)}, Ljava.lang.Object;, (JI)V, wait, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"wait[JSDOC_METHOD_REF]{{@link #wait(long)}, Ljava.lang.Object;, (J)V, wait, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"wait[JSDOC_METHOD_REF]{{@link #wait()}, Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"BasicTestTextIns[JSDOC_METHOD_REF]{{@link #BasicTestTextIns(int, float, Class)}, Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE_IT+"}\n" + 
		"BasicTestTextIns[JSDOC_METHOD_REF]{{@link #BasicTestTextIns()}, Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+JAVADOC_RELEVANCE_IT+"}"
	);
}

public void test076() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: #blabla\n" + 
		"	 */\n" + 
		"	BasicTestTextIns() {}\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#");
	assertSortedResults(
		"clone[JSDOC_METHOD_REF]{{@link #clone()}, Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"equals[JSDOC_METHOD_REF]{{@link #equals(Object)}, Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"finalize[JSDOC_METHOD_REF]{{@link #finalize()}, Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"getClass[JSDOC_METHOD_REF]{{@link #getClass()}, Ljava.lang.Object;, ()Ljava.lang.Class;, getClass, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"hashCode[JSDOC_METHOD_REF]{{@link #hashCode()}, Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"notify[JSDOC_METHOD_REF]{{@link #notify()}, Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"notifyAll[JSDOC_METHOD_REF]{{@link #notifyAll()}, Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"toString[JSDOC_METHOD_REF]{{@link #toString()}, Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"wait[JSDOC_METHOD_REF]{{@link #wait(long, int)}, Ljava.lang.Object;, (JI)V, wait, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"wait[JSDOC_METHOD_REF]{{@link #wait(long)}, Ljava.lang.Object;, (J)V, wait, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"wait[JSDOC_METHOD_REF]{{@link #wait()}, Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DICNRNSIT+"}\n" + 
		"BasicTestTextIns[JSDOC_METHOD_REF]{{@link #BasicTestTextIns(int, float, Class)}, Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE_IT+"}\n" + 
		"BasicTestTextIns[JSDOC_METHOD_REF]{{@link #BasicTestTextIns()}, Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+JAVADOC_RELEVANCE_IT+"}"
	);
}

public void test077() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link # }\n" + 
		"	 */\n" + 
		"	BasicTestTextIns() {}\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#", 0); // empty token
	assertSortedResults(
		"clone[FUNCTION_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DICNRNS+"}\n" + 
		"equals[FUNCTION_REF]{equals(Object), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+this.positions+R_DICNRNS+"}\n" + 
		"finalize[FUNCTION_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DICNRNS+"}\n" + 
		"getClass[FUNCTION_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class;, getClass, null, "+this.positions+R_DICNRNS+"}\n" + 
		"hashCode[FUNCTION_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DICNRNS+"}\n" + 
		"notify[FUNCTION_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DICNRNS+"}\n" + 
		"notifyAll[FUNCTION_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DICNRNS+"}\n" + 
		"toString[FUNCTION_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(long, int), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(long), Ljava.lang.Object;, (J)V, wait, (millis), "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DICNRNS+"}\n" + 
		"BasicTestTextIns[FUNCTION_REF<CONSTRUCTOR>]{BasicTestTextIns(int, float, Class), Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"BasicTestTextIns[FUNCTION_REF<CONSTRUCTOR>]{BasicTestTextIns(), Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test078() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: {@link #BasicTestTextIns(\n" + 
		"	 */\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "BasicTestTextIns(");
	assertSortedResults(
		"BasicTestTextIns[FUNCTION_REF<CONSTRUCTOR>]{BasicTestTextIns(int, float, Class), Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test079() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: BasicTestTextIns#BasicTestTextIns(int,\n" + 
		"	 */\n" + 
		"	BasicTestTextIns() {}\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "BasicTestTextIns#BasicTestTextIns(int,");
	assertSortedResults(
		"BasicTestTextIns[JSDOC_METHOD_REF]{{@link BasicTestTextIns#BasicTestTextIns(int, float, Class)}, Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE_IT+"}"
	);
}

public void test080() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after: BasicTestTextIns#BasicTestTextIns(int,\n" + 
		"	 * 	Note: completion takes place just after opening brace\n" + 
		"	 */\n" + 
		"	BasicTestTextIns() {}\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "BasicTestTextIns#BasicTestTextIns(");
	assertSortedResults(
			"BasicTestTextIns[JSDOC_METHOD_REF]{{@link BasicTestTextIns#BasicTestTextIns(int, float, Class)}, Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE_IT+"}\n" + 
			"BasicTestTextIns[JSDOC_METHOD_REF]{{@link BasicTestTextIns#BasicTestTextIns()}, Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+JAVADOC_RELEVANCE_IT+"}"
	);
}

public void test081() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	javadoc.text.BasicTestTextIns#BasicTestTextIns(\n" + 
		"	 */\n" + 
		"	void method() {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "javadoc.text.BasicTestTextIns#BasicTestTextIns(");
	assertSortedResults(
		"BasicTestTextIns[JSDOC_METHOD_REF]{{@link javadoc.text.BasicTestTextIns#BasicTestTextIns()}, Ljavadoc.text.BasicTestTextIns;, ()V, BasicTestTextIns, null, "+this.positions+JAVADOC_RELEVANCE_IT+"}"
	);
}

public void test082() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	#BasicTestTextIns(int, float, java.lang.\n" + 
		"	 */\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#BasicTestTextIns(int, float, java.lang.");
	assertSortedResults(
		"BasicTestTextIns[JSDOC_METHOD_REF]{{@link #BasicTestTextIns(int, float, Class)}, Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE_IT+"}"
	);
}

public void test083() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	#BasicTestTextIns(int, float, java.lang.Cla\n" + 
		"	 */\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#BasicTestTextIns(int, float, java.lang.Cla");
	assertSortedResults(
		"BasicTestTextIns[JSDOC_METHOD_REF]{{@link #BasicTestTextIns(int, float, Class)}, Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE_IT+"}"
	);
}

public void test084() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	#BasicTestTextIns(int, float, Class)\n" + 
		"	 * 	Note: completion takes place before closing parenthesis\n" + 
		"	 */\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#BasicTestTextIns(int, float, Class");
	assertSortedResults(
		"BasicTestTextIns[JSDOC_METHOD_REF]{{@link #BasicTestTextIns(int, float, Class)}, Ljavadoc.text.BasicTestTextIns;, (IFLjava.lang.Class;)V, BasicTestTextIns, (xxx, real, clazz), "+this.positions+JAVADOC_RELEVANCE_IT+"}"
	);
}

public void test085() throws JavaScriptModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"	void method() {}\n" + 
		"	/**\n" + 
		"	 * Completion after:\n" + 
		"	 * 	#BasicTestTextIns(int, float, Class)\n" + 
		"	 * 	Note: completion takes place after closing parenthesis\n" + 
		"	 */\n" + 
		"	BasicTestTextIns(int xxx, float real, Class clazz) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.js", source, true, "#BasicTestTextIns(int, float, Class)");
	assertSortedResults("");
	}
}
