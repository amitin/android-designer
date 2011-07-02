package org.eclipse.wb.android.internal.model.property.editor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.style.AbstractStylePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.SubStylePropertyImpl;
import org.eclipse.wb.internal.core.model.property.editor.style.impl.BooleanStylePropertyImpl;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Property editor for Format.FLAG attributes.
 * 
 * @author mitin_aa
 * @coverage model.property.editor
 */
public final class DefaultFlagsPropertyEditor extends AbstractStylePropertyEditor {
  private final Map<String, Integer> m_flagValues;
  private final Map<Integer, String> m_backFlagValues = Maps.newHashMap();
  private final List<Integer> m_flags = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DefaultFlagsPropertyEditor(Map<String, Integer> flagValues) {
    m_flagValues = flagValues;
    // prepare sub properties & store back values
    Set<Entry<String, Integer>> entries = m_flagValues.entrySet();
    for (Entry<String, Integer> entry : entries) {
      // store back values
      m_backFlagValues.put(entry.getValue(), entry.getKey());
      m_flags.add(entry.getValue());
      // add sub-property
      Integer value = entry.getValue();
      // skip zero flag value
      if (value == 0) {
        continue;
      }
      String flagName = entry.getKey();
      BooleanStylePropertyImpl property =
          new BooleanStylePropertyImpl(this, flagName, flagName, value);
      m_properties.add(property);
    }
  }

  @Override
  protected void setStyleValue(Property property, long newValue) throws Exception {
    String expression = getExpression(getPropertyForValue(newValue));
    ((GenericPropertyImpl) property).setExpression(expression, Property.UNKNOWN_VALUE);
  }

  @Override
  protected String getText(Property mainProperty) throws Exception {
    return getExpression(mainProperty);
  }

  private String getExpression(Property mainProperty) throws Exception {
    StringBuffer source = new StringBuffer();
    for (SubStylePropertyImpl property : m_properties) {
      String sFlag = property.getFlagValue(mainProperty);
      if (sFlag != null) {
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
}