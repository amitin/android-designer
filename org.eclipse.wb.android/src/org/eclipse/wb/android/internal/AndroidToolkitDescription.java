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

import org.eclipse.wb.android.internal.preferences.IPreferenceConstants;
import org.eclipse.wb.core.branding.BrandingUtils;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;

import org.eclipse.jface.preference.IPreferenceStore;


import org.osgi.framework.Bundle;

/**
 * Toolkit Description for Android toolkit.
 * 
 * @author mitin_aa
 * @coverage android
 */
public final class AndroidToolkitDescription extends ToolkitDescription {
  public static final ToolkitDescription INSTANCE = new AndroidToolkitDescription();
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IPreferenceStore store = Activator.getDefault().getPreferenceStore();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getId() {
    return IPreferenceConstants.TOOLKIT_ID;
  }

  @Override
  public String getName() {
    return "Android toolkit";
  }

  @Override
  public String getProductName() {
    return BrandingUtils.getBranding().getProductName();
  }

  @Override
  public Bundle getBundle() {
    return Activator.getDefault().getBundle();
  }

  @Override
  public IPreferenceStore getPreferences() {
    return store;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public GenerationSettings getGenerationSettings() {
    return null;
  }
}
