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

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.syncope.core.persistence.api.dao.search.AttributeCond;
import org.apache.syncope.core.persistence.api.dao.search.OrderByClause;
import org.apache.syncope.core.persistence.api.entity.AnyUtils;
import org.apache.syncope.core.persistence.api.entity.PlainAttr;
import org.apache.syncope.core.persistence.api.entity.PlainAttrUniqueValue;
import org.apache.syncope.core.persistence.api.entity.PlainAttrValue;
import org.apache.syncope.core.persistence.api.entity.PlainSchema;
import org.apache.syncope.core.provisioning.api.serialization.POJOHelper;
import org.apache.syncope.core.persistence.api.entity.JSONPlainAttr;

public class MyJPAJSONAnySearchDAO extends AbstractJPAJSONAnySearchDAO {

    @Override
    protected void parseOrderByForPlainSchema(
            final SearchSupport svs,
            final OrderBySupport obs,
            final OrderBySupport.Item item,
            final OrderByClause clause,
            final PlainSchema schema,
            final String fieldName) {

        // keep track of involvement of non-mandatory schemas in the order by clauses
        obs.nonMandatorySchemas = !"true".equals(schema.getMandatoryCondition());

        obs.views.add(svs.field());

        item.select = svs.field().alias + "."
                + (schema.isUniqueConstraint() ? "attrUniqueValue" : svs.fieldName(schema.getType()))
                + " AS " + fieldName;
        item.where = "plainSchema = '" + fieldName + "'";
        item.orderBy = fieldName + " " + clause.getDirection().name();
    }

    private void fillAttrQuery(
            final AnyUtils anyUtils,
            final StringBuilder query,
            final PlainAttrValue attrValue,
            final PlainSchema schema,
            final AttributeCond cond,
            final boolean not,
            final List<Object> parameters) {

        Pair<String, Boolean> schemaInfo = schemaInfo(schema.getType(), cond.getType());

        if (!not && cond.getType() == AttributeCond.Type.EQ) {
            PlainAttr<?> container = anyUtils.newPlainAttr();
            container.setSchema(schema);
            if (attrValue instanceof PlainAttrUniqueValue) {
                container.setUniqueValue((PlainAttrUniqueValue) attrValue);
            } else {
                ((JSONPlainAttr) container).add(attrValue);
            }

            query.append("JSON_CONTAINS(plainAttrs, '").
                    append(POJOHelper.serialize(Arrays.asList(container))).
                    append("')");
        } else {
            query.append("plainSchema = ?").append(setParameter(parameters, cond.getSchema())).
                    append(" AND ").
                    append(schemaInfo.getRight() ? "LOWER(" : "").
                    append(schema.isUniqueConstraint()
                            ? "attrUniqueValue ->> '$." + schemaInfo.getLeft() + "'"
                            : schemaInfo.getLeft()).
                    append(schemaInfo.getRight() ? ")" : "");

            appendOp(query, cond.getType(), not);

            query.append(schemaInfo.getRight() ? "LOWER(" : "").
                    append("?").append(setParameter(parameters, cond.getExpression())).
                    append(schemaInfo.getRight() ? ")" : "");
        }
    }

    @Override
    protected String getQuery(
            final AttributeCond cond,
            final boolean not,
            final List<Object> parameters,
            final SearchSupport svs) {

        Pair<PlainSchema, PlainAttrValue> checked;
        try {
            checked = check(cond, svs.anyTypeKind);
        } catch (IllegalArgumentException e) {
            return EMPTY_QUERY;
        }

        // normalize NULL / NOT NULL checks
        if (not) {
            if (cond.getType() == AttributeCond.Type.ISNULL) {
                cond.setType(AttributeCond.Type.ISNOTNULL);
            } else if (cond.getType() == AttributeCond.Type.ISNOTNULL) {
                cond.setType(AttributeCond.Type.ISNULL);
            }
        }

        StringBuilder query = new StringBuilder("SELECT DISTINCT any_id FROM ").
                append(svs.field().name).append(" WHERE ");
        switch (cond.getType()) {
            case ISNOTNULL:
                query.append("JSON_SEARCH(plainAttrs, 'one', '").
                        append(checked.getLeft().getKey()).
                        append("', NULL, '$[*].schema') IS NOT NULL");
                break;

            case ISNULL:
                query.append("JSON_SEARCH(plainAttrs, 'one', '").
                        append(checked.getLeft().getKey()).
                        append("', NULL, '$[*].schema') IS NULL");
                break;

            default:
                fillAttrQuery(anyUtilsFactory.getInstance(svs.anyTypeKind),
                        query, checked.getRight(), checked.getLeft(), cond, not, parameters);
        }

        return query.toString();
    }
}
