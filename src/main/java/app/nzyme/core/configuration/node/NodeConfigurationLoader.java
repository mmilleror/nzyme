/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package app.nzyme.core.configuration.node;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import app.nzyme.core.configuration.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Files;

public class NodeConfigurationLoader {

    private static final Logger LOG = LogManager.getLogger(NodeConfigurationLoader.class);

    private final Config root;
    private final Config general;
    private final Config interfaces;

    public NodeConfigurationLoader(File configFile, boolean skipValidation) throws InvalidConfigurationException, IncompleteConfigurationException, FileNotFoundException {
        if (!Files.isReadable(configFile.toPath())) {
            throw new FileNotFoundException("File at [" + configFile.getPath() + "] does not exist or is not readable. Check path and permissions.");
        }

        this.root = ConfigFactory.parseFile(configFile).resolve();

        try {
            this.general = root.getConfig(ConfigurationKeys.GENERAL);
            this.interfaces = root.getConfig(ConfigurationKeys.INTERFACES);
        } catch(ConfigException e) {
            throw new IncompleteConfigurationException("Incomplete configuration.", e);
        }

        if (!skipValidation) {
            validate();
        }
    }

    public NodeConfiguration get() {
        return NodeConfiguration.create(
                parseVersionchecksEnabled(),
                parseFetchOUIsEnabled(),
                parseDatabasePath(),
                parseRestListenUri(),
                parseHttpExternalUri(),
                parsePluginDirectory(),
                parseCryptoDirectory(),
                parseNtpServer()
        );
    }

    private String parseDatabasePath() {
        return general.getString(ConfigurationKeys.DATABASE_PATH);
    }

    private boolean parseVersionchecksEnabled() {
        return general.getBoolean(ConfigurationKeys.VERSIONCHECKS);
    }

    private boolean parseFetchOUIsEnabled() {
        return general.getBoolean(ConfigurationKeys.FETCH_OUIS);
    }

    private String parsePluginDirectory() {
        return general.getString(ConfigurationKeys.PLUGIN_DIRECTORY);
    }

    private String parseCryptoDirectory() {
        return general.getString(ConfigurationKeys.CRYPTO_DIRECTORY);
    }

    private String parseNtpServer() {
        return general.getString(ConfigurationKeys.NTP_SERVER);
    }

    private URI parseRestListenUri() {
        return URI.create(interfaces.getString(ConfigurationKeys.REST_LISTEN_URI));
    }

    private URI parseHttpExternalUri() {
        return URI.create(interfaces.getString(ConfigurationKeys.HTTP_EXTERNAL_URI));
    }

    private void validate() throws IncompleteConfigurationException, InvalidConfigurationException {
        // Completeness and type validity.
        ConfigurationValidator.expect(general, ConfigurationKeys.DATABASE_PATH, ConfigurationKeys.GENERAL, String.class);
        ConfigurationValidator.expect(general, ConfigurationKeys.VERSIONCHECKS, ConfigurationKeys.GENERAL, Boolean.class);
        ConfigurationValidator.expect(general, ConfigurationKeys.FETCH_OUIS, ConfigurationKeys.GENERAL, Boolean.class);
        ConfigurationValidator.expect(general, ConfigurationKeys.PLUGIN_DIRECTORY, ConfigurationKeys.GENERAL, String.class);
        ConfigurationValidator.expect(general, ConfigurationKeys.CRYPTO_DIRECTORY, ConfigurationKeys.GENERAL, String.class);
        ConfigurationValidator.expect(general, ConfigurationKeys.NTP_SERVER, ConfigurationKeys.GENERAL, String.class);

        // Plugin directory exists and is readable?
        File pluginDirectory = new File(parsePluginDirectory());
        if (!pluginDirectory.exists()) {
            throw new InvalidConfigurationException("Plugin directory [" + parsePluginDirectory() + "] does not exist.");
        }

        if (!pluginDirectory.isDirectory()) {
            throw new InvalidConfigurationException("Plugin directory [" + parsePluginDirectory() + "] is not a directory.");
        }

        if (!pluginDirectory.canRead()) {
            throw new InvalidConfigurationException("Plugin directory [" + parsePluginDirectory() + "] is not readable.");
        }

        // Crypto directory exists and is readable?
        File cryptoKeyDirectory = new File(parseCryptoDirectory());
        if (!cryptoKeyDirectory.exists()) {
            throw new InvalidConfigurationException("Crypto directory [" + parseCryptoDirectory() + "] does not exist.");
        }

        if (!cryptoKeyDirectory.isDirectory()) {
            throw new InvalidConfigurationException("Crypto directory [" + parseCryptoDirectory() + "] is not a directory.");
        }

        if (!cryptoKeyDirectory.canRead()) {
            throw new InvalidConfigurationException("Crypto directory [" + parseCryptoDirectory() + "] is not readable.");
        }

        if (!cryptoKeyDirectory.canWrite()) {
            throw new InvalidConfigurationException("Crypto directory [" + parseCryptoDirectory() + "] is not writable.");
        }

        // REST listen URI can be parsed into a URI and is TLS.
        try {
            URI uri = parseRestListenUri();
            if (!uri.getScheme().equals("https")) {
                throw new IncompleteConfigurationException("Parameter [interfaces." + ConfigurationKeys.REST_LISTEN_URI + "] must be using HTTPS/TLS.");
            }
        } catch(IllegalArgumentException e) {
            LOG.error(e);
            throw new InvalidConfigurationException("Parameter [interfaces." + ConfigurationKeys.REST_LISTEN_URI + "] cannot be parsed into a URI. Make sure it is correct.");
        }

        // HTTP external URI can be parsed into a URI and is TLS.
        try {
            URI uri = parseHttpExternalUri();
            if (!uri.getScheme().equals("https")) {
                throw new IncompleteConfigurationException("Parameter [interfaces." + ConfigurationKeys.HTTP_EXTERNAL_URI + "] must be using HTTPS/TLS.");
            }
        } catch(IllegalArgumentException e) {
            LOG.error(e);
            throw new InvalidConfigurationException("Parameter [interfaces." + ConfigurationKeys.HTTP_EXTERNAL_URI + "] cannot be parsed into a URI. Make sure it is correct.");
        }
    }

}
