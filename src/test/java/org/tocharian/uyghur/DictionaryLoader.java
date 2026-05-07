/*
 * Licensed to Tocharian under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Tocharian licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may
 * obtain a copy of the License at
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

package org.tocharian.uyghur;

import org.tocharian.uyghur.morphology.dictionary.UnifiedDictionaryManager;
import org.tocharian.uyghur.morphology.dictionary.UnifiedDictionaryManager.DictionaryView;

import java.io.IOException;
import java.util.Map;

/**
 * Test helper for manually inspecting dictionary views.
 */
public class DictionaryLoader {
    private final UnifiedDictionaryManager unifiedManager;
    private DictionaryView currentView;

    public DictionaryLoader() {
        this.unifiedManager = UnifiedDictionaryManager.shared();
        this.currentView = DictionaryView.SPLIT;
    }

    public Map<String, String[]> getDictionary() {
        if (!unifiedManager.isInitialized()) {
            try {
                unifiedManager.initialize();
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize unified dictionary: " + e.getMessage(), e);
            }
        }

        return getMergedDictionary();
    }

    private Map<String, String[]> getMergedDictionary() {
        Map<String, String[]> baseDict;

        switch (currentView) {
            case ORIGINAL:
                baseDict = new java.util.HashMap<>(unifiedManager.getOriginalView());
                break;
            case SPLIT:
                baseDict = new java.util.HashMap<>(unifiedManager.getSplitView());
                break;
            case CUSTOM:
                return unifiedManager.getCustomView();
            default:
                baseDict = new java.util.HashMap<>(unifiedManager.getSplitView());
        }

        baseDict.putAll(unifiedManager.getCustomView());
        return baseDict;
    }

    public void initializeFromResource(String resourcePath) throws IOException {
        if (this.currentView == DictionaryView.SPLIT && resourcePath.contains("original")) {
            this.currentView = DictionaryView.ORIGINAL;
        } else if (this.currentView == DictionaryView.SPLIT && resourcePath.contains("custom")) {
            this.currentView = DictionaryView.CUSTOM;
        }

        if (!unifiedManager.isInitialized()) {
            unifiedManager.initialize();
        }
    }

    public void setDictionaryView(DictionaryView view) {
        this.currentView = view;
    }

    public DictionaryView getCurrentView() {
        return currentView;
    }

    public UnifiedDictionaryManager getUnifiedManager() {
        return unifiedManager;
    }

    public void initializeFromUrl(String urlPath) throws IOException {
        initializeFromResource("/dictionaries/thuuy_morph_raw.txt");
    }
}
