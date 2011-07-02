package org.eclipse.wb.android.internal.model.util;

import org.eclipse.wb.android.internal.parser.AndroidDescriptionProcessor;
import org.eclipse.wb.android.internal.support.AndroidUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectResolveTag;
import org.eclipse.wb.internal.core.xml.model.utils.NamespacesHelper;


import org.apache.commons.lang.StringUtils;

/**
 * {@link XmlObjectResolveTag} for Android.
 * 
 * @author mitin_aa
 * @coverage android.model
 */
public final class AndroidTagResolver extends NamespacesHelper {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AndroidTagResolver(XmlObjectInfo rootObject) {
    super(rootObject.getCreationSupport().getElement());
    rootObject.addBroadcastListener(new XmlObjectResolveTag() {
      public void invoke(XmlObjectInfo object, Class<?> clazz, String[] namespace, String[] tag)
          throws Exception {
        invoke0(object, clazz, namespace, tag);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // XmlObjectResolveTag
  //
  ////////////////////////////////////////////////////////////////////////////
  private void invoke0(XmlObjectInfo object, Class<?> clazz, String[] namespace, String[] tag)
      throws Exception {
    if (AndroidDescriptionProcessor.isAndroid(object)) {
      String className = clazz.getName();
      namespace[0] = "";
      if (AndroidUtils.isFrameworkClass(clazz)) {
        className = StringUtils.substringAfterLast(className, ".");
      }
      // use fully-qualified for custom widgets
      tag[0] = className;
    }
  }
}
