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
package org.eclipse.wb.android.internal.gef.figure;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;

import org.eclipse.swt.SWT;

/**
 * Draws a rectangle which size is determined by the bounds set to it.
 * 
 * @author mitin_aa
 * @coverage android.gef.figure
 */
public final class StyledRectangleFigure extends Figure {
  private final int lineStyle;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public StyledRectangleFigure() {
    this(SWT.LINE_SOLID);
  }

  /**
   * Creates the figure with specified line style. Possible values are SWT.LINE_*.
   */
  public StyledRectangleFigure(int lineStyle) {
    super();
    this.lineStyle = lineStyle;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Paint
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void paintClientArea(Graphics graphics) {
    graphics.setLineStyle(lineStyle);
    graphics.drawRectangle(getClientArea().getResized(-1, -1));
  }
}
