# Build Guide for Elastic Uyghur Analyzer Plugin

This document provides instructions for building, testing, and packaging the Elastic Uyghur Analyzer Plugin for Elasticsearch 8.x and 9.x.

## Prerequisites

- JDK 17 (compatible with Elasticsearch 8.7.0)
- JDK 21 or higher for Elasticsearch 9.x builds
- Git
- Gradle 7.6.1 wrapper for Elasticsearch 8.x builds
- Gradle 8.14 for Elasticsearch 9.x builds
- Docker (for testing with Elasticsearch)

> **Note**: The embedded Gradle Wrapper is version 7.6.1 and is intended for the default Elasticsearch 8.x build with JDK 17. Elasticsearch 9.x build tools require a newer Gradle runtime and Java 21 or higher.

## Building the Plugin

### Clone the Repository

```bash
git clone https://github.com/TocharianOU/elastic-uyghur-analyzer.git
cd elastic-uyghur-analyzer
```

### Building the Plugin

The plugin is configured to build the Elasticsearch 8.x artifact by default.

```bash
./gradlew clean check
```

The plugin ZIP file will be created at `build/distributions/uyghur-analyzer-plugin-2.2.0-es8.zip`.

To build the Elasticsearch 9.x artifact, use Java 21 or higher, Gradle 8.14, Elasticsearch 9.4.0, and Lucene 10.4.0:

```bash
curl -fsSL https://services.gradle.org/distributions/gradle-8.14-bin.zip -o /tmp/gradle-8.14-bin.zip
unzip -q /tmp/gradle-8.14-bin.zip -d /tmp
/tmp/gradle-8.14/bin/gradle clean check -PesMajor=9 -PelasticsearchVersion=9.4.0 -PluceneVersion=10.4.0
```

The Elasticsearch 9.x plugin ZIP file will be created at `build/distributions/uyghur-analyzer-plugin-2.2.0-es9.zip`.

## Testing with Elasticsearch

### Start Elasticsearch with Docker

```bash
# Start Elasticsearch
docker run -d --name es -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "ELASTIC_PASSWORD=your_password" \
  docker.elastic.co/elasticsearch/elasticsearch:8.19.15

# Wait for Elasticsearch to start
sleep 30
```

### Install the Plugin

```bash
# Copy the plugin to the container and install it
docker cp build/distributions/uyghur-analyzer-plugin-2.2.0-es8.zip es:/tmp/
docker exec es elasticsearch-plugin install file:///tmp/uyghur-analyzer-plugin-2.2.0-es8.zip

# Restart Elasticsearch to apply the plugin
docker restart es

# Wait for Elasticsearch to start
sleep 30

# Verify the plugin is installed
docker exec es elasticsearch-plugin list
```

### Start Kibana (Optional)

```bash
docker run -d --name kibana -p 5601:5601 \
  -e "ELASTICSEARCH_HOSTS=http://host.docker.internal:9200" \
  -e "ELASTICSEARCH_USERNAME=elastic" \
  -e "ELASTICSEARCH_PASSWORD=your_password" \
  docker.elastic.co/kibana/kibana:8.19.15
```

## Testing the Plugin

### Create a Test Index

```bash
# Create a test index with the Uyghur analyzer
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

### Test the Analyzer

```bash
# Test the analyzer
curl -k -X POST "https://localhost:9200/uyghur_test/_analyze" \
  -u elastic:your_password \
  -H "Content-Type: application/json" \
  -d '{
    "analyzer": "uyghur_analyzer",
    "text": "ئورۇنلاشتۇرۇشلارنى تاكسىدا"
  }'
```

Expected output:
```json
{
  "tokens": [
    {"token": "ئورۇنلاشتۇرۇشلار", "start_offset": 0, "end_offset": 16, "type": "word", "position": 0},
    {"token": "نى", "start_offset": 17, "end_offset": 19, "type": "word", "position": 1},
    {"token": "تاكسى", "start_offset": 19, "end_offset": 24, "type": "word", "position": 2},
    {"token": "دا", "start_offset": 25, "end_offset": 27, "type": "word", "position": 3}
  ]
}
```

## Accessing Kibana

Access Kibana in your browser at http://localhost:5601. Log in with:
- Username: elastic
- Password: your_password

To access Kibana's terminal:

```bash
docker exec -it --user root kibana /bin/bash
```

## Troubleshooting

### Java Version Issues

Elasticsearch 8.x builds require Java 17. If you encounter Java version issues:

```bash
export JAVA_HOME=/path/to/java17
```

If you encounter version compatibility issues when building with Gradle, use JDK 17 with the embedded Gradle 7.6.1 wrapper for Elasticsearch 8.x artifacts. Use Java 21 or higher with Gradle 8.14 for Elasticsearch 9.x artifacts.

### Plugin Installation Failures

If the plugin fails to install in Elasticsearch:

1. Check Elasticsearch logs: `docker logs es`
2. Ensure the plugin ZIP structure is correct
3. Make sure Elasticsearch has enough disk space

### Connection Issues

If you cannot connect to Elasticsearch:

```bash
# Check if Elasticsearch is running
docker ps | grep es

# Check Elasticsearch logs
docker logs es
```

## Cleaning Up

```bash
# Stop and remove containers
docker stop es kibana
docker rm es kibana
```

## Additional Resources

- [Elasticsearch 8.19 Documentation](https://www.elastic.co/guide/en/elasticsearch/reference/8.19/index.html)
- [Elasticsearch Plugin Development Guide](https://www.elastic.co/guide/en/elasticsearch/plugins/8.19/plugin-authors.html)
- [Elasticsearch Analysis Plugin Documentation](https://www.elastic.co/guide/en/elasticsearch/plugins/8.19/analysis.html)

## Support

If you encounter any issues or have questions, please [open an issue](https://github.com/TocharianOU/elastic-uyghur-analyzer/issues) on GitHub.
