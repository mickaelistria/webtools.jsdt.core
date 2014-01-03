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
package org.eclipse.wst.jsdt.core.tests.rewrite.describing;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.IPackageFragment;
import org.eclipse.wst.jsdt.core.dom.AST;
import org.eclipse.wst.jsdt.core.dom.ASTNode;
import org.eclipse.wst.jsdt.core.dom.Block;
import org.eclipse.wst.jsdt.core.dom.ExpressionStatement;
import org.eclipse.wst.jsdt.core.dom.FieldDeclaration;
import org.eclipse.wst.jsdt.core.dom.FunctionDeclaration;
import org.eclipse.wst.jsdt.core.dom.FunctionInvocation;
import org.eclipse.wst.jsdt.core.dom.Initializer;
import org.eclipse.wst.jsdt.core.dom.JSdoc;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;
import org.eclipse.wst.jsdt.core.dom.Modifier;
import org.eclipse.wst.jsdt.core.dom.Name;
import org.eclipse.wst.jsdt.core.dom.PrimitiveType;
import org.eclipse.wst.jsdt.core.dom.SimpleName;
import org.eclipse.wst.jsdt.core.dom.SingleVariableDeclaration;
import org.eclipse.wst.jsdt.core.dom.TagElement;
import org.eclipse.wst.jsdt.core.dom.TextElement;
import org.eclipse.wst.jsdt.core.dom.TryStatement;
import org.eclipse.wst.jsdt.core.dom.Type;
import org.eclipse.wst.jsdt.core.dom.TypeDeclaration;
import org.eclipse.wst.jsdt.core.dom.VariableDeclarationExpression;
import org.eclipse.wst.jsdt.core.dom.VariableDeclarationFragment;
import org.eclipse.wst.jsdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.wst.jsdt.core.dom.rewrite.ListRewrite;

public class ASTRewritingMethodDeclTest extends ASTRewritingTest {
	
	private static final Class THIS= ASTRewritingMethodDeclTest.class;

	public ASTRewritingMethodDeclTest(String name) {
		super(name);
	}

	public static Test allTests() {
		return new Suite(THIS);
	}
	
	public static Test setUpTest(Test someTest) {
		TestSuite suite= new Suite("one test");
		suite.addTest(someTest);
		return suite;
	}
	
	public static Test suite() {
		return allTests();
	}

	/** @deprecated using deprecated code */
	public void testMethodDeclChanges() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("    public void gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		
		{ // convert constructor to method: insert return type
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "E");
			
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			
			// from constructor to method
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}
		{ // change return type
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "gee");
			assertTrue("Has no return type: gee", methodDecl.getReturnType() != null);
			
			Type returnType= methodDecl.getReturnType();
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			rewrite.replace(returnType, newReturnType, null);
		}
		{ // remove return type
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "hee");
			assertTrue("Has no return type: hee", methodDecl.getReturnType() != null);
						
			// from method to constructor
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}
		{ // rename method name
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "iee");
			
			SimpleName name= methodDecl.getName();
			SimpleName newName= ast.newSimpleName("xii");
			
			rewrite.replace(name, newName, null);
		}				
		{ // rename first param & last throw statement
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "jee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.replace((ASTNode) parameters.get(0), newParam, null);
						
			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 2 thrown exceptions", thrownExceptions.size() == 2);
			Name newThrownException= ast.newSimpleName("ArrayStoreException");
			rewrite.replace((ASTNode) thrownExceptions.get(1), newThrownException, null);			
		}
		{ // rename first and second param & rename first and last exception
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "kee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			rewrite.replace((ASTNode) parameters.get(0), newParam1, null);
			rewrite.replace((ASTNode) parameters.get(1), newParam2, null);
			
			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);
			Name newThrownException1= ast.newSimpleName("ArrayStoreException");
			Name newThrownException2= ast.newSimpleName("InterruptedException");
			rewrite.replace((ASTNode) thrownExceptions.get(0), newThrownException1, null);
			rewrite.replace((ASTNode) thrownExceptions.get(2), newThrownException2, null);
		}		
		{ // rename all params & rename second exception
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "lee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");			
			SingleVariableDeclaration newParam3= createNewParam(ast, "m3");	
			rewrite.replace((ASTNode) parameters.get(0), newParam1, null);
			rewrite.replace((ASTNode) parameters.get(1), newParam2, null);
			rewrite.replace((ASTNode) parameters.get(2), newParam3, null);
			
			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);
			Name newThrownException= ast.newSimpleName("ArrayStoreException");
			rewrite.replace((ASTNode) thrownExceptions.get(1), newThrownException, null);
		}				
		
					
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public float E(int p1, int p2, int p3) {}\n");
		buf.append("    public float gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void xii(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(float m, int p2, int p3) throws IllegalArgumentException, ArrayStoreException {}\n");
		buf.append("    public abstract void kee(float m1, float m2, int p3) throws ArrayStoreException, IllegalAccessException, InterruptedException;\n");
		buf.append("    public abstract void lee(float m1, float m2, float m3) throws IllegalArgumentException, ArrayStoreException, SecurityException;\n");
		buf.append("}\n");	
			
		assertEqualString(preview, buf.toString());
	}
	
	public void testMethodReturnTypeChanges() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E() {}\n");
		buf.append("    E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ E(int i, int j) {}\n");
		buf.append("    public void gee1() {}\n");
		buf.append("    void gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ void gee3() {}\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List list= type.bodyDeclarations();
		
		{ // insert return type, add second modifier
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(0);
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(Modifier.PUBLIC | Modifier.FINAL), null);
		
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			
			// from constructor to method
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
			
		}
		{ // insert return type, add (first) modifier
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(1);
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(Modifier.FINAL), null);
			
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			
			// from constructor to method
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}
		
		{ // insert return type, add second modifier with comments
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(2);
			
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(Modifier.FINAL), null);
			
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			
			// from constructor to method
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
			
		}
		
		{ // remove return type, add second modifier
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(3);
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(Modifier.PUBLIC | Modifier.FINAL), null);
		
			// from method to constructor 
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE_PROPERTY, null, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
			
		}
		{ // remove return type, add (first) modifier
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(4);
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(Modifier.FINAL), null);
			
			// from method to constructor 
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE_PROPERTY, null, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}
		
		{ // remove return type, add second modifier with comments
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(5);
			
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(Modifier.FINAL), null);
			
			// from method to constructor 
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE_PROPERTY, null, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}
		
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public final float E() {}\n");
		buf.append("    final float E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final /* comment */ float E(int i, int j) {}\n");
		buf.append("    public final gee1() {}\n");
		buf.append("    final gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final gee3() {}\n");
		buf.append("}\n");	
			
		assertEqualString(preview, buf.toString());

	}
	
	public void testMethodReturnTypeChanges2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public synchronized E() {}\n");
		buf.append("    public E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public /* comment */ E(int i, int j) {}\n");
		buf.append("    public synchronized void gee1() {}\n");
		buf.append("    public void gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public /* comment */ void gee3() {}\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List list= type.bodyDeclarations();
		
		{ // insert return type, remove second modifier
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(0);
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(Modifier.PUBLIC), null);
		
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			
			// from constructor to method
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
			
		}
		{ // insert return type, remove (only) modifier
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(1);
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(0), null);
			
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			
			// from constructor to method
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}
		
		{ // insert return type, remove modifier with comments
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(2);
			
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(0), null);
			
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			
			// from constructor to method
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
			
		}
		
		{ // remove return type, remove second modifier
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(3);
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(Modifier.PUBLIC), null);
		
			// from method to constructor 
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE_PROPERTY, null, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
			
		}
		{ // remove return type, remove (only) modifier
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(4);
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(0), null);
			
			// from method to constructor 
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE_PROPERTY, null, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}
		
		{ // remove return type, remove modifier with comments
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(5);
			
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(0), null);
			
			// from method to constructor 
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE_PROPERTY, null, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}
		
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public float E() {}\n");
		buf.append("    float E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ float E(int i, int j) {}\n");
		buf.append("    public gee1() {}\n");
		buf.append("    gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    gee3() {}\n");
		buf.append("}\n");	
			
		assertEqualString(preview, buf.toString());

	}


	
	public void testMethodReturnTypeChangesAST3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E() {}\n");
		buf.append("    E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ E(int i, int j) {}\n");
		buf.append("    public void gee1() {}\n");
		buf.append("    void gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ void gee3() {}\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST3(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List list= type.bodyDeclarations();
		
		{ // insert return type, add second modifier
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(0);
			
			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, FunctionDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
			
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			
			// from constructor to method
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
			
		}
		{ // insert return type, add (first) modifier
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(1);
			
			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, FunctionDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
			
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			
			// from constructor to method
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}
		
		{ // insert return type, add second modifier with comments
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(2);
			
			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, FunctionDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
			
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			
			// from constructor to method
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
			
		}
		
		{ // remove return type, add second modifier
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(3);

			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, FunctionDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
			
			// from method to constructor 
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
			
		}
		{ // remove return type, add (first) modifier
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(4);

			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, FunctionDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);			
			
			// from method to constructor 
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}
		
		{ // remove return type, add second modifier with comments
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(5);
			
			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, FunctionDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
			
			// from method to constructor 
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}
		
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public final float E() {}\n");
		buf.append("    final float E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final /* comment */ float E(int i, int j) {}\n");
		buf.append("    public final gee1() {}\n");
		buf.append("    final gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final gee3() {}\n");
		buf.append("}\n");	
			
		assertEqualString(preview, buf.toString());

	}
	
	public void testMethodReturnTypeChanges2AST3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public synchronized E() {}\n");
		buf.append("    public E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public /* comment */ E(int i, int j) {}\n");
		buf.append("    public synchronized void gee1() {}\n");
		buf.append("    public void gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public /* comment */ void gee3() {}\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST3(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List list= type.bodyDeclarations();
		
		{ // insert return type, remove second modifier
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(0);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(1), null);
		
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			
			// from constructor to method
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
			
		}
		{ // insert return type, remove (only) modifier
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(1);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
			
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			
			// from constructor to method
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}
		
		{ // insert return type, remove modifier with comments
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(2);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);			
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			
			// from constructor to method
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}
		
		{ // remove return type, remove second modifier
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(3);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(1), null);
			
			// from method to constructor 
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
			
		}
		{ // remove return type, remove (only) modifier
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(4);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
			
			// from method to constructor 
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}
		
		{ // remove return type, remove modifier with comments
			FunctionDeclaration methodDecl= (FunctionDeclaration) list.get(5);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
			
			// from method to constructor 
			rewrite.set(methodDecl, FunctionDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}
		
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public float E() {}\n");
		buf.append("    float E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ float E(int i, int j) {}\n");
		buf.append("    public gee1() {}\n");
		buf.append("    gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    gee3() {}\n");
		buf.append("}\n");	
			
		assertEqualString(preview, buf.toString());

	}


	
	
	public void testListRemoves() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("    public void gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		
		{ // delete first param
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "E");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(0), null);
		}
		{ // delete second param & remove exception & remove public
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "gee");
			
			// change flags
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(0), null);
			
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(1), null);
			
			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 1 thrown exceptions", thrownExceptions.size() == 1);
			rewrite.remove((ASTNode) thrownExceptions.get(0), null);
		}		
		{ // delete last param
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "hee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(2), null);	
		}				
		{ // delete first and second param & remove first exception
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "iee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);
			
			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 2 thrown exceptions", thrownExceptions.size() == 2);
			rewrite.remove((ASTNode) thrownExceptions.get(0), null);	
		}				
		{ // delete first and last param & remove second
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "jee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(2), null);
			
			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 2 thrown exceptions", thrownExceptions.size() == 2);
			rewrite.remove((ASTNode) thrownExceptions.get(1), null);			
		}
		{ // delete second and last param & remove first exception
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "kee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(1), null);
			rewrite.remove((ASTNode) parameters.get(2), null);
			
			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);
			rewrite.remove((ASTNode) thrownExceptions.get(1), null);
		}		
		{ // delete all params & remove first and last exception
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "lee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);
			rewrite.remove((ASTNode) parameters.get(2), null);
			
			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);
			rewrite.remove((ASTNode) thrownExceptions.get(0), null);
			rewrite.remove((ASTNode) thrownExceptions.get(2), null);				
		}				


		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p2, int p3) {}\n");
		buf.append("    void gee(int p1, int p3) {}\n");
		buf.append("    public void hee(int p1, int p2) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p3) throws IllegalAccessException {}\n");
		buf.append("    public void jee(int p2) throws IllegalArgumentException {}\n");
		buf.append("    public abstract void kee(int p1) throws IllegalArgumentException, SecurityException;\n");
		buf.append("    public abstract void lee() throws IllegalAccessException;\n");
		buf.append("}\n");	
			
		assertEqualString(preview, buf.toString());

	}
	
	public void testListRemoves2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void setMyProp(String property1) {}\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type = (TypeDeclaration) astRoot.types().get(0);
		
		{ // delete param, insert new
			FunctionDeclaration methodDecl= (FunctionDeclaration) type.bodyDeclarations().get(0);
			List parameters= methodDecl.parameters();
			rewrite.remove((ASTNode) parameters.get(0), null);
			
			SingleVariableDeclaration decl= ast.newSingleVariableDeclaration();
			decl.setType(ast.newPrimitiveType(PrimitiveType.INT));
			decl.setName(ast.newSimpleName("property11"));
			
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.PARAMETERS_PROPERTY).insertLast(decl, null);
			
		}
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void setMyProp(int property11) {}\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());
	}

	
	public void testListInserts() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("    public void gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		
		{ // insert before first param & insert an exception
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "E");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.PARAMETERS_PROPERTY).insertFirst(newParam, null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 0 thrown exceptions", thrownExceptions.size() == 0);
			
			Name newThrownException= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertFirst(newThrownException, null);

		}
		{ // insert before second param & insert before first exception & add synchronized
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "gee");
			
			// change flags
			int newModifiers= Modifier.PUBLIC | Modifier.SYNCHRONIZED;
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);
			
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ASTNode secondParam= (ASTNode) parameters.get(1);
			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.PARAMETERS_PROPERTY).insertBefore(newParam, secondParam, null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 1 thrown exceptions", thrownExceptions.size() == 1);
			
			ASTNode firstException= (ASTNode) thrownExceptions.get(0);
			Name newThrownException= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertBefore(newThrownException, firstException, null);
		}		
		{ // insert after last param & insert after first exception & add synchronized, static
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "hee");
			
			// change flags
			int newModifiers= Modifier.PUBLIC | Modifier.SYNCHRONIZED | Modifier.STATIC;
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);
			
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			
			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.PARAMETERS_PROPERTY).insertLast(newParam, null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 1 thrown exceptions", thrownExceptions.size() == 1);
			
			ASTNode firstException= (ASTNode) thrownExceptions.get(0);
			Name newThrownException= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertAfter(newThrownException, firstException, null);

		}				
		{ // insert 2 params before first & insert between two exception
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "iee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ASTNode firstParam= (ASTNode) parameters.get(0);
			
			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			
			ListRewrite listRewrite = rewrite.getListRewrite(methodDecl, FunctionDeclaration.PARAMETERS_PROPERTY);
			listRewrite.insertBefore(newParam1, firstParam, null);
			listRewrite.insertBefore(newParam2, firstParam, null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 2 thrown exceptions", thrownExceptions.size() == 2);
			
			ASTNode firstException= (ASTNode) thrownExceptions.get(0);
			Name newThrownException= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertAfter(newThrownException, firstException, null);
		}			
		{ // insert 2 params after first & replace the second exception and insert new after
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "jee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ListRewrite listRewrite = rewrite.getListRewrite(methodDecl, FunctionDeclaration.PARAMETERS_PROPERTY);

			ASTNode firstParam= (ASTNode) parameters.get(0);
			
			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			listRewrite.insertAfter(newParam2, firstParam, null);
			listRewrite.insertAfter(newParam1, firstParam, null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 2 thrown exceptions", thrownExceptions.size() == 2);
			
			Name newThrownException1= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException1, null);

			Name newThrownException2= ast.newSimpleName("ArrayStoreException");
			rewrite.replace((ASTNode) thrownExceptions.get(1), newThrownException2, null);
		}
		{ // insert 2 params after last & remove the last exception and insert new after
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "kee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, FunctionDeclaration.PARAMETERS_PROPERTY);
			ASTNode lastParam= (ASTNode) parameters.get(2);
			
			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");

			listRewrite.insertAfter(newParam2, lastParam, null);
			listRewrite.insertAfter(newParam1, lastParam, null);
			
			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);
			
			ASTNode lastException= (ASTNode) thrownExceptions.get(2);
			rewrite.remove(lastException, null);
			
			Name newThrownException= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertBefore(newThrownException, lastException, null);
		}	
		{ // insert at first and last position & remove 2nd, add after 2nd, remove 3rd
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "lee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, FunctionDeclaration.PARAMETERS_PROPERTY);
			
			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			listRewrite.insertFirst(newParam1, null);
			listRewrite.insertLast(newParam2, null);
			
			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);
			
			ASTNode secondException= (ASTNode) thrownExceptions.get(1);
			ASTNode lastException= (ASTNode) thrownExceptions.get(2);
			rewrite.remove(secondException, null);
			rewrite.remove(lastException, null);
			
			Name newThrownException= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertAfter(newThrownException, secondException, null);

		}				


		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(float m, int p1, int p2, int p3) throws InterruptedException {}\n");
		buf.append("    public synchronized void gee(int p1, float m, int p2, int p3) throws InterruptedException, IllegalArgumentException {}\n");
		buf.append("    public static synchronized void hee(int p1, int p2, int p3, float m) throws IllegalArgumentException, InterruptedException {}\n");
		buf.append("    public void iee(float m1, float m2, int p1, int p2, int p3) throws IllegalArgumentException, InterruptedException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, float m1, float m2, int p2, int p3) throws IllegalArgumentException, ArrayStoreException, InterruptedException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3, float m1, float m2) throws IllegalArgumentException, IllegalAccessException, InterruptedException;\n");
		buf.append("    public abstract void lee(float m1, int p1, int p2, int p3, float m2) throws IllegalArgumentException, InterruptedException;\n");
		buf.append("}\n");	
			
		assertEqualString(preview, buf.toString());

	}

	public void testListInsert() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		
		{ // insert at first and last position & remove 2nd, add after 2nd, remove 3rd
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "lee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, FunctionDeclaration.PARAMETERS_PROPERTY);
			
			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			listRewrite.insertFirst(newParam1, null);
			listRewrite.insertLast(newParam2, null);
			
			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);
			
			rewrite.remove((ASTNode) thrownExceptions.get(1), null);
			rewrite.remove((ASTNode) thrownExceptions.get(2), null);
			
			Name newThrownException= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException, null);
		}				


	
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public abstract void lee(float m1, int p1, int p2, int p3, float m2) throws IllegalArgumentException, InterruptedException;\n");
		buf.append("}\n");	
			
		assertEqualString(preview, buf.toString());

	}
	
	public void testListCombinations() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("    public void gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		
		{ // delete all and insert after & insert 2 exceptions
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "E");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
		
			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);
			rewrite.remove((ASTNode) parameters.get(2), null);

			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.PARAMETERS_PROPERTY).insertLast(newParam, null);


			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 0 thrown exceptions", thrownExceptions.size() == 0);
			
			Name newThrownException1= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException1, null);

			Name newThrownException2= ast.newSimpleName("ArrayStoreException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException2, null);
			
		}
		{ // delete first 2, replace last and insert after & replace first exception and insert before
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "gee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);
			
			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			rewrite.replace((ASTNode) parameters.get(2), newParam1, null);
						
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.PARAMETERS_PROPERTY).insertLast(newParam2, null);


			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 1 thrown exceptions", thrownExceptions.size() == 1);
			
			Name modifiedThrownException= ast.newSimpleName("InterruptedException");
			rewrite.replace((ASTNode) thrownExceptions.get(0), modifiedThrownException, null);
						
			Name newThrownException2= ast.newSimpleName("ArrayStoreException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException2, null);

		}		
		{ // delete first 2, replace last and insert at first & remove first and insert before
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "hee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);
			
			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			rewrite.replace((ASTNode) parameters.get(2), newParam1, null);
						
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.PARAMETERS_PROPERTY).insertFirst(newParam2, null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 1 thrown exceptions", thrownExceptions.size() == 1);
			
			rewrite.remove((ASTNode) thrownExceptions.get(0), null);
						
			Name newThrownException2= ast.newSimpleName("ArrayStoreException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException2, null);
		}				


		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(float m) throws InterruptedException, ArrayStoreException {}\n");
		buf.append("    public void gee(float m1, float m2) throws InterruptedException, ArrayStoreException {}\n");
		buf.append("    public void hee(float m2, float m1) throws ArrayStoreException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");	
			
		assertEqualString(preview, buf.toString());

	}
	
	public void testListCombination() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		
		{ // delete all and insert after & insert 2 exceptions
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "E");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
		
			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);
			rewrite.remove((ASTNode) parameters.get(2), null);

			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.PARAMETERS_PROPERTY).insertLast(newParam, null);

			List thrownExceptions= methodDecl.thrownExceptions();
			assertTrue("must be 0 thrown exceptions", thrownExceptions.size() == 0);
			
			Name newThrownException1= ast.newSimpleName("InterruptedException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException1, null);
			
			Name newThrownException2= ast.newSimpleName("ArrayStoreException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException2, null);

			
		}

		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(float m) throws InterruptedException, ArrayStoreException {}\n");
		buf.append("}\n");	
			
		assertEqualString(preview, buf.toString());

	}
	
	public void testListCombination2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("\n");	
		buf.append("    void bar() {\n");
		buf.append("    }\n");
		buf.append("\n");	
		buf.append("    void foo2() {\n");
		buf.append("       // user comment\n");
		buf.append("    }\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		
		FunctionDeclaration[] methods= type.getMethods();
		Arrays.sort(methods, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((FunctionDeclaration) o1).getName().getIdentifier().compareTo(((FunctionDeclaration) o2).getName().getIdentifier());
			}
		});
		
		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		for (int i= 0; i < methods.length; i++) {
			ASTNode copy= rewrite.createMoveTarget(methods[i]);
			listRewrite.insertLast(copy, null);
		}

		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    void bar() {\n");
		buf.append("    }\n");
		buf.append("\n");	
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("\n");	
		buf.append("    void foo2() {\n");
		buf.append("       // user comment\n");
		buf.append("    }\n");
		buf.append("}\n");	
			
		assertEqualString(preview, buf.toString());

	}
	
	
	public void testMethodBody() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("    public void gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		
		{ // replace block
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "E");
			
			Block body= methodDecl.getBody();
			assertTrue("No body: E", body != null);
			
			Block newBlock= ast.newBlock();

			rewrite.replace(body, newBlock, null);
		}
		{ // delete block & set abstract
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "gee");
			
			// change flags
			int newModifiers= Modifier.PUBLIC | Modifier.ABSTRACT;
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);
			
			Block body= methodDecl.getBody();
			assertTrue("No body: gee", body != null);

			rewrite.remove(body, null);
		}
		{ // insert block & set to private
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "kee");
			
			// change flags
			int newModifiers= Modifier.PRIVATE;
			rewrite.set(methodDecl, FunctionDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);
			
			
			Block body= methodDecl.getBody();
			assertTrue("Has body", body == null);
			
			Block newBlock= ast.newBlock();
			rewrite.set(methodDecl, FunctionDeclaration.BODY_PROPERTY, newBlock, null);
		}		

		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {\n");
		buf.append("    }\n");
		buf.append("    public abstract void gee(int p1, int p2, int p3) throws IllegalArgumentException;\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    private void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException {\n");
		buf.append("    }\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");	
		assertEqualString(preview, buf.toString());

	}
	
	public void testMethodDeclarationExtraDimensions() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1() { return null; }\n");
		buf.append("    public Object foo2() throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo3()[][] { return null; }\n");
		buf.append("    public Object foo4()[][] throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo5()[][] { return null; }\n");
		buf.append("    public Object foo6(int i)[][] throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo7(int i)[][] { return null; }\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		
		{ // add extra dim, add throws
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo1");
			
			rewrite.set(methodDecl, FunctionDeclaration.EXTRA_DIMENSIONS_PROPERTY, new Integer(1), null);
			
			Name newThrownException2= ast.newSimpleName("ArrayStoreException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException2, null);

		}
		{ // add extra dim, remove throws
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo2");
			
			rewrite.set(methodDecl, FunctionDeclaration.EXTRA_DIMENSIONS_PROPERTY, new Integer(1), null);
			
			rewrite.remove((ASTNode) methodDecl.thrownExceptions().get(0), null);			
		}		
		{ // remove extra dim, add throws
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo3");

			rewrite.set(methodDecl, FunctionDeclaration.EXTRA_DIMENSIONS_PROPERTY, new Integer(1), null);
			
			Name newThrownException2= ast.newSimpleName("ArrayStoreException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException2, null);

		}
		{ // add extra dim, remove throws
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo4");
			
			rewrite.set(methodDecl, FunctionDeclaration.EXTRA_DIMENSIONS_PROPERTY, new Integer(1), null);
			
			rewrite.remove((ASTNode) methodDecl.thrownExceptions().get(0), null);			
		}
		{ // add params, add extra dim, add throws
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo5");
			
			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.PARAMETERS_PROPERTY).insertLast(newParam1, null);

			
			rewrite.set(methodDecl, FunctionDeclaration.EXTRA_DIMENSIONS_PROPERTY, new Integer(4), null);
			
			Name newThrownException2= ast.newSimpleName("ArrayStoreException");
			rewrite.getListRewrite(methodDecl, FunctionDeclaration.THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException2, null);
	
		}
		{ // remove params, add extra dim, remove throws
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo6");
			
			rewrite.remove((ASTNode) methodDecl.parameters().get(0), null);		
			
			rewrite.set(methodDecl, FunctionDeclaration.EXTRA_DIMENSIONS_PROPERTY, new Integer(4), null);
			
			rewrite.remove((ASTNode) methodDecl.thrownExceptions().get(0), null);			
		}
		{ // remove block
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo7");
			rewrite.remove(methodDecl.getBody(), null);			
		}					
		
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1()[] throws ArrayStoreException { return null; }\n");
		buf.append("    public Object foo2()[] { return null; }\n");
		buf.append("    public Object foo3()[] throws ArrayStoreException { return null; }\n");
		buf.append("    public Object foo4()[] { return null; }\n");
		buf.append("    public Object foo5(float m1)[][][][] throws ArrayStoreException { return null; }\n");
		buf.append("    public Object foo6()[][][][] { return null; }\n");
		buf.append("    public Object foo7(int i)[][];\n");		
		buf.append("}\n");	
		assertEqualString(preview, buf.toString());

	}
	
	public void testModifiersAST3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public Object foo2() { return null; }\n");
		buf.append("    public Object foo3() { return null; }\n");
		buf.append("    /** javadoc comment */\n");	
		buf.append("    public Object foo4() { return null; }\n");
		buf.append("    Object foo5() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public Object foo6() { return null; }\n");
		buf.append("    public Object foo7() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public static Object foo8() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    Object foo9() { return null; }\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST3(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		
		{ // insert first and last
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo1");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, FunctionDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
			listRewrite.insertLast(ast.newModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD), null);
		}
		{ // insert 2x first
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo2");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, FunctionDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD), null);
		}		
		{ // remove and insert first
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo3");
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, FunctionDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
		}
		{ // remove and insert last
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo4");
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, FunctionDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
		}
		{ // insert first and insert Javadoc
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo5");
			JSdoc javadoc= ast.newJSdoc();
			TextElement textElem= ast.newTextElement();
			textElem.setText("Hello");
			TagElement tagElement= ast.newTagElement();
			tagElement.fragments().add(textElem);
			javadoc.tags().add(tagElement);
			rewrite.set(methodDecl, FunctionDeclaration.JAVADOC_PROPERTY, javadoc, null);
			
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, FunctionDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
		}
		{ // remove modifier and remove javadoc
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo6");
			rewrite.remove(methodDecl.getJavadoc(), null);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
		}
		{ // remove modifier and insert javadoc
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo7");
			
			JSdoc javadoc= ast.newJSdoc();
			TextElement textElem= ast.newTextElement();
			textElem.setText("Hello");
			TagElement tagElement= ast.newTagElement();
			tagElement.fragments().add(textElem);
			javadoc.tags().add(tagElement);
			rewrite.set(methodDecl, FunctionDeclaration.JAVADOC_PROPERTY, javadoc, null);
			
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
		}
		{ // remove all
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo8");
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(1), null);
		}
		{ // insert (first) with javadoc
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo9");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, FunctionDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
		}	
		
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    final public synchronized Object foo1() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    static final public Object foo2() { return null; }\n");
		buf.append("    final Object foo3() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final Object foo4() { return null; }\n");
		buf.append("    /**\n");
		buf.append("     * Hello\n");
		buf.append("     */\n");
		buf.append("    final Object foo5() { return null; }\n");
		buf.append("    Object foo6() { return null; }\n");
		buf.append("    /**\n");
		buf.append("     * Hello\n");
		buf.append("     */\n");
		buf.append("    Object foo7() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    Object foo8() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final Object foo9() { return null; }\n");
		buf.append("}\n");	
		assertEqualString(preview, buf.toString());
	}
	

	
	public void testFieldDeclaration() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    int i1= 1;\n");
		buf.append("    int i2= 1, k2= 2, n2= 3;\n");
		buf.append("    static final int i3= 1, k3= 2, n3= 3;\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("A.js", buf.toString(), false, null);
		
		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		
		AST ast= astRoot.getAST();
		
		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "A");
		
		FieldDeclaration[] fieldDeclarations= type.getFields();
		assertTrue("Number of fieldDeclarations not 3", fieldDeclarations.length == 3);
		{	// add modifier, change type, add fragment
			FieldDeclaration decl= fieldDeclarations[0];
			
			// add modifier
			int newModifiers= Modifier.FINAL;
			rewrite.set(decl, FieldDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);
			
			PrimitiveType newType= ast.newPrimitiveType(PrimitiveType.BOOLEAN);
			rewrite.replace(decl.getType(), newType, null);
					
			VariableDeclarationFragment frag=	ast.newVariableDeclarationFragment();
			frag.setName(ast.newSimpleName("k1"));
			frag.setInitializer(null);

			rewrite.getListRewrite(decl, FieldDeclaration.FRAGMENTS_PROPERTY).insertLast(frag, null);

		}
		{	// add modifiers, remove first two fragments, replace last
			FieldDeclaration decl= fieldDeclarations[1];
			
			// add modifier
			int newModifiers= Modifier.FINAL | Modifier.STATIC | Modifier.TRANSIENT;
			rewrite.set(decl, FieldDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);
			
			List fragments= decl.fragments();
			assertTrue("Number of fragments not 3", fragments.size() == 3);
			
			rewrite.remove((ASTNode) fragments.get(0), null);
			rewrite.remove((ASTNode) fragments.get(1), null);
			
			VariableDeclarationFragment frag=	ast.newVariableDeclarationFragment();
			frag.setName(ast.newSimpleName("k2"));
			frag.setInitializer(null);
			
			rewrite.replace((ASTNode) fragments.get(2), frag, null);
		}
		{	// remove modifiers
			FieldDeclaration decl= fieldDeclarations[2];
			
			// change modifier
			int newModifiers= 0;
			rewrite.set(decl, FieldDeclaration.MODIFIERS_PROPERTY, new Integer(newModifiers), null);
		}
				
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    final boolean i1= 1, k1;\n");
		buf.append("    static final transient int k2;\n");
		buf.append("    int i3= 1, k3= 2, n3= 3;\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	
	public void testInitializer() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    {\n");
		buf.append("        foo();\n");
		buf.append("    }\n");
		buf.append("    static {\n");
		buf.append("    }\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("A.js", buf.toString(), false, null);
		
		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		
		AST ast= astRoot.getAST();
		
		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "A");
		
		List declarations= type.bodyDeclarations();
		assertTrue("Number of fieldDeclarations not 2", declarations.size() == 2);
		{	// change modifier, replace body
			Initializer initializer= (Initializer) declarations.get(0);
			
			// add modifier
			int newModifiers= Modifier.STATIC;
			rewrite.set(initializer, Initializer.MODIFIERS_PROPERTY, new Integer(newModifiers), null);
			
			
			Block block= ast.newBlock();
			block.statements().add(ast.newReturnStatement());
			
			rewrite.replace(initializer.getBody(), block, null);
		}
		{	// change modifier
			Initializer initializer= (Initializer) declarations.get(1);
			
			int newModifiers= 0;
			rewrite.set(initializer, Initializer.MODIFIERS_PROPERTY, new Integer(newModifiers), null);
			
		}
				
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    static {\n");
		buf.append("        return;\n");
		buf.append("    }\n");
		buf.append("    {\n");
		buf.append("    }\n");
		buf.append("}\n");	
		
		assertEqualString(preview, buf.toString());

	}
	
	
	public void testMethodDeclarationParamShuffel() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1(int i, boolean b) { return null; }\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		
		{ // add extra dim, add throws
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo1");
			
			List params= methodDecl.parameters();
			
			SingleVariableDeclaration first= (SingleVariableDeclaration) params.get(0);
			SingleVariableDeclaration second= (SingleVariableDeclaration) params.get(1);
			rewrite.replace(first.getName(), ast.newSimpleName("x"), null);
			rewrite.replace(second.getName(), ast.newSimpleName("y"), null);
				
			ASTNode copy1= rewrite.createCopyTarget(first);
			ASTNode copy2= rewrite.createCopyTarget(second);
			
			rewrite.replace(first, copy2, null);
			rewrite.replace(second, copy1, null);
			
		}
	
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1(boolean y, int x) { return null; }\n");
		buf.append("}\n");	
		assertEqualString(preview, buf.toString());

	}	
	

	public void testMethodDeclarationParamShuffel1() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1(int i, boolean b) { return null; }\n");
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("E.js", buf.toString(), false, null);	
		
		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		
		{ 
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo1");
			
			List params= methodDecl.parameters();
			
			SingleVariableDeclaration first= (SingleVariableDeclaration) params.get(0);
			SingleVariableDeclaration second= (SingleVariableDeclaration) params.get(1);
				
			ASTNode copy2= rewrite.createCopyTarget(second);

			rewrite.replace(first, copy2, null);
			rewrite.remove(second, null);
		}
	
		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1(boolean b) { return null; }\n");
		buf.append("}\n");	
		assertEqualString(preview, buf.toString());

	}
		
	public void testMethodDeclaration_bug24916() throws Exception {
	
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    private int DD()[]{\n");
		buf.append("    };\n");
		buf.append("}\n");
		IJavaScriptUnit cu= pack1.createCompilationUnit("DD.js", buf.toString(), false, null);

		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "DD");
			
			rewrite.set(methodDecl, FunctionDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
			rewrite.set(methodDecl, FunctionDeclaration.EXTRA_DIMENSIONS_PROPERTY, new Integer(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    private DD(){\n");
		buf.append("    };\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}
	
	public void testMethodComments1() throws Exception {
	
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("    private void foo(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");	
		buf.append("}\n");
		IJavaScriptUnit cu= pack1.createCompilationUnit("DD.js", buf.toString(), false, null);

		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo");
			rewrite.remove(methodDecl, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");	
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}
	
	public void testMethodComments2() throws Exception {
	
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("    private void foo(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");	
		buf.append("}\n");
		IJavaScriptUnit cu= pack1.createCompilationUnit("DD.js", buf.toString(), false, null);

		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo2");
			ASTNode node= rewrite.createCopyTarget(methodDecl);

			ASTNode firstDecl= (ASTNode) type.bodyDeclarations().get(0);
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertAfter(node, firstDecl, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("    private void foo(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("\n");				
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");	
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}
	
	public void testMethodComments3() throws Exception {
	
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("\n");		
		buf.append("    private void foo(){\n");
		buf.append("    } // another\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");	
		buf.append("}\n");
		IJavaScriptUnit cu= pack1.createCompilationUnit("DD.js", buf.toString(), false, null);

		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo");
			rewrite.remove(methodDecl, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("\n");			
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");	
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}
	
	
	public void testBUG_38447() throws Exception {
		
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("\n");		
		buf.append("    private void foo(){\n");
		buf.append("\n"); // missing closing bracket
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");	
		buf.append("}\n");
		IJavaScriptUnit cu= pack1.createCompilationUnit("DD.js", buf.toString(), false, null);

		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo");
			rewrite.remove(methodDecl, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("\n");			
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");	
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMethodComments4() throws Exception {
	
	
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("\n");		
		buf.append("    private void foo(){\n");
		buf.append("    } // another\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");	
		buf.append("}\n");
		IJavaScriptUnit cu= pack1.createCompilationUnit("DD.js", buf.toString(), false, null);

		JavaScriptUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo");
			ASTNode copy= rewrite.createCopyTarget(methodDecl);
			
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(copy, null);
			
			FunctionDeclaration newMethodDecl= createNewMethod(astRoot.getAST(), "xoo", false);
			rewrite.replace(methodDecl, newMethodDecl, null);
			
			//FunctionDeclaration methodDecl2= findMethodDeclaration(type, "foo1");
			//rewrite.markAsReplaced(methodDecl2, copy);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("\n");		
		buf.append("    private void xoo(String str) {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("\n");	
		buf.append("    private void foo(){\n");
		buf.append("    } // another\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}
	
	/** @deprecated using deprecated code */
	public void testInsertFieldAfter() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    private int fCount1;\n");
		buf.append("\n");	
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		IJavaScriptUnit cu= pack1.createCompilationUnit("DD.js", buf.toString(), false, null);

		JavaScriptUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			VariableDeclarationFragment frag= ast.newVariableDeclarationFragment();
			frag.setName(ast.newSimpleName("fColor"));
			FieldDeclaration newField= ast.newFieldDeclaration(frag);
			newField.setType(ast.newPrimitiveType(PrimitiveType.CHAR));
			newField.setModifiers(Modifier.PRIVATE);
					
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertAt(newField, 1, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    private int fCount1;\n");
		buf.append("    private char fColor;\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	
	public void testVarArgs() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    private void foo1(String format, Object... args){\n");
		buf.append("    }\n");
		buf.append("    private void foo2(String format, Object[] args) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		IJavaScriptUnit cu= pack1.createCompilationUnit("DD.js", buf.toString(), false, null);

		JavaScriptUnit astRoot= createAST3(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo1");
			SingleVariableDeclaration param= (SingleVariableDeclaration) methodDecl.parameters().get(1);
			rewrite.set(param, SingleVariableDeclaration.VARARGS_PROPERTY, Boolean.FALSE, null);
		}
		{
			FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo2");
			SingleVariableDeclaration param= (SingleVariableDeclaration) methodDecl.parameters().get(1);
			rewrite.set(param, SingleVariableDeclaration.TYPE_PROPERTY, ast.newPrimitiveType(PrimitiveType.INT), null);
			rewrite.set(param, SingleVariableDeclaration.VARARGS_PROPERTY, Boolean.TRUE, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    private void foo1(String format, Object args){\n");
		buf.append("    }\n");
		buf.append("    private void foo2(String format, int... args) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	

	public void testMethodDeclChangesBug77538() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("// comment\n");
		buf.append("public class A {\n");
		buf.append("	public int foo() {\n");
		buf.append("		return 0;\n");
		buf.append("	}\n");	
		buf.append("}\n");	
		IJavaScriptUnit cu= pack1.createCompilationUnit("A.js", buf.toString(), false, null);	
		
		// Get method declaration and its body
		JavaScriptUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "A");
		FunctionDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block body = methodDecl.getBody();
		
	   // start record of the modifications
	   astRoot.recordModifications();
	   
	   // Modify method body
		Block newBody = ast.newBlock();
		methodDecl.setBody(newBody);
		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName("lock"));
		fragment.setInitializer(ast.newQualifiedName(ast.newSimpleName("OS"), ast.newSimpleName("lock")));
		VariableDeclarationExpression variableDeclarationExpression = ast.newVariableDeclarationExpression(fragment);
		variableDeclarationExpression.setType(ast.newSimpleType(ast.newSimpleName("Lock")));
		ExpressionStatement expressionStatement = ast.newExpressionStatement(variableDeclarationExpression);
		newBody.statements().add(expressionStatement);
		TryStatement tryStatement = ast.newTryStatement();
		FunctionInvocation methodInvocation = ast.newFunctionInvocation();
		methodInvocation.setName(ast.newSimpleName("lock"));
		methodInvocation.setExpression(ast.newSimpleName("lock"));
		ExpressionStatement expressionStatement2 = ast.newExpressionStatement(methodInvocation);
		body.statements().add(0, expressionStatement2);
		tryStatement.setBody(body);
		Block finallyBlock = ast.newBlock();
		tryStatement.setFinally(finallyBlock);
		methodInvocation = ast.newFunctionInvocation();
		methodInvocation.setName(ast.newSimpleName("unLock"));
		methodInvocation.setExpression(ast.newSimpleName("lock"));
		expressionStatement2 = ast.newExpressionStatement(methodInvocation);
		finallyBlock.statements().add(expressionStatement2);
		newBody.statements().add(tryStatement);

		// Verify that body extended length does not become negative!
		assertFalse("Invalid extended length for "+body, astRoot.getExtendedLength(body)<0);
	}
	

}
