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

/**
 * Contains various preference constants for Android.
 * 
 * @author mitin_aa
 * @coverage android.preferences
 */
public interface IPreferenceConstants
    extends
      org.eclipse.wb.internal.core.preferences.IPreferenceConstants {
  String TOOLKIT_ID = "org.eclipse.wb.android";
  String P_ANDROID_SDK_LOCATION = TOOLKIT_ID + ".sdk.location";
}
