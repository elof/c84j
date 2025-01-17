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
 *
 */

package com.c8db;

import com.c8db.entity.ErrorEntity;

/**
 *
 */
public class C8DBException extends RuntimeException {

    private static final long serialVersionUID = 6165638002614173801L;
    private final ErrorEntity entity;
    private final Integer responseCode;

    public C8DBException(final ErrorEntity errorEntity) {
        super(String.format("Response: %s, Error: %s - %s", errorEntity.getCode(), errorEntity.getErrorNum(),
                errorEntity.getErrorMessage()));
        this.entity = errorEntity;
        this.responseCode = null;
    }

    public C8DBException(final String message) {
        super(message);
        this.entity = null;
        this.responseCode = null;
    }

    public C8DBException(final String message, final Integer responseCode) {
        super(message);
        this.entity = null;
        this.responseCode = responseCode;
    }

    public C8DBException(final Throwable cause) {
        super(cause);
        this.entity = null;
        this.responseCode = null;
    }

    public C8DBException(final String message,final Throwable cause){
        super(message,cause);
        this.entity = null;
        this.responseCode = null;
    }
    /**
     * @return ArangoDB error message
     */
    public String getErrorMessage() {
        return entity != null ? entity.getErrorMessage() : null;
    }

    /**
     * @return ArangoDB exception
     */
    public String getException() {
        return entity != null ? entity.getException() : null;
    }

    /**
     * @return HTTP response code
     */
    public Integer getResponseCode() {
        Integer entityResponseCode = entity != null ? entity.getCode() : null;
        return responseCode != null ? responseCode : entityResponseCode;
    }

    /**
     * @return ArangoDB error number
     */
    public Integer getErrorNum() {
        return entity != null ? entity.getErrorNum() : null;
    }

}
