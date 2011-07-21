/*******************************************************************************
 * Copyright (c) 2011 Alexander Mitin (Alexander.Mitin@gmail.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Mitin (Alexander.Mitin@gmail.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.android.internal.gef.part;

import org.eclipse.wb.android.internal.gef.policy.layouts.table.TableLayoutEditPolicy;
import org.eclipse.wb.android.internal.model.layouts.table.TableLayoutInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.RequestProcessor;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.xml.gef.part.AbstractComponentEditPart;

import java.util.List;

/**
 * {@link GraphicalEditPart} for {@link TableLayoutInfo}.
 * 
 * @author mitin_aa
 * @coverage android.gef
 */
public final class TableLayoutEditPart extends AbstractComponentEditPart {
  private final TableLayoutInfo layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableLayoutEditPart(TableLayoutInfo layout) {
    super(layout);
    this.layout = layout;
    addRequestProcessor(new RequestProcessor() {
      @Override
      public Request process(EditPart editPart, Request req) throws Exception {
        if (req instanceof ChangeBoundsRequest) {
          ChangeBoundsRequest request = (ChangeBoundsRequest) req;
          List<EditPart> editParts = request.getEditParts();
          if (!editParts.isEmpty()) {
            if (editParts.get(0).getParent().getParent() == editPart) {
              request.setType(Request.REQ_MOVE);
            } else {
              request.setType(Request.REQ_ADD);
            }
            return request;
          }
        }
        // do nothing
        return req;
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(new TableLayoutEditPolicy(layout));
  }
}