package io.hatchet.sdk.internal;

import io.github.cdimascio.dotenv.Dotenv;
import io.hatchet.sdk.exceptions.ConfigException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ClientConfigLoaderTest {

    @Test
    void loadWithAllValuesShouldMapCorrectly(@TempDir Path tempDir) throws Exception {
        Dotenv dotenv = mock(Dotenv.class);
        when(dotenv.get("HATCHET_CLIENT_TENANT_ID")).thenReturn("tenant123");
        when(dotenv.get("HATCHET_CLIENT_TOKEN")).thenReturn("secret");
        when(dotenv.get("HATCHET_CLIENT_SERVER_URL")).thenReturn("https://server:9090");
        when(dotenv.get("HATCHET_CLIENT_HOST_PORT")).thenReturn("1.2.3.4:9999");
        when(dotenv.get("HATCHET_CLIENT_NAMESPACE")).thenReturn("ns");
        when(dotenv.get("HATCHET_CLOUD_REGISTER_ID")).thenReturn("reg123");
        when(dotenv.get("HATCHET_CLOUD_ACTIONS")).thenReturn("a1,a2 , a3");
        when(dotenv.get("HATCHET_CLIENT_NO_GRPC_RETRY")).thenReturn("true");
        when(dotenv.get("HATCHET_CLIENT_AUTOSCALING_TARGET")).thenReturn("autoscale");

        // create actual temp files so WorkerTlsConfig validation succeeds
        Path certPath = Files.createFile(tempDir.resolve("cert.p12"));
        Path keyPath  = Files.createFile(tempDir.resolve("key.pem"));
        Path caPath   = Files.createFile(tempDir.resolve("ca.pem"));

        when(dotenv.get("HATCHET_CLIENT_TLS_STRATEGY")).thenReturn("mtls");
        when(dotenv.get("HATCHET_CLIENT_TLS_CERT_FILE")).thenReturn(certPath.toString());
        when(dotenv.get("HATCHET_CLIENT_TLS_KEY_FILE")).thenReturn(keyPath.toString());
        when(dotenv.get("HATCHET_CLIENT_TLS_KEY")).thenReturn(null);
        when(dotenv.get("HATCHET_CLIENT_TLS_ROOT_CA_FILE")).thenReturn(caPath.toString());
        when(dotenv.get("HATCHET_CLIENT_TLS_SERVER_NAME")).thenReturn("serverName");

        ClientConfigLoader loader = new ClientConfigLoader(dotenv);
        ClientConfig cfg = loader.load();

        assertThat(cfg.getTenantId()).isEqualTo("tenant123");
        assertThat(cfg.getToken()).isEqualTo("secret");
        assertThat(cfg.getServerURL()).isEqualTo("https://server:9090");
        assertThat(cfg.getGRPCBroadcastAddress()).isEqualTo("1.2.3.4:9999");
        assertThat(cfg.getNamespace()).isEqualTo("ns");
        assertThat(cfg.getCloudRegisterId()).contains("reg123");
        assertThat(cfg.getRunnableActions()).containsExactly("a1", "a2", "a3");
        assertThat(cfg.isNoGrpcRetry()).isTrue();
        assertThat(cfg.getAutoscalingTarget()).contains("autoscale");

        WorkerTlsConfig tls = cfg.getWorkerTlsConfig();
        assertThat(tls.getStrategy()).isEqualTo(WorkerTlsConfig.Strategy.MTLS);
        assertThat(tls.getCertFile()).isNotNull();
        assertThat(tls.getCertFile().getPath()).isEqualTo(certPath.toString());
        assertThat(tls.getRootCAFile()).isNotNull();
        assertThat(tls.getRootCAFile().getPath()).isEqualTo(caPath.toString());
        assertThat(tls.getServerNameOverride()).isEqualTo("serverName");
        // inline key expected to be null since we provided keyFile
        assertThat(tls.getKey()).isNull();
    }

    @Test
    void loadShouldThrowWhenCertFileMissing() {
        Dotenv dotenv = mock(Dotenv.class);
        when(dotenv.get("HATCHET_CLIENT_TLS_STRATEGY")).thenReturn("mtls");
        when(dotenv.get("HATCHET_CLIENT_TLS_CERT_FILE")).thenReturn("does-not-exist.p12");
        // keep other TLS values absent

        ClientConfigLoader loader = new ClientConfigLoader(dotenv);

        assertThatThrownBy(loader::load)
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("TLS certFile does not exist or is not readable");
    }

    @Test
    void loadWithMissingValuesShouldUseDefaults() {
        // returns null by default
        Dotenv dotenv = mock(Dotenv.class);

        ClientConfigLoader loader = new ClientConfigLoader(dotenv);
        ClientConfig cfg = loader.load();

        assertThat(cfg.getServerURL()).isEqualTo("localhost:7077");
        assertThat(cfg.getGRPCBroadcastAddress()).isEqualTo("localhost:8888");
        assertThat(cfg.getNamespace()).isEmpty();
        assertThat(cfg.getRunnableActions()).isEmpty();
        assertThat(cfg.isNoGrpcRetry()).isFalse();
        assertThat(cfg.getCloudRegisterId()).isEmpty();
        assertThat(cfg.getAutoscalingTarget()).isEmpty();
        assertThat(cfg.getWorkerTlsConfig().getStrategy())
                .isEqualTo(WorkerTlsConfig.Strategy.NONE);
    }

    @Test
    void shouldFallbackTlsStrategyIfInvalid() {
        Dotenv dotenv = mock(Dotenv.class);
        when(dotenv.get("HATCHET_CLIENT_TLS_STRATEGY")).thenReturn("weird");

        ClientConfigLoader loader = new ClientConfigLoader(dotenv);
        ClientConfig config = loader.load();

        assertThat(config.getWorkerTlsConfig().getStrategy())
                .isEqualTo(WorkerTlsConfig.Strategy.NONE);
    }

    @Test
    void commaListParsingHandlesEmptyAndWhitespace() {
        Dotenv dotenv = mock(Dotenv.class);
        when(dotenv.get("HATCHET_CLOUD_ACTIONS")).thenReturn(" , a ,  ,b,");
        ClientConfigLoader loader = new ClientConfigLoader(dotenv);
        ClientConfig cfg = loader.load();

        assertThat(cfg.getRunnableActions()).containsExactly("a", "b");
    }

    @Test
    void booleanParsingIsCaseInsensitiveAndDefaultsFalse() {
        Dotenv dotenv = mock(Dotenv.class);
        when(dotenv.get("HATCHET_CLIENT_NO_GRPC_RETRY")).thenReturn("TrUe");
        ClientConfigLoader loader = new ClientConfigLoader(dotenv);
        ClientConfig cfg = loader.load();
        assertThat(cfg.isNoGrpcRetry()).isTrue();

        when(dotenv.get("HATCHET_CLIENT_NO_GRPC_RETRY")).thenReturn(null);
        loader = new ClientConfigLoader(dotenv);
        cfg = loader.load();
        assertThat(cfg.isNoGrpcRetry()).isFalse();
    }

    @Test
    void fallbackToDefaultsWhenEnvBlankOrMissing() {
        Dotenv dotenv = mock(Dotenv.class);
        when(dotenv.get("HATCHET_CLIENT_SERVER_URL")).thenReturn("   ");
        when(dotenv.get("HATCHET_CLIENT_HOST_PORT")).thenReturn(null);

        ClientConfigLoader loader = new ClientConfigLoader(dotenv);
        ClientConfig cfg = loader.load();

        assertThat(cfg.getServerURL()).isEqualTo("localhost:7077");
        assertThat(cfg.getGRPCBroadcastAddress()).isEqualTo("localhost:8888");
    }

    @Test
    void loaderShouldBeCaseInsensitiveForKeys() {
        Dotenv dotenv = mock(Dotenv.class);
        // lowercase
        when(dotenv.get("hatchet_client_server_url")).thenReturn("http://lowercase");
        ClientConfig cfg = new ClientConfigLoader(dotenv).load();
        assertThat(cfg.getServerURL()).isEqualTo("http://lowercase");

        // uppercase original
        Dotenv dotenv2 = mock(Dotenv.class);
        when(dotenv2.get("HATCHET_CLIENT_SERVER_URL")).thenReturn("http://upper");
        cfg = new ClientConfigLoader(dotenv2).load();
        assertThat(cfg.getServerURL()).isEqualTo("http://upper");
    }
}
