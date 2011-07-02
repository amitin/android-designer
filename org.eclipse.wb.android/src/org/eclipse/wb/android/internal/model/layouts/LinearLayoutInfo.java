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
package org.eclipse.wb.android.internal.model.layouts;

import org.eclipse.wb.android.internal.model.property.editor.FlagsPropertyEditor;
import org.eclipse.wb.android.internal.model.widgets.ViewGroupInfo;
import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;

import org.eclipse.swt.SWT;

import com.android.ide.common.layout.LayoutConstants;

import org.apache.commons.lang.StringUtils;

/**
 * Model class for LinearLayout.
 * 
 * @author mitin_aa
 * @coverage android.model
 */
@SuppressWarnings("restriction")
public class LinearLayoutInfo extends ViewGroupInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LinearLayoutInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    new LinearLayoutSelectionActionsSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this layout support horizontal/vertical switching.
   */
  public boolean isRevolvable() {
    return true;
  }

  /**
   * @return <code>true</code> if this is horizontal layout.
   */
  public boolean isHorizontal() throws Exception {
    Property property = getPropertyByTitle(LayoutConstants.ATTR_ORIENTATION);
    if (property == null) {
      return false;
    }
    int value = (Integer) property.getValue();
    // LinearLayout.VERTICAL
    return value != 1;
  }

  /**
   * Sets horizontal orientation.
   */
  public void setHorizontal(boolean value) throws Exception {
    GenericProperty property =
        (GenericProperty) getPropertyByTitle(LayoutConstants.ATTR_ORIENTATION);
    if (property == null) {
      return;
    }
    property.setExpression(value
        ? LayoutConstants.VALUE_HORIZONTAL
        : LayoutConstants.VALUE_VERTICAL, Property.UNKNOWN_VALUE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected DocumentElement getEmptyChildrenElement() throws Exception {
    DocumentElement element = super.getEmptyChildrenElement();
    if (element != null) {
      element.setAttribute("android:layout_width", LayoutConstants.VALUE_MATCH_PARENT);
      element.setAttribute("android:layout_height", LayoutConstants.VALUE_MATCH_PARENT);
    }
    return element;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Actions support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if widget has gravity flag equivalent to given <code>position</code>.
   */
  final boolean hasGravityValue(ViewInfo child, boolean isHorizontal, int position)
      throws Exception {
    Property property = getLayoutPropertyByTitle(child, LayoutConstants.ATTR_GRAVITY);
    if (property == null) {
      return false;
    }
    Object value = property.getValue();
    if (Property.UNKNOWN_VALUE == value) {
      return false;
    }
    int mask = isHorizontal ? 0x07 : 0x70;
    int toSearch = positionToGravity(position, isHorizontal);
    int gravity = (Integer) value & mask;
    return gravity == toSearch;
  }

  final void setGravity(ViewInfo child, boolean isHorizontal, int position) throws Exception {
    Property property = getLayoutPropertyByTitle(child, LayoutConstants.ATTR_GRAVITY);
    if (property == null) {
      return;
    }
    Object value = property.getValue();
    if (Property.UNKNOWN_VALUE == value) {
      value = 0;
    }
    int toSet = positionToGravity(position, isHorizontal);
    int mask = isHorizontal ? 0x07 : 0x70;
    int keepBits = (Integer) value & ~mask;
    GenericPropertyImpl genericProperty = (GenericPropertyImpl) property;
    // the 'gravity' property should have a special editor, use it to setup the expression.
    FlagsPropertyEditor editor = (FlagsPropertyEditor) genericProperty.getDescription().getEditor();
    String expression = editor.getValueSource(property, keepBits | toSet);
    genericProperty.setExpression(expression, Property.UNKNOWN_VALUE);
  }

  private int positionToGravity(int position, boolean isHorizontal) {
    switch (position) {
      case SWT.LEFT :
        return 0x03;
      case SWT.RIGHT :
        return 0x05;
      case SWT.TOP :
        return 0x30;
      case SWT.BOTTOM :
        return 0x50;
      case SWT.CENTER :
        return isHorizontal ? 0x01 : 0x10;
      case SWT.FILL :
        return isHorizontal ? 0x07 : 0x70;
    }
    throw new IllegalArgumentException("Invalid gravity position.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Margins operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return margins for given child as insets.
   */
  public Insets getMargins(final ViewInfo child) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Insets>() {
      public Insets runObject() throws Exception {
        int left = getMarginValue(child, IPositionConstants.WEST);
        int right = getMarginValue(child, IPositionConstants.EAST);
        int top = getMarginValue(child, IPositionConstants.NORTH);
        int bottom = getMarginValue(child, IPositionConstants.SOUTH);
        Insets insets = new Insets(top, left, bottom, right);
        if (Insets.ZERO_INSETS.equals(insets)) {
          // try 'margin'
          int margin = getMarginValue(child, IPositionConstants.NONE);
          return new Insets(margin);
        } else {
          return insets;
        }
      }
    }, Insets.ZERO_INSETS);
  }

  /**
   * Sets a margin value to given child for given side.
   */
  public void setMargin(ViewInfo child, int side, int margin) throws Exception {
    String marginTitle = getMarginPropertyName(side);
    GenericPropertyImpl marginProperty =
        (GenericPropertyImpl) getLayoutPropertyByTitle(child, marginTitle);
    if (margin == 0) {
      marginProperty.setValue(Property.UNKNOWN_VALUE);
    } else {
      marginProperty.setExpression(margin + "px", Property.UNKNOWN_VALUE);
    }
  }

  private String getMarginPropertyName(int side) {
    String marginTitle = "margin";
    if (side == IPositionConstants.WEST) {
      marginTitle += "Left";
    } else if (side == IPositionConstants.EAST) {
      marginTitle += "Right";
    } else if (side == IPositionConstants.NORTH) {
      marginTitle += "Top";
    } else if (side == IPositionConstants.SOUTH) {
      marginTitle += "Bottom";
    }
    return marginTitle;
  }

  private int getMarginValue(ViewInfo child, int side) throws Exception {
    String marginTitle = getMarginPropertyName(side);
    GenericPropertyImpl marginProperty =
        (GenericPropertyImpl) getLayoutPropertyByTitle(child, marginTitle);
    Object value = marginProperty.getValue();
    if (Property.UNKNOWN_VALUE == value) {
      return 0;
    }
    String marginValue = (String) value;
    if (!marginValue.endsWith("px")) {
      // TODO: convert to px!
      return 0;
    }
    try {
      return Integer.parseInt(StringUtils.removeEnd(marginValue, "px"));
    } catch (Throwable e) {
      return 0;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void applyChildDefaultLayoutParams(ViewInfo child) throws Exception {
    if (isHorizontal()) {
      toggleMatchParent(child, true, false);
      toggleMatchParent(child, false, true);
    } else {
      toggleMatchParent(child, true, true);
      toggleMatchParent(child, false, false);
    }
  }
}
