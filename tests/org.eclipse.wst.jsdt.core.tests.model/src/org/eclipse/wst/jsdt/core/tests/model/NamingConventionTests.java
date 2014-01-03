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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.wst.jsdt.core.Flags;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.NamingConventions;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;

public class NamingConventionTests extends AbstractJavaModelTests {

IJavaScriptProject project;

public NamingConventionTests(String name) {
	super(name);
}

public static Test suite() {
	return buildModelTestSuite(NamingConventionTests.class);
}
/**
 * Setup for the next test.
 */
public void setUp() throws Exception {
	super.setUp();
	
	project = createJavaProject("P", new String[]{"src"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
}
/**
 * Cleanup after the previous test.
 */
public void tearDown() throws Exception {
	this.deleteProject("P"); //$NON-NLS-1$
	
	super.tearDown();
}
private String toString(char[][] suggestions) {
	if(suggestions == null) {
		return ""; //$NON-NLS-1$
	}
	
	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < suggestions.length; i++) {
		if(i != 0) {
			buffer.append('\n');
		}
		buffer.append(suggestions[i]);
	}
	return buffer.toString();
}
public void testSuggestFieldName001() {
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneName".toCharArray(), //$NON-NLS-1$
		0,
		0,
		CharOperation.NO_CHAR_CHAR);
	
	assertEquals(
		"name\n" + //$NON-NLS-1$
		"oneName", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName002() {
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneClass".toCharArray(), //$NON-NLS-1$
		0,
		0,
		CharOperation.NO_CHAR_CHAR);
	
	assertEquals(
		"class1\n" + //$NON-NLS-1$
		"oneClass", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName003() {
	Hashtable options = JavaScriptCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,"f"); //$NON-NLS-1$
	JavaScriptCore.setOptions(options);
	
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneName".toCharArray(), //$NON-NLS-1$
		0,
		0,
		CharOperation.NO_CHAR_CHAR);
	
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	JavaScriptCore.setOptions(options);
	assertEquals(
		"fName\n" + //$NON-NLS-1$
		"fOneName\n" + //$NON-NLS-1$
		"name\n" + //$NON-NLS-1$
		"oneName", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName004() {
	Hashtable options = JavaScriptCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,"_"); //$NON-NLS-1$
	JavaScriptCore.setOptions(options);
	
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneName".toCharArray(), //$NON-NLS-1$
		0,
		0,
		CharOperation.NO_CHAR_CHAR);
	
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	JavaScriptCore.setOptions(options);
	assertEquals(
		"_name\n" + //$NON-NLS-1$
		"_oneName\n" + //$NON-NLS-1$
		"name\n" + //$NON-NLS-1$
		"oneName", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName005() {
	Hashtable options = JavaScriptCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,"f"); //$NON-NLS-1$
	Object staticFieldPrefixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaScriptCore.CODEASSIST_STATIC_FIELD_PREFIXES,"fg"); //$NON-NLS-1$
	JavaScriptCore.setOptions(options);
	
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneName".toCharArray(), //$NON-NLS-1$
		0,
		Flags.AccStatic,
		CharOperation.NO_CHAR_CHAR);
	
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaScriptCore.CODEASSIST_STATIC_FIELD_PREFIXES,staticFieldPrefixPreviousValue);
	JavaScriptCore.setOptions(options);
	assertEquals(
		"fgName\n" + //$NON-NLS-1$
		"fgOneName\n" + //$NON-NLS-1$
		"name\n" + //$NON-NLS-1$
		"oneName", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName006() {
	Hashtable options = JavaScriptCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaScriptCore.setOptions(options);
	
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneName".toCharArray(), //$NON-NLS-1$
		0,
		0,
		CharOperation.NO_CHAR_CHAR);
	
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaScriptCore.setOptions(options);
	assertEquals(
		"preNamesuf\n" + //$NON-NLS-1$
		"preOneNamesuf\n" + //$NON-NLS-1$
		"preName\n" + //$NON-NLS-1$
		"preOneName\n" + //$NON-NLS-1$
		"namesuf\n" + //$NON-NLS-1$
		"oneNamesuf\n" + //$NON-NLS-1$
		"name\n" + //$NON-NLS-1$
		"oneName", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName007() {
	Hashtable options = JavaScriptCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaScriptCore.setOptions(options);
	
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"int".toCharArray(), //$NON-NLS-1$
		0,
		0,
		CharOperation.NO_CHAR_CHAR);
	
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaScriptCore.setOptions(options);
	assertEquals(
		"preIsuf\n" + //$NON-NLS-1$
		"preI\n" + //$NON-NLS-1$
		"isuf\n" + //$NON-NLS-1$
		"i", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName008() {
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneName".toCharArray(), //$NON-NLS-1$
		0,
		0,
		new char[][]{"name".toCharArray()}); //$NON-NLS-1$
	
	assertEquals(
		"name2\n" + //$NON-NLS-1$
		"oneName", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName009() {
	Hashtable options = JavaScriptCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaScriptCore.setOptions(options);
	
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneName".toCharArray(), //$NON-NLS-1$
		0,
		0,
		new char[][]{"preNamesuf".toCharArray()}); //$NON-NLS-1$
	
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaScriptCore.setOptions(options);
	assertEquals(
		"preName2suf\n" + //$NON-NLS-1$
		"preOneNamesuf\n" + //$NON-NLS-1$
		"preName\n" + //$NON-NLS-1$
		"preOneName\n" + //$NON-NLS-1$
		"namesuf\n" + //$NON-NLS-1$
		"oneNamesuf\n" + //$NON-NLS-1$
		"name\n" + //$NON-NLS-1$
		"oneName", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName010() {
	Hashtable options = JavaScriptCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaScriptCore.setOptions(options);
	
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneName".toCharArray(), //$NON-NLS-1$
		1,
		0,
		new char[][]{"preNamesuf".toCharArray()}); //$NON-NLS-1$
	
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaScriptCore.setOptions(options);
	assertEquals(
		"preNamessuf\n" + //$NON-NLS-1$
		"preOneNamessuf\n" + //$NON-NLS-1$
		"preNames\n" + //$NON-NLS-1$
		"preOneNames\n" + //$NON-NLS-1$
		"namessuf\n" + //$NON-NLS-1$
		"oneNamessuf\n" + //$NON-NLS-1$
		"names\n" + //$NON-NLS-1$
		"oneNames", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName011() {
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"Factory".toCharArray(), //$NON-NLS-1$
		1,
		0,
		CharOperation.NO_CHAR_CHAR); //$NON-NLS-1$
	
	assertEquals(
		"factories", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName012() {
	String[] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c", //$NON-NLS-1$
		"FooBar", //$NON-NLS-1$
		0,
		0,
		new String[]{"bar"}); //$NON-NLS-1$
	
	assertEquals(
		"bar2\n" + //$NON-NLS-1$
		"fooBar", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName013() {
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"java.lang".toCharArray(), //$NON-NLS-1$
		"Class".toCharArray(), //$NON-NLS-1$
		0,
		0,
		CharOperation.NO_CHAR_CHAR);
	
	assertEquals(
		"class1",//$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName014() {
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"java.lang".toCharArray(), //$NON-NLS-1$
		"Class".toCharArray(), //$NON-NLS-1$
		0,
		0,
		new char[][]{"class1".toCharArray()}); //$NON-NLS-1$
	
	assertEquals(
		"class2",//$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName015() {
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"".toCharArray(), //$NON-NLS-1$
		"#".toCharArray(), //$NON-NLS-1$
		0,
		0,
		CharOperation.NO_CHAR_CHAR);
	
	assertEquals(
		"name",//$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName016() {
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"".toCharArray(), //$NON-NLS-1$
		"#".toCharArray(), //$NON-NLS-1$
		0,
		0,
		new char[][]{"name".toCharArray()}); //$NON-NLS-1$
	
	assertEquals(
		"name2",//$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=35356
 */
public void testSuggestFieldName017() {
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"".toCharArray(), //$NON-NLS-1$
		"names".toCharArray(), //$NON-NLS-1$
		0,
		0,
		new char[][]{});
	
	assertEquals(
		"names",//$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=35356
 */
public void testSuggestFieldName018() {
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"".toCharArray(), //$NON-NLS-1$
		"names".toCharArray(), //$NON-NLS-1$
		1,
		0,
		new char[][]{});
	
	assertEquals(
		"names",//$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=35356
 */
public void testSuggestFieldName019() {
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"".toCharArray(), //$NON-NLS-1$
		"MyClass".toCharArray(), //$NON-NLS-1$
		0,
		0,
		new char[][]{});
	
	assertEquals(
		"class1\n" + //$NON-NLS-1$
		"myClass", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=35356
 */
public void testSuggestFieldName020() {
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"".toCharArray(), //$NON-NLS-1$
		"MyClass".toCharArray(), //$NON-NLS-1$
		1,
		0,
		new char[][]{});
	
	assertEquals(
		"classes\n" + //$NON-NLS-1$
		"myClasses", //$NON-NLS-1$
		toString(suggestions));
}
public void testRemovePrefixAndSuffixForFieldName001() {
	Hashtable options = JavaScriptCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaScriptCore.setOptions(options);
	
	char[] name = NamingConventions.removePrefixAndSuffixForFieldName(
		project,
		"preOneNamesuf".toCharArray(), //$NON-NLS-1$
		0);
	
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaScriptCore.setOptions(options);
	assertEquals(
		"oneName", //$NON-NLS-1$
		new String(name));
}
public void testRemovePrefixAndSuffixForFieldName002() {
	Hashtable options = JavaScriptCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,"pr, pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaScriptCore.setOptions(options);
	
	char[] name = NamingConventions.removePrefixAndSuffixForFieldName(
		project,
		"preOneNamesuf".toCharArray(), //$NON-NLS-1$
		Flags.AccStatic);
	
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaScriptCore.setOptions(options);
	assertEquals(
		"preOneNamesuf", //$NON-NLS-1$
		new String(name));
}
public void testRemovePrefixAndSuffixForFieldName003() {
	Hashtable options = JavaScriptCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,"pr, pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaScriptCore.setOptions(options);
	
	char[] name = NamingConventions.removePrefixAndSuffixForFieldName(
		project,
		"preOneNamesuf".toCharArray(), //$NON-NLS-1$
		0);
	
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaScriptCore.setOptions(options);
	assertEquals(
		"oneName", //$NON-NLS-1$
		new String(name));
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=114086
public void testRemovePrefixAndSuffixForFieldName004() {
	Hashtable options = JavaScriptCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,"pre,"); //$NON-NLS-1$
	JavaScriptCore.setOptions(options);
	
	char[] name = NamingConventions.removePrefixAndSuffixForFieldName(
		project,
		"preOneName".toCharArray(), //$NON-NLS-1$
		0);
	
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	JavaScriptCore.setOptions(options);
	assertEquals(
		"oneName", //$NON-NLS-1$
		new String(name));
}
public void testRemovePrefixAndSuffixForLocalName001() {
	Hashtable options = JavaScriptCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaScriptCore.CODEASSIST_LOCAL_PREFIXES);
	options.put(JavaScriptCore.CODEASSIST_LOCAL_PREFIXES,"pr, pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaScriptCore.CODEASSIST_LOCAL_SUFFIXES);
	options.put(JavaScriptCore.CODEASSIST_LOCAL_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaScriptCore.setOptions(options);
	
	char[] name = NamingConventions.removePrefixAndSuffixForLocalVariableName(
		project,
		"preOneNamesuf".toCharArray() //$NON-NLS-1$
		);
	
	options.put(JavaScriptCore.CODEASSIST_LOCAL_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaScriptCore.CODEASSIST_LOCAL_SUFFIXES,fieldSuffixPreviousValue);
	JavaScriptCore.setOptions(options);
	assertEquals(
		"oneName", //$NON-NLS-1$
		new String(name));
}
public void testSuggestGetterName001() {
	char[] suggestion = NamingConventions.suggestGetterName(
		project,
		"fieldName".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);
	
	assertEquals(
		"getFieldName", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName002() {
	char[] suggestion = NamingConventions.suggestGetterName(
		project,
		"FieldName".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);
	
	assertEquals(
		"getFieldName", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName003() {
	Hashtable options = JavaScriptCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,"pr, pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaScriptCore.setOptions(options);
	
	char[] suggestion = NamingConventions.suggestGetterName(
		project,
		"preFieldName".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);
	
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaScriptCore.setOptions(options);
	assertEquals(
		"getFieldName", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName004() {
	Hashtable options = JavaScriptCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,"pr, pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaScriptCore.setOptions(options);
	
	char[] suggestion = NamingConventions.suggestGetterName(
		project,
		"preFieldNamesuf".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);
	
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaScriptCore.setOptions(options);
	assertEquals(
		"getFieldName", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName005() {
	Hashtable options = JavaScriptCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,"pr, pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaScriptCore.setOptions(options);
	
	char[] suggestion = NamingConventions.suggestGetterName(
		project,
		"preFieldNamesuf".toCharArray(), //$NON-NLS-1$
		0,
		true,
		CharOperation.NO_CHAR_CHAR);
	
	options.put(JavaScriptCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaScriptCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaScriptCore.setOptions(options);
	assertEquals(
		"isFieldName", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName006() {
	char[] suggestion = NamingConventions.suggestGetterName(
		project,
		"isSomething".toCharArray(), //$NON-NLS-1$
		0,
		true,
		CharOperation.NO_CHAR_CHAR);
	
	assertEquals(
		"isSomething", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName007() {
	char[] suggestion = NamingConventions.suggestGetterName(
		project,
		"isSomething".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);
	
	assertEquals(
		"getIsSomething", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestSetterName001() {
	char[] suggestion = NamingConventions.suggestSetterName(
		project,
		"isSomething".toCharArray(), //$NON-NLS-1$
		0,
		true,
		CharOperation.NO_CHAR_CHAR);
	
	assertEquals(
		"setSomething", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestSetterName002() {
	char[] suggestion = NamingConventions.suggestSetterName(
		project,
		"isSomething".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);
	
	assertEquals(
		"setIsSomething", //$NON-NLS-1$
		new String(suggestion));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=133562
 */
public void testSuggestLocalName001() {
	Map options = this.project.getOptions(true);
	try {
		Map newOptions = new HashMap(options);
		newOptions.put(JavaScriptCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaScriptCore.VERSION_1_5);
		newOptions.put(JavaScriptCore.COMPILER_SOURCE, JavaScriptCore.VERSION_1_5);
		newOptions.put(JavaScriptCore.COMPILER_COMPLIANCE, JavaScriptCore.VERSION_1_5);
		this.project.setOptions(newOptions);

		String[] suggestions = NamingConventions.suggestLocalVariableNames(
			project,
			"",//$NON-NLS-1$
			"Enum",//$NON-NLS-1$
			0,
			new String[]{"o"});
		
		assertEquals(
			"enum1", //$NON-NLS-1$
			toString(suggestions));
	} finally {
		this.project.setOptions(options);
	}
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=133562
 */
public void testSuggestLocalName002() {
	Map options = this.project.getOptions(true);
	try {
		Map newOptions = new HashMap(options);
		newOptions.put(JavaScriptCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaScriptCore.VERSION_1_5);
		newOptions.put(JavaScriptCore.COMPILER_SOURCE, JavaScriptCore.VERSION_1_5);
		newOptions.put(JavaScriptCore.COMPILER_COMPLIANCE, JavaScriptCore.VERSION_1_5);
		this.project.setOptions(newOptions);

		String[] suggestions = NamingConventions.suggestLocalVariableNames(
			project,
			"",//$NON-NLS-1$
			"Enums",//$NON-NLS-1$
			0,
			new String[]{"o"});
		
		assertEquals(
			"enums", //$NON-NLS-1$
			toString(suggestions));
	} finally {
		this.project.setOptions(options);
	}
}
}
