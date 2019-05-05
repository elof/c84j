/*
 * DISCLAIMER
 *
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
 *
 */

package com.c8.internal.util;

import com.c8.internal.net.AccessType;
import com.c8.velocystream.Request;

/**
 * 
 *
 */
public final class RequestUtils {

	public static final String HEADER_ALLOW_DIRTY_READ = "X-C8-Allow-Dirty-Read";

	private RequestUtils() {
		super();
	}

	public static Request allowDirtyRead(final Request request) {
		return request.putHeaderParam(HEADER_ALLOW_DIRTY_READ, "true");
	}

	public static AccessType determineAccessType(final Request request) {
		if (request.getHeaderParam().containsKey(HEADER_ALLOW_DIRTY_READ)) {
			return AccessType.DIRTY_READ;
		}
		switch (request.getRequestType()) {
		case GET:
			return AccessType.READ;
		default:
			return AccessType.WRITE;
		}
	}

}
