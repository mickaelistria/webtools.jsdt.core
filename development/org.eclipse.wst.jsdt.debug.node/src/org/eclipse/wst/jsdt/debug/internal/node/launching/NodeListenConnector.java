/*******************************************************************************
 * Copyright (c) 2012, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.debug.internal.node.launching;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.wst.jsdt.debug.core.jsdi.VirtualMachine;
import org.eclipse.wst.jsdt.debug.core.jsdi.connect.ListeningConnector;

/**
 * {@link ListeningConnector} for Node.js
 */
public class NodeListenConnector implements ListeningConnector {

	public static final String CONNECTOR_ID = "org.eclipse.wst.jsdt.debug.node.listening.connector"; //$NON-NLS-1$
	
	/**
	 * Constructor
	 */
	public NodeListenConnector() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.debug.core.jsdi.connect.Connector#defaultArguments()
	 */
	public Map defaultArguments() {
		HashMap args = new HashMap();
		//TODO add args
		return args;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.debug.core.jsdi.connect.Connector#description()
	 */
	public String description() {
		return Messages.NodeListenConnector_1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.debug.core.jsdi.connect.Connector#name()
	 */
	public String name() {
		return Messages.NodeListenConnector_2;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.debug.core.jsdi.connect.Connector#id()
	 */
	public String id() {
		return CONNECTOR_ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.debug.core.jsdi.connect.ListeningConnector#accept(java.util.Map)
	 */
	public VirtualMachine accept(Map arguments) throws IOException {
		return null;
	}
}