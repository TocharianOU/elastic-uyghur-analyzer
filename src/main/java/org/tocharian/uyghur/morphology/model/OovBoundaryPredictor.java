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
import java.util.Comparator;
import java.util.List;

/**
 * Predicts morpheme boundaries for words not covered by dictionary paths.
 */
public interface OovBoundaryPredictor {
    SegmentationPath predict(String word);

    static OovBoundaryPredictor suffixBackoff(WeightedMorphologyModel model) {
        return new SuffixBackoffBoundaryPredictor(model);
    }
}

class SuffixBackoffBoundaryPredictor implements OovBoundaryPredictor {
    private final List<String> suffixes;

    SuffixBackoffBoundaryPredictor(WeightedMorphologyModel model) {
        this.suffixes = new ArrayList<>(model.suffixes());
        this.suffixes.sort(Comparator.comparingInt(String::length).reversed());
    }

    @Override
    public SegmentationPath predict(String word) {
        if (word == null || word.length() <= 3) {
            return new SegmentationPath(List.of(word == null ? "" : word), 99.0, false, "oov-single-token");
        }

        List<String> reversedSuffixes = new ArrayList<>();
        String remaining = word;
        boolean changed = true;

        while (changed && remaining.length() > 2) {
            changed = false;
            for (String suffix : suffixes) {
                if (suffix.length() > remaining.length() - 1) {
                    continue;
                }

                if (remaining.endsWith(suffix)) {
                    reversedSuffixes.add(suffix);
                    remaining = remaining.substring(0, remaining.length() - suffix.length());
                    changed = true;
                    break;
                }
            }
        }

        if (reversedSuffixes.isEmpty() || remaining.length() < 2) {
            return new SegmentationPath(List.of(word), 99.0, false, "oov-no-boundary");
        }

        List<String> segments = new ArrayList<>();
        segments.add(remaining);
        for (int i = reversedSuffixes.size() - 1; i >= 0; i--) {
            segments.add(reversedSuffixes.get(i));
        }

        double cost = 8.0 + reversedSuffixes.size();
        return new SegmentationPath(segments, cost, true, "oov-suffix-boundary");
    }
}
