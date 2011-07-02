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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.android.ide.common.resources.ResourceItem;
import com.android.ide.common.resources.ResourceRepository;
import com.android.resources.ResourceType;

import org.apache.commons.lang.ArrayUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Content provider for the {@link ResourceDialog}.
 * 
 * @author mitin_aa
 * @coverage android.support.resources
 */
public class ResourceContentProvider implements ITreeContentProvider {
  private ResourceRepository m_resources;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ResourceContentProvider() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ITreeContentProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object[] getChildren(Object parentElement) {
    if (parentElement instanceof ResourceType) {
      Collection<ResourceItem> resourceCollection =
          m_resources.getResourceItemsOfType((ResourceType) parentElement);
      ResourceItem[] array =
          resourceCollection.toArray(new ResourceItem[resourceCollection.size()]);
      Arrays.sort(array);
      return array;
    }
    return null;
  }

  public boolean hasChildren(Object element) {
    if (element instanceof ResourceType) {
      return m_resources.hasResourcesOfType((ResourceType) element);
    }
    return false;
  }

  public Object[] getElements(Object inputElement) {
    if (inputElement instanceof ResourceRepository) {
      if ((ResourceRepository) inputElement == m_resources) {
        // get available resources
        List<ResourceType> types = m_resources.getAvailableResourceTypes();
        ResourceType[] typesArray = types.toArray(new ResourceType[types.size()]);
        Arrays.sort(typesArray);
        return typesArray;
      }
    }
    return ArrayUtils.EMPTY_OBJECT_ARRAY;
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    if (newInput instanceof ResourceRepository) {
      m_resources = (ResourceRepository) newInput;
    }
  }

  public void dispose() {
  }

  public Object getParent(Object element) {
    return null;
  }
}
