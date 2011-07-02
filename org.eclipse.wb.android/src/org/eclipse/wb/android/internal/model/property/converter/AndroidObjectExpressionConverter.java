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

/**
 * Expression converter which able to convert attribute value to Object value.
 * 
 * @author mitin_aa
 * @coverage android.model.property
 */
public abstract class AndroidObjectExpressionConverter {
  /**
   * @return the Object value for given string xml attribute value.
   */
  public abstract Object toObject(String value);
}
