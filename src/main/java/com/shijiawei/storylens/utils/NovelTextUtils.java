package com.shijiawei.storylens.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NovelTextUtils {

    //匹配"第X章/回/節"，X可為阿拉伯數字或中文數字，後方可接章節標題直到行尾
    private static final Pattern CHAPTER_PATTERN = Pattern.compile(
            "^\\s*第[零一二三四五六七八九十百千萬0-9]+[章回節].*$",
            Pattern.MULTILINE
    );

    private NovelTextUtils() {
    }

    /**
     * 將小說內容按章節標題拆分為Map。
     *
     * @param content 小說全文
     * @return key為章節順序(從1開始)，value為該章節完整內容(含標題行)
     */
    public static Map<Integer, String> splitChapters(String content) {
        if (content == null || content.isBlank()) {
            return Map.of();
        }

        Map<Integer, String> chapters = new LinkedHashMap<>();
        Matcher matcher = CHAPTER_PATTERN.matcher(content);

        //收集所有章節標題的起始位置
        List<Integer> positions = new ArrayList<>();
        while (matcher.find()) {
            positions.add(matcher.start());
        }

        if (positions.isEmpty()) {
            //無章節標記，整篇作為第1章
            chapters.put(1, content);
            return chapters;
        }

        //若第一個章節標題之前有內容，歸入第1章(前言/序章)
        int chapterIndex = 1;
        String preface = content.substring(0, positions.get(0)).trim();
        if (!preface.isEmpty()) {
            chapters.put(chapterIndex++, preface);
        }

        for (int i = 0; i < positions.size(); i++) {
            int start = positions.get(i);
            int end = (i + 1 < positions.size()) ? positions.get(i + 1) : content.length();
            chapters.put(chapterIndex++, content.substring(start, end).trim());
        }

        return chapters;
    }
}
