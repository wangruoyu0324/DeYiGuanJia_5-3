/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.chuxinbuer.deyiguanjia.database;

public class LogFactory {
	private static final String TAG = "CommonLog";
	private static CommonLog log = null;

	public static CommonLog createLog(String tag) {
		CommonLog newlog = new CommonLog();

		newlog.setTag(tag);
		return newlog;
	}
	public static CommonLog createLog() {
		if (log == null) {
    		log = new CommonLog();
		}
		
		log.setTag(TAG);
		return log;
	}
	
}