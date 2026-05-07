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

package org.tocharian.uyghur.morphology.analyzer;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.tocharian.uyghur.morphology.dictionary.UnifiedDictionaryManager.DictionaryView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 基于规则的形态分析器测试类
 */
public class RuleBasedMorphologyAnalyzerTest {
    private RuleBasedMorphologyAnalyzer analyzer;

    @Before
    public void setUp() throws IOException {
        analyzer = new RuleBasedMorphologyAnalyzer();
        analyzer.initialize();
    }

    @Test
    public void knownDictionaryWordUsesSelectedView() {
        MorphologyAnalysisResult original = analyzer.analyze("ئائىلىدىكى", DictionaryView.ORIGINAL);
        MorphologyAnalysisResult split = analyzer.analyze("ئائىلىدىكى", DictionaryView.SPLIT);

        assertEquals(Arrays.asList("ئائىلە", "دىكى"), original.getMorphemes());
        assertEquals(Arrays.asList("ئائىلى", "دىكى"), split.getMorphemes());
        assertEquals(MorphologyAnalysisResult.AnalysisMethod.DICTIONARY_EXACT, original.getMethod());
        assertEquals(MorphologyAnalysisResult.AnalysisMethod.DICTIONARY_EXACT, split.getMethod());
    }

    @Test
    public void defaultAnalyzeUsesSplitView() {
        MorphologyAnalysisResult result = analyzer.analyze("ئائىلىدىكى");

        assertEquals(Arrays.asList("ئائىلى", "دىكى"), result.getMorphemes());
        assertEquals(MorphologyAnalysisResult.AnalysisMethod.DICTIONARY_EXACT, result.getMethod());
    }

    @Test
    public void unknownWordsReturnNonEmptyAnalysis() {
        for (String word : Arrays.asList("كىتابلىرىمنىڭ", "ئۆيلەردىكى", "دوستلىرىنى")) {
            MorphologyAnalysisResult result = analyzer.analyze(word, DictionaryView.SPLIT);

            assertNotNull(result);
            assertEquals(word, result.getOriginalWord());
            assertFalse(result.getMorphemes().isEmpty());
            assertTrue(result.getConfidence() > 0);
        }
    }
} 