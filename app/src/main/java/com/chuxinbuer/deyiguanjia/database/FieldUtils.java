/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.chuxinbuer.deyiguanjia.database;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 实体的工具类 *
 *
 *
 */
public class FieldUtils {
    /**
     * 设置属性值
     *
     * @param field
     * @param object
     * @param fieldValue
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static void setValueToFiled(Field field, Object object,
                                       String fieldValue) throws IllegalArgumentException,
            IllegalAccessException {

        field.setAccessible(true);//允许插入
        Class fieldType = field.getType();// 属性的类型

        if ((Integer.TYPE == fieldType) || (Integer.class == fieldType)) {//int
            Integer value = Integer.parseInt(fieldValue);
            field.set(object, value);
        } else if (String.class == fieldType) {//string
            field.set(object, fieldValue);
        } else if ((Long.TYPE == fieldType) || (Long.class == fieldType)) {//long
            Long value = Long.parseLong(fieldValue);
            field.set(object, value);
        } else if ((Float.TYPE == fieldType) || (Float.class == fieldType)) {//float
            Float value = Float.parseFloat(fieldValue);
            field.set(object, value);
        } else if ((Short.TYPE == fieldType) || (Short.class == fieldType)) {//short
            Short value = Short.parseShort(fieldValue);
            field.set(object, value);
        } else if ((Double.TYPE == fieldType) || (Double.class == fieldType)) {//double
            Double value = Double.parseDouble(fieldValue);
            field.set(object, value);
        } else if (Character.TYPE == fieldType) {//char
            Character value = Character.valueOf(fieldValue.charAt(0));
            field.set(object, value);
        } else if (Date.class == fieldType) {//date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date date = sdf.parse(fieldValue);
                field.set(object, date);
            } catch (Exception e) {
                LogFactory.createLog().e(e);
            }

        } else if (Boolean.TYPE == fieldType || Boolean.class == fieldType) {
            field.setBoolean(object, (Boolean.parseBoolean(String.valueOf(fieldValue))));
        }
    }

    public static void setDefValueToFiled(Field field, Object object) throws IllegalArgumentException,
            IllegalAccessException {
        field.setAccessible(true);//允许插入
        Class fieldType = field.getType();// 属性的类型

        if ((Integer.TYPE == fieldType) || (Integer.class == fieldType)) {//int
            Integer value = 0;
            field.set(object, value);
        } else if (String.class == fieldType) {//string
            field.set(object, "");
        } else if ((Long.TYPE == fieldType) || (Long.class == fieldType)) {//long
            Long value = Long.parseLong("0");
            field.set(object, value);
        } else if ((Float.TYPE == fieldType) || (Float.class == fieldType)) {//float
            Float value = Float.parseFloat("0");
            field.set(object, value);
        } else if ((Short.TYPE == fieldType) || (Short.class == fieldType)) {//short
            Short value = Short.parseShort("0");
            field.set(object, value);
        } else if ((Double.TYPE == fieldType) || (Double.class == fieldType)) {//double
            Double value = Double.parseDouble("0");
            field.set(object, value);
        } else if (Date.class == fieldType) {//date
            try {
                Date date = null;
                field.set(object, date);
            } catch (Exception e) {
                LogFactory.createLog().e(e);
            }
        } else if (Boolean.class == fieldType) {
            field.setBoolean(object, false);
        }
    }

    /**
     * 拿到指定的属性值*
     * @param field
     * @param object
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static void getValueFromField(Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);

        Class fieldType = field.getType();

        if (Integer.TYPE == fieldType || Integer.class == fieldType) {//int
            field.getInt(object);
        } else if (String.class == fieldType) {//string
            String.valueOf(field.get(object));
        } else if (Short.class == fieldType) {//short
            field.getShort(object);
        } else if (Long.class == fieldType) {//long
            field.getLong(object);
        } else if (Float.class == fieldType || Float.TYPE == fieldType) {//float
            field.getFloat(object);
        } else if (Double.class == fieldType) {//double
            field.getDouble(object);
        } else if (Character.class == fieldType) {//char
            field.getChar(object);
        } else if (Date.class == fieldType) {//date
            String.valueOf(field.get(object));
        } else if (Boolean.class == fieldType) {//boolean
            field.getBoolean(object);
        }
    }

    public static Object getFieldValue(Field field, Object object) throws Exception {
        Object temp = null;
        field.setAccessible(true);
        Class fildType = field.getType();
        if (String.class == fildType || Character.class == fildType) {
            temp = String.valueOf(field.get(object));
        } else if (Integer.TYPE == fildType || Integer.class == fildType) {
            temp = field.getInt(object);
        } else if (boolean.class == fildType) {
            temp = field.getBoolean(object);
        } else if (Long.class == fildType) {
            temp = field.getLong(object);
        } else if (Float.class == fildType || Float.TYPE == fildType) {
            temp = field.getFloat(object);
        } else if (Double.class == fildType || Double.TYPE == fildType) {
            temp = field.getDouble(object);
        }
        return temp;
    }


    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public static void getSpecificTypeOfFile(Context context, String[] extension) {
        //从外存中获取
        Uri fileUri = MediaStore.Files.getContentUri("external");
        //筛选列，这里只筛选了：文件路径和不含后缀的文件名
        String[] projection = new String[]{
                MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.TITLE
        };
        //构造筛选语句
        String selection = "";
        for (int i = 0; i < extension.length; i++) {
            if (i != 0) {
                selection = selection + " OR ";
            }
            selection = selection + MediaStore.Files.FileColumns.DATA + " LIKE '%" + extension[i] + "'";
        }
        //按时间递增顺序对结果进行排序;待会从后往前移动游标就可实现时间递减
        String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED;
        //获取内容解析器对象
        ContentResolver resolver = context.getContentResolver();
        //获取游标
        Cursor cursor = resolver.query(fileUri, projection, selection, null, sortOrder);
        if (cursor == null)
            return;
        //游标从最后开始往前递减，以此实现时间递减顺序（最近访问的文件，优先显示）
        if (cursor.moveToLast()) {
            do {
                //输出文件的完整路径
                String data = cursor.getString(0);
                Log.d("tag", data);
            } while (cursor.moveToPrevious());
        }
        cursor.close();

    }
}
