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
package org.eclipse.wb.android.internal.parser;

import org.eclipse.wb.android.internal.model.util.AndroidListenerProperties;
import org.eclipse.wb.android.internal.model.util.AndroidTagResolver;
import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.utils.GlobalStateXml;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;


/**
 * Parser for Android UI.
 * 
 * @author mitin_aa
 * @coverage android.parser
 */
public final class AndroidParser {
  private final AndroidEditorContext m_context;
  private XmlObjectInfo m_rootModel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AndroidParser(AndroidEditorContext context) throws Exception {
    m_context = context;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  public XmlObjectInfo parse() throws Exception {
    // prepare for parsing
    GlobalStateXml.setEditorContext(m_context);
    m_context.initialize();
    m_context.setParsing(true);
    //
    m_context.getAndroidBridge().parse();
    buildHierarhy();
    // done
    m_context.setParsing(false);
    XmlObjectUtils.callRootProcessors(m_rootModel);
    new AndroidTagResolver(m_rootModel);
    new AndroidListenerProperties(m_rootModel);
    //
    GlobalStateXml.activate(m_rootModel);
    m_rootModel.getBroadcast(ObjectInfoTreeComplete.class).invoke();
    m_rootModel.refresh_dispose();
    return m_rootModel;
  }

  /**
   * 
   */
  private void buildHierarhy() throws Exception {
    m_context.getAndroidBridge().accept(new AndroidHierarchyBuilder() {
      @Override
      public ObjectInfo visit(com.android.ide.common.rendering.api.ViewInfo nativeView,
          ObjectInfo parent) throws Exception {
        Object androidObject = nativeView.getViewObject();
        DocumentElement element = (DocumentElement) nativeView.getCookie();
        if (element == null || androidObject == null) {
          return null;
        }
        XmlObjectInfo objectInfo = createObjectInfo(androidObject, element);
        if (objectInfo != null) {
          if (parent == null) {
            m_rootModel = objectInfo; // FIXME
          } else {
            parent.addChild(objectInfo);
          }
        }
        return objectInfo;
      }
    });
  }

  private XmlObjectInfo createObjectInfo(Object targetObject, DocumentElement element)
      throws Exception {
    XmlObjectInfo objectInfo;
    {
      Class<?> componentClass = targetObject.getClass();
      CreationSupport creationSupport = new ElementCreationSupport(element);
      objectInfo = XmlObjectUtils.createObject(m_context, componentClass, creationSupport);
      GlobalStateXml.activate(objectInfo);
    }
    // done
    objectInfo.setObject(targetObject);
    objectInfo.putArbitraryValue(ViewInfo.FLAG_HAS_DEFAULT_PROPERTIES, Boolean.TRUE);
    return objectInfo;
  }
}
