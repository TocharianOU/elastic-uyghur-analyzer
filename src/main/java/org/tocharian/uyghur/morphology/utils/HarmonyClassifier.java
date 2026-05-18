/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package org.tocharian.uyghur.morphology.utils;

import java.util.Set;

/**
 * Three-way vowel harmony classifier (Phase β).
 *
 * <p>Backed by the common three-way Uyghur vowel harmony analysis:
 * <ul>
 *   <li>Back vowels: /ɑ o u/ — ا و ۇ</li>
 *   <li>Front vowels: /æ ø y/ — ە ۆ ۈ</li>
 *   <li>Neutral / transparent vowels: /ɪ e/ — ى ې (do NOT trigger harmony)</li>
 * </ul>
 *
 * <p>Primary source: Mayer, McCollum & Major (2022) "Issues in Uyghur Phonology",
 * Language and Linguistics Compass 16, DOI:10.1111/lnc3.12478, §2.3.
 *
 * <p>This class is intentionally separate from {@link UyghurCharacterUtils} which
 * uses a legacy two-way classification (with neutral vowels misclassified as front)
 * and is still consumed by the rule-based suffix stripper. New restoration code
 * should use this class instead.
 */
public final class HarmonyClassifier {

    public enum HarmonyClass {
        /** Last non-neutral vowel is a back vowel /ɑ o u/. */
        BACK,
        /** Last non-neutral vowel is a front vowel /æ ø y/. */
        FRONT,
        /** Word contains only neutral vowels (or no vowels). Default to FRONT
         *  by community convention; see suspect list A1 for caveats. */
        NEUTRAL
    }

    private static final Set<Character> BACK_VOWELS = Set.of('ا', 'و', 'ۇ');
    private static final Set<Character> FRONT_VOWELS = Set.of('ە', 'ۆ', 'ۈ');
    private static final Set<Character> NEUTRAL_VOWELS = Set.of('ى', 'ې');

    private HarmonyClassifier() {}

    /**
     * Classify a word/stem by its last non-neutral vowel.
     *
     * <p>If the word contains only neutral vowels (e.g. ئىش, مېنىڭ), returns
     * {@link HarmonyClass#NEUTRAL}. Callers may map NEUTRAL to FRONT by default,
     * but should be aware this is a community heuristic, not a hard rule
     * (Mayer, Major & Yakup 2022 showed covert harmonic preferences exist).
     */
    public static HarmonyClass classify(String word) {
        if (word == null || word.isEmpty()) {
            return HarmonyClass.NEUTRAL;
        }
        for (int i = word.length() - 1; i >= 0; i--) {
            char c = word.charAt(i);
            if (BACK_VOWELS.contains(c)) {
                return HarmonyClass.BACK;
            }
            if (FRONT_VOWELS.contains(c)) {
                return HarmonyClass.FRONT;
            }
        }
        return HarmonyClass.NEUTRAL;
    }

    /** Convenience: returns FRONT for NEUTRAL inputs (the most common downstream
     *  default per community convention). Use {@link #classify(String)} directly
     *  if you need to distinguish NEUTRAL from FRONT. */
    public static HarmonyClass classifyWithFrontDefault(String word) {
        HarmonyClass c = classify(word);
        return c == HarmonyClass.NEUTRAL ? HarmonyClass.FRONT : c;
    }

    public static boolean isBackVowel(char c) {
        return BACK_VOWELS.contains(c);
    }

    public static boolean isFrontVowel(char c) {
        return FRONT_VOWELS.contains(c);
    }

    public static boolean isNeutralVowel(char c) {
        return NEUTRAL_VOWELS.contains(c);
    }
}
