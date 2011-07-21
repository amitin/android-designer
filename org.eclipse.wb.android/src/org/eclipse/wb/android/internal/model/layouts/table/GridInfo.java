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

import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;

/**
 * Implementation of IGridInfo for Android TableLayout.
 * 
 * @author mitin_aa
 * @coverage android.model
 */
final class GridInfo implements IGridInfo {
  private final TableLayoutSupport m_layoutSupport;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridInfo(TableLayoutSupport layoutSupport) {
    m_layoutSupport = layoutSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dimensions
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getRowCount() {
    return m_layoutSupport.getRowCount();
  }

  public int getColumnCount() {
    return m_layoutSupport.getColumnCount();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Intervals
  //
  ////////////////////////////////////////////////////////////////////////////
  public Interval[] getRowIntervals() {
    return m_layoutSupport.getRowIntervals();
  }

  public Interval[] getColumnIntervals() {
    return m_layoutSupport.getColumnIntervals();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cells
  //
  ////////////////////////////////////////////////////////////////////////////
  public Rectangle getComponentCells(IAbstractComponentInfo component) {
    return m_layoutSupport.getViewCells((ViewInfo) component);
  }

  public Rectangle getCellsRectangle(Rectangle cells) {
    Interval[] columnIntervals = m_layoutSupport.getColumnIntervals();
    Interval[] rowIntervals = m_layoutSupport.getRowIntervals();
    int x = columnIntervals[cells.x].begin;
    int y = rowIntervals[cells.y].begin;
    int w = columnIntervals[cells.right() - 1].end() - x;
    int h = rowIntervals[cells.bottom() - 1].end() - y;
    Rectangle rectangle = new Rectangle(x, y, w, h);
    return rectangle;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isRTL() {
    return false;
  }

  public Insets getInsets() {
    return Insets.ZERO_INSETS;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Virtual columns
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean hasVirtualColumns() {
    return true;
  }

  public int getVirtualColumnSize() {
    return 25;
  }

  public int getVirtualColumnGap() {
    return 5;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Virtual rows
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean hasVirtualRows() {
    return true;
  }

  public int getVirtualRowSize() {
    return 25;
  }

  public int getVirtualRowGap() {
    return 5;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Checks
  //
  ////////////////////////////////////////////////////////////////////////////
  public IAbstractComponentInfo getOccupied(int column, int row) {
    return m_layoutSupport.getViewAt(row, column);
  }
}