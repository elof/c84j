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

package com.c8.entity;

import com.arangodb.velocypack.annotations.SerializedName;

/**
 * @author Mark Vollmary
 *
 * @see <a href="https://docs.c8db.com/current/HTTP/Gharial/Edges.html#modify-an-edge">API Documentation</a>
 */
public class EdgeUpdateEntity extends DocumentEntity {

	@SerializedName("_oldRev")
	private String oldRev;

	public EdgeUpdateEntity() {
		super();
	}

	public String getOldRev() {
		return oldRev;
	}

}
