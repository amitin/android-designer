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
package org.eclipse.wb.android.internal;

import org.eclipse.wb.internal.core.model.description.IToolkitProvider;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;

/**
 * Implementation of {@link IToolkitProvider} for Android.
 * 
 * @author mitin_aa
 * @coverage android
 */
public final class ToolkitProvider implements IToolkitProvider {
  public static final ToolkitDescription DESCRIPTION = AndroidToolkitDescription.INSTANCE;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IToolkitProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public ToolkitDescription getDescription() {
    return DESCRIPTION;
  }
}
