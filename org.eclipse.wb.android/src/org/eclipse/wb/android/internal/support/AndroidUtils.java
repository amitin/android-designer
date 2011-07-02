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

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.parser.QAttribute;
import org.eclipse.wb.internal.core.utils.xml.parser.QHandlerAdapter;
import org.eclipse.wb.internal.core.utils.xml.parser.QParser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import com.android.ide.eclipse.adt.internal.project.ProjectHelper;
import com.android.sdklib.SdkConstants;

import org.apache.commons.collections.map.ReferenceMap;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * Various Android-related utils.
 * 
 * @author mitin_aa
 * @coverage android.support
 */
@SuppressWarnings("restriction")
public final class AndroidUtils {
  private final static String NS_CUSTOM_RESOURCES = "http://schemas.android.com/apk/res/%1$s"; //$NON-NLS-1$

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private AndroidUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  private static Map<IProject, ManifestTrackingData> m_manifestTracking =
      new ReferenceMap(ReferenceMap.WEAK, ReferenceMap.HARD);

  private static class ManifestTrackingData {
    private final long timeStamp;
    private final String packageName;

    private ManifestTrackingData(long timeStamp, String packageName) {
      this.timeStamp = timeStamp;
      this.packageName = packageName;
    }
  }

  /**
   * @return package name for application as defined in AndroidManifest.xml.
   */
  public static String getPackageFromManifest(IProject project) throws Exception {
    IFile manifest = ProjectHelper.getManifest(project);
    // check for existing data
    long timeStamp = manifest.getModificationStamp();
    ManifestTrackingData trackingInfo = m_manifestTracking.get(project);
    if (trackingInfo != null && trackingInfo.timeStamp == timeStamp) {
      return trackingInfo.packageName;
    }
    // parse
    final String[] packageName = new String[1];
    QParser.parse(new InputStreamReader(manifest.getContents()), new QHandlerAdapter() {
      @Override
      public void startElement(int offset,
          int length,
          String tag,
          Map<String, String> attributes,
          List<QAttribute> attrList,
          boolean closed) throws Exception {
        if ("manifest".equalsIgnoreCase(tag)) {
          packageName[0] = attributes.get("package");
        }
      }
    });
    m_manifestTracking.put(project, new ManifestTrackingData(timeStamp, packageName[0]));
    return packageName[0];
  }

  /**
   * @return the namespace for component class: for android sdk class it is
   *         "http://schemas.android.com/apk/res/android" and for custom classes it is
   *         "http://schemas.android.com/apk/res/%package_name%".
   */
  public static String getNamespace(Class<?> componentClass, IProject project) {
    boolean frameworkClass = isFrameworkClass(componentClass);
    return frameworkClass ? SdkConstants.NS_RESOURCES : getCustomNamespace(project);
  }

  /**
   * @return the namespace for custom attributes, by appending package name from AndroidManifest.
   */
  public static String getCustomNamespace(IProject project) {
    try {
      return String.format(NS_CUSTOM_RESOURCES, getPackageFromManifest(project));
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  /**
   * @return <code>true</code> if given class is the instance of
   *         'android.view.ViewGroup$LayoutParams'.
   */
  public static boolean isLayoutParamClass(Class<?> componentClass) {
    return ReflectionUtils.isSuccessorOf(componentClass, "android.view.ViewGroup$LayoutParams");
  }

  /**
   * @return <code>true</code> if given class is the framework class, i.e. package starts with
   *         'android'.
   */
  public static boolean isFrameworkClass(Class<?> componentClass) {
    String className = componentClass.getName();
    return className.startsWith("android.");
  }
}
