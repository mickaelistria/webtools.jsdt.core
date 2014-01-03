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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.IJavaScriptElement;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.WorkingCopyOwner;

public class SelectionJavadocModelTests extends AbstractJavaModelTests {
	
	IJavaScriptElement element;

	public SelectionJavadocModelTests(String name) {
		super(name, 3);
//		this.endChar = "";
		this.displayName = true;
	}

	static {
//		TESTS_PREFIX = "testBug";
//		TESTS_NUMBERS = new int[] { 86380 };
//		TESTS_RANGE = new int[] { 13, 16 };
	}

	public static Test suite() {
		return buildModelTestSuite(SelectionJavadocModelTests.class);
	}

	public void setUpSuite() throws Exception {
		super.setUpSuite();	
		setUpJavaProject("Tests", "1.5");
	}
	public void tearDownSuite() throws Exception {
		deleteProject("Tests");
		super.tearDownSuite();
	}

	void setUnit(String name, String source) throws JavaScriptModelException {
		this.workingCopies = new IJavaScriptUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Tests/"+name, source);
	}

	void assertElementEquals(String message, String expected) {
		assertElementEquals(message, expected, this.element);
	}

	void assertSelectionIsEmpty(IJavaScriptUnit unit, String selection) throws JavaScriptModelException {
		assertSelectionIsEmpty(unit, selection, 1);
	}

	void assertSelectionIsEmpty(IJavaScriptUnit unit, String selection, int occurences) throws JavaScriptModelException {
		int[] selectionPositions = selectionInfo(unit, selection, occurences);
		IJavaScriptElement[] elements = unit.codeSelect(selectionPositions[0], selectionPositions[1]);
		assertTrue("Selection should be empty", elements == null || elements.length == 0);
	}

	public void test00() throws JavaScriptModelException {
		setUnit("Test.js",
			"" + 
			"" + 
			"function bar() {\n" + 
			"		foo();\n" + 
			"}\n" + 
			"function foo() {}\n" + 
			"}\n"
		);
		this.element = selectMethod(this.workingCopies[0], "foo");
		assertElementEquals("Invalid selected method",
			"foo() [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]"
		);
	}


	public void test01() throws JavaScriptModelException {
		setUnit("Test.js",
			"" + 
			"" + 
			"function foo() {}\n" + 
			"}\n" + 
			"function bar() {\n" + 
			"		foo();\n" + 
			"}\n"
		);
		this.element = selectMethod(this.workingCopies[0], "foo");
		assertElementEquals("Invalid selected method",
			"foo() [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]"
		);
	}

	public void test01a() throws JavaScriptModelException {
		setUnit("Test.js",
			"" + 
			"" + 
			"function bar() {\n" + 
			"		foo();\n" + 
			"}\n" + 
			"function foo() {}\n" + 
			"}\n"
		);
		this.element = selectMethod(this.workingCopies[0], "foo");
		assertElementEquals("Invalid selected method",
			"foo() [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]"
		);
	}

	public void test02() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	/** {@link #foo() foo} */\n" + 
			"	void bar() {\n" + 
			"		foo();\n" + 
			"	}\n" + 
			"	void foo() {}\n" + 
			"}\n"
		);
		this.element = selectMethod(this.workingCopies[0], "foo");
		assertElementEquals("Invalid selected method",
			"foo() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]"
		);
	}

	public void test03() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	/** @see Test */\n" + 
			"	void foo() {}\n" + 
			"}\n"
		);
		this.element = selectType(this.workingCopies[0], "Test", 2);
		assertElementEquals("Invalid selected type",
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]"
		);
	}

	public void test04() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	/** Javadoc {@link Test} */\n" + 
			"	void foo() {}\n" + 
			"}\n"
		);
		this.element = selectType(this.workingCopies[0], "Test", 2);
		assertElementEquals("Invalid selected type",
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]"
		);
	}

	public void test05() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	int field;\n" + 
			"	/** @see #field */\n" + 
			"	void foo() {}\n" + 
			"}\n"
		);
		this.element = selectField(this.workingCopies[0], "field", 2);
		assertElementEquals("Invalid selected field",
			"field [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]"
		);
	}

	public void test06() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	int field;\n" + 
			"	/**{@link #field}*/\n" + 
			"	void foo() {}\n" + 
			"}\n"
		);
		this.element = selectField(this.workingCopies[0], "field", 2);
		assertElementEquals("Invalid selected field",
			"field [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]"
		);
	}

	public void test07() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	/**\n" + 
			"	 * @see Test#field\n" + 
			"	 * @see #foo(int, String)\n" + 
			"	 * @see Test#foo(int, String)\n" + 
			"	 */\n" + 
			"	void bar() {\n" + 
			"		foo(0, \"\");\n" + 
			"	}\n" + 
			"	int field;\n" + 
			"	void foo(int x, String s) {}\n" + 
			"}\n"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[7];
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		elements[1] = selectField(this.workingCopies[0], "field");
		elements[2] = selectMethod(this.workingCopies[0], "foo");
		elements[3] = selectType(this.workingCopies[0], "String");
		elements[4] = selectType(this.workingCopies[0], "Test", 3);
		elements[5] = selectMethod(this.workingCopies[0], "foo", 2);
		elements[6] = selectType(this.workingCopies[0], "String", 2);
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"field [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"foo(int, String) [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"String [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]\n" + 
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"foo(int, String) [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"String [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]",
			elements
		);
	}

	public void test08() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	/**\n" + 
			"	 * First {@link #foo(int, String)}\n" + 
			"	 * Second {@link Test#foo(int, String) method foo}\n" + 
			"	 * Third {@link Test#field field}\n" + 
			"	 */\n" + 
			"	void bar() {\n" + 
			"		foo(0, \"\");\n" + 
			"	}\n" + 
			"	int field;\n" + 
			"	void foo(int x, String s) {}\n" + 
			"}\n"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[7];
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		elements[1] = selectField(this.workingCopies[0], "field");
		elements[2] = selectMethod(this.workingCopies[0], "foo");
		elements[3] = selectType(this.workingCopies[0], "String");
		elements[4] = selectType(this.workingCopies[0], "Test", 3);
		elements[5] = selectMethod(this.workingCopies[0], "foo", 2);
		elements[6] = selectType(this.workingCopies[0], "String", 2);
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"field [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"foo(int, String) [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"String [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]\n" + 
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"foo(int, String) [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"String [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]",
			elements
		);
	}

	public void test09() throws JavaScriptModelException {
		setUnit("test/junit/Test.js",
			"package test.junit;\n" + 
			"public class Test {\n" + 
			"	/**\n" + 
			"	 * @see test.junit.Test\n" + 
			"	 * @see test.junit.Test#field\n" + 
			"	 * @see test.junit.Test#foo(Object[] array)\n" + 
			"	 */\n" + 
			"	void bar() {\n" + 
			"		foo(null);\n" + 
			"	}\n" + 
			"	int field;\n" + 
			"	void foo(Object[] array) {}\n" + 
			"}\n"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[6];
		assertSelectionIsEmpty(this.workingCopies[0], "test", 2);
		assertSelectionIsEmpty(this.workingCopies[0], "junit", 2);
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		assertSelectionIsEmpty(this.workingCopies[0], "test", 3);
		assertSelectionIsEmpty(this.workingCopies[0], "junit", 3);
		elements[1] = selectType(this.workingCopies[0], "Test", 3);
		elements[2] = selectField(this.workingCopies[0], "field");
		assertSelectionIsEmpty(this.workingCopies[0], "test", 4);
		assertSelectionIsEmpty(this.workingCopies[0], "junit", 4);
		elements[3] = selectType(this.workingCopies[0], "Test", 4);
		elements[4] = selectMethod(this.workingCopies[0], "foo");
		elements[5] = selectType(this.workingCopies[0], "Object");
		assertSelectionIsEmpty(this.workingCopies[0], "array");
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.js [in test.junit [in <project root> [in Tests]]]]\n" + 
			"Test [in [Working copy] Test.js [in test.junit [in <project root> [in Tests]]]]\n" + 
			"field [in Test [in [Working copy] Test.js [in test.junit [in <project root> [in Tests]]]]]\n" + 
			"Test [in [Working copy] Test.js [in test.junit [in <project root> [in Tests]]]]\n" + 
			"foo(Object[]) [in Test [in [Working copy] Test.js [in test.junit [in <project root> [in Tests]]]]]\n" + 
			"Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]",
			elements
		);
	}

	public void test10() throws JavaScriptModelException {
		setUnit("test/junit/Test.js",
			"package test.junit;\n" + 
			"public class Test {\n" + 
			"	/** Javadoc {@linkplain test.junit.Test}\n" + 
			"	 * {@linkplain test.junit.Test#field field}\n" + 
			"	 * last line {@linkplain test.junit.Test#foo(Object[] array) foo(Object[])}\n" + 
			"	 */\n" + 
			"	void bar() {\n" + 
			"		foo(null);\n" + 
			"	}\n" + 
			"	int field;\n" + 
			"	void foo(Object[] array) {}\n" + 
			"}\n"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[6];
		assertSelectionIsEmpty(this.workingCopies[0], "test", 2);
		assertSelectionIsEmpty(this.workingCopies[0], "junit", 2);
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		assertSelectionIsEmpty(this.workingCopies[0], "test", 3);
		assertSelectionIsEmpty(this.workingCopies[0], "junit", 3);
		elements[1] = selectType(this.workingCopies[0], "Test", 3);
		elements[2] = selectField(this.workingCopies[0], "field");
		assertSelectionIsEmpty(this.workingCopies[0], "test", 4);
		assertSelectionIsEmpty(this.workingCopies[0], "junit", 4);
		elements[3] = selectType(this.workingCopies[0], "Test", 4);
		elements[4] = selectMethod(this.workingCopies[0], "foo");
		elements[5] = selectType(this.workingCopies[0], "Object");
		assertSelectionIsEmpty(this.workingCopies[0], "array");
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.js [in test.junit [in <project root> [in Tests]]]]\n" + 
			"Test [in [Working copy] Test.js [in test.junit [in <project root> [in Tests]]]]\n" + 
			"field [in Test [in [Working copy] Test.js [in test.junit [in <project root> [in Tests]]]]]\n" + 
			"Test [in [Working copy] Test.js [in test.junit [in <project root> [in Tests]]]]\n" + 
			"foo(Object[]) [in Test [in [Working copy] Test.js [in test.junit [in <project root> [in Tests]]]]]\n" + 
			"Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]",
			elements
		);
	}

	public void test11() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	/**\n" + 
			"	 * @throws RuntimeException runtime exception\n" + 
			"	 * @throws InterruptedException interrupted exception\n" + 
			"	 */\n" + 
			"	void foo() {}\n" + 
			"}\n"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[2];
		elements[0] = selectType(this.workingCopies[0], "RuntimeException");
		elements[1] = selectType(this.workingCopies[0], "InterruptedException");
		assertElementsEqual("Invalid selection(s)",
			"RuntimeException [in RuntimeException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]\n" + 
			"InterruptedException [in InterruptedException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]",
			elements
		);
	}

	public void test12() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	/**\n" + 
			"	 * @exception RuntimeException runtime exception\n" + 
			"	 * @exception InterruptedException interrupted exception\n" + 
			"	 */\n" + 
			"	void foo() {}\n" + 
			"}\n"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[2];
		elements[0] = selectType(this.workingCopies[0], "RuntimeException");
		elements[1] = selectType(this.workingCopies[0], "InterruptedException");
		assertElementsEqual("Invalid selection(s)",
			"RuntimeException [in RuntimeException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]\n" + 
			"InterruptedException [in InterruptedException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]",
			elements
		);
	}

	public void test13() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	/**\n" + 
			"	 * @param xxx integer param\n" +
			"	 * @param str string param\n" +
			"	 */\n" + 
			"	void foo(int xxx, String str) {}\n" + 
			"}\n"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[2];
		elements[0] = selectLocalVariable(this.workingCopies[0], "xxx");
		elements[1] = selectLocalVariable(this.workingCopies[0], "str");
		assertElementsEqual("Invalid selection(s)",
			"xxx [in foo(int, String) [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]\n" + 
			"str [in foo(int, String) [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]",
			elements
		);
	}

	public void test14() throws JavaScriptModelException {
		setUnit("Test.js",
			"/**\n" + 
			" * Javadoc of {@link Test}\n" + 
			" * @see Field#foo\n" + 
			" */\n" + 
			"public class Test {}\n" + 
			"/**\n" + 
			" * Javadoc on {@link Field} to test selection in javadoc field references\n" + 
			" * @see #foo\n" + 
			" */\n" + 
			"class Field {\n" + 
			"	/**\n" + 
			"	 * Javadoc on {@link #foo} to test selection in javadoc field references\n" + 
			"	 * @see #foo\n" + 
			"	 * @see Field#foo\n" + 
			"	 */\n" + 
			"	int foo;\n" + 
			"}\n"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[9];
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		elements[1] = selectType(this.workingCopies[0], "Field");
		elements[2] = selectField(this.workingCopies[0], "foo");
		elements[3] = selectType(this.workingCopies[0], "Field", 2);
		elements[4] = selectField(this.workingCopies[0], "foo", 2);
		elements[5] = selectField(this.workingCopies[0], "foo", 3);
		elements[6] = selectField(this.workingCopies[0], "foo", 4);
		elements[7] = selectType(this.workingCopies[0], "Field", 4);
		elements[8] = selectField(this.workingCopies[0], "foo", 5);
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"Field [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"foo [in Field [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"Field [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"foo [in Field [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"foo [in Field [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"foo [in Field [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"Field [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"foo [in Field [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]",
			elements
		);
	}

	public void test15() throws JavaScriptModelException {
		setUnit("Test.js",
			"/**\n" + 
			" * Javadoc of {@link Test}\n" + 
			" * @see Method#foo\n" + 
			" */\n" + 
			"public class Test {}\n" + 
			"/**\n" + 
			" * Javadoc on {@link Method} to test selection in javadoc method references\n" + 
			" * @see #foo\n" + 
			" */\n" + 
			"class Method {\n" + 
			"	/**\n" + 
			"	 * Javadoc on {@link #foo} to test selection in javadoc method references\n" + 
			"	 * @see #foo\n" + 
			"	 * @see Method#foo\n" + 
			"	 */\n" + 
			"	void bar() {}\n" + 
			"	/**\n" + 
			"	 * Method with parameter and throws clause to test selection in javadoc\n" + 
			"	 * @param xxx TODO\n" + 
			"	 * @param str TODO\n" + 
			"	 * @throws RuntimeException blabla\n" + 
			"	 * @throws InterruptedException bloblo\n" + 
			"	 */\n" + 
			"	void foo(int xxx, String str) throws RuntimeException, InterruptedException {}\n" + 
			"}\n"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[13];
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		elements[1] = selectType(this.workingCopies[0], "Method");
		elements[2] = selectMethod(this.workingCopies[0], "foo");
		elements[3] = selectType(this.workingCopies[0], "Method", 2);
		elements[4] = selectMethod(this.workingCopies[0], "foo", 2);
		elements[5] = selectMethod(this.workingCopies[0], "foo", 3);
		elements[6] = selectMethod(this.workingCopies[0], "foo", 4);
		elements[7] = selectType(this.workingCopies[0], "Method", 4);
		elements[8] = selectMethod(this.workingCopies[0], "foo", 5);
		elements[9] = selectLocalVariable(this.workingCopies[0], "xxx");
		elements[10] = selectLocalVariable(this.workingCopies[0], "str");
		elements[11] = selectType(this.workingCopies[0], "RuntimeException");
		elements[12] = selectType(this.workingCopies[0], "InterruptedException");
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"Method [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"foo(int, String) [in Method [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"Method [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"foo(int, String) [in Method [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"foo(int, String) [in Method [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"foo(int, String) [in Method [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"Method [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"foo(int, String) [in Method [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"xxx [in foo(int, String) [in Method [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]\n" + 
			"str [in foo(int, String) [in Method [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]\n" + 
			"RuntimeException [in RuntimeException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]\n" + 
			"InterruptedException [in InterruptedException.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in Tests]]]]",
			elements
		);
	}

	public void test16() throws JavaScriptModelException {
		setUnit("Test.js",
			"/**\n" + 
			" * Javadoc of {@link Test}\n" + 
			" * @see Other\n" + 
			" */\n" + 
			"public class Test {}\n" + 
			"/**\n" + 
			" * Javadoc of {@link Other}\n" + 
			" * @see Test\n" + 
			" */\n" + 
			"class Other {}\n"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[4];
		elements[0] = selectType(this.workingCopies[0], "Test");
		elements[1] = selectType(this.workingCopies[0], "Other");
		elements[2] = selectType(this.workingCopies[0], "Test", 3);
		elements[3] = selectType(this.workingCopies[0], "Other", 2);
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"Other [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"Other [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]",
			elements
		);
	}

	public void test17() throws JavaScriptModelException {
		setUnit("Test.js",
			"/**\n" + 
			" * @see Test.Field#foo\n" + 
			" */\n" + 
			"public class Test {\n" + 
			"	/**\n" + 
			"	 * @see Field#foo\n" + 
			"	 */\n" + 
			"	class Field {\n" + 
			"		/**\n" + 
			"		 * @see #foo\n" + 
			"		 * @see Field#foo\n" + 
			"		 * @see Test.Field#foo\n" + 
			"		 */\n" + 
			"		int foo;\n" + 
			"	}\n" + 
			"}\n"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[11];
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		elements[1] = selectType(this.workingCopies[0], "Field");
		elements[2] = selectField(this.workingCopies[0], "foo");
		elements[3] = selectType(this.workingCopies[0], "Field", 2);
		elements[4] = selectField(this.workingCopies[0], "foo", 2);
		elements[5] = selectField(this.workingCopies[0], "foo", 3);
		elements[6] = selectType(this.workingCopies[0], "Field", 4);
		elements[7] = selectField(this.workingCopies[0], "foo", 4);
		elements[8] = selectType(this.workingCopies[0], "Test", 3);
		elements[9] = selectType(this.workingCopies[0], "Field", 5);
		elements[10] = selectField(this.workingCopies[0], "foo", 5);
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"Field [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"foo [in Field [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]\n" + 
			"Field [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"foo [in Field [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]\n" + 
			"foo [in Field [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]\n" + 
			"Field [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"foo [in Field [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]\n" + 
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"Field [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"foo [in Field [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]",
			elements
		);
	}

	public void test18() throws JavaScriptModelException {
		setUnit("Test.js",
			"/**\n" + 
			" * @see Test.Method#foo()\n" + 
			" */\n" + 
			"public class Test {\n" + 
			"	/**\n" + 
			"	 * @see Method#foo()\n" + 
			"	 */\n" + 
			"	class Method {\n" + 
			"		/**\n" + 
			"		 * @see #foo()\n" + 
			"		 * @see Method#foo()\n" + 
			"		 * @see Test.Method#foo()\n" + 
			"		 */\n" + 
			"		void foo() {}\n" + 
			"	}\n" + 
			"}"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[11];
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		elements[1] = selectType(this.workingCopies[0], "Method");
		elements[2] = selectMethod(this.workingCopies[0], "foo");
		elements[3] = selectType(this.workingCopies[0], "Method", 2);
		elements[4] = selectMethod(this.workingCopies[0], "foo", 2);
		elements[5] = selectMethod(this.workingCopies[0], "foo", 3);
		elements[6] = selectType(this.workingCopies[0], "Method", 4);
		elements[7] = selectMethod(this.workingCopies[0], "foo", 4);
		elements[8] = selectType(this.workingCopies[0], "Test", 3);
		elements[9] = selectType(this.workingCopies[0], "Method", 5);
		elements[10] = selectMethod(this.workingCopies[0], "foo", 5);
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"Method [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"foo() [in Method [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]\n" + 
			"Method [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"foo() [in Method [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]\n" + 
			"foo() [in Method [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]\n" + 
			"Method [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"foo() [in Method [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]\n" + 
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"Method [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"foo() [in Method [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]",
			elements
		);
	}

	public void test19() throws JavaScriptModelException {
		setUnit("Test.js",
			"/**\n" + 
			" * @see Test.Other\n" + 
			" */\n" + 
			"public class Test {\n" + 
			"	/**\n" + 
			"	 * @see Test\n" + 
			"	 * @see Other\n" + 
			"	 * @see Test.Other\n" + 
			"	 */\n" + 
			"	class Other {}\n" + 
			"}"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[6];
		elements[0] = selectType(this.workingCopies[0], "Test");
		elements[1] = selectType(this.workingCopies[0], "Other");
		elements[2] = selectType(this.workingCopies[0], "Test", 3);
		elements[3] = selectType(this.workingCopies[0], "Other", 2);
		elements[4] = selectType(this.workingCopies[0], "Test", 4);
		elements[5] = selectType(this.workingCopies[0], "Other", 3);
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"Other [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"Other [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"Other [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]",
			elements
		);
	}

	public void test20() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	void bar() {\n" + 
			"		/**\n" + 
			"		 * @see Field#foo\n" + 
			"		 */\n" + 
			"		class Field {\n" + 
			"			/**\n" + 
			"			 * @see #foo\n" + 
			"			 * @see Field#foo\n" + 
			"			 */\n" + 
			"			int foo;\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[5];
		elements[0] = selectType(this.workingCopies[0], "Field");
		elements[1] = selectField(this.workingCopies[0], "foo");
		elements[2] = selectField(this.workingCopies[0], "foo", 2);
		elements[3] = selectType(this.workingCopies[0], "Field", 3);
		elements[4] = selectField(this.workingCopies[0], "foo", 3);
		// Running test with Unix/Windows do not matter even if result includes positions as we use working copies
		assertElementsEqual("Invalid selection(s)",
			"Field [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]\n" + 
			"foo [in Field [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]\n" + 
			"foo [in Field [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]\n" + 
			"Field [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]\n" + 
			"foo [in Field [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]",
			elements
		);
	}

	public void test21() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	void bar() {\n" + 
			"		/**\n" + 
			"		 * @see Method#foo()\n" + 
			"		 */\n" + 
			"		class Method {\n" + 
			"			/**\n" + 
			"			 * @see #foo()\n" + 
			"			 * @see Method#foo()\n" + 
			"			 */\n" + 
			"			void foo() {}\n" + 
			"		}\n" + 
			"	}\n" + 
			"}"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[5];
		elements[0] = selectType(this.workingCopies[0], "Method");
		elements[1] = selectMethod(this.workingCopies[0], "foo");
		elements[2] = selectMethod(this.workingCopies[0], "foo", 2);
		elements[3] = selectType(this.workingCopies[0], "Method", 3);
		elements[4] = selectMethod(this.workingCopies[0], "foo", 3);
		// Running test with Unix/Windows do not matter even if result includes positions as we use working copies
		assertElementsEqual("Invalid selection(s)",
			"Method [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]\n" + 
			"foo() [in Method [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]\n" + 
			"foo() [in Method [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]\n" + 
			"Method [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]\n" + 
			"foo() [in Method [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]",
			elements
		);
	}

	public void test22() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	void bar() {\n" + 
			"		/**\n" + 
			"		 * @see Test\n" + 
			"		 * @see Other\n" + 
			"		 */\n" + 
			"		class Other {}\n" + 
			"	}\n" + 
			"}"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[2];
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		elements[1] = selectType(this.workingCopies[0], "Other");
		// Running test with Unix/Windows do not matter even if result includes positions as we use working copies
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"Other [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]",
			elements
		);
	}

	public void test23() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	void bar() {\n" + 
			"		new Object() {\n" + 
			"			/**\n" + 
			"			 * @see Field#foo\n" + 
			"			 */\n" + 
			"			class Field {\n" + 
			"				/**\n" + 
			"				 * @see #foo\n" + 
			"				 * @see Field#foo\n" + 
			"				 */\n" + 
			"				int foo;\n" + 
			"			}\n" + 
			"		};\n" + 
			"	}\n" + 
			"}\n"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[5];
		elements[0] = selectType(this.workingCopies[0], "Field");
		elements[1] = selectField(this.workingCopies[0], "foo");
		elements[2] = selectField(this.workingCopies[0], "foo", 2);
		elements[3] = selectType(this.workingCopies[0], "Field", 3);
		elements[4] = selectField(this.workingCopies[0], "foo", 3);
		// Running test with Unix/Windows do not matter even if result includes positions as we use working copies
		assertElementsEqual("Invalid selection(s)",
			"Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]\n" + 
			"foo [in Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]]\n" + 
			"foo [in Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]]\n" + 
			"Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]\n" + 
			"foo [in Field [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]]",
			elements
		);
	}

	public void test24() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	void bar() {\n" + 
			"		new Object() {\n" + 
			"			/**\n" + 
			"			 * @see Method#foo()\n" + 
			"			 */\n" + 
			"			class Method {\n" + 
			"				/**\n" + 
			"				 * @see #foo()\n" + 
			"				 * @see Method#foo()\n" + 
			"				 */\n" + 
			"				void foo() {}\n" + 
			"			}\n" + 
			"		};\n" + 
			"	}\n" + 
			"}"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[5];
		elements[0] = selectType(this.workingCopies[0], "Method");
		elements[1] = selectMethod(this.workingCopies[0], "foo");
		elements[2] = selectMethod(this.workingCopies[0], "foo", 2);
		elements[3] = selectType(this.workingCopies[0], "Method", 3);
		elements[4] = selectMethod(this.workingCopies[0], "foo", 3);
		// Running test with Unix/Windows do not matter even if result includes positions as we use working copies
		assertElementsEqual("Invalid selection(s)",
			"Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]\n" + 
			"foo() [in Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]]\n" + 
			"foo() [in Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]]\n" + 
			"Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]\n" + 
			"foo() [in Method [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]]",
			elements
		);
	}

	public void test25() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	void bar() {\n" + 
			"		new Object() {\n" + 
			"			/**\n" + 
			"			 * @see Test\n" + 
			"			 * @see Other\n" + 
			"			 */\n" + 
			"			class Other {}\n" + 
			"		};\n" + 
			"	}\n" + 
			"}"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[2];
		elements[0] = selectType(this.workingCopies[0], "Test", 2);
		elements[1] = selectType(this.workingCopies[0], "Other");
		// Running test with Unix/Windows do not matter even if result includes positions as we use working copies
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"Other [in <anonymous #1> [in bar() [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]]]",
			elements
		);
	}

	public void test26() throws JavaScriptModelException {
		setUnit("Test.js",
			"public class Test {\n" + 
			"	static int field;\n" + 
			"	/** \n" +
			"	 * First {@value #field}" +
			"	 * Second {@value Test#field}" +
			"	 */\n" + 
			"	void foo() {}\n" + 
			"}\n"
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[3];
		elements[0] = selectField(this.workingCopies[0], "field");
		elements[1] = selectType(this.workingCopies[0], "Test");
		elements[2] = selectField(this.workingCopies[0], "field");
		assertElementsEqual("Invalid selection(s)",
			"field [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]\n" + 
			"Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]\n" + 
			"field [in Test [in [Working copy] Test.js [in <default> [in <project root> [in Tests]]]]]",
			elements
		);
	}

	/**
	 * Bug 86380: [1.5][search][annot] Add support to find references inside annotations on a package declaration
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=86380"
	 */
	public void testBug86380() throws CoreException {
		this.wcOwner = new WorkingCopyOwner() {};
		workingCopies = new IJavaScriptUnit[2];
		workingCopies[0] = getWorkingCopy("/Tests/b86380/package-info.js",
			"/**\n" + 
			" * Valid javadoc.\n" + 
			" * @see Test\n" + 
			" * @see Unknown\n" + 
			" * @see Test#foo()\n" + 
			" * @see Test#unknown()\n" + 
			" * @see Test#field\n" + 
			" * @see Test#unknown\n" + 
			" * @param unexpected\n" + 
			" * @throws unexpected\n" + 
			" * @return unexpected \n" + 
			" */\n" + 
			"package b86380;\n",
			wcOwner,
			null/*don't compute problems*/
		);
		workingCopies[1] = getWorkingCopy("/Tests/b86380/Test.js",
			"/**\n" + 
			" * Invalid javadoc\n" + 
			" */\n" + 
			"package b86380;\n" + 
			"public class Test {\n" + 
			"	public int field;\n" + 
			"	public void foo() {}\n" + 
			"}\n",
			wcOwner,
			null/*don't compute problems*/
		);
		IJavaScriptElement[] elements = new IJavaScriptElement[3];
		elements[0] = selectType(this.workingCopies[0], "Test");
		elements[1] = selectMethod(this.workingCopies[0], "foo");
		elements[2] = selectField(this.workingCopies[0], "field");
		assertElementsEqual("Invalid selection(s)",
			"Test [in [Working copy] Test.js [in b86380 [in <project root> [in Tests]]]]\n" + 
			"foo() [in Test [in [Working copy] Test.js [in b86380 [in <project root> [in Tests]]]]]\n" + 
			"field [in Test [in [Working copy] Test.js [in b86380 [in <project root> [in Tests]]]]]",
			elements
		);
	}

	/**
	 * Bug 90266: [select] Code select returns null when there's a string including a slash on same line
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=90266"
	 */
	public void testBug90266_String() throws JavaScriptModelException {
		setUnit("b90266/Test.js",
			"package b90266;\n" + 
			"public class Test {\n" + 
			"	public int field;\n" + 
			"	public void foo(String str, int i) {}\n" +
			"	public void bar() {\n" + 
			"		foo(\"String including / (slash)\", this.field)\n" + 
			"	}\n" + 
			"}\n"
		);
		int[] selectionPositions = selectionInfo(workingCopies[0], "field", 2);
		IJavaScriptElement[] elements = workingCopies[0].codeSelect(selectionPositions[0], 0);
		assertElementsEqual("Invalid selection(s)",
			"field [in Test [in [Working copy] Test.js [in b90266 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	public void testBug90266_Char() throws JavaScriptModelException {
		setUnit("b90266/Test.js",
			"package b90266;\n" + 
			"public class Test {\n" + 
			"	public int field;\n" + 
			"	public void foo(Char c, int i) {}\n" +
			"	public void bar() {\n" + 
			"		foo('/', this.field)\n" + 
			"	}\n" + 
			"}\n"
		);
		int[] selectionPositions = selectionInfo(workingCopies[0], "field", 2);
		IJavaScriptElement[] elements = workingCopies[0].codeSelect(selectionPositions[0], 0);
		assertElementsEqual("Invalid selection(s)",
			"field [in Test [in [Working copy] Test.js [in b90266 [in <project root> [in Tests]]]]]",
			elements
		);
	}
	/**
	);
 * @bug 165701: [model] No hint for ambiguous javadoc
 * @test Ensure that no exception is thrown while selecting method in javadoc comment
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=165701"
 */
public void testBug165701() throws JavaScriptModelException {
	setUnit("b165701/Test.js",
		"package b165701;\n" + 
		"/**\n" + 
		" * @see #fooo(int)\n" + 
		" */\n" + 
		"public class Test {\n" + 
		"	public void foo() {}\n" + 
		"}\n"
	);
	int[] selectionPositions = selectionInfo(workingCopies[0], "fooo", 1);
	IJavaScriptElement[] elements = workingCopies[0].codeSelect(selectionPositions[0], 0);
	assertElementsEqual("Invalid selection(s)",
		"Test [in [Working copy] Test.js [in b165701 [in <project root> [in Tests]]]]",
		elements
	);
}

/**
 * @bug 165794: [model] No hint for ambiguous javadoc
 * @test Ensure that no exception is thrown while selecting method in javadoc comment
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=165794"
 */
public void testBug165794() throws JavaScriptModelException {
	setUnit("b165794/Test.js",
		"package b165794;\n" + 
		"/**\n" + 
		" * No reasonable hint for resolving the {@link #getMax(A)}.\n" + 
		" */\n" + 
		"public class X {\n" + 
		"    /**\n" + 
		"     * Extends Number method.\n" + 
		"     * @see #getMax(A ipZ)\n" + 
		"     */\n" + 
		"    public <T extends Y> T getMax(final A<T> ipY) {\n" + 
		"        return ipY.t();\n" + 
		"    }\n" + 
		"    \n" + 
		"    /**\n" + 
		"     * Extends Exception method.\n" + 
		"     * @see #getMax(A ipY)\n" + 
		"     */\n" + 
		"    public <T extends Z> T getMax(final A<T> ipZ) {\n" + 
		"        return ipZ.t();\n" + 
		"    }\n" + 
		"}\n" + 
		"class A<T> {\n" + 
		"	T t() { return null; }\n" + 
		"}\n" + 
		"class Y {}\n" + 
		"class Z {}"
	);
	int[] selectionPositions = selectionInfo(workingCopies[0], "getMax", 1);
	IJavaScriptElement[] elements = workingCopies[0].codeSelect(selectionPositions[0], 0);
	assertElementsEqual("Invalid selection(s)",
		"getMax(A<T>) [in X [in [Working copy] Test.js [in b165794 [in <project root> [in Tests]]]]]",
		elements
	);
}}
