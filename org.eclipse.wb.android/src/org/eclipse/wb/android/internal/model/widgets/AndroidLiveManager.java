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

import org.eclipse.wb.internal.core.model.util.live.ILiveCacheEntry;
import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.xml.model.utils.AbstractLiveManager;

import org.eclipse.swt.graphics.Image;

/**
 * Default live components manager implementation for Android toolkit.
 * 
 * @author mitin_aa
 * @coverage android.model.widgets
 */
public class AndroidLiveManager extends AbstractLiveManager {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AndroidLiveManager(ViewInfo widget) {
    super(widget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Abstract_LiveManager
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractComponentInfo createLiveComponent() throws Exception {
    // TODO:
    return null;
  }

  @Override
  protected ILiveCacheEntry createComponentCacheEntry(AbstractComponentInfo liveComponentInfo) {
    // TODO:
    return null;
  }

  @Override
  protected ILiveCacheEntry createComponentCacheEntryEx(Throwable e) {
    // TODO:
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Image} of this component.
   */
  public Image getImage() {
    // TODO:
    return null;
  }
}
