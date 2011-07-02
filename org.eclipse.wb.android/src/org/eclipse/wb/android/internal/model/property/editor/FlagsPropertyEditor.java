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

import org.eclipse.wb.android.internal.model.property.IPropertiesConstants;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.style.AbstractStylePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.SubStylePropertyImpl;
import org.eclipse.wb.internal.core.model.property.editor.style.impl.BooleanStylePropertyImpl;
import org.eclipse.wb.internal.core.model.property.editor.style.impl.EnumerationStylePropertyImpl;
import org.eclipse.wb.internal.core.model.property.editor.style.impl.MacroStylePropertyImpl;
import org.eclipse.wb.internal.core.model.property.editor.style.impl.MacroUsingEqualsStylePropertyImpl;
import org.eclipse.wb.internal.core.model.property.editor.style.impl.SelectionStylePropertyImpl;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.xml.model.property.IConfigurablePropertyObject;


import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Configurable property editor for Format.FLAG attributes. See {@link AbstractStylePropertyEditor}.
 * 
 * @author mitin_aa
 * @coverage model.property.editor
 */
public final class FlagsPropertyEditor extends AbstractStylePropertyEditor
    implements
      IConfigurablePropertyObject {
  private Map<String, Integer> m_flagValues;
  private EditorContext m_context;
  private Map<String, Object> m_parameters;
  private boolean m_configured = false;

  @Override
  protected void setStyleValue(Property property, long newValue) throws Exception {
    GenericPropertyImpl prop = (GenericPropertyImpl) property;
    ensureConfigured(prop);
    String expression = getExpression(getPropertyForValue(newValue));
    prop.setExpression(expression, Property.UNKNOWN_VALUE);
  }

  @Override
  protected String getText(Property mainProperty) throws Exception {
    ensureConfigured((GenericPropertyImpl) mainProperty);
    return getExpression(mainProperty);
  }

  /**
   * @return the attribute value for external requesters.
   */
  public final String getValueSource(Property mainProperty, Object value) throws Exception {
    ensureConfigured((GenericPropertyImpl) mainProperty);
    Property property = getPropertyForValue(value);
    return getExpression(property);
  }

  private String getExpression(Property mainProperty) throws Exception {
    StringBuffer source = new StringBuffer();
    long macroFlag = 0;
    // handle macro properties
    for (SubStylePropertyImpl property : m_macroProperties) {
      String sFlag = property.getFlagValue(mainProperty);
      if (sFlag != null) {
        // add separator if need
        if (source.length() != 0) {
          source.append("|");
        }
        // add flag
        source.append(sFlag);
        macroFlag |= property.getFlag(sFlag);
      }
    }
    // handle other (set, select) properties
    for (SubStylePropertyImpl property : m_otherProperties) {
      String sFlag = property.getFlagValue(mainProperty);
      if (sFlag != null) {
        // skip current flag if it is a part of macro flag
        long flag = property.getFlag(sFlag);
        if (macroFlag != 0 && (macroFlag & flag) != 0) {
          continue;
        }
        // add separator if need
        if (source.length() != 0) {
          source.append("|");
        }
        // add flag
        source.append(sFlag);
      }
    }
    if (source.length() == 0) {
      return null;
    }
    return source.toString();
  }

  @Override
  public Property[] getProperties(Property mainProperty) throws Exception {
    ensureConfigured((GenericPropertyImpl) mainProperty);
    return super.getProperties(mainProperty);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurablePropertyObject
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(EditorContext context, Map<String, Object> parameters) throws Exception {
    m_context = context;
    m_parameters = parameters;
    m_configured = false;
  }

  /**
   * Configures the editor on demand.
   */
  @SuppressWarnings("unchecked")
  private void ensureConfigured(GenericPropertyImpl targetProperty) throws Exception {
    if (!m_configured) {
      m_properties.clear();
      GenericPropertyDescription description = targetProperty.getDescription();
      m_flagValues =
          (Map<String, Integer>) description.getArbitraryValue(IPropertiesConstants.KEY_ENUMS_FLAGS_VALUES_MAP);
      if (m_flagValues != null) {
        configureSet(m_properties, m_context, m_parameters);
        configureMacro(m_properties, m_context, m_parameters, false);
        configureMacro(m_properties, m_context, m_parameters, true);
        configureSelect(m_properties, m_context, m_parameters);
        configureEnums(m_properties, m_context, m_parameters);
        configureMacroSet(targetProperty, m_properties, m_context, m_parameters);
      }
      m_configured = true;
    }
  }

  private void configureEnums(List<SubStylePropertyImpl> properties,
      EditorContext context,
      Map<String, Object> parameters) throws Exception {
    int selectIndex = 0;
    while (true) {
      // prepare "enum" key
      String key = "enum" + Integer.toString(selectIndex++);
      if (!parameters.containsKey(key)) {
        break;
      }
      // prepare all part's
      String[] values = StringUtils.split((String) parameters.get(key));
      // title
      String title = values[0];
      // clear mask
      String mask16 = values[1]; // 0x[value]
      int clearMask = Integer.parseInt(mask16.substring(2), 16);
      // prepare flag string values
      int flagCount = 0;
      String[] flagValues = new String[values.length - 2];
      for (int i = 0; i < flagValues.length; i++) {
        String flag = values[i + 2];
        if (!StringUtils.isNumeric(flag) && !m_flagValues.containsKey(flag)) {
          continue;
        }
        flagValues[flagCount++] = flag;
      }
      // flag values
      long[] flags = new long[flagCount];
      String[] sFlags = new String[flagCount];
      for (int i = 0; i < flagCount; i++) {
        String flag = flagValues[i];
        if (StringUtils.isNumeric(flag)) {
          flags[i] = Long.parseLong(flag);
        } else {
          flags[i] = m_flagValues.get(flag);
        }
        sFlags[i] = flag;
      }
      // add property
      SubStylePropertyImpl property =
          new EnumerationStylePropertyImpl(this, title, flags, sFlags, clearMask);
      properties.add(property);
      m_otherProperties.add(property);
    }
  }

  private void configureSet(List<SubStylePropertyImpl> properties,
      EditorContext context,
      Map<String, Object> parameters) throws Exception {
    if (parameters.containsKey("set")) {
      String[] setters = StringUtils.split((String) parameters.get("set"));
      // loop of all set's
      for (int i = 0; i < setters.length; i++) {
        // prepare flag name
        String[] names = StringUtils.split(setters[i], ':');
        String flagName = names[0];
        // prepare flag value
        long flag = m_flagValues.get(flagName);
        // add property
        SubStylePropertyImpl property;
        if (names.length == 2) {
          property = new BooleanStylePropertyImpl(this, names[1], flagName, flag);
        } else {
          property = new BooleanStylePropertyImpl(this, flagName.toLowerCase(), flagName, flag);
        }
        properties.add(property);
        m_otherProperties.add(property);
      }
    }
  }

  private void configureMacroSet(GenericPropertyImpl mainProperty,
      List<SubStylePropertyImpl> properties,
      EditorContext context,
      Map<String, Object> parameters) throws Exception {
    int setIndex = 0;
    while (true) {
      // prepare "macroSet" key
      String key = "macroSet" + Integer.toString(setIndex++);
      if (!parameters.containsKey(key)) {
        break;
      }
      // prepare all part's
      String[] values = StringUtils.split((String) parameters.get(key));
      // should be at least 3 values
      if (values.length < 3) {
        addWarning(mainProperty, key, "must be at least three values length.");
        continue;
      }
      // mask
      String mainFlagString = values[0]; // 0x[value]
      String clearMaskString = values[1]; // 0x[value]
      if (!mainFlagString.startsWith("0x") || !clearMaskString.startsWith("0x")) {
        addWarning(mainProperty, key, "first two values both must be hexadecial.");
        continue;
      }
      long mainFlagMask = Long.parseLong(clearMaskString.substring(2), 16);
      long mainFlag = Long.parseLong(mainFlagString.substring(2), 16);
      // prepare flag string values
      int flagCount = 0;
      String[] flagValues = new String[values.length - 1];
      for (int i = 0; i < flagValues.length; i++) {
        String flag = values[i + 1];
        if (!m_flagValues.containsKey(flag)) {
          // no such flag 
          continue;
        }
        flagValues[flagCount++] = flag;
      }
      // add properties
      for (int i = 0; i < flagCount; i++) {
        // add properties
        String sFlag = flagValues[i];
        int flag = m_flagValues.get(sFlag);
        SubStylePropertyImpl property =
            new MaskedSetSubStylePropertyImpl(this, sFlag, sFlag, flag, mainFlag, mainFlagMask);
        properties.add(property);
        m_macroProperties.add(property);
      }
    }
  }

  private void addWarning(GenericPropertyImpl mainProperty, String name, String message) {
    m_context.addWarning(new EditorWarning("Invalid property editor parameter '"
        + name
        + "' for property '"
        + mainProperty.getDescription().getId()
        + "': "
        + message));
  }

  private void configureMacro(List<SubStylePropertyImpl> properties,
      EditorContext context,
      Map<String, Object> parameters,
      boolean useEquals) throws Exception {
    int macroIndex = 0;
    while (true) {
      // prepare "macro" key
      String key = (useEquals ? "macroUsingEquals" : "macro") + Integer.toString(macroIndex++);
      if (!parameters.containsKey(key)) {
        break;
      }
      // prepare all part's
      String[] values = StringUtils.split((String) parameters.get(key));
      // title
      String title = values[0];
      // clear mask
      String mask16 = values[1]; // 0x[value]
      int clearMask = 0xFFFFFFFF;
      boolean hasMask = false;
      if (mask16.startsWith("0x")) {
        clearMask = Integer.parseInt(mask16.substring(2), 16);
        hasMask = true;
      }
      // prepare flag string values
      int flagCount = 0;
      String[] flagValues = new String[values.length - 1];
      for (int i = hasMask ? 1 : 0; i < flagValues.length; i++) {
        String flag = values[i + 1];
        if (!m_flagValues.containsKey(flag)) {
          // no such flag 
          continue;
        }
        flagValues[flagCount++] = flag;
      }
      // flag values
      long[] flags = new long[flagCount];
      String[] sFlags = new String[flagCount + 1];
      sFlags[flagCount] = "";
      for (int i = 0; i < flagCount; i++) {
        String flag = flagValues[i];
        flags[i] = m_flagValues.get(flag);
        sFlags[i] = flag;
      }
      // add property
      SubStylePropertyImpl property =
          useEquals
              ? new MacroUsingEqualsStylePropertyImpl(this, title, flags, sFlags, clearMask)
              : new MacroStylePropertyImpl(this, title, flags, sFlags);
      properties.add(property);
      m_macroProperties.add(property);
    }
  }

  private void configureSelect(List<SubStylePropertyImpl> properties,
      EditorContext context,
      Map<String, Object> parameters) throws Exception {
    int selectIndex = 0;
    while (true) {
      // prepare "select" key
      String key = "select" + Integer.toString(selectIndex++);
      if (!parameters.containsKey(key)) {
        break;
      }
      // prepare all part's
      String[] values = StringUtils.split((String) parameters.get(key));
      // title
      String title = values[0];
      // default value
      String defaultString = values[1];
      long defaultFlag;
      if (StringUtils.isNumeric(defaultString)) {
        defaultFlag = Long.parseLong(defaultString);
      } else {
        defaultFlag = m_flagValues.get(defaultString);
      }
      // prepare flag string values
      int flagCount = 0;
      String[] flagValues = new String[values.length - 2];
      for (int i = 0; i < flagValues.length; i++) {
        String flag = values[i + 2];
        if (!StringUtils.isNumeric(flag) && !m_flagValues.containsKey(flag)) {
          // invalid flag
          continue;
        }
        flagValues[flagCount++] = flag;
      }
      // flag values
      long[] flags = new long[flagCount];
      String[] sFlags = new String[flagCount];
      for (int i = 0; i < flagCount; i++) {
        String flag = flagValues[i];
        if (StringUtils.isNumeric(flag)) {
          flags[i] = Long.parseLong(flag);
        } else {
          flags[i] = m_flagValues.get(flag);
        }
        sFlags[i] = flag;
      }
      // add property
      SubStylePropertyImpl property =
          new SelectionStylePropertyImpl(this, title, flags, sFlags, defaultFlag);
      properties.add(property);
      m_otherProperties.add(property);
    }
  }
}
