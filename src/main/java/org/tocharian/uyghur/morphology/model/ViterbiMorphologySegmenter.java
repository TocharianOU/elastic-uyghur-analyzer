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
import java.util.List;

/**
 * Viterbi-style best path decoder over weighted roots and suffixes.
 */
public class ViterbiMorphologySegmenter {
    private static final int MIN_ROOT_LENGTH = 2;
    private static final int MIN_SUFFIX_LENGTH = 1;
    private static final double MAX_ACCEPTED_COST = 14.0;

    private final WeightedMorphologyModel model;

    public ViterbiMorphologySegmenter(WeightedMorphologyModel model) {
        this.model = model;
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
