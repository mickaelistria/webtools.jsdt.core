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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.internal.codeassist.RelevanceConstants;

import junit.framework.*;

public class CompletionWithMissingTypesTests2 extends ModifyingResourceTests implements RelevanceConstants {

public CompletionWithMissingTypesTests2(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	setUpJavaProject("Completion");
}
public void tearDownSuite() throws Exception {
	deleteProject("Completion");
	
	super.tearDownSuite();
}

protected static void assertResults(String expected, String actual) {
	try {
		assertEquals(expected, actual);
	} catch(ComparisonFailure c) {
		System.out.println(actual);
		System.out.println();
		throw c;
	}
}
static {
//	TESTS_NAMES = new String[] { "testBug96950" };
}
public static Test suite() {
	return buildModelTestSuite(CompletionWithMissingTypesTests2.class);
}

File createFile(File parent, String name, String content) throws IOException {
	File file = new File(parent, name);
	FileOutputStream out = new FileOutputStream(file);
	out.write(content.getBytes());
	out.close();
	return file;
}
File createDirectory(File parent, String name) {
	File dir = new File(parent, name);
	dir.mkdirs();
	return dir;
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0001() throws Exception {
	Hashtable oldOptions = JavaScriptCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaScriptCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaScriptCore.ERROR);
		options.put(JavaScriptCore.COMPILER_PB_DISCOURAGED_REFERENCE, JavaScriptCore.WARNING);
		options.put(JavaScriptCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaScriptCore.DISABLED);
		options.put(JavaScriptCore.CODEASSIST_DISCOURAGED_REFERENCE_CHECK, JavaScriptCore.DISABLED);
		JavaScriptCore.setOptions(options);
		
		// create variable
//		JavaScriptCore.setClasspathVariables(
//			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
//			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
//			null);

		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			new String[]{"JCL_LIB"});
		
		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/XX.js",
				"package a;\n"+
				"public class XX {\n"+
				"  void foo() {}\n"+
				"}");

		this.createFolder("/P1/src/b");
		this.createFile(
				"/P1/src/b/XX.js",
				"package b;\n"+
				"public class XX {\n"+
				"  void foo() {}\n"+
				"}");
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			new String[]{"JCL_LIB"},
			null,
			null,
			new String[]{"/P1"},
			new String[][]{{}},
			new String[][]{{"a/*"}},
			new boolean[]{false},
			null,
			null,
			"1.4");
		this.createFile(
			"/P2/src/YY.js",
			"public class YY {\n"+
			"  void foo() {\n"+
			"    XX x = null;\n"+
			"    x.fo\n"+
			"  }\n"+
			"}");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
		requestor.allowAllRequiredProposals();
		IJavaScriptUnit cu= getCompilationUnit("P2", "src", "", "YY.js");
		
		String str = cu.getSource();
		String completeBehind = "x.fo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
		
		int relevance1 = R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
		int start1 = str.lastIndexOf("x.fo") + "x.".length();
		int end1 = start1 + "fo".length();
		int start2 = str.lastIndexOf("XX");
		int end2 = start2 + "XX".length();
		assertResults(
				"foo[FUNCTION_REF]{foo(), La.XX;, ()V, foo, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
				"   XX[TYPE_REF]{a.XX, a, La.XX;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}\n" +
				"foo[FUNCTION_REF]{foo(), Lb.XX;, ()V, foo, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
				"   XX[TYPE_REF]{b.XX, b, Lb.XX;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
				requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		JavaScriptCore.setOptions(oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0002() throws Exception {
	Hashtable oldOptions = JavaScriptCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaScriptCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaScriptCore.ERROR);
		options.put(JavaScriptCore.COMPILER_PB_DISCOURAGED_REFERENCE, JavaScriptCore.WARNING);
		options.put(JavaScriptCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaScriptCore.ENABLED);
		options.put(JavaScriptCore.CODEASSIST_DISCOURAGED_REFERENCE_CHECK, JavaScriptCore.DISABLED);
		JavaScriptCore.setOptions(options);
		
		// create variable
//		JavaScriptCore.setClasspathVariables(
//			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
//			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
//			null);

		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			new String[]{"JCL_LIB"});
		
		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/XX.js",
				"package a;\n"+
				"public class XX {\n"+
				"  void foo() {}\n"+
				"}");

		this.createFolder("/P1/src/b");
		this.createFile(
				"/P1/src/b/XX.js",
				"package b;\n"+
				"public class XX {\n"+
				"  void foo() {}\n"+
				"}");
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			new String[]{"JCL_LIB"},
			null,
			null,
			new String[]{"/P1"},
			new String[][]{{}},
			new String[][]{{"a/*"}},
			new boolean[]{false},
			null,
			null,
			"1.4");
		this.createFile(
			"/P2/src/YY.js",
			"public class YY {\n"+
			"  void foo() {\n"+
			"    XX x = null;\n"+
			"    x.fo\n"+
			"  }\n"+
			"}");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
		requestor.allowAllRequiredProposals();
		IJavaScriptUnit cu= getCompilationUnit("P2", "src", "", "YY.js");
		
		String str = cu.getSource();
		String completeBehind = "x.fo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
		
		int relevance1 = R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
		int start1 = str.lastIndexOf("x.fo") + "x.".length();
		int end1 = start1 + "fo".length();
		int start2 = str.lastIndexOf("XX");
		int end2 = start2 + "XX".length();
		assertResults(
				"foo[FUNCTION_REF]{foo(), Lb.XX;, ()V, foo, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
				"   XX[TYPE_REF]{b.XX, b, Lb.XX;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
				requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		JavaScriptCore.setOptions(oldOptions);
	}
}
}
