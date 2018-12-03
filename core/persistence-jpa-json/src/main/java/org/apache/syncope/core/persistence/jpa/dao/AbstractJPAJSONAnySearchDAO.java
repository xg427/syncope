/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.core.persistence.jpa.dao;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.common.lib.types.AttrSchemaType;
import org.apache.syncope.core.persistence.api.dao.search.AttributeCond;

abstract class AbstractJPAJSONAnySearchDAO extends JPAAnySearchDAO {

    @Override
    SearchSupport buildSearchSupport(final AnyTypeKind kind) {
        return new SearchSupport(kind);
    }

    @Override
    protected void processOBS(final SearchSupport svs, final OrderBySupport obs, final StringBuilder where) {
        obs.views.forEach(searchView -> {
            where.append(',').
                    append(searchView.name).
                    append(' ').append(searchView.alias);
        });
    }

    protected Pair<String, Boolean> schemaInfo(final AttrSchemaType schemaType, final AttributeCond.Type condType) {
        String key;
        boolean lower = false;
        switch (schemaType) {
            case Boolean:
                key = "booleanValue";
                break;

            case Date:
                key = "dateValue";
                break;

            case Double:
                key = "doubleValue";
                break;

            case Long:
                key = "longValue";
                break;

            case Binary:
                key = "binaryValue";
                break;

            default:
                lower = condType == AttributeCond.Type.IEQ || condType == AttributeCond.Type.ILIKE;
                key = "stringValue";
        }

        return Pair.of(key, lower);
    }

    protected void appendOp(final StringBuilder query, final AttributeCond.Type condType, final boolean not) {
        switch (condType) {
            case LIKE:
            case ILIKE:
                if (not) {
                    query.append("NOT ");
                }
                query.append(" LIKE ");
                break;

            case GE:
                if (not) {
                    query.append('<');
                } else {
                    query.append(">=");
                }
                break;

            case GT:
                if (not) {
                    query.append("<=");
                } else {
                    query.append('>');
                }
                break;

            case LE:
                if (not) {
                    query.append('>');
                } else {
                    query.append("<=");
                }
                break;

            case LT:
                if (not) {
                    query.append(">=");
                } else {
                    query.append('<');
                }
                break;

            case EQ:
            case IEQ:
            default:
                if (not) {
                    query.append('!');
                }
                query.append('=');
        }
    }
}
