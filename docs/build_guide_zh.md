# Elastic Uyghur Analyzer 插件构建指南

本文档提供了构建、测试和打包 Elasticsearch 8.7.0 版本的 Elastic Uyghur Analyzer 插件的说明。

## 前提条件

- JDK 17（与 Elasticsearch 8.7.0 兼容）
- Git
- Gradle 7.6.1（项目已内置，无需单独安装）
- Docker（用于测试 Elasticsearch）

> **注意**：本项目使用内置的 Gradle Wrapper（版本 7.6.1），它与 JDK 17 兼容。请确保使用正确的 JDK 版本以避免构建问题。

## 构建插件

### 克隆仓库

```bash
git clone https://github.com/TocharianOU/elastic-uyghur-analyzer.git
cd elastic-uyghur-analyzer
```

### 构建插件

该插件默认配置为针对 Elasticsearch 8.7.0 进行构建。

```bash
./gradlew clean assemble
```

插件 ZIP 文件将在 `build/distributions/uyghur-analyzer-plugin-8.7.0.zip` 创建。

## 使用 Elasticsearch 进行测试

### 使用 Docker 启动 Elasticsearch

```bash
# 为 Elasticsearch 和 Kibana 创建 Docker 网络
docker network create elastic

# 启动 Elasticsearch
docker run -d --name es -p 9200:9200 -p 9301:9300 \
  -e "discovery.type=single-node" \
  -e "ELASTIC_PASSWORD=your_password" \
  --net elastic \
  docker.elastic.co/elasticsearch/elasticsearch:8.7.0

# 等待 Elasticsearch 启动
sleep 30

# 为 kibana_system 用户设置密码
curl -X PUT "https://localhost:9200/_security/user/kibana_system/_password" \
  -k -u elastic:your_password \
  -H "Content-Type: application/json" \
  -d '{"password": "your_password"}'
```

### 安装插件

```bash
# 将插件复制到容器并安装
docker cp build/distributions/uyghur-analyzer-plugin-8.7.0.zip es:/tmp/
docker exec -it -u root es bash -c "chown elasticsearch:root /tmp/uyghur-analyzer-plugin-8.7.0.zip && \
  /usr/share/elasticsearch/bin/elasticsearch-plugin install file:///tmp/uyghur-analyzer-plugin-8.7.0.zip"

# 重启 Elasticsearch 以应用插件
docker restart es

# 等待 Elasticsearch 启动
sleep 30

# 验证插件已安装
docker exec es elasticsearch-plugin list
```

### 启动 Kibana

```bash
docker run -d --name kibi -p 5601:5601 \
  -e "ELASTICSEARCH_HOSTS=https://es:9200" \
  -e "ELASTICSEARCH_USERNAME=kibana_system" \
  -e "ELASTICSEARCH_PASSWORD=your_password" \
  -e "ELASTICSEARCH_SSL_VERIFICATIONMODE=none" \
  --net elastic \
  docker.elastic.co/kibana/kibana:8.7.0
```

## 测试插件

### 创建测试索引

```bash
# 使用维吾尔语分析器创建测试索引
curl -k -X PUT "https://localhost:9200/uyghur_test" \
  -u elastic:your_password \
  -H "Content-Type: application/json" \
  -d '{
    "settings": {
      "analysis": {
        "analyzer": {
          "uyghur_analyzer": {
            "type": "uyghur_original_analyzer"
          }
        }
      }
    },
    "mappings": {
      "properties": {
        "content": {
          "type": "text",
          "analyzer": "uyghur_analyzer"
        }
      }
    }
  }'
```

### 测试分析器

```bash
# 测试分析器
curl -k -X POST "https://localhost:9200/uyghur_test/_analyze" \
  -u elastic:your_password \
  -H "Content-Type: application/json" \
  -d '{
    "analyzer": "uyghur_analyzer",
    "text": "مەن ئۇيغۇرچە سۆزلەيمەن"
  }'
```

## 访问 Kibana

在浏览器中访问 http://localhost:5601。使用以下凭据登录：
- 用户名：elastic
- 密码：your_password

要访问 Kibana 的终端：

```bash
docker exec -it --user root kibi /bin/bash
```

## 故障排除

### Java 版本问题

Elasticsearch 8.7.0 需要 Java 17。如果遇到 Java 版本问题：

```bash
export JAVA_HOME=/path/to/java17
```

如果您在使用 Gradle 构建时遇到版本兼容性问题，请确保使用 JDK 17，因为内置的 Gradle 7.6.1 与此版本兼容。

### 插件安装失败

如果插件在 Elasticsearch 中安装失败：

1. 检查 Elasticsearch 日志：`docker logs es`
2. 确保插件 ZIP 结构正确
3. 确保 Elasticsearch 有足够的磁盘空间

### 连接问题

如果无法连接到 Elasticsearch：

```bash
# 检查 Elasticsearch 是否正在运行
docker ps | grep es

# 检查 Elasticsearch 日志
docker logs es
```

## 清理

```bash
# 停止并删除容器
docker stop es kibi
docker rm es kibi

# 删除网络
docker network rm elastic
```

## 其他资源

- [Elasticsearch 8.7.0 文档](https://www.elastic.co/guide/en/elasticsearch/reference/8.7/index.html)
- [Elasticsearch 插件开发指南](https://www.elastic.co/guide/en/elasticsearch/plugins/8.7/plugin-authors.html)
- [Elasticsearch 分析插件文档](https://www.elastic.co/guide/en/elasticsearch/plugins/8.7/analysis.html)

## 支持

如果您遇到任何问题或有疑问，请在 GitHub 上[提交问题](https://github.com/TocharianOU/elastic-uyghur-analyzer/issues)。
```
