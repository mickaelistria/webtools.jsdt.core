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

import java.io.File;

import junit.framework.Test;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.tests.util.Util;
import org.eclipse.wst.jsdt.internal.compiler.impl.CompilerOptions;

/**
 * Test class for completion in Javadoc comment of a package declaration.
 */
public class JavadocPackageCompletionModelTest extends AbstractJavadocCompletionModelTest {

public JavadocPackageCompletionModelTest(String name) {
	super(name);
}

static {
//	TESTS_RANGE = new int[] { 11, 12 };
//	TESTS_NUMBERS = new int[] { 16 };
}
public static Test suite() {
	return buildModelTestSuite(JavadocPackageCompletionModelTest.class);
}

/* (non-Javadoc)
 * @see org.eclipse.wst.jsdt.core.tests.model.AbstractJavadocCompletionModelTest#setUp()
 */
protected void setUp() throws Exception {
	super.setUp();
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
}

/*
 * Write files for self-hosting debug.
 */
protected void writeFiles(String[] sources) {

	// Get write directory path
	if (WRITE_DIR_FILE == null) return;
	
	// Get test name
	String testName = getName();
	int idx = testName.indexOf(" - ");
	if (idx > 0) {
		testName = testName.substring(idx+3);
	}
//	testName = "Test"+testName.substring(4);
	
	// Write sources to dir
	int length = sources.length / 2;
	for (int i=0; i<length; i++) {
		
		// Get pathes
		IPath filePath = new Path(sources[2*i]).removeFirstSegments(2); // remove project and source folder
		IPath dirPath = filePath.removeLastSegments(1);
		String fileDir = dirPath.toString();
		String typeName = filePath.removeFileExtension().lastSegment();
		if (i==0 && !typeName.equals("package-info")) {
			System.err.println("Invalid type name: '"+typeName+"' for test "+testName);
			continue;
		}
		
		// Create package dirs or delete dirs if already exist
		File packageDir = new File(WRITE_DIR_FILE, fileDir);
		if (!PACKAGE_FILES.contains(packageDir)) {
			if (packageDir.exists()) {
				PACKAGE_FILES.add(packageDir);
				Util.flushDirectoryContent(packageDir);
			} else if (packageDir.mkdirs()) {
				PACKAGE_FILES.add(packageDir);
			} else {
				System.err.println(packageDir+" does not exist and CANNOT be created!!!");
				continue;
			}
		}
		
		// Create test dir
		File testDir = new File(packageDir, testName);
		if (!testDir.exists() && !testDir.mkdir()) {
			System.err.println(testDir+" does not exist and CANNOT be created!!!");
			continue;
		}
		
		// Store names info
		String fullPathName = testDir.getAbsolutePath()+"\\"+typeName+".js";
		System.out.println("Write file "+fullPathName);
		String contents = null;
		if (i==0) { // package-info
			contents = sources[2*i+1].substring(0, sources[2*i+1].lastIndexOf(';'));
			contents += "."+testName+";\n";
		} else {
			int index = sources[2*i+1].indexOf(';');
			contents = sources[2*i+1].substring(0, index);
			contents += "."+testName;
			contents += sources[2*i+1].substring(index);
		}
		Util.writeToFile(contents, fullPathName);
	}
}

/**
 * @category Tests for tag names completion
 */
public void test001() throws JavaScriptModelException {
	String source =
		"/**\n" +
		" * Completion on empty tag name:\n" +
		" * 	@\n" +
		" */\n" +
		"package javadoc;\n";
	completeInJavadoc("/Completion/src/javadoc/package-info.js", source, true, "@");
	assertResults(
		"author[JSDOC_BLOCK_TAG]{@author, null, null, author, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"see[JSDOC_BLOCK_TAG]{@see, null, null, see, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"version[JSDOC_BLOCK_TAG]{@version, null, null, version, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"category[JSDOC_BLOCK_TAG]{@category, null, null, category, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"since[JSDOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"serial[JSDOC_BLOCK_TAG]{@serial, null, null, serial, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"link[JSDOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"docRoot[JSDOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"linkplain[JSDOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"value[JSDOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test002() throws JavaScriptModelException {
	String source =
		"/**\n" +
		" * Completion on impossible tag name:\n" +
		" * 	@deprec\n" +
		" */\n" +
		"package javadoc;\n";
	completeInJavadoc("/Completion/src/javadoc/package-info.js", source, true, "@deprec");
	assertResults("");
}

public void test003() throws JavaScriptModelException {
	String source = 
		"/**\n" +
		" * Completion on one letter:\n" +
		" * 	@a\n" +
		" */\n" +
		"package javadoc;\n";
	completeInJavadoc("/Completion/src/javadoc/package-info.js", source, true, "@a");
	assertResults(
		"author[JSDOC_BLOCK_TAG]{@author, null, null, author, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test004() throws JavaScriptModelException {
	String source =
		"/**\n" +
		" * Completion with several letters:\n" +
		" * 	@ser\n" +
		" */\n" +
		"package javadoc;\n";
	completeInJavadoc("/Completion/src/javadoc/package-info.js", source, true, "@ser");
	assertResults(
		"serial[JSDOC_BLOCK_TAG]{@serial, null, null, serial, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test005() throws JavaScriptModelException {
	String source =
		"/**\n" +
		" * Completion on full tag name:\n" +
		" * 	@since\n" +
		" */\n" +
		"package javadoc;\n";
	completeInJavadoc("/Completion/src/javadoc/package-info.js", source, true, "@since");
	assertResults(
		"since[JSDOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test006() throws JavaScriptModelException {
	String source =
		"/**\n" +
		" * Completion on @ inside text\n" +
		" */\n" +
		"package javadoc;\n";
	completeInJavadoc("/Completion/src/javadoc/package-info.js", source, true, "@");
	assertResults(
		"link[JSDOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"docRoot[JSDOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"linkplain[JSDOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"value[JSDOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test007() throws JavaScriptModelException {
	String source =
		"/**\n" +
		" * Completion on @d inside text\n" +
		" */\n" +
		"package javadoc;\n";
	completeInJavadoc("/Completion/src/javadoc/package-info.js", source, true, "@d");
	assertResults(
		"docRoot[JSDOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

/**
 * @category Tests for types completion
 */
public void test010() throws JavaScriptModelException {
	String source =
		"/**\n" + 
		" * Completion after:\n" + 
		" * 	@see Obj\n" + 
		" */\n" + 
		"package javadoc.tags;\n";
	completeInJavadoc("/Completion/src/javadoc/tags/package-info.js", source, true, "Obj");
	assertResults(
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+R_DICUNR+"}"
	);
}

public void test011() throws JavaScriptModelException {
	String source =
		"/**\n" + 
		" * Completion after:\n" + 
		" * 	@see BasicTestRef\n" + 
		" */\n" + 
		"package javadoc.tags;\n";
	completeInJavadoc("/Completion/src/javadoc/tags/package-info.js", source, true, "BasicTestRef");
	assertResults(
		"BasicTestReferences[TYPE_REF]{org.eclipse.wst.jsdt.core.tests.BasicTestReferences, org.eclipse.wst.jsdt.core.tests, Lorg.eclipse.wst.jsdt.core.tests.BasicTestReferences;, null, null, "+this.positions+R_DICNR+"}"
	);
}

public void test012() throws JavaScriptModelException {
	String source =
		"/**\n" + 
		" * Completion after:\n" + 
		" * 	@see org.eclipse.wst.jsdt.core.tests.BasicTestRef\n" + 
		" * 		Note: JDT-UI failed on this one\n" + 
		" */\n" + 
		"package javadoc.tags;\n";
	completeInJavadoc("/Completion/src/javadoc/tags/package-info.js", source, true, "org.eclipse.wst.jsdt.core.tests.BasicTestRef");
	assertResults(
		"BasicTestReferences[TYPE_REF]{org.eclipse.wst.jsdt.core.tests.BasicTestReferences, org.eclipse.wst.jsdt.core.tests, Lorg.eclipse.wst.jsdt.core.tests.BasicTestReferences;, null, null, "+this.positions+R_DICQNR+"}"
	);
}

public void test013() throws JavaScriptModelException {
	String source =
		"/**\n" + 
		" * Completion after:\n" + 
		" * 	@see java.la\n" + 
		" * 		Note: JDT-UI fails on this one\n" + 
		" */\n" + 
		"package javadoc.tags;\n";
	completeInJavadoc("/Completion/src/javadoc/tags/package-info.js", source, true, "java.la");
	assertResults(
		"java.lang.annotation[PACKAGE_REF]{java.lang.annotation, java.lang.annotation, null, null, null, "+this.positions+R_DICQNR+"}\n" + 
		"java.lang[PACKAGE_REF]{java.lang, java.lang, null, null, null, "+this.positions+R_DICQNR+"}"
	);
}

public void test014() throws JavaScriptModelException {
	String source =
		"/**\n" + 
		" * Completion after:\n" + 
		" * 	@see pack.Bin\n" + 
		" */\n" + 
		"package javadoc.tags;\n";
	completeInJavadoc("/Completion/src/javadoc/tags/package-info.js", source, true, "pack.Bin");
	assertSortedResults(
		"Bin1[TYPE_REF]{pack.Bin1, pack, Lpack.Bin1;, null, null, "+this.positions+R_DICQNR+"}\n" + 
		"Bin2[TYPE_REF]{pack.Bin2, pack, Lpack.Bin2;, null, null, "+this.positions+R_DICQNR+"}\n" + 
		"Bin3[TYPE_REF]{pack.Bin3, pack, Lpack.Bin3;, null, null, "+this.positions+R_DICQNR+"}"
	);
}

public void test015() throws JavaScriptModelException {
	String source =
		"/**\n" + 
		" * Completion after:\n" + 
		" * 	@see I\n" + 
		" * 		Note: completion list shoud not include base types.\n" + 
		" */\n" + 
		"package javadoc.tags;\n";
	completeInJavadoc("/Completion/src/javadoc/tags/package-info.js", source, true, "I");
	assertSortedResults(
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+R_DICUNR+"}\n" + 
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DICUNR+"}"
	);
}

/**
 * @category Tests for fields completion
 */
public void test020() throws JavaScriptModelException {
	String source =
		"/**\n" + 
		" * Completion after:\n" + 
		" * 	@see BasicTestReferences#FIE\n" + 
		" */\n" + 
		"package javadoc.tags;\n";
	completeInJavadoc("/Completion/src/javadoc/tags/package-info.js", source, true, "FIE");
	assertResults("");
}

public void test021() throws JavaScriptModelException {
	String source =
		"/**\n" + 
		" * Completion after:\n" + 
		" * 	@see org.eclipse.wst.jsdt.core.tests.BasicTestReferences#FIE\n" + 
		" */\n" + 
		"package javadoc.tags;\n";
	completeInJavadoc("/Completion/src/javadoc/tags/package-info.js", source, true, "FIE");
	assertResults(
		"FIELD[FIELD_REF]{FIELD, Lorg.eclipse.wst.jsdt.core.tests.BasicTestReferences;, I, FIELD, null, "+this.positions+R_DICNR+"}"
	);
}

public void test022() throws JavaScriptModelException {
	String[] sources = {
		"/Completion/src/javadoc/tags/package-info.js",
			"/**\n" + 
			" * Completion after:\n" + 
			" * 	@see OtherTypes#bar\n" + 
			" */\n" + 
			"package javadoc.tags;\n",
		"/Completion/src/javadoc/tags/OtherTypes.js",
			"package javadoc.tags;\n" + 
			"public class OtherTypes {\n" + 
			"	int bar;\n" + 
			"}"
	};
	completeInJavadoc(sources, true, "bar");
	assertResults(
		"bar[FIELD_REF]{bar, Ljavadoc.tags.OtherTypes;, I, bar, null, "+this.positions+R_DICENNRNS+"}"
	);
}

public void test023() throws JavaScriptModelException {
	String source =
		"/**\n" + 
		" * Completion after:\n" + 
		" * 	@see BasicTestReferences#\n" + 
		" */\n" + 
		"package javadoc.tags;\n";
	completeInJavadoc("/Completion/src/javadoc/tags/package-info.js", source, true, "#", 0); // empty token
	assertResults("");
}

public void test024() throws JavaScriptModelException {
	String source =
		"/**\n" + 
		" * Completion after:\n" + 
		" * 	@see org.eclipse.wst.jsdt.core.tests.BasicTestReferences#\n" + 
		" */\n" + 
		"package javadoc.tags;\n";
	completeInJavadoc("/Completion/src/javadoc/tags/package-info.js", source, true, "#", 0); // empty token
	assertResults(
		"FIELD[FIELD_REF]{FIELD, Lorg.eclipse.wst.jsdt.core.tests.BasicTestReferences;, I, FIELD, null, "+this.positions+R_DICNR+"}\n" + 
		"wait[FUNCTION_REF]{wait(long, int), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(long), Ljava.lang.Object;, (J)V, wait, (millis), "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DICNRNS+"}\n" + 
		"toString[FUNCTION_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DICNRNS+"}\n" + 
		"notifyAll[FUNCTION_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DICNRNS+"}\n" + 
		"notify[FUNCTION_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DICNRNS+"}\n" + 
		"hashCode[FUNCTION_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DICNRNS+"}\n" + 
		"getClass[FUNCTION_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class;, getClass, null, "+this.positions+R_DICNRNS+"}\n" + 
		"finalize[FUNCTION_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DICNRNS+"}\n" + 
		"equals[FUNCTION_REF]{equals(Object), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+this.positions+R_DICNRNS+"}\n" + 
		"clone[FUNCTION_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DICNRNS+"}\n" + 
		"BasicTestReferences[FUNCTION_REF<CONSTRUCTOR>]{BasicTestReferences(), Lorg.eclipse.wst.jsdt.core.tests.BasicTestReferences;, ()V, BasicTestReferences, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test025() throws JavaScriptModelException {
	String[] sources = {
		"/Completion/src/javadoc/tags/package-info.js",
			"/**\n" + 
			" * Completion after:\n" + 
			" * 	@see OtherTypes#\n" + 
			" */\n" + 
			"package javadoc.tags;\n",
		"/Completion/src/javadoc/tags/OtherTypes.js",
			"package javadoc.tags;\n" + 
			"public class OtherTypes {\n" + 
			"	int foo;\n" + 
			"	Object obj;\n" + 
			"}"
	};
	completeInJavadoc(sources, true, "#", 0); // empty token
	assertResults(
		"obj[FIELD_REF]{obj, Ljavadoc.tags.OtherTypes;, Ljava.lang.Object;, obj, null, "+this.positions+R_DICNRNS+"}\n" + 
		"foo[FIELD_REF]{foo, Ljavadoc.tags.OtherTypes;, I, foo, null, "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(long, int), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(long), Ljava.lang.Object;, (J)V, wait, (millis), "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DICNRNS+"}\n" + 
		"toString[FUNCTION_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DICNRNS+"}\n" + 
		"notifyAll[FUNCTION_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DICNRNS+"}\n" + 
		"notify[FUNCTION_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DICNRNS+"}\n" + 
		"hashCode[FUNCTION_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DICNRNS+"}\n" + 
		"getClass[FUNCTION_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class;, getClass, null, "+this.positions+R_DICNRNS+"}\n" + 
		"finalize[FUNCTION_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DICNRNS+"}\n" + 
		"equals[FUNCTION_REF]{equals(Object), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+this.positions+R_DICNRNS+"}\n" + 
		"clone[FUNCTION_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DICNRNS+"}\n" + 
		"OtherTypes[FUNCTION_REF<CONSTRUCTOR>]{OtherTypes(), Ljavadoc.tags.OtherTypes;, ()V, OtherTypes, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

/**
 * @category Tests for methods completion
 */
public void test030() throws JavaScriptModelException {
	String[] sources = {
		"/Completion/src/javadoc/tags/package-info.js",
			"/**\n" + 
			" * Completion after:\n" + 
			" * 	@see OtherTypes#meth\n" + 
			" */\n" + 
			"package javadoc.tags;",
		"/Completion/src/javadoc/tags/OtherTypes.js",
			"package javadoc.tags;\n" + 
			"public class OtherTypes {\n" + 
			"	void method() {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "meth");
	assertResults(
		"method[FUNCTION_REF]{method(), Ljavadoc.tags.OtherTypes;, ()V, method, null, "+this.positions+R_DICNRNS+"}"
	);
}
public void test031() throws JavaScriptModelException {
	String[] sources = {
		"/Completion/src/javadoc/tags/package-info.js",
			"/**\n" + 
			" * Completion after:\n" + 
			" * 	@see OtherTypes#\n" + 
			" */\n" + 
			"package javadoc.tags;",
		"/Completion/src/javadoc/tags/OtherTypes.js",
			"package javadoc.tags;\n" + 
			"public class OtherTypes {\n" + 
			"	void method() {};\n" +
			"	void foo() {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "#", 0); // empty token
	assertResults(
		"method[FUNCTION_REF]{method(), Ljavadoc.tags.OtherTypes;, ()V, method, null, "+this.positions+R_DICNRNS+"}\n" + 
		"foo[FUNCTION_REF]{foo(), Ljavadoc.tags.OtherTypes;, ()V, foo, null, "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(long, int), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(long), Ljava.lang.Object;, (J)V, wait, (millis), "+this.positions+R_DICNRNS+"}\n" + 
		"wait[FUNCTION_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DICNRNS+"}\n" + 
		"toString[FUNCTION_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DICNRNS+"}\n" + 
		"notifyAll[FUNCTION_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DICNRNS+"}\n" + 
		"notify[FUNCTION_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DICNRNS+"}\n" + 
		"hashCode[FUNCTION_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DICNRNS+"}\n" + 
		"getClass[FUNCTION_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class;, getClass, null, "+this.positions+R_DICNRNS+"}\n" + 
		"finalize[FUNCTION_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DICNRNS+"}\n" + 
		"equals[FUNCTION_REF]{equals(Object), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+this.positions+R_DICNRNS+"}\n" + 
		"clone[FUNCTION_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DICNRNS+"}\n" + 
		"OtherTypes[FUNCTION_REF<CONSTRUCTOR>]{OtherTypes(), Ljavadoc.tags.OtherTypes;, ()V, OtherTypes, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

/**
 * @category Tests for constructors completion
 */
public void test040() throws JavaScriptModelException {
	String[] sources = {
		"/Completion/src/javadoc/tags/package-info.js",
			"/**\n" + 
			" * Completion after:\n" + 
			" * 	@see OtherTypes#O\n" + 
			" */\n" + 
			"package javadoc.tags;\n",
		"/Completion/src/javadoc/tags/OtherTypes.js",
			"package javadoc.tags;\n" + 
			"public class OtherTypes {\n" + 
			"	void method() {};\n" +
			"	void foo() {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "O", 2); // 2nd occurence
	assertResults(
		"OtherTypes[FUNCTION_REF<CONSTRUCTOR>]{OtherTypes(), Ljavadoc.tags.OtherTypes;, ()V, OtherTypes, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}
public void test041() throws JavaScriptModelException {
	String[] sources = {
		"/Completion/src/javadoc/tags/package-info.js",
			"/**\n" + 
			" * Completion after:\n" + 
			" * 	@see OtherTypes#O\n" + 
			" */\n" + 
			"package javadoc.tags;\n",
		"/Completion/src/javadoc/tags/OtherTypes.js",
			"package javadoc.tags;\n" + 
			"public class OtherTypes {\n" + 
			"	OtherTypes(int x) {};\n" +
			"	OtherTypes(Object obj, String str) {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "O", 2); // 2nd occurence
	assertResults(
		"OtherTypes[FUNCTION_REF<CONSTRUCTOR>]{OtherTypes(Object, String), Ljavadoc.tags.OtherTypes;, (Ljava.lang.Object;Ljava.lang.String;)V, OtherTypes, (obj, str), "+this.positions+JAVADOC_RELEVANCE+"}\n" + 
		"OtherTypes[FUNCTION_REF<CONSTRUCTOR>]{OtherTypes(int), Ljavadoc.tags.OtherTypes;, (I)V, OtherTypes, (x), "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}
}
