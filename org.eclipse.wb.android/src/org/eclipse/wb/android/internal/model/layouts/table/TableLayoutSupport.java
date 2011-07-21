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

import com.google.common.collect.Maps;

import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.Map;

/**
 * Android TableLayout model support.
 * 
 * @author mitin_aa
 * @coverage android.model
 */
public final class TableLayoutSupport {
  private final TableLayoutInfo m_layout;
  private CellInfo[][] m_cells;
  private final Interval[] m_rowIntervals;
  private final Interval[] m_columnIntervals;
  private int m_rows;
  private int m_columns;
  private final Map<ViewInfo, Rectangle> m_viewRectangleCells = Maps.newHashMap();
  private final Map<ViewInfo, CellInfo> m_viewCells = Maps.newHashMap();

  public TableLayoutSupport(TableLayoutInfo layout) throws Exception {
    m_layout = layout;
    // row/column count
    List<TableRowInfo> tableRows = layout.getChildrenRows();
    int[] maxWidths = (int[]) ReflectionUtils.getFieldObject(layout.getObject(), "mMaxWidths");
    if (maxWidths == null) {
      // empty
      maxWidths = new int[0];
    }
    m_rows = tableRows.size();
    m_columns = maxWidths.length;
    // intervals
    m_rowIntervals = new Interval[m_rows];
    m_columnIntervals = new Interval[m_columns];
    // prepare cells
    m_cells = new CellInfo[m_rows][m_columns];
    // setup row intervals & cell data
    int offsetHeight = 0;
    for (int row = 0; row < m_rows; ++row) {
      TableRowInfo rowInfo = tableRows.get(row);
      int rowHeight = rowInfo.getBounds().height;
      m_rowIntervals[row] = new Interval(offsetHeight, rowHeight);
      List<ViewInfo> views = rowInfo.getChildrenViews();
      if (views.isEmpty()) {
        // just fill with empty cells 
        for (int column = 0; column < m_columns; ++column) {
          m_cells[row][column] = new CellInfo(row, column);
        }
      } else {
        for (int viewIndex = 0, column = 0; column < m_columns; ++viewIndex) {
          // the rest of cells is empty, append empty cells 
          if (viewIndex >= views.size()) {
            m_cells[row][column] = new CellInfo(row, column);
            column++;
            continue;
          }
          // get view
          ViewInfo view = views.get(viewIndex);
          int explicitColumn = TableLayoutUtils.getExplicitColumn(view);
          if (explicitColumn != -1) {
            // add empty cells before (if any)
            for (int fillColumn = column; fillColumn < explicitColumn; ++fillColumn) {
              m_cells[row][fillColumn] = new CellInfo(row, fillColumn);
            }
            column = explicitColumn;
          }
          CellInfo cell = new CellInfo(row, column);
          m_cells[row][column] = cell;
          cell.view = view;
          m_viewCells.put(view, cell);
          int span = TableLayoutUtils.getSpanValue(rowInfo, view);
          cell.span = span;
          // add empty cells for spanning
          for (int spannedColumn = column + 1; spannedColumn < column + span; ++spannedColumn) {
            CellInfo spannedSpace = new CellInfo(row, spannedColumn);
            spannedSpace.spannedViewCell = cell;
            m_cells[row][spannedColumn] = spannedSpace;
          }
          // store cell occupation
          Rectangle cellRect = new Rectangle(column, row, span, 1);
          m_viewRectangleCells.put(view, cellRect);
          column += span;
        }
      }
      offsetHeight += rowHeight;
    }
    // setup column intervals 
    int offsetWidth = 0;
    for (int column = 0; column < m_columns; column++) {
      m_columnIntervals[column] = new Interval(offsetWidth, maxWidths[column]);
      offsetWidth += maxWidths[column];
    }
  }

  public ViewInfo getViewAt(int row, int column) {
    if (row < m_rows && column < m_columns) {
      return m_cells[row][column].view;
    }
    return null;
  }

  public int getViewRow(ViewInfo view) {
    return 0;
  }

  public int getViewColumn(ViewInfo view) {
    return 0;
  }

  public int getRowCount() {
    return m_rows;
  }

  public int getColumnCount() {
    return m_columns;
  }

  public ViewInfo getNextViewAt(int row, int column) {
    CellInfo[] rowCells = m_cells[row];
    for (int columnIndex = column; columnIndex < rowCells.length; columnIndex++) {
      CellInfo cell = rowCells[columnIndex];
      if (!cell.isEmpty()) {
        return cell.view;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visual Data
  //
  ////////////////////////////////////////////////////////////////////////////
  public Interval[] getRowIntervals() {
    return m_rowIntervals;
  }

  public Interval[] getColumnIntervals() {
    return m_columnIntervals;
  }

  public Rectangle getViewCells(ViewInfo view) {
    return m_viewRectangleCells.get(view);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Insert
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds information about new row. Note: it doesn't update visual information.
   */
  public void insertRow(int rowIndex) throws Exception {
    CellInfo newRowCells[] = new CellInfo[m_columns];
    for (int column = 0; column < m_columns; ++column) {
      newRowCells[column] = new CellInfo(rowIndex, column);
    }
    m_cells = (CellInfo[][]) ArrayUtils.add(m_cells, rowIndex, newRowCells);
    // update cells row
    for (int row = rowIndex + 1; row < m_rows + 1; ++row) {
      for (int column = 0; column < m_columns; ++column) {
        m_cells[row][column].row++;
      }
    }
    m_rows++;
  }

  /**
   * Adds information about new column. Note: it doesn't update visual information.
   */
  public void insertColumn(int columnIndex) throws Exception {
    for (int row = 0; row < m_rows; ++row) {
      CellInfo newCell = new CellInfo(row, columnIndex);
      CellInfo cell = m_cells[row][columnIndex];
      // insert a cell into row cells
      m_cells[row] = (CellInfo[]) ArrayUtils.add(m_cells[row], columnIndex, newCell);
      // extend span
      if (cell.isSpanSpace()) {
        cell.spannedViewCell.span++;
        newCell.spannedViewCell = cell.spannedViewCell;
        TableLayoutUtils.setSpanValue(cell.spannedViewCell.view, cell.spannedViewCell.span);
      } else {
        // find a next cell with view to right to adjust the column number
        boolean prevEmpty = true;
        for (int column = columnIndex + 1; column < m_columns + 1; ++column) {
          CellInfo rightCell = m_cells[row][column];
          if (!rightCell.isEmpty()) {
            if (prevEmpty) {
              // set explicit column
              TableLayoutUtils.setExplicitColumn(rightCell.view, column);
            }
            prevEmpty = false;
          } else {
            prevEmpty = true;
          }
        }
      }
    }
    m_columns++;
    // fix column number in cell
    for (int row = 0; row < m_rows; ++row) {
      for (int column = columnIndex + 1; column < m_columns; ++column) {
        m_cells[row][column].column++;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Remove
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Removes a view from grid.
   * 
   * @param view
   *          A view to remove.
   */
  public void deleteView(ViewInfo view) throws Exception {
    CellInfo cell = m_viewCells.get(view);
    cell.view = null;
    int row = cell.row;
    int column = cell.column + 1;
    // set explicit column for right neighbor
    preserveColumn(row, column);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Span
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Managing span for cell.
   * 
   * @param view
   *          A view to manage the span of.
   * @param startColumn
   *          A column at which the span start of.
   * @param spanLength
   *          A span length in cells.
   */
  public void setSpan(ViewInfo view, int startColumn, int spanLength) throws Exception {
    CellInfo cell = m_viewCells.get(view);
    int row = cell.row;
    // get next cell at this cell's span end to preserve column if it is not empty.
    int endColumn = cell.column + cell.span;
    preserveColumn(row, endColumn);
    // clear cells
    int currentSpan = cell.span;
    for (int column = cell.column; column < cell.column + currentSpan; ++column) {
      CellInfo toClear = m_cells[row][column];
      toClear.view = null;
      toClear.spannedViewCell = null;
      toClear.span = 1;
    }
    // set span
    cell = m_cells[row][startColumn];
    cell.view = view;
    cell.span = spanLength;
    for (int column = cell.column + 1; column < cell.column + cell.span; ++column) {
      m_cells[row][column].spannedViewCell = cell;
    }
    // explicitly set the column & span
    TableLayoutUtils.setExplicitColumn(view, startColumn);
    TableLayoutUtils.setSpanValue(view, spanLength);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the column explicitly if the cell has a view.
   */
  private void preserveColumn(int row, int column) throws Exception {
    if (column < m_columns) {
      CellInfo rCell = m_cells[row][column];
      if (rCell.view != null) {
        TableLayoutUtils.setExplicitColumn(rCell.view, column);
      }
    }
  }
}
