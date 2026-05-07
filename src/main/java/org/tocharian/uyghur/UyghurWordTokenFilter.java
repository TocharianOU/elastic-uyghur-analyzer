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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.tocharian.uyghur.morphology.analyzer.RuleBasedMorphologyAnalyzer;
import org.tocharian.uyghur.morphology.analyzer.MorphologyAnalysisResult;
import org.tocharian.uyghur.morphology.dictionary.UnifiedDictionaryManager.DictionaryView;

import java.io.IOException;

public class UyghurWordTokenFilter extends TokenFilter {
    private final CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAttr = addAttribute(OffsetAttribute.class);
    private final PositionIncrementAttribute positionIncrementAttr = addAttribute(PositionIncrementAttribute.class);
    private final RuleBasedMorphologyAnalyzer morphologyAnalyzer;
    private final DictionaryView viewType;
    private String[] currentParts;
    private int currentPartIndex;
    private int currentStartOffset;
    private int currentEndOffset;
    private int currentPositionIncrement;

    protected UyghurWordTokenFilter(TokenStream input, RuleBasedMorphologyAnalyzer analyzer, DictionaryView viewType) {
        super(input);
        this.morphologyAnalyzer = analyzer;
        this.viewType = viewType;
    }
    


    @Override
    public final boolean incrementToken() throws IOException {
        // 如果当前有分割结果待输出，继续输出
        if (currentParts != null && currentPartIndex < currentParts.length) {
            clearAttributes();
            termAttr.append(currentParts[currentPartIndex]);
            int length = currentParts[currentPartIndex].length();
            int partEndOffset = currentPartIndex == currentParts.length - 1
                ? currentEndOffset
                : Math.min(currentStartOffset + length, currentEndOffset);
            offsetAttr.setOffset(currentStartOffset, partEndOffset);
            positionIncrementAttr.setPositionIncrement(currentPartIndex == 0 ? currentPositionIncrement : 1);
            currentStartOffset = partEndOffset;
            currentPartIndex++;
            return true;
        }

        // 获取下一个token
        if (input.incrementToken()) {
            String token = termAttr.toString();
            
            // 使用形态学分析器进行分析
            if (morphologyAnalyzer != null) {
                MorphologyAnalysisResult result = morphologyAnalyzer.analyze(token, viewType);
                
                if (result != null && result.getMorphemes().size() > 1) {
                    // 有分析结果，进行分割
                    currentParts = result.getMorphemes().toArray(new String[0]);
                    currentPartIndex = 0;
                    currentStartOffset = offsetAttr.startOffset();
                    currentEndOffset = offsetAttr.endOffset();
                    currentPositionIncrement = positionIncrementAttr.getPositionIncrement();
                    return incrementToken(); // 递归调用输出第一个分割部分
                } else {
                    // 没有分析结果或只有一个段，原样输出
                    return true;
                }
            } else {
                // 没有形态学分析器，原样输出（不应该发生）
                return true;
            }
        } else {
            return false;
        }
    }
}
