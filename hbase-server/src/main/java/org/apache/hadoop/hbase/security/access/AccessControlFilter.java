/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.security.access;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.KeyValueUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.exceptions.DeserializationException;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.filter.FilterBase;
import org.apache.hadoop.hbase.security.User;

/**
 * <strong>NOTE: for internal use only by AccessController implementation</strong>
 *
 * <p>
 * TODO: There is room for further performance optimization here.
 * Calling TableAuthManager.authorize() per KeyValue imposes a fair amount of
 * overhead.  A more optimized solution might look at the qualifiers where
 * permissions are actually granted and explicitly limit the scan to those.
 * </p>
 * <p>
 * We should aim to use this _only_ when access to the requested column families
 * is not granted at the column family levels.  If table or column family
 * access succeeds, then there is no need to impose the overhead of this filter.
 * </p>
 */
class AccessControlFilter extends FilterBase {

  private TableAuthManager authManager;
  private TableName table;
  private User user;

  /**
   * For Writable
   */
  AccessControlFilter() {
  }

  AccessControlFilter(TableAuthManager mgr, User ugi,
      TableName tableName) {
    authManager = mgr;
    table = tableName;
    user = ugi;
  }

  @Override
  public ReturnCode filterKeyValue(Cell c) {
    // TODO go and redo auth manager to use Cell instead of KV.
    KeyValue kv = KeyValueUtil.ensureKeyValue(c);
    if (authManager.authorize(user, table, kv, TablePermission.Action.READ)) {
      return ReturnCode.INCLUDE;
    }
    return ReturnCode.NEXT_COL;
  }

  /**
   * @return The filter serialized using pb
   */
  public byte [] toByteArray() {
    // no implementation, server-side use only
    throw new UnsupportedOperationException(
      "Serialization not supported.  Intended for server-side use only.");
  }

  /**
   * @param pbBytes A pb serialized {@link AccessControlFilter} instance
   * @return An instance of {@link AccessControlFilter} made from <code>bytes</code>
   * @throws org.apache.hadoop.hbase.exceptions.DeserializationException
   * @see {@link #toByteArray()}
   */
  public static AccessControlFilter parseFrom(final byte [] pbBytes)
  throws DeserializationException {
    // no implementation, server-side use only
    throw new UnsupportedOperationException(
      "Serialization not supported.  Intended for server-side use only.");
  }
}
