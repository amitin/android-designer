/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.android.internal.model.util;

import org.eclipse.wb.android.internal.model.widgets.ViewInfo;

import org.eclipse.jface.action.IContributionManager;

/**
 * Helper for morphing {@link ViewInfo} for one component class to another.
 * 
 * @author sablin_aa
 * @coverage android.model
 */
public class MorphingSupport<T extends ViewInfo>
    extends
      org.eclipse.wb.internal.core.xml.model.utils.MorphingSupport<T> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  protected MorphingSupport(String toolkitClassName, T component) {
    super(toolkitClassName, component);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contribution
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If possible, contributes "morph" actions.
   * 
   * @param component
   *          the {@link ViewInfo} that should be morphed.
   * @param manager
   *          the {@link IContributionManager} to add action to.
   */
  public static void contribute(ViewInfo view, IContributionManager manager) throws Exception {
    contribute(new MorphingSupport<ViewInfo>("android.view.View", view), manager);
  }
}
