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
package org.eclipse.wb.android.internal.model.property.event;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import org.eclipse.wb.internal.core.utils.GenericTypeResolver;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Information about single event listener.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage android.model.property
 */
public final class ListenerInfo {
  private final Class<?> m_componentClass;
  private final Method m_method;
  private final String m_methodSignature;
  private final boolean m_deprecated;
  private String m_name;
  private final String m_simpleName;
  private final Class<?> m_interfaceType;
  private final GenericTypeResolver m_externalResolver;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ListenerInfo(Method method, Class<?> componentClass, GenericTypeResolver externalResolver) {
    m_method = method;
    m_componentClass = componentClass;
    m_externalResolver = externalResolver;
    m_methodSignature = ReflectionUtils.getMethodSignature(m_method);
    m_deprecated = m_method.getAnnotation(Deprecated.class) != null;
    m_name = _getListenerName(m_method);
    m_simpleName = _getListenerSimpleName(m_method);
    m_interfaceType = m_method.getParameterTypes()[0];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the "qualified" name of listener method, it should be shortened later, if possible.
   */
  private static String _getListenerName(Method addListenerMethod) {
    String name = _getListenerSimpleName(addListenerMethod);
    // use qualified name, turn into simple later 
    String parameterName = "(" + addListenerMethod.getParameterTypes()[0].getName() + ")";
    return name + parameterName;
  }

  /**
   * @return the name of listener method, to display for user. For example <code>key</code> for
   *         <code>addKeyListener()</code>.
   */
  private static String _getListenerSimpleName(Method addListenerMethod) {
    String name = addListenerMethod.getName();
    // convert into simple name
    name = StringUtils.removeStart(name, "set");
    name = StringUtils.removeEnd(name, "Listener");
    name = StringUtils.uncapitalize(name);
    // if become empty, use full name
    if (name.length() == 0) {
      name = addListenerMethod.getName();
    }
    return name;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the <code>setOnXXXListener</code> {@link Method}.
   */
  public Method getMethod() {
    return m_method;
  }

  /**
   * @return the signature of <code>setOnXXXListener</code> {@link Method}.
   */
  public String getMethodSignature() {
    return m_methodSignature;
  }

  /**
   * @return <code>true</code> if listener {@link Method} is deprecated.
   */
  public boolean isDeprecated() {
    return m_deprecated;
  }

  /**
   * @return the name of this listener.
   */
  public String getName() {
    return m_name;
  }

  /**
   * @return the simple name of this listener.
   */
  public String getSimpleName() {
    return m_simpleName;
  }

  /**
   * @return the listener interface {@link Class}.
   */
  public Class<?> getInterface() {
    return m_interfaceType;
  }

  /**
   * @return the {@link Class} that should be used for this listener.
   */
  public Class<?> getListenerType() {
    return m_interfaceType;
  }

  public GenericTypeResolver getResolver() {
    GenericTypeResolver resolver_1 =
        GenericTypeResolver.superClass(
            m_externalResolver,
            m_componentClass,
            m_method.getDeclaringClass());
    GenericTypeResolver resolver_2 = GenericTypeResolver.argumentOfMethod(resolver_1, m_method, 0);
    return resolver_2;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Methods access
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<ListenerMethodInfo> m_methods;

  /**
   * @return the {@link ListenerMethodInfo}'s for each method in this listener.
   */
  public List<ListenerMethodInfo> getMethods() {
    if (m_methods == null) {
      m_methods = Lists.newArrayList();
      // prepare method information objects
      for (Method method : m_interfaceType.getMethods()) {
        if (isListenerMethod(method)) {
          m_methods.add(new ListenerMethodInfo(this, method));
        }
      }
      // sort by name
      Collections.sort(m_methods, new Comparator<ListenerMethodInfo>() {
        public int compare(ListenerMethodInfo method_1, ListenerMethodInfo method_2) {
          return method_1.getName().compareTo(method_2.getName());
        }
      });
    }
    return m_methods;
  }

  /**
   * @return <code>true</code> if given {@link Method} is valid handler for some event in listener.
   */
  private boolean isListenerMethod(Method method) {
    if (method.isBridge()) {
      return false;
    }
    if (ReflectionUtils.isAbstract(method)) {
      return true;
    }
    if (method.getDeclaringClass() == m_interfaceType) {
      return true;
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  static void useSimpleNamesWherePossible(List<ListenerInfo> listeners) {
    // prepare map: simple name -> qualified names
    Multimap<String, String> simplePropertyNames = HashMultimap.create();
    for (ListenerInfo listener : listeners) {
      String qualifiedName = listener.getName();
      String simpleName = getSimpleName(qualifiedName);
      simplePropertyNames.put(simpleName, qualifiedName);
    }
    // if simple name is unique, use it
    for (ListenerInfo listener : listeners) {
      String qualifiedName = listener.getName();
      String simpleName = getSimpleName(qualifiedName);
      if (simplePropertyNames.get(simpleName).size() == 1) {
        listener.m_name = simpleName;
      }
    }
  }

  private static String getSimpleName(String qualifiedName) {
    return StringUtils.substringBefore(qualifiedName, "(");
  }
}
