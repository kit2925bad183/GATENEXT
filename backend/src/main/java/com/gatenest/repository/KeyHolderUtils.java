package com.gatenest.repository;

import org.springframework.jdbc.support.KeyHolder;

import java.util.Map;

public final class KeyHolderUtils {

    private KeyHolderUtils() {}

    public static Long extractId(KeyHolder keyHolder) {
        if (keyHolder == null) {
            return null;
        }

        try {
            Map<String, Object> keys = keyHolder.getKeys();
            if (keys != null && !keys.isEmpty()) {
                for (Map.Entry<String, Object> entry : keys.entrySet()) {
                    if ("id".equalsIgnoreCase(entry.getKey()) && entry.getValue() instanceof Number num) {
                        return num.longValue();
                    }
                }
                for (Object val : keys.values()) {
                    if (val instanceof Number num) {
                        return num.longValue();
                    }
                }
            }
        } catch (Exception ignored) {
        }

        if (!keyHolder.getKeyList().isEmpty()) {
            Map<String, Object> firstMap = keyHolder.getKeyList().get(0);
            for (Map.Entry<String, Object> entry : firstMap.entrySet()) {
                if ("id".equalsIgnoreCase(entry.getKey()) && entry.getValue() instanceof Number num) {
                    return num.longValue();
                }
            }
            for (Object val : firstMap.values()) {
                if (val instanceof Number num) {
                    return num.longValue();
                }
            }
        }

        try {
            Number key = keyHolder.getKey();
            return key != null ? key.longValue() : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
