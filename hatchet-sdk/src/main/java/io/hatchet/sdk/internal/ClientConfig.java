package io.hatchet.sdk.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ClientConfig {
    private final String tenantId;
    private final String token;
    private final boolean noGrpcRetry;
    private final String serverURL;
    private final String grpcBroadcastAddress;
    private final WorkerTlsConfig workerTlsConfig;
    private final String namespace;
    private final Optional<String> cloudRegisterId;
    private final List<String> runnableActions;
    private final Map<String, String> presetWorkerLabels;
    private final Optional<String> autoscalingTarget;
    private final boolean debug;

    private ClientConfig(Builder b) {
        this.tenantId = b.tenantId;
        this.token = b.token;
        this.noGrpcRetry = b.noGrpcRetry;
        this.serverURL = b.serverURL;
        this.grpcBroadcastAddress = b.grpcBroadcastAddress;
        this.workerTlsConfig = b.workerTlsConfig == null
                ? WorkerTlsConfig.builder()
                .strategy(WorkerTlsConfig.Strategy.NONE)
                .build()
                : b.workerTlsConfig;
        this.namespace = b.namespace;
        this.cloudRegisterId = Optional.ofNullable(b.cloudRegisterId);
        this.runnableActions = b.runnableActions == null
                ? Collections.emptyList()
                : List.copyOf(b.runnableActions);
        this.presetWorkerLabels = b.presetWorkerLabels == null
                ? Collections.emptyMap()
                : Map.copyOf(b.presetWorkerLabels);
        this.autoscalingTarget = Optional.ofNullable(b.autoscalingTarget);
        this.debug = b.debug;
    }

    public String getTenantId() {
        return tenantId; }

    public String getToken() {
        return token;
    }

    public boolean isNoGrpcRetry() {
        return noGrpcRetry;
    }

    public String getServerURL() {
        return serverURL;
    }

    public String getGRPCBroadcastAddress() {
        return grpcBroadcastAddress;
    }

    public WorkerTlsConfig getWorkerTlsConfig() {
        return workerTlsConfig;
    }

    public String getNamespace() {
        return namespace;
    }

    public Optional<String> getCloudRegisterId() {
        return cloudRegisterId;
    }

    public List<String> getRunnableActions() {
        return runnableActions;
    }

    public Map<String, String> getPresetWorkerLabels() {
        return presetWorkerLabels;
    }

    public Optional<String> getAutoscalingTarget() {
        return autoscalingTarget;
    }

    public boolean isDebug() {
        return debug;
    }

    /**
     * Create a builder initialized with the current values for easy field overrides
     */
    public Builder toBuilder() {
        return new Builder()
                .tenantId(this.tenantId)
                .token(this.token)
                .noGrpcRetry(this.noGrpcRetry)
                .serverURL(this.serverURL)
                .grpcBroadcastAddress(this.grpcBroadcastAddress)
                .tls(this.workerTlsConfig)
                .namespace(this.namespace)
                .cloudRegisterId(this.cloudRegisterId.orElse(null))
                .runnableActions(this.runnableActions)
                .presetWorkerLabels(this.presetWorkerLabels)
                .autoscalingTarget(this.autoscalingTarget.orElse(null))
                .debug(this.debug);
    }

    public static final class Builder {
        private String tenantId;
        private String token;
        private boolean noGrpcRetry;
        private String serverURL;
        private String grpcBroadcastAddress;
        private WorkerTlsConfig workerTlsConfig;
        private String namespace = "";
        private String cloudRegisterId;
        private List<String> runnableActions;
        private Map<String, String> presetWorkerLabels;
        private String autoscalingTarget;
        private boolean debug;

        public Builder tenantId(String v) {
            tenantId = v;
            return this;
        }

        public Builder token(String v) {
            token = v;
            return this;
        }

        public Builder noGrpcRetry(boolean v) {
            noGrpcRetry = v;
            return this;
        }

        public Builder serverURL(String v) {
            serverURL = v;
            return this;
        }

        public Builder grpcBroadcastAddress(String v) {
            grpcBroadcastAddress = v;
            return this;
        }

        public Builder tls(WorkerTlsConfig v) {
            workerTlsConfig = v;
            return this;
        }

        public Builder namespace(String v) {
            namespace = v;
            return this;
        }

        public Builder cloudRegisterId(String v) {
            cloudRegisterId = v;
            return this;
        }

        public Builder runnableActions(List<String> v) {
            runnableActions = v;
            return this;
        }

        public Builder presetWorkerLabels(Map<String, String> v) {
            presetWorkerLabels = v;
            return this; }

        public Builder autoscalingTarget(String v) {
            autoscalingTarget = v;
            return this;
        }

        public Builder debug(boolean v) {
            debug = v;
            return this;
        }

        public ClientConfig build() {
            return new ClientConfig(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "ClientConfig{" +
                "tenantId='" + tenantId + '\'' +
                ", token='" + getSafeToken(token) + '\'' +
                ", noGrpcRetry=" + noGrpcRetry +
                ", serverURL='" + serverURL + '\'' +
                ", grpcBroadcastAddress='" + grpcBroadcastAddress + '\'' +
                ", workerTls=" + (workerTlsConfig == null ? "null" : workerTlsConfig.toString()) +
                ", namespace='" + namespace + '\'' +
                ", cloudRegisterId=" + cloudRegisterId +
                ", runnableActions=" + runnableActions +
                ", presetWorkerLabels=" + presetWorkerLabels +
                ", autoscalingTarget=" + autoscalingTarget +
                ", debug=" + debug +
                '}';
    }

    private static String getSafeToken(String token) {
        if (token == null) return "null";
        else return "***";
    }
}
