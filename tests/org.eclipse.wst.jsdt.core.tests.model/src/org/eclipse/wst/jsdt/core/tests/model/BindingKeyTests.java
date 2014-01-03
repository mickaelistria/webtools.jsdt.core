/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.core.tests.model;

import org.eclipse.wst.jsdt.core.BindingKey;

import junit.framework.Test;

public class BindingKeyTests extends AbstractJavaModelTests {
	
	static {
	}

	public BindingKeyTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(BindingKeyTests.class);
	}
	
	protected void assertBindingKeyEquals(String expected, String key) {
		if (!(expected.equals(key)))
			System.out.println(displayString(key, 3) + ",");
		assertEquals(expected, key);
	}
	
	protected void assertBindingKeySignatureEquals(String expected, String key) {
		BindingKey bindingKey = new BindingKey(key);
		String signature = bindingKey.toSignature();
		if (!(expected.equals(signature)))
			System.out.println(displayString(signature, 3) + ",");
		assertEquals(expected, signature);
	}
	
	/*
	 * Package.
	 */
	public void test001() {
		assertBindingKeySignatureEquals(
			"p",
			"p"
		);
	}

	/*
	 * Top level type in non default package.
	 */
	public void test002() {
		assertBindingKeySignatureEquals(
			"Lp.X;",
			"Lp/X;"
		);
	}

	/*
	 * Top level type in default package.
	 */
	public void test003() {
		assertBindingKeySignatureEquals(
			"LClazz;",
			"LClazz;"
		);
	}

	/*
	 * Member type
	 */
	public void test004() {
		assertBindingKeySignatureEquals(
			"Lp.X$Member;",
			"Lp/X$Member;"
		);
	}

	/*
	 * Member type (2 levels deep)
	 */
	public void test005() {
		assertBindingKeySignatureEquals(
			"Lp1.X$Member1$Member2;",
			"Lp1/X$Member1$Member2;"
		);
	}

	/*
	 * Anonymous type
	 */
	public void test006() {
		assertBindingKeySignatureEquals(
			"Lp1.X$1;",
			"Lp1/X$1;"
		);
	}

	/*
	 * Local type
	 */
	public void test007() {
		assertBindingKeySignatureEquals(
			"Lp1.X$1$Y;",
			"Lp1/X$1$Y;"
		);
	}

	/*
	 * Array type
	 */
	public void test008() {
		assertBindingKeySignatureEquals(
			"[Lp1.X;",
			"[Lp1/X;"
		);
	}

	/*
	 * Secondary type
	 */
	public void test012() {
		assertBindingKeySignatureEquals(
			"Lp1.Secondary;",
			"Lp1/X~Secondary;"
		);
	}

	/*
	 * Anonymous in a secondary type
	 */
	public void test013() {
		assertBindingKeySignatureEquals(
			"Lp1.Secondary$1;",
			"Lp1/X~Secondary$1;"
		);
	}

	/*
	 * Method
	 * (regression test for bug 85811 BindingKey.toSignature should return method signature for methods)
	 */
	public void test014() {
		assertBindingKeySignatureEquals(
			"(Ljava.lang.String;I)Z",
			"Lp1/X;.foo(Ljava/lang/String;I)Z"
		);
	}
	
	/*
	 * Create a type binding key from a fully qualified name
	 */
	public void test015() {
		String key = BindingKey.createTypeBindingKey("java.lang.Object");
		assertBindingKeyEquals(
			"Ljava/lang/Object;",
			key);
	}

	/*
	 * Create a type binding key from an array type name
	 */
	public void test017() {
		String key = BindingKey.createTypeBindingKey("Boolean[]");
		assertBindingKeyEquals(
			"[LBoolean;",
			key);
	}

	/*
	 * Create an array type binding key
	 */
	public void test020() {
		String key = BindingKey.createArrayTypeBindingKey("Ljava/lang/Object;", 1);
		assertBindingKeyEquals(
			"[Ljava/lang/Object;",
			key);
	}

	/*
	 * Create an array type binding key
	 */
	public void test021() {
		String key = BindingKey.createArrayTypeBindingKey("I", 2);
		assertBindingKeyEquals(
			"[[I",
			key);
	}
	
	/*
	 * Method starting with an upper case corresponding to a primitive type
	 * (regression test for bug 94398 Error attempting to find References)
	 */
	public void test034() {
		assertBindingKeySignatureEquals(
			"(Ljava.lang.String;I)Z",
			"Lp1/X;.Set(Ljava/lang/String;I)Z"
		);
	}
	
	/*
	 * Field
	 * (regression test for bug  87362 BindingKey#internalToSignature() should return the field's type signature)
	 */
	public void test037() {
		assertBindingKeySignatureEquals(
			"Ljava.lang.String;",
			"Lp/X;.foo)Ljava/lang/String;"
		);
	}

	/*
	 * Base type
	 * (regression test for bug 97187 [rendering] Shows Single Char for primitve Types)
	 */
	public void test039() {
		assertBindingKeySignatureEquals(
			"Z",
			"Z"
		);
	}
}
