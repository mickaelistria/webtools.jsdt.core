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

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.jsdt.core.*;
import org.eclipse.wst.jsdt.internal.codeassist.RelevanceConstants;

import junit.framework.*;

public abstract class AbstractJavaModelCompletionTests extends AbstractJavaModelTests implements RelevanceConstants {
	public static List COMPLETION_SUITES = null;
	protected static IJavaScriptProject COMPLETION_PROJECT;
	protected class CompletionResult {
		public String proposals;
		public String context;
		public int cursorLocation;
		public int tokenStart;
		public int tokenEnd;
	}
	Hashtable oldOptions;
	IJavaScriptUnit wc = null;
public AbstractJavaModelCompletionTests(String name) {
	super(name);
}
protected void addLibrary(String projectName, String jarName, String sourceZipName, String docZipName, boolean exported) throws JavaScriptModelException {
	IJavaScriptProject javaProject = getJavaProject(projectName);
	IProject project = javaProject.getProject();
	String projectPath = '/' + project.getName() + '/';
	
	IIncludePathAttribute[] extraAttributes;
	if(docZipName == null) {
		extraAttributes = new IIncludePathAttribute[0];
	} else {
		extraAttributes =
			new IIncludePathAttribute[]{
				JavaScriptCore.newIncludepathAttribute(
						IIncludePathAttribute.JSDOC_LOCATION_ATTRIBUTE_NAME,
						"jar:platform:/resource"+projectPath+docZipName+"!/")};
	}
	
	addLibraryEntry(
			javaProject,
			new Path(projectPath + jarName),
			sourceZipName == null ? null : new Path(projectPath + sourceZipName),
			sourceZipName == null ? null : new Path(""),
			null,
			null,
			extraAttributes,
			exported);
} 
protected void removeLibrary(String projectName, String jarName) throws CoreException, IOException {
	IJavaScriptProject javaProject = getJavaProject(projectName);		
	IProject project = javaProject.getProject();
	String projectPath = '/' + project.getName() + '/';
	removeLibraryEntry(javaProject, new Path(projectPath + jarName));
}
public IJavaScriptUnit getWorkingCopy(String path, String source) throws JavaScriptModelException {
	return super.getWorkingCopy(path, source, this.wcOwner, null);
}
protected CompletionResult complete(String path, String source, String completeBehind) throws JavaScriptModelException {
	return this.complete(path, source, false, completeBehind);
}
protected CompletionResult complete(String path, String source, boolean showPositions, String completeBehind) throws JavaScriptModelException {
	return this.complete(path,source,showPositions, completeBehind, null, null);
}
protected CompletionResult complete(String path, String source, boolean showPositions, String completeBehind, String tokenStartBehind, String token) throws JavaScriptModelException {
	this.wc = getWorkingCopy(path, source);

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, showPositions);
	String str = this.wc.getSource();
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	int tokenStart = -1;
	int tokenEnd = -1;
	if(tokenStartBehind != null && token != null) {
		tokenStart = str.lastIndexOf(tokenStartBehind) + tokenStartBehind.length();
		tokenEnd = tokenStart + token.length() - 1;
	}
	this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);
	
	CompletionResult result =  new CompletionResult();
	result.proposals = requestor.getResults();
	result.context = requestor.getContext();
	result.cursorLocation = cursorLocation;
	result.tokenStart = tokenStart;
	result.tokenEnd = tokenEnd;
	return result;
}
protected CompletionResult contextComplete(IJavaScriptUnit cu, int cursorLocation) throws JavaScriptModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, false);
	cu.codeComplete(cursorLocation, requestor, this.wcOwner);
	
	CompletionResult result =  new CompletionResult();
	result.proposals = requestor.getResults();
	result.context = requestor.getContext();
	result.cursorLocation = cursorLocation;
	return result;
}
protected CompletionResult snippetContextComplete(
		IType type,
		String snippet,
		int insertion,
		int cursorLocation,
		boolean isStatic) throws JavaScriptModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, false);
	type.codeComplete(snippet.toCharArray(), insertion, cursorLocation, null, null, null, isStatic, requestor, this.wcOwner);
	
	CompletionResult result =  new CompletionResult();
	result.proposals = requestor.getResults();
	result.context = requestor.getContext();
	result.cursorLocation = cursorLocation;
	return result;
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	this.oldOptions = JavaScriptCore.getOptions();
	waitUntilIndexesReady();
}
protected void setUp() throws Exception {
	super.setUp();
	this.wcOwner = new WorkingCopyOwner(){};
}
public void tearDownSuite() throws Exception {
	JavaScriptCore.setOptions(this.oldOptions);
	this.oldOptions = null;
	if (COMPLETION_SUITES == null) {
		deleteProject("Completion");
	} else {
		COMPLETION_SUITES.remove(getClass());
		if (COMPLETION_SUITES.size() == 0) {
			deleteProject("Completion");
			COMPLETION_SUITES = null;
		}
	}
	super.tearDownSuite();
}
protected void tearDown() throws Exception {
	if(this.wc != null) {
		this.wc.discardWorkingCopy();
		this.wc = null;
	}
	super.tearDown();
}
protected void assertResults(String expected, String actual) {
	try {
		assertEquals(expected, actual);
	} catch(ComparisonFailure c) {
		System.out.println(actual);
		System.out.println();
		throw c;
	}
}
}
