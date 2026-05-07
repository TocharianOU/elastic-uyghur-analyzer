/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.tocharian.uyghur.morphology.utils;

import java.util.Set;
import java.util.HashSet;

/**
 * 维吾尔语字符处理工具类
 * 提供元音分类、字符检测等功能
 */
public class UyghurCharacterUtils {
    
    // 前元音
    private static final Set<Character> FRONT_VOWELS = Set.of(
        'ې', 'ە', 'ى', 'ۈ', 'ۆ'
    );
    
    // 后元音
    private static final Set<Character> BACK_VOWELS = Set.of(
        'ا', 'و', 'ۇ'
    );
    
    // 所有元音
    private static final Set<Character> ALL_VOWELS = new HashSet<>();
    static {
        ALL_VOWELS.addAll(FRONT_VOWELS);
        ALL_VOWELS.addAll(BACK_VOWELS);
    }
    
    // 常见辅音
    private static final Set<Character> CONSONANTS = Set.of(
        'ب', 'پ', 'ت', 'ج', 'چ', 'خ', 'د', 'ر', 'ز', 'ژ', 'س', 'ش', 
        'غ', 'ف', 'ق', 'ك', 'گ', 'ل', 'م', 'ن', 'ڭ', 'ھ', 'ۋ', 'ي'
    );
    
    /**
     * 判断字符是否为前元音
     */
    public static boolean isFrontVowel(char c) {
        return FRONT_VOWELS.contains(c);
    }
    
    /**
     * 判断字符是否为后元音
     */
    public static boolean isBackVowel(char c) {
        return BACK_VOWELS.contains(c);
    }
    
    /**
     * 判断字符是否为元音
     */
    public static boolean isVowel(char c) {
        return ALL_VOWELS.contains(c);
    }
    
    /**
     * 判断字符是否为辅音
     */
    public static boolean isConsonant(char c) {
        return CONSONANTS.contains(c);
    }
    
    /**
     * 获取字符串中的最后一个元音
     */
    public static char getLastVowel(String text) {
        for (int i = text.length() - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (isVowel(c)) {
                return c;
            }
        }
        return '\0'; // 未找到元音
    }
    
    /**
     * 获取字符串中的第一个元音
     */
    public static char getFirstVowel(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isVowel(c)) {
                return c;
            }
        }
        return '\0'; // 未找到元音
    }
    
    /**
     * 检查两个元音是否符合和谐律
     * 前元音与前元音和谐，后元音与后元音和谐
     */
    public static boolean isVowelHarmony(char vowel1, char vowel2) {
        if (vowel1 == '\0' || vowel2 == '\0') {
            return true; // 如果有一个不是元音，不进行和谐检查
        }
        
        return (isFrontVowel(vowel1) && isFrontVowel(vowel2)) ||
               (isBackVowel(vowel1) && isBackVowel(vowel2));
    }
    
    /**
     * 检查字符串是否包含维吾尔语字符
     */
    public static boolean containsUyghurCharacters(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isVowel(c) || isConsonant(c)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 清理和标准化维吾尔语文本
     */
    public static String normalizeText(String text) {
        if (text == null) {
            return "";
        }

        StringBuilder normalized = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\u0640' || c == '\u200C' || c == '\u200D') {
                continue;
            }

            normalized.append(normalizeDigit(c));
        }

        return normalized.toString().trim().replaceAll("\\s+", " ");
    }

    private static char normalizeDigit(char c) {
        if (c >= '\u0660' && c <= '\u0669') {
            return (char) ('0' + c - '\u0660');
        }

        if (c >= '\u06F0' && c <= '\u06F9') {
            return (char) ('0' + c - '\u06F0');
        }

        return c;
    }
    
    /**
     * 检查是否可能是元音弱化
     * 如 ە→ى, ا→ى
     */
    public static boolean isPossibleVowelWeakening(char original, char weakened) {
        // ە (e) → ى (i)
        if (original == 'ە' && weakened == 'ى') {
            return true;
        }
        // ا (a) → ى (i) 
        if (original == 'ا' && weakened == 'ى') {
            return true;
        }
        return false;
    }
    
    /**
     * 获取元音类型（前元音或后元音）
     */
    public static String getVowelType(char vowel) {
        if (isFrontVowel(vowel)) {
            return "front";
        } else if (isBackVowel(vowel)) {
            return "back";
        } else {
            return "unknown";
        }
    }
} 