package app.nzyme.core.configuration.node;

import com.google.auto.value.AutoValue;

import java.net.URI;


@AutoValue
public abstract class NodeConfiguration {

    public abstract boolean versionchecksEnabled();
    public abstract boolean fetchOuis();

    public abstract String databasePath();

    public abstract URI restListenUri();
    public abstract URI httpExternalUri();

    public abstract String pluginDirectory();

    public abstract String cryptoDirectory();

    public abstract String ntpServer();

    public static NodeConfiguration create(boolean versionchecksEnabled, boolean fetchOuis, String databasePath, URI restListenUri, URI httpExternalUri, String pluginDirectory, String cryptoDirectory, String ntpServer) {
        return builder()
                .versionchecksEnabled(versionchecksEnabled)
                .fetchOuis(fetchOuis)
                .databasePath(databasePath)
                .restListenUri(restListenUri)
                .httpExternalUri(httpExternalUri)
                .pluginDirectory(pluginDirectory)
                .cryptoDirectory(cryptoDirectory)
                .ntpServer(ntpServer)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_NodeConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder versionchecksEnabled(boolean versionchecksEnabled);

        public abstract Builder fetchOuis(boolean fetchOuis);

        public abstract Builder databasePath(String databasePath);

        public abstract Builder restListenUri(URI restListenUri);

        public abstract Builder httpExternalUri(URI httpExternalUri);

        public abstract Builder pluginDirectory(String pluginDirectory);

        public abstract Builder cryptoDirectory(String cryptoDirectory);

        public abstract Builder ntpServer(String ntpServer);

        public abstract NodeConfiguration build();
    }

}