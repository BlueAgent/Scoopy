package scoopy.common.util;

import java.lang.reflect.Field;

public class ReflectionUtils {
    public static Field getField(Class clazz, String name) {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getField(Field f, Object instance) {
        try {
            f.setAccessible(true);
            //noinspection unchecked We want it to crash at runtime if it fails
            return (T) f.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getField(Class clazz, String name, Object instance) {
        return getField(getField(clazz, name), instance);
    }
}
