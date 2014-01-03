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
package org.eclipse.wst.jsdt.core.tests.compiler.parser;

import org.eclipse.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;

public class SourceInitializer extends SourceField {
public SourceInitializer(
	int declarationStart, 
	int modifiers) {
	super(declarationStart, modifiers, null, null, -1, -1, null);
}

public void setDeclarationSourceEnd(int declarationSourceEnd) {
	this.declarationEnd = declarationSourceEnd;
}

public String toString(int tab) {
	if (modifiers == ClassFileConstants.AccStatic) {
		return tabString(tab) + "static {}";
	}
	return tabString(tab) + "{}";
}
}
