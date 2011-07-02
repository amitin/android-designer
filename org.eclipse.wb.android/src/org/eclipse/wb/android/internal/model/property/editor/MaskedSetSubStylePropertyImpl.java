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

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.AbstractStylePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.SubStylePropertyImpl;
import org.eclipse.wb.internal.core.model.property.editor.style.actions.BooleanStyleAction;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

/**
 * Special support for optional Android flags.
 * 
 * Usage: 0x'main_flag' 0x'main_flag_mask' flag1 flag2 ...<br>
 * For example textMultiLine (0x00020001) | textLongMessage (0x00000051). Here are
 * 
 * <pre> 
 * main_flag == 0x01, main_flag_mask == 0x0F
 * </pre>
 * 
 * @author mitin_aa
 * @coverage model.property.editor
 */
public final class MaskedSetSubStylePropertyImpl extends SubStylePropertyImpl {
  private final String m_sFlag;
  private final long m_flag;
  private final long m_mainFlag;
  private final long m_mainFlagMask;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MaskedSetSubStylePropertyImpl(AbstractStylePropertyEditor editor,
      String title,
      String sFlag,
      long flag,
      long mainFlag,
      long mainFlagMask) {
    super(editor, title);
    m_sFlag = sFlag;
    m_mainFlag = mainFlag;
    m_mainFlagMask = mainFlagMask;
    m_flag = flag & ~m_mainFlagMask;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // As string
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void getAsString(StringBuilder builder) {
    builder.append(getTitle());
    builder.append(" boolean: ");
    builder.append(m_sFlag);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public PropertyEditor createEditor() {
    return BooleanPropertyEditor.INSTANCE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public long getFlag(String sFlag) {
    return m_flag;
  }

  @Override
  public String getFlagValue(Property property) throws Exception {
    return isSet(property) ? m_sFlag : null;
  }

  private boolean isSet(Property property) throws Exception {
    long style = getStyleValue(property);
    long maskedStyle = style & m_mainFlagMask;
    if (maskedStyle != m_mainFlag) {
      // considered as 'not set' if no 'main' flag set
      return false;
    }
    return (style & m_flag) != 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getValue(Property property) throws Exception {
    return isSet(property) ? Boolean.TRUE : Boolean.FALSE;
  }

  @Override
  public void setValue(Property property, Object value) throws Exception {
    boolean setValue = value != Property.UNKNOWN_VALUE && (Boolean) value;
    long style = getStyleValue(property);
    if (setValue) {
      long maskedStyle = style & m_mainFlagMask;
      if (maskedStyle != m_mainFlag) {
        style = m_mainFlag | m_flag;
      } else {
        style |= m_flag;
      }
    } else {
      style ^= m_flag;
    }
    setStyleValue(property, style);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Popup menu
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void contributeActions(Property property, IMenuManager manager) throws Exception {
    // create
    IAction action = new BooleanStyleAction(property, this);
    // configure
    action.setChecked(isSet(property));
    // add to menu
    manager.add(action);
  }
}
