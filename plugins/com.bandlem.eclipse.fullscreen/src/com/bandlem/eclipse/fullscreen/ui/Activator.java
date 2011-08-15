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

import java.lang.reflect.Method;

import org.eclipse.swt.internal.cocoa.NSWindow;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Set all existing windows to have full-screen behaviour, and also permit new
 * windows to be registered with same as well.
 */
@SuppressWarnings("restriction")
public class Activator extends AbstractUIPlugin implements IWindowListener {

	private static final String METHOD_NAME = "setCollectionBehavior";

	public static class Startup implements Runnable, IStartup {

		private IWorkbenchWindow[] windows;

		@Override
		public void earlyStartup() {
			IWorkbench workbench = PlatformUI.getWorkbench();
			windows = workbench
					.getWorkbenchWindows();
			workbench.getDisplay().asyncExec(this);
		}

		@Override
		public void run() {
			for (int i = 0; i < windows.length; i++) {
				IWorkbenchWindow iWorkbenchWindow = windows[i];
				setWindowFullscreen(iWorkbenchWindow.getShell());
			}
		}
	}

	// The plug-in ID
	public static final String PLUGIN_ID = "com.bandlem.eclipse.fullscreen.ui"; //$NON-NLS-1$

	private static Method setCollectionBehavior;

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
	@SuppressWarnings("rawtypes")
	public void start(BundleContext context) throws Exception {
		super.start(context);
		String className = "org.eclipse.swt.internal.cocoa.NSWindow";
		Class clazz = Class.forName(className); // throws a
												// ClassNotFoundException if
												// running on the wrong platform
												// and doesn't start
		setCollectionBehavior = getMethod(clazz, METHOD_NAME, Long.TYPE);
		if (setCollectionBehavior == null)
			setCollectionBehavior = getMethod(clazz, METHOD_NAME, Integer.TYPE);
		if (setCollectionBehavior == null)
			throw new IllegalArgumentException("Cannot find method "
					+ METHOD_NAME + " in " + clazz);
		PlatformUI.getWorkbench().addWindowListener(this);
	}

	/**
	 * Gets the method from the class with the given method name and (single)
	 * argument
	 * 
	 * @param clazz the class to extract the method from
	 * @param methodName the method to extract
	 * @param types the types of the method argument
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Method getMethod(Class clazz, String methodName, Class... types) {
		try {
			return clazz.getMethod(methodName, types);
		} catch (Throwable t) {
			return null;
		}
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

	private static void setWindowFullscreen(final Shell shell) {
		try {
			NSWindow nswindow = shell.view.window();
			nswindow.setToolbar(null);
			setCollectionBehavior.invoke(nswindow, (int) (1 << 7));
		} catch (Throwable t) {
			// ignore, not applicable for this platform
		}
	}
}
