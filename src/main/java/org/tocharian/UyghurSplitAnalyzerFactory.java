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

package org.tocharian;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.plugin.analysis.AnalyzerFactory;
import org.elasticsearch.plugin.NamedComponent;
import org.uyghur.morphology.dictionary.UnifiedDictionaryManager.DictionaryView;

import java.io.IOException;

@NamedComponent(value = "uyghur_split_analyzer")
public class UyghurSplitAnalyzerFactory implements AnalyzerFactory {
    private final DictionaryLoader dictionaryLoader;

    public UyghurSplitAnalyzerFactory() {
        this.dictionaryLoader = new DictionaryLoader();
        try {
            // 使用统一词典系统，设置为Split视图
            dictionaryLoader.setDictionaryView(DictionaryView.SPLIT);
            dictionaryLoader.initializeFromResource("/dictionaries/thuuy_morph_raw.txt");
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Uyghur Split Analyzer: " + e.getMessage(), e);
        }
    }

    @Override
    public Analyzer create() {
        return new UyghurAnalyzer(dictionaryLoader.getDictionary());
    }
}
