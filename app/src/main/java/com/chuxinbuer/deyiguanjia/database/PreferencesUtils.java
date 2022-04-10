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
package com.chuxinbuer.deyiguanjia.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * SharePreferences的工具类 *
 */
public class PreferencesUtils {

    private Context mContext;
    private static PreferencesUtils preferencesUtils = null;

    private PreferencesUtils(Context mContext) {
        this.mContext = mContext;
    }

    public static PreferencesUtils getPreferencesUtilsInstance(Context mContext) {
        if (preferencesUtils == null) {
            preferencesUtils = new PreferencesUtils(mContext);
        }
        return preferencesUtils;
    }

    /**
     * * 存储单个属性 *
     *
     * @param field
     * @param sp
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private void saveField(Field field, SharedPreferences sp, Object object)
            throws IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        Class fildType = field.getType();
        if (String.class == fildType || Character.class == fildType) {
            sp.edit().putString(field.getName(), String.valueOf(field.get(object))).commit();
        } else if (Integer.TYPE == fildType || Integer.class == fildType) {
            sp.edit().putInt(field.getName(), field.getInt(object)).commit();
        } else if (boolean.class == fildType|| Boolean.TYPE == fildType) {
            sp.edit().putBoolean(field.getName(), field.getBoolean(object)).commit();
        } else if (Long.class == fildType|| Long.TYPE == fildType) {
            sp.edit().putLong(field.getName(), field.getLong(object)).commit();
        } else if (Float.class == fildType || Float.TYPE == fildType) {
            sp.edit().putFloat(field.getName(), field.getFloat(object)).commit();
        }
        // sp.edit().putString(field.getName(),
        // String.valueOf(field.get(object))).commit();
    }

    /**
     * 拿到单个属性
     *
     * @param field
     * @param sp
     * @return
     */
    private String getFieldFromSp(Field field, SharedPreferences sp) {
        field.setAccessible(true);
        Class fildType = field.getType();
        if (String.class == fildType || Character.class == fildType) {
            return sp.getString(field.getName(), "");
        } else if (Integer.TYPE == fildType || Integer.class == fildType) {
            return String.valueOf(sp.getInt(field.getName(), 0));
        } else if (Boolean.class == fildType) {
            return String.valueOf(sp.getBoolean(field.getName(), false));
        } else if (Long.class == fildType) {
            return String.valueOf(sp.getLong(field.getName(), 0L));
        } else if (Float.class == fildType || Float.TYPE == fildType) {
            return String.valueOf(sp.getFloat(field.getName(), 0F));
        } else if (Double.class == fildType || Double.TYPE == fildType) {
            return String.valueOf(sp.getFloat(field.getName(), 0F));
        }
        return "";

    }

    /**
     * 数据存储 *
     *
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public void save(Object object) throws IllegalArgumentException,
            IllegalAccessException {
        SharedPreferences sp = mContext.getSharedPreferences(object.getClass()
                .getSimpleName(), 0);// 1:read 2:write
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isFinal(field.getModifiers()))
                saveField(field, sp, object);
        }
    }

    public void clear(Object object) {
        SharedPreferences sp = mContext.getSharedPreferences(object.getClass()
                .getSimpleName(), 0);// 1:read 2:write
        Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * 获得存储对象 *
     *
     * @param classzz
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public Object getObjectFromSp(Class<?> classzz)
            throws InstantiationException, IllegalAccessException {
        Object object = classzz.newInstance();
        String name = object.getClass().getSimpleName();
        SharedPreferences sp = mContext.getSharedPreferences(
                classzz.getSimpleName(), 0);// 1:read 2:write
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                if (!Modifier.isFinal(field.getModifiers()))
                    FieldUtils.setValueToFiled(field, object,
                            getFieldFromSp(field, sp));
            } catch (IllegalArgumentException e) {
                LogFactory.createLog().e(e);
            } catch (IllegalAccessException e) {
                LogFactory.createLog().e(e);
            }
        }
        return object;
    }

    public Object getObjectFromSp(Class<?> classzz, Object object)
            throws InstantiationException, IllegalAccessException {
        String name = object.getClass().getSimpleName();
        SharedPreferences sp = mContext.getSharedPreferences(
                classzz.getSimpleName(), 0);// 1:read 2:write
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {

            try {
                if (!Modifier.isFinal(field.getModifiers()))
                    FieldUtils.setValueToFiled(field, object,
                            getFieldFromSp(field, sp));
            } catch (IllegalArgumentException e) {
                LogFactory.createLog().e(e);
            } catch (IllegalAccessException e) {
                LogFactory.createLog().e(e);
            }
        }
        return object;
    }

    /**
     *保存单个对象数据
     */
    public void saveOne(Object object, Field field)
            throws IllegalArgumentException, IllegalAccessException {
        String name = object.getClass().getSimpleName();
        SharedPreferences sp = mContext.getSharedPreferences(object.getClass()
                .getSimpleName(), 0);// 1:read 2:write
        saveField(field, sp, object);
    }

    /**
     * 获取单个对象数据
     */
    public String getOne(Object object, Field field) {
        SharedPreferences sp = mContext.getSharedPreferences(object.getClass()
                .getSimpleName(), 0);// 1:read 2:write
        return getFieldFromSp(field, sp);
    }

}
