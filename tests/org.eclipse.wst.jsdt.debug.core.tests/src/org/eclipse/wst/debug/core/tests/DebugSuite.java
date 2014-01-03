/*******************************************************************************
 *  Copyright (c) 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.debug.core.tests;

import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.eclipse.swt.widgets.Display;

/**
 * Debug test suite framework that runs test in a non UI thread.
 */
public abstract class DebugSuite extends TestSuite {
	
	/**
	 * Flag that indicates test are in progress
	 */
	protected boolean fTesting = true;


	/**
	 * Construct the test suite.
	 */
	public DebugSuite() {
	}
	
	/**
	 * Runs the tests and collects their result in a TestResult.
	 * The debug tests cannot be run in the UI thread or the event
	 * waiter blocks the UI when a resource changes.
	 * @see junit.framework.TestSuite#run(junit.framework.TestResult)
	 */
	public void run(final TestResult result) {
		final Display display = Display.getCurrent();
		Thread thread = null;
		try {
			Runnable r = new Runnable() {
				public void run() {
					for (Enumeration e= tests(); e.hasMoreElements(); ) {
				  		if (result.shouldStop() )
				  			break;
						Test test= (Test)e.nextElement();
						runTest(test, result);
					}					
					fTesting = false;
					display.wake();
				}
			};
			thread = new Thread(r);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		while (fTesting) {
			try {
				if (!display.readAndDispatch())
					display.sleep();
			} catch (Throwable e) {
				e.printStackTrace();
			}			
		}		
	}

}

