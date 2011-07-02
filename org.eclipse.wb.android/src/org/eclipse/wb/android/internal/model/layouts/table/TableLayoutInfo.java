/*******************************************************************************
 * Copyright (c) 2011 Alexander Mitin (Alexander.Mitin@gmail.com)
 * Copyright (c) 2011 Andrey Sablin (Sablin.Andrey@gmail.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Mitin (Alexander.Mitin@gmail.com) - initial API and implementation
 *    Andrey Sablin (Sablin.Andrey@gmail.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.android.internal.model.layouts.table;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.android.internal.model.layouts.LinearLayoutInfo;
import org.eclipse.wb.android.internal.model.widgets.ViewGroupInfo;
import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.Debug;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;


import java.util.List;
import java.util.Map;

/**
 * Model class for TableLayout.
 * 
 * @author sablin_aa
 * @author mitin_aa
 * @coverage android.model
 */
public class TableLayoutInfo extends LinearLayoutInfo {
  private IGridInfo m_gridInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableLayoutInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (parent instanceof TableRowInfo) {
          TableRowInfo parentRow = (TableRowInfo) parent;
          if (parentRow.getParent() == TableLayoutInfo.this) {
            onDeleteView((ViewInfo) child);
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    m_gridInfo = null;
    super.refresh_dispose();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    // FIXME: maybe remove m_rows/m_columns?
    m_rows.clear();
    m_columns.clear();
    // add rows
    List<TableRowInfo> childrenRows = getChildrenRows();
    for (TableRowInfo tableRowInfo : childrenRows) {
      m_rows.add(new RowInfo(this, tableRowInfo));
    }
    // fetch columns
    try {
      m_maxWidths = (int[]) ReflectionUtils.getFieldObject(getObject(), "mMaxWidths");
      if (m_maxWidths == null) {
        m_maxWidths = new int[0];
      }
    } catch (Throwable e) {
      // no such field, TODO: fallback to bounds?
      Debug.println("No 'mMaxWidth'!");
      m_maxWidths = new int[0];
    }
    // add columns
    for (int i = 0; i < m_maxWidths.length; i++) {
      m_columns.add(new ColumnInfo(this));
    }
  }

  /**
   * @return
   */
  public IGridInfo getGridInfo() {
    if (m_gridInfo == null) {
      try {
        fetchCells();
      } catch (Throwable e) {
        ReflectionUtils.propagate(e);
      }
      m_gridInfo = new IGridInfo() {
        ////////////////////////////////////////////////////////////////////////////
        //
        // Dimensions
        //
        ////////////////////////////////////////////////////////////////////////////
        public int getRowCount() {
          return m_rowIntervals.length;
        }

        public int getColumnCount() {
          return m_columnIntervals.length;
        }

        ////////////////////////////////////////////////////////////////////////////
        //
        // Intervals
        //
        ////////////////////////////////////////////////////////////////////////////
        public Interval[] getRowIntervals() {
          return m_rowIntervals;
        }

        public Interval[] getColumnIntervals() {
          return m_columnIntervals;
        }

        ////////////////////////////////////////////////////////////////////////////
        //
        // Cells
        //
        ////////////////////////////////////////////////////////////////////////////
        public Rectangle getComponentCells(IAbstractComponentInfo component) {
          return m_viewsToCells.get(component);
        }

        public Rectangle getCellsRectangle(Rectangle cells) {
          int x = m_columnIntervals[cells.x].begin;
          int y = m_rowIntervals[cells.y].begin;
          int w = m_columnIntervals[cells.right() - 1].end() - x;
          int h = m_rowIntervals[cells.bottom() - 1].end() - y;
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
          for (Map.Entry<ViewInfo, Rectangle> entry : m_viewsToCells.entrySet()) {
            if (entry.getValue().contains(column, row)) {
              return entry.getKey();
            }
          }
          return null;
        }
      };
    }
    return m_gridInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cell info
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<ViewInfo, Rectangle> m_viewsToCells = Maps.newHashMap();
  private Interval[] m_columnIntervals;
  private Interval[] m_rowIntervals;
  // low-level data from TableInfo object
  private int[] m_maxWidths = null;

  /**
   * 
   */
  private void fetchCells() throws Exception {
    m_viewsToCells.clear();
    // fetch rows
    List<TableRowInfo> childRows = getChildrenRows();
    m_rowIntervals = new Interval[childRows.size()];
    int offset = 0;
    for (int i = 0; i < m_rowIntervals.length; i++) {
      TableRowInfo rowInfo = childRows.get(i);
      Rectangle rowBounds = rowInfo.getBounds();
      m_rowIntervals[i] = new Interval(offset, rowBounds.height);
      offset += rowBounds.height;
    }
    if (m_maxWidths != null) {
      offset = 0;
      m_columnIntervals = new Interval[m_maxWidths.length];
      for (int i = 0; i < m_columnIntervals.length; i++) {
        m_columnIntervals[i] = new Interval(offset, m_maxWidths[i]);
        offset += m_maxWidths[i];
      }
    } else {
      // TODO: fallback to bounds?
      m_columnIntervals = new Interval[0];
    }
    // map child-cell
    for (int row = 0; row < childRows.size(); row++) {
      TableRowInfo rowInfo = childRows.get(row);
      List<ViewInfo> rowChildren = rowInfo.getChildrenViews();
      for (int cell = 0, column = 0; cell < rowChildren.size(); cell++) {
        ViewInfo viewInfo = rowChildren.get(cell);
        // check explicitly defined column
        int explicitColumn = getExplicitColumn(rowInfo, viewInfo);
        if (explicitColumn != -1) {
          column = explicitColumn;
        }
        int span = getSpanValue(rowInfo, viewInfo);
        // cell cannot span vertically
        Rectangle cellRect = new Rectangle(column, row, span, 1);
        column += span;
        m_viewsToCells.put(viewInfo, cellRect);
      }
    }
  }

  /**
   * @return the span value as layout param.
   */
  private int getSpanValue(ViewGroupInfo parentRow, ViewInfo viewInfo) throws Exception {
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
  private void setSpanValue(ViewGroupInfo parentRow, ViewInfo viewInfo, int span) throws Exception {
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
  private int getExplicitColumn(ViewGroupInfo parentRow, ViewInfo viewInfo) throws Exception {
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
  private void setExplicitColumn(ViewGroupInfo parentRow, ViewInfo viewInfo, int column)
      throws Exception {
    Property columnProperty = parentRow.getLayoutPropertyByTitle(viewInfo, "column");
    if (columnProperty != null) {
      columnProperty.setValue(column);
    }
  }

  /**
   * Removes 'column' layout param value.
   */
  private void removeExplicitColumn(ViewGroupInfo parentRow, ViewInfo viewInfo) throws Exception {
    Property columnProperty = parentRow.getLayoutPropertyByTitle(viewInfo, "column");
    if (columnProperty != null) {
      columnProperty.setValue(Property.UNKNOWN_VALUE);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Helpers/Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<TableRowInfo> getChildrenRows() {
    return getChildren(TableRowInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isRevolvable() {
    return false;
  }

  @Override
  public boolean isHorizontal() throws Exception {
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Columns/Rows
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<ColumnInfo> m_columns = Lists.newArrayList();
  private final List<RowInfo> m_rows = Lists.newArrayList();

  /**
   * @return the list of all {@link ColumnInfo}'s.
   */
  public final List<ColumnInfo> getColumns() {
    return m_columns;
  }

  /**
   * @return the list of all {@link RowInfo}'s.
   */
  public final List<RowInfo> getRows() {
    return m_rows;
  }

  public boolean isEmptyRow(int index) {
    return false;
  }

  public boolean isEmptyColumn(int index) {
    return false;
  }

  private void prepareCell(int column, boolean insertColumn, int row, boolean insertRow)
      throws Exception {
    // insert column/row
    if (insertColumn) {
      insertColumn(column);
    }
    if (insertRow) {
      insertRow(row);
    }
    // append rows
    while (m_rows.size() <= row) {
      insertRow(m_rows.size());
    }
  }

  private void insertRow(int rowIndex) throws Exception {
    // create new row
    XmlObjectInfo newRowInfo =
        XmlObjectUtils.createObject(
            getContext(),
            "android.widget.TableRow",
            new ElementCreationSupport());
    // reference row
    List<TableRowInfo> childrenRows = getChildrenRows();
    TableRowInfo nextRow = null;
    if (rowIndex < childrenRows.size()) {
      nextRow = childrenRows.get(rowIndex);
    }
    // add
    XmlObjectUtils.add(newRowInfo, Associations.direct(), this, nextRow);
    m_rows.add(rowIndex, new RowInfo(this, (TableRowInfo) newRowInfo));
  }

  private void insertColumn(int columnIndex) throws Exception {
    // "insert" a column by adding explicit 'column' layout param value to other rows' children
    List<TableRowInfo> childrenRows = getChildrenRows();
    for (TableRowInfo tableRowInfo : childrenRows) {
      ViewInfo view = findViewForColumnToInsert(tableRowInfo, columnIndex);
      // if spanned, extend span
      int spanValue = getSpanValue(tableRowInfo, view);
      if (spanValue > 1) {
        setSpanValue(tableRowInfo, view, spanValue + 1);
        continue;
      }
      // set explicit 'column', it may be removed later
      int explicitColumn = getExplicitColumn(tableRowInfo, view);
      if (explicitColumn != -1) {
        setExplicitColumn(tableRowInfo, view, explicitColumn + 1);
      } else {
        setExplicitColumn(tableRowInfo, view, columnIndex + 1);
      }
      // adjust rightmost neighbors' explicit 'column' values, if any.
      List<ViewInfo> views = tableRowInfo.getChildrenViews();
      int thisViewIndex = views.indexOf(view);
      if (thisViewIndex + 1 < views.size()) {
        for (int i = thisViewIndex + 1; i < views.size(); ++i) {
          view = views.get(i);
          explicitColumn = getExplicitColumn(tableRowInfo, view);
          if (explicitColumn != -1) {
            setExplicitColumn(tableRowInfo, view, explicitColumn + 1);
          }
        }
      }
    }
    m_columns.add(columnIndex, new ColumnInfo(this));
  }

  /**
   * @return view, which should be adjusted to have a new column inserted. If column is in spanned
   *         region, returns view which span intersected (left neighbor), otherwise returns a next
   *         right neighbor if any.
   */
  private ViewInfo findViewForColumnToInsert(TableRowInfo parentRow, int columnIndex)
      throws Exception {
    List<ViewInfo> views = parentRow.getChildrenViews();
    int currentColumn = 0;
    for (ViewInfo view : views) {
      int explicitColumn = getExplicitColumn(parentRow, view);
      if (explicitColumn != -1) {
        currentColumn = explicitColumn;
      }
      if (currentColumn >= columnIndex) {
        return view;
      }
      int span = getSpanValue(parentRow, view);
      if (span > 1) {
        if (currentColumn + span >= columnIndex) {
          return view;
        }
      }
      currentColumn += span;
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE(ViewInfo newView,
      int column,
      boolean insertColumn,
      int row,
      boolean insertRow) throws Exception {
    prepareCell(column, insertColumn, row, insertRow);
    // find reference
    RowInfo rowInfo = getRows().get(row);
    TableRowInfo referenceRow = rowInfo.getTableRowInfo();
    ViewInfo referenceView = findReferenceView(referenceRow, column);
    // do add
    XmlObjectUtils.add(newView, Associations.direct(), referenceRow, referenceView);
    // set the column explicitly, could be removed later
    setExplicitColumn(referenceRow, newView, column);
    // do some optimizing
    optimize();
  }

  /**
   * Removed unneeded explicitly defined column values.
   */
  private void optimize() throws Exception {
    List<TableRowInfo> emptyRows = Lists.newArrayList();
    List<TableRowInfo> parentRows = getChildrenRows();
    for (TableRowInfo parentRow : parentRows) {
      List<ViewInfo> views = parentRow.getChildrenViews();
      if (views.isEmpty()) {
        // store row to remove
        emptyRows.add(parentRow);
      } else {
        for (int index = 0, currentColumn = 0; index < views.size(); ++index) {
          ViewInfo view = views.get(index);
          int explicitColumn = getExplicitColumn(parentRow, view);
          if (explicitColumn == currentColumn) {
            removeExplicitColumn(parentRow, view);
          } else if (explicitColumn > currentColumn) {
            currentColumn = explicitColumn;
          }
          int span = getSpanValue(parentRow, view);
          currentColumn += span;
        }
      }
    }
    // remove empty rows
    for (TableRowInfo emptyRow : emptyRows) {
      emptyRow.delete();
    }
    // TODO: make columns number optimizing by counting 
    // children in other rows which are to the left of this child
  }

  /**
   * @return
   */
  private ViewInfo findReferenceView(TableRowInfo referenceRow, int column) throws Exception {
    List<ViewInfo> rowChildren = referenceRow.getChildrenViews();
    for (int i = 0; i < rowChildren.size(); i++) {
      ViewInfo viewInfo = rowChildren.get(i);
      // check explicitly defined column
      int explicitColumn = getExplicitColumn(referenceRow, viewInfo);
      if (explicitColumn >= column) {
        return viewInfo;
      }
      if (i == column) {
        return viewInfo;
      }
      int span = getSpanValue(referenceRow, viewInfo);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param child
   */
  private void onDeleteView(ViewInfo child) throws Exception {
    optimize();
  }

  public void command_deleteRow(int index, boolean b) {
    Debug.println("command_deleteRow");
  }

  public void command_deleteColumn(int index, boolean b) {
    Debug.println("command_deleteColumn");
  }
}
