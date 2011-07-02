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
package org.eclipse.wb.android.internal.model.property.converter;

import org.eclipse.wb.internal.core.xml.model.description.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.xml.model.property.converter.ExpressionConverter;

import com.android.ide.common.api.IAttributeInfo.Format;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Factory for create expression converters for Android attributes types.
 * 
 * @author mitin_aa
 * @coverage android.model.property
 */
@SuppressWarnings("restriction")
public final class AndroidConverters {
  private AndroidConverters() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public static AndroidObjectExpressionConverter getObjectConverter(Format[] formats,
      Map<String, Integer> flagsValues) {
    if (Format.FLAG.in(formats)) {
      if (flagsValues != null) {
        return new FlagsConverter(flagsValues);
      }
    }
    if (Format.ENUM.in(formats)) {
      if (flagsValues != null) {
        return new EnumConverter(flagsValues);
      }
    }
    if (Format.BOOLEAN.in(formats)) {
      return BooleanConverter.INSTANCE;
    }
    if (Format.INTEGER.in(formats)) {
      return IntegerConverter.INSTANCE;
    }
    if (Format.FLOAT.in(formats)) {
      return FloatConverter.INSTANCE;
    }
    return StringConverter.INSTANCE;
  }

  /**
   * 
   */
  public static ExpressionConverter getExpressionConverter(Format[] formats,
      Map<String, Integer> enumFlagValues) throws Exception {
    if (Format.FLAG.in(formats) || Format.ENUM.in(formats)) {
      return null;
    }
    if (Format.BOOLEAN.in(formats)) {
      return DescriptionPropertiesHelper.getConverterForType(boolean.class);
    }
    if (Format.INTEGER.in(formats)) {
      return DescriptionPropertiesHelper.getConverterForType(int.class);
    }
    if (Format.FLOAT.in(formats)) {
      return DescriptionPropertiesHelper.getConverterForType(float.class);
    }
    return DescriptionPropertiesHelper.getConverterForType(String.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FlagsConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class FlagsConverter extends AndroidObjectExpressionConverter {
    private final Map<String, Integer> m_enumFlagValues;

    private FlagsConverter(Map<String, Integer> enumFlagValues) {
      m_enumFlagValues = enumFlagValues;
    }

    @Override
    public Object toObject(String value) {
      String[] parts = StringUtils.split(value, "|");
      int flagsValue = 0;
      if (parts != null) {
        for (String flagName : parts) {
          flagsValue |= m_enumFlagValues.get(flagName);
        }
      }
      return flagsValue;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // EnumConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class EnumConverter extends AndroidObjectExpressionConverter {
    private final Map<String, Integer> m_enumFlagValues;

    private EnumConverter(Map<String, Integer> enumFlagValues) {
      m_enumFlagValues = enumFlagValues;
    }

    @Override
    public Object toObject(String value) {
      return m_enumFlagValues.get(value);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // BooleanConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class BooleanConverter extends AndroidObjectExpressionConverter {
    private static final AndroidObjectExpressionConverter INSTANCE = new BooleanConverter();

    @Override
    public Object toObject(String value) {
      return Boolean.parseBoolean(value);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // IntegerConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class IntegerConverter extends AndroidObjectExpressionConverter {
    private static final AndroidObjectExpressionConverter INSTANCE = new IntegerConverter();

    @Override
    public Object toObject(String value) {
      return Integer.parseInt(value);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // DoubleConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class FloatConverter extends AndroidObjectExpressionConverter {
    private static final AndroidObjectExpressionConverter INSTANCE = new FloatConverter();

    @Override
    public Object toObject(String value) {
      return Float.parseFloat(value);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // StringConverter (default)
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class StringConverter extends AndroidObjectExpressionConverter {
    private static final AndroidObjectExpressionConverter INSTANCE = new StringConverter();

    @Override
    public Object toObject(String value) {
      return value;
    }
  }
}
