/*******************************************************************************
 * Copyright (c) 2011 Andrey Sablin (Sablin.Andrey@gmail.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrey Sablin (Sablin.Andrey@gmail.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.android.internal.gef.part;

import org.eclipse.wb.android.internal.gef.policy.OrientationSwitcherSelectionEditPolicy;
import org.eclipse.wb.android.internal.model.layouts.LinearLayoutInfo;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.xml.gef.part.AbstractComponentEditPart;

/**
 * {@link GraphicalEditPart} for {@link LinearLayoutInfo}.
 * 
 * @author sablin_aa
 * @coverage android.gef
 */
public class LinearLayoutEditPart extends AbstractComponentEditPart {
  private final LinearLayoutInfo layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LinearLayoutEditPart(LinearLayoutInfo layout) {
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
    if (layout.isRevolvable()) {
      installEditPolicy(new OrientationSwitcherSelectionEditPolicy());
    }
  }
}