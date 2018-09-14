package org.apache.cordova.plugin.api.xforothersdk.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lu on 15-12-2.
 */
public class ProviderData {
    /**
     * 所有要使用{@link ProviderData}中的接口T obj必须实现此接口，否则会抛出RuntimeException
     */
    public interface IProviderData {

    }

    private static final String FILED_TYPE_STRING = "java.lang.String";
    private static final String FILED_TYPE_INT = "int";
    private static final String FILED_TYPE_INT_CLASS = "java.lang.Integer";
    private static final String FILED_TYPE_LONG = "long";
    private static final String FILED_TYPE_LONG_CLASS = "java.lang.Long";
    private static final String FILED_TYPE_FLOAT = "float";
    private static final String FILED_TYPE_FLOAT_CLASS = "java.lang.Float";
    private static final String FILED_TYPE_DOUBLE = "double";
    private static final String FILED_TYPE_DOUBLE_CLASS = "java.lang.Double";
    private static final String FILED_TYPE_BOOLEAN = "boolean";
    private static final String FILED_TYPE_BOOLEAN_CLASS = "java.lang.Boolean";


    private static final String VALUECOLUMN_NAME = "value";
    private static final String[] VALUECOLUMNS = new String[]{VALUECOLUMN_NAME};

    public static Cursor toValueCursor(String value) {
        Object[] v = new String[]{value};
        MatrixCursor cur = new MatrixCursor(VALUECOLUMNS);
        cur.moveToFirst();
        cur.addRow(v);
        return cur;
    }

    public static String getValueCursorValue(Cursor c) {
        c.moveToFirst();
        int columnIndex = c.getColumnIndex(VALUECOLUMN_NAME);
        if (columnIndex < 0)
            return null;
        return c.getString(columnIndex);
    }

    public static List<String> valuesFromValuesCursor(Cursor c) {
        List<String> values = new ArrayList<String>();
        int columnIndex = c.getColumnIndex(VALUECOLUMN_NAME);
        if (columnIndex < 0)
            return values;
        if (c.moveToFirst()) {
            do {
                values.add(c.getString(columnIndex));
            } while (c.moveToNext());
        }
        return values;
    }

    /**
     * @param list  需要转成Cursor的List对象
     * @param clazz 实现了{@link IProviderData}的class
     * @param <T>   实现了{@link IProviderData}的模板T
     * @return 转换成功的Cursor 如果失败会抛出RuntimeException
     */
    public static <T> Cursor listToCursor(List<T> list, Class<T> clazz) {
//        if (!IProviderData.class.isAssignableFrom(clazz))
//            throw new RuntimeException("The class " + clazz.getName() + " should implement IProviderData!!!");
        try {
            String[] columns = getClazzColumns(clazz);
            List<Object[]> values = new ArrayList<Object[]>();
            for (T t : list)
                values.add(getObjectValues(t, clazz));

            MatrixCursor cur = new MatrixCursor(columns);
            cur.moveToFirst();
            for (Object[] _value : values)
                cur.addRow(_value);
            return cur;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> boolean isDefaultType(Class<T> clazz) {
        String clazzName = clazz.getSimpleName().toLowerCase();
        if (FILED_TYPE_STRING.toLowerCase().contains(clazzName) ||
                FILED_TYPE_INT_CLASS.toLowerCase().contains(clazzName) ||
                FILED_TYPE_LONG_CLASS.toLowerCase().contains(clazzName) ||
                FILED_TYPE_FLOAT_CLASS.toLowerCase().contains(clazzName) ||
                FILED_TYPE_DOUBLE_CLASS.toLowerCase().contains(clazzName) ||
                FILED_TYPE_BOOLEAN_CLASS.toLowerCase().contains(clazzName))
            return true;
        return false;
    }

    private static <T> String[] getClazzColumns(Class<T> clazz) {
        List<String> columns = new ArrayList<String>();
        if (isDefaultType(clazz)) {
            columns.add("value");
        } else {
            Class<?> _clazz = clazz;
            do {
                Field[] fields = _clazz.getDeclaredFields();
                for (Field field : fields) {
                    if ((field.getModifiers() & Modifier.PRIVATE) == 0)
                        continue;
                    if ((field.getModifiers() & Modifier.STATIC) != 0)
                        continue;
                    field.setAccessible(true);
                    String field_name = field.getName();
                    columns.add(field_name);
                }
                _clazz = _clazz.getSuperclass();
            } while (_clazz != null && IProviderData.class.isAssignableFrom(_clazz));
        }
        String[] _columns = new String[columns.size()];
        _columns = columns.toArray(_columns);
        return _columns;
    }

    private static <T> Object[] getObjectValues(T t, Class<T> clazz) {
        List<String> values = new ArrayList<String>();
        if (isDefaultType(clazz)) {
            values.add(t.toString());
        } else {
            Class<?> _clazz = clazz;
            do {
                Field[] fields = _clazz.getDeclaredFields();
                for (Field field : fields) {
                    if ((field.getModifiers() & Modifier.PRIVATE) == 0)
                        continue;
                    if ((field.getModifiers() & Modifier.STATIC) != 0)
                        continue;
                    if ((field.getModifiers() & Modifier.TRANSIENT) != 0)
                        continue;

                    field.setAccessible(true);
                    String field_type = field.getType().toString();
                    try {
                        if (field_type.contains(FILED_TYPE_STRING)) {
                            values.add((String) field.get(t));
                        } else if (field_type.contains(FILED_TYPE_LONG) || field_type.contains(FILED_TYPE_LONG_CLASS)) {
                            values.add(String.valueOf(field.get(t)));
                        } else if (field_type.contains(FILED_TYPE_DOUBLE) || field_type.contains(FILED_TYPE_DOUBLE_CLASS)) {
                            values.add(String.valueOf(field.get(t)));
                        } else if (field_type.contains(FILED_TYPE_INT) || field_type.contains(FILED_TYPE_INT_CLASS)) {
                            values.add(String.valueOf(field.get(t)));
                        } else if (field_type.contains(FILED_TYPE_FLOAT) || field_type.contains(FILED_TYPE_FLOAT_CLASS)) {
                            values.add(String.valueOf(field.get(t)));
                        } else if (field_type.contains(FILED_TYPE_BOOLEAN) || field_type.contains(FILED_TYPE_BOOLEAN_CLASS)) {
                            values.add(String.valueOf(field.get(t)));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                _clazz = _clazz.getSuperclass();
            } while (_clazz != null && IProviderData.class.isAssignableFrom(_clazz));
        }
        Object[] _values = new Object[values.size()];
        _values = values.toArray(_values);
        return _values;
    }

    /**
     * @param c     需要转换成List的Cursor对象
     * @param clazz 实现了{@link IProviderData}的class
     * @param <T>   实现了{@link IProviderData}的模板T
     * @return 转换成功的List 如果失败会抛出RuntimeException
     */
    public static <T> List<T> listFromCursor(Cursor c, Class<T> clazz) {
        if (!IProviderData.class.isAssignableFrom(clazz) && !isDefaultType(clazz))
            throw new RuntimeException("The class " + clazz.getName() + " should implement IProviderData!!!");
        List<T> list = new ArrayList<T>();
        if (c != null && c.getCount() > 0 && c.moveToFirst()) {
            do {
                T t = null;
                try {
                    t = getClassFields(c, clazz);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (t != null)
                    list.add(t);
            } while (c.moveToNext());
            return list;
        }
        return list;
    }

    /**
     * @param c     需要转换成T的Cursor对象
     * @param clazz 实现了{@link IProviderData}的class
     * @param <T>   实现了{@link IProviderData}的模板T
     * @return 转换成功的T对象 如果失败会抛出RuntimeException
     */
    public static <T> T objectFromCursor(Cursor c, Class<T> clazz) {
        if (!IProviderData.class.isAssignableFrom(clazz) && !isDefaultType(clazz))
            throw new RuntimeException("The class " + clazz.getName() + " should implement IProviderData!!!");
        try {
            if (c.moveToFirst()) {
                return getClassFields(c, clazz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static <T> T getClassFields(Cursor c, Class<T> clazz) throws Exception {
        T t = null;
        if (isDefaultType(clazz)) {
            String value = c.getString(c.getColumnIndex("value"));
            String clazzName = clazz.getSimpleName().toLowerCase();
            if (FILED_TYPE_STRING.toLowerCase().contains(clazzName))
                t = (T) value;
            if (FILED_TYPE_INT_CLASS.toLowerCase().contains(clazzName))
                t = (T) Integer.valueOf(value);
            if (FILED_TYPE_LONG_CLASS.toLowerCase().contains(clazzName))
                t = (T) Long.valueOf(value);
            if (FILED_TYPE_FLOAT_CLASS.toLowerCase().contains(clazzName))
                t = (T) Float.valueOf(value);
            if (FILED_TYPE_DOUBLE_CLASS.toLowerCase().contains(clazzName))
                t = (T) Double.valueOf(value);
            if (FILED_TYPE_BOOLEAN_CLASS.toLowerCase().contains(clazzName))
                t = (T) Boolean.valueOf(value);
        } else {
            t = clazz.newInstance();
            Class<?> _clazz = clazz;
            do {
                Field[] fields = _clazz.getDeclaredFields();
                for (Field field : fields) {// --for() begin
                    if ((field.getModifiers() & Modifier.PRIVATE) == 0)
                        continue;
                    if ((field.getModifiers() & Modifier.STATIC) != 0)
                        continue;
                    if ((field.getModifiers() & Modifier.TRANSIENT) != 0)
                        continue;
                    field.setAccessible(true);
                    String field_name = field.getName().toLowerCase();
                    String field_type = field.getType().toString();
                    int columnIndex = c.getColumnIndex(field_name);
                    if (columnIndex < 0)
                        continue;
                    try {
                        if (field_type.contains(FILED_TYPE_STRING)) {
                            field.set(t, c.getString(columnIndex));
                        } else if (field_type.contains(FILED_TYPE_LONG) || field_type.contains(FILED_TYPE_LONG_CLASS)) {
                            field.set(t, c.getLong(columnIndex));
                        } else if (field_type.contains(FILED_TYPE_DOUBLE) || field_type.contains(FILED_TYPE_DOUBLE_CLASS)) {
                            field.set(t, c.getDouble(columnIndex));
                        } else if (field_type.contains(FILED_TYPE_INT) || field_type.contains(FILED_TYPE_INT_CLASS)) {
                            field.set(t, c.getInt(columnIndex));
                        } else if (field_type.contains(FILED_TYPE_FLOAT) || field_type.contains(FILED_TYPE_FLOAT_CLASS)) {
                            field.set(t, c.getFloat(columnIndex));
                        } else if (field_type.contains(FILED_TYPE_BOOLEAN) || field_type.contains(FILED_TYPE_BOOLEAN_CLASS)) {
                            field.set(t, Boolean.valueOf(c.getString(columnIndex)));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                _clazz = _clazz.getSuperclass();
            } while (_clazz != null && IProviderData.class.isAssignableFrom(_clazz));
        }
        return t;
    }

    /**
     * @param t     需要转换成Cursor的T对象
     * @param clazz 实现了{@link IProviderData}的class
     * @param <T>   实现了{@link IProviderData}的模板T
     * @return 转换成功的Cursor对象 如果失败会抛出RuntimeException
     */
    public static <T> Cursor objectToCursor(T t, Class<T> clazz) {
        if (!IProviderData.class.isAssignableFrom(clazz) && !isDefaultType(clazz))
            throw new RuntimeException("The class " + clazz.getName() + " should implement IProviderData!!!");
        try {
            String[] columns = getClazzColumns(clazz);
            Object[] values = getObjectValues(t, clazz);
            MatrixCursor cur = new MatrixCursor(columns);
            cur.moveToFirst();
            cur.addRow(values);
            return cur;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param obj 需要转换成ContentValues obj，此obj需要实现{@link IProviderData}
     * @return 转换成功的ContentValues， 如果失败会抛出RuntimeException
     */
    public static ContentValues toContentValues(Object obj) {
        if (!IProviderData.class.isInstance(obj))
            throw new RuntimeException("The class " + obj.getClass().getName() + " should implement IProviderData!!!");
        try {
            Class<?> clazz = obj.getClass();
            ContentValues cv = new ContentValues();
            do {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {// --for() begin
                    if ((field.getModifiers() & Modifier.PRIVATE) == 0)
                        continue;
                    if ((field.getModifiers() & Modifier.STATIC) != 0)
                        continue;
                    if ((field.getModifiers() & Modifier.TRANSIENT) != 0)
                        continue;
                    field.setAccessible(true);
                    String field_name = field.getName();
                    String field_type = field.getType().toString();
                    try {
                        if (field_type.contains(FILED_TYPE_STRING)) {
                            cv.put(field_name, (String) field.get(obj));
                        } else if (field_type.contains(FILED_TYPE_LONG) || field_type.contains(FILED_TYPE_LONG_CLASS)) {
                            cv.put(field_name, (Long) field.get(obj));
                        } else if (field_type.contains(FILED_TYPE_DOUBLE) || field_type.contains(FILED_TYPE_DOUBLE_CLASS)) {
                            cv.put(field_name, (Double) field.get(obj));
                        } else if (field_type.contains(FILED_TYPE_INT) || field_type.contains(FILED_TYPE_INT_CLASS)) {
                            cv.put(field_name, (Integer) field.get(obj));
                        } else if (field_type.contains(FILED_TYPE_FLOAT) || field_type.contains(FILED_TYPE_FLOAT_CLASS)) {
                            cv.put(field_name, (Float) field.get(obj));
                        } else if (field_type.contains(FILED_TYPE_BOOLEAN) || field_type.contains(FILED_TYPE_BOOLEAN_CLASS)) {
                            cv.put(field_name, (Boolean) field.get(obj));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                clazz = clazz.getSuperclass();
            } while (clazz != null && IProviderData.class.isAssignableFrom(clazz));
            return cv;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static <T> T fromContentValues(ContentValues cv, Class<T> clazz) {
        if (!IProviderData.class.isAssignableFrom(clazz))
            throw new RuntimeException("The class " + clazz.getName() + " should implement IProviderData!!!");
        try {
            T t = clazz.newInstance();

            Class<?> _clazz = clazz;
            do {
                Field[] fields = _clazz.getDeclaredFields();
                for (Field field : fields) {// --for() begin
                    if ((field.getModifiers() & Modifier.PRIVATE) == 0)
                        continue;
                    if ((field.getModifiers() & Modifier.STATIC) != 0)
                        continue;
                    if ((field.getModifiers() & Modifier.TRANSIENT) != 0)
                        continue;
                    field.setAccessible(true);
                    String field_name = field.getName().toLowerCase();
                    String field_type = field.getType().toString();
                    try {
                        if (field_type.contains(FILED_TYPE_STRING)) {
                            field.set(t, cv.getAsString(field_name));
                        } else if (field_type.contains(FILED_TYPE_LONG) || field_type.contains(FILED_TYPE_LONG_CLASS)) {
                            field.set(t, cv.getAsLong(field_name));
                        } else if (field_type.contains(FILED_TYPE_DOUBLE) || field_type.contains(FILED_TYPE_DOUBLE_CLASS)) {
                            field.set(t, cv.getAsDouble(field_name));
                        } else if (field_type.contains(FILED_TYPE_INT) || field_type.contains(FILED_TYPE_INT_CLASS)) {
                            field.set(t, cv.getAsInteger(field_name));
                        } else if (field_type.contains(FILED_TYPE_FLOAT) || field_type.contains(FILED_TYPE_FLOAT_CLASS)) {
                            field.set(t, cv.getAsFloat(field_name));
                        } else if (field_type.contains(FILED_TYPE_BOOLEAN) || field_type.contains(FILED_TYPE_BOOLEAN_CLASS)) {
                            field.set(t, cv.getAsBoolean(field_name));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                _clazz = _clazz.getSuperclass();
            } while (_clazz != null && IProviderData.class.isAssignableFrom(_clazz));
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
