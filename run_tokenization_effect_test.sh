#!/bin/bash

echo "=== ES维吾尔语分词效果测试器 ==="
echo "编译并运行分词测试程序..."
echo

# 编译测试工具及其依赖类
echo "1. 编译测试工具..."
./gradlew testClasses

if [ $? -ne 0 ]; then
    echo "测试工具编译失败！"
    exit 1
fi

echo "✓ 测试程序编译成功"
echo

# 运行测试程序
echo "2. 启动分词效果测试器..."
echo "----------------------------------------"
java -cp "build/classes/java/test:build/classes/java/main:build/resources/main:build/resources/test:build/libs/*" TokenizationEffectTester

echo "----------------------------------------"
echo "测试完成！" 