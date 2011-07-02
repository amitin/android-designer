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
package org.eclipse.wb.android.internal.model.widgets;

import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.xml.model.TopBoundsSupport;

/**
 * A {@link TopBoundsSupport} implementation for Android UI.
 * 
 * TODO: possibly use mobile devices plugin.
 * 
 * @author mitin_aa
 * @coverage android.model
 */
public class AndroidTopBoundsSupport extends TopBoundsSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AndroidTopBoundsSupport(AbstractComponentInfo componentInfo) {
    super(componentInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TopBoundsSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void apply() throws Exception {
    // TODO: not implemented
  }

  @Override
  public void setSize(int width, int height) throws Exception {
    // TODO: not implemented
  }

  @Override
  public boolean show() throws Exception {
    return false;
  }
}
