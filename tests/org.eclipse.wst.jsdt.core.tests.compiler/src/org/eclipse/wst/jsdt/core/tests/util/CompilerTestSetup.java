/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.core.tests.util;

import java.util.Enumeration;

import junit.extensions.TestDecorator;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class CompilerTestSetup extends TestDecorator {

	String complianceLevel;

	public CompilerTestSetup(Test test, String complianceLevel) {
		super(test);
		this.complianceLevel = complianceLevel;
	}

	protected void initTest(Object test) {
		if (test instanceof AbstractCompilerTest) {
			AbstractCompilerTest compilerTest = (AbstractCompilerTest)test;
			compilerTest.initialize(this);
			return;
		}
		if (test instanceof TestSuite) {
			TestSuite testSuite = (TestSuite)test;
			Enumeration evaluationTestClassTests = testSuite.tests();
			while (evaluationTestClassTests.hasMoreElements()) {
				initTest(evaluationTestClassTests.nextElement());
			}
			return;
		}
	}

	public void run(TestResult result) {
		try {
			setUp();
			super.run(result);
		} finally {
			tearDown();
		}
	}

	protected void setUp() {
		// Init wrapped suite
		initTest(fTest);
	}

	protected void tearDown() {
	}
}
