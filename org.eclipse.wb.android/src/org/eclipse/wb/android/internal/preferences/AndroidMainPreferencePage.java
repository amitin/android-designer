package org.eclipse.wb.android.internal.preferences;

import org.eclipse.wb.android.internal.ToolkitProvider;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.preferences.bind.AbstractBindingPreferencesPage;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.ui.AbstractBindingComposite;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;


/**
 * {@link PreferencePage} with general Android preferences.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage android.preferences.ui
 */
public final class AndroidMainPreferencePage extends AbstractBindingPreferencesPage {
  public static final String ID = IPreferenceConstants.TOOLKIT_ID
      + ".preferences.AndroidMainPreferencePage";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AndroidMainPreferencePage() {
    super(ToolkitProvider.DESCRIPTION);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractBindingComposite createBindingComposite(Composite parent) {
    return new ContentsComposite(parent, m_bindManager, m_preferences);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  private class ContentsComposite extends AbstractBindingComposite {
    public ContentsComposite(Composite parent,
        DataBindManager bindManager,
        IPreferenceStore preferences) {
      super(parent, bindManager, preferences);
      GridLayoutFactory.create(this).noMargins().columns(2);
      checkButton(
          this,
          2,
          PreferencesMessages.MainPreferencePage_highlightBorders,
          IPreferenceConstants.P_GENERAL_HIGHLIGHT_CONTAINERS);
      checkButton(
          this,
          2,
          PreferencesMessages.MainPreferencePage_showTextInComponentsTree,
          IPreferenceConstants.P_GENERAL_TEXT_SUFFIX);
      checkButton(
          this,
          2,
          PreferencesMessages.MainPreferencePage_showImportantProperties,
          IPreferenceConstants.P_GENERAL_IMPORTANT_PROPERTIES_AFTER_ADD);
      checkButton(
          this,
          2,
          PreferencesMessages.MainPreferencePage_autoDirectEdit,
          IPreferenceConstants.P_GENERAL_DIRECT_EDIT_AFTER_ADD);
    }
  }
}
