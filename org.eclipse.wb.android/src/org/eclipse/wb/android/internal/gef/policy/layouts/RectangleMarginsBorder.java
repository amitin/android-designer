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

import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.border.Border;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * A border that provides padding outlined with rectangle.
 * 
 * @author mitin_aa
 * @coverage android.gef.figure
 */
public class RectangleMarginsBorder extends Border {
  private final Color m_marginColor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Constructs a {@link RectangleMarginsBorder} with dimensions specified by <i>insets</i>.
   * 
   * @param marginColor
   */
  public RectangleMarginsBorder(Insets insets, Color marginColor) {
    super(insets);
    m_marginColor = marginColor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Border
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void paint(int ownerWidth, int ownerHeight, Graphics graphics) {
    // prepare border rectangle
    Rectangle paintBorderRectangle = new Rectangle(0, 0, ownerWidth, ownerHeight);
    paintBorderRectangle.width--;
    paintBorderRectangle.height--;
    // draw border
    graphics.pushState();
    graphics.setLineWidth(1);
    graphics.setLineStyle(SWT.LINE_DASHDOT);
    graphics.setForegroundColor(m_marginColor);
    graphics.drawRectangle(paintBorderRectangle);
    graphics.popState();
    // crop and draw inner
    paintBorderRectangle.crop(getInsets());
    graphics.drawRectangle(paintBorderRectangle);
  }
}