/*******************************************************************************
 * Copyright (c) 2011, Alex Blewitt.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Blewitt - initial API and implementation
 *******************************************************************************/
package com.bandlem.eclipse.fullscreen.ui;

import org.eclipse.swt.internal.cocoa.NSWindow;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Set all existing windows to have full-screen behaviour, and also permit new windows to be registered with same as well.
 */
@SuppressWarnings("restriction")
public class Activator extends AbstractUIPlugin implements IWindowListener {

	public static class Startup implements IStartup {

		@Override
		public void earlyStartup() {
			// will trigger the bundle start
		}		
	}
	
	// The plug-in ID
	public static final String PLUGIN_ID = "com.bandlem.eclipse.fullscreen.ui"; //$NON-NLS-1$

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		PlatformUI.getWorkbench().addWindowListener(this);
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		System.err.println("vvv The below messages are caused by the existing windows having setCollectionBehaviour called after startup from com.bandlem.eclipse.fullscreen.ui");
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchWindow iWorkbenchWindow = windows[i];
			// Note: this will cause some 'object leaking with no autorelease pool' messages,
			// since it's supposed to be set before the window is opened, not after.
			setWindowFullscreen(iWorkbenchWindow.getShell());
		}
		System.err.println("^^^ The above messages are caused by the existing windows having setCollectionBehaviour called after startup from com.bandlem.eclipse.fullscreen.ui");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	@Override
	public void windowActivated(IWorkbenchWindow window) {
	}

	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
	}

	@Override
	public void windowClosed(IWorkbenchWindow window) {
	}

	@Override
	public void windowOpened(IWorkbenchWindow window) {
		Shell shell = window.getShell();
		setWindowFullscreen(shell);
	}

	private void setWindowFullscreen(Shell shell) {
		if ("macosx".equals(System.getProperty("osgi.os"))
				&& "10.7".equals(System.getProperty("os.version"))) {
			NSWindow nswindow = shell.view.window();
			nswindow.setCollectionBehavior(1 << 7);
		}
	}

}
