package io.hatchet.sdk.internal;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Loads a ClientConfig from environment variables from .env file.
 * Default configurations are set if missing from .env file for development ease.
 * <p>
 * Mapping of env variables:
 * - HATCHET_CLIENT_TENANT_ID
 * - HATCHET_CLIENT_TOKEN
 * - HATCHET_CLIENT_HOST_PORT
 * - HATCHET_CLIENT_SERVER_URL
 * - HATCHET_CLIENT_NAMESPACE
 * - HATCHET_CLOUD_REGISTER_ID
 * - HATCHET_CLOUD_ACTIONS
 * - HATCHET_CLIENT_NO_GRPC_RETRY
 * - HATCHET_CLIENT_AUTOSCALING_TARGET
 * <p>
 * Worker TLS envs:
 * - HATCHET_CLIENT_TLS_STRATEGY
 * - HATCHET_CLIENT_TLS_CERT_FILE
 * - HATCHET_CLIENT_TLS_KEY
 * - HATCHET_CLIENT_TLS_KEY_FILE
 * - HATCHET_CLIENT_TLS_ROOT_CA_FILE
 * - HATCHET_CLIENT_TLS_SERVER_NAME
 */
public final class ClientConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ClientConfigLoader.class);

    private static final String DEFAULT_SERVER_URL = "localhost:7077";
    private static final String DEFAULT_HOST_PORT = "localhost:8888";

    private final Dotenv dotenv;

    public ClientConfigLoader() {
        this.dotenv = Dotenv.configure().ignoreIfMissing().load();
    }

    public ClientConfigLoader(Dotenv dotenv) {
        this.dotenv = dotenv;
    }

    public ClientConfig load() {
        String tenantId = env("HATCHET_CLIENT_TENANT_ID");

        String token = env("HATCHET_CLIENT_TOKEN");

        String serverURL = envOrDefault("HATCHET_CLIENT_SERVER_URL",
                DEFAULT_SERVER_URL);

        String hostPort = envOrDefault("HATCHET_CLIENT_HOST_PORT",
                DEFAULT_HOST_PORT);

        String namespace = envOrDefault("HATCHET_CLIENT_NAMESPACE",
                "");

        String cloudRegisterID = env("HATCHET_CLOUD_REGISTER_ID");

        List<String> runnableActions = parseCommaList(
                envOrDefault("HATCHET_CLOUD_ACTIONS", ""));

        boolean noGrpcRetry = parseBooleanEnv(env("HATCHET_CLIENT_NO_GRPC_RETRY"),
                false);

        String autoscalingTarget = env("HATCHET_CLIENT_AUTOSCALING_TARGET");

        String tlsStrategyRaw = envOrDefault("HATCHET_CLIENT_TLS_STRATEGY",
                "none");
        WorkerTlsConfig.Strategy strategy;
        try {
            strategy = WorkerTlsConfig.Strategy.valueOf(
                    tlsStrategyRaw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            logger.debug("Unrecognized HATCHET_CLIENT_TLS_STRATEGY='{}', falling back to NONE",
                    tlsStrategyRaw);
            strategy = WorkerTlsConfig.Strategy.NONE;
        }

        WorkerTlsConfig.Builder tlsBuilder = WorkerTlsConfig.builder().strategy(strategy);

        Optional.ofNullable(env("HATCHET_CLIENT_TLS_CERT"))
                .ifPresent(tlsBuilder::cert);
        Optional.ofNullable(envFile("HATCHET_CLIENT_TLS_CERT_FILE"))
                .ifPresent(tlsBuilder::certFile);
        Optional.ofNullable(env("HATCHET_CLIENT_TLS_KEY"))
                .ifPresent(tlsBuilder::key);
        Optional.ofNullable(envFile("HATCHET_CLIENT_TLS_KEY_FILE"))
                .ifPresent(tlsBuilder::keyFile);
        Optional.ofNullable(env("HATCHET_CLIENT_TLS_ROOT_CA"))
                .ifPresent(tlsBuilder::rootCA);
        Optional.ofNullable(envFile("HATCHET_CLIENT_TLS_ROOT_CA_FILE"))
                .ifPresent(tlsBuilder::rootCAFile);
        Optional.ofNullable(env("HATCHET_CLIENT_TLS_SERVER_NAME"))
                .ifPresent(tlsBuilder::serverNameOverride);

        WorkerTlsConfig workerTls = tlsBuilder.build();

        return ClientConfig.builder()
                .tenantId(tenantId)
                .token(token)
                .noGrpcRetry(noGrpcRetry)
                .serverURL(serverURL)
                .grpcBroadcastAddress(hostPort)
                .tls(workerTls)
                .namespace(namespace)
                .cloudRegisterId(cloudRegisterID)
                .runnableActions(runnableActions)
                .autoscalingTarget(autoscalingTarget)
                .build();
    }

    private String env(String key) {
        String lower = dotenv.get(key.toLowerCase());
        if (lower != null && !lower.isBlank()) return lower.trim();
        String orig = dotenv.get(key);
        return (orig != null && !orig.isBlank()) ? orig.trim() : null;
    }

    private String envOrDefault(String key, String def) {
        String v = env(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    private File envFile(String key) {
        String p = env(key);
        if (p == null || p.isBlank()) return null;
        return new File(p);
    }

    private static boolean parseBooleanEnv(String raw, boolean defaultValue) {
        if (raw == null || raw.isBlank()) return defaultValue;
        return Boolean.parseBoolean(raw.trim());
    }

    private static List<String> parseCommaList(String raw) {
        if (raw == null || raw.isBlank()) return Collections.emptyList();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}

