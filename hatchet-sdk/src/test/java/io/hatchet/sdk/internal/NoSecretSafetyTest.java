package io.hatchet.sdk.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Verify that toString()/debug output is safe and does not leak secrets.
 */
class NoSecretSafetyTest {

    @Test
    void toStringDoesNotLeakSecrets() {
        WorkerTlsConfig tls = WorkerTlsConfig.builder()
                .strategy(WorkerTlsConfig.Strategy.MTLS)
                .cert("inline-cert")
                .key("super-secret-key")
                .build();

        String s = tls.toString();
        assertThat(s).doesNotContain("inline-cert");
        assertThat(s).doesNotContain("super-secret-key");

        ClientConfig cfg = ClientConfig.builder()
                .token("verylongtokensecret12345")
                .serverURL("localhost")
                .build();

        String c = cfg.toString();
        assertThat(c).doesNotContain("verylongtokensecret12345");
        assertThat(c).contains("***");
    }
}
