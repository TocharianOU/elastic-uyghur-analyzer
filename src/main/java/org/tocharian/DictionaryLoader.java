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

import org.uyghur.morphology.dictionary.UnifiedDictionaryManager;
import org.uyghur.morphology.dictionary.UnifiedDictionaryManager.DictionaryView;

import java.io.IOException;
import java.util.Map;

/**
 * 统一词典加载器
 * 使用UnifiedDictionaryManager作为单一数据源
 */
public class DictionaryLoader {
    private final UnifiedDictionaryManager unifiedManager;
    private DictionaryView currentView;

    public DictionaryLoader() {
        this.unifiedManager = new UnifiedDictionaryManager();
        this.currentView = DictionaryView.SPLIT; // 默认使用分割视图
    }

    public Map<String, String[]> getDictionary() {
        if (!unifiedManager.isInitialized()) {
            try {
                unifiedManager.initialize();
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize unified dictionary: " + e.getMessage(), e);
            }
        }
        
        // 返回合并后的词典，自定义词典优先
        return getMergedDictionary();
    }
    
    /**
     * 获取合并后的词典，自定义词典优先
     */
    private Map<String, String[]> getMergedDictionary() {
        Map<String, String[]> baseDict;
        
        // 根据当前视图获取基础词典
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
        
        // 自定义词典覆盖基础词典
        Map<String, String[]> customDict = unifiedManager.getCustomView();
        baseDict.putAll(customDict);
        
        return baseDict;
    }

    /**
     * 为了向后兼容保留的方法
     * 根据资源路径推断词典类型（仅在未显式设置视图时）
     */
    public void initializeFromResource(String resourcePath) throws IOException {
        // 仅在未显式设置视图类型时才根据路径推断
        if (this.currentView == DictionaryView.SPLIT && resourcePath.contains("original")) {
            this.currentView = DictionaryView.ORIGINAL;
        } else if (this.currentView == DictionaryView.SPLIT && resourcePath.contains("custom")) {
            this.currentView = DictionaryView.CUSTOM;
        }
        // 如果已经显式设置了视图类型，则保持不变
        
        // 初始化统一词典管理器
        if (!unifiedManager.isInitialized()) {
            unifiedManager.initialize();
        }
    }

    /**
     * 设置词典视图类型
     */
    public void setDictionaryView(DictionaryView view) {
        this.currentView = view;
    }

    /**
     * 获取当前词典视图类型
     */
    public DictionaryView getCurrentView() {
        return currentView;
    }

    /**
     * 获取统一词典管理器实例
     */
    public UnifiedDictionaryManager getUnifiedManager() {
        return unifiedManager;
    }

    /**
     * 为了向后兼容保留的URL加载方法（现在使用统一系统）
     */
    public void initializeFromUrl(String urlPath) throws IOException {
        // URL加载暂时不支持，使用本地统一词典
        System.out.println("URL加载已弃用，使用统一词典系统");
        initializeFromResource("/dictionaries/thuuy_morph_raw.txt");
    }
}
