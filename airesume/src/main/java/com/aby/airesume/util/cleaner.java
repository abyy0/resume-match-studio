package com.aby.airesume.util;

public class cleaner {

    public static String clean(String text) {
        if (text == null)
            return "";

        return text
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
