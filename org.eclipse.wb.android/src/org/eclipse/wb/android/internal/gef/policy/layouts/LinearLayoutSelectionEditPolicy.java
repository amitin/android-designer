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
package org.eclipse.wb.android.internal.gef.policy.layouts;

import com.google.common.collect.Lists;

import org.eclipse.wb.android.internal.gef.figure.StyledRectangleFigure;
import org.eclipse.wb.android.internal.model.layouts.LinearLayoutInfo;
import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.helpers.BroadcastListenerHelper;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.AbstractRelativeLocator;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.ICursorConstants;
import org.eclipse.wb.draw2d.ILocator;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.border.Border;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.SquareHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;


import java.util.List;

/**
 * A selection policy for LinearLayout items.
 * 
 * @author mitin_aa
 * @coverage android.gef.policy
 */
public final class LinearLayoutSelectionEditPolicy extends SelectionEditPolicy {
  private static final Color MARGIN_COLOR = new Color(null, 255, 102, 255);
  private final LinearLayoutInfo m_layout;
  private final ViewInfo m_child;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LinearLayoutSelectionEditPolicy(LinearLayoutInfo layout, ViewInfo child) {
    m_layout = layout;
    m_child = child;
    // add listener to re-show selection on child changing
    new BroadcastListenerHelper(m_child, this, new ObjectEventListener() {
      @Override
      public void refreshed() throws Exception {
        boolean activePolicy = isActive();
        boolean isSelected = getHost().getSelected() == EditPart.SELECTED_PRIMARY;
        boolean isDeleted = m_child.isDeleted();
        if (activePolicy && isSelected && !isDeleted) {
          hideSelection();
          showSelection();
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    MoveHandle moveHandle = new MoveHandle(getHost());
    // use light-blue color
    moveHandle.setForeground(IColorConstants.lightBlue);
    Border border = new RectangleMarginsBorder(getChildMargins(m_child), MARGIN_COLOR);
    moveHandle.setBorder(border);
    handles.add(moveHandle);
    handles.add(createMarginHandle(IPositionConstants.NORTH, 0.75));
    handles.add(createMarginHandle(IPositionConstants.WEST, 0.75));
    handles.add(createMarginHandle(IPositionConstants.EAST, 0.25));
    handles.add(createMarginHandle(IPositionConstants.SOUTH, 0.25));
    return handles;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Margin handles
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String REQ_RESIZE_MARGINS = "resize_margins";

  /**
   * Create special handle for maintain margins.
   */
  private Handle createMarginHandle(int direction, double percent) {
    return new MarginHandle(direction, createComponentLocator(direction, percent));
  }

  /**
   * @return {@link ILocator} that positions handles on component side.
   */
  private ILocator createComponentLocator(int direction, double percent) {
    Figure reference = getHostFigure();
    Insets margins = getChildMargins(m_child);
    if (direction == IPositionConstants.WEST) {
      return new MarginsRelativeLocator(reference, margins, 0, percent);
    } else if (direction == IPositionConstants.EAST) {
      return new MarginsRelativeLocator(reference, margins, 1, percent);
    } else if (direction == IPositionConstants.NORTH) {
      return new MarginsRelativeLocator(reference, margins, percent, 0);
    } else if (direction == IPositionConstants.SOUTH) {
      return new MarginsRelativeLocator(reference, margins, percent, 1);
    }
    throw new IllegalArgumentException("Illegal direction: " + direction);
  }

  /**
   * Special locator to draw margin handles.
   */
  private static final class MarginsRelativeLocator extends AbstractRelativeLocator {
    private final Figure m_reference;
    private final Insets m_insets;

    public MarginsRelativeLocator(Figure reference,
        Insets insets,
        double relativeX,
        double relativeY) {
      super(relativeX, relativeY);
      m_reference = reference;
      m_insets = insets;
    }

    @Override
    protected Rectangle getReferenceRectangle() {
      Rectangle bounds = m_reference.getBounds().getCopy();
      bounds.expand(m_insets);
      FigureUtils.translateFigureToAbsolute(m_reference, bounds);
      return bounds;
    }
  }
  /**
   * Handle.
   */
  private class MarginHandle extends SquareHandle {
    public MarginHandle(int direction, ILocator locator) {
      super(getHost(), locator);
      setCursor(ICursorConstants.Directional.getCursor(direction));
      setDragTrackerTool(new ResizeTracker(direction, REQ_RESIZE_MARGINS));
    }

    @Override
    protected Color getFillColor() {
      return isPrimary() ? MARGIN_COLOR : IColorConstants.white;
    }

    @Override
    protected Color getBorderColor() {
      return IColorConstants.black;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks and Requests 
  //
  ////////////////////////////////////////////////////////////////////////////
  private Figure m_lineFeedback;
  private Command m_marginChangeCommand;
  private TextFeedback m_hintFeedback;

  @Override
  public boolean understandsRequest(Request request) {
    return super.understandsRequest(request) || request.getType() == REQ_RESIZE_MARGINS;
  }

  @Override
  public void showSourceFeedback(Request req) {
    if (REQ_RESIZE_MARGINS.equals(req.getType())) {
      ChangeBoundsRequest request = (ChangeBoundsRequest) req;
      // prepare direction
      int direction = request.getResizeDirection();
      boolean isEast = direction == IPositionConstants.EAST;
      boolean isWest = direction == IPositionConstants.WEST;
      boolean isNorth = direction == IPositionConstants.NORTH;
      boolean isSouth = direction == IPositionConstants.SOUTH;
      Rectangle bounds = PolicyUtils.getAbsoluteBounds(getHost());
      Insets margins = getChildMargins(m_child);
      Dimension sizeDelta = request.getSizeDelta();
      int margin = -1;
      if (isWest || isEast) {
        margin = sizeDelta.width + (isWest ? margins.left : margins.right);
        if (margin > 0) {
          bounds.resize(margin, 0);
          if (isWest) {
            bounds.translate(-margin, 0);
          }
        }
      } else if (isNorth || isSouth) {
        margin = sizeDelta.height + (isNorth ? margins.top : margins.bottom);
        if (margin > 0) {
          bounds.resize(0, margin);
          if (isNorth) {
            bounds.translate(0, -margin);
          }
        }
      }
      m_marginChangeCommand = new MarginChangeCommand(m_child, direction, margin);
      // show feedback
      {
        // add feedback figure
        if (m_lineFeedback == null) {
          m_lineFeedback = new StyledRectangleFigure(SWT.LINE_DASH);
          m_lineFeedback.setForeground(MARGIN_COLOR);
          addFeedback(m_lineFeedback);
        }
        // set bounds
        {
          PolicyUtils.translateAbsoluteToFeedback(this, bounds);
          m_lineFeedback.setBounds(bounds);
        }
        if (margin >= 0) {
          showMarginHint(bounds, direction, margin);
        }
      }
    }
  }

  private void showMarginHint(Rectangle bounds, int direction, int margin) {
    if (m_hintFeedback == null) {
      m_hintFeedback = createTextFeedback(true);
      m_hintFeedback.add();
    }
    m_hintFeedback.setText(margin + "px");
    Dimension textSize = m_hintFeedback.getSize();
    Rectangle textBounds = bounds.getCopy();
    int x = 0;
    int y = 0;
    switch (direction) {
      case IPositionConstants.WEST :
        x = textBounds.x - textSize.width - 2;
        y = textBounds.y + textBounds.height / 2 - textSize.height / 2;
        break;
      case IPositionConstants.EAST :
        x = textBounds.right() + 2;
        y = textBounds.y + textBounds.height / 2 - textSize.height / 2;
        break;
      case IPositionConstants.NORTH :
        x = textBounds.x + textBounds.width / 2 - textSize.width / 2;
        y = textBounds.y - textSize.height - 2;
        break;
      case IPositionConstants.SOUTH :
        x = textBounds.x + textBounds.width / 2 - textSize.width / 2;
        y = textBounds.bottom() + 2;
        break;
    }
    textBounds.x = x < 0 ? 0 : x;
    textBounds.y = y < 0 ? 0 : y;
    m_hintFeedback.setLocation(textBounds.getLocation());
  }

  private TextFeedback createTextFeedback(boolean isHorizontal) {
    TextFeedback textFeedback =
        new TextFeedback(getLayer(IEditPartViewer.FEEDBACK_LAYER_ABV_1), isHorizontal);
    return textFeedback;
  }

  @Override
  public void eraseSourceFeedback(Request request) {
    if (m_lineFeedback != null) {
      removeFeedback(m_lineFeedback);
      m_lineFeedback = null;
    }
    if (m_hintFeedback != null) {
      m_hintFeedback.remove();
      m_hintFeedback = null;
    }
    super.eraseSourceFeedback(request);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Command getCommand(Request request) {
    if (REQ_RESIZE_MARGINS.equals(request.getType())) {
      return m_marginChangeCommand;
    }
    return super.getCommand(request);
  }

  private final class MarginChangeCommand extends EditCommand {
    private final int m_direction;
    private final int m_margin;
    private final ViewInfo m_child;

    private MarginChangeCommand(ViewInfo child, int direction, int margin) {
      super(child);
      m_child = child;
      m_direction = direction;
      m_margin = margin < 0 ? 0 : margin;
    }

    @Override
    protected void executeEdit() throws Exception {
      if (m_margin >= 0) {
        m_layout.setMargin(m_child, m_direction, m_margin);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc/Helper
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return margins for given child as insets.
   */
  private Insets getChildMargins(ViewInfo child) {
    return m_layout.getMargins(child);
  }
}
