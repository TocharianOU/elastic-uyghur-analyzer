# Build Guide for Elastic Uyghur Analyzer Plugin

This document provides instructions for building, testing, and packaging the Elastic Uyghur Analyzer Plugin for Elasticsearch 8.7.0.

## Prerequisites

- JDK 17 (compatible with Elasticsearch 8.7.0)
- Git
- Gradle 7.6.1 (included in the project, no need to install separately)
- Docker (for testing with Elasticsearch)

> **Note**: This project uses the embedded Gradle Wrapper (version 7.6.1) which is compatible with JDK 17. Please ensure you use the correct JDK version to avoid build issues.

## Building the Plugin

### Clone the Repository

```bash
git clone https://github.com/TocharianOU/elastic-uyghur-analyzer.git
cd elastic-uyghur-analyzer
```

### Building the Plugin

The plugin is configured to build against Elasticsearch 8.7.0 by default.

```bash
./gradlew clean assemble
```

The plugin ZIP file will be created at `build/distributions/uyghur-analyzer-plugin-8.7.0.zip`.

## Testing with Elasticsearch

### Start Elasticsearch with Docker

```bash
# Create a Docker network for Elasticsearch and Kibana
docker network create elastic

# Start Elasticsearch
docker run -d --name es -p 9200:9200 -p 9301:9300 \
  -e "discovery.type=single-node" \
  -e "ELASTIC_PASSWORD=your_password" \
  --net elastic \
  docker.elastic.co/elasticsearch/elasticsearch:8.7.0

# Wait for Elasticsearch to start
sleep 30

# Set password for kibana_system user
curl -X PUT "https://localhost:9200/_security/user/kibana_system/_password" \
  -k -u elastic:your_password \
  -H "Content-Type: application/json" \
  -d '{"password": "your_password"}'
```

### Install the Plugin

```bash
# Copy the plugin to the container and install it
docker cp build/distributions/uyghur-analyzer-plugin-8.7.0.zip es:/tmp/
docker exec -it -u root es bash -c "chown elasticsearch:root /tmp/uyghur-analyzer-plugin-8.7.0.zip && \
  /usr/share/elasticsearch/bin/elasticsearch-plugin install file:///tmp/uyghur-analyzer-plugin-8.7.0.zip"

# Restart Elasticsearch to apply the plugin
docker restart es

# Wait for Elasticsearch to start
sleep 30

# Verify the plugin is installed
docker exec es elasticsearch-plugin list
```

### Start Kibana

```bash
docker run -d --name kibi -p 5601:5601 \
  -e "ELASTICSEARCH_HOSTS=https://es:9200" \
  -e "ELASTICSEARCH_USERNAME=kibana_system" \
  -e "ELASTICSEARCH_PASSWORD=your_password" \
  -e "ELASTICSEARCH_SSL_VERIFICATIONMODE=none" \
  --net elastic \
  docker.elastic.co/kibana/kibana:8.7.0
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
    "text": "مەن ئۇيغۇرچە سۆزلەيمەن"
  }'
```

## Accessing Kibana

Access Kibana in your browser at http://localhost:5601. Log in with:
- Username: elastic
- Password: your_password

To access Kibana's terminal:

```bash
docker exec -it --user root kibi /bin/bash
```

## Troubleshooting

### Java Version Issues

Elasticsearch 8.7.0 requires Java 17. If you encounter Java version issues:

```bash
export JAVA_HOME=/path/to/java17
```

If you encounter version compatibility issues when building with Gradle, make sure to use JDK 17 as the embedded Gradle 7.6.1 is compatible with this version.

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
docker stop es kibi
docker rm es kibi

# Remove network
docker network rm elastic
```

## Additional Resources

- [Elasticsearch 8.7.0 Documentation](https://www.elastic.co/guide/en/elasticsearch/reference/8.7/index.html)
- [Elasticsearch Plugin Development Guide](https://www.elastic.co/guide/en/elasticsearch/plugins/8.7/plugin-authors.html)
- [Elasticsearch Analysis Plugin Documentation](https://www.elastic.co/guide/en/elasticsearch/plugins/8.7/analysis.html)

## Support

If you encounter any issues or have questions, please [open an issue](https://github.com/TocharianOU/elastic-uyghur-analyzer/issues) on GitHub.
```
