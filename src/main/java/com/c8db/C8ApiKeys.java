/*
 * Copyright (c) 2021 Macrometa Corp All rights reserved.
 */

package com.c8db;

import com.c8db.entity.ApiKeyCreateEntity;
import com.c8db.entity.ApiKeyJwtEntity;
import com.c8db.entity.GeoFabricPermissions;
import com.c8db.entity.Permissions;

import java.util.Map;

/**
 * Interface for operations on administration level.
 */
public interface C8ApiKeys extends C8SerializationAccessor {

    /**
     * Validates APIKey and returns its properties
     *
     * @param apikey The apiKey
     * @return The api key properties
     */
    ApiKeyJwtEntity validateApiKey(final String apikey);

    /**
     * Validates JWT and returns its properties
     *
     * @param jwt The JWT
     * @return The JWT properties
     */
    ApiKeyJwtEntity validateJwt(final String jwt);

    /**
     * Get access level for all resources such as the GeoFabric as well as access level for collections and streams.
     * @param keyId key id of a stream. ApiKey has the next structure: tenant.keyId.hash
     * @return result map of GeoFabrics' names with access levels for GeoFabric, collections and streams.
     */
    Map<String, GeoFabricPermissions> getResourcesPermissions(final String keyId);

    /**
     * Get the GeoFabric access level
     * @param keyId key id of a stream. ApiKey has the next structure: tenant.keyId.hash
     * @return result of access level.
     */
    Permissions getGeoFabricPermissions(final String keyId);

    /**
     * Get access level for streams
     * @param keyId key id of a stream. ApiKey has the next structure: tenant.keyId.hash
     * @param full Return the full set of access levels for all streams. If set to false, return the read-only streams.
     * @return result map of streams with access levels.
     */
    Map<String, Permissions> getStreamsPermissions(final String keyId, final boolean full);

    /**
     * Get the stream access level
     * @param keyId key id of a stream. ApiKey has the next structure: tenant.keyId.hash
     * @param stream stream name
     * @return result of access level.
     */
    Permissions getStreamPermissions(final String keyId, final String stream);

    /**
     * Creates an APIKey using given keyid.
     *
     * @param keyId Key id for the apiKey.
     * @return The api key properties.
     */
    ApiKeyCreateEntity createApiKey(final String keyId);

    /**
     * Deletes an APIKey using given keyid.
     *
     * @param keyId Key id for the apiKey to be deleted.
     */
    void deleteApiKey(final String keyId);

    // TODO: Implement other required apikeys features.

}
