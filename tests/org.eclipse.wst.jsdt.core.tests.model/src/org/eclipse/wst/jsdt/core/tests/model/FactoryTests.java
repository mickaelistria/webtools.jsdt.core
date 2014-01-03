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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.wst.jsdt.core.*;
import junit.framework.Test;
public class FactoryTests extends ModifyingResourceTests {
public FactoryTests(String name) {
	super(name);
}

public static Test suite() {
	return buildModelTestSuite(FactoryTests.class);
}
/**
 * Ensures that a Java model element can be created from a IFile that
 * is a class file.
 */
public void testCreateBinaryToolObject() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {}, new String[] {"lib"});
		IFile file = this.createFile("/P/lib/X.class", "");
		
		IJavaScriptElement object = JavaScriptCore.create(file);
		assertTrue("tooling object not created", object != null);
		assertTrue("class file does not exist", object.exists());
		assertTrue("wrong object created", object instanceof IClassFile);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that a Java model element can be created from a IFile that
 * is a Java file.  Ensures that any two model elements created will share
 * the same project.
 */
public void testCreateCompilationUnits() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"});
		this.createFolder("/P/src/x/y/z");
		IFile fileA = this.createFile(
			"/P/src/x/y/z/A.js", 
			"package x.y.z;\n" +
			"public class A {\n" +
			"}"
		);
		IFile fileB = this.createFile(
			"/P/src/x/y/B.js", 
			"package x.y;\n" +
			"public class B {\n" +
			"}"
		);

		IJavaScriptElement objectA = JavaScriptCore.create(fileA);
		assertTrue("tooling object A not created", objectA != null);
		assertTrue("wrong object A created", objectA instanceof IJavaScriptUnit);
		assertTrue("compilation unit A does not exist", objectA.exists());

		IJavaScriptElement objectB = JavaScriptCore.create(fileB);
		assertTrue("tooling object B not created", objectB != null);
		assertTrue("wrong object B created", objectB instanceof IJavaScriptUnit);
		assertTrue("compilation unit B does not exist", objectB.exists());

		assertEquals("should share project", ((IJavaScriptUnit)objectA).getJavaScriptProject(), ((IJavaScriptUnit)objectB).getJavaScriptProject());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that a Java model element can be created from a IFile that
 * is a Java file. Even if not on the classpath (in this case it should not exist).
 */
public void testCreateCompilationUnitsNotOnClasspath() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"});
		this.createFolder("/P/other/nested");
		IFile fileA = this.createFile("/P/other/A.js", "public class A {}");
		IFile fileB = this.createFile("/P/other/nested/B.js", "public class B {}");
		IFile fileC = this.createFile("/P/C.js", "public class C {}");
		
		IJavaScriptElement objectA = JavaScriptCore.create(fileA);
		assertTrue("tooling object A not created", objectA != null);
		assertTrue("wrong object A created", objectA instanceof IJavaScriptUnit);
		assertTrue("compilation unit A should not exist", !objectA.exists());

		IJavaScriptElement objectB = JavaScriptCore.create(fileB);
		assertTrue("tooling object B not created", objectB != null);
		assertTrue("wrong object B created", objectB instanceof IJavaScriptUnit);
		assertTrue("compilation unit B should not exist", !objectB.exists());

		assertEquals("should share project", ((IJavaScriptUnit)objectA).getJavaScriptProject(), ((IJavaScriptUnit)objectB).getJavaScriptProject());

		IJavaScriptElement objectC = JavaScriptCore.create(fileC);
		assertTrue("tooling object C not created", objectC != null);
		assertTrue("wrong object C created", objectC instanceof IJavaScriptUnit);
		assertTrue("compilation unit C should not exist", !objectC.exists());

		IPackageFragment pkg= (IPackageFragment)objectA.getParent() ;
		IPackageFragmentRoot root= (IPackageFragmentRoot)pkg.getParent();
		assertEquals("pkg should be default", "", pkg.getElementName());
		assertEquals("unexpected parent's folder", this.getFolder("/P/other"), pkg.getResource());
		assertEquals("unexpected root", this.getFolder("/P/other"), root.getResource());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that a Java model element can be created from a IFolder.  
 * Test that no elements are created if there is no classpath.
 * Ensure that the correct Java model element is created based on the
 * classpath.  
 */
public void testCreateFolderToolObjects() throws CoreException {
	try {
		IJavaScriptProject javaProject = this.createJavaProject("P", new String[] {});
		this.createFolder("/P/src/x/y/z");
		
		IFolder src =this.getFolder("/P/src");
		IFolder res = src.getFolder("x");
		IJavaScriptElement object = JavaScriptCore.create(res);
		assertTrue("tooling object 1 should not be created", object == null);
	
		//set a classpath
		IIncludePathEntry[] classpath= new IIncludePathEntry[] {JavaScriptCore.newSourceEntry(src.getFullPath())};
		javaProject.setRawIncludepath(classpath, null);
	
		//test with a class path
		object = JavaScriptCore.create(src);
		assertTrue("tooling object 2 should be created", object != null);
		assertTrue("tooling object 2 should be a IPackageFragmentRoot", object instanceof IPackageFragmentRoot);
		assertEquals("IPackageFragmentRoot 2 name is incorrect", "src", object.getElementName());
		assertTrue("root 'src' does not exist", object.exists());
		
		object = JavaScriptCore.create(res);
		assertTrue("tooling object 3 should be created", object != null);
		assertTrue("tooling object 3 should be a IPackageFragment", object instanceof IPackageFragment);
		assertEquals("IPackageFragment 3 name is incorrect", "x", object.getElementName());
		assertTrue("package 'com' does not exist", object.exists());
	
		IFolder subFolder= res.getFolder("y");
		object= JavaScriptCore.create(subFolder);
		assertTrue("tooling object 'x.y' should be created", object != null);
		assertTrue("tooling object 'x.y' should be a IPackageFragment", object instanceof IPackageFragment);
		assertEquals("IPackageFragment 'x.y' name is incorrect", "x.y", object.getElementName());
		assertTrue("package 'x.y' does not exist", object.exists());
	
		//not on or below the class path
		IFolder bin = this.getFolder("/P/bin");
		object = JavaScriptCore.create(bin);
		assertTrue("tooling object 4 should not be created", object == null);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that the factory correctly handles empty java files 
 */
public void testCreateFromEmptyJavaFile() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"});
		IFile file = this.createFile("/P/src/X.js", "");

		IJavaScriptElement cu = JavaScriptCore.create(file);
		assertTrue("does not handle empty Java files", cu != null);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that the factory correctly handles files without extensions
 */
public void testCreateFromFileWithoutExtension() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"});
		IFile file = this.createFile("/P/src/FileWithoutExtension", "public class X {}");

		IJavaScriptElement cu = JavaScriptCore.create(file);
		assertTrue("invalid file not detected", cu == null);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that factory correctly handles invalid mementos.
 */
public void testCreateFromInvalidMemento()  {
	assertTrue("invalid parameter not detected", JavaScriptCore.create((String) null) == null);
	assertTrue("should return the java model", JavaScriptCore.create("") != null);
}
/**
 * Ensures that a Java model element can be created from a IFile
 * that is a zip or jar.  
 */
public void testCreateJarToolObject() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {}, new String[] {"/P/lib.jar"});
		IFile file = this.createFile("/P/lib.jar", "");

		IJavaScriptElement jar = JavaScriptCore.create(file);
		assertTrue("tooling object not created", jar != null);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that a Java model element can be created from a class folder library that is in the project's output.
 * (regression test for bug 25538 Conflict of classfolder and outputfolder not reported)
*/ 
public void testCreateLibInOutput() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {}, new String[] {"/P/lib"});
		IFolder folder = this.createFolder("/P/lib");

		IJavaScriptElement lib = JavaScriptCore.create(folder);
		assertTrue("tooling object not created", lib != null);
	} finally {
		this.deleteProject("P");
	}
}
}
