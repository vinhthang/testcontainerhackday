package com.github.vinhthang.boot;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class ElasticSearchLearningTests {
    private static final String IMAGE_NAME =
            "docker.elastic.co/elasticsearch/elasticsearch:8.6.1";

    private static final ElasticsearchContainer container =
            new ElasticsearchContainer(IMAGE_NAME)
                    .withExposedPorts(9200)
                    .withPassword("s3cret");

    private static final NodeSelector INGEST_NODE_SELECTOR = nodes -> {
        final Iterator<Node> iterator = nodes.iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            // roles may be null if we don't know, thus we keep the node in then...
            if (node.getRoles() != null && node.getRoles().isIngest() == false) {
                iterator.remove();
            }
        }
    };

    private static ElasticsearchClient client;
    private static RestClient restClient;

    @BeforeAll
    public static void startElasticsearchCreateLocalClient() {
        container.start();

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "s3cret"));

        HttpHost host = new HttpHost("localhost", container.getMappedPort(9200), "https");
        final RestClientBuilder builder = RestClient.builder(host)
                .setHttpClientConfigCallback(clientBuilder -> {
                    clientBuilder.setSSLContext(container.createSslContextFromCa());
                    clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    return clientBuilder;
                })
                .setNodeSelector(INGEST_NODE_SELECTOR);

        final ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        restClient = builder.build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper(mapper));
        client = new ElasticsearchClient(transport);
    }

    @Test
    public void test_request() throws Exception {
        Response response = restClient.performRequest(new Request("GET", "/_cluster/health"));

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);

        assertThat(client.info().clusterName()).isEqualTo("docker-cluster");
        assertThat(client.info().version().number()).isEqualTo("8.6.1");
    }
}
