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
 * Copyright (c) 2022 Macrometa Corp All rights reserved.
 */

package com.c8db.entity;

public enum CollectionType {

    KV(1), DOCUMENT(2), EDGES(3), DYNAMO(4);

    private final int type;

    CollectionType(final int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static CollectionType fromType(final int type) {
        for (final CollectionType cType : CollectionType.values()) {
            if (cType.type == type) {
                return cType;
            }
        }
        return null;
    }
}
