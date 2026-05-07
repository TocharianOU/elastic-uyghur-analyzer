#!/bin/bash

echo "=== 维吾尔语分词效果测试 ==="
echo "编译和运行分词测试工具..."
echo

# 编译测试工具及其依赖类
echo "编译测试工具..."
./gradlew testClasses

if [ $? -eq 0 ]; then
    echo "编译成功！"
    echo
    echo "启动分词测试工具..."
    echo "========================================"
    
    # 运行分词测试
    java -cp "build/classes/java/test:build/classes/java/main:build/resources/main:build/resources/test:build/libs/*" org.tocharian.uyghur.morphology.TokenizationTester
else
    echo "编译失败！请检查代码。"
    exit 1
fi 