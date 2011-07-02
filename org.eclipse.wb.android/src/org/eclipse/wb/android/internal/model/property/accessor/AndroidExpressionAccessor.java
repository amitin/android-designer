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
package org.eclipse.wb.android.internal.model.property.accessor;

import org.eclipse.wb.android.internal.model.property.converter.AndroidObjectExpressionConverter;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.utils.NamespacesHelper;


import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * The accessor for Android xml attributes.
 * 
 * @author mitin_aa
 * @coverage android.model.property
 */
public final class AndroidExpressionAccessor extends ExpressionAccessor {
  private final String m_namespaceUrl;
  private final IJavaValueAccessor m_getter;
  private AndroidObjectExpressionConverter m_converter;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AndroidExpressionAccessor(String localName,
      String namespaceUrl,
      final String javaDoc,
      IJavaValueAccessor getter,
      AndroidObjectExpressionConverter converter) {
    super(localName);
    m_namespaceUrl = namespaceUrl;
    m_getter = getter;
    m_converter = converter;
    m_tooltipProvider = new PropertyTooltipTextProvider() {
      @Override
      protected String getText(Property property) throws Exception {
        return javaDoc;
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getDefaultValue(XmlObjectInfo object) throws Exception {
    Map<Object, Object> arbitraries = object.getArbitraries();
    if (arbitraries.containsKey(this)) {
      return object.getArbitraryValue(this);
    }
    return Property.UNKNOWN_VALUE;
  }

  @Override
  public Object getValue(XmlObjectInfo object) throws Exception {
    String expression = getExpression(object);
    if (expression == null) {
      return Property.UNKNOWN_VALUE;
    }
    if (m_converter != null) {
      return m_converter.toObject(expression);
    }
    return Property.UNKNOWN_VALUE;
  }

  @Override
  public String getExpression(XmlObjectInfo object) {
    DocumentElement element = getElement(object);
    return element.getAttribute(getResolvedAttribute(element, m_attribute));
  }

  @Override
  public void setExpression(XmlObjectInfo object, String expression) throws Exception {
    DocumentElement element = getElement(object);
    element.setAttribute(getResolvedAttribute(element, m_attribute), expression);
    ExecutionUtils.refresh(object);
  }

  private String getResolvedAttribute(DocumentElement element, String attribute) {
    if (StringUtils.isEmpty(m_namespaceUrl)) {
      return attribute;
    }
    NamespacesHelper helper = new NamespacesHelper(element.getRoot());
    String name = helper.ensureName(m_namespaceUrl, "ns");
    return name + ":" + attribute;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void visit(XmlObjectInfo object, int state) throws Exception {
    super.visit(object, state);
    if (state == STATE_OBJECT_READY) {
      object.putArbitraryValue(this, fetchObjectValue(object));
    }
  }

  /**
   * @return the default value to get stored, could be {@link Property#UNKNOWN_VALUE}.
   */
  private Object fetchObjectValue(final XmlObjectInfo object) {
    if (m_getter != null) {
      return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Object>() {
        public Object runObject() throws Exception {
          Object toolkitObject = object.getObject();
          return m_getter.getValue(toolkitObject);
        }
      }, Property.UNKNOWN_VALUE);
    } else {
      return Property.UNKNOWN_VALUE;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  private final PropertyTooltipProvider m_tooltipProvider;

  @Override
  public <T> T getAdapter(Class<T> adapter) {
    if (adapter == PropertyTooltipProvider.class) {
      return adapter.cast(m_tooltipProvider);
    }
    // other
    return super.getAdapter(adapter);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean hasGetter() {
    return m_getter != null;
  }
}
