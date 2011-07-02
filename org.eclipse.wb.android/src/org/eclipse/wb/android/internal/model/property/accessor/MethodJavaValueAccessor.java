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

import java.lang.reflect.Method;

/**
 * @author mitin_aa
 */
public final class MethodJavaValueAccessor implements IJavaValueAccessor {
  private final String m_getterName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MethodJavaValueAccessor(Method getter) {
    m_getterName = getter.getName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IJavaValueAccessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getValue(Object object) throws Exception {
    return ReflectionUtils.invokeMethod2(object, m_getterName);
  }
}
