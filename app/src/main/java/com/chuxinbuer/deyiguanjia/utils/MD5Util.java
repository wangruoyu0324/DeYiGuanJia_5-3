/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.chuxinbuer.deyiguanjia.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

/** 实现了MD5工具，使用md5算法完成加密 */

public final class MD5Util {
	private static final char hexDigits[] = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	public final static String getMessageDigest(byte[] buffer) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(buffer);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}
	/** 对文件使用md5算法完成加密 */
	public static String encode(File file) {
		FileInputStream in = null;
		MessageDigest md5 = null;
		try {
			in = new FileInputStream(file);
			FileChannel ch = in.getChannel();
			MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY,
					0, file.length());
			md5 = MessageDigest.getInstance("MD5");
			md5.update(byteBuffer);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return toHex(md5.digest());
	}

	/** 对字符串使用md5算法完成加密 */
	public static String encode(String arg) {
		if (arg == null) {
			arg = "";
		}
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
			md5.update(arg.getBytes("utf-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toHex(md5.digest());
	}

	private static String toHex(byte[] bytes) {
		StringBuffer str = new StringBuffer(32);
		for (byte b : bytes) {
			str.append(hexDigits[(b & 0xf0) >> 4]);
			str.append(hexDigits[(b & 0x0f)]);
		}
		return str.toString();
	}

	public static String md5_3(String arg) {
		if (arg == null) {
			arg = "";
		}
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
			byte[] a = md5.digest(arg.getBytes("utf-8"));
			a = md5.digest(a);
			md5.update(a);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toHex(md5.digest());
	}

	public static String getMD5String(String str) {
		return getMD5String(str.getBytes());
	}

	/**
	 * MD5加密以byte数组表示的字符串
	 * 
	 * @param bytes
	 *            目标byte数组
	 * @return MD5加密后的字符串
	 */
	public static String getMD5String(byte[] bytes) {

		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
			md5.update(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toHex(md5.digest());
	}
}