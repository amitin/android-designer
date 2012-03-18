package org.eclipse.wb.android.internal.gef.policy.layouts.table.header.part;

import org.eclipse.wb.android.internal.model.layouts.table.TableLayoutInfo;
import org.eclipse.wb.android.internal.model.layouts.table.TableLayoutInfo.HeaderInfo;
import org.eclipse.wb.core.gef.header.Headers;
import org.eclipse.wb.core.gef.header.IHeaderMenuProvider;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.tools.ParentTargetDragEditPartTracker;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * {@link EditPart} for column/row header of TableLayout.
 * 
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage android.gef.policy
 */
public abstract class DimensionHeaderEditPart extends GraphicalEditPart
    implements
      IHeaderMenuProvider {
  protected static final Color COLOR_NORMAL = Headers.COLOR_HEADER;
  protected static final Color COLOR_EMPTY = new Color(null, 255, 235, 235);
  protected static final Font DEFAULT_FONT = new Font(null, "Arial", 7, SWT.NONE);
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Figure m_containerFigure;
  protected final TableLayoutInfo m_tableLayout;
  protected final HeaderInfo m_header;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionHeaderEditPart(TableLayoutInfo tableLayout,
      HeaderInfo header,
      Figure containerFigure) {
    m_tableLayout = tableLayout;
    m_header = header;
    m_containerFigure = containerFigure;
    setModel(header);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the index of this {@link HeaderInfo}.
   */
  public final int getIndex() {
    return m_header.getIndex();
  }

  /**
   * @return the model.
   */
  public final HeaderInfo getHeader() {
    return m_header;
  }

  public TableLayoutInfo getTableLayout() {
    return m_tableLayout;
  }

  /**
   * @return the offset of {@link Figure} with headers relative to the absolute layer.
   */
  public final Point getOffset() {
    Point offset = new Point(0, 0);
    FigureUtils.translateFigureToAbsolute2(m_containerFigure, offset);
    return offset;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dragging
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final Tool getDragTrackerTool(Request request) {
    return new ParentTargetDragEditPartTracker(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refreshVisuals() {
    // update background
    {
      getFigure().setBackground(COLOR_NORMAL);
    }
  }
}
