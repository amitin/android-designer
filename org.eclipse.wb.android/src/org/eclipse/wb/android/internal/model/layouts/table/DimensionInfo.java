package org.eclipse.wb.android.internal.model.layouts.table;

/**
 * Information about single column/row in {@link TableLayoutInfo}.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage android.model
 */
public abstract class DimensionInfo {
  protected final TableLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionInfo(TableLayoutInfo layout) {
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the index of this {@link DimensionInfo} in array of all dimensions.
   */
  public abstract int getIndex();

  /**
   * @return <code>true</code> if this {@link DimensionInfo} contains no {@link ViewInfo}s.
   */
  public abstract boolean isEmpty();

  /**
   * @return the title to display to user.
   */
  public abstract String getTitle();

  /**
   * Deletes this {@link DimensionInfo}.
   */
  public abstract void delete() throws Exception;
}
