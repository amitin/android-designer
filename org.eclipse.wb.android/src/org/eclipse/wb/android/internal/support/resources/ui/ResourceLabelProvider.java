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
package org.eclipse.wb.android.internal.support.resources.ui;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.android.ide.common.resources.ResourceFile;
import com.android.ide.common.resources.ResourceItem;
import com.android.resources.ResourceType;

/**
 * Label provider for the ResourceDialog.
 * 
 * @author mitin_aa
 * @coverage android.support.resources
 */
public class ResourceLabelProvider implements ILabelProvider, ITableLabelProvider {
  public String getText(Object element) {
    return getColumnText(element, 0);
  }

  public String getColumnText(Object element, int columnIndex) {
    if (columnIndex == 0) {
      if (element instanceof ResourceType) {
        return ((ResourceType) element).getDisplayName();
      } else if (element instanceof ResourceItem) {
        return ((ResourceItem) element).getName();
      } else if (element instanceof ResourceFile) {
        return ((ResourceFile) element).getFolder().getConfiguration().toDisplayString();
      }
      return null;
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Not used
  //
  ////////////////////////////////////////////////////////////////////////////
  public void addListener(ILabelProviderListener listener) {
  }

  public void dispose() {
  }

  public boolean isLabelProperty(Object element, String property) {
    return false;
  }

  public void removeListener(ILabelProviderListener listener) {
  }

  public Image getColumnImage(Object element, int columnIndex) {
    return null;
  }

  public Image getImage(Object element) {
    return null;
  }
}
