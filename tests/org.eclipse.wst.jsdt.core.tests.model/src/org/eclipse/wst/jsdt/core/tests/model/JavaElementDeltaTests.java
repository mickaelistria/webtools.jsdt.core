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

import java.util.ArrayList;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.jsdt.core.ElementChangedEvent;
import org.eclipse.wst.jsdt.core.IIncludePathEntry;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.IElementChangedListener;
import org.eclipse.wst.jsdt.core.IJavaScriptElementDelta;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IPackageFragment;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.internal.core.*;
import org.eclipse.wst.jsdt.core.*;

import junit.framework.Test;

/**
 * These test ensure that modifications in Java projects are correctly reported
 * as IJavaElementDeltas.
 */
public class JavaElementDeltaTests extends ModifyingResourceTests {

	public class DeltaListener implements IElementChangedListener {
		ArrayList deltas;
		int eventType;

		public DeltaListener() {
			DeltaListener.this.deltas = new ArrayList();
			DeltaListener.this.eventType = -1;
		}

		public DeltaListener(int eventType) {
			DeltaListener.this.deltas = new ArrayList();
			DeltaListener.this.eventType = eventType;
		}

		public void elementChanged(ElementChangedEvent event) {
			if (DeltaListener.this.eventType == -1
					|| event.getType() == DeltaListener.this.eventType) {
				DeltaListener.this.deltas.add(event.getDelta());
			}
		}

		public void flush() {
			DeltaListener.this.deltas = new ArrayList();
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0, length = DeltaListener.this.deltas.size(); i < length; i++) {
				IJavaScriptElementDelta delta = (IJavaScriptElementDelta) this.deltas
						.get(i);
				IJavaScriptElementDelta[] children = delta
						.getAffectedChildren();
				if (children.length > 0) {
					for (int j = 0, childrenLength = children.length; j < childrenLength; j++) {
						buffer.append(children[j]);
						if (j != childrenLength - 1) {
							buffer.append("\n");
						}
					}
				} else {
					buffer.append(delta);
				}
				if (i != length - 1) {
					buffer.append("\n\n");
				}
			}
			return buffer.toString();
		}
	}

	public static Test suite() {
		return buildModelTestSuite(JavaElementDeltaTests.class);
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
		// TESTS_PREFIX = "testBug100772_ProjectScope";
		// TESTS_NAMES = new String[] { "testAddInvalidSubfolder" };
		// TESTS_NUMBERS = new int[] { 100772 };
		// TESTS_RANGE = new int[] { 83304, -1 };
	}

	public JavaElementDeltaTests(String name) {
		super(name);
	}

	/**
	 * Ensures that adding a comment to a working copy and commiting it triggers
	 * an empty fine grained delta with the kind set for POST_CHANGE listeners.
	 * (regression test for bug 32937 Kind not set for empty fine-grained delta)
	 */
	public void testAddCommentAndCommit() throws CoreException {
		DeltaListener listener = new DeltaListener(
				ElementChangedEvent.POST_CHANGE);
		IJavaScriptUnit copy = null;
		try {
			createJavaProject("P", new String[] { "" });
			createFile("P/X.js", "function X() {\n" + "}");
			IJavaScriptUnit unit = getCompilationUnit("P", "", "", "X.js");
			copy = unit.getWorkingCopy(null);

			// add comment to working copy
			copy.getBuffer().setContents(
					"function X(){\n" + "  // some comment\n" + "}");

			// commit working copy
			JavaScriptCore.addElementChangedListener(listener,
					ElementChangedEvent.POST_CHANGE);
			copy.commitWorkingCopy(true, null);
			assertEquals(
					"Unexpected delta after committing working copy",
					"P[*]: {CHILDREN}\n"
							+ "	<project root>[*]: {CHILDREN}\n"
							+ "		<default>[*]: {CHILDREN}\n"
							+ "			X.js[*]: {CONTENT | FINE GRAINED | PRIMARY RESOURCE}",
					listener.toString());
		} finally {
			JavaScriptCore.removeElementChangedListener(listener);
			if (copy != null)
				copy.discardWorkingCopy();
			deleteProject("P");
		}
	}

	/*
	 * Add cu in default package test (proj=src=bin).
	 */
	public void testAddCuInDefaultPkg1() throws CoreException {
		try {
			createJavaProject("P", new String[] { "" });
			startDeltas();
			createFile("P/X.js", "function X() {\n" + "}");
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	<project root>[*]: {CHILDREN}\n"
					+ "		<default>[*]: {CHILDREN}\n" + "			X.js[+]: {}");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Add cu in default package test (proj!=src!=bin).
	 */
	public void testAddCuInDefaultPkg2() throws CoreException {
		try {
			createJavaProject("P", new String[] { "src" });
			startDeltas();
			createFile("P/src/X.js", "function X() {\n" + "}");
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	src[*]: {CHILDREN}\n" + "		<default>[*]: {CHILDREN}\n"
					+ "			X.js[+]: {}");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Add cu after opening its project (regression test for 56870 copied file
	 * not shown in package explorer / java browser [ccp])
	 */
	public void testAddCuAfterProjectOpen() throws CoreException {
		try {
			IJavaScriptProject p1 = createJavaProject("P1",
					new String[] { "src" });
			IJavaScriptProject p2 = createJavaProject("P2",
					new String[] { "src" });
			createFile("P2/src/X.js", "function X() {\n" + "}");
			IProject project = p2.getProject();
			project.close(null);

			// invalidate roots
			p1.setRawIncludepath(new IIncludePathEntry[] {}, null);

			// open project
			project.open(null);

			startDeltas();
			createFile("P2/src/Y.js", "function Y() {\n" + "}");
			assertDeltas("Unexpected delta", "P2[*]: {CHILDREN}\n"
					+ "	src[*]: {CHILDREN}\n" + "		<default>[*]: {CHILDREN}\n"
					+ "			Y.js[+]: {}");
		} finally {
			stopDeltas();
			deleteProject("P1");
			deleteProject("P2");
		}
	}

	/*
	 * Add the .classpath file to a Java project that was missing it.
	 * (regression test for bug 26128 packages don't appear in package explorer
	 * view)
	 */
	public void testAddDotClasspathFile() throws CoreException {
		try {
			createProject("P");
			createFolder("/P/src");

			// add Java nature
			editFile(
					"/P/.project",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
							+ "<projectDescription>\n"
							+ "	<name>Test</name>\n"
							+ "	<comment></comment>\n"
							+ "	<projects>\n"
							+ "	</projects>\n"
							+ "	<buildSpec>\n"
							+ "		<buildCommand>\n"
							+ "			<name>org.eclipse.wst.jsdt.core.jsbuilder</name>\n"
							+ "			<arguments>\n"
							+ "			</arguments>\n"
							+ "		</buildCommand>\n"
							+ "	</buildSpec>\n"
							+ "	<natures>\n"
							+ "		<nature>org.eclipse.wst.jsdt.core.jsnature</nature>\n"
							+ "	</natures>\n" + "</projectDescription>");

			startDeltas();
			createFile(
					"P/.classpath",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
							+ "<classpath>\n"
							+ "    <classpathentry kind=\"src\" path=\"src\"/>\n"
							+ "    <classpathentry kind=\"output\" path=\"bin\"/>\n"
							+ "</classpath>");
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	<project root>[*]: {REMOVED FROM CLASSPATH}\n"
					+ "	src[*]: {ADDED TO CLASSPATH}\n"
					+ "	ResourceDelta(/P/.classpath)[+]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Ensure that a resource delta is fired when a file is added to a non-java
	 * project. (regression test for bug 18698 Seeing non-java projects in
	 * package view)
	 */
	public void testAddFileToNonJavaProject() throws CoreException {
		try {
			createProject("P");
			startDeltas();
			createFile("/P/toto.txt", "");
			assertDeltas("Unexpected delta", "ResourceDelta(/P)");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Ensure that adding a folder in a non-Java folder (i.e. a folder with an
	 * invalid package name) reports the correct delta (regression test for bug
	 * 130982 META-INF directories shown as empty META-INF.* packages in J2EE
	 * Navigator)
	 */
	public void testAddFolderInNonJavaFolder() throws CoreException {
		try {
			createJavaProject("P");
			createFolder("/P/META-INF");
			startDeltas();
			createFolder("/P/META-INF/folder");
			assertDeltas("Unexpected delta", "P[*]: {CONTENT}\n"
					+ "	ResourceDelta(/P/META-INF)[*]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Ensure that a resource delta is fired when a .name folder is added to a
	 * java project where prj=src. (regression test for bug 31383 Strange
	 * rendering of of link resources when link points to Eclipse workspace)
	 */
	public void testAddInvalidSubfolder() throws CoreException {
		try {
			createJavaProject("P");
			startDeltas();
			createFolder("/P/p/.folder");
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	<project root>[*]: {CHILDREN}\n" + "		p[+]: {}");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Add the java nature to an existing project.
	 */
	public void testAddJavaNature() throws CoreException {
		try {
			createProject("P");
			startDeltas();
			addJavaNature("P");
			assertDeltas("Unexpected delta", "P[+]: {}\n" + "ResourceDelta(/P)");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Add the java nature to an existing project and set the classpath in an
	 * IWorkspaceRunnable. Ensures that adding a non-java resource reports the
	 * correct delta. (regression test for bug 44066 Package Explorer doesn't
	 * show new file)
	 */
	public void testAddJavaNatureAndClasspath() throws CoreException {
		try {
			createProject("P");
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					addJavaNature("P");
					createFolder("/P/src");
					getJavaProject("P").setRawIncludepath(
							new IIncludePathEntry[] { JavaScriptCore
									.newSourceEntry(new Path("/P/src")) },
							null);
				}
			}, null);
			startDeltas();
			createFile("/P/src/file.txt", "");
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	src[*]: {CONTENT}\n"
					+ "		ResourceDelta(/P/src/file.txt)[+]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Add a java project.
	 */
	public void testAddJavaProject() throws CoreException {
		try {
			startDeltas();
			createJavaProject("P", new String[] { "" });
			assertDeltas("Unexpected delta", "P[+]: {}");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Add a non-java project.
	 */
	public void testAddNonJavaProject() throws CoreException {
		try {
			startDeltas();
			createProject("P");
			assertDeltas("Should get a non-Java resource delta",
					"ResourceDelta(/P)");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	public void testAddPackageSourceIsBin() throws CoreException {
		try {
			createJavaProject("P", new String[] { "src" });
			startDeltas();
			createFolder("P/src/x");
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	src[*]: {CHILDREN}\n" + "		x[+]: {}");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Add 2 java projects in an IWorkspaceRunnable.
	 */
	public void testAddTwoJavaProjects() throws CoreException {
		try {
			startDeltas();
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					createJavaProject("P1", new String[] { "" });
					createJavaProject("P2", new String[] { "src" });
				}
			}, null);
			assertEquals("Unexpected delta", "P1[+]: {}\n" + "P2[+]: {}",
					getSortedByProjectDeltas());
		} finally {
			stopDeltas();
			deleteProject("P1");
			deleteProject("P2");
		}
	}

	/*
	 * Add 2 java projects in an IWorkspaceRunnable.
	 */
	public void testAddTwoJavaProjectsWithExtraSetClasspath()
			throws CoreException {
		try {
			startDeltas();
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					IJavaScriptProject p1 = createJavaProject("P1",
							new String[] { "" });
					// should be a no-op and no extra delta volley should be
					// fired
					p1.setRawIncludepath(p1.getRawIncludepath(), null);
					createJavaProject("P2", new String[] { "src" });
				}
			}, null);
			assertEquals("Unexpected delta", "P1[+]: {}\n" + "P2[+]: {}",
					getSortedByProjectDeltas());
		} finally {
			stopDeltas();
			deleteProject("P1");
			deleteProject("P2");
		}
	}

	/*
	 * Batch operation test.
	 */
	public void testBatchOperation() throws CoreException {
		try {
			createJavaProject("P", new String[] { "src" });
			createFolder("P/src/x");
			createFile("P/src/x/A.js", "function A() {\n" + "}");
			startDeltas();
			JavaScriptCore.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					IJavaScriptUnit unit = getCompilationUnit("P/src/x/A.js");
					// unit.createType("class B {}", null, false, monitor);
					unit.createField("var i;", null, false, monitor);
				}
			}, null);
			assertDeltas(
					"Unexpected delta",
					"P[*]: {CHILDREN}\n"
							+ "	src[*]: {CHILDREN}\n"
							+ "		x[*]: {CHILDREN}\n"
							+ "			A.js[*]: {CHILDREN | FINE GRAINED | PRIMARY RESOURCE}\n"
							+ "				i[+]: {}");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Ensures that if a project's output folder is used as a lib folder in
	 * another project, building the first project results in the correct delta
	 * in the other project.
	 */
	public void testBuildProjectUsedAsLib() throws CoreException {
		try {
			IJavaScriptProject p1 = createJavaProject("P1",
					new String[] { "src1" }, new String[] { "JCL_LIB" });
			createJavaProject("P2", new String[] { "src2" },
					new String[] { "/P1/bin1" });
			createFile("/P1/src1/X.js", "function X() {\n" + "}");

			// force opening of project to avoid external jar delta
			p1.open(null);

			startDeltas();

			// full build
			p1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			waitForAutoBuild();
			assertDeltas("Unexpected delta (1)", "P2[*]: {CHILDREN}\n"
					+ "	/P1/bin1[*]: {CHILDREN}\n"
					+ "		<default>[*]: {CHILDREN}\n" + "			X.class[+]: {}");

			editFile("/P1/src1/X.js", "function X() {\n" +
			// "  void foo() {}\n" +
					"}\n" + "function Y() {\n" + "}");
			clearDeltas();

			// incremental buid
			p1.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
					null);
			waitForAutoBuild();
			assertDeltas("Unexpected delta (2)", "P2[*]: {CHILDREN}\n"
					+ "	/P1/bin1[*]: {CHILDREN}\n"
					+ "		<default>[*]: {CHILDREN}\n"
					+ "			X.class[*]: {CONTENT}\n" + "			Y.class[+]: {}");
		} finally {
			stopDeltas();
			deleteProject("P1");
			deleteProject("P2");
		}
	}

	/**
	 * Ensures that changing the custom output folder of a source entry triggers
	 * a F_REMOVED_FROM_CLASSPATH and F_ADDED_TO_CLASSPATH delta.
	 */
	public void testChangeCustomOutput() throws CoreException {
		try {
			IJavaScriptProject proj = createJavaProject("P",
					new String[] { "src" }, "bin", new String[] { "bin1" });
			startDeltas();
			setClasspath(proj, new IIncludePathEntry[] { JavaScriptCore
					.newSourceEntry(new Path("/P/src"), new IPath[0], new Path(
							"/P/bin2")) });
			assertDeltas(
					"Unexpected delta",
					"P[*]: {CHILDREN | CLASSPATH CHANGED}\n"
							+ "	src[*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n"
							+ "	ResourceDelta(/P/.classpath)[*]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	//
	// /*
	// * Ensures that the setting the classpath where the only change is the
	// export flag
	// * triggers a F_CLASSPATH CHANGED delta.
	// * (regression test for bug 75517 No classpath delta for project becoming
	// exported)
	// */
	// public void testChangeExportFlag() throws CoreException {
	// try {
	// createJavaProject("P1");
	// IJavaScriptProject proj = createJavaProject("P2", new String[] {}, new
	// String[] {}, new String[] {"/P1"}, new boolean[] {false}, "bin");
	// startDeltas();
	// setClasspath(
	// proj,
	// new IIncludePathEntry[] {
	// JavaScriptCore.newProjectEntry(new Path("/P1"), true)
	// });
	// assertDeltas(
	// "Unexpected delta",
	// "P2[*]: {CONTENT | CLASSPATH CHANGED}\n" +
	// "	ResourceDelta(/P2/.classpath)[*]"
	// );
	// } finally {
	// stopDeltas();
	// deleteProject("P1");
	// deleteProject("P2");
	// }
	// }

	/**
	 * Ensures that the setting the classpath with a library entry triggers a
	 * F_REMOVED_FROM_CLASSPATH and F_ADDED_TO_CLASSPATH delta.
	 */
	public void testChangeRootKind() throws CoreException {
		try {
			IJavaScriptProject proj = createJavaProject("P",
					new String[] { "src" });
			startDeltas();
			setClasspath(proj, new IIncludePathEntry[] { JavaScriptCore
					.newLibraryEntry(new Path("/P/src"), null, null, false) });
			assertDeltas(
					"Unexpected delta",
					"P[*]: {CHILDREN | CLASSPATH CHANGED}\n"
							+ "	src[*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n"
							+ "	ResourceDelta(/P/.classpath)[*]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Close a java project.
	 */
	public void testCloseJavaProject() throws CoreException {
		try {
			createJavaProject("P", new String[] { "" });
			IProject project = getProject("P");
			startDeltas();
			project.close(null);
			assertDeltas("Unexpected delta", "P[*]: {CLOSED}\n"
					+ "ResourceDelta(/P)");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Close a non-java project.
	 */
	public void testCloseNonJavaProject() throws CoreException {
		try {
			createProject("P");
			IProject project = getProject("P");
			startDeltas();
			project.close(null);
			assertDeltas("Unexpected delta", "ResourceDelta(/P)");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	// /*
	// * Closing a non-java project that contains a jar referenced in another
	// project should produce
	// * a delta on this other project.
	// * (regression test for bug 19058 Closing non-java project doesn't remove
	// root from java project)
	// */
	// public void testCloseNonJavaProjectUpdateDependent() throws CoreException
	// {
	// try {
	// createProject("SP");
	// createFile("/SP/x.jar", "");
	// createJavaProject("JP", new String[] {""}, new String[] {"/SP/x.jar"},
	// "");
	// IProject project = getProject("SP");
	// startDeltas();
	// project.close(null);
	// assertDeltas(
	// "Unexpected delta",
	// "JP[*]: {CHILDREN}\n" +
	// "	/SP/x.jar[-]: {}\n" +
	// "ResourceDelta(/SP)"
	// );
	// } finally {
	// stopDeltas();
	// deleteProject("SP");
	// deleteProject("JP");
	// }
	// }
	/**
	 * Test that deltas are generated when a compilation unit is added and
	 * removed from a package via core API.
	 */
	public void testCompilationUnitRemoveAndAdd() throws CoreException {
		try {
			createJavaProject("P");
			// createFolder("/P/p");
			IFile file = createFile("/P/X.js", "function X() {\n" + "}");

			// delete cu
			startDeltas();
			deleteResource(file);
			assertDeltas("Unexpected delta after deleting /P/X.js",
					"P[*]: {CHILDREN}\n" + "	<project root>[*]: {CHILDREN}\n"
							+ "		<default>[*]: {CHILDREN}\n" + "			X.js[-]: {}");

			// add cu
			clearDeltas();
			createFile("/P/X.js", "function X() {\n" + "}");
			assertDeltas("Unexpected delta after adding /P/p/X.js",
					"P[*]: {CHILDREN}\n" + "	<project root>[*]: {CHILDREN}\n"
							+ "		p[*]: {CHILDREN}\n" + "			X.js[+]: {}");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	public void testCreateSharedWorkingCopy() throws CoreException {
		IJavaScriptUnit copy = null;
		try {
			createJavaProject("P", new String[] { "" });
			createFile("P/X.js", "function X() {\n" + "}");
			IJavaScriptUnit unit = getCompilationUnit("P", "", "", "X.js");
			startDeltas();
			copy = unit.getWorkingCopy(new WorkingCopyOwner() {
			}, null);
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	<project root>[*]: {CHILDREN}\n"
					+ "		<default>[*]: {CHILDREN}\n"
					+ "			[Working copy] X.js[+]: {}");
		} finally {
			stopDeltas();
			if (copy != null)
				copy.discardWorkingCopy();
			deleteProject("P");
		}
	}

	public void testCreateWorkingCopy() throws CoreException {
		IJavaScriptUnit copy = null;
		try {
			createJavaProject("P", new String[] { "" });
			createFile("P/X.js", "function X() {\n" + "}");
			IJavaScriptUnit unit = getCompilationUnit("P", "", "", "X.js");
			startDeltas();
			copy = unit.getWorkingCopy(null);
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	<project root>[*]: {CHILDREN}\n"
					+ "		<default>[*]: {CHILDREN}\n"
					+ "			[Working copy] X.js[+]: {}");
		} finally {
			stopDeltas();
			if (copy != null)
				copy.discardWorkingCopy();
			deleteProject("P");
		}
	}

	/*
	 * Ensure that the delta is correct if a package is copied on top of an
	 * existing package. (regression test for bug 61270 Wrong delta when copying
	 * a package that overrides another package)
	 */
	public void testCopyAndOverwritePackage() throws CoreException {
		try {
			createJavaProject("P", new String[] { "src1", "src2" });
			// createFolder("/P/src1/p");
			createFile("P/src1/X.js", "function X() {\n" + "}");
			createFolder("/P/src2");
			startDeltas();
			getPackage("/P/src1").copy(getPackageFragmentRoot("/P/src2"),
					null/* no sibling */, null/* no rename */, true/* replace */,
					null/* no progress */);
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	src2[*]: {CHILDREN}\n" + "		p[*]: {CHILDREN}\n"
					+ "			X.js[+]: {}");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/**
	 * Test that deltas are generated when a compilation unit not on the
	 * classpath is added and removed from a package via core API.
	 */
	public void testCUNotOnClasspath() throws CoreException {
		try {
			createJavaProject("P", new String[] {});
			// createFolder("/P/src/p");
			IFile file = createFile("/P/src/p/X.js", "function X() {\n" + "}");

			startDeltas();
			deleteResource(file);
			assertDeltas("Unexpected delta after deletion of /P/src/X.js",
					"P[*]: {CONTENT}\n" + "	ResourceDelta(/P/src)[*]");

			clearDeltas();
			createFile("/P/src/X.js", "function X() {\n" + "}");
			assertDeltas("Unexpected delta after addition of /P/src/X.js",
					"P[*]: {CONTENT}\n" + "	ResourceDelta(/P/src)[*]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	// /*
	// * Ensure that deleting a jar that is in a folder and that is on the
	// classpath reports
	// * a removed pkg fragment root delta.
	// * (regression test for bug 27068 Elements in the Package Explorer are
	// displayed but don't more exist [package explorer])
	// *
	// */
	// public void testDeleteInnerJar() throws CoreException {
	// try {
	// createJavaProject("P", new String[] {"src"}, new String[]
	// {"/P/lib/x.jar"}, "bin");
	// createFolder("/P/lib");
	// IFile file = createFile("/P/lib/x.jar", "");
	// startDeltas();
	// deleteResource(file);
	// assertDeltas(
	// "Unexpected deltas",
	// "P[*]: {CHILDREN | CONTENT}\n" +
	// "	lib/x.jar[-]: {}\n" +
	// "	ResourceDelta(/P/lib)[*]"
	// );
	// } finally {
	// stopDeltas();
	// deleteProject("P");
	// }
	// }
	/*
	 * Ensure that deleting a non-Java folder that contains a source root folder
	 * reports a removed root delta as well as a resource delta for the removed
	 * folder. (regression test for bug 24045 Error deleting parent folder of
	 * source folder)
	 */
	public void testDeleteNonJavaFolder() throws CoreException {
		try {
			createJavaProject("P", new String[] { "foo/bar" });
			IFolder folder = getFolder("/P/foo");
			startDeltas();
			deleteResource(folder);
			assertDeltas("Unexpected deltas", "P[*]: {CHILDREN | CONTENT}\n"
					+ "	foo/bar[-]: {}\n" + "	ResourceDelta(/P/foo)[-]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Ensures that changing a project's classpath and deleting it in an
	 * IWorkspaceRunnable doesn't throw an NPE (regression test for bug 148015
	 * NPE in log from ClasspathChange)
	 */
	public void testDeleteProjectAfterChangingClasspath() throws CoreException {
		try {
			final IJavaScriptProject project = createJavaProject("P");
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					project.setRawIncludepath(createClasspath("P",
							new String[] { "/P/src", "" }),
							monitor);
					deleteProject("P");
				}
			};
			startDeltas();
			getWorkspace().run(runnable, null/* no progress */);
			assertDeltas("Unexpected delta", "P[-]: {}");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}/*
	 * Ensure that deleting a project and setting the classpath on another
	 * project in an IWorkspaceRunnable doesn't throw a NullPointerException
	 * (regression test for bug 25197 NPE importing external plugins)
	 */

	public void testDeleteProjectSetCPAnotherProject() throws CoreException {
		final IJavaScriptProject project = createJavaProject("P1",
				new String[] { "src" });
		createJavaProject("P2", new String[] {});

		try {
			startDeltas();
			JavaScriptCore.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					deleteProject("P2");
					project.setRawIncludepath(
							new IIncludePathEntry[] { JavaScriptCore
									.newSourceEntry(project.getPath()) }, null);
				}
			}, null);
			assertDeltas("Unexpected deltas",
					"P1[*]: {CHILDREN | CLASSPATH CHANGED}\n"
							+ "	<project root>[*]: {ADDED TO CLASSPATH}\n"
							+ "	src[*]: {REMOVED FROM CLASSPATH}\n"
							+ "	ResourceDelta(/P1/.classpath)[*]\n"
							+ "P2[-]: {}");
		} finally {
			stopDeltas();
			deleteProject("P1");
			deleteProject("P2");
		}
	}

	public void testDiscardWorkingCopy1() throws CoreException { // renamed from
																	// testDestroyWorkingCopy
		IJavaScriptUnit copy = null;
		try {
			createJavaProject("P", new String[] { "" });
			createFile("P/X.js", "public class X {\n" + "}");
			IJavaScriptUnit unit = getCompilationUnit("P", "", "", "X.js");
			copy = unit.getWorkingCopy(null);
			startDeltas();
			copy.discardWorkingCopy();
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	<project root>[*]: {CHILDREN}\n"
					+ "		<default>[*]: {CHILDREN}\n"
					+ "			[Working copy] X.js[-]: {}");
		} finally {
			stopDeltas();
			if (copy != null)
				copy.discardWorkingCopy();
			deleteProject("P");
		}
	}

	public void testDiscardWorkingCopy2() throws CoreException { // renamed from
																	// testDestroySharedWorkingCopy
		IJavaScriptUnit copy = null;
		try {
			createJavaProject("P", new String[] { "" });
			createFile("P/X.js", "public class X {\n" + "}");
			IJavaScriptUnit unit = getCompilationUnit("P", "", "", "X.js");
			copy = unit.getWorkingCopy(new WorkingCopyOwner() {
			}, null);
			startDeltas();
			copy.discardWorkingCopy();
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	<project root>[*]: {CHILDREN}\n"
					+ "		<default>[*]: {CHILDREN}\n"
					+ "			[Working copy] X.js[-]: {}");
		} finally {
			stopDeltas();
			if (copy != null)
				copy.discardWorkingCopy();
			deleteProject("P");
		}
	}

	/*
	 * Ensures that a delta listener that asks for POST_CHANGE events gets those
	 * events and no others.
	 */
	public void testListenerPostChange() throws CoreException {
		DeltaListener listener = new DeltaListener(
				ElementChangedEvent.POST_CHANGE);
		IJavaScriptUnit wc = null;
		try {
			createJavaProject("P", new String[] { "" });
			JavaScriptCore.addElementChangedListener(listener,
					ElementChangedEvent.POST_CHANGE);

			// cu creation
			IPackageFragment pkg = getPackage("P");
			IJavaScriptUnit cu = pkg.createCompilationUnit("X.js",
					"public class X {\n" + "}", false, null);
			assertEquals(
					"Unexpected delta after creating CU",
					"P[*]: {CHILDREN}\n" + "	<project root>[*]: {CHILDREN}\n"
							+ "		<default>[*]: {CHILDREN}\n" + "			X.js[+]: {}",
					listener.toString());
			listener.flush();

			// type creation
			cu.createType("class A {\n" + "}", cu.getType("X"), false, null);
			assertEquals(
					"Unexpected delta after creating type",
					"P[*]: {CHILDREN}\n"
							+ "	<project root>[*]: {CHILDREN}\n"
							+ "		<default>[*]: {CHILDREN}\n"
							+ "			X.js[*]: {CHILDREN | FINE GRAINED | PRIMARY RESOURCE}\n"
							+ "				A[+]: {}", listener.toString());
			listener.flush();

			// non-java resource creation
			createFile("P/readme.txt", "");
			assertEquals("Unexpected delta after creating non-java resource",
					"P[*]: {CONTENT}\n" + "	ResourceDelta(/P/readme.txt)[+]",
					listener.toString());
			listener.flush();

			// shared working copy creation
			wc = cu.getWorkingCopy(new WorkingCopyOwner() {
			}, null);
			assertEquals("Unexpected delta after creating shared working copy",
					"P[*]: {CHILDREN}\n" + "	<project root>[*]: {CHILDREN}\n"
							+ "		<default>[*]: {CHILDREN}\n"
							+ "			[Working copy] X.js[+]: {}", listener
							.toString());
			listener.flush();

			// reconcile
			wc.getBuffer().setContents(
					"public class X {\n" + "  public void foo() {\n" + "  }\n"
							+ "}");
			wc.reconcile(IJavaScriptUnit.NO_AST, false, null, null);
			assertEquals("Unexpected delta after reconciling working copy", "",
					listener.toString());
			listener.flush();

			// commit
			wc.commitWorkingCopy(false, null);
			assertEquals(
					"Unexpected delta after committing working copy",
					"P[*]: {CHILDREN}\n"
							+ "	<project root>[*]: {CHILDREN}\n"
							+ "		<default>[*]: {CHILDREN}\n"
							+ "			X.js[*]: {CHILDREN | FINE GRAINED | PRIMARY RESOURCE}\n"
							+ "				X[*]: {CHILDREN | FINE GRAINED}\n"
							+ "					foo()[+]: {}\n" + "				A[-]: {}", listener
							.toString());
			listener.flush();

			// shared working copy destruction
			wc.discardWorkingCopy();
			assertEquals(
					"Unexpected delta after destroying shared working copy",
					"P[*]: {CHILDREN}\n" + "	<project root>[*]: {CHILDREN}\n"
							+ "		<default>[*]: {CHILDREN}\n"
							+ "			[Working copy] X.js[-]: {}", listener
							.toString());
			listener.flush();
			wc = null;

		} finally {
			if (wc != null)
				wc.discardWorkingCopy();
			JavaScriptCore.removeElementChangedListener(listener);
			deleteProject("P");
		}
	}

	/*
	 * Ensures that a delta listener that asks for POST_RECONCILE events gets
	 * those events and no others.
	 */
	public void testListenerReconcile() throws CoreException {
		DeltaListener listener = new DeltaListener(
				ElementChangedEvent.POST_RECONCILE);
		IJavaScriptUnit wc = null;
		try {
			createJavaProject("P", new String[] { "" });
			JavaScriptCore.addElementChangedListener(listener,
					ElementChangedEvent.POST_RECONCILE);

			// cu creation
			IPackageFragment pkg = getPackage("P");
			IJavaScriptUnit cu = pkg.createCompilationUnit("X.js",
					"public class X {\n" + "}", false, null);
			assertEquals("Unexpected delta after creating CU", "", listener
					.toString());
			listener.flush();

			// type creation
			cu.createType("class A {\n" + "}", cu.getType("X"), false, null);
			assertEquals("Unexpected delta after creating type", "", listener
					.toString());
			listener.flush();

			// non-java resource creation
			createFile("P/readme.txt", "");
			assertEquals("Unexpected delta after creating non-java resource",
					"", listener.toString());
			listener.flush();

			// shared working copy creation
			wc = cu.getWorkingCopy(new WorkingCopyOwner() {
			}, null);
			assertEquals("Unexpected delta after creating shared working copy",
					"", listener.toString());
			listener.flush();

			// reconcile
			wc.getBuffer().setContents(
					"public class X {\n" + "  public void foo() {\n" + "  }\n"
							+ "}");
			wc.reconcile(IJavaScriptUnit.NO_AST, false, null, null);
			assertEquals("Unexpected delta after reconciling working copy",
					"X[*]: {CHILDREN | FINE GRAINED}\n" + "	foo()[+]: {}\n"
							+ "A[-]: {}", listener.toString());
			listener.flush();

			// commit
			wc.commitWorkingCopy(false, null);
			assertEquals("Unexpected delta after committing working copy", "",
					listener.toString());
			listener.flush();

			// shared working copy destruction
			wc.discardWorkingCopy();
			assertEquals(
					"Unexpected delta after destroying shared working copy",
					"", listener.toString());
			listener.flush();
			wc = null;

		} finally {
			if (wc != null)
				wc.discardWorkingCopy();
			JavaScriptCore.removeElementChangedListener(listener);
			deleteProject("P");
		}
	}

	/**
	 * Ensures that merging a java delta with another one that contains a
	 * resource delta results in a java delta with the resource delta.
	 * (regression test for 11210 ResourceDeltas are lost when merging deltas)
	 */
	public void testMergeResourceDeltas() throws CoreException {
		try {
			createJavaProject("P", new String[] { "" });
			startDeltas();
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					// an operation that creates a java delta without firing it
					IPackageFragment pkg = getPackageFragment("P", "", "");
					pkg.createCompilationUnit("X.js", "public class X {\n"
							+ "}", true, null);

					// an operation that generates a non java resource delta
					createFile("P/Y.txt", "");
				}
			}, null);
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	<project root>[*]: {CHILDREN}\n"
					+ "		<default>[*]: {CHILDREN}\n" + "			X.js[+]: {}\n"
					+ "	ResourceDelta(/P/Y.txt)[+]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}

	}

	public void testModifyMethodBodyAndSave() throws CoreException {
		IJavaScriptUnit workingCopy = null;
		try {
			createJavaProject("P", new String[] { "" });
			createFolder("P/x/y");
			createFile("P/x/y/A.js", "package x.y;\n" + "public class A {\n"
					+ "  public void foo() {\n" + "  }\n" + "}");
			IJavaScriptUnit cu = getCompilationUnit("P/x/y/A.js");
			workingCopy = cu.getWorkingCopy(null);
			workingCopy.getBuffer().setContents(
					"package x.y;\n" + "public class A {\n"
							+ "  public void foo() {\n"
							+ "    // method body change \n" + "  }\n" + "}");

			startDeltas();
			workingCopy.commitWorkingCopy(true, null);
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	<project root>[*]: {CHILDREN}\n"
					+ "		x.y[*]: {CHILDREN}\n"
					+ "			A.js[*]: {CONTENT | FINE GRAINED | PRIMARY RESOURCE}");
		} finally {
			stopDeltas();
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
			deleteProject("P");
		}
	}

	/*
	 * Ensures that modifying the project output location (i.e. simulate a
	 * build) doesn't report any delta.
	 */
	public void testModifyOutputLocation1() throws CoreException {
		try {
			createJavaProject("P", new String[] { "src" });

			startDeltas();
			createFile("/P/bin/X.class", "");
			assertDeltas("Unexpected delta", "");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Ensures that modifying a custom output location (i.e. simulate a build)
	 * doesn't report any delta. (regression test for bug 27494 Source folder
	 * output folder shown in Package explorer)
	 */
	public void testModifyOutputLocation2() throws CoreException {
		try {
			createJavaProject("P", new String[] { "src" }, "bin1",
					new String[] { "bin2" });

			startDeltas();
			createFile("/P/bin2/X.class", "");
			assertDeltas("Unexpected delta", "");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Ensures that modifying a custom output location (i.e. simulate a build)
	 * doesn't report any delta. (regression test for bug 27494 Source folder
	 * output folder shown in Package explorer)
	 */
	public void testModifyOutputLocation3() throws CoreException {
		try {
			createJavaProject("P", new String[] { "src1", "src2" }, "bin",
					new String[] { "src1", null });

			startDeltas();
			createFile("/P/bin/X.class", "");
			assertDeltas("Unexpected delta", "");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Ensures that modifying a custom output location (i.e. simulate a build)
	 * doesn't report any delta. (regression test for bug 32629 DeltaProcessor
	 * walking some binary output)
	 */
	public void testModifyOutputLocation4() throws CoreException {
		try {
			createJavaProject("P");
			editFile(
					"/P/.classpath",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
							+ "<classpath>\n"
							+ "    <classpathentry excluding=\"src/\" kind=\"src\" output=\"bin1\" path=\"\"/>\n"
							+ "    <classpathentry kind=\"src\" output=\"bin2\" path=\"src\"/>\n"
							+ "    <classpathentry kind=\"output\" path=\"bin\"/>\n"
							+ "</classpath>");
			createFolder("/P/bin2");

			startDeltas();
			createFile("/P/bin2/X.class", "");
			assertDeltas("Unexpected delta", "");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/**
	 * bug 18953
	 */
	public void testModifyProjectDescriptionAndRemoveFolder()
			throws CoreException {
		try {
			IJavaScriptProject project = createJavaProject("P");
			final IProject projectFolder = project.getProject();
			final IFolder folder = createFolder("/P/folder");

			startDeltas();
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					IProjectDescription desc = projectFolder.getDescription();
					desc.setComment("A comment");
					projectFolder.setDescription(desc, null);
					deleteResource(folder);
				}
			}, null);

			assertDeltas("Unexpected delta", "P[*]: {CHILDREN | CONTENT}\n"
					+ "	<project root>[*]: {CHILDREN}\n" + "		folder[-]: {}\n"
					+ "	ResourceDelta(/P/.project)[*]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Move a cu from a package to its enclosing package. (regression test for
	 * bug 7033 Stale packages view after moving compilation units)
	 */
	public void testMoveCuInEnclosingPkg() throws CoreException {
		try {
			createJavaProject("P", new String[] { "" });
			createFolder("P/x/y");
			createFile("P/x/y/A.js", "package x.y;\n" + "public class A {\n"
					+ "}");
			IJavaScriptUnit cu = getCompilationUnit("P/x/y/A.js");
			IPackageFragment pkg = getPackage("P/x");

			startDeltas();
			cu.move(pkg, null, null, true, null);
			assertDeltas(
					"Unexpected delta",
					"P[*]: {CHILDREN}\n"
							+ "	<project root>[*]: {CHILDREN}\n"
							+ "		x[*]: {CHILDREN}\n"
							+ "			A.js[+]: {MOVED_FROM(A.js [in x.y [in <project root> [in P]]])}"
							+ "		x.y[*]: {CHILDREN}\n"
							+ "			A.js[-]: {MOVED_TO(A.js [in x [in <project root> [in P]]])}\n");
			assertElementDescendants("Unexpected children for package x", "x\n"
					+ "  A.js\n" + "    package x\n" + "    class A", pkg);
			assertElementDescendants("Unexpected children for package x.y",
					"x.y", getPackage("P/x/y"));
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Move a non-java resources that is under a dot-named folder. (regression
	 * test for bug 6687 Wrong JavaModel refresh after drag and drop outside
	 * folder with dot in name)
	 */
	public void testMoveResInDotNamedFolder() throws CoreException {
		try {
			createJavaProject("P", new String[] {});
			IProject project = getProject("P");
			createFolder("P/x.y");
			IFile file = createFile("P/x.y/test.txt", "");

			startDeltas();
			file.move(project.getFullPath().append("test.txt"), true, null);
			assertDeltas("Unexpected delta", "P[*]: {CONTENT}\n"
					+ "	ResourceDelta(/P/test.txt)[+]\n"
					+ "	ResourceDelta(/P/x.y)[*]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Move 2 non-java resources that were outside classpath to a package
	 * fragment root. (regression test for bug 28583 Missing one unit in package
	 * view)
	 */
	public void testMoveTwoResInRoot() throws CoreException {
		try {
			createJavaProject("P", new String[] { "src" });
			final IFile f1 = createFile("P/X.js", "public class X {}");
			final IFile f2 = createFile("P/Y.js", "public class Y {}");

			startDeltas();
			JavaScriptCore.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					f1.move(new Path("/P/src/X.js"), true, null);
					f2.move(new Path("/P/src/Y.js"), true, null);
				}
			}, null);
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN | CONTENT}\n"
					+ "	src[*]: {CHILDREN}\n" + "		<default>[*]: {CHILDREN}\n"
					+ "			X.js[+]: {}\n" + "			Y.js[+]: {}\n"
					+ "	ResourceDelta(/P/X.js)[-]\n"
					+ "	ResourceDelta(/P/Y.js)[-]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/**
	 * Test that deltas are generated when a nested package fragment root is
	 * removed and added via core API.
	 */
	public void testNestedRootParentMove() throws CoreException {
		try {
			createJavaProject("P",
					new String[] { "nested2/src", "nested/src" });
			deleteFolder("/P/nested2/src");

			startDeltas();
			IFolder folder = getFolder("/P/nested/src");
			folder.move(new Path("/P/nested2/src"), false, null);
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN | CONTENT}\n"
					+ "	nested2/src[+]: {MOVED_FROM(nested/src [in P])}\n"
					+ "	nested/src[-]: {MOVED_TO(nested2/src [in P])}\n"
					+ "	ResourceDelta(/P/nested)[*]\n"
					+ "	ResourceDelta(/P/nested2)[*]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/**
	 * Test that deltas are generated when a non-java file is removed and added
	 */
	public void testNonJavaResourceRemoveAndAdd() throws CoreException {
		try {
			createJavaProject("P", new String[] { "src" });
			IFile file = createFile("/P/src/read.txt", "");

			startDeltas();
			deleteResource(file);
			assertDeltas("Unexpected delta after deleting /P/src/read.txt",
					"P[*]: {CHILDREN}\n" + "	src[*]: {CONTENT}\n"
							+ "		ResourceDelta(/P/src/read.txt)[-]");

			clearDeltas();
			createFile("/P/src/read.txt", "");
			assertDeltas("Unexpected delta after creating /P/src/read.txt",
					"P[*]: {CHILDREN}\n" + "	src[*]: {CONTENT}\n"
							+ "		ResourceDelta(/P/src/read.txt)[+]");

		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Open a java project.
	 */
	public void testOpenJavaProject() throws CoreException {
		try {
			createJavaProject("P", new String[] { "" });
			IProject project = getProject("P");
			project.close(null);
			startDeltas();
			project.open(null);
			assertDeltas("Unexpected delta", "P[*]: {OPENED}\n"
					+ "ResourceDelta(/P)");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Open a non-java project.
	 */
	public void testOpenNonJavaProject() throws CoreException {
		try {
			createProject("P");
			IProject project = getProject("P");
			project.close(null);
			startDeltas();
			project.open(null);
			assertDeltas("Unexpected delta", "ResourceDelta(/P)");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/**
	 * Ensures that .classpath overwrite is taken into account. (regression test
	 * for bug 21420 Changing .classpath doesn't update JDT)
	 */
	public void testOverwriteClasspath() throws CoreException {
		try {
			createJavaProject("P", new String[] { "" });
			createFolder("P/src");
			createFolder("P/bin");
			final IFile newCP = createFile(
					"P/.classpath2",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
							+ "<classpath>\n"
							+ "    <classpathentry kind=\"src\" path=\"src\"/>\n"
							+ "    <classpathentry kind=\"output\" path=\"bin\"/>\n"
							+ "</classpath>");
			startDeltas();
			IWorkspaceRunnable run = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					IFile oldCP = newCP.getParent().getFile(
							new Path(".classpath"));
					deleteResource(oldCP);
					newCP.move(new Path("/P/.classpath"), true, null);
				}
			};
			getWorkspace().run(run, null);
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	<project root>[*]: {REMOVED FROM CLASSPATH}\n"
					+ "	src[*]: {ADDED TO CLASSPATH}\n"
					+ "	ResourceDelta(/P/.classpath)[*]\n"
					+ "	ResourceDelta(/P/.classpath2)[-]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/**
	 * Test that deltas are generated when package fragments are added and
	 * removed from a root via core API.
	 */
	public void testPackageFragmentAddAndRemove() throws CoreException {
		try {
			createJavaProject("P", new String[] { "src" });

			startDeltas();
			IFolder folder = createFolder("/P/src/p");
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	src[*]: {CHILDREN}\n" + "		p[+]: {}");

			clearDeltas();
			deleteResource(folder);
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	src[*]: {CHILDREN}\n" + "		p[-]: {}");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/**
	 * Test that deltas are generated when a package fragment is moved via core
	 * API.
	 */
	public void testPackageFragmentMove() throws CoreException {
		try {
			createJavaProject("P", new String[] { "src" });
			IFolder folder = createFolder("/P/src/p");

			startDeltas();
			folder.move(new Path("/P/src/p2"), false, null);
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	src[*]: {CHILDREN}\n"
					+ "		p[-]: {MOVED_TO(p2 [in src [in P]])}\n"
					+ "		p2[+]: {MOVED_FROM(p [in src [in P]])}");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/**
	 * Test that deltas are generated when a package fragment root is removed
	 * and added via core API.
	 */
	public void testPackageFragmentRootRemoveAndAdd() throws CoreException {
		try {
			createJavaProject("P", new String[] { "src" });

			startDeltas();
			deleteFolder("/P/src");
			assertDeltas("Unexpected delta after deleting /P/src",
					"P[*]: {CHILDREN}\n" + "	src[-]: {}");

			clearDeltas();
			createFolder("/P/src");
			assertDeltas("Unexpected delta after creating /P/src",
					"P[*]: {CHILDREN}\n" + "	src[+]: {}");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Remove then add a binary project (in a workspace runnable). (regression
	 * test for 24775 Wrong delta when replacing binary project with source
	 * project)
	 */
	public void testRemoveAddBinaryProject() throws CoreException {
		try {
			IJavaScriptProject project = createJavaProject("P",
					new String[] { "" });
			createFile("P/lib.jar", "");
			project.setRawIncludepath(new IIncludePathEntry[] { JavaScriptCore
					.newLibraryEntry(new Path("/P/lib.jar"), null, null) },
					null);

			startDeltas();
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					deleteProject("P");
					createJavaProject("P", new String[] { "" });
				}
			}, null);
			assertDeltas("Unexpected delta",
					"P[*]: {CHILDREN | CLASSPATH CHANGED}\n"
							+ "	<project root>[*]: {ADDED TO CLASSPATH}\n"
							+ "	lib.jar[-]: {}\n"
							+ "	ResourceDelta(/P/.classpath)[*]\n"
							+ "	ResourceDelta(/P/.project)[*]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Remove then add a java project (in a workspace runnable).
	 */
	public void testRemoveAddJavaProject() throws CoreException {
		try {
			createJavaProject("P", new String[] { "" });
			startDeltas();
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					deleteProject("P");
					createJavaProject("P", new String[] { "" });
				}
			}, null);
			assertDeltas("Unexpected delta",
					"P[*]: {CONTENT | CLASSPATH CHANGED}\n"
							+ "	ResourceDelta(/P/.classpath)[*]\n"
							+ "	ResourceDelta(/P/.project)[*]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Change the .classpath file so as to remove a classpath entry and remove
	 * the corresponding resource. (regression test for bug 24517 type view does
	 * not notice when jar disappears)
	 */
	public void testRemoveCPEntryAndRoot1() throws CoreException {
		try {
			IJavaScriptProject project = createJavaProject("P",
					new String[] { "src" });

			// ensure that the project is open (there are clients of the delta
			// only if the project is open)
			project.open(null);

			startDeltas();
			JavaScriptCore.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					editFile(
							"/P/.classpath",
							"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
									+ "<classpath>\n"
									+ "    <classpathentry kind=\"output\" path=\"bin\"/>\n"
									+ "</classpath>");
					deleteFolder("/P/src");
				}
			}, null);
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	src[-]: {}\n" + "	ResourceDelta(/P/.classpath)[*]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Remove a classpath entry and remove the corresponding resource.
	 * (regression test for bug 24517 type view does not notice when jar
	 * disappears)
	 */
	public void testRemoveCPEntryAndRoot2() throws CoreException {
		try {
			final IJavaScriptProject project = createJavaProject("P",
					new String[] { "src" });

			// ensure that the project is open (there are clients of the delta
			// only if the project is open)
			project.open(null);

			startDeltas();
			JavaScriptCore.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					project.setRawIncludepath(new IIncludePathEntry[] {}, null);
					deleteFolder("/P/src");
				}
			}, null);
			assertDeltas("Unexpected delta",
					"P[*]: {CHILDREN | CLASSPATH CHANGED}\n" + "	src[-]: {}\n"
							+ "	ResourceDelta(/P/.classpath)[*]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Remove the resource of a classpath entry and remove the classpath entry.
	 * (regression test for bug 24517 type view does not notice when jar
	 * disappears)
	 */
	public void testRemoveCPEntryAndRoot3() throws CoreException {
		try {
			final IJavaScriptProject project = createJavaProject("P",
					new String[] { "src" });

			// ensure that the project is open (there are clients of the delta
			// only if the project is open)
			project.open(null);

			startDeltas();
			JavaScriptCore.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					deleteFolder("/P/src");
					project.setRawIncludepath(new IIncludePathEntry[] {}, null);
				}
			}, null);
			assertDeltas("Unexpected delta",
					"P[*]: {CHILDREN | CLASSPATH CHANGED}\n" + "	src[-]: {}\n"
							+ "	ResourceDelta(/P/.classpath)[*]");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/*
	 * Remove the java nature of an existing java project.
	 */
	public void testRemoveJavaNature() throws CoreException {
		try {
			createJavaProject("P", new String[] { "" });
			startDeltas();
			removeJavaNature("P");
			assertDeltas("Unexpected delta", "P[-]: {}\n" + "ResourceDelta(/P)");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	public void testRemoveJavaProject() throws CoreException {
		try {
			createJavaProject("P", new String[] { "" });
			startDeltas();
			deleteProject("P");
			assertDeltas("Unexpected delta", "P[-]: {}");
		} finally {
			stopDeltas();
		}
	}

	/*
	 * Remove a non-java project.
	 */
	public void testRemoveNonJavaProject() throws CoreException {
		try {
			createProject("P");
			startDeltas();
			deleteProject("P");
			assertDeltas("Should get a non-Java resource delta",
					"ResourceDelta(/P)");
		} finally {
			stopDeltas();
		}
	}

	/*
	 * Removing a non-java project that contains a jar referenced in another
	 * project should produce a delta on this other project. (regression test
	 * for bug 19058 Closing non-java project doesn't remove root from java
	 * project)
	 */
	public void testRemoveNonJavaProjectUpdateDependent1() throws CoreException {
		try {
			createProject("SP");
			createFile("/SP/x.jar", "");
			createJavaProject("JP", new String[] { "" },
					new String[] { "/SP/x.jar" });
			startDeltas();
			deleteProject("SP");
			assertDeltas("Unexpected delta", "JP[*]: {CHILDREN}\n"
					+ "	/SP/x.jar[-]: {}\n" + "ResourceDelta(/SP)");
		} finally {
			stopDeltas();
			deleteProject("SP");
			deleteProject("JP");
		}
	}

	/*
	 * Removing a non-java project and another project reference a non exiting
	 * folder in the deleted project should not produce a delta on this other
	 * project (and no null pointer exception) (regression test for bug 19131
	 * NPE when removing a project containing missing classfile folder)
	 */
	public void testRemoveNonJavaProjectUpdateDependent2() throws CoreException {
		try {
			createProject("SP");
			createJavaProject("JP", new String[] { "" },
					new String[] { "/SP/missing" });
			startDeltas();
			deleteProject("SP");
			assertDeltas("Unexpected delta", "ResourceDelta(/SP)");
		} finally {
			stopDeltas();
			deleteProject("SP");
			deleteProject("JP");
		}
	}

	/*
	 * Removing a non-java project that contains a jar referenced in another
	 * project should produce a delta on this other project. Case of the removal
	 * being done right after start-up. (regression test for bug 31377
	 * NullPointerException on binary import)
	 */
	public void testRemoveNonJavaProjectUpdateDependent3() throws CoreException {
		try {
			createProject("SP");
			createFile("/SP/x.jar", "");
			createJavaProject("JP", new String[] { "" },
					new String[] { "/SP/x.jar" });

			// simulate start-up state of DeltaProcessor
			DeltaProcessingState deltaState = JavaModelManager
					.getJavaModelManager().deltaState;
			deltaState.oldRoots = null;
			deltaState.roots = null;
			deltaState.rootsAreStale = true;

			startDeltas();
			JavaScriptCore.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					deleteProject("SP");
				}
			}, null);
			assertDeltas("Unexpected delta", "JP[*]: {CHILDREN}\n"
					+ "	/SP/x.jar[-]: {}\n" + "ResourceDelta(/SP)");
		} finally {
			stopDeltas();
			deleteProject("SP");
			deleteProject("JP");
		}
	}

	/*
	 * Rename a java project. (regression test for bug 7030
	 * IllegalArgumentException renaming project)
	 */
	public void testRenameJavaProject() throws CoreException {
		try {
			createJavaProject("P", new String[] { "" });
			startDeltas();
			renameProject("P", "P1");
			assertDeltas("Unexpected delta", "P[-]: {MOVED_TO(P1)}\n"
					+ "P1[+]: {MOVED_FROM(P)}");
		} finally {
			stopDeltas();
			deleteProject("P");
			deleteProject("P1");
		}
	}

	public void testRenameMethodAndSave() throws CoreException {
		IJavaScriptUnit workingCopy = null;
		try {
			createJavaProject("P", new String[] { "" });
			createFolder("P/x/y");
			createFile("P/x/y/A.js", "package x.y;\n" + "public class A {\n"
					+ "  public void foo1() {\n" + "  }\n" + "}");
			IJavaScriptUnit cu = getCompilationUnit("P/x/y/A.js");
			workingCopy = cu.getWorkingCopy(null);
			workingCopy.getBuffer().setContents(
					"package x.y;\n" + "public class A {\n"
							+ "  public void foo2() {\n" + "  }\n" + "}");

			startDeltas();
			workingCopy.commitWorkingCopy(true, null);
			assertDeltas(
					"Unexpected delta",
					"P[*]: {CHILDREN}\n"
							+ "	<project root>[*]: {CHILDREN}\n"
							+ "		x.y[*]: {CHILDREN}\n"
							+ "			A.js[*]: {CHILDREN | FINE GRAINED | PRIMARY RESOURCE}\n"
							+ "				A[*]: {CHILDREN | FINE GRAINED}\n"
							+ "					foo1()[-]: {}\n" + "					foo2()[+]: {}");
		} finally {
			stopDeltas();
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
			deleteProject("P");
		}
	}

	/*
	 * Rename a non-java project. (regression test for bug 30224 No JavaElement
	 * delta when renaming non-Java project)
	 */
	public void testRenameNonJavaProject() throws CoreException {
		try {
			createProject("P");
			startDeltas();
			renameProject("P", "P1");
			assertDeltas("Unexpected delta", "ResourceDelta(/P)\n"
					+ "ResourceDelta(/P1)");
		} finally {
			stopDeltas();
			deleteProject("P");
			deleteProject("P1");
		}
	}

	/*
	 * Rename an outer pkg fragment. (regression test for bug 24685 Inner
	 * package fragments gets deleted - model out of synch)
	 */
	public void testRenameOuterPkgFragment() throws CoreException {
		try {
			createJavaProject("P", new String[] { "" });
			createFolder("P/x/y");
			createFile("P/x/y/X.js", "package x.y;\n" + "public class X {\n"
					+ "}");
			startDeltas();
			IPackageFragment pkg = getPackageFragment("P", "", "x");
			pkg.rename("z", false, null);
			assertDeltas("Unexpected delta", "P[*]: {CHILDREN}\n"
					+ "	<project root>[*]: {CHILDREN}\n" + "		z[+]: {}");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}

	/**
	 * Ensures that saving a working copy doesn't change the underlying
	 * resource. (only commit should do so)
	 */
	public void testSaveWorkingCopy() throws CoreException {
		IJavaScriptUnit copy = null;
		try {
			createJavaProject("P", new String[] { "" });
			createFile("P/X.js", "public class X {\n" + "}");
			IJavaScriptUnit unit = getCompilationUnit("P", "", "", "X.js");
			copy = unit.getWorkingCopy(null);
			copy.getType("X").createMethod("void foo() {}", null, true, null);
			startDeltas();
			copy.save(null, true);
			assertDeltas("Unexpected delta after saving working copy", "");
			copy.commitWorkingCopy(true, null);
			assertDeltas(
					"Unexpected delta after committing working copy",
					"P[*]: {CHILDREN}\n"
							+ "	<project root>[*]: {CHILDREN}\n"
							+ "		<default>[*]: {CHILDREN}\n"
							+ "			X.js[*]: {CHILDREN | FINE GRAINED | PRIMARY RESOURCE}\n"
							+ "				X[*]: {CHILDREN | FINE GRAINED}\n"
							+ "					foo()[+]: {}");
		} finally {
			stopDeltas();
			if (copy != null)
				copy.discardWorkingCopy();
			deleteProject("P");
		}
	}

	/**
	 * Ensure that a classpath change is detected even on a project which got
	 * closed
	 */
	public void testSetClasspathOnFreshProject() throws CoreException {
		try {
			createProject("LibProj");
			createFile("LibProj/mylib.jar", "");
			JavaProject p1 = (JavaProject) createJavaProject("P1",
					new String[] { "" });
			createFolder("P1/src2");

			p1.getProject().close(null);
			p1.getProject().open(null);

			startDeltas();

			IIncludePathEntry[] classpath = new IIncludePathEntry[] {
					JavaScriptCore.newSourceEntry(new Path("/P1/src2")),
					JavaScriptCore.newLibraryEntry(new Path(
							"/LibProj/mylib.jar"), null, null) };
			p1.setRawIncludepath(classpath, null);
			assertDeltas(
					"Should notice src2 and myLib additions to the classpath",
					"P1[*]: {CHILDREN | CLASSPATH CHANGED}\n"
							+ "	<project root>[*]: {REMOVED FROM CLASSPATH}\n"
							+ "	/LibProj/mylib.jar[*]: {ADDED TO CLASSPATH}\n"
							+ "	src2[*]: {ADDED TO CLASSPATH}\n"
							+ "	ResourceDelta(/P1/.classpath)[*]");
		} finally {
			stopDeltas();
			deleteProject("P1");
			deleteProject("LibProj");
		}
	}

	/**
	 * Ensures that setting a classpath variable when there is exactly one
	 * project triggers a java element delta.
	 */
	public void testSetClasspathVariable1() throws CoreException {
		try {
			createProject("LibProj");
			createFile("LibProj/mylib.jar", "");
			createFile("LibProj/otherlib.jar", "");
			JavaScriptCore.setIncludepathVariables(new String[] { "LIB" },
					new IPath[] { new Path("/LibProj/mylib.jar") }, null);
			createJavaProject("P", new String[] { "" }, new String[] { "LIB" });
			startDeltas();
			JavaScriptCore.setIncludepathVariables(new String[] { "LIB" },
					new IPath[] { new Path("/LibProj/otherlib.jar") }, null);
			assertDeltas(
					"Unexpected delta after setting classpath variable",
					"P[*]: {CHILDREN}\n"
							+ "	/LibProj/mylib.jar[*]: {REMOVED FROM CLASSPATH}\n"
							+ "	/LibProj/otherlib.jar[*]: {ADDED TO CLASSPATH}");
		} finally {
			stopDeltas();
			deleteProject("P");
			deleteProject("LibProj");
		}
	}

	/**
	 * Ensures that setting a classpath variable when there are more than one
	 * project triggers a java element delta.
	 */
	public void testSetClasspathVariable2() throws CoreException {
		try {
			createProject("LibProj");
			createFile("LibProj/mylib.jar", "");
			createFile("LibProj/otherlib.jar", "");
			JavaScriptCore.setIncludepathVariables(new String[] { "LIB" },
					new IPath[] { new Path("/LibProj/mylib.jar") }, null);
			createJavaProject("P1", new String[] { "" },
					new String[] { "LIB" });
			createJavaProject("P2", new String[] { "" },
					new String[] { "LIB" });
			startDeltas();
			JavaScriptCore.setIncludepathVariables(new String[] { "LIB" },
					new IPath[] { new Path("/LibProj/otherlib.jar") }, null);
			assertEquals(
					"Unexpected delta after setting classpath variable",
					"P1[*]: {CHILDREN}\n"
							+ "	/LibProj/mylib.jar[*]: {REMOVED FROM CLASSPATH}\n"
							+ "	/LibProj/otherlib.jar[*]: {ADDED TO CLASSPATH}\n"
							+ "P2[*]: {CHILDREN}\n"
							+ "	/LibProj/mylib.jar[*]: {REMOVED FROM CLASSPATH}\n"
							+ "	/LibProj/otherlib.jar[*]: {ADDED TO CLASSPATH}",
					getSortedByProjectDeltas());
		} finally {
			stopDeltas();
			deleteProject("P1");
			deleteProject("P2");
			deleteProject("LibProj");
		}
	}

	/**
	 * Ensures that committing a working copy fires a fine grained delta.
	 */
	public void testWorkingCopyCommit() throws CoreException {
		try {
			createJavaProject("P", new String[] { "" });
			createFolder("P/x/y");
			createFile("P/x/y/A.js", "package x.y;\n" + "public class A {\n"
					+ "}");
			IJavaScriptUnit cu = getCompilationUnit("P/x/y/A.js");
			IJavaScriptUnit copy = cu.getWorkingCopy(null);
			copy.getBuffer().setContents(
					"package x.y;\n" + "public class A {\n"
							+ "  public void foo() {\n" + "  }\n" + "}");
			copy.save(null, false);
			startDeltas();
			copy.commitWorkingCopy(true, null);
			assertDeltas(
					"Unexpected delta after commit",
					"P[*]: {CHILDREN}\n"
							+ "	<project root>[*]: {CHILDREN}\n"
							+ "		x.y[*]: {CHILDREN}\n"
							+ "			A.js[*]: {CHILDREN | FINE GRAINED | PRIMARY RESOURCE}\n"
							+ "				A[*]: {CHILDREN | FINE GRAINED}\n"
							+ "					foo()[+]: {}");
		} finally {
			stopDeltas();
			deleteProject("P");
		}
	}
}
