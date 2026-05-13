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
import java.util.List;

/**
 * Result returned by the weighted decoder.
 */
public class SegmentationPath {
    private final List<String> segments;
    private final double cost;
    private final boolean modelBacked;
    private final String notes;

    public SegmentationPath(List<String> segments, double cost, boolean modelBacked, String notes) {
        this.segments = Collections.unmodifiableList(segments);
        this.cost = cost;
        this.modelBacked = modelBacked;
        this.notes = notes;
    }

    public List<String> getSegments() {
        return segments;
    }

    public double getCost() {
        return cost;
    }

    public boolean isModelBacked() {
        return modelBacked;
    }

    public String getNotes() {
        return notes;
    }

    public boolean hasSplit() {
        return segments.size() > 1;
    }

    public double toConfidence() {
        if (!hasSplit()) {
            return 0.30;
        }

        double confidence = 1.0 / (1.0 + Math.max(0.0, cost));
        return Math.max(0.45, Math.min(0.82, confidence + 0.35));
    }
}
