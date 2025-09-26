package io.hatchet.sdk.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ClientConfigTest {

    @Test
    void buildAndGettersShouldReturnAllFields(@TempDir Path tempDir) throws Exception {
        List<String> actions = List.of("action1", "action2");
        Map<String, String> labels = Map.of("k1", "v1");

        Path cert = Files.createFile(tempDir.resolve("client-cert.pem"));
        Path key  = Files.createFile(tempDir.resolve("client-key.pem"));
        Path ca   = Files.createFile(tempDir.resolve("ca.pem"));

        WorkerTlsConfig tls = WorkerTlsConfig.builder()
                .strategy(WorkerTlsConfig.Strategy.MTLS)
                .certFile(cert.toFile())
                .keyFile(key.toFile())
                .rootCAFile(ca.toFile())
                .serverNameOverride("hatchet.local")
                .build();

        ClientConfig cfg = ClientConfig.builder()
                .tenantId("tenant-xyz")
                .token("token-abc")
                .noGrpcRetry(true)
                .serverURL("https://hatchet.example")
                .grpcBroadcastAddress("host:1234")
                .tls(tls)
                .namespace("dev")
                .cloudRegisterId("cloud-123")
                .runnableActions(actions)
                .presetWorkerLabels(labels)
                .autoscalingTarget("autoscale-1")
                .debug(true)
                .build();

        // assert each getter returns expected value
        assertThat(cfg.getTenantId()).isEqualTo("tenant-xyz");
        assertThat(cfg.getToken()).isEqualTo("token-abc");
        assertThat(cfg.isNoGrpcRetry()).isTrue();
        assertThat(cfg.getServerURL()).isEqualTo("https://hatchet.example");
        assertThat(cfg.getGRPCBroadcastAddress()).isEqualTo("host:1234");
        assertThat(cfg.getNamespace()).isEqualTo("dev");
        assertThat(cfg.getCloudRegisterId()).hasValue("cloud-123");
        assertThat(cfg.getRunnableActions()).containsExactlyElementsOf(actions);
        assertThat(cfg.getPresetWorkerLabels()).isEqualTo(labels);
        assertThat(cfg.getAutoscalingTarget()).hasValue("autoscale-1");
        assertThat(cfg.isDebug()).isTrue();

        // tls
        assertThat(cfg.getWorkerTlsConfig().getStrategy()).isEqualTo(tls.getStrategy());
        assertThat(cfg.getWorkerTlsConfig().getRootCAFile().getPath()).isEqualTo(ca.toString());
        assertThat(cfg.getWorkerTlsConfig().getServerNameOverride()).isEqualTo(tls.getServerNameOverride());
        assertThat(cfg.getWorkerTlsConfig().getCertFile().getPath()).isEqualTo(cert.toString());
        assertThat(cfg.getWorkerTlsConfig().getKeyFile().getPath()).isEqualTo(key.toString());
    }

    @Test
    void toBuilderRoundTripShouldPreserveAllFields() {
        ClientConfig original = ClientConfig.builder()
                .tenantId("t")
                .token("tok")
                .serverURL("s")
                .namespace("n")
                .debug(true)
                .build();

        ClientConfig rebuilt = original.toBuilder().build();

        // compare field-by-field (no equals implemented)
        assertThat(rebuilt.getTenantId()).isEqualTo(original.getTenantId());
        assertThat(rebuilt.getToken()).isEqualTo(original.getToken());
        assertThat(rebuilt.getServerURL()).isEqualTo(original.getServerURL());
        assertThat(rebuilt.getNamespace()).isEqualTo(original.getNamespace());
        assertThat(rebuilt.isDebug()).isEqualTo(original.isDebug());
    }

    @Test
    void defaultsShouldBeEmptyOrSafe() {
        ClientConfig cfg = ClientConfig.builder().build();

        // collections are empty (not null)
        assertThat(cfg.getRunnableActions()).isNotNull().isEmpty();
        assertThat(cfg.getPresetWorkerLabels()).isNotNull().isEmpty();

        // optionals are empty
        assertThat(cfg.getCloudRegisterId()).isEmpty();
        assertThat(cfg.getAutoscalingTarget()).isEmpty();

        // namespace default is empty string
        assertThat(cfg.getNamespace()).isEqualTo("");

        // tls default should be none
        assertThat(cfg.getWorkerTlsConfig()).isNotNull();
        assertThat(cfg.getWorkerTlsConfig().getStrategy())
                .isEqualTo(WorkerTlsConfig.Strategy.NONE);

        // debug default off
        assertThat(cfg.isDebug()).isFalse();
    }

    @Test
    void collectionsAreImmutableAfterBuild() {
        List<String> mutable = new ArrayList<>();
        mutable.add("x");

        ClientConfig cfg = ClientConfig.builder()
                .runnableActions(mutable)
                .build();

        // mutate source list
        mutable.add("y");

        // config's list should remain a copy (not affected)
        assertThat(cfg.getRunnableActions()).containsExactly("x");
    }
}
