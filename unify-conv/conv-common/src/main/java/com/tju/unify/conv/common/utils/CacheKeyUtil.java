package com.tju.unify.conv.common.utils;

import org.springframework.util.DigestUtils;

import java.util.Collection;
import java.util.stream.Collectors;

public class CacheKeyUtil {

    /**
     * 为集合生成缓存Key
     */
    public static String generateKeyForCollection(Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return "empty";
        }

        // 排序并拼接
        String sortedString = collection.stream()
                .map(Object::toString)
                .sorted()
                .collect(Collectors.joining(","));

        // 如果字符串太长，使用MD5
        if (sortedString.length() > 100) {
            return "md5:" + DigestUtils.md5DigestAsHex(sortedString.getBytes());
        }

        return sortedString;
    }

    /**
     * 为集合生成缓存Key（带前缀）
     */
    public static String generateKeyForCollection(String prefix, Collection<?> collection) {
        return prefix + ":" + generateKeyForCollection(collection);
    }
}