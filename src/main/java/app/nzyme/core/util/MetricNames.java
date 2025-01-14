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

package app.nzyme.core.util;

import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.database.DatabaseImpl;
import app.nzyme.core.integrations.geoip.GeoIpService;
import app.nzyme.core.ouis.OUIManager;
import app.nzyme.core.rest.interceptors.TapTableSizeInterceptor;
import app.nzyme.core.security.authentication.PasswordHasher;

import static com.codahale.metrics.MetricRegistry.name;

public class MetricNames {

    public static final String OUI_LOOKUP_TIMING = name(OUIManager.class, "lookup-timing");
    public static final String DATABASE_SIZE = name(DatabaseImpl.class, "size");
    public static final String GEOIP_CACHE_SIZE = name(GeoIpService.class, "cache-size");
    public static final String PGP_ENCRYPTION_TIMING = name(Crypto.class, "encryption-timing");
    public static final String PGP_DECRYPTION_TIMING = name(Crypto.class, "decryption-timing");
    public static final String PASSWORD_HASHING_TIMER = name(PasswordHasher.class, "hashing-timer");
    public static final String TAP_TABLE_REQUEST_SIZES = name(TapTableSizeInterceptor.class, "request_size");

}
