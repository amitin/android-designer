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

import com.google.common.collect.Sets;

import org.eclipse.wb.android.internal.model.property.IPropertiesConstants;
import org.eclipse.wb.android.internal.model.property.accessor.AndroidExpressionAccessor;
import org.eclipse.wb.android.internal.model.property.accessor.FieldJavaValueAccessor;
import org.eclipse.wb.android.internal.model.property.accessor.IJavaValueAccessor;
import org.eclipse.wb.android.internal.model.property.accessor.MethodJavaValueAccessor;
import org.eclipse.wb.android.internal.model.property.converter.AndroidConverters;
import org.eclipse.wb.android.internal.model.property.editor.ColorPropertyEditor;
import org.eclipse.wb.android.internal.model.property.editor.DefaultEnumPropertyEditor;
import org.eclipse.wb.android.internal.model.property.editor.DefaultFlagsPropertyEditor;
import org.eclipse.wb.android.internal.model.property.editor.ReferencePropertyEditor;
import org.eclipse.wb.android.internal.model.property.editor.delegating.DelegatingPropertyEditor;
import org.eclipse.wb.android.internal.support.AndroidBridge;
import org.eclipse.wb.android.internal.support.AndroidUtils;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.FloatPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.IntegerPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.description.IDescriptionRulesProvider;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.accessor.FieldExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.accessor.MethodExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.xml.model.utils.NamespacesHelper;

import com.android.ide.common.api.IAttributeInfo;
import com.android.ide.common.api.IAttributeInfo.Format;
import com.android.ide.common.layout.LayoutConstants;
import com.android.ide.common.resources.platform.AttributeInfo;
import com.android.ide.common.resources.platform.DeclareStyleableInfo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adds a rule for working with Android xml attributes as properties.
 * 
 * @author mitin_aa
 * @coverage android.parser
 */
@SuppressWarnings("restriction")
public class AndroidDescriptionRulesProvider implements IDescriptionRulesProvider {
  public void addRules(Digester digester, EditorContext context, Class<?> componentClass) {
    if (ReflectionUtils.isSuccessorOf(componentClass, "android.view.View")
        || ReflectionUtils.isSuccessorOf(componentClass, "android.view.ViewGroup$LayoutParams")) {
      addAndroidPropertiesRule(digester, context);
    }
  }

  private void addAndroidPropertiesRule(final Digester digester, EditorContext ctx) {
    // hook on "standard-bean-properties"
    String pattern = "component/standard-bean-properties";
    digester.addRule(pattern, new AndroidPropertiesRule((AndroidEditorContext) ctx));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AndroidPropertiesRule
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class AndroidPropertiesRule extends Rule {
    private final AndroidEditorContext m_context;
    private final AndroidBridge m_androidBridge;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    private AndroidPropertiesRule(AndroidEditorContext context) {
      m_context = context;
      m_androidBridge = m_context.getAndroidBridge();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Rule
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
      ComponentDescription componentDescription = (ComponentDescription) digester.peek();
      addProperties(componentDescription);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Properties
    //
    ////////////////////////////////////////////////////////////////////////////
    private void addProperties(ComponentDescription componentDescription) throws Exception {
      Class<?> componentClass = componentDescription.getComponentClass();
      // create set
      Set<NamespacedAttribute> attrsSet = Sets.newHashSet();
      // collect
      collectAttributes(componentClass, attrsSet);
      for (Iterator<NamespacedAttribute> I = attrsSet.iterator(); I.hasNext();) {
        NamespacedAttribute attribute = I.next();
        createPropertyForAttribute(
            componentDescription,
            attribute,
            m_androidBridge.getEnumFlagValues(componentClass, attribute.attribute.getName()));
      }
    }

    /**
     * Invokes {@link #collectAttributes0} and adds 'style' attribute if needed.
     */
    private void collectAttributes(Class<?> componentClass, Set<NamespacedAttribute> attrsSet)
        throws Exception {
      collectAttributes0(componentClass, attrsSet);
      if (!AndroidUtils.isLayoutParamClass(componentClass)) {
        // All views and groups have an implicit "style" attribute which is a reference.
        AttributeInfo styleInfo = new AttributeInfo("style", new Format[]{Format.REFERENCE});
        styleInfo.setJavaDoc("A reference to a custom style");
        attrsSet.add(NamespacedAttribute.getNamespaced("", styleInfo));
      }
    }

    /**
     * Recursive. Collects xml attributes.
     */
    private void collectAttributes0(Class<?> componentClass, Set<NamespacedAttribute> attrsSet)
        throws Exception {
      if (componentClass.getSuperclass() != null) {
        collectAttributes0(componentClass.getSuperclass(), attrsSet);
      }
      DeclareStyleableInfo style =
          m_androidBridge.getStyleable(componentClass, getStyleableKey(componentClass));
      if (style != null) {
        AttributeInfo[] attributes = style.getAttributes();
        CollectionUtils.addAll(
            attrsSet,
            NamespacedAttribute.getNamespaced(
                AndroidUtils.getNamespace(componentClass, m_context.getJavaProject().getProject()),
                attributes));
      }
    }

    /**
     * Creates single attribute-based property for given attribute info. Tries to match java-based
     * properties to attribute-based.
     */
    private void createPropertyForAttribute(ComponentDescription componentDescription,
        NamespacedAttribute attribute,
        Map<String, Integer> enumFlagValues) throws Exception {
      IAttributeInfo attributeInfo = attribute.attribute;
      String namespaceUri = attribute.namespace;
      String localAttributeName = attributeInfo.getName();
      // prepare property parts
      String id =
          StringUtils.isEmpty(namespaceUri) ? localAttributeName : NamespacesHelper.getName(
              m_context.getRootElement(),
              namespaceUri) + ":" + localAttributeName;
      // prepare java-based value accessor
      IJavaValueAccessor javaValueAccessor =
          createJavaValueAccessor(componentDescription, localAttributeName);
      // create the accessor
      Format[] formats = attributeInfo.getFormats();
      ExpressionAccessor accessor =
          new AndroidExpressionAccessor(localAttributeName,
              namespaceUri,
              attributeInfo.getJavaDoc(),
              javaValueAccessor,
              AndroidConverters.getObjectConverter(formats, enumFlagValues));
      ExpressionConverter converter =
          AndroidConverters.getExpressionConverter(formats, enumFlagValues);
      // editor
      PropertyEditor editor = getPropertyEditor(attributeInfo, enumFlagValues);
      // create property descriptor
      GenericPropertyDescription propertyDescriptor =
          new GenericPropertyDescription(id, localAttributeName, null, accessor);
      // remove 'layout_' prefix
      if (localAttributeName.startsWith(LayoutConstants.ATTR_LAYOUT_PREFIX)) {
        propertyDescriptor.putTag(
            "title",
            StringUtils.removeStart(localAttributeName, LayoutConstants.ATTR_LAYOUT_PREFIX));
      }
      // converter & editor
      propertyDescriptor.setConverter(converter);
      propertyDescriptor.setEditor(editor);
      // store attribute local name, namespace & attribute info
      propertyDescriptor.putArbitraryValue(
          IPropertiesConstants.KEY_ATTRIBUTE_NAMESPACE_URI,
          namespaceUri);
      propertyDescriptor.putArbitraryValue(
          IPropertiesConstants.KEY_ATTRIBUTE_LOCAL_NAME,
          localAttributeName);
      propertyDescriptor.putArbitraryValue(IPropertiesConstants.KEY_ATTRIBUTE, attributeInfo);
      // store enums & flags values
      if (Format.FLAG.in(formats) || Format.ENUM.in(formats)) {
        propertyDescriptor.putArbitraryValue(
            IPropertiesConstants.KEY_ENUMS_FLAGS_VALUES_MAP,
            enumFlagValues);
      }
      componentDescription.addProperty(propertyDescriptor);
    }

    /**
     * Tries to find the way how to get a default property value from java object.
     */
    private IJavaValueAccessor createJavaValueAccessor(ComponentDescription componentDescription,
        String localAttributeName) {
      Class<?> componentClass = componentDescription.getComponentClass();
      // remove 'layout_' prefix, layout params attrs have such prefix
      if (localAttributeName.startsWith(LayoutConstants.ATTR_LAYOUT_PREFIX)) {
        localAttributeName =
            StringUtils.removeStart(localAttributeName, LayoutConstants.ATTR_LAYOUT_PREFIX);
      }
      // search for added class based property matching attribute
      String javaPropertyId = null;
      List<GenericPropertyDescription> properties = componentDescription.getProperties();
      for (GenericPropertyDescription propertyDescription : properties) {
        if (propertyDescription.getAccessor() instanceof AndroidExpressionAccessor) {
          // don't check twice
          continue;
        }
        String propertyName = propertyDescription.getName();
        if (localAttributeName.equals(propertyName)) {
          javaPropertyId = propertyDescription.getId();
          break;
        }
      }
      // try to get a method getter or field from java-based accessor, if any
      if (javaPropertyId != null) {
        GenericPropertyDescription javaPropertyDescriptor =
            componentDescription.getProperty(javaPropertyId);
        if (javaPropertyDescriptor.getAccessor() instanceof MethodExpressionAccessor) {
          MethodExpressionAccessor accessor =
              (MethodExpressionAccessor) javaPropertyDescriptor.getAccessor();
          Method getter = accessor.getGetter();
          if (getter != null) {
            return new MethodJavaValueAccessor(getter);
          }
        } else if (javaPropertyDescriptor.getAccessor() instanceof FieldExpressionAccessor) {
          FieldExpressionAccessor accessor =
              (FieldExpressionAccessor) javaPropertyDescriptor.getAccessor();
          Field field = accessor.getField();
          if (field != null) {
            return new FieldJavaValueAccessor(field);
          }
        }
      }
      // look for field named as attribute
      {
        Field field = ReflectionUtils.getFieldByName(componentClass, localAttributeName);
        if (field != null) {
          return new FieldJavaValueAccessor(field);
        }
      }
      // try special debug field
      {
        String fieldName = "m" + StringUtils.capitalize(localAttributeName);
        Field field = ReflectionUtils.getFieldByName(componentClass, fieldName);
        if (field != null) {
          return new FieldJavaValueAccessor(field);
        }
      }
      return null;
    }

    /**
     * Creates the property editor for Android property.
     */
    private PropertyEditor getPropertyEditor(IAttributeInfo attribute,
        Map<String, Integer> enumFlagValues) throws Exception {
      PropertyEditor editor = getPropertyEditorPrimitive(attribute, enumFlagValues);
      Format[] formats = attribute.getFormats();
      if (Format.REFERENCE.in(formats)) {
        if (editor != null) {
          DelegatingPropertyEditor delegatingEditor = new DelegatingPropertyEditor();
          delegatingEditor.add(new ReferencePropertyEditor());
          delegatingEditor.add(editor);
          editor = delegatingEditor;
        } else {
          editor = new ReferencePropertyEditor();
        }
      }
      return editor;
    }

    /**
     * Creates the property editor for Android property for simple types.
     */
    private PropertyEditor getPropertyEditorPrimitive(IAttributeInfo attributeInfo,
        Map<String, Integer> enumFlagValues) throws Exception {
      Format[] formats = attributeInfo.getFormats();
      if (Format.FLAG.in(formats)) {
        return new DefaultFlagsPropertyEditor(enumFlagValues);
      }
      if (Format.ENUM.in(formats)) {
        return new DefaultEnumPropertyEditor(enumFlagValues);
      }
      if (Format.BOOLEAN.in(formats)) {
        return BooleanPropertyEditor.INSTANCE;
      }
      if (Format.COLOR.in(formats)) {
        return ColorPropertyEditor.INSTANCE;
      }
      if (Format.INTEGER.in(formats)) {
        return IntegerPropertyEditor.INSTANCE;
      }
      if (Format.FLOAT.in(formats)) {
        return FloatPropertyEditor.INSTANCE;
      }
      return DescriptionPropertiesHelper.getEditorForType(String.class);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Inner classes
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Wrapper for attribute to have a namespace.
   */
  private static final class NamespacedAttribute {
    private final IAttributeInfo attribute;
    private final String namespace;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    private NamespacedAttribute(String namespace, AttributeInfo attribute) {
      this.namespace = namespace;
      this.attribute = attribute;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Object
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof NamespacedAttribute)) {
        return false;
      }
      NamespacedAttribute other = (NamespacedAttribute) obj;
      return attribute.getName().equals(other.attribute.getName())
          && namespace.equals(other.namespace);
    }

    @Override
    public int hashCode() {
      return (namespace + attribute.getName()).hashCode();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public static NamespacedAttribute[] getNamespaced(String namespace, AttributeInfo[] attributes) {
      NamespacedAttribute[] named = new NamespacedAttribute[attributes.length];
      for (int i = 0; i < attributes.length; i++) {
        named[i] = new NamespacedAttribute(namespace, attributes[i]);
      }
      return named;
    }

    public static NamespacedAttribute getNamespaced(String namespace, AttributeInfo attribute) {
      return new NamespacedAttribute(namespace, attribute);
    }
  }

  /**
   * @return the key string to find a styleable in styleable map (attrs.xml).
   */
  private static String getStyleableKey(Class<?> componentClass) {
    // HACK: TableRow.LayoutParams has TableRow_Cell key
    if (ReflectionUtils.isSuccessorOf(componentClass, "android.widget.TableRow$LayoutParams")) {
      return "TableRow_Cell";
    }
    // for layout params it is like'LinearLayout_Layout', for ordinary view it is the short class name.
    String className = componentClass.getName();
    String classShortName = CodeUtils.getShortClass(className);
    String key;
    if (AndroidUtils.isLayoutParamClass(componentClass)) {
      Class<?> enclosingClass = componentClass.getEnclosingClass();
      // Transforms "LinearLayout" and "LayoutParams" into "LinearLayout_Layout".
      String xmlName =
          String.format(
              "%1$s_%2$s",
              CodeUtils.getShortClass(enclosingClass.getName()),
              classShortName);
      key = xmlName.replaceFirst("Params$", "");
    } else {
      key = classShortName;
    }
    return key;
  }
}
