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
package org.eclipse.wb.android.internal.model.util;

import com.google.common.collect.Lists;

import org.eclipse.wb.android.internal.model.property.event.AndroidEventProperty;
import org.eclipse.wb.android.internal.model.property.event.ListenerInfo;
import org.eclipse.wb.internal.core.utils.GenericTypeResolver;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ClassMap;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectEventListeners;
import org.eclipse.wb.internal.core.xml.model.property.event.AbstractListenerProperty;


import org.apache.commons.lang.ArrayUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Support adding Android specific {@link AbstractListenerProperty}s.
 * 
 * @author mitin_aa
 * @coverage android.model
 */
public final class AndroidListenerProperties {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AndroidListenerProperties(XmlObjectInfo rootObject) {
    rootObject.addBroadcastListener(new XmlObjectEventListeners() {
      public void invoke(XmlObjectInfo object, List<AbstractListenerProperty> properties)
          throws Exception {
        addListeners(object, properties);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final ClassMap<List<ListenerInfo>> m_widgetEvents = ClassMap.create();

  /**
   * Adds listeners for events supported by given widget.
   */
  private void addListeners(XmlObjectInfo object, List<AbstractListenerProperty> properties)
      throws Exception {
    List<ListenerInfo> events = getWidgetEvents(object);
    for (ListenerInfo listener : events) {
      properties.add(new AndroidEventProperty(object, listener));
    }
  }

  /**
   * @return the {@link List} of events which are supported by given widget.
   */
  private static List<ListenerInfo> getWidgetEvents(XmlObjectInfo widget) {
    Class<?> componentClass = widget.getDescription().getComponentClass();
    List<ListenerInfo> events = m_widgetEvents.get(componentClass);
    if (events == null) {
      GenericTypeResolver typeResolver = new GenericTypeResolver(null);
      events = Lists.newArrayList();
      m_widgetEvents.put(componentClass, events);
      {
        // events in Android has 'OnXXXListener' inner class as listener interface with
        // appropriate 'setOnXXXListener' method in main class
        Pattern pattern = Pattern.compile("On.*Listener");
        Class<?>[] classes = componentClass.getClasses();
        if (!ArrayUtils.isEmpty(classes)) {
          for (Class<?> innerClass : classes) {
            String shortClassName = CodeUtils.getShortClass(innerClass.getName());
            Matcher matcher = pattern.matcher(shortClassName);
            if (matcher.matches()) {
              // additionally check for method, it should be public (RefUtils returns with any visibility)
              Method method =
                  ReflectionUtils.getMethod(componentClass, "set" + shortClassName, innerClass);
              if (method != null && (method.getModifiers() & Modifier.PUBLIC) != 0) {
                ListenerInfo listener = new ListenerInfo(method, componentClass, typeResolver);
                events.add(listener);
              }
            }
          }
        }
      }
      // sort
      Collections.sort(events, new Comparator<ListenerInfo>() {
        public int compare(ListenerInfo o1, ListenerInfo o2) {
          return o1.getSimpleName().compareTo(o2.getSimpleName());
        }
      });
    }
    return events;
  }
}
