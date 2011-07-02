package org.eclipse.wb.android.internal.model.layouts.table;

/**
 * Information about single column in {@link TableLayoutInfo}.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage android.model
 */
public final class ColumnInfo extends DimensionInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnInfo(TableLayoutInfo layout) {
    super(layout);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int getIndex() {
    return m_layout.getColumns().indexOf(this);
  }

  @Override
  public boolean isEmpty() {
    return m_layout.isEmptyColumn(getIndex());
  }

  @Override
  public String getTitle() {
    return "column: " + getIndex();
  }

  @Override
  public void delete() throws Exception {
    m_layout.command_deleteColumn(getIndex(), true);
  }
}
