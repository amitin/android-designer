package org.eclipse.wb.android.internal.model.layouts.table;


/**
 * Information about single row in {@link TableLayoutInfo}.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage android.model
 */
public final class RowInfo extends DimensionInfo {
  private final TableRowInfo m_tableRowInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowInfo(TableLayoutInfo layout, TableRowInfo tableRowInfo) {
    super(layout);
    m_tableRowInfo = tableRowInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int getIndex() {
    return m_layout.getRows().indexOf(this);
  }

  @Override
  public boolean isEmpty() {
    return m_layout.isEmptyRow(getIndex());
  }

  @Override
  public String getTitle() {
    return "row: " + getIndex();
  }

  @Override
  public void delete() throws Exception {
    m_layout.command_deleteRow(getIndex(), true);
  }

  /**
   * @return
   */
  public TableRowInfo getTableRowInfo() {
    return m_tableRowInfo;
  }
}
