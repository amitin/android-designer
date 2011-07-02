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
package org.eclipse.wb.android.internal.model.property.editor.delegating;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

/**
 * 
 * @author mitin_aa
 */
public interface IPropertyEditorDelegatorAcceptor {
  IPropertyEditorDelegatorAcceptor TRUE = new IPropertyEditorDelegatorAcceptor() {
    public boolean accepts(PropertyEditor editor, Property property) {
      return true;
    }
  };

  boolean accepts(PropertyEditor editor, Property property) throws Exception;
}