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
}
