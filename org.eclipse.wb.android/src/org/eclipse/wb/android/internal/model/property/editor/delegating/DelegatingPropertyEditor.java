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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.CompoundPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import java.util.List;

/**
 * @author mitin_aa
 */
public class DelegatingPropertyEditor extends PropertyEditor {
  private final List<PropertyEditorDesc> m_editors = Lists.newArrayList();
  private PropertyEditor m_activeEditor;

  public void add(PropertyEditor editor) {
    IPropertyEditorDelegatorAcceptor acceptor;
    if (editor instanceof IPropertyEditorDelegatorAcceptor) {
      acceptor = (IPropertyEditorDelegatorAcceptor) editor;
    } else {
      acceptor = IPropertyEditorDelegatorAcceptor.TRUE;
    }
    add(editor, acceptor);
  }

  public void add(PropertyEditor editor, IPropertyEditorDelegatorAcceptor acceptor) {
    m_editors.add(new PropertyEditorDesc(editor, acceptor));
  }

  @Override
  public PropertyEditorPresentation getPresentation() {
    CompoundPropertyEditorPresentation compoundPresentation =
        new CompoundPropertyEditorPresentation();
    for (PropertyEditorDesc desc : m_editors) {
      PropertyEditorPresentation presentation = desc.editor.getPresentation();
      if (presentation != null) {
        compoundPresentation.add(presentation);
      }
    }
    return compoundPresentation;
  }

  @Override
  public void paint(Property property, GC gc, int x, int y, int width, int height) throws Exception {
    getEditorForProperty(property).paint(property, gc, x, y, width, height);
  }

  @Override
  public boolean activate(PropertyTable propertyTable, Property property, Point location)
      throws Exception {
    m_activeEditor = getEditorForProperty(property);
    return m_activeEditor.activate(propertyTable, property, location);
  }

  @Override
  public void deactivate(PropertyTable propertyTable, Property property, boolean save) {
    m_activeEditor.deactivate(propertyTable, property, save);
  }

  @Override
  public void setBounds(Rectangle bounds) {
    if (m_activeEditor != null) {
      m_activeEditor.setBounds(bounds);
    }
  }

  private PropertyEditor getEditorForProperty(final Property property) {
    PropertyEditor propertyEditor =
        ExecutionUtils.runObject(new RunnableObjectEx<PropertyEditor>() {
          public PropertyEditor runObject() throws Exception {
            for (PropertyEditorDesc desc : m_editors) {
              if (desc.acceptor.accepts(desc.editor, property)) {
                return desc.editor;
              }
            }
            return null;
          }
        });
    if (propertyEditor == null) {
      throw new IllegalArgumentException("No editor found for the property '"
          + property.getTitle()
          + "'");
    }
    return propertyEditor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Inner classes
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class PropertyEditorDesc {
    public PropertyEditor editor;
    public IPropertyEditorDelegatorAcceptor acceptor;

    public PropertyEditorDesc(PropertyEditor editor, IPropertyEditorDelegatorAcceptor acceptor) {
      this.editor = editor;
      this.acceptor = acceptor;
    }
  }
}
