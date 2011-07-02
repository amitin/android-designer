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
package org.eclipse.wb.android.internal.gefTree;

import org.eclipse.wb.core.gefTree.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;

/**
 * {@link ILayoutEditPolicyFactory} for Android.
 * 
 * @author mitin_aa
 * @coverage android.gefTree
 */
public final class LayoutEditPolicyFactory implements ILayoutEditPolicyFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // ILayoutEditPolicyFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model) {
    // not found
    return null;
  }
}
