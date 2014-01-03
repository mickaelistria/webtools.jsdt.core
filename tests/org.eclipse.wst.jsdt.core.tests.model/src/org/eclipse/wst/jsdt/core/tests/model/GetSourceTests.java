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
package org.eclipse.wst.jsdt.core.tests.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.jsdt.core.*;

import junit.framework.Test;

public class GetSourceTests extends ModifyingResourceTests {

	IJavaScriptUnit cu;

	public GetSourceTests(String name) {
		super(name);
	}

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		createJavaProject("P");
		createFolder("/P/p");
		createFile("/P/p/X.js", "package p;\n" + "import java.lang.*;\n"
				+ "public class X {\n" + "  public Object field;\n"
				+ "  private int s\\u0069ze;\n" + "  void foo(String s) {\n"
				+ "    final int var1 = 2;\n" + "    Object var2;\n"
				+ "    for (int i = 0;  i < 10; i++) {}\n" + "  }\n"
				+ "  private int bar() {\n" + "    return 1;\n" + "  }\n"
				+ "  /**\n" + "   * Returns the size.\n" + "   * @return\n"
				+ "   *     the size\n" + "   */\n"
				+ "  int getSiz\\u0065 () {\n" + "    return this.size;\n"
				+ "  }\n" + "}");
		this.cu = getCompilationUnit("/P/p/X.js");
		String cuSource = "package p;\n" + "public class Constants {\n"
				+ "  static final long field1 = 938245798324893L;\n"
				+ "  static final long field2 = 938245798324893l;\n"
				+ "  static final long field3 = 938245798324893;\n"
				+ "  static final char field4 = ' ';\n"
				+ "  static final double field5 = 938245798324893D;\n"
				+ "  static final float field6 = 123456f;\n"
				+ "  static final int field7 = 1<<0;\n" + "}";
		createFile("/P/p/Constants.js", cuSource);
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
		// TESTS_NAMES = new String[] { "TypeParameterBug73884" };
		// Numbers of tests to run: "test<number>" will be run for each number
		// of this array
		// TESTS_NUMBERS = new int[] { 13 };
		// Range numbers of tests to run: all tests between "test<first>" and
		// "test<last>" will be run for { first, last }
		// TESTS_RANGE = new int[] { 16, -1 };
	}

	public static Test suite() {
		return buildModelTestSuite(GetSourceTests.class);
	}

	public void tearDownSuite() throws Exception {
		deleteProject("P");
		super.tearDownSuite();
	}

	/**
	 * Ensure the source for a field contains the modifiers, field type, name,
	 * and terminator.
	 */
	public void testField() throws JavaScriptModelException {
		IType type = this.cu.getType("X");
		IField field = type.getField("field");

		String actualSource = field.getSource();
		String expectedSource = "public Object field;";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}

	/**
	 * Ensure the source for an import contains the 'import' keyword, name, and
	 * terminator.
	 */
	public void testImport() throws JavaScriptModelException {
		IImportDeclaration i = this.cu.getImport("java.lang.*");

		String actualSource = i.getSource();
		String expectedSource = "import java.lang.*;";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}

	/*
	 * Ensures the source for a local variable contains the modifiers, type and
	 * name.
	 */
	public void testLocalVariable1() throws JavaScriptModelException {
		ILocalVariable var = getLocalVariable("/P/p/X.js", "var1 = 2;", "var1");

		String actualSource = ((ISourceReference) var).getSource();
		String expectedSource = "final int var1 = 2;";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}

	/*
	 * Ensures the source for a local variable contains the modifiers, type and
	 * name.
	 */
	public void testLocalVariable2() throws JavaScriptModelException {
		ILocalVariable var = getLocalVariable("/P/p/X.js", "var2;", "var2");

		String actualSource = ((ISourceReference) var).getSource();
		String expectedSource = "Object var2;";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}

	/*
	 * Ensures the source for a local variable contains the modifiers, type and
	 * name.
	 */
	public void testLocalVariable3() throws JavaScriptModelException {
		ILocalVariable var = getLocalVariable("/P/p/X.js", "i = 0;", "i");

		String actualSource = ((ISourceReference) var).getSource();
		String expectedSource = "int i = 0"; // semi-colon is not part of the
												// local declaration in a for
												// statement
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}

	/*
	 * Ensures the source for a local variable contains the modifiers, type and
	 * name.
	 */
	public void testLocalVariable4() throws JavaScriptModelException {
		ILocalVariable var = getLocalVariable("/P/p/X.js", "s) {", "s");

		String actualSource = ((ISourceReference) var).getSource();
		String expectedSource = "String s";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}

	/**
	 * Ensure the source for a method contains the modifiers, return type,
	 * selector, and terminator.
	 */
	public void testMethod() throws JavaScriptModelException {
		IType type = this.cu.getType("X");
		IFunction method = type.getFunction("bar", new String[0]);

		String actualSource = method.getSource();
		String expectedSource = "private int bar() {\n" + "    return 1;\n"
				+ "  }";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}

	/*
	 * Ensures the name range for a method with syntax errors in its header is
	 * correct. (regression test for bug 43139 Delete member in Outliner not
	 * working)
	 */
	public void testNameRangeMethodWithSyntaxError() throws CoreException {
		try {
			String cuSource = "package p;\n" + "public class Y {\n"
					+ "  void foo() {\n" + "  }\n" + "  void static bar() {}\n"
					+ "}";
			createFile("/P/p/Y.js", cuSource);
			IFunction method = getCompilationUnit("/P/p/Y.js").getType("Y")
					.getFunction("bar", new String[0]);

			String actualSource = getNameSource(cuSource, method);
			String expectedSource = "bar";
			assertSourceEquals("Unexpected source'", expectedSource,
					actualSource);
		} finally {
			deleteFile("/P/p/Y.js");
		}
	}

	/*
	 * Ensures the name range for an anonymous class is correct. (regression
	 * test for bug 44450 Strange name range for anonymous classes)
	 */
	public void testNameRangeAnonymous() throws CoreException {
		try {
			String cuSource = "package p;\n" + "public class Y {\n"
					+ "  void foo() {\n" + "    Y y = new Y() {};\n"
					+ "    class C {\n" + "    }\n" + "  }\n" + "}";
			createFile("/P/p/Y.js", cuSource);
			IType anonymous = getCompilationUnit("/P/p/Y.js").getType("Y")
					.getFunction("foo", new String[0]).getType("", 1);

			String actualSource = getNameSource(cuSource, anonymous);
			String expectedSource = "Y";
			assertSourceEquals("Unexpected source'", expectedSource,
					actualSource);
		} finally {
			deleteFile("/P/p/Y.js");
		}
	}

	/**
	 * Ensure the source for a field contains the modifiers, field type, name,
	 * and terminator, and unicode characters.
	 */
	public void testUnicodeField() throws JavaScriptModelException {
		IType type = this.cu.getType("X");
		IField field = type.getField("size");

		String actualSource = field.getSource();
		String expectedSource = "private int s\\u0069ze;";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}

	/**
	 * Ensure the source for a field contains the modifiers, field type, name,
	 * and terminator, and unicode characters.
	 */
	public void testUnicodeMethod() throws JavaScriptModelException {
		IType type = this.cu.getType("X");
		IFunction method = type.getFunction("getSize", null);

		String actualSource = method.getSource();
		String expectedSource = "/**\n" + "   * Returns the size.\n"
				+ "   * @return\n" + "   *     the size\n" + "   */\n"
				+ "  int getSiz\\u0065 () {\n" + "    return this.size;\n"
				+ "  }";
		assertSourceEquals("Unexpected source", expectedSource, actualSource);
	}

	/**
	 * Test the field constant
	 */
	private IField getConstantField(String fieldName) {
		IType type = getCompilationUnit("/P/p/Constants.js").getType(
				"Constants");
		IField field = type.getField(fieldName);
		return field;
	}
}
