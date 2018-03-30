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
package org.apache.syncope.core.provisioning.java.data;

import org.apache.syncope.common.lib.to.RemediationTO;
import org.apache.syncope.core.persistence.api.entity.Remediation;
import org.apache.syncope.core.provisioning.api.data.RemediationDataBinder;
import org.apache.syncope.core.spring.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class RemediationDataBinderImpl implements RemediationDataBinder {

    private static final String[] IGNORE_PROPERTIES = {
        "payload", "anyTOPayload", "anyPatchPayload", "keyPayload", "pullTask" };

    @Override
    public RemediationTO getRemediationTO(final Remediation remediation) {
        RemediationTO remediationTO = new RemediationTO();

        BeanUtils.copyProperties(remediation, remediationTO);

        switch (remediation.getOperation()) {
            case CREATE:
                remediationTO.setAnyTOPayload(
                        remediation.getPayloadAsTO(remediation.getAnyTypeKind().getTOClass()));
                break;

            case UPDATE:
                remediationTO.setAnyPatchPayload(
                        remediation.getPayloadAsPatch(remediation.getAnyTypeKind().getPatchClass()));
                break;

            case DELETE:
                remediationTO.setKeyPayload(remediation.getPayloadAsKey());
                break;

            default:
        }

        if (remediation.getPullTask() != null) {
            remediationTO.setPullTask(remediation.getPullTask().getKey());
            remediationTO.setResource(remediation.getPullTask().getResource().getKey());
        }

        return remediationTO;
    }

}