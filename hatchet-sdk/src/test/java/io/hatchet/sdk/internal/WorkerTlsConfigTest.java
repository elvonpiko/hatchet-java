package io.hatchet.sdk.internal;

import io.hatchet.sdk.exceptions.ConfigException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;


class WorkerTlsConfigTest {

    @Test
    void noneStrategyShouldReturnNone() {
        WorkerTlsConfig cfg = WorkerTlsConfig
                .builder()
                .strategy(WorkerTlsConfig.Strategy.NONE)
                .build();
        assertThat(cfg.getStrategy()).isEqualTo(WorkerTlsConfig.Strategy.NONE);
        assertThat(cfg.isEnabled()).isFalse();
    }

    @Test
    void missingFilesShouldThrowWhenSpecified() {
        WorkerTlsConfig.Builder b = WorkerTlsConfig.builder()
                .strategy(WorkerTlsConfig.Strategy.TLS)
                .rootCAFile(new File("does-not-exist.pem"));

        assertThatThrownBy(b::build)
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("TLS rootCAFile does not exist or is not readable");
    }

    @Test
    void blankStringsAreNormalizedToNull() {
        WorkerTlsConfig cfg = WorkerTlsConfig.builder()
                .strategy(WorkerTlsConfig.Strategy.NONE)
                .cert("   ")
                .key("")
                .rootCA(" ")
                .build();

        assertThat(cfg.getCert()).isNull();
        assertThat(cfg.getKey()).isNull();
        assertThat(cfg.getRootCA()).isNull();
    }

    @Test
    void strategyInferenceRootCaOnlyShouldBeTls() {
        WorkerTlsConfig cfg = WorkerTlsConfig.builder()
                .strategy(WorkerTlsConfig.Strategy.NONE)
                .rootCA("inline-ca")
                .build();

        assertThat(cfg.getStrategy()).isEqualTo(WorkerTlsConfig.Strategy.TLS);
        assertThat(cfg.isEnabled()).isTrue();
    }

    @Test
    void strategyInferenceInlineCertPlusKeyShouldBeMtls() {
        WorkerTlsConfig cfg = WorkerTlsConfig.builder()
                .strategy(WorkerTlsConfig.Strategy.NONE)
                .cert("cert-inline")
                .key("key-inline")
                .build();

        assertThat(cfg.getStrategy()).isEqualTo(WorkerTlsConfig.Strategy.MTLS);
        assertThat(cfg.isEnabled()).isTrue();
    }

    @Test
    void strategyInferenceFilePairShouldBeMtls(@TempDir Path tempDir) throws Exception {
        Path cert = Files.createFile(tempDir.resolve("c.pem"));
        Path key  = Files.createFile(tempDir.resolve("k.pem"));

        WorkerTlsConfig cfg = WorkerTlsConfig.builder()
                .strategy(WorkerTlsConfig.Strategy.NONE)
                .certFile(cert.toFile())
                .keyFile(key.toFile())
                .build();

        assertThat(cfg.getStrategy()).isEqualTo(WorkerTlsConfig.Strategy.MTLS);
    }

    @Test
    void mTlsWithOnlyOneFileShouldThrow(@TempDir Path tempDir) throws Exception {
        Path cert = Files.createFile(tempDir.resolve("c.pem"));

        WorkerTlsConfig.Builder b = WorkerTlsConfig.builder()
                .strategy(WorkerTlsConfig.Strategy.MTLS)
                .certFile(cert.toFile());

        assertThatThrownBy(b::build).isInstanceOf(ConfigException.class);
    }

    @Test
    void mTlsWithOnlyInlineCertShouldThrow() {
        WorkerTlsConfig.Builder b = WorkerTlsConfig.builder()
                .strategy(WorkerTlsConfig.Strategy.MTLS)
                .cert("only-inline");

        assertThatThrownBy(b::build).isInstanceOf(ConfigException.class);
    }
}
