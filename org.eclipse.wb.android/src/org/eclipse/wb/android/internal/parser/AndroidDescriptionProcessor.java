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
package org.eclipse.wb.android.internal.parser;

import org.eclipse.wb.android.internal.ToolkitProvider;
import org.eclipse.wb.android.internal.model.property.IPropertiesConstants;
import org.eclipse.wb.android.internal.model.property.accessor.AndroidExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.description.IDescriptionProcessor;

import com.android.ide.common.api.IAttributeInfo;
import com.android.ide.common.api.IAttributeInfo.Format;
import com.android.ide.common.layout.LayoutConstants;
import com.android.sdklib.SdkConstants;

import java.util.List;

/**
 * {@link IDescriptionProcessor} for Android.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage android.parser
 */
@SuppressWarnings("restriction")
public class AndroidDescriptionProcessor implements IDescriptionProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IDescriptionProcessor INSTANCE = new AndroidDescriptionProcessor();

  private AndroidDescriptionProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDescriptionProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(EditorContext context, ComponentDescription componentDescription)
      throws Exception {
    if (isAndroid(context)) {
      // remove java-based properties
      List<GenericPropertyDescription> properties = componentDescription.getProperties();
      for (GenericPropertyDescription property : properties) {
        if (!(property.getAccessor() instanceof AndroidExpressionAccessor)) {
          // looks like this is unknown property, remove it
          // removing the editor removes the property from description
          property.setEditor(null);
        }
        IAttributeInfo attribute =
            (IAttributeInfo) property.getArbitraryValue(IPropertiesConstants.KEY_ATTRIBUTE);
        if (attribute != null) {
          // setup default value if no getter found and default value is not yet set in description
          AndroidExpressionAccessor accessor = (AndroidExpressionAccessor) property.getAccessor();
          if (!accessor.hasGetter() && Property.UNKNOWN_VALUE == property.getDefaultValue()) {
            setupDefaultValue(property, attribute.getFormats());
          }
        }
        // promote 'id' property category to system
        String ns =
            (String) property.getArbitraryValue(IPropertiesConstants.KEY_ATTRIBUTE_NAMESPACE_URI);
        String localName =
            (String) property.getArbitraryValue(IPropertiesConstants.KEY_ATTRIBUTE_LOCAL_NAME);
        if (LayoutConstants.ATTR_ID.equals(localName) && SdkConstants.NS_RESOURCES.equals(ns)) {
          property.setCategory(PropertyCategory.system(9));
        }
      }
    }
  }

  /**
   * Sets default values basing on format.
   */
  private void setupDefaultValue(GenericPropertyDescription property, Format[] formats) {
    if (Format.BOOLEAN.in(formats)) {
      property.setDefaultValue(false);
    }
    if (Format.FLAG.in(formats) || Format.ENUM.in(formats)) {
      property.setDefaultValue(0);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link XmlObjectInfo} is Android.
   */
  public static boolean isAndroid(XmlObjectInfo object) {
    EditorContext context = object.getContext();
    return isAndroid(context);
  }

  /**
   * @return <code>true</code> if given {@link EditorContext} is Android.
   */
  public static boolean isAndroid(EditorContext context) {
    return context.getToolkit().getId().equals(ToolkitProvider.DESCRIPTION.getId());
  }
}
