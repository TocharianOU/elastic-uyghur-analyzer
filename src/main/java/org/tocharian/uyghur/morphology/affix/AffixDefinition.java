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

package org.tocharian.uyghur.morphology.affix;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Linguistically annotated Uyghur affix entry.
 */
public class AffixDefinition {
    private final String id;
    private final String archiform;
    private final List<String> surfaces;
    private final String category;
    private final String slot;
    private final AffixKind kind;
    private final String harmony;
    private final String source;
    private final String note;

    public AffixDefinition(String id,
                           String archiform,
                           List<String> surfaces,
                           String category,
                           String slot,
                           AffixKind kind,
                           String harmony,
                           String source,
                           String note) {
        this.id = requireText(id, "id");
        this.archiform = requireText(archiform, "archiform");
        this.surfaces = Collections.unmodifiableList(Objects.requireNonNull(surfaces, "surfaces"));
        if (surfaces.isEmpty()) {
            throw new IllegalArgumentException("surfaces must not be empty");
        }
        this.category = requireText(category, "category");
        this.slot = requireText(slot, "slot");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.harmony = requireText(harmony, "harmony");
        this.source = requireText(source, "source");
        this.note = note == null ? "" : note;
    }

    public String getId() {
        return id;
    }

    public String getArchiform() {
        return archiform;
    }

    public List<String> getSurfaces() {
        return surfaces;
    }

    public String getCategory() {
        return category;
    }

    public String getSlot() {
        return slot;
    }

    public AffixKind getKind() {
        return kind;
    }

    public String getHarmony() {
        return harmony;
    }

    public String getSource() {
        return source;
    }

    public String getNote() {
        return note;
    }

    public boolean hasSurface(String surface) {
        return surfaces.contains(surface);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
