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

import org.apache.lucene.tests.analysis.BaseTokenStreamTestCase;
import org.tocharian.uyghur.morphology.dictionary.UnifiedDictionaryManager.DictionaryView;

import java.io.IOException;

public class UyghurAnalyzerTest extends BaseTokenStreamTestCase {
    public void originalAnalyzerEmitsSequentialMorphemesWithOffsets() throws IOException {
        assertAnalyzesTo(
            new UyghurAnalyzer(DictionaryView.ORIGINAL),
            "ئائىلىدىكى",
            new String[] {"ئائىلە", "دىكى"},
            new int[] {0, 6},
            new int[] {6, 10},
            null,
            new int[] {1, 1}
        );
    }

    public void splitAnalyzerEmitsSelectedDictionaryView() throws IOException {
        assertAnalyzesTo(
            new UyghurAnalyzer(DictionaryView.SPLIT),
            "ئائىلىدىكى",
            new String[] {"ئائىلى", "دىكى"},
            new int[] {0, 6},
            new int[] {6, 10},
            null,
            new int[] {1, 1}
        );
    }

    public void analyzerHandlesMixedInputWithoutTokenStreamErrors() throws IOException {
        checkRandomData(random(), new UyghurAnalyzer(DictionaryView.SPLIT), 100);
    }

    public void emptyInputProducesNoTokens() throws IOException {
        assertAnalyzesTo(
            new UyghurAnalyzer(DictionaryView.SPLIT),
            "",
            new String[] {}
        );
    }

    public void punctuationOnlyInputProducesNoTokens() throws IOException {
        assertAnalyzesTo(
            new UyghurAnalyzer(DictionaryView.SPLIT),
            "،؛؟!.,",
            new String[] {}
        );
    }

    public void unknownAsciiTokenPassesThrough() throws IOException {
        assertAnalyzesTo(
            new UyghurAnalyzer(DictionaryView.SPLIT),
            "unknown123",
            new String[] {"unknown123"},
            new int[] {0},
            new int[] {10},
            null,
            new int[] {1}
        );
    }

    public void mixedScriptInputKeepsStableOffsets() throws IOException {
        assertAnalyzesTo(
            new UyghurAnalyzer(DictionaryView.SPLIT),
            "hello ئائىلىدىكى 123",
            new String[] {"hello", "ئائىلى", "دىكى", "123"},
            new int[] {0, 6, 12, 17},
            new int[] {5, 12, 16, 20},
            null,
            new int[] {1, 1, 1, 1}
        );
    }

    public void longUnknownTokenDoesNotBreakAnalysis() throws IOException {
        String longToken = "كومپيۇتېر".repeat(32);
        assertAnalyzesTo(
            new UyghurAnalyzer(DictionaryView.SPLIT),
            longToken,
            new String[] {longToken},
            new int[] {0},
            new int[] {longToken.length()},
            null,
            new int[] {1}
        );
    }
}
