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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Immutable weighted model compiled from dictionary segmentations.
 */
public class WeightedMorphologyModel {
    private final Map<String, Double> rootCosts;
    private final Map<String, Double> suffixCosts;
    private final Map<String, Double> transitionCosts;
    private final double unknownRootCost;
    private final double unknownSuffixCost;

    public WeightedMorphologyModel(Map<String, Double> rootCosts,
                                   Map<String, Double> suffixCosts,
                                   Map<String, Double> transitionCosts,
                                   double unknownRootCost,
                                   double unknownSuffixCost) {
        this.rootCosts = Collections.unmodifiableMap(new HashMap<>(rootCosts));
        this.suffixCosts = Collections.unmodifiableMap(new HashMap<>(suffixCosts));
        this.transitionCosts = Collections.unmodifiableMap(new HashMap<>(transitionCosts));
        this.unknownRootCost = unknownRootCost;
        this.unknownSuffixCost = unknownSuffixCost;
    }

    public boolean hasRoot(String root) {
        return rootCosts.containsKey(root);
    }

    public boolean hasSuffix(String suffix) {
        return suffixCosts.containsKey(suffix);
    }

    public double rootCost(String root) {
        return rootCosts.getOrDefault(root, unknownRootCost + lengthPenalty(root));
    }

    public double suffixCost(String suffix) {
        return suffixCosts.getOrDefault(suffix, unknownSuffixCost + lengthPenalty(suffix));
    }

    public double transitionCost(String previousSuffix, String nextSuffix) {
        return transitionCosts.getOrDefault(transitionKey(previousSuffix, nextSuffix), 1.25);
    }

    public Set<String> suffixes() {
        return suffixCosts.keySet();
    }

    public int rootCount() {
        return rootCosts.size();
    }

    public int suffixCount() {
        return suffixCosts.size();
    }

    public int transitionCount() {
        return transitionCosts.size();
    }

    public static String transitionKey(String previousSuffix, String nextSuffix) {
        return previousSuffix + "\u0000" + nextSuffix;
    }

    private double lengthPenalty(String segment) {
        if (segment == null || segment.isEmpty()) {
            return 4.0;
        }
        return Math.min(2.5, segment.length() * 0.18);
    }
}
