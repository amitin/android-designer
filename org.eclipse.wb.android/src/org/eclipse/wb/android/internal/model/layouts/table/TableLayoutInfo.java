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

import org.eclipse.wb.android.internal.model.layouts.LinearLayoutInfo;
import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.Debug;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import java.util.List;

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
    m_layoutSupport = new TableLayoutSupport(this);
  }

  /**
   * @return
   */
  public IGridInfo getGridInfo() {
    if (m_gridInfo == null) {
      m_gridInfo = new GridInfo(m_layoutSupport);
    }
    return m_gridInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Helpers/Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  List<TableRowInfo> getChildrenRows() {
    return getChildren(TableRowInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LinearLayoutInfo
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
  private TableLayoutSupport m_layoutSupport;

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
    while (m_layoutSupport.getRowCount() <= row) {
      insertRow(m_layoutSupport.getRowCount());
    }
  }

  private void insertRow(int rowIndex) throws Exception {
    // create new row object
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
    m_layoutSupport.insertRow(rowIndex);
  }

  private void insertColumn(int columnIndex) throws Exception {
    m_layoutSupport.insertColumn(columnIndex);
  }

  /**
   * Removes unneeded explicitly defined column values, empty rows
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
          int explicitColumn = TableLayoutUtils.getExplicitColumn(view);
          if (explicitColumn == currentColumn) {
            TableLayoutUtils.removeExplicitColumn(parentRow, view);
          } else if (explicitColumn > currentColumn) {
            currentColumn = explicitColumn;
          }
          int span = TableLayoutUtils.getSpanValue(parentRow, view);
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
    // remove columns either
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
    m_layoutSupport.deleteView(child);
    optimize();
  }

  public void command_deleteRow(int index, boolean b) {
    Debug.println("command_deleteRow");
  }

  public void command_deleteColumn(int index, boolean b) {
    Debug.println("command_deleteColumn");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Create command
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE(ViewInfo newView,
      int column,
      boolean insertColumn,
      int row,
      boolean insertRow) throws Exception {
    prepareCell(column, insertColumn, row, insertRow);
    // find reference
    List<TableRowInfo> rows = getChildrenRows();
    // the row object should exist at this moment
    TableRowInfo referenceRow = rows.get(row);
    ViewInfo referenceView = m_layoutSupport.getNextViewAt(row, column);
    // do add
    XmlObjectUtils.add(newView, Associations.direct(), referenceRow, referenceView);
    // set the column explicitly, could be removed later
    TableLayoutUtils.setExplicitColumn(referenceRow, newView, column);
    // do some optimizing
    optimize();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Span command
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * 
   */
  public void command_SPAN(ViewInfo view, Rectangle cells) throws Exception {
    // set span
    m_layoutSupport.setSpan(view, cells.x, cells.width);
    // do optimize
    optimize();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * 
   */
  public void command_MOVE(ViewInfo view,
      int column,
      boolean columnInsert,
      int row,
      boolean rowInsert) throws Exception {
    prepareCell(column, columnInsert, row, rowInsert);
    /*RowInfo rowInfo = getRows().get(row);
    TableRowInfo parentRow = (TableRowInfo) view.getParent();
    if (rowInfo.getTableRowInfo() == parentRow) {
      // move inside row, no reparent
      ViewInfo referenceView = m_layoutSupport.getNextViewAt(row, column);
      XmlObjectUtils.move(view, Associations.direct(), parentRow, referenceView);
      TableLayoutUtils.setExplicitColumn(parentRow, view, column);
    }
    // preserve column
    preserveColumn(parentRow, view);*/
    // optimize
    optimize();
  }
}
