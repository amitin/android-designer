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

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * @author mitin_aa
 */
public final class FieldJavaValueAccessor implements IJavaValueAccessor {
  private final Field m_field;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FieldJavaValueAccessor(Field field) {
    m_field = field;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IJavaValueAccessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getValue(Object object) throws Exception {
    return ReflectionUtils.getFieldObject(object, m_field.getName());
  }
}
