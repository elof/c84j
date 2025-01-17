/*
 * Copyright (c) 2022 Macrometa Corp All rights reserved.
 */
package com.c8db.internal;

import com.c8db.C8DBException;
import com.c8db.C8KeyValue;
import com.c8db.entity.MultiDocumentEntity;
import com.c8db.entity.BaseKeyValue;
import com.c8db.entity.C8KVEntity;
import com.c8db.entity.DocumentCreateEntity;
import com.c8db.entity.DocumentDeleteEntity;
import com.c8db.internal.util.DocumentUtil;
import com.c8db.model.C8KVPairReadOptions;
import com.c8db.model.CollectionCreateOptions;
import com.c8db.model.DocumentCreateOptions;
import com.c8db.model.DocumentDeleteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class C8KeyValueImpl extends InternalC8KeyValue<C8DBImpl, C8DatabaseImpl, C8ExecutorSync>
        implements C8KeyValue {

    private static final Logger LOGGER = LoggerFactory.getLogger(C8KeyValueImpl.class);

    protected C8KeyValueImpl(final C8DatabaseImpl db, final String name) {
        super(db, name);
    }

    @Override
    public C8KVEntity create(final Boolean expiration, CollectionCreateOptions options) throws C8DBException {
        return executor.execute(createRequest(name, expiration, options), C8KVEntity.class);
    }

    @Override
    public void drop() throws C8DBException {
        executor.execute(dropRequest(), Void.class);
    }

    @Override
    public C8KVEntity truncate() throws C8DBException {
        return executor.execute(truncateRequest(), Void.class);
    }

    @Override
    public  <T> MultiDocumentEntity<DocumentCreateEntity<T>> insertKVPairs(final Collection<T>  values,
                                                                                 DocumentCreateOptions options)
            throws C8DBException {
        return executor.execute(insertKVPairsRequest(values, options), insertKVPairsResponseDeserializer(values, options));
    }

    @Override
    public DocumentDeleteEntity<Void> deleteKVPair(String key) throws C8DBException {
        return executor.execute(deleteKVPairRequest(key, new DocumentDeleteOptions()),
                deleteKVPairResponseDeserializer(Void.class));
    }

    @Override
    public MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteKVPairs(Collection<?> values) throws C8DBException {
        return executor.execute(deleteKVPairsRequest(values, new DocumentDeleteOptions()),
                deleteKVPairsResponseDeserializer(Void.class));
    }

    @Override
    public <T> T getKVPair(String key) throws C8DBException {
        DocumentUtil.validateDocumentKey(key);
        try {
            return executor.execute(getKVPairRequest(key), BaseKeyValue.class);
        } catch (final C8DBException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage(), e);
            }

            // handle Response: 404, Error: 1655 - transaction not found
            if (e.getErrorNum() != null && e.getErrorNum() == 1655) {
                throw e;
            }

            if ((e.getResponseCode() != null
                    && (e.getResponseCode() == 404 || e.getResponseCode() == 304 || e.getResponseCode() == 412))) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public <T> MultiDocumentEntity<T> getKVPairs(final Collection<String> keys, final C8KVPairReadOptions options)
            throws C8DBException {
        return executor.execute(getKVPairsRequest(keys, options), getKVPairsResponseDeserializer());
    }
}
