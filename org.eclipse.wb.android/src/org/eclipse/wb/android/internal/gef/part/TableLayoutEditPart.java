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
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.xml.gef.part.AbstractComponentEditPart;


/**
 * {@link GraphicalEditPart} for {@link TableLayoutInfo}.
 * 
 * @author mitin_aa
 * @coverage android.gef
 */
public class TableLayoutEditPart extends AbstractComponentEditPart {
  private final TableLayoutInfo layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableLayoutEditPart(TableLayoutInfo layout) {
    super(layout);
    this.layout = layout;
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