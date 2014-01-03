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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.wst.jsdt.core.*;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.internal.core.JavaModelManager;
import org.eclipse.wst.jsdt.internal.core.JavaModelStatus;
import org.eclipse.wst.jsdt.internal.core.UserLibraryJsGlobalScopeContainer;
import org.eclipse.wst.jsdt.internal.core.UserLibraryManager;

import junit.framework.Test;

public class ClasspathInitializerTests extends ModifyingResourceTests {
	
public static class DefaultVariableInitializer implements VariablesInitializer.ITestInitializer {
	Map variableValues;
	
	/*
	 * values is [<var name>, <var value>]*
	 */
	public DefaultVariableInitializer(String[] values) {
		variableValues = new HashMap();
		for (int i = 0; i < values.length; i+=2) {
			variableValues.put(values[i], new Path(values[i+1]));
		}
	}
	
	public void initialize(String variable) throws JavaScriptModelException {
		if (variableValues == null) return;
		JavaScriptCore.setIncludepathVariable(
			variable, 
			(IPath)variableValues.get(variable), 
			null);
	}
}

public static class DefaultContainerInitializer implements ContainerInitializer.ITestInitializer {
	
	public static class DefaultContainer implements IJsGlobalScopeContainer {
		char[][] libPaths;
		public DefaultContainer(char[][] libPaths) {
			this.libPaths = libPaths;
		}
		/**
		 * @deprecated Use {@link #getIncludepathEntries()} instead
		 */
		public IIncludePathEntry[] getClasspathEntries() {
			return getIncludepathEntries();
		}
		public IIncludePathEntry[] getIncludepathEntries() {
			int length = this.libPaths.length;
			IIncludePathEntry[] entries = new IIncludePathEntry[length];
			for (int j = 0; j < length; j++) {
			    IPath path = new Path(new String(this.libPaths[j]));
			    if (path.segmentCount() == 1) {
			        entries[j] = JavaScriptCore.newProjectEntry(path);
			    } else {
					entries[j] = JavaScriptCore.newLibraryEntry(path, null, null);
			    }
			}
			return entries;
		}
		public String getDescription() {
			return "Test container";
		}
		public int getKind() {
			return IJsGlobalScopeContainer.K_APPLICATION;
		}
		public IPath getPath() {
			return new Path("org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER");
		}
		/* (non-Javadoc)
		 * @see org.eclipse.wst.jsdt.core.IJsGlobalScopeContainer#resolvedLibraryImport(java.lang.String)
		 */
		public String[] resolvedLibraryImport(String a) {
			return new String[] {a};
		}
	}
	
	Map containerValues;
	CoreException exception;
	
	/*
	 * values is [<project name>, <lib path>[,<lib path>]* ]*
	 */
	public DefaultContainerInitializer(String[] values) {
		containerValues = new HashMap();
		for (int i = 0; i < values.length; i+=2) {
			final String projectName = values[i];
			final char[][] libPaths = CharOperation.splitOn(',', values[i+1].toCharArray());
			containerValues.put(
				projectName, 
				newContainer(libPaths)
			);
		}
	}
	protected DefaultContainer newContainer(final char[][] libPaths) {
		return new DefaultContainer(libPaths);
	}
	public boolean allowFailureContainer() {
		return true;
	}
	public void initialize(IPath containerPath, IJavaScriptProject project) throws CoreException {
		if (containerValues == null) return;
		try {
			JavaScriptCore.setJsGlobalScopeContainer(
				containerPath, 
				new IJavaScriptProject[] {project},
				new IJsGlobalScopeContainer[] {(IJsGlobalScopeContainer)containerValues.get(project.getElementName())}, 
				null);
		} catch (CoreException e) {
			this.exception = e;
			throw e;
		}
	}
}
// Simple container initializer, which keeps setting container to null
// (30920 - stackoverflow when setting container to null)
public class NullContainerInitializer implements ContainerInitializer.ITestInitializer {
	public boolean hasRun = false;
	public boolean allowFailureContainer() {
		return false; // allow the initializer to run again
	}
	public void initialize(IPath containerPath, IJavaScriptProject project) throws CoreException {
		hasRun = true;
		JavaScriptCore.setJsGlobalScopeContainer(
			containerPath, 
			new IJavaScriptProject[] {project}, 
			new IJsGlobalScopeContainer[] { null }, 
			null);
	}
}

public ClasspathInitializerTests(String name) {
	super(name);
}
public static Test suite() {
	return buildModelTestSuite(ClasspathInitializerTests.class);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
	// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_NAMES = new String[] { "testContainerInitializer12" };
	// Numbers of tests to run: "test<number>" will be run for each number of this array
//		TESTS_NUMBERS = new int[] { 2, 12 };
	// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		TESTS_RANGE = new int[] { 16, -1 };
}
protected void tearDown() throws Exception {
	// Cleanup caches
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	manager.containers = new HashMap(5);
	manager.variables = new HashMap(5);

	super.tearDown();
}
public void testContainerInitializer01() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"}));
		IJavaScriptProject p2 = createJavaProject(
				"P2", 
				new String[] {}, 
				new String[] {"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER"});
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(getFile("/P1/lib.jar"));
		assertTrue("/P1/lib.jar should exist", root.exists());
	} finally {
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
	}
}
public void testContainerInitializer02() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"}));
		IJavaScriptProject p2 = createJavaProject(
				"P2", 
				new String[] {}, 
				new String[] {"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER"});
				
		// simulate state on startup
		simulateExitRestart();
		
		startDeltas();
		p2.getResolvedIncludepath(true);
		
		assertDeltas(
			"Unexpected delta on startup", 
			""
		);
	} finally {
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
	}
}
public void testContainerInitializer03() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"}));
		createJavaProject(
				"P2", 
				new String[] {}, 
				new String[] {"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER"});
				
		// change value of TEST_CONTAINER
		createFile("/P1/lib2.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib2.jar"}));

		// simulate state on startup
		simulateExitRestart();
		
		startDeltas();
		getJavaProject("P2").getResolvedIncludepath(true);
		
		assertDeltas(
			"Unexpected delta on startup", 
			"P2[*]: {CHILDREN}\n" + 
			"	/P1/lib.jar[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	/P1/lib2.jar[*]: {ADDED TO CLASSPATH}"
		);
	} finally {
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
	}
}
/* Ensure that initializer is not callled when resource tree is locked.
 * (regression test for bug 29585 Core Exception as resource tree is locked initializing classpath container)
 */
public void testContainerInitializer04() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		DefaultContainerInitializer initializer = new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"});
		ContainerInitializer.setInitializer(initializer);
		createJavaProject(
				"P2", 
				new String[] {""}, 
				new String[] {"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER"});
				
		// simulate state on startup
		simulateExitRestart();
		
		startDeltas();
		createFile("/P2/X.js", "public class X {}");
		
		assertEquals("Should not get exception", null, initializer.exception);
		
		assertDeltas(
			"Unexpected delta on startup", 
			"P2[*]: {CHILDREN}\n" + 
			"	<project root>[*]: {CHILDREN}\n" + 
			"		<default>[*]: {CHILDREN}\n" + 
			"			X.java[+]: {}"
		);
	} finally {
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
	}
}
/* 
 * 30920 - Stack overflow when container resolved to null
 */
public void testContainerInitializer05() throws CoreException {
	try {
		NullContainerInitializer nullInitializer = new NullContainerInitializer();
		ContainerInitializer.setInitializer(nullInitializer);
		createJavaProject(
				"P1", 
				new String[] {""}, 
				new String[] {"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER"});
				
		// simulate state on startup
		simulateExitRestart();
		
		startDeltas();

		// will trigger classpath resolution (with null container value)
		createFile("/P1/X.js", "public class X {}");
		assertDeltas(
			"Unexpected delta on startup", 
			"P1[*]: {CHILDREN}\n" + 
			"	<project root>[*]: {CHILDREN}\n" + 
			"		<default>[*]: {CHILDREN}\n" + 
			"			X.java[+]: {}"
		);
		assertTrue("initializer did not run", nullInitializer.hasRun);
		
		// next cp resolution request will rerun the initializer
		waitForAutoBuild();
		nullInitializer.hasRun = false; // reset		
		getJavaProject("P1").getResolvedIncludepath(true);
		assertTrue("initializer did not run", nullInitializer.hasRun); // initializer should have run again (since keep setting to null)

		// assigning new (non-null) value to container
		waitForAutoBuild();
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P1", "/P1/lib.jar"}));
		clearDeltas();
		getJavaProject("P1").getResolvedIncludepath(true);
		assertDeltas(
			"Unexpected delta after setting container", 
			"P1[*]: {CHILDREN}\n" + 
			"	lib.jar[*]: {ADDED TO CLASSPATH}"
		);

	} catch (StackOverflowError e) {
		e.printStackTrace();
		assertTrue("stack overflow assigning container", false);
	} finally {
		stopDeltas();
		deleteProject("P1");
	}
}
/*
 * Ensures that running the initializer during a reconcile operation just after workspace startup
 * doesn't throw a NPE
 * (regression test for bug 48818 NPE in delta processor)
  */
public void testContainerInitializer06() throws CoreException {
    IJavaScriptUnit workingCopy = null;
	try {
		createProject("P1");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", ""}));
		createJavaProject(
				"P2", 
				new String[] {"src"}, 
				new String[] {"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER"});
		createFile(
			"/P2/src/X,java",
			"public class X {\n" +
			"}"
		);
				
		// change value of TEST_CONTAINER
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1"}));

		// simulate state on startup
		simulateExitRestart();
		
		startDeltas();
		workingCopy = getCompilationUnit("/P2/src/X.js");
		workingCopy.becomeWorkingCopy(null);
		
		assertDeltas(
			"Unexpected delta on startup", 
			"P2[*]: {CHILDREN}\n" + 
			"	src[*]: {CHILDREN}\n" + 
			"		<default>[*]: {CHILDREN}\n" + 
			"			[Working copy] X.java[+]: {PRIMARY WORKING COPY}"
		);
	} finally {
		stopDeltas();
		if (workingCopy != null) workingCopy.discardWorkingCopy();
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensure that an OperationCanceledException goes through
 * (regression test for bug 59363 Should surface cancellation exceptions)
 */
public void testContainerInitializer07() throws CoreException {
	try {
		boolean gotException = false;
		try {
			ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P1", "/P1/lib.jar"}) {
				public void initialize(IPath containerPath, IJavaScriptProject project) throws CoreException {
					throw new OperationCanceledException("test");
				}});
			IJavaScriptProject p1 = createJavaProject(
					"P1", 
					new String[] {}, 
					new String[] {"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER"});
			p1.getResolvedIncludepath(true);
		} catch (OperationCanceledException e) {
			gotException = true;
		}
		assertTrue("Should get an OperationCanceledException", gotException);
	} finally {
		stopDeltas();
		deleteProject("P1");
	}
}
/*
 * Ensure that the stack doesn't blow up if initializer is missbehaving
 * (regression test for bug 61052 Flatten cp container initialization)
 */
public void testContainerInitializer08() throws CoreException {
	final int projectLength = 10;
	final String[] projects = new String[projectLength];
	for (int i = 0; i < projectLength; i++) {
		projects[i] = "P" + i;
	}
	try {
		String[] projectRefs = new String[(projectLength-1) * 2];
		for (int i = 0; i < projectLength-1; i++) {
			projectRefs[i*2] = "P" + i;
			projectRefs[(i*2)+1] = "/P" + i + "/test.jar";
		}
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(projectRefs) {
			void foo(int n) {
				if (n > 0) {
					foo(n-1);
					return;
				}
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				for (int i = 0; i < projectLength-1; i++) {
					try {
						JavaScriptCore.create(root.getProject(projects[i])).getResolvedIncludepath(true);
					} catch (JavaScriptModelException e) {
						// project doesn't exist: ignore
					}
				}
			}
			public void initialize(IPath containerPath, IJavaScriptProject project) throws CoreException {
				foo(500);
				super.initialize(containerPath, project);
			}
		});
		JavaScriptCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i = 0; i < projectLength; i++) {
					createProject(projects[i]);
					editFile(
						"/" + projects[i] + "/.project",
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
						"<projectDescription>\n" + 
						"	<name>" + projects[i] + "</name>\n" + 
						"	<comment></comment>\n" + 
						"	<projects>\n" + 
						(i == 0 ? "" : "<project>" + projects[i-1] + "</project>\n") +
						"	</projects>\n" + 
						"	<buildSpec>\n" + 
						"		<buildCommand>\n" + 
						"			<name>org.eclipse.wst.jsdt.core.javabuilder</name>\n" + 
						"			<arguments>\n" + 
						"			</arguments>\n" + 
						"		</buildCommand>\n" + 
						"	</buildSpec>\n" + 
						"	<natures>\n" + 
						"		<nature>org.eclipse.wst.jsdt.core.javanature</nature>\n" + 
						"	</natures>\n" + 
						"</projectDescription>"
					);
					createFile(
						"/" + projects[i] + "/.classpath",
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<classpath>\n" +
						(i == 0 ? "" : "<classpathentry kind=\"src\" path=\"/" + projects[i-1] + "\"/>\n") +					 
						"	<classpathentry kind=\"con\" path=\"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER\"/>\n" +
						"	<classpathentry kind=\"output\" path=\"\"/>\n" +
						"</classpath>"
					);
				}
			}
		}, null);
		getJavaProject("P0").getResolvedIncludepath(true);
	} finally {
		stopDeltas();
		deleteProjects(projects);
	}
}
/*
 * Ensure that a StackOverFlowError is not thrown if the initializer asks for the resolved classpath
 * that is being resolved.
 * (regression test for bug 61040 Should add protect for reentrance to #getResolvedClasspath)
 */
public void testContainerInitializer09() throws CoreException {
	try {
		ClasspathInitializerTests.DefaultContainerInitializer initializer = new ClasspathInitializerTests.DefaultContainerInitializer(new String[] {"P1", "/P1/lib.jar"}) {
			protected DefaultContainer newContainer(char[][] libPaths) {
				return new DefaultContainer(libPaths) {
					/**
					 * @deprecated Use {@link #getIncludepathEntries()} instead
					 */
					public IIncludePathEntry[] getClasspathEntries() {
						return getIncludepathEntries();
					}

					public IIncludePathEntry[] getIncludepathEntries() {
						try {
							getJavaProject("P1").getResolvedIncludepath(true);
						} catch (JavaScriptModelException e) {
							// project doesn't exist: ignore
						}
						return super.getIncludepathEntries();
					}
				};
			}
		};
		ContainerInitializer.setInitializer(initializer);
		JavaScriptCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				createProject("P1");
				editFile(
					"/P1/.project",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
					"<projectDescription>\n" + 
					"	<name>P1</name>\n" + 
					"	<comment></comment>\n" + 
					"	<projects>\n" + 
					"	</projects>\n" + 
					"	<buildSpec>\n" + 
					"		<buildCommand>\n" + 
					"			<name>org.eclipse.wst.jsdt.core.javabuilder</name>\n" + 
					"			<arguments>\n" + 
					"			</arguments>\n" + 
					"		</buildCommand>\n" + 
					"	</buildSpec>\n" + 
					"	<natures>\n" + 
					"		<nature>org.eclipse.wst.jsdt.core.javanature</nature>\n" + 
					"	</natures>\n" + 
					"</projectDescription>"
				);
				createFile(
					"/P1/.classpath",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<classpath>\n" +
					"	<classpathentry kind=\"con\" path=\"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER\"/>\n" +
					"	<classpathentry kind=\"output\" path=\"\"/>\n" +
					"</classpath>"
				);
			}
		}, null);
		getJavaProject("P1").getResolvedIncludepath(true);
	} finally {
		stopDeltas();
		ContainerInitializer.setInitializer(null);
		deleteProject("P1");
	}
}
/*
 * Ensure that creating a Java project initializes a container and refreshes the external jar at the same time
 * without throwing a ConcurrentModificationException
 * (regression test for bug 63534 ConcurrentModificationException after "catching up")
 */
public void testContainerInitializer10() throws CoreException {
	class LogListener implements ILogListener {
    	IStatus log;
        public void logging(IStatus status, String plugin) {
            this.log = status;
        }
	}
	LogListener listener = new LogListener();
	try {
		Platform.addLogListener(listener);
		final IJavaScriptProject p1 = createJavaProject("P1");
		final IJavaScriptProject p2 = createJavaProject("P2");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P3", "/P1"}) {
	        public void initialize(IPath containerPath, IJavaScriptProject project) throws CoreException {
	            super.initialize(containerPath, project);
	            getJavaModel().refreshExternalArchives(new IJavaScriptElement[] {p1}, null);
	        }
		});
		getWorkspace().run(new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                p2.setRawIncludepath(new IIncludePathEntry[] {JavaScriptCore.newSourceEntry(new Path("/P2/src"))}, null);
				createProject("P3");
                editFile(
                	"/P3/.project",
                	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                	"<projectDescription>\n" + 
                	"	<name>P3</name>\n" + 
                	"	<comment></comment>\n" + 
                	"	<projects>\n" + 
                	"	</projects>\n" + 
                	"	<buildSpec>\n" + 
                	"		<buildCommand>\n" + 
                	"			<name>org.eclipse.wst.jsdt.core.javabuilder</name>\n" + 
                	"			<arguments>\n" + 
                	"			</arguments>\n" + 
                	"		</buildCommand>\n" + 
                	"	</buildSpec>\n" + 
                	"	<natures>\n" + 
                	"		<nature>org.eclipse.wst.jsdt.core.javanature</nature>\n" + 
                	"	</natures>\n" + 
                	"</projectDescription>\n"
                );
                createFile(
                	"/P3/.classpath",
                	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                	"<classpath>\n" + 
                	"	<classpathentry kind=\"src\" path=\"\"/>\n" + 
                	"	<classpathentry kind=\"var\" path=\"JCL_LIB\"/>\n" + 
                	"	<classpathentry kind=\"con\" path=\"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER\"/>\n" + 
                	"	<classpathentry kind=\"output\" path=\"\"/>\n" + 
                	"</classpath>"
                );
             }
        }, null);
		
		assertEquals("Should not get any exception in log", null, listener.log);
	} finally {
	    Platform.removeLogListener(listener);
		deleteProject("P1");
		deleteProject("P2");
		deleteProject("P3");
	}
}
/*
 * Ensure that a classpath initializer is not run on shutdown
 * (regression test for bug 93941 Classpath initialization on shutdown)
 */
public void testContainerInitializer11() throws CoreException {
	boolean hasExited = false;
	try {
		ContainerInitializer.setInitializer(null);
		createJavaProject(
			"P", 
			new String[] {}, 
			new String[] {"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER"});
		simulateExitRestart();
		ClasspathInitializerTests.DefaultContainerInitializer initializer = new ClasspathInitializerTests.DefaultContainerInitializer(new String[] {}) {
			public void initialize(IPath containerPath,IJavaScriptProject project) throws CoreException {
				assertTrue("Should not initialize container on shutdown", false);
			}
		};
		ContainerInitializer.setInitializer(initializer);
		simulateExit();
		hasExited = true;
	} finally {
		ContainerInitializer.setInitializer(null);
		if (hasExited)
			simulateRestart();
		deleteProject("P");
	}
}

/*
 * Ensure that the initializer is removed from the cache when the project is deleted
 * (regression test for bug 116072 cached classpath containers not removed when project deleted)
 */
public void testContainerInitializer12() throws CoreException {
	try {
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P1", "/P1/lib.jar"}));
		IJavaScriptProject project =  createJavaProject(
			"P1", 
			new String[] {}, 
			new String[] {"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER"});
		createFile("/P1/lib.jar", "");
		IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/P1/lib.jar"));
		assertTrue("/P1/lib.jar should exist", root.exists());
		deleteProject("P1");
		
		class Initializer extends DefaultContainerInitializer {
			boolean initialized;
			public Initializer(String[] args) {
				super(args);
			}
			public void initialize(IPath containerPath, IJavaScriptProject p) throws CoreException {
				super.initialize(containerPath, p);
				this.initialized = true;
			}
		}
		Initializer initializer = new Initializer(new String[] {"P1", "/P1/lib.jar"});
		ContainerInitializer.setInitializer(initializer);
		createJavaProject(
			"P1", 
			new String[] {}, 
			new String[] {"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER"});
		createFile("/P1/lib.jar", "");
		assertTrue("/P1/lib.jar should exist", root.exists());
		assertTrue("Should have been initialized", initializer.initialized);
	} finally {
		stopDeltas();
		deleteProject("P1");
	}
}

/* 
 * Ensures that no resource deta is reported if a container that was not initialized is initialized with null
 * (regression test for bug 149043 Unresolvable classpath container leads to lots of scheduled jobs)
 */
public void testContainerInitializer13() throws CoreException {
	IResourceChangeListener listener = new IResourceChangeListener() {
		StringBuffer buffer = new StringBuffer();
		public void resourceChanged(IResourceChangeEvent event) {
			this.buffer.append(event.getDelta().findMember(new Path("/P1")));
		}
		public String toString() {
			return this.buffer.toString();
		}
	};
	try {
		NullContainerInitializer nullInitializer = new NullContainerInitializer();
		ContainerInitializer.setInitializer(nullInitializer);
		IJavaScriptProject project = createJavaProject(
				"P1", 
				new String[] {}, 
				new String[] {"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER"});
				
		// simulate state on startup
		simulateExitRestart();
		
		getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
		
		// force resolution of container
		project.findPackageFragmentRoots(project.getRawIncludepath()[0]);

		assertEquals(
			"Unexpected resource delta on startup", 
			"",
			listener.toString()
		);
	} finally {
		getWorkspace().removeResourceChangeListener(listener);
		deleteProject("P1");
	}
}

/*
 * Ensures that a misbehaving container (that initializes another project than the one asked for) doesn't cause
 * the container to be initialized again
 * (regression test for bug 160005 Add protection about misbehaving container initializer)
 */
public void testContainerInitializer14() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		class Container extends DefaultContainerInitializer {
			int initializeCount = 0;
			Container(String[] values) {
				super(values);
			}
			public void initialize(IPath containerPath, IJavaScriptProject project) 	throws CoreException {
				this.initializeCount++;
				super.initialize(containerPath, getJavaProject("P1"));
			}
		}
		Container container = new Container(new String[] {"P2", "/P1/lib.jar"});
		ContainerInitializer.setInitializer(container);
		IJavaScriptProject p2 = createJavaProject(
				"P2", 
				new String[] {}, 
				new String[] {"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER"});
		p2.getResolvedIncludepath(true);
		assertEquals("Unexpected number of initalizations", 1, container.initializeCount);
	} finally {
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
	}
}

/*
 * Ensures that if a container is misbehaving (it doesn't initialize a project when asked for),
 * then the resulting container's classpath is not null
 * (regression test for bug 161846 Expanding a java project with invalid classpath container entries in Project Explorer causes CPU to stay at 100%)
 */
public void testContainerInitializer15() throws CoreException {
	try {
		class Container extends DefaultContainerInitializer {
			Container(String[] values) {
				super(values);
			}
			public void initialize(IPath containerPath, IJavaScriptProject project) 	throws CoreException {
			}
		}
		Container container = new Container(new String[] {"P1", "/P1/lib.jar"});
		ContainerInitializer.setInitializer(container);
		IJavaScriptProject p1 = createJavaProject(
				"P1", 
				new String[] {}, 
				new String[] {"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER"});
		IJsGlobalScopeContainer JsGlobalScopeContainer = JavaScriptCore.getJsGlobalScopeContainer(new Path("org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER"), p1);
		assertClasspathEquals(JsGlobalScopeContainer.getIncludepathEntries(), "");
	} finally {
		stopDeltas();
		deleteProject("P1");
	}
}
/*
 * Ensure that an initializer cannot return a project entry that points to the project of the container (cycle).
 */
public void testContainerInitializer16() throws CoreException {
	try {
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P1", "/P1"}));
		JavaScriptModelException exception = null;
		try {
			IJavaScriptProject p1 = createJavaProject(
				"P1", 
				new String[] {}, 
				new String[] {"org.eclipse.wst.jsdt.core.tests.model.TEST_CONTAINER"});
			p1.getResolvedIncludepath(true);
		} catch (JavaScriptModelException e) {
			exception = e;
		}
		assertExceptionEquals(
			"Unexpected expection", 
			"Project cannot reference itself: P1", 
			exception);
	} finally {
		stopDeltas();
		deleteProject("P1");
	}
}
public void testVariableInitializer01() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {"TEST_LIB", "/P1/lib.jar"}));
		IJavaScriptProject p2 = createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB"});
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(getFile("/P1/lib.jar"));
		assertTrue("/P1/lib.jar should exist", root.exists());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer02() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		createFile("/P1/src.zip", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {
			"TEST_LIB", "/P1/lib.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		}));
		IJavaScriptProject p2 = createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"});
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(getFile("/P1/lib.jar"));
		assertEquals("Unexpected source attachment path", "/P1/src.zip", root.getSourceAttachmentPath().toString());
		assertEquals("Unexpected source attachment root path", "src", root.getSourceAttachmentRootPath().toString());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer03() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		createFile("/P1/src.zip", "");
		String[] variableValues = new String[] {
			"TEST_LIB", "/P1/lib.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		};
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(variableValues));
		createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"});

		// simulate state on startup
		simulateExitRestart();
		
		startDeltas();
		//JavaModelManager.CP_RESOLVE_VERBOSE=true;		
		getJavaProject("P2").getResolvedIncludepath(true);
		
		assertDeltas(
			"Unexpected delta on startup", 
			""
		);
	} finally {
		//JavaModelManager.CP_RESOLVE_VERBOSE=false;		
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer04() throws CoreException {
	try {
		final StringBuffer buffer = new StringBuffer();
		VariablesInitializer.setInitializer(new VariablesInitializer.ITestInitializer() {
			public void initialize(String variable) throws JavaScriptModelException {
				buffer.append("Initializing " + variable + "\n");
				IPath path = new Path(variable.toLowerCase());
				buffer.append("Setting variable " + variable + " to " + path + "\n");
				JavaScriptCore.setIncludepathVariable(variable, path, null);
			}
		});
		createJavaProject("P", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"});
		assertEquals(
			"Initializing TEST_LIB\n" +
			"Setting variable TEST_LIB to test_lib\n",
			buffer.toString());
	} finally {
		deleteProject("P");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer05() throws CoreException {
	try {
		final StringBuffer buffer = new StringBuffer();
		VariablesInitializer.setInitializer(new VariablesInitializer.ITestInitializer() {
			public void initialize(String variable) throws JavaScriptModelException {
				buffer.append("Initializing " + variable + "\n");
				IPath path = new Path(variable.toLowerCase());
				JavaScriptCore.getIncludepathVariable("TEST_SRC");
				buffer.append("Setting variable " + variable + " to " + path + "\n");
				JavaScriptCore.setIncludepathVariable(variable, path, null);
			}
		});
		createJavaProject("P", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"});
		assertEquals(
			"Initializing TEST_LIB\n" +
			"Initializing TEST_SRC\n" +
			"Setting variable TEST_SRC to test_src\n" +
			"Setting variable TEST_LIB to test_lib\n",
			buffer.toString());
	} finally {
		deleteProject("P");
		VariablesInitializer.reset();
	}
}
/*
 * Ensures that if the initializer doesn't initialize a variable, it can be
 * initialized later on.
 */
public void testVariableInitializer06() throws CoreException {
	try {
		final StringBuffer buffer = new StringBuffer();
		VariablesInitializer.setInitializer(new VariablesInitializer.ITestInitializer() {
			public void initialize(String variable) {
				// do nothing
				buffer.append("Ignoring request to initialize");
			}
		});
		IPath path = JavaScriptCore.getIncludepathVariable("TEST_SRC");
		assertEquals(
			"Unexpected value of TEST_SRC after initializer was called",
			null,
			path);
		IPath varValue = new Path("src.zip");
		JavaScriptCore.setIncludepathVariable("TEST_SRC", varValue, null);
		path = JavaScriptCore.getIncludepathVariable("TEST_SRC");
		assertEquals(
			"Unexpected value of TEST_SRC after setting it",
			varValue,
			path);
	} finally {
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer07() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		createFile("/P1/src.zip", "");
		String[] variableValues = new String[] {
			"TEST_LIB", "/P1/lib.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		};
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(variableValues));
		createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"});

		// change value of TEST_LIB
		createFile("/P1/lib2.jar", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {
			"TEST_LIB", "/P1/lib2.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		}));

		// simulate state on startup
		simulateExitRestart();
		
		startDeltas();
		//JavaModelManager.CP_RESOLVE_VERBOSE=true;		
		getJavaProject("P2").getResolvedIncludepath(true);
		
		assertDeltas(
			"Unexpected delta on startup", 
			"P2[*]: {CHILDREN}\n" + 
			"	/P1/lib.jar[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	/P1/lib2.jar[*]: {ADDED TO CLASSPATH}"
		);
	} finally {
		//JavaModelManager.CP_RESOLVE_VERBOSE=false;		
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
		VariablesInitializer.reset();
	}
}
/*
 * Ensure that an OperationCanceledException goes through
 * (regression test for bug 59363 Should surface cancellation exceptions)
 */

public void testVariableInitializer08() throws CoreException {
	try {
		boolean gotException = false;
		try {
			VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {"TEST_LIB", "/P1/lib.jar"}) {
				public void initialize(String variable) throws JavaScriptModelException {
					throw new OperationCanceledException("test");
				}
			});
			IJavaScriptProject p1 = createJavaProject("P1", new String[] {}, new String[] {"TEST_LIB"});
			p1.getResolvedIncludepath(true);
		} catch (OperationCanceledException e) {
			gotException = true;
		}
		assertTrue("Should get an OperationCanceledException", gotException);
	} finally {
		deleteProject("P1");
		VariablesInitializer.reset();
	}
}

/*
 * Ensure that removing a classpath variable while initializing it doesn't throw a StackOverFlowError
 * (regression test for bug 112609 StackOverflow when initializing Java Core)
 */
public void testVariableInitializer09() throws CoreException {
	try {
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {"TEST_LIB", "/P1/lib.jar"}) {
			public void initialize(String variable) throws JavaScriptModelException {
				JavaScriptCore.removeIncludepathVariable("TEST_LIB", null);
			}
		});
		IJavaScriptProject p1 = createJavaProject("P1", new String[] {}, new String[] {"TEST_LIB"});
		IIncludePathEntry[] resolvedClasspath = p1.getResolvedIncludepath(true);
		assertClasspathEquals(
			resolvedClasspath, 
			""
		);
	} finally {
		deleteProject("P1");
		VariablesInitializer.reset();
	}
}
/*
 * Ensures that not initializing a classpath variable and asking for its value returns null
 * (regression test for bug 113110 TestFailures in DebugSuite)
 */
public void testVariableInitializer10() throws CoreException {
	try {
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {"TEST_LIB", "/P1/lib.jar"}) {
			public void initialize(String variable) throws JavaScriptModelException {
				// don't initialize
			}
		});
		// force resolution
		JavaScriptCore.getIncludepathVariable("TEST_LIB");
		// second call should still be null
		assertEquals("TEST_LIB should be null", null, JavaScriptCore.getIncludepathVariable("TEST_LIB"));
	} finally {
		deleteProject("P1");
		VariablesInitializer.reset();
	}
}
/**
 * Bug 125965: [prefs] "Export/Import preferences" should let user to choose wich preference to export/import
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=125965"
 */
public void testVariableInitializer11() throws CoreException {
	try {
		// Create initializer
		String varName = "TEST_LIB";
		String initialValue = "/P1/lib.jar";
		String newValue = "/tmp/file.jar";
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {varName, initialValue}));
		assertEquals("JavaScriptCore classpath value should have been initialized", JavaScriptCore.getIncludepathVariable(varName).toString(), initialValue);
		
		// Modify preference
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		IEclipsePreferences preferences = manager.getInstancePreferences();
		preferences.put(JavaModelManager.CP_VARIABLE_PREFERENCES_PREFIX+varName, newValue);
	
		// verify that JavaScriptCore preferences have been reset
		assertEquals("JavaScriptCore classpath value should be unchanged", JavaScriptCore.getIncludepathVariable(varName).toString(), initialValue);
		assertEquals("JavaScriptCore preferences value should be unchanged", preferences.get(varName, "X"), initialValue);
	} finally {
		VariablesInitializer.reset();
	}
}

/**
 * @bug 138599: [model][classpath] Need a way to mark a classpath variable as deprecated
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=138599"
 */
public void testVariableInitializerDeprecated() throws CoreException {
	try {
		// Create initializer
		String varName = "TEST_DEPRECATED";
		String filePath = "/P1/lib.jar";
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {varName, filePath}));
		assertEquals("JavaScriptCore classpath value should have been initialized", JavaScriptCore.getIncludepathVariable(varName).toString(), filePath);
		
		// Verify that Classpath Variable is deprecated
		assertEquals("JavaScriptCore classpath variable should be deprecated", "Test deprecated flag", JavaScriptCore.getIncludepathVariableDeprecationMessage(varName));

		// Create project
		IJavaScriptProject project = createJavaProject("P1");
		createFile("/P1/lib.jar", "");
		IIncludePathEntry variable = JavaScriptCore.newVariableEntry(new Path("TEST_DEPRECATED"), null, null);
		IJavaScriptModelStatus status = JavaScriptConventions.validateClasspathEntry(project, variable, false);
		assertStatus("Classpath variable 'TEST_DEPRECATED' in project P1 is deprecated: 'Test deprecated flag'", status);
		assertFalse("Status should not be OK", status.isOK());
		assertEquals("Status should have WARNING severity", IStatus.WARNING, status.getSeverity());
		assertEquals("Status should have deprecated code", IJavaScriptModelStatusConstants.DEPRECATED_VARIABLE, status.getCode());
	} finally {
		VariablesInitializer.reset();
		deleteProject("P1");
	}
}
public void testVariableInitializerUnboundAndDeprecated() throws CoreException {
	try {
		// Create initializer
		String varName = "TEST_DEPRECATED";
		String filePath = "/P1/lib.jar";
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {varName, filePath}));
		assertEquals("JavaScriptCore classpath value should have been initialized", JavaScriptCore.getIncludepathVariable(varName).toString(), filePath);
		
		// Verify that Classpath Variable is deprecated
		assertEquals("JavaScriptCore classpath variable should be deprecated", "Test deprecated flag", JavaScriptCore.getIncludepathVariableDeprecationMessage(varName));

		// Create project
		IJavaScriptProject project = createJavaProject("P1");
		IIncludePathEntry variable = JavaScriptCore.newVariableEntry(new Path("TEST_DEPRECATED"), null, null);
		IJavaScriptModelStatus status = JavaScriptConventions.validateClasspathEntry(project, variable, false);
		assertStatus("Project P1 is missing required library: 'lib.jar'", status);
		assertFalse("Status should not be OK", status.isOK());
		assertEquals("Status should have WARNING severity", IStatus.ERROR, status.getSeverity());
		assertEquals("Status should have deprecated code", IJavaScriptModelStatusConstants.INVALID_INCLUDEPATH, status.getCode());
	} finally {
		VariablesInitializer.reset();
		deleteProject("P1");
	}
}

/**
 * @bug 156226: [model][classpath] Allow classpath variable to be marked as non modifiable
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=156226"
 */
public void testVariableInitializerReadOnly() throws CoreException {
	try {
		// Create initializer
		String varName = "TEST_READ_ONLY";
		String path = "/P1/lib.jar";
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] { varName, path }));
		assertEquals("JavaScriptCore classpath value should have been initialized", JavaScriptCore.getIncludepathVariable(varName).toString(), path);

		// verify that Classpath Variable is read-only
		assertTrue("JavaScriptCore classpath variable should be read-only", JavaScriptCore.isIncludepathVariableReadOnly(varName));

		// Create project
		IJavaScriptProject project = createJavaProject("P1");
		createFile("/P1/lib.jar", "");
		IIncludePathEntry variable = JavaScriptCore.newVariableEntry(new Path("TEST_READ_ONLY"), null, null);
		IJavaScriptModelStatus status = JavaScriptConventions.validateClasspathEntry(project, variable, false);
		assertStatus("OK", status);
		assertTrue("Status should be OK", status.isOK());
		assertEquals("Status should be VERIFIED_OK", JavaModelStatus.VERIFIED_OK, status);
	} finally {
		VariablesInitializer.reset();
		deleteProject("P1");
	}
}
public void testVariableInitializerDeprecatedAndReadOnly() throws CoreException {
	try {
		// Create initializer
		String varName = "TEST_DEPRECATED_READ_ONLY";
		String path = "/P1/lib.jar";
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] { varName, path }));
		assertEquals("JavaScriptCore classpath value should have been initialized", JavaScriptCore.getIncludepathVariable(varName).toString(), path);

		// verify that Classpath Variable is read-only
		assertEquals("JavaScriptCore classpath variable should be deprecated", "A deprecated and read-only initializer", JavaScriptCore.getIncludepathVariableDeprecationMessage(varName));
		assertTrue("JavaScriptCore classpath variable should be read-only", JavaScriptCore.isIncludepathVariableReadOnly(varName));

		// Create project
		IJavaScriptProject project = createJavaProject("P1");
		createFile("/P1/lib.jar", "");
		IIncludePathEntry variable = JavaScriptCore.newVariableEntry(new Path("TEST_DEPRECATED_READ_ONLY"), null, null);
		IJavaScriptModelStatus status = JavaScriptConventions.validateClasspathEntry(project, variable, false);
		assertStatus("Classpath variable 'TEST_DEPRECATED_READ_ONLY' in project P1 is deprecated: 'A deprecated and read-only initializer'", status);
		assertFalse("Status should not be OK", status.isOK());
		assertEquals("Status should have WARNING severity", IStatus.WARNING, status.getSeverity());
		assertEquals("Status should have deprecated code", IJavaScriptModelStatusConstants.DEPRECATED_VARIABLE, status.getCode());
	} finally {
		VariablesInitializer.reset();
		deleteProject("P1");
	}
}

/**
 * @bug 172207: [model] Marker for deprecated classpath variable should always have WARNING severity
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=172207"
 */
public void testVariableInitializerBug172207() throws CoreException {
	try {
		// Create initializer
		String varName = "TEST_DEPRECATED_READ_ONLY";
		String path = "/P1/lib.jar";
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] { varName, path }));
		assertEquals("JavaScriptCore classpath value should have been initialized", JavaScriptCore.getIncludepathVariable(varName).toString(), path);

		// verify that Classpath Variable is read-only
		assertEquals("JavaScriptCore classpath variable should be deprecated", "A deprecated and read-only initializer", JavaScriptCore.getIncludepathVariableDeprecationMessage(varName));
		assertTrue("JavaScriptCore classpath variable should be read-only", JavaScriptCore.isIncludepathVariableReadOnly(varName));

		// Create project
		IJavaScriptProject project = createJavaProject("P1");
		createFile("/P1/lib.jar", "");
		IIncludePathEntry variable = JavaScriptCore.newVariableEntry(new Path("TEST_DEPRECATED_READ_ONLY"), null, null);
		IIncludePathEntry[] entries = project.getRawIncludepath();
		int length = entries.length;
		System.arraycopy(entries, 0, entries = new IIncludePathEntry[length+1], 0, length);
		entries[length] = variable;
		project.setRawIncludepath(entries, null);

		// verify markers
		waitForAutoBuild();
		IMarker[] markers = project.getProject().findMarkers(IJavaScriptModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
		sortMarkers(markers);
		assertMarkers("Unexpected marker(s)",
			"Classpath variable 'TEST_DEPRECATED_READ_ONLY' in project P1 is deprecated: 'A deprecated and read-only initializer'",
			markers);
		assertEquals("Marker on deprecated variable should be a WARNING", IMarker.SEVERITY_WARNING, markers[0].getAttribute(IMarker.SEVERITY, -1));
	} finally {
		VariablesInitializer.reset();
		deleteProject("P1");
	}
}

/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=61872"
 */
public void testUserLibraryInitializer1() throws CoreException {
	try {
		// Create new user library "SWT"
		JsGlobalScopeContainerInitializer initializer= JavaScriptCore.getJsGlobalScopeContainerInitializer(JavaScriptCore.USER_LIBRARY_CONTAINER_ID);
		String libraryName = "SWT";
		IPath containerPath = new Path(JavaScriptCore.USER_LIBRARY_CONTAINER_ID);
		UserLibraryJsGlobalScopeContainer containerSuggestion = new UserLibraryJsGlobalScopeContainer(libraryName);
		initializer.requestJsGlobalScopeContainerUpdate(containerPath.append(libraryName), null, containerSuggestion);

		// Create java project
		createJavaProject("p61872");
		IFile jarFile = createFile("/p61872/swt.jar", "");
		IFile srcFile = createFile("/p61872/swtsrc.zip", "");

		// Modify user library
		Preferences preferences = JavaScriptCore.getPlugin().getPluginPreferences();
		String propertyName = UserLibraryManager.CP_USERLIBRARY_PREFERENCES_PREFIX+"SWT";
		StringBuffer propertyValue = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<userlibrary systemlibrary=\"false\" version=\"1\">\r\n<archive");
		String jarFullPath = getWorkspaceRoot().getLocation().append(jarFile.getFullPath()).toString();
		propertyValue.append(" path=\""+jarFullPath);
		propertyValue.append("\"/>\r\n</userlibrary>\r\n");
		preferences.setValue(propertyName, propertyValue.toString());
		JavaScriptCore.getPlugin().savePluginPreferences();

		// Modify project classpath
		editFile(
			"/p61872/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"con\" path=\"org.eclipse.wst.jsdt.USER_LIBRARY/SWT\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>"
		);

		// Verify 
		IIncludePathEntry[] entries = getJavaProject("p61872").getResolvedIncludepath(true);
		assertEquals("Invalid entries number in resolved classpath for project p61872!", 1, entries.length);
		assertEquals("Invalid path for project 61872 classpath entry!", jarFullPath.toLowerCase(), entries[0].getPath().toString().toLowerCase());
		assertNull("Project 61872 classpath entry should not have any source attached!", entries[0].getSourceAttachmentPath());

		// Modify user library
		propertyValue = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<userlibrary systemlibrary=\"false\" version=\"1\">\r\n<archive");
		String srcFullPath = getWorkspaceRoot().getLocation().append(srcFile.getFullPath()).toString();
		propertyValue.append(" sourceattachment=\""+srcFullPath);
		propertyValue.append("\" path=\""+jarFullPath);
		propertyValue.append("\"/>\r\n</userlibrary>\r\n");
		preferences.setValue(propertyName, propertyValue.toString());
		JavaScriptCore.getPlugin().savePluginPreferences();

		// Verify 
		entries = getJavaProject("p61872").getResolvedIncludepath(true);
		assertEquals("Invalid entries number in resolved classpath for project p61872!", 1, entries.length);
		assertEquals("Invalid path for project 61872 classpath entry!", jarFullPath.toLowerCase(), entries[0].getPath().toString().toLowerCase());
		assertEquals("Invalid source attachement path for project 61872 classpath entry!", srcFullPath.toLowerCase(), entries[0].getSourceAttachmentPath().toString().toLowerCase());
	} finally {
		deleteProject("p61872");
	}
}
}
