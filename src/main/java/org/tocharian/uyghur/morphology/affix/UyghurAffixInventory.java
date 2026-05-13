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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads the curated Uyghur affix inventory used by morphology-aware analyzers.
 */
public class UyghurAffixInventory {
    private static final String DEFAULT_RESOURCE = "/morphology/uyghur_affix_inventory.tsv";
    private static final int EXPECTED_COLUMNS = 9;

    private final List<AffixDefinition> definitions;
    private final Map<String, AffixDefinition> byId;
    private final Map<String, List<AffixDefinition>> bySurface;
    private final Map<String, List<AffixDefinition>> byCategory;
    private final Map<String, List<AffixDefinition>> bySlot;

    private UyghurAffixInventory(List<AffixDefinition> definitions) {
        this.definitions = Collections.unmodifiableList(new ArrayList<>(definitions));
        this.byId = indexById(definitions);
        this.bySurface = indexBySurface(definitions);
        this.byCategory = indexByValue(definitions, IndexValue.CATEGORY);
        this.bySlot = indexByValue(definitions, IndexValue.SLOT);
    }

    public static UyghurAffixInventory loadDefault() throws IOException {
        InputStream inputStream = UyghurAffixInventory.class.getResourceAsStream(DEFAULT_RESOURCE);
        if (inputStream == null) {
            throw new IOException("Unable to find Uyghur affix inventory resource: " + DEFAULT_RESOURCE);
        }
        return load(inputStream);
    }

    public static UyghurAffixInventory load(InputStream inputStream) throws IOException {
        List<AffixDefinition> definitions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                definitions.add(parseLine(trimmed, lineNumber));
            }
        }
        return new UyghurAffixInventory(definitions);
    }

    public List<AffixDefinition> all() {
        return definitions;
    }

    public AffixDefinition getById(String id) {
        return byId.get(id);
    }

    public List<AffixDefinition> findBySurface(String surface) {
        return bySurface.getOrDefault(surface, Collections.emptyList());
    }

    public List<AffixDefinition> findByCategory(String category) {
        return byCategory.getOrDefault(normalizeKey(category), Collections.emptyList());
    }

    public List<AffixDefinition> findBySlot(String slot) {
        return bySlot.getOrDefault(normalizeKey(slot), Collections.emptyList());
    }

    public boolean isKnownSurface(String surface) {
        return bySurface.containsKey(surface);
    }

    private static AffixDefinition parseLine(String line, int lineNumber) {
        String[] columns = line.split("\t", -1);
        if (columns.length != EXPECTED_COLUMNS) {
            throw new IllegalArgumentException("Invalid affix inventory row at line " + lineNumber
                + ": expected " + EXPECTED_COLUMNS + " columns but got " + columns.length);
        }

        List<String> surfaces = new ArrayList<>();
        for (String surface : columns[2].split("\\|")) {
            String normalizedSurface = surface.trim();
            if (!normalizedSurface.isEmpty() && !surfaces.contains(normalizedSurface)) {
                surfaces.add(normalizedSurface);
            }
        }

        return new AffixDefinition(
            columns[0],
            columns[1],
            surfaces,
            columns[3],
            columns[4],
            AffixKind.valueOf(columns[5].trim()),
            columns[6],
            columns[7],
            columns[8]
        );
    }

    private static Map<String, AffixDefinition> indexById(List<AffixDefinition> definitions) {
        Map<String, AffixDefinition> result = new HashMap<>();
        for (AffixDefinition definition : definitions) {
            AffixDefinition previous = result.put(definition.getId(), definition);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate affix id: " + definition.getId());
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, List<AffixDefinition>> indexBySurface(List<AffixDefinition> definitions) {
        Map<String, List<AffixDefinition>> result = new HashMap<>();
        for (AffixDefinition definition : definitions) {
            for (String surface : definition.getSurfaces()) {
                result.computeIfAbsent(surface, ignored -> new ArrayList<>()).add(definition);
            }
        }
        return freezeListMap(result);
    }

    private static Map<String, List<AffixDefinition>> indexByValue(List<AffixDefinition> definitions,
                                                                    IndexValue indexValue) {
        Map<String, List<AffixDefinition>> result = new HashMap<>();
        for (AffixDefinition definition : definitions) {
            String key = indexValue == IndexValue.CATEGORY ? definition.getCategory() : definition.getSlot();
            result.computeIfAbsent(normalizeKey(key), ignored -> new ArrayList<>()).add(definition);
        }
        return freezeListMap(result);
    }

    private static Map<String, List<AffixDefinition>> freezeListMap(Map<String, List<AffixDefinition>> map) {
        Map<String, List<AffixDefinition>> result = new HashMap<>();
        for (Map.Entry<String, List<AffixDefinition>> entry : map.entrySet()) {
            result.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(result);
    }

    private static String normalizeKey(String key) {
        return key == null ? "" : key.trim().toUpperCase(Locale.ROOT);
    }

    private enum IndexValue {
        CATEGORY,
        SLOT
    }
}
