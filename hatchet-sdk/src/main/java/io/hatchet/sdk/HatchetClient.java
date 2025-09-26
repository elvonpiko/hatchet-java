package io.hatchet.sdk;

import io.hatchet.sdk.exceptions.ConfigException;
import io.hatchet.sdk.internal.ClientConfig;
import io.hatchet.sdk.internal.ClientConfigLoader;
import io.hatchet.sdk.internal.WorkerTlsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class HatchetClient {
    private static final Logger log = LoggerFactory.getLogger(HatchetClient.class);
    private final ClientConfig config;

    private HatchetClient(ClientConfig config) {
        this.config = config;

        if (config.isDebug()) {
            log.debug("HatchetClient initialized with config: {}", config);
        } else {
            log.info("HatchetClient initialized with ServerURL={}, Namespace={}",
                    config.getServerURL(), config.getNamespace());
        }
    }

    /**
     * build a HatchetClient using environment variables
     * loads .env variables if present otherwise defaults if needed
     */
    public static Builder fromEnv() {
        ClientConfig cfg = new ClientConfigLoader().load();
        return new Builder(cfg);
    }

    public ClientConfig getConfig() {
        return config;
    }

    public static class Builder {
        private final ClientConfig.Builder config;

        private Builder(ClientConfig base) {
            this.config = base.toBuilder();
        }

        public Builder withTenantId(String tenantId) {
            config.tenantId(tenantId);
            return this;
        }

        public Builder withToken(String token) {
            config.token(token);
            return this;
        }

        public Builder withNoGrpcRetry(boolean v) {
            config.noGrpcRetry(v);
            return this;
        }

        public Builder withServerURL(String url) {
            config.serverURL(url);
            return this;
        }

        public Builder withGrpcBroadcastAddress(String addr) {
            config.grpcBroadcastAddress(addr);
            return this;
        }

        public Builder withTls(WorkerTlsConfig tls) {
            config.tls(tls);
            return this;
        }

        public Builder withNamespace(String ns) {
            config.namespace(ns);
            return this;
        }

        public Builder withCloudRegisterId(String id) {
            config.cloudRegisterId(id);
            return this;
        }

        public Builder withRunnableActions(List<String> actions) {
            config.runnableActions(actions);
            return this;
        }

        public Builder withPresetWorkerLabels(Map<String, String> labels) {
            config.presetWorkerLabels(labels);
            return this;
        }

        public Builder withAutoscalingTarget(String target) {
            config.autoscalingTarget(target);
            return this;
        }

        public Builder withDebug(boolean debug) {
            config.debug(debug);
            return this;
        }

        /**
         * client config validation on build for both manual or fromEnv() initialization
         */
        public HatchetClient build() {
            ClientConfig loadedConfig = config.build();

            if (loadedConfig.getServerURL() == null ||
                    loadedConfig.getServerURL().isBlank()) {
                throw new ConfigException("serverURL must be provided");
            }

            if (loadedConfig.getGRPCBroadcastAddress() == null ||
                    loadedConfig.getGRPCBroadcastAddress().isBlank()) {
                throw new ConfigException("Grpc broadcast address must be provided");
            }

            if (loadedConfig.getToken() == null ||
                    loadedConfig.getToken().isBlank()) {
                throw new ConfigException("token must be provided");
            }

            return new HatchetClient(loadedConfig);
        }
    }

    /**
     * build a HatchetClient without defaults user must set fields manually.
     */
    public static Builder builder() {
        return new Builder(ClientConfig.builder().build());
    }
}
