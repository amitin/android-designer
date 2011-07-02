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
package org.eclipse.wb.android.internal.preferences;

import org.eclipse.wb.android.internal.ToolkitProvider;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


/**
 * Initializer for Android Designer preferences.
 * 
 * @author mitin_aa
 * @coverage android.preferences
 */
public final class PreferenceInitializer extends AbstractPreferenceInitializer {
  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore preferences = ToolkitProvider.DESCRIPTION.getPreferences();
    // general
    preferences.setDefault(IPreferenceConstants.P_GENERAL_HIGHLIGHT_CONTAINERS, true);
    preferences.setDefault(IPreferenceConstants.P_GENERAL_TEXT_SUFFIX, true);
    preferences.setDefault(IPreferenceConstants.P_GENERAL_IMPORTANT_PROPERTIES_AFTER_ADD, false);
    preferences.setDefault(IPreferenceConstants.P_GENERAL_DIRECT_EDIT_AFTER_ADD, true);
  }
}
