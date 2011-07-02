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

import org.eclipse.wb.android.internal.Activator;
import org.eclipse.wb.android.internal.model.property.IPropertiesConstants;
import org.eclipse.wb.android.internal.model.property.editor.delegating.IPropertyEditorDelegatorAcceptor;
import org.eclipse.wb.android.internal.parser.AndroidEditorContext;
import org.eclipse.wb.android.internal.support.resources.ui.ResourceDialog;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractTextPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;

import com.android.ide.common.api.IAttributeInfo;
import com.android.ide.common.api.IAttributeInfo.Format;
import com.android.resources.ResourceType;

/**
 * @author mitin_aa
 */
@SuppressWarnings("restriction")
public class ReferencePropertyEditor extends AbstractTextPropertyEditor
    implements
      IPropertyEditorDelegatorAcceptor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final PropertyEditorPresentation m_presentation = new ButtonPropertyEditorPresentation() {
    @Override
    protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
      GenericPropertyImpl prop = (GenericPropertyImpl) property;
      XmlObjectInfo xmlObjectInfo = prop.getObject();
      AndroidEditorContext context = (AndroidEditorContext) xmlObjectInfo.getContext();
      ResourceDialog dialog =
          new ResourceDialog(DesignerPlugin.getShell(), Activator.getDefault(), context);
      Object value = prop.getValue();
      if (value instanceof String && ((String) value).startsWith("@")) {
        dialog.setReference((String) value);
      } else {
        // try to guess primary type
        // FIXME: revisit
        IAttributeInfo attribute =
            (IAttributeInfo) prop.getDescription().getArbitraryValue(
                IPropertiesConstants.KEY_ATTRIBUTE);
        if (attribute != null) {
          Format[] formats = attribute.getFormats();
          for (Format format : formats) {
            if (!Format.REFERENCE.equals(format)) {
              dialog.setPrimaryResourceType(convertFormatToType(format));
              break;
            }
          }
        }
      }
      if (dialog.open() == Window.OK) {
        prop.setExpression(dialog.getReferenceValue(), Property.UNKNOWN_VALUE);
      }
    }

    @Override
    protected Image getImage() {
      return Activator.getImage("properties/r.png");
    }
  };

  @Override
  public PropertyEditorPresentation getPresentation() {
    return m_presentation;
  }

  /**
   * Convert attribute format into resource type
   */
  protected ResourceType convertFormatToType(Format format) {
    switch (format) {
      case COLOR :
        return ResourceType.COLOR;
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractTextPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getEditorText(Property property) throws Exception {
    return getText(property);
  }

  @Override
  protected boolean setEditorText(Property property, String text) throws Exception {
    return false;
  }

  @Override
  protected String getText(Property property) throws Exception {
    return (String) property.getValue();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPropertyEditorDelegatorAcceptor
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean accepts(PropertyEditor editor, Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof String) {
      String stringValue = (String) value;
      return stringValue.startsWith("@");
    }
    return false;
  }
}
