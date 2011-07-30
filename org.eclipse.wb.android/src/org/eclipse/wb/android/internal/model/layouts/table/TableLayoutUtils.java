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
package org.eclipse.wb.android.internal.model.layouts.table;

import org.eclipse.wb.android.internal.model.widgets.ViewGroupInfo;
import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.check.Assert;

/**
 * Various utils for TableLayout support.
 * 
 * @author mitin_aa
 * @coverage android.model
 */
final class TableLayoutUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Private constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private TableLayoutUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the span value as layout param.
   */
  public static int getSpanValue(ViewGroupInfo parentRow, ViewInfo viewInfo) throws Exception {
    Property spanProperty = parentRow.getLayoutPropertyByTitle(viewInfo, "span");
    if (spanProperty != null) {
      Object value = spanProperty.getValue();
      if (value instanceof Integer) {
        Integer intValue = (Integer) value;
        Assert.isTrue(intValue > 0, "Invalid span value: " + intValue);
        return intValue;
      }
    }
    return 1;
  }

  /**
   * Sets 'span' layout param for view; if span == 1 or invalid, sets default 'span' property value.
   */
  public static void setSpanValue(ViewInfo viewInfo, int span) throws Exception {
    ViewGroupInfo parentRow = (ViewGroupInfo) viewInfo.getParent();
    Object spanValue;
    if (span > 1) {
      spanValue = span;
    } else {
      spanValue = Property.UNKNOWN_VALUE;
    }
    Property spanProperty = parentRow.getLayoutPropertyByTitle(viewInfo, "span");
    if (spanProperty != null) {
      spanProperty.setValue(spanValue);
    }
  }

  /**
   * @return the layout param defined column value.
   */
  public static int getExplicitColumn(ViewInfo viewInfo) throws Exception {
    ViewGroupInfo parentRow = (ViewGroupInfo) viewInfo.getParent();
    Property columnProperty = parentRow.getLayoutPropertyByTitle(viewInfo, "column");
    if (columnProperty != null) {
      Object value = columnProperty.getValue();
      if (value instanceof Integer) {
        return (Integer) value;
      }
    }
    return -1;
  }

  /**
   * Explicitly sets a column value as layout param.
   */
  public static void setExplicitColumn(ViewInfo viewInfo, int column) throws Exception {
    ViewGroupInfo parentRow = (ViewGroupInfo) viewInfo.getParent();
    setExplicitColumn(parentRow, viewInfo, column);
  }

  /**
   * Explicitly sets a column value as layout param.
   */
  public static void setExplicitColumn(ViewGroupInfo parentRow, ViewInfo viewInfo, int column)
      throws Exception {
    Property columnProperty = parentRow.getLayoutPropertyByTitle(viewInfo, "column");
    if (columnProperty != null) {
      columnProperty.setValue(column);
    }
  }

  /**
   * Removes 'column' layout param value.
   */
  public static void removeExplicitColumn(ViewInfo viewInfo) throws Exception {
    ViewGroupInfo parentRow = (ViewGroupInfo) viewInfo.getParent();
    Property columnProperty = parentRow.getLayoutPropertyByTitle(viewInfo, "column");
    if (columnProperty != null) {
      columnProperty.setValue(Property.UNKNOWN_VALUE);
    }
  }
}
