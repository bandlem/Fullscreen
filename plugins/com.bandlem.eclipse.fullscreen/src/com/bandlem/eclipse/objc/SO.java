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
package com.bandlem.eclipse.objc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.internal.cocoa.OS;
import org.eclipse.swt.internal.cocoa.id;

/**
 * Handles some of the Objective-C code in a architecture neutral way.
 * 
 * Since SWT is compiled for 32-bit or 64-bit access separately (and types like
 * {@link id} correspond to NSUInteger, which is 32-bit on a 32-bit system and
 * 64-bit on a 64-bit system), we can't just use the same parameter for all
 * method invocations.
 * 
 * This front-end to the Objective-C runtime uses <code>long</code> for
 * everything, and maps it to <code>int</code> when running on a 32-bit system.
 * As a result, code can call this and not have to worry about the distinction
 * between the two.
 * 
 * It also allows access to selectors not normally available in SWT, via the
 * {@link #selector(String)} call. For example, invoking
 * <code>selector("toggleFullScreen:")</code> allows that method to be called
 * even if {@link OS} doesn't provide a handle to that natively, which it
 * doesn't on at least Eclipse 3.6 and Eclipse 3.7 systems.
 * 
 * Finally, since a lot of this uses reflection, it also provides some helper
 * methods to get a field to make the reflection API a little more sane.
 * 
 * @author Alex Blewitt <alex.blewit@gmail.com>
 */
@SuppressWarnings({ "rawtypes", "restriction" })
public class SO {
	/**
	 * Subset of helpers for reflective access to methods
	 */
	public static class Reflect {

		/**
		 * Executes a method, which returns a <code>long</code> (compatible)
		 * value.
		 * 
		 * @param target
		 *            the instance to execute against (if it is a Class, will be
		 *            static access)
		 * @param method
		 *            the method name
		 * @param types
		 *            the list of argument types (if any)
		 * @param args
		 *            the list of arguments (if any)
		 * @throws RuntimeException
		 *             if any reflection invocation method occurs.
		 * @return the long return value
		 */
		public static long executeLong(Object target, String method,
				Object... args) {
			Class types[] = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				types[i] = args[i].getClass();
			}
			return executeLong(target, method, types, args);
		}

		/**
		 * Executes a method, which returns a <code>long</code> (compatible)
		 * value.
		 * 
		 * @param target
		 *            the instance to execute against (if it is a Class, will be
		 *            static access)
		 * @param method
		 *            the method name
		 * @param types
		 *            the list of argument types (if any)
		 * @param args
		 *            the list of arguments (if any)
		 * @throws RuntimeException
		 *             if any reflection invocation method occurs.
		 * @return the long return value
		 */
		@SuppressWarnings("unchecked")
		public static long executeLong(Object target, String method,
				Class[] types, Object... args) {
			Class clazz = (Class) (target instanceof Class ? target : target
					.getClass());
			Object[] newArgs;
			try {
				if (NSUInteger == Long.TYPE) {
					newArgs = args;
				} else {
					newArgs = new Object[args.length];
					for (int i = 0; i < args.length; i++) {
						newArgs[i] = new Integer(((Number) args[i]).intValue());
					}
				}
				Method m = clazz.getMethod(method, types);
				Number n = ((Number) m.invoke(target, newArgs));
				return n == null ? -1 : n.longValue();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * Gets a field from the object, which can either be a Class (for static
		 * access) or an instance
		 * 
		 * @param target
		 *            the object/class to obtain the field from
		 * @param field
		 *            the field name
		 * @throws RuntimeException
		 *             if any problems occur with reflection
		 * @return the result
		 */
		public static Object getField(Object target, String field) {
			try {
				return (target instanceof Class ? (Class) target : target
						.getClass()).getField(field).get(target);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	}

	/**
	 * This represents the corresponding Java primitive type of NSUInteger, and
	 * can be passed in methods that perform lookups via reflection.
	 * 
	 * On a 32-bit system, this will have the value <code>Integer.TYPE</code>
	 * whilst on a 64-bit system it will have the value <code>Long.TYPE</code>.
	 * 
	 * This is dynamically determined based on the type of {@link OS#class_NSObjectl}
	 * compiled into the SWT library.
	 */
	public static final Class NSUInteger = Reflect.getField(OS.class,
			"class_NSObject").getClass() == Long.class ? Long.TYPE : Integer.TYPE;

	/**
	 * Private cache of selectors-to-ids.
	 */
	private static Map<String, Number> selectors = new HashMap<String, Number>();

	/**
	 * Gets the id associated with an object of type id (base Cocoa SWT class).
	 * 
	 * @param id
	 *            the object
	 * @return the long value
	 */
	public static long getID(id id) {
		return ((Number) Reflect.getField(id, "id")).longValue();
	}

	public static void objc_msgSend(long target, long sel, long arg) {
		Reflect.executeLong(OS.class, "objc_msgSend", new Class[] { NSUInteger,
				NSUInteger, NSUInteger }, target, sel, arg);
	}

	public static long selector(String sel) {
		try {
			Number selector = selectors.get(sel);
			if (selector == null) {
				selector = (Number) (OS.class.getMethod("sel_registerName",
						String.class).invoke(null, sel));
				selectors.put(sel, selector);
			}
			return selector.longValue();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
