package io.hatchet.sdk.internal;

import io.hatchet.sdk.exceptions.ConfigException;

import java.io.File;
import java.util.Objects;

/**
 * Plain data plus validation here no grpc channel application
 */
public final class WorkerTlsConfig {

    public enum Strategy { NONE, TLS, MTLS }

    private final Strategy strategy;

    // inline pem
    private final String cert;

    // file path to cert
    private final File certFile;

    private final String key;

    private final File keyFile;

    private final String rootCA;

    private final File rootCAFile;

    private final String serverNameOverride;

    private WorkerTlsConfig(Builder b) {
        this.strategy = b.strategy;
        this.cert = b.cert;
        this.certFile = b.certFile;
        this.key = b.key;
        this.keyFile = b.keyFile;
        this.rootCA = b.rootCA;
        this.rootCAFile = b.rootCAFile;
        this.serverNameOverride = b.serverNameOverride;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public String getCert() {
        return cert;
    }

    public File getCertFile() {
        return certFile;
    }

    public String getKey() {
        return key;
    }

    public File getKeyFile() {
        return keyFile;
    }

    public String getRootCA() {
        return rootCA;
    }

    public File getRootCAFile() {
        return rootCAFile;
    }

    public String getServerNameOverride() {
        return serverNameOverride;
    }

    public boolean isEnabled() {
        return strategy != Strategy.NONE;
    }

    public static final class Builder {
        private Strategy strategy = Strategy.NONE;
        private String cert;
        private File certFile;
        private String key;
        private File keyFile;
        private String rootCA;
        private File rootCAFile;
        private String serverNameOverride;

        public Builder strategy(Strategy s) {
            this.strategy = Objects.requireNonNull(s);
            return this;
        }

        public Builder cert(String cert) {
            this.cert = cert; return this;
        }

        public Builder certFile(File certFile) {
            this.certFile = certFile;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder keyFile(File keyFile) {
            this.keyFile = keyFile;
            return this;
        }

        public Builder rootCA(String rootCA) {
            this.rootCA = rootCA;
            return this;
        }

        public Builder rootCAFile(File rootCAFile) {
            this.rootCAFile = rootCAFile;
            return this;
        }

        public Builder serverNameOverride(String serverNameOverride) {
            this.serverNameOverride = serverNameOverride;
            return this;
        }

        public WorkerTlsConfig build() {
            // normalize blanks
            if (cert != null && cert.isBlank()) cert = null;
            if (key != null && key.isBlank()) key = null;
            if (rootCA != null && rootCA.isBlank()) rootCA = null;

            // file existence checks
            if (certFile != null && (!certFile.exists()
                    || !certFile.isFile()
                    || !certFile.canRead())) {
                throw new ConfigException("TLS certFile does not exist or is not readable: "
                        + certFile.getPath());
            }
            if (keyFile != null && (!keyFile.exists()
                    || !keyFile.isFile()
                    || !keyFile.canRead())) {
                throw new ConfigException("TLS keyFile does not exist or is not readable: "
                        + keyFile.getPath());
            }
            if (rootCAFile != null && (!rootCAFile.exists()
                    || !rootCAFile.isFile()
                    || !rootCAFile.canRead())) {
                throw new ConfigException("TLS rootCAFile does not exist or is not readable: "
                        + rootCAFile.getPath());
            }

            // infer strategy if none
            if (strategy == Strategy.NONE) {
                boolean hasClient = (certFile != null && keyFile != null)
                        || (cert != null && key != null);
                boolean hasRoot = rootCAFile != null || rootCA != null;
                if (hasClient) strategy = Strategy.MTLS;
                else if (hasRoot) strategy = Strategy.TLS;
            }

            // validate mtls
            if (strategy == Strategy.MTLS) {
                boolean hasFiles = certFile != null && keyFile != null;
                boolean hasInline = cert != null && key != null;
                if (!hasFiles && !hasInline) {
                    throw new ConfigException(
                            "MTLS selected but client certificate and/or key not provided; provide both inline or both files.");
                }
            }

            return new WorkerTlsConfig(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        String certFileName = certFile == null ? "null" : certFile.getName();
        String rootCAFileName = rootCAFile == null ? "null" : rootCAFile.getName();
        String keyFileName = keyFile == null ? "null" : keyFile.getName();

        return "WorkerTlsConfig{" +
                "strategy=" + strategy +
                ", certFile=" + certFileName +
                ", keyFile=" + keyFileName +
                ", rootCAFile=" + rootCAFileName +
                ", serverNameOverride=" + serverNameOverride +
                ", certInlined=" + (cert != null) +
                ", keyInlined=" + (key != null) +
                ", rootCAInlined=" + (rootCA != null) +
                "}";
    }
}
