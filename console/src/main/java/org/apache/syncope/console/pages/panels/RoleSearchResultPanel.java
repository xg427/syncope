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
package org.apache.syncope.console.pages.panels;

import java.util.ArrayList;
import java.util.List;
import org.apache.syncope.client.search.NodeCond;
import org.apache.syncope.client.to.AbstractAttributableTO;
import org.apache.syncope.client.to.RoleTO;
import org.apache.syncope.client.validation.SyncopeClientCompositeErrorException;
import org.apache.syncope.console.pages.ResultStatusModalPage;
import org.apache.syncope.console.pages.RoleModalPage;
import org.apache.syncope.console.pages.StatusModalPage;
import org.apache.syncope.console.rest.AbstractAttributableRestClient;
import org.apache.syncope.console.wicket.markup.html.form.ActionLink;
import org.apache.syncope.console.wicket.markup.html.form.ActionLinksPanel;
import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

public class RoleSearchResultPanel extends AbstractSearchResultPanel {

    public <T extends AbstractAttributableTO> RoleSearchResultPanel(final String id, final boolean filtered,
            final NodeCond searchCond, final PageReference callerRef,
            final AbstractAttributableRestClient restClient) {

        super(id, filtered, searchCond, callerRef, restClient);
    }

    @Override
    protected List<IColumn<AbstractAttributableTO, String>> getColumns() {
        final List<IColumn<AbstractAttributableTO, String>> columns =
                new ArrayList<IColumn<AbstractAttributableTO, String>>();

        final String[] colnames = {"id", "name", "entitlements"};
        for (String name : colnames) {
            columns.add(
                    new PropertyColumn<AbstractAttributableTO, String>(new ResourceModel(name, name), name, name));
        }

        columns.add(new AbstractColumn<AbstractAttributableTO, String>(new ResourceModel("actions", "")) {

            @Override
            public String getCssClass() {
                return "action";
            }

            @Override
            public void populateItem(final Item<ICellPopulator<AbstractAttributableTO>> cellItem,
                    final String componentId, final IModel<AbstractAttributableTO> model) {

                final ActionLinksPanel panel = new ActionLinksPanel(componentId, model);

                panel.add(new ActionLink() {

                    private static final long serialVersionUID = -3722207913631435501L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        statusmodal.setPageCreator(new ModalWindow.PageCreator() {

                            private static final long serialVersionUID = -7834632442532690940L;

                            @Override
                            public Page createPage() {
                                return new StatusModalPage(page.getPageReference(), statusmodal, model.getObject());
                            }
                        });

                        statusmodal.show(target);
                    }
                }, ActionLink.ActionType.SEARCH, "Roles", "read");

                panel.add(new ActionLink() {

                    private static final long serialVersionUID = -3722207913631435501L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        editmodal.setPageCreator(new ModalWindow.PageCreator() {

                            private static final long serialVersionUID = -7834632442532690940L;

                            @Override
                            public Page createPage() {
                                return new RoleModalPage(
                                        page.getPageReference(), editmodal, (RoleTO) model.getObject());
                            }
                        });

                        editmodal.show(target);
                    }
                }, ActionLink.ActionType.EDIT, "Roles", "update");

                panel.add(new ActionLink() {

                    private static final long serialVersionUID = -3722207913631435501L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        try {
                            final RoleTO roleTO = (RoleTO) restClient.delete(model.getObject().getId());

                            page.setModalResult(true);

                            editmodal.setPageCreator(new ModalWindow.PageCreator() {

                                private static final long serialVersionUID = -7834632442532690940L;

                                @Override
                                public Page createPage() {
                                    return new ResultStatusModalPage(editmodal, roleTO);
                                }
                            });

                            editmodal.show(target);
                        } catch (SyncopeClientCompositeErrorException scce) {
                            error(getString("operation_error") + ": " + scce.getMessage());
                            target.add(feedbackPanel);
                        }
                    }
                }, ActionLink.ActionType.DELETE, "Roles", "delete");

                cellItem.add(panel);
            }
        });

        return columns;
    }
}
