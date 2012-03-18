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

import org.eclipse.wb.android.internal.model.layouts.LinearLayoutInfo;
import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.draw2d.geometry.Rectangle;
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
  private TableLayoutSupport m_layoutSupport;
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
          if (parentRow.getParent() == TableLayoutInfo.this && !parentRow.isDeleting()) {
            deleteView((ViewInfo) child);
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
   * @return the {@link IGridInfo} instance for policy.
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
  private TableRowInfo getRowObjectInfo(int row) {
    List<TableRowInfo> rows = getChildrenRows();
    // the row object should exist at this moment
    return rows.get(row);
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
    while (m_layoutSupport.getRowCount() - 1 < row) {
      insertRow(m_layoutSupport.getRowCount());
    }
    // append columns
    while (m_layoutSupport.getColumnCount() - 1 < column) {
      insertColumn(m_layoutSupport.getColumnCount());
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
   * Moves a row from sourceIndex to targetIndex. Invoked by moving a header.<br>
   * TODO: probably do move using model, but it's fine to move a row this way, because the model
   * will be re-created after the command is executed.
   * 
   * @param sourceIndex
   * @param targetIndex
   */
  public void moveRow(int sourceIndex, int targetIndex) throws Exception {
    // reference row
    List<TableRowInfo> childrenRows = getChildrenRows();
    TableRowInfo thisRow = childrenRows.get(sourceIndex);
    TableRowInfo nextRow = null;
    if (targetIndex < childrenRows.size()) {
      nextRow = childrenRows.get(targetIndex);
    }
    XmlObjectUtils.move(thisRow, Associations.direct(), this, nextRow);
  }

  /**
   * Moves a column from sourceIndex to targetIndex.
   * 
   * @param sourceIndex
   * @param targetIndex
   */
  public void moveColumn(int sourceIndex, int targetIndex) throws Exception {
    m_layoutSupport.moveColumn(sourceIndex, targetIndex);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Deletes a view from layout.
   */
  private void deleteView(ViewInfo child) throws Exception {
    m_layoutSupport.deleteView(child);
  }

  /**
   * Deletes a ViewInfo from hierarchy, doesn't update layout model. Note: invoked from layout model
   * itself.
   */
  void deleteView0(ViewInfo view) throws Exception {
    view.delete();
  }

  /**
   * Deletes a row from xml, doesn't update layout model. Note: invoked from layout model itself.
   */
  void deleteRow0(int row) throws Exception {
    List<TableRowInfo> rows = getChildrenRows();
    TableRowInfo rowInfo = rows.get(row);
    rowInfo.delete();
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
    TableRowInfo referenceRow = getRowObjectInfo(row);
    ViewInfo referenceView = m_layoutSupport.getNextViewAt(row, column);
    // do add
    XmlObjectUtils.add(newView, Associations.direct(), referenceRow, referenceView);
    m_layoutSupport.addView(newView, row, column);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Span command
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets span length for a view. Note: TableLayout supports horizontal spanning only.
   */
  public void command_SPAN(ViewInfo view, Rectangle cells) throws Exception {
    // set span
    m_layoutSupport.setSpan(view, cells.x, cells.width);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Performs views moving.
   */
  public void command_MOVE(ViewInfo view,
      int column,
      boolean columnInsert,
      int row,
      boolean rowInsert) throws Exception {
    prepareCell(column, columnInsert, row, rowInsert);
    // do move
    TableRowInfo referenceRow = getRowObjectInfo(row);
    ViewInfo referenceView = m_layoutSupport.getNextViewAt(row, column);
    XmlObjectUtils.move(view, Associations.direct(), referenceRow, referenceView);
    m_layoutSupport.move(view, row, column);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link TableLayoutSupport} instance.
   */
  public TableLayoutSupport getLayoutSupport() {
    return m_layoutSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Inner classes
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * A model for headers.
   */
  public static final class HeaderInfo {
    private final int m_index;

    public HeaderInfo(int index) {
      m_index = index;
    }

    public int getIndex() {
      return m_index;
    }
  }
}
