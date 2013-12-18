/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.debug.opera.internal.jsdi.request;

import org.eclipse.wst.jsdt.debug.core.jsdi.request.VMDisconnectRequest;
import org.eclipse.wst.jsdt.debug.opera.internal.jsdi.VirtualMachineImpl;

/**
 * Default {@link VMDisconnectRequest} implementation for Opera
 * 
 * @since 0.1
 */
public class VMDisconnectRequestImpl extends EventRequestImpl implements VMDisconnectRequest {

	/**
	 * Constructor
	 * @param vm
	 */
	public VMDisconnectRequestImpl(VirtualMachineImpl vm) {
		super(vm);
	}
}