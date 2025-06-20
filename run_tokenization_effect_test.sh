#!/bin/bash

echo "=== ES维吾尔语分词效果测试器 ==="
echo "编译并运行分词测试程序..."
echo

# 编译项目
echo "1. 编译项目..."
./gradlew build -x test

if [ $? -ne 0 ]; then
    echo "项目编译失败！"
    exit 1
fi

echo "✓ 项目编译成功"
echo

# 编译测试程序
echo "2. 编译测试程序..."
javac -cp "build/libs/*:src/main/java" -d build/classes TokenizationEffectTester.java

if [ $? -ne 0 ]; then
    echo "测试程序编译失败！"
    exit 1
fi

echo "✓ 测试程序编译成功"
echo

# 运行测试程序
echo "3. 启动分词效果测试器..."
echo "----------------------------------------"
java -cp "build/libs/*:build/classes:src/main/java" TokenizationEffectTester

echo "----------------------------------------"
echo "测试完成！" 