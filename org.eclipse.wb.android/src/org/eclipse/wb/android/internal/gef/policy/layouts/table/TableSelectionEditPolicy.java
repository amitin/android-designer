package org.eclipse.wb.android.internal.gef.policy.layouts.table;

import com.google.common.collect.Lists;

import org.eclipse.wb.android.internal.model.layouts.table.TableLayoutInfo;
import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridSelectionEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

import java.util.List;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link TableLayoutInfo}.
 * 
 * @author mitin_aa
 * @coverage android.gef.policy
 */
public final class TableSelectionEditPolicy extends AbstractGridSelectionEditPolicy {
  private final TableLayoutInfo m_layout;
  private final ViewInfo m_component;
  private final TableGridHelper m_gridHelper = new TableGridHelper(this, false);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableSelectionEditPolicy(TableLayoutInfo panel, ViewInfo component) {
    super(component);
    m_layout = panel;
    m_component = component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isActiveLayout() {
    return true;
  }

  @Override
  protected IGridInfo getGridInfo() {
    return m_layout.getGridInfo();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handlesList = Lists.newArrayList();
    handlesList.add(createMoveHandle());
    // add span handles
    {
      handlesList.add(createSpanHandle(IPositionConstants.WEST, 0.25));
      handlesList.add(createSpanHandle(IPositionConstants.EAST, 0.75));
    }
    return handlesList;
  }

  @Override
  protected void showPrimarySelection() {
    super.showPrimarySelection();
    m_gridHelper.showGridFeedback();
  }

  @Override
  protected void hideSelection() {
    m_gridHelper.eraseGridFeedback();
    super.hideSelection();
  }

  @Override
  protected Figure createAlignmentFigure(IAbstractComponentInfo component, boolean horizontal) {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void translateModelToFeedback(Rectangle bounds) {
    TableGridHelper.translateModelToFeedbackSelection(this, bounds);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Span
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command createSpanCommand(final boolean horizontal, final Rectangle cells) {
    if (!horizontal) {
      return null;
    }
    return new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        m_layout.command_SPAN(m_component, cells);
      }
    };
  }
}
