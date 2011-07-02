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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.android.ide.common.resources.ResourceItem;
import com.android.ide.common.resources.ResourceRepository;
import com.android.resources.ResourceType;

import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Set;

/**
 * Class for doing filtering in {@link ResourceDialog}.
 * 
 * @author mitin_aa
 * @coverage android.support.resources
 */
public final class ResourceViewerFilter extends ViewerFilter {
  private String m_resourceNamePrefix;
  private Set<ResourceType> m_resourceTypeSet;
  private ResourceRepository m_resources;

  ////////////////////////////////////////////////////////////////////////////
  //
  // ViewerFilter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean select(Viewer viewer, Object parentElement, Object element) {
    if (element instanceof ResourceType) {
      boolean selected = m_resourceTypeSet.isEmpty() || m_resourceTypeSet.contains(element);
      if (selected) {
        // check children
        Collection<ResourceItem> resources =
            m_resources.getResourceItemsOfType((ResourceType) element);
        for (ResourceItem resourceItem : resources) {
          if (select(viewer, element, resourceItem)) {
            // at least one selected
            return true;
          }
        }
        return false;
      }
      return selected;
    } else if (element instanceof ResourceItem) {
      ResourceItem item = (ResourceItem) element;
      return StringUtils.isEmpty(m_resourceNamePrefix)
          || item.getName().startsWith(m_resourceNamePrefix);
    }
    return false;
  }

  /**
   * @param filterText
   */
  public void setResourcePrefix(String filterText) {
    m_resourceNamePrefix = filterText;
  }

  /**
   * @param resourceTypeSet
   */
  public void setResourceTypes(Set<ResourceType> resourceTypeSet) {
    m_resourceTypeSet = resourceTypeSet;
  }

  /**
   * @param repository
   */
  public void setRepository(ResourceRepository repository) {
    m_resources = repository;
  }
}
