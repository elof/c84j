/*
 * DISCLAIMER
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.c8.internal;

import java.util.HashMap;
import java.util.Map;

import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.c8.entity.DocumentField;
import com.c8.entity.VertexEntity;
import com.c8.entity.VertexUpdateEntity;
import com.c8.internal.C8Executor.ResponseDeserializer;
import com.c8.internal.util.DocumentUtil;
import com.c8.internal.util.RequestUtils;
import com.c8.internal.util.C8SerializationFactory.Serializer;
import com.c8.model.DocumentReadOptions;
import com.c8.model.VertexCreateOptions;
import com.c8.model.VertexDeleteOptions;
import com.c8.model.VertexReplaceOptions;
import com.c8.model.VertexUpdateOptions;
import com.c8.util.C8Serializer;
import com.c8.velocystream.Request;
import com.c8.velocystream.RequestType;
import com.c8.velocystream.Response;

/**
 * 
 *
 */
public abstract class InternalC8VertexCollection<A extends InternalC8DB<E>, D extends InternalC8Database<A, E>, G extends InternalC8Graph<A, D, E>, E extends C8Executor>
		extends C8Executeable<E> {

	private static final String PATH_API_GHARIAL = "/_api/gharial";
	private static final String VERTEX = "vertex";

	private final G graph;
	private final String name;

	protected InternalC8VertexCollection(final G graph, final String name) {
		super(graph.executor, graph.util, graph.context);
		this.graph = graph;
		this.name = name;
	}

	public G graph() {
		return graph;
	}

	public String name() {
		return name;
	}

	protected Request dropRequest() {
		return request(graph.db().name(), RequestType.DELETE, PATH_API_GHARIAL, graph.name(), VERTEX, name);
	}

	protected <T> Request insertVertexRequest(final T value, final VertexCreateOptions options) {
		final Request request = request(graph.db().name(), RequestType.POST, PATH_API_GHARIAL, graph.name(), VERTEX,
			name);
		final VertexCreateOptions params = (options != null ? options : new VertexCreateOptions());
		request.putQueryParam(C8RequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
		request.setBody(util(Serializer.CUSTOM).serialize(value));
		return request;
	}

	protected <T> ResponseDeserializer<VertexEntity> insertVertexResponseDeserializer(final T value) {
		return new ResponseDeserializer<VertexEntity>() {
			@Override
			public VertexEntity deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody().get(VERTEX);
				final VertexEntity doc = util().deserialize(body, VertexEntity.class);
				final Map<DocumentField.Type, String> values = new HashMap<DocumentField.Type, String>();
				values.put(DocumentField.Type.ID, doc.getId());
				values.put(DocumentField.Type.KEY, doc.getKey());
				values.put(DocumentField.Type.REV, doc.getRev());
				executor.documentCache().setValues(value, values);
				return doc;
			}
		};
	}

	protected Request getVertexRequest(final String key, final DocumentReadOptions options) {
		final Request request = request(graph.db().name(), RequestType.GET, PATH_API_GHARIAL, graph.name(), VERTEX,
			DocumentUtil.createDocumentHandle(name, key));
		final DocumentReadOptions params = (options != null ? options : new DocumentReadOptions());
		request.putHeaderParam(C8RequestParam.IF_NONE_MATCH, params.getIfNoneMatch());
		request.putHeaderParam(C8RequestParam.IF_MATCH, params.getIfMatch());
		if (params.getAllowDirtyRead() == Boolean.TRUE) {
			RequestUtils.allowDirtyRead(request);
		}
		return request;
	}

	protected <T> ResponseDeserializer<T> getVertexResponseDeserializer(final Class<T> type) {
		return new ResponseDeserializer<T>() {
			@Override
			public T deserialize(final Response response) throws VPackException {
				return util(Serializer.CUSTOM).deserialize(response.getBody().get(VERTEX), type);
			}
		};
	}

	protected <T> Request replaceVertexRequest(final String key, final T value, final VertexReplaceOptions options) {
		final Request request = request(graph.db().name(), RequestType.PUT, PATH_API_GHARIAL, graph.name(), VERTEX,
			DocumentUtil.createDocumentHandle(name, key));
		final VertexReplaceOptions params = (options != null ? options : new VertexReplaceOptions());
		request.putQueryParam(C8RequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putHeaderParam(C8RequestParam.IF_MATCH, params.getIfMatch());
		request.setBody(util(Serializer.CUSTOM).serialize(value));
		return request;
	}

	protected <T> ResponseDeserializer<VertexUpdateEntity> replaceVertexResponseDeserializer(final T value) {
		return new ResponseDeserializer<VertexUpdateEntity>() {
			@Override
			public VertexUpdateEntity deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody().get(VERTEX);
				final VertexUpdateEntity doc = util().deserialize(body, VertexUpdateEntity.class);
				final Map<DocumentField.Type, String> values = new HashMap<DocumentField.Type, String>();
				values.put(DocumentField.Type.REV, doc.getRev());
				executor.documentCache().setValues(value, values);
				return doc;
			}
		};
	}

	protected <T> Request updateVertexRequest(final String key, final T value, final VertexUpdateOptions options) {
		final Request request;
		request = request(graph.db().name(), RequestType.PATCH, PATH_API_GHARIAL, graph.name(), VERTEX,
			DocumentUtil.createDocumentHandle(name, key));
		final VertexUpdateOptions params = (options != null ? options : new VertexUpdateOptions());
		request.putQueryParam(C8RequestParam.KEEP_NULL, params.getKeepNull());
		request.putQueryParam(C8RequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putHeaderParam(C8RequestParam.IF_MATCH, params.getIfMatch());
		request.setBody(
			util(Serializer.CUSTOM).serialize(value, new C8Serializer.Options().serializeNullValues(true)));
		return request;
	}

	protected <T> ResponseDeserializer<VertexUpdateEntity> updateVertexResponseDeserializer(final T value) {
		return new ResponseDeserializer<VertexUpdateEntity>() {
			@Override
			public VertexUpdateEntity deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody().get(VERTEX);
				final VertexUpdateEntity doc = util().deserialize(body, VertexUpdateEntity.class);
				final Map<DocumentField.Type, String> values = new HashMap<DocumentField.Type, String>();
				values.put(DocumentField.Type.REV, doc.getRev());
				executor.documentCache().setValues(value, values);
				return doc;
			}
		};
	}

	protected Request deleteVertexRequest(final String key, final VertexDeleteOptions options) {
		final Request request = request(graph.db().name(), RequestType.DELETE, PATH_API_GHARIAL, graph.name(), VERTEX,
			DocumentUtil.createDocumentHandle(name, key));
		final VertexDeleteOptions params = (options != null ? options : new VertexDeleteOptions());
		request.putQueryParam(C8RequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putHeaderParam(C8RequestParam.IF_MATCH, params.getIfMatch());
		return request;
	}

}
