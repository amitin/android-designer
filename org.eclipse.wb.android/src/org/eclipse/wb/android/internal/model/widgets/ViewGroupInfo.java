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

import org.eclipse.wb.android.internal.Activator;
import org.eclipse.wb.android.internal.editor.ExtractIncludeAction;
import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;

import com.android.ide.common.layout.LayoutConstants;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Base class for views which are container views.
 * 
 * @author mitin_aa
 * @coverage android.model
 */
@SuppressWarnings("restriction")
public class ViewGroupInfo extends ViewInfo {
  private final Map<XmlObjectInfo, Property> m_layoutProperties = Maps.newIdentityHashMap();
  private final ViewGroupInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewGroupInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    setupAddSelectionActions();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link DocumentElement} representing the text to display on an empty ViewGroup.
   */
  protected DocumentElement getEmptyChildrenElement() throws Exception {
    DocumentElement element = getElement();
    String text = XmlObjectUtils.getParameter(this, "emptyViewGroupText");
    if (element != null && !StringUtils.isEmpty(text) && !isIncluded()) {
      DocumentElement fakeElement = new DocumentElement("TextView");
      element.addChild(fakeElement);
      fakeElement.setAttribute("android:text", text);
      return fakeElement;
    }
      return null;
    }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Contributes properties for layout for given child.
   * 
   * TODO: use View.getLayoutParams() to fetch default layout properties values? But looks like they
   * don't have any actually useful default values.
   */
  public void addLayoutProperties(final ViewInfo child, List<Property> properties) throws Exception {
    ComplexProperty property = getLayoutParamProperty(child);
    if (property != null) {
      properties.add(property);
    }
  }

  private ComplexProperty getLayoutParamProperty(final ViewInfo child) throws Exception {
    Property layoutProperty = m_layoutProperties.get(child);
    if (layoutProperty == null) {
      Class<?> layoutParamsClass = getLayoutParamsClass();
      // no params could be found
      if (layoutParamsClass == null) {
        return null;
      }
      ComponentDescription description =
          ComponentDescriptionHelper.getDescription(getContext(), layoutParamsClass);
      List<GenericPropertyDescription> propertiesDescriptions = description.getProperties();
      // still no layout params?
      if (propertiesDescriptions.isEmpty()) {
        return null;
      }
      List<Property> layoutProperties = Lists.newArrayList();
      for (GenericPropertyDescription propertyDescription : propertiesDescriptions) {
        GenericProperty property = new GenericPropertyImpl(child, propertyDescription);
        layoutProperties.add(property);
      }
      String propertyTitle =
          CodeUtils.getShortClass(getDescription().getComponentClass().getName());
      layoutProperty =
          new ComplexProperty("LayoutParams",
              propertyTitle,
              layoutProperties.toArray(new Property[layoutProperties.size()]));
      layoutProperty.setCategory(PropertyCategory.system(8));
      m_layoutProperties.put(child, layoutProperty);
    }
    return (ComplexProperty) layoutProperty;
  }

  /**
   * @return the LayoutParam property for given this ViewGroup <code>child</code>.
   */
  public final Property getLayoutPropertyByTitle(ViewInfo child, String title) throws Exception {
    ComplexProperty layoutProperty = getLayoutParamProperty(child);
    if (layoutProperty == null) {
      return null;
    }
    Property[] properties = layoutProperty.getProperties();
    for (Property property : properties) {
      if (property.getTitle().equals(title)) {
        return property;
      }
    }
    return null;
  }

  protected final Class<?> getLayoutParamsClass() {
    Class<?> componentClass = getDescription().getComponentClass();
    // search component class
    Class<?> layoutParamsClass = findLayoutParamsClass(componentClass);
    if (layoutParamsClass == null) {
      // not found, go search in super-classes
      for (Class<?> superClass = componentClass.getSuperclass(); layoutParamsClass == null
          && superClass != null; superClass = superClass.getSuperclass()) {
        layoutParamsClass = findLayoutParamsClass(superClass);
      }
    }
    return layoutParamsClass;
  }

  private Class<?> findLayoutParamsClass(Class<?> componentClass) {
    Class<?>[] classes = componentClass.getDeclaredClasses();
    for (Class<?> clazz : classes) {
      if (ReflectionUtils.isSuccessorOf(clazz, "android.view.ViewGroup$LayoutParams")) {
        return clazz;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Actions support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if the layout_width/layout_height property set to other than
   *         'wrap_content'.
   */
  protected boolean isMatchingParent(ViewInfo widget, boolean isHorizontal) throws Exception {
    String title = isHorizontal ? "width" : "height";
    Property property = getLayoutPropertyByTitle(widget, title);
    if (property == null) {
      return false;
    }
    Object value = property.getValue();
    if (Property.UNKNOWN_VALUE == value) {
      return false;
    }
    // wrap_content == -2
    return (Integer) value != -2;
  }

  /**
   * Toggles layout_width/layout_height property value to 'match_content' or 'wrap_parent'.
   */
  protected void toggleMatchParent(ViewInfo widget, boolean isHorizontal, boolean match)
      throws Exception {
    String title = isHorizontal ? "width" : "height";
    Property property = getLayoutPropertyByTitle(widget, title);
    if (property == null) {
      return;
    }
    String expression =
        match ? LayoutConstants.VALUE_MATCH_PARENT : LayoutConstants.VALUE_WRAP_CONTENT;
    ((GenericPropertyImpl) property).setExpression(expression, Property.UNKNOWN_VALUE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection actions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Setup toolbar/popup selection actions.
   */
  private void setupAddSelectionActions() {
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addSelectionActions(List<ObjectInfo> objects, List<Object> actions)
          throws Exception {
        if (objects.isEmpty()) {
          return;
        }
        // target is not on our container
        if (objects.get(0).getParent() != m_this) {
          return;
        }
        // create match parent toggle actions
        actions.add(new Separator());
        addMatchParentAction(objects, actions, true, "grow.gif", "Toggle Fill Width.");
        addMatchParentAction(objects, actions, false, "grow.gif", "Toggle Fill Height.");
      }

      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object == m_this && !m_this.isRoot() && getElement() != null && !isIncluded()) {
          ExtractIncludeAction action = new ExtractIncludeAction(m_this);
          manager.appendToGroup(IContextMenuConstants.GROUP_LAYOUT, action);
        }
      }
    });
  }

  private void addMatchParentAction(List<ObjectInfo> objects,
      List<Object> actions,
      boolean isHorisontal,
      String imagePath,
      String tooltip) throws Exception {
    boolean isChecked = true;
    for (ObjectInfo object : objects) {
      if (!isMatchingParent((ViewInfo) object, isHorisontal)) {
        isChecked = false;
        break;
      }
    }
    actions.add(new MatchParentToggleAction(objects,
        isHorisontal,
        imagePath,
        tooltip,
        isChecked,
        !isChecked));
  }

  private final class MatchParentToggleAction extends ObjectInfoAction {
    private final boolean m_match;
    private final boolean m_horizontal;
    private final List<ObjectInfo> m_objects;

    public MatchParentToggleAction(List<ObjectInfo> objects,
        boolean horizontal,
        String iconPath,
        String tooltip,
        boolean checked,
        boolean match) {
      super(m_this, "", AS_CHECK_BOX);
      m_objects = objects;
      m_horizontal = horizontal;
      String path = "info/layout/ViewGroup/" + (m_horizontal ? "h" : "v") + "/menu/" + iconPath;
      setImageDescriptor(Activator.getImageDescriptor(path));
      setToolTipText(tooltip);
      setChecked(checked);
      m_match = match;
    }

    @Override
    protected void runEx() throws Exception {
      for (ObjectInfo object : m_objects) {
        toggleMatchParent((ViewInfo) object, m_horizontal, m_match);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE_after(ViewInfo child, ViewInfo nextChild) throws Exception {
    // apply default layout parameters
    applyChildDefaultLayoutParams(child);
  }

  /**
   * Apply default layout parameters.
   */
  protected void applyChildDefaultLayoutParams(ViewInfo child) throws Exception {
    toggleMatchParent(child, true, true);
    toggleMatchParent(child, false, false);
  }
}
