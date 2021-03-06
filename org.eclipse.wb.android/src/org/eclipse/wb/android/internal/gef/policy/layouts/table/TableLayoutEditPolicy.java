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
package org.eclipse.wb.android.internal.gef.policy.layouts.table;

import com.google.common.collect.Lists;

import org.eclipse.wb.android.internal.gef.policy.layouts.table.header.layout.ColumnsLayoutEditPolicy;
import org.eclipse.wb.android.internal.gef.policy.layouts.table.header.layout.RowsLayoutEditPolicy;
import org.eclipse.wb.android.internal.gef.policy.layouts.table.header.part.ColumnHeaderEditPart;
import org.eclipse.wb.android.internal.gef.policy.layouts.table.header.part.RowHeaderEditPart;
import org.eclipse.wb.android.internal.model.layouts.table.TableLayoutInfo;
import org.eclipse.wb.android.internal.model.layouts.table.TableLayoutInfo.HeaderInfo;
import org.eclipse.wb.android.internal.model.layouts.table.TableLayoutSupport;
import org.eclipse.wb.android.internal.model.layouts.table.TableRowInfo;
import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.events.IEditPartListener;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

import org.eclipse.jface.action.IMenuManager;

import java.util.List;

/**
 * An edit policy for TableLayout.
 * 
 * @author mitin_aa
 * @coverage android.gef.policy
 */
public final class TableLayoutEditPolicy extends AbstractGridLayoutEditPolicy {
  private final TableLayoutInfo m_layout;
  private final IEditPartListener m_listener = new IEditPartListener() {
    public void childAdded(EditPart child, int index) {
      if (child.getModel() instanceof TableRowInfo) {
        child.addEditPartListener(m_listener);
      } else {
        installChildPolicy(child);
      }
    }

    public void removingChild(EditPart child, int index) {
      child.removeEditPartListener(m_listener);
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableLayoutEditPolicy(TableLayoutInfo layout) {
    super(layout);
    m_layout = layout;
    m_gridTargetHelper = new TableGridHelper(this, true);
    m_gridSelectionHelper = new TableGridHelper(this, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children edit parts
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void decorateChild(EditPart child) {
    if (child.getModel() instanceof TableRowInfo) {
      for (EditPart child2 : child.getChildren()) {
        installChildPolicy(child2);
      }
    }
  }

  private void installChildPolicy(EditPart child) {
    ViewInfo widget = (ViewInfo) child.getModel();
    EditPolicy selectionPolicy = new TableSelectionEditPolicy(m_layout, widget);
    child.installEditPolicy(EditPolicy.SELECTION_ROLE, selectionPolicy);
  }

  @Override
  public void activate() {
    GraphicalEditPart host = getHost();
    host.addEditPartListener(m_listener);
    List<EditPart> hostChildren = host.getChildren();
    for (EditPart hostChild : hostChildren) {
      if (hostChild.getModel() instanceof TableRowInfo) {
        hostChild.addEditPartListener(m_listener);
      }
    }
    super.activate();
  }

  @Override
  public void deactivate() {
    super.deactivate();
    getHost().removeEditPartListener(m_listener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractGridLayoutEditPolicy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected IGridInfo getGridInfo() {
    return m_layout.getGridInfo();
  }

  @Override
  protected void updateGridTarget(Point mouseLocation) throws Exception {
    {
      m_target = new GridTarget();
      // prepare location in model
      Point location = mouseLocation.getCopy();
      PolicyUtils.translateAbsoluteToModel(this, location);
      // prepare grid information
      IGridInfo gridInfo = m_layout.getGridInfo();
      Interval[] columnIntervals = gridInfo.getColumnIntervals();
      Interval[] rowIntervals = gridInfo.getRowIntervals();
      int lastX =
          columnIntervals.length != 0
              ? columnIntervals[columnIntervals.length - 1].end()
              : gridInfo.getInsets().left;
      int lastY =
          rowIntervals.length != 0
              ? rowIntervals[rowIntervals.length - 1].end()
              : gridInfo.getInsets().top;
      // prepare insert bounds
      {
        if (columnIntervals.length != 0) {
          m_target.m_rowInsertBounds.x = columnIntervals[0].begin - INSERT_MARGINS;
          m_target.m_rowInsertBounds.setRight(lastX + INSERT_MARGINS);
        } else {
          m_target.m_rowInsertBounds.x = 0;
          m_target.m_rowInsertBounds.setRight(getHostFigure().getSize().width);
        }
        if (rowIntervals.length != 0) {
          m_target.m_columnInsertBounds.y = rowIntervals[0].begin - INSERT_MARGINS;
          m_target.m_columnInsertBounds.setBottom(lastY + INSERT_MARGINS);
        } else {
          m_target.m_columnInsertBounds.y = 0;
          m_target.m_columnInsertBounds.setBottom(getHostFigure().getSize().height);
        }
      }
      // find existing column
      for (int columnIndex = 0; columnIndex < columnIntervals.length; columnIndex++) {
        boolean isLast = columnIndex == columnIntervals.length - 1;
        Interval interval = columnIntervals[columnIndex];
        Interval nextInterval = !isLast ? columnIntervals[columnIndex + 1] : null;
        // before first
        if (location.x < columnIntervals[0].begin) {
          m_target.m_column = 0;
          m_target.m_columnInsert = true;
          // prepare parameters
          int[] parameters =
              getInsertFeedbackParameters(new Interval(0, 0), interval, INSERT_COLUMN_SIZE);
          // feedback
          m_target.m_feedbackBounds.x = parameters[3];
          m_target.m_feedbackBounds.width = parameters[4] - parameters[3];
          // insert
          m_target.m_columnInsertBounds.x = parameters[1];
          m_target.m_columnInsertBounds.width = parameters[2] - parameters[1];
          // stop
          break;
        }
        // gap or near to end of interval
        if (!isLast) {
          int gap = nextInterval.begin - interval.end();
          boolean directGap = interval.end() <= location.x && location.x < nextInterval.begin;
          boolean narrowGap = gap < 2 * INSERT_COLUMN_SIZE;
          boolean nearEnd = Math.abs(location.x - interval.end()) < INSERT_COLUMN_SIZE;
          boolean nearBegin = Math.abs(location.x - nextInterval.begin) < INSERT_COLUMN_SIZE;
          if (directGap || narrowGap && (nearEnd || nearBegin)) {
            m_target.m_column = columnIndex + 1;
            m_target.m_columnInsert = true;
            // prepare parameters
            int[] parameters =
                getInsertFeedbackParameters(interval, nextInterval, INSERT_COLUMN_SIZE);
            // feedback
            m_target.m_feedbackBounds.x = parameters[3];
            m_target.m_feedbackBounds.width = parameters[4] - parameters[3];
            // insert
            m_target.m_columnInsertBounds.x = parameters[1];
            m_target.m_columnInsertBounds.width = parameters[2] - parameters[1];
            // stop
            break;
          }
        }
        // column
        if (interval.contains(location.x)) {
          m_target.m_column = columnIndex;
          // feedback
          m_target.m_feedbackBounds.x = interval.begin;
          m_target.m_feedbackBounds.width = interval.length + 1;
          // stop
          break;
        }
      }
      // find virtual column
      if (m_target.m_column == -1) {
        int columnGap = gridInfo.getVirtualColumnGap();
        int columnSize = gridInfo.getVirtualColumnSize();
        //
        int newWidth = columnSize + columnGap;
        int newDelta = (location.x - lastX - columnGap / 2) / newWidth;
        //
        m_target.m_column = columnIntervals.length + newDelta;
        m_target.m_feedbackBounds.x = lastX + columnGap + newWidth * newDelta;
        m_target.m_feedbackBounds.width = columnSize + 1;
      }
      // find existing row
      for (int rowIndex = 0; rowIndex < rowIntervals.length; rowIndex++) {
        boolean isLast = rowIndex == rowIntervals.length - 1;
        Interval interval = rowIntervals[rowIndex];
        Interval nextInterval = !isLast ? rowIntervals[rowIndex + 1] : null;
        // before first
        if (location.y < rowIntervals[0].begin) {
          m_target.m_row = 0;
          m_target.m_rowInsert = true;
          // prepare parameters
          int[] parameters =
              getInsertFeedbackParameters(new Interval(0, 0), interval, INSERT_ROW_SIZE);
          // feedback
          m_target.m_feedbackBounds.y = parameters[3];
          m_target.m_feedbackBounds.height = parameters[4] - parameters[3];
          // insert
          m_target.m_rowInsertBounds.y = parameters[1];
          m_target.m_rowInsertBounds.height = parameters[2] - parameters[1];
          // stop
          break;
        }
        // gap or near to end of interval
        if (!isLast) {
          int gap = nextInterval.begin - interval.end();
          boolean directGap = interval.end() <= location.y && location.y < nextInterval.begin;
          boolean narrowGap = gap < 2 * INSERT_ROW_SIZE;
          boolean nearEnd = Math.abs(location.y - interval.end()) < INSERT_ROW_SIZE;
          boolean nearBegin = Math.abs(location.y - nextInterval.begin) < INSERT_ROW_SIZE;
          if (directGap || narrowGap && (nearEnd || nearBegin)) {
            m_target.m_row = rowIndex + 1;
            m_target.m_rowInsert = true;
            // prepare parameters
            int[] parameters = getInsertFeedbackParameters(interval, nextInterval, INSERT_ROW_SIZE);
            // feedback
            m_target.m_feedbackBounds.y = parameters[3];
            m_target.m_feedbackBounds.height = parameters[4] - parameters[3];
            // insert
            m_target.m_rowInsertBounds.y = parameters[1];
            m_target.m_rowInsertBounds.height = parameters[2] - parameters[1];
            // stop
            break;
          }
        }
        // row
        if (interval.contains(location.y)) {
          m_target.m_row = rowIndex;
          // feedback
          m_target.m_feedbackBounds.y = interval.begin;
          m_target.m_feedbackBounds.height = interval.length + 1;
          // stop
          break;
        }
      }
      // find virtual row
      if (m_target.m_row == -1) {
        int rowGap = gridInfo.getVirtualRowGap();
        int rowSize = gridInfo.getVirtualRowSize();
        //
        int newHeight = rowSize + rowGap;
        int newDelta = (location.y - lastY - rowGap / 2) / newHeight;
        //
        m_target.m_row = rowIntervals.length + newDelta;
        m_target.m_feedbackBounds.y = lastY + rowGap + newHeight * newDelta;
        m_target.m_feedbackBounds.height = rowSize + 1;
      }
    }
  }

  /**
   * Determines parameters of insert feedback.
   * 
   * @return the array of: visual gap, begin/end of insert feedback, begin/end of target feedback.
   */
  public static int[] getInsertFeedbackParameters(Interval interval,
      Interval nextInterval,
      int minGap) {
    int gap = nextInterval.begin - interval.end();
    int visualGap = Math.max(gap, minGap);
    // determine x1/x2
    int x1, x2;
    {
      int a = interval.end();
      int b = nextInterval.begin;
      int x1_2 = a + b - visualGap;
      x1 = x1_2 % 2 == 0 ? x1_2 / 2 : x1_2 / 2 - 1;
      x2 = a + b - x1;
      // we don't want to have insert feedback be same as intervals
      if (x1 == a - 1) {
        x1--;
        x2++;
      }
    }
    //
    return new int[]{visualGap, x1, x2, x1 - minGap, x2 + minGap};
  }

  public void buildContextMenu(IMenuManager manager, boolean horizontal) {
  }

  public void handleDoubleClick(boolean horizontal) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(CreateRequest request) {
    if (m_target.m_valid) {
      final ViewInfo newView = (ViewInfo) request.getNewObject();
      return new EditCommand(m_layout) {
        @Override
        protected void executeEdit() throws Exception {
          m_layout.command_CREATE(
              newView,
              m_target.m_column,
              m_target.m_columnInsert,
              m_target.m_row,
              m_target.m_rowInsert);
        }
      };
    }
    return null;
  }

  @Override
  protected Command getMoveCommand(ChangeBoundsRequest request) {
    if (m_target.m_valid && request.getEditParts().size() == 1) {
      EditPart moveEditPart = request.getEditParts().get(0);
      if (moveEditPart.getModel() instanceof ViewInfo) {
        final ViewInfo component = (ViewInfo) moveEditPart.getModel();
        return new EditCommand(m_layout) {
          @Override
          protected void executeEdit() throws Exception {
            m_layout.command_MOVE(
                component,
                m_target.m_column,
                m_target.m_columnInsert,
                m_target.m_row,
                m_target.m_rowInsert);
          }
        };
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IHeadersProvider 
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutEditPolicy getContainerLayoutPolicy(boolean horizontal) {
    if (horizontal) {
      return new ColumnsLayoutEditPolicy(this, m_layout);
    } else {
      return new RowsLayoutEditPolicy(this, m_layout);
    }
  }

  public List<?> getHeaders(boolean horizontal) {
    TableLayoutSupport layoutSupport = m_layout.getLayoutSupport();
    List<TableLayoutInfo.HeaderInfo> headers = Lists.newArrayList();
    int count = horizontal ? layoutSupport.getColumnCount() : layoutSupport.getRowCount();
    for (int index = 0; index < count; ++index) {
      headers.add(new TableLayoutInfo.HeaderInfo(index));
    }
    return headers;
  }

  public EditPart createHeaderEditPart(boolean horizontal, Object model) {
    if (horizontal) {
      return new ColumnHeaderEditPart(m_layout, (HeaderInfo) model, getHostFigure());
    } else {
      return new RowHeaderEditPart(m_layout, (HeaderInfo) model, getHostFigure());
    }
  }
}
