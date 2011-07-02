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
package org.eclipse.wb.android.internal.support;

import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;

import static com.android.ide.common.layout.LayoutConstants.ATTR_ID;
import static com.android.ide.common.layout.LayoutConstants.ID_PREFIX;
import static com.android.ide.common.layout.LayoutConstants.NEW_ID_PREFIX;


import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * ID managing utils.
 * 
 * Some code is from ADT's DescriptorsUtils.
 * 
 * @author mitin_aa
 * @coverage android.support
 */
@SuppressWarnings("restriction")
public final class IdSupport {
  private static final String DEFAULT_WIDGET_PREFIX = "widget";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private IdSupport() {
  }

  /**
   * Removes "@id/" or "@id+/" prefixes resulting id suitable to find the View with.
   */
  public static String getSimpleId(String id) {
    id = StringUtils.removeStart(id, NEW_ID_PREFIX);
    id = StringUtils.removeStart(id, ID_PREFIX);
    return id;
  }

  /**
   * @return the 'id' attribute associated with View. If no id is set yet, it generates the new one
   *         and associates it with View.
   */
  public static String getId(ViewInfo view) throws Exception {
    String id = getIdOrNull(view);
    if (id == null) {
      DocumentElement element = view.getElement();
      id = getFreeWidgetId(element);
      view.getPropertyByTitle(ATTR_ID).setValue(id);
    }
    return id;
  }

  /**
   * @return the 'id' attribute associated with View. If no id is set yet, returns null.
   */
  public static String getIdOrNull(ViewInfo view) throws Exception {
    Property idProperty = view.getPropertyByTitle(ATTR_ID);
    Object value = idProperty.getValue();
    if (value instanceof String) {
      return (String) value;
    }
    return null;
  }

  /**
   * Given a UI node, returns the first available id that matches the pattern "prefix%d".
   * <p/>
   * TabWidget is a special case and the method will always return "@android:id/tabs".
   * 
   * @param element
   *          The UI node that gives the prefix to match.
   * @return A suitable generated id in the attribute form needed by the XML id tag (e.g.
   *         "@+id/something")
   */
  public static String getFreeWidgetId(DocumentElement element) {
    String name = element.getTagLocal();
    return getFreeWidgetId(element.getRoot(), name);
  }

  /**
   * Given a UI root node and a potential XML node name, returns the first available id that matches
   * the pattern "prefix%d".
   * <p/>
   * TabWidget is a special case and the method will always return "@android:id/tabs".
   * 
   * @param element
   *          The root UI node to search for name conflicts from
   * @param name
   *          The XML node prefix name to look for
   * @return A suitable generated id in the attribute form needed by the XML id tag (e.g.
   *         "@+id/something")
   */
  private static String getFreeWidgetId(DocumentElement element, String name) {
    if ("TabWidget".equals(name)) { //$NON-NLS-1$
      return "@android:id/tabs"; //$NON-NLS-1$
    }
    return NEW_ID_PREFIX + getFreeWidgetId(element, new Object[]{name, null, null, null});
  }

  /**
   * Given a UI root node, returns the first available id that matches the pattern "prefix%d".
   * 
   * For recursion purposes, a "context" is given. Since Java doesn't have in-out parameters in
   * methods and we're not going to do a dedicated type, we just use an object array which must
   * contain one initial item and several are built on the fly just for internal storage:
   * <ul>
   * <li>prefix(String): The prefix of the generated id, i.e. "widget". Cannot be null.
   * <li>index(Integer): The minimum index of the generated id. Must start with null.
   * <li>generated(String): The generated widget currently being searched. Must start with null.
   * <li>map(Set<String>): A set of the ids collected so far when walking through the widget
   * hierarchy. Must start with null.
   * </ul>
   * 
   * @param rootElement
   *          The Ui root node where to start searching recursively. For the initial call you want
   *          to pass the document root.
   * @param params
   *          An in-out context of parameters used during recursion, as explained above.
   * @return A suitable generated id
   */
  @SuppressWarnings("unchecked")
  private static String getFreeWidgetId(DocumentElement rootElement, Object[] params) {
    Set<String> map = (Set<String>) params[3];
    if (map == null) {
      params[3] = map = new HashSet<String>();
    }
    int num = params[1] == null ? 0 : ((Integer) params[1]).intValue();
    String generated = (String) params[2];
    String prefix = (String) params[0];
    if (generated == null) {
      int pos = prefix.indexOf('.');
      if (pos >= 0) {
        prefix = prefix.substring(pos + 1);
      }
      pos = prefix.indexOf('$');
      if (pos >= 0) {
        prefix = prefix.substring(pos + 1);
      }
      prefix = prefix.replaceAll("[^a-zA-Z]", ""); //$NON-NLS-1$ $NON-NLS-2$
      if (prefix.length() == 0) {
        prefix = DEFAULT_WIDGET_PREFIX;
      } else {
        // Lowercase initial character
        prefix = Character.toLowerCase(prefix.charAt(0)) + prefix.substring(1);
      }
      do {
        num++;
        generated = String.format("%1$s%2$d", prefix, num); //$NON-NLS-1$
      } while (map.contains(generated.toLowerCase()));
      params[0] = prefix;
      params[1] = num;
      params[2] = generated;
    }
    String id = rootElement.getAttribute("android:" + ATTR_ID);
    if (id != null) {
      id = id.replace(NEW_ID_PREFIX, ""); //$NON-NLS-1$
      id = id.replace(ID_PREFIX, ""); //$NON-NLS-1$
      if (map.add(id.toLowerCase()) && map.contains(generated.toLowerCase())) {
        do {
          num++;
          generated = String.format("%1$s%2$d", prefix, num); //$NON-NLS-1$
        } while (map.contains(generated.toLowerCase()));
        params[1] = num;
        params[2] = generated;
      }
    }
    for (DocumentElement child : rootElement.getChildren()) {
      getFreeWidgetId(child, params);
    }
    // Note: return params[2] (not "generated") since it could have changed during recursion.
    return (String) params[2];
  }
}
