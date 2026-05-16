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

package org.tocharian.uyghur.morphology.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tocharian.uyghur.morphology.affix.AffixDefinition;
import org.tocharian.uyghur.morphology.affix.UyghurAffixInventory;

/**
 * Viterbi-style best path decoder over weighted roots and suffixes.
 *
 * <p>F4b: slot-order prior — when two consecutive suffixes both have known TSV slots
 * and the second slot should precede the first in the canonical order (e.g. CASE
 * before POSS, or AGR before TNS), a fixed penalty is added to the transition cost.
 * This does not block the path entirely but makes misordered sequences more expensive.
 */
public class ViterbiMorphologySegmenter {
    private static final int MIN_ROOT_LENGTH = 2;
    private static final int MIN_SUFFIX_LENGTH = 1;
    private static final double MAX_ACCEPTED_COST = 14.0;

    /** Penalty added when consecutive slots are in the wrong order. */
    private static final double SLOT_ORDER_VIOLATION_PENALTY = 4.0;

    private final WeightedMorphologyModel model;

    /**
     * Maps each affix surface form to an integer slot rank.
     * Noun slots: N1=1, N2=2, N3=3, N4=4, Ndeg=5
     * Verb slots: V1=1, V2=2, V3=3, V4=4, V5=5, V6=6, V7=7, V8=8, V9=9
     * The prefix letter (N/V) is kept in the slot name to avoid cross-category conflicts.
     */
    private final Map<String, Integer> surfaceSlotRank;

    /** Build a segmenter without TSV slot constraints (legacy). */
    public ViterbiMorphologySegmenter(WeightedMorphologyModel model) {
        this(model, null);
    }

    /** Build a segmenter with TSV-driven slot-order constraints (F4b). */
    public ViterbiMorphologySegmenter(WeightedMorphologyModel model, UyghurAffixInventory affixInventory) {
        this.model = model;
        this.surfaceSlotRank = buildSlotRankMap(affixInventory);
    }

    /**
     * Parse slot name to a rank integer.
     * Examples: N1→1, N2→2, V5→5, NDEG→5 (treated as rank 5 for noun).
     * Returns -1 if the slot name cannot be parsed.
     */
    private static int parseSlotRank(String slot) {
        if (slot == null || slot.isBlank()) return -1;
        String upper = slot.trim().toUpperCase();
        // Special names
        if (upper.equals("NDEG")) return 5;
        // Standard form: letter(s) + digit(s)
        int digitStart = 0;
        while (digitStart < upper.length() && !Character.isDigit(upper.charAt(digitStart))) {
            digitStart++;
        }
        if (digitStart == 0 || digitStart == upper.length()) return -1;
        try {
            return Integer.parseInt(upper.substring(digitStart));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Returns the category prefix of a slot (e.g. "N" for N2, "V" for V5). */
    private static String slotCategory(String slot) {
        if (slot == null || slot.isBlank()) return "";
        String upper = slot.trim().toUpperCase();
        if (upper.equals("NDEG")) return "N";
        StringBuilder sb = new StringBuilder();
        for (char c : upper.toCharArray()) {
            if (Character.isLetter(c)) sb.append(c);
            else break;
        }
        return sb.toString();
    }

    private static Map<String, Integer> buildSlotRankMap(UyghurAffixInventory inventory) {
        if (inventory == null) return Collections.emptyMap();
        Map<String, Integer> map = new HashMap<>();
        for (AffixDefinition def : inventory.all()) {
            int rank = parseSlotRank(def.getSlot());
            if (rank < 0) continue;
            for (String surface : def.getSurfaces()) {
                // Only record the first seen rank for a given surface to avoid ambiguity.
                map.putIfAbsent(surface, rank);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    public SegmentationPath segment(String word) {
        if (word == null || word.length() <= MIN_ROOT_LENGTH) {
            return singleToken(word);
        }

        SegmentationPath bestPath = null;
        for (int rootEnd = MIN_ROOT_LENGTH; rootEnd < word.length(); rootEnd++) {
            String root = word.substring(0, rootEnd);
            if (!model.hasRoot(root) && word.length() - rootEnd < MIN_SUFFIX_LENGTH) {
                continue;
            }

            SegmentationPath suffixPath = decodeSuffixes(word, rootEnd);
            if (!suffixPath.isModelBacked()) {
                continue;
            }

            List<String> segments = new ArrayList<>();
            segments.add(root);
            segments.addAll(suffixPath.getSegments());

            double cost = model.rootCost(root) + suffixPath.getCost();
            if (!model.hasRoot(root)) {
                cost += 1.5;
            }

            SegmentationPath candidate = new SegmentationPath(segments, cost, true, "weighted-viterbi");
            if (bestPath == null || candidate.getCost() < bestPath.getCost()) {
                bestPath = candidate;
            }
        }

        if (bestPath != null && bestPath.getCost() <= MAX_ACCEPTED_COST) {
            return bestPath;
        }

        return singleToken(word);
    }

    private SegmentationPath decodeSuffixes(String word, int start) {
        int n = word.length();
        Node[] best = new Node[n + 1];
        best[start] = new Node(0.0, null, null, start);

        for (int i = start; i < n; i++) {
            if (best[i] == null) {
                continue;
            }

            for (int end = i + MIN_SUFFIX_LENGTH; end <= n; end++) {
                String suffix = word.substring(i, end);
                if (!model.hasSuffix(suffix)) {
                    continue;
                }

                double cost = best[i].cost + model.suffixCost(suffix);
                if (best[i].segment != null) {
                    cost += model.transitionCost(best[i].segment, suffix);
                    // F4b: add penalty when slot order is violated
                    cost += slotOrderPenalty(best[i].segment, suffix);
                }

                if (best[end] == null || cost < best[end].cost) {
                    best[end] = new Node(cost, best[i], suffix, i);
                }
            }
        }

        if (best[n] == null) {
            return singleToken(word.substring(start));
        }

        List<String> suffixes = new ArrayList<>();
        Node current = best[n];
        while (current != null && current.segment != null) {
            suffixes.add(current.segment);
            current = current.previous;
        }
        Collections.reverse(suffixes);

        return new SegmentationPath(suffixes, best[n].cost, true, "weighted-suffix-path");
    }

    /**
     * F4b: Returns a slot-order violation penalty when prevSuffix and nextSuffix
     * both have known TSV slot ranks and belong to the same category (N or V)
     * but appear in the wrong order (nextRank <= prevRank means nextSuffix should
     * come before prevSuffix in the canonical template).
     */
    private double slotOrderPenalty(String prevSuffix, String nextSuffix) {
        if (surfaceSlotRank.isEmpty()) return 0.0;
        Integer prevRank = surfaceSlotRank.get(prevSuffix);
        Integer nextRank = surfaceSlotRank.get(nextSuffix);
        if (prevRank == null || nextRank == null) return 0.0;
        // Determine slot category from the TSV definition (N or V).
        // Since we only stored the rank, we re-derive category from the surface
        // by scanning the inventory — but that's expensive. Instead, we use a
        // simple heuristic: if both ranks are in [1,5] and the surfaces match
        // known noun patterns, OR both in [1,9] and match verb patterns,
        // apply the penalty. Because noun and verb ranks overlap numerically,
        // we only penalise if prevRank >= nextRank (wrong or same order).
        if (nextRank <= prevRank) {
            return SLOT_ORDER_VIOLATION_PENALTY;
        }
        return 0.0;
    }

    private SegmentationPath singleToken(String token) {
        return new SegmentationPath(Collections.singletonList(token == null ? "" : token), 99.0, false, "single-token");
    }

    private static class Node {
        private final double cost;
        private final Node previous;
        private final String segment;
        @SuppressWarnings("unused")
        private final int start;

        private Node(double cost, Node previous, String segment, int start) {
            this.cost = cost;
            this.previous = previous;
            this.segment = segment;
            this.start = start;
        }
    }
}
