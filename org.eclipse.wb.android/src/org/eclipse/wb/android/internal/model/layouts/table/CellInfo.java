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
package org.eclipse.wb.android.internal.model.layouts.table;

import org.eclipse.wb.android.internal.model.widgets.ViewInfo;

/**
 * Data structure representing a single cell in grid.
 * 
 * @author mitin_aa
 * @coverage android.model
 */
final class CellInfo {
  ViewInfo view;
  int span;
  int row;
  int column;
  CellInfo spannedViewCell;

  public CellInfo(int row, int column) {
    this.row = row;
    this.column = column;
  }

  public boolean isEmpty() {
    return view == null && !isSpanSpace();
  }

  public boolean isSpanSpace() {
    return spannedViewCell != null;
  }

  @Override
  public String toString() {
    return "["
        + row
        + ", "
        + column
        + "]"
        + (isEmpty() ? " empty" : "")
        + (span > 1 ? " spanned:" + span : "");
  }
}
