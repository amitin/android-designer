package org.eclipse.wb.android.internal.gef.policy.layouts.table.header.selection;

import com.google.common.collect.Lists;

import org.eclipse.wb.android.internal.gef.policy.layouts.table.header.part.DimensionHeaderEditPart;
import org.eclipse.wb.core.gef.header.AbstractHeaderSelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.ILocator;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

import java.util.List;

/**
 * Abstract {@link SelectionEditPolicy} for {@link DimensionHeaderEditPart}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
abstract class DimensionSelectionEditPolicy extends AbstractHeaderSelectionEditPolicy {
  protected static final String REQ_RESIZE = "resize";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
    super(mainPolicy);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    // move handle
    {
      MoveHandle moveHandle = new MoveHandle(getHost(), new HeaderMoveHandleLocator());
      moveHandle.setForeground(IColorConstants.red);
      handles.add(moveHandle);
    }
    //
    return handles;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move location
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link ILocator} to place handle directly on header.
   */
  private class HeaderMoveHandleLocator implements ILocator {
    public void relocate(Figure target) {
      Figure reference = getHostFigure();
      Rectangle bounds = reference.getBounds().getCopy();
      FigureUtils.translateFigureToFigure(reference, target, bounds);
      target.setBounds(bounds);
    }
  }
}
