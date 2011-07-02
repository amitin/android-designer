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
package org.eclipse.wb.android.internal.model.property.editor;

import org.eclipse.wb.core.controls.CCombo3;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractComboPropertyEditor;

/**
 * Property editor for {@link Format#ENUM}.
 * 
 * @author mitin_aa
 * @coverage model.property.editor
 */
public final class EnumPropertyEditor extends AbstractComboPropertyEditor {
  private final String[] m_enumValues;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EnumPropertyEditor(String[] enumValues) {
    m_enumValues = enumValues;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractComboPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addItems(Property property, CCombo3 combo) throws Exception {
    for (String item : m_enumValues) {
      combo.add(item);
    }
  }

  @Override
  protected void selectItem(Property property, CCombo3 combo) throws Exception {
    combo.setText(getText(property));
  }

  @Override
  protected void toPropertyEx(Property property, CCombo3 combo, int index) throws Exception {
    property.setValue(m_enumValues[index]);
  }

  @Override
  protected String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (Property.UNKNOWN_VALUE.equals(value)) {
      return "";
    }
    // this means that default value passed.
    if (value instanceof Number) {
      int ordinal = ((Number) value).intValue();
      return m_enumValues[ordinal];
    }
    return (String) value;
  }
}
