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

import org.eclipse.wb.android.internal.model.layouts.LinearLayoutInfo;
import org.eclipse.wb.core.gef.header.IHeadersProvider;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

import org.eclipse.jface.action.IMenuManager;

import java.util.List;

/**
 * Policy for TableRow. Used for re-route commands into TableLayout policy.
 * 
 * @author mitin_aa
 * @coverage android.gef
 */
public final class TableRowEditPart extends LinearLayoutEditPart {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableRowEditPart(LinearLayoutInfo layout) {
    super(layout);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Re-routing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Command getCommand(Request request) {
    if (request instanceof ChangeBoundsRequest) {
      return getParent().getCommand(request);
    }
    return super.getCommand(request);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Forward headers
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    EditPolicy editPolicy = getParent().getEditPolicy(EditPolicy.LAYOUT_ROLE);
    if (editPolicy != null && editPolicy instanceof IHeadersProvider) {
      installEditPolicy(new HeadersForwardEditPolicy((IHeadersProvider) editPolicy));
    }
  }

  /**
   * Forward the headers to parent part.
   * 
   * @author mitin_aa
   */
  private static final class HeadersForwardEditPolicy extends EditPolicy
      implements
        IHeadersProvider {
    private final IHeadersProvider m_headersProvider;

    public HeadersForwardEditPolicy(IHeadersProvider headersProvider) {
      m_headersProvider = headersProvider;
    }

    public LayoutEditPolicy getContainerLayoutPolicy(boolean horizontal) {
      return m_headersProvider.getContainerLayoutPolicy(horizontal);
    }

    public List<?> getHeaders(boolean horizontal) {
      return m_headersProvider.getHeaders(horizontal);
    }

    public EditPart createHeaderEditPart(boolean horizontal, Object model) {
      return m_headersProvider.createHeaderEditPart(horizontal, model);
    }

    public void buildContextMenu(IMenuManager manager, boolean horizontal) {
      m_headersProvider.buildContextMenu(manager, horizontal);
    }

    public void handleDoubleClick(boolean horizontal) {
      m_headersProvider.handleDoubleClick(horizontal);
    }
  }
}
