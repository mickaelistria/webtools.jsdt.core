/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.debug.internal.rhino.ui;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

/**
 * Image registry for shared images used in JavaScript Debug UI
 * 
 * @since 1.0
 */
public final class RhinoImageRegistry {

	/** 
	 * The image registry containing <code>Image</code>s and <code>ImageDescriptor</code>s.
	 */
	private static ImageRegistry imageRegistry;
	private static ImageDescriptorRegistry descRegistry;
	
	private static String ICONS_PATH = "$nl$/icons/"; //$NON-NLS-1$
	
	/**
	 * Icon paths
	 */
	final static String ELCL = ICONS_PATH + "elcl16/"; //enabled - size 16x16 //$NON-NLS-1$
	final static String OVR = ICONS_PATH + "ovr16/"; //overlays //$NON-NLS-1$
	final static String DLCL = ICONS_PATH + "dlcl16/"; //disabled icons //$NON-NLS-1$
	
	/**
	 * Declare all images
	 */
	private static void declareImages() {
		//ELCL
		declareRegistryImage(ISharedImages.IMG_SCRIPT, ELCL + "script.gif"); //$NON-NLS-1$
		declareRegistryImage(ISharedImages.IMG_RHINO, ELCL + "rhino.gif"); //$NON-NLS-1$
		declareRegistryImage(ISharedImages.IMG_MAIN_TAB, ELCL + "main_tab.gif"); //$NON-NLS-1$
		declareRegistryImage(ISharedImages.IMG_LIBRARY, ELCL + "library.gif"); //$NON-NLS-1$
	}
	
	/**
	 * Declare an Image in the registry table.
	 * @param key 	The key to use when registering the image
	 * @param path	The path where the image can be found. This path is relative to where
	 *				this plugin class is found (i.e. typically the packages directory)
	 */
	private final static void declareRegistryImage(String key, String path) {
		ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
		Bundle bundle = Platform.getBundle(RhinoUIPlugin.PLUGIN_ID);
		URL url = null;
		if (bundle != null){
			url = FileLocator.find(bundle, new Path(path), null);
			if(url != null) {
				desc = ImageDescriptor.createFromURL(url);
			}
		}
		imageRegistry.put(key, desc);
	}
	
	/**
	 * Returns the shared image for the given key or <code>null</code>.
	 * @param key
	 * @return the requested image or <code>null</code> if the image does not exist
	 */
	public static Image getSharedImage(String key) {
		if (imageRegistry == null) {
			initializeImageRegistry();
		}
		return imageRegistry.get(key);
	}
	
	/**
	 * Initializes the registry if it has not been already
	 * @return the initialized registry
	 */
	private synchronized static ImageRegistry initializeImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry(PlatformUI.getWorkbench().getDisplay());
			declareImages();
		}
		return imageRegistry;
	}
	
	/**
	 * Creates the image from the descriptor and caches it for proper disposal
	 * @param descriptor
	 * @return
	 */
	public static Image getImage(ImageDescriptor descriptor) {
		if(descRegistry == null) {
			descRegistry = new ImageDescriptorRegistry();
		}
		return descRegistry.get(descriptor);
	}
	
	/**
	 * Disposes the image registry
	 */
	public static void dispose() {
		if(imageRegistry != null) {
			imageRegistry.dispose();
		}
		if(descRegistry != null) {
			descRegistry.dispose();
		}
	}
	
	/**
	 * Constructor
	 */
	private RhinoImageRegistry() {
		// no direct instantiation
	}
}
