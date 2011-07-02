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
package org.eclipse.wb.android.internal.editor;

import org.eclipse.wb.android.internal.support.AndroidUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.xml.editor.actions.IPairResourceProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;


import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * {@link IPairResourceProvider} for Android Designer.
 * 
 * @author mitin_aa
 * @coverage android.editor
 */
public final class AndroidPairResourceProvider implements IPairResourceProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IPairResourceProvider INSTANCE = new AndroidPairResourceProvider();

  private AndroidPairResourceProvider() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPairResourceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public IFile getPair(final IFile file) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<IFile>() {
      public IFile runObject() throws Exception {
        if (file.getFileExtension().equalsIgnoreCase("xml")) {
          return getJavaFile(file);
        }
        if (file.getFileExtension().equalsIgnoreCase("java")) {
          return getLayoutFile(file);
        }
        return null;
      }
    }, null);
  }

  /**
   * @return the companion xml layout file used by this Activity.
   */
  protected IFile getLayoutFile(IFile file) {
    return null;
  }

  /**
   * @return the Activity file which uses given xml layout.
   */
  protected IFile getJavaFile(IFile layoutFile) throws Exception {
    IProject project = layoutFile.getProject();
    String packageName = AndroidUtils.getPackageFromManifest(project);
    if (packageName == null) {
      return null;
    }
    IJavaProject javaProject = JavaCore.create(project);
    IType rType = javaProject.findType(packageName, "R.layout");
    String layoutName =
        StringUtils.removeEnd(layoutFile.getName(), "." + layoutFile.getFileExtension());
    IField field = rType.getField(layoutName);
    List<IJavaElement> references = CodeUtils.searchReferences(field);
    for (IJavaElement element : references) {
      // TODO: ask the user if found more than one?
      return (IFile) element.getUnderlyingResource();
    }
    return null;
  }
}
