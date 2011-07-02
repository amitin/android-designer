package org.eclipse.wb.android.internal.gef.policy.layouts.table;

import org.eclipse.wb.android.internal.model.layouts.table.TableLayoutInfo;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridHelper;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.geometry.Translatable;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.GraphicalEditPolicy;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;


/**
 * Helper for displaying grid for {@link TableLayoutInfo}.
 * 
 * @author mitin_aa
 * @coverage android.gef.policy
 */
public final class TableGridHelper extends AbstractGridHelper {
  private final GraphicalEditPolicy m_policy;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableGridHelper(GraphicalEditPolicy policy, boolean forTarget) {
    super(policy, forTarget);
    m_policy = policy;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected IGridInfo getGridInfo() {
    return ((TableLayoutInfo) getAbstractLayout()).getGridInfo();
  }

  @Override
  protected GraphicalEditPart getHost() {
    if (m_policy instanceof LayoutEditPolicy) {
      return m_policy.getHost();
    } else {
      // ->TableRow->TableLayout
      return getHostForSelectionPolicy(m_policy);
    }
  }

  @Override
  protected void translateModelToFeedback(Translatable t) {
    if (m_policy instanceof LayoutEditPolicy) {
      PolicyUtils.translateModelToFeedback((LayoutEditPolicy) m_policy, t);
    } else {
      translateModelToFeedbackSelection(m_policy, t);
    }
  }

  static void translateModelToFeedbackSelection(GraphicalEditPolicy policy, Translatable t) {
    GraphicalEditPart containerEditPart = getHostForSelectionPolicy(policy);
    // translate: client area -> container figure
    {
      IAbstractComponentInfo container = (IAbstractComponentInfo) containerEditPart.getModel();
      t.translate(container.getClientAreaInsets());
      PolicyUtils.modelToFeedback_rightToLeft(t, container);
    }
    // translate to layer
    {
      Figure hostFigure = containerEditPart.getFigure();
      Figure layer = policy.getHost().getViewer().getLayer(IEditPartViewer.FEEDBACK_LAYER);
      FigureUtils.translateFigureToFigure2(hostFigure, layer, t);
    }
  }

  private static GraphicalEditPart getHostForSelectionPolicy(GraphicalEditPolicy policy) {
    return (GraphicalEditPart) policy.getHost().getParent().getParent();
  }
}
