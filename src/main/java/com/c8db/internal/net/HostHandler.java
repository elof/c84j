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
 * Modifications copyright (c) 2022 Macrometa Corp All rights reserved.
 *
 */

package com.c8db.internal.net;

import com.c8db.Service;

import java.io.IOException;

public interface HostHandler {

    void applyService(Service name);

    Host get(HostHandle hostHandle, AccessType accessType);

    void success();

    void fail();

    void reset();

    void confirm();

    void close() throws IOException;

    void closeCurrentOnError();
}
