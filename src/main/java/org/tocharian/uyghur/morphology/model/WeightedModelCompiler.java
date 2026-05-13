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

import org.tocharian.uyghur.morphology.dictionary.UnifiedDictionaryManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds weighted decoding tables from the existing unified dictionary views.
 */
public class WeightedModelCompiler {
    public WeightedMorphologyModel compile(UnifiedDictionaryManager dictionaryManager) {
        Map<String, Integer> rootCounts = new HashMap<>();
        Map<String, Integer> suffixCounts = new HashMap<>();
        Map<String, Integer> transitionCounts = new HashMap<>();

        addSegmentations(dictionaryManager.getOriginalView(), rootCounts, suffixCounts, transitionCounts);
        addSegmentations(dictionaryManager.getSplitView(), rootCounts, suffixCounts, transitionCounts);
        addSegmentations(dictionaryManager.getCustomView(), rootCounts, suffixCounts, transitionCounts);

        Map<String, Double> rootCosts = toCosts(rootCounts);
        Map<String, Double> suffixCosts = toCosts(suffixCounts);
        Map<String, Double> transitionCosts = toCosts(transitionCounts);

        return new WeightedMorphologyModel(rootCosts, suffixCosts, transitionCosts, 4.5, 5.5);
    }

    private void addSegmentations(Map<String, String[]> entries,
                                  Map<String, Integer> rootCounts,
                                  Map<String, Integer> suffixCounts,
                                  Map<String, Integer> transitionCounts) {
        for (String[] segments : entries.values()) {
            if (segments == null || segments.length == 0) {
                continue;
            }

            increment(rootCounts, segments[0]);
            String previousSuffix = null;
            for (int i = 1; i < segments.length; i++) {
                String suffix = segments[i];
                if (suffix == null || suffix.isEmpty()) {
                    continue;
                }

                increment(suffixCounts, suffix);
                if (previousSuffix != null) {
                    increment(transitionCounts, WeightedMorphologyModel.transitionKey(previousSuffix, suffix));
                }
                previousSuffix = suffix;
            }
        }
    }

    private Map<String, Double> toCosts(Map<String, Integer> counts) {
        Map<String, Double> costs = new HashMap<>();
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        int vocabulary = Math.max(1, counts.size());

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            double probability = (entry.getValue() + 1.0) / (total + vocabulary);
            costs.put(entry.getKey(), -Math.log(probability));
        }

        return costs;
    }

    private void increment(Map<String, Integer> counts, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        counts.put(value, counts.getOrDefault(value, 0) + 1);
    }
}
