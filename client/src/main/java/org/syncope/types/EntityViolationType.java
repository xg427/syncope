/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.syncope.types;

public enum EntityViolationType {

    Standard(""),
    MultivalueAndUniqueConstraint(
    "org.syncope.core.validation.schema.multivalueAndUniqueConstraint"),
    MoreThanOneNonNull(
    "org.syncope.core.validation.attrvalue.moreThanOneNonNull"),
    InvalidValues(
    "org.syncope.core.validation.attr.invalidValues");

    private String message;

    private EntityViolationType(final String message) {
        this.message = message;
    }

    public void setMessageTemplate(final String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this == Standard ? message : super.toString();
    }
}
