package io.hatchet.sdk;

import io.hatchet.sdk.exceptions.ConfigException;
import io.hatchet.sdk.internal.ClientConfig;
import io.hatchet.sdk.internal.WorkerTlsConfig;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class HatchetClientTest {

    @Test
    void buildWithMissingServerUrlShouldThrow() {
        HatchetClient.Builder builder = HatchetClient.builder()
                .withToken("test-token");

        assertThatThrownBy(builder::build).isInstanceOf(ConfigException.class);
    }

    @Test
    void buildWithMissingRequiredFieldsShouldThrow() {
        HatchetClient.Builder b1 = HatchetClient.builder()
                .withServerURL("localhost:8888");

        assertThatThrownBy(b1::build).isInstanceOf(ConfigException.class);

        HatchetClient.Builder b2 = HatchetClient.builder()
                .withGrpcBroadcastAddress("localhost:7077");

        assertThatThrownBy(b2::build).isInstanceOf(ConfigException.class);

        HatchetClient.Builder b3 = HatchetClient.builder()
                .withToken("sometoken");

        assertThatThrownBy(b2::build).isInstanceOf(ConfigException.class);
    }

    @Test
    void buildWithRequiredFieldsShouldSucceed() {
        HatchetClient client = HatchetClient.builder()
                .withServerURL("localhost:8888")
                .withToken("abc123")
                .withGrpcBroadcastAddress("localhost:7077")
                .withTls(WorkerTlsConfig
                        .builder()
                        .strategy(WorkerTlsConfig.Strategy.NONE)
                        .build())
                .build();

        assertThat(client).isNotNull();
        ClientConfig cfg = client.getConfig();
        assertThat("localhost:8888").isEqualTo(cfg.getServerURL());
        assertThat("abc123").isEqualTo(cfg.getToken());
    }

    @Test
    void buildWithAllOptionsShouldPopulateConfig() {
        HatchetClient client = HatchetClient.builder()
                .withServerURL("localhost:8888")
                .withToken("abc123")
                .withTenantId("tenant1")
                .withNamespace("dev-ns")
                .withNoGrpcRetry(true)
                .withGrpcBroadcastAddress("localhost:9999")
                .withCloudRegisterId("cloud-1")
                .withRunnableActions(List.of("action1", "action2"))
                .withPresetWorkerLabels(Map.of("env", "test"))
                .withAutoscalingTarget("target-1")
                .withDebug(true)
                .withTls(WorkerTlsConfig
                        .builder()
                        .strategy(WorkerTlsConfig.Strategy.NONE)
                        .build())
                .build();

        ClientConfig cfg = client.getConfig();

        assertThat(cfg.getTenantId()).isEqualTo("tenant1");
        assertThat(cfg.getNamespace()).isEqualTo("dev-ns");
        assertThat(cfg.isNoGrpcRetry()).isTrue();
        assertThat(cfg.getGRPCBroadcastAddress()).isEqualTo("localhost:9999");
        assertThat(cfg.getCloudRegisterId()).hasValue("cloud-1");
        assertThat(cfg.getRunnableActions()).containsExactly("action1", "action2");
        assertThat(cfg.getPresetWorkerLabels()).isEqualTo(Map.of("env", "test"));
        assertThat(cfg.getAutoscalingTarget()).hasValue("target-1");
        assertThat(cfg.isDebug()).isTrue();
    }
}
