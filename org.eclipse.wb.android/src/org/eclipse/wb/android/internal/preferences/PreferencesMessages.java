package org.eclipse.wb.android.internal.preferences;

import org.eclipse.osgi.util.NLS;

public class PreferencesMessages extends NLS {
  private static final String BUNDLE_NAME =
      "org.eclipse.wb.android.internal.preferences.PreferencesMessages"; //$NON-NLS-1$
  public static String MainPreferencePage_autoDirectEdit;
  public static String MainPreferencePage_highlightBorders;
  public static String MainPreferencePage_showImportantProperties;
  public static String MainPreferencePage_showTextInComponentsTree;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, PreferencesMessages.class);
  }

  private PreferencesMessages() {
  }
}
