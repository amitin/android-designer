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
package org.eclipse.wb.android.internal.model.widgets;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.android.internal.parser.AndroidEditorContext;
import org.eclipse.wb.android.internal.parser.AndroidHierarchyBuilder;
import org.eclipse.wb.android.internal.support.AndroidBridge;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;

import org.eclipse.swt.graphics.Image;


import java.util.List;
import java.util.Map;

/**
 * Model for any View in Android.
 * 
 * @author mitin_aa
 * @coverage android.model
 */
public class ViewInfo extends AbstractComponentInfo {
  public static final String FLAG_HAS_DEFAULT_PROPERTIES = "hasDefaultProperties";
  private com.android.ide.common.rendering.api.ViewInfo m_androidViewInfo;
  private final ViewInfo m_this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    m_this = this;
    addBroadcastListeners();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcast
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds broadcast listeners.
   */
  private void addBroadcastListeners() {
    addBroadcastListener(new XmlObjectAddProperties() {
      public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
        if (object == m_this) {
          ViewGroupInfo parent = getParentViewGroup();
          if (parent != null) {
            parent.addLayoutProperties(m_this, properties);
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_create() throws Exception {
    if (isRoot()) {
      final Map<DocumentElement, XmlObjectInfo> doc2Child = Maps.newHashMap();
      final List<DocumentElement> emptyChildrenElements = Lists.newArrayList();
      // process hierarchy 
      accept(new ObjectInfoVisitor() {
        @Override
        public boolean visit(ObjectInfo objectInfo) throws Exception {
          XmlObjectInfo xmlObjectInfo = (XmlObjectInfo) objectInfo;
          doc2Child.put(xmlObjectInfo.getElement(), xmlObjectInfo);
          if (objectInfo instanceof ViewGroupInfo) {
            ViewGroupInfo viewGroupInfo = (ViewGroupInfo) objectInfo;
            if (viewGroupInfo.getChildren(ViewInfo.class).isEmpty()) {
              // add fake child to empty container
              DocumentElement element = viewGroupInfo.getEmptyChildrenElement();
              if (element != null) {
                emptyChildrenElements.add(element);
              }
            }
          }
          return true;
        }
      });
      // render...
      AndroidBridge androidBridge = getAndroidBridge();
      androidBridge.render();
      // remove fake children
      for (DocumentElement documentElement : emptyChildrenElements) {
        documentElement.remove();
      }
      // process objects
      androidBridge.accept(new AndroidHierarchyBuilder() {
        @Override
        public ObjectInfo visit(com.android.ide.common.rendering.api.ViewInfo nativeView,
            ObjectInfo parent) throws Exception {
          DocumentElement documentElement = (DocumentElement) nativeView.getCookie();
          XmlObjectInfo objectInfo = doc2Child.get(documentElement);
          if (objectInfo == null) {
            return null;
          }
          // for newly created components it needs to fetch default values
          if (objectInfo.getArbitraryValue(FLAG_HAS_DEFAULT_PROPERTIES) != Boolean.TRUE) {
            objectInfo.putArbitraryValue(FLAG_HAS_DEFAULT_PROPERTIES, Boolean.TRUE);
          } else {
            // skip re-fetch default values as they are may be changed already
            objectInfo.setObjectReadySent(true);
          }
          objectInfo.setObject(nativeView.getViewObject());
          // store native java object info
          if (objectInfo instanceof ViewInfo) {
            ViewInfo viewInfo = (ViewInfo) objectInfo;
            viewInfo.setAndroidViewInfo(nativeView);
          }
          return objectInfo;
        }
      });
    }
    super.refresh_create();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    if (isRoot()) {
      AndroidEditorContext androidContext = getAndroidContext();
      Image image = androidContext.getAndroidBridge().getImage();
      setImage(image);
    }
    // prepare model bounds
    Rectangle modelBounds;
    {
      int x = m_androidViewInfo.getLeft();
      int y = m_androidViewInfo.getTop();
      int width = m_androidViewInfo.getRight() - m_androidViewInfo.getLeft();
      int height = m_androidViewInfo.getBottom() - m_androidViewInfo.getTop();
      modelBounds = new Rectangle(x, y, width, height);
      if (isRoot()) {
        // FIXME need full hierarchy rolling up
        org.eclipse.swt.graphics.Rectangle imageBounds = getImage().getBounds();
        Insets insets =
            new Insets(y, x, imageBounds.height - y - height, imageBounds.width - x - width);
        setClientAreaInsets(insets);
        modelBounds.crop(insets.getNegated());
      }
      setModelBounds(modelBounds);
    }
    super.refresh_fetch();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Live" support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the instance of {@link AndroidLiveManager} to fetch "live" data.
   */
  protected AndroidLiveManager getLiveComponentsManager() {
    return new AndroidLiveManager(this);
  }

  @Override
  protected Image getLiveImage() {
    return getLiveComponentsManager().getImage();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TopBoundsSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new AndroidTopBoundsSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  public final AndroidEditorContext getAndroidContext() {
    return (AndroidEditorContext) getContext();
  }

  public final AndroidBridge getAndroidBridge() {
    return getAndroidContext().getAndroidBridge();
  }

  /**
   * @return parent as ViewGroupInfo.
   */
  protected final ViewGroupInfo getParentViewGroup() {
    return (ViewGroupInfo) getParent();
  }

  public final List<ViewInfo> getChildrenViews() {
    return getChildren(ViewInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param viewInfo
   */
  public void setAndroidViewInfo(com.android.ide.common.rendering.api.ViewInfo viewInfo) {
    m_androidViewInfo = viewInfo;
  }

  /**
   * @return <code>true</code> if this {@link ViewInfo} describes included UI-element;
   */
  public boolean isIncluded() {
    DocumentElement element = getElement();
    if (element != null) {
      return "include".equalsIgnoreCase(element.getTag());
    }
    return false;
  }
}
