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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.controls.CCombo3;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractComboPropertyEditor;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Property editor for Format.ENUM attributes.
 * 
 * @author mitin_aa
 * @coverage model.property.editor
 */
public class DefaultEnumPropertyEditor extends AbstractComboPropertyEditor {
  private final Map<String, Integer> m_flagValues;
  private final Map<Integer, String> m_backFlagValues = Maps.newHashMap();
  private final List<String> m_flags = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DefaultEnumPropertyEditor(Map<String, Integer> flagValues) {
    m_flagValues = flagValues;
    // store back values
    Set<Entry<String, Integer>> entries = m_flagValues.entrySet();
    for (Entry<String, Integer> entry : entries) {
      // store back values
      m_backFlagValues.put(entry.getValue(), entry.getKey());
      m_flags.add(entry.getKey());
    }
  }

  @Override
  protected void addItems(Property property, CCombo3 combo) throws Exception {
    for (Entry<String, Integer> entry : m_flagValues.entrySet()) {
      combo.add(entry.getKey());
    }
  }

  @Override
  protected void selectItem(Property property, CCombo3 combo) throws Exception {
    combo.setText(getText(property));
  }

  @Override
  protected void toPropertyEx(Property property, CCombo3 combo, int index) throws Exception {
    GenericPropertyImpl prop = (GenericPropertyImpl) property;
    if (index == -1) {
      // clear value
      prop.setExpression(null, Property.UNKNOWN_VALUE);
      return;
    }
    String flagName = m_flags.get(index);
    Integer value = m_flagValues.get(flagName);
    prop.setExpression(flagName, value);
  }

  @Override
  protected String getText(Property property) throws Exception {
    Object value = property.getValue();
    return m_backFlagValues.get(value);
  }
}
