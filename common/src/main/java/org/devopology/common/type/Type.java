package org.devopology.common.type;

import org.devopology.common.precondition.Precondition;

public class Type {

    private Type() {
        // DO NOTHING
    }

    public static boolean isType(Class clazz, Object value) {
        Precondition.notNull(clazz, "class is null");

        if ((value != null) && (clazz.getName().equals(value.getClass().getName()))) {
            return true;
        }

        return false;
    }
}
