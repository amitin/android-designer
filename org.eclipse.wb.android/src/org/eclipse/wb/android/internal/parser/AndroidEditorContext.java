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

import org.eclipse.wb.android.internal.AndroidToolkitDescription;
import org.eclipse.wb.android.internal.support.AndroidBridge;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProviderFactory;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.CompositeClassLoader;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.ILiveEditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;


import org.apache.commons.lang.NotImplementedException;

import java.util.List;

/**
 * {@link EditorContext} for Android.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage android.parser
 */
public final class AndroidEditorContext extends EditorContext {
  private AndroidBridge m_androidBridge;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AndroidEditorContext(IFile file, IDocument document) throws Exception {
    super(AndroidToolkitDescription.INSTANCE, file, document);
    configureDescriptionVersionsProviders();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize/dispose
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void initialize() throws Exception {
    super.initialize();
    m_androidBridge = new AndroidBridge(this);
  }

  @Override
  public void dispose() throws Exception {
    m_androidBridge.dispose();
    super.dispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the androidBridge
   */
  public AndroidBridge getAndroidBridge() {
    return m_androidBridge;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassLoader
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addParentClassLoaders(final CompositeClassLoader parentClassLoader)
      throws Exception {
    super.addParentClassLoaders(parentClassLoader);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Live support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ILiveEditorContext getLiveContext() {
    return m_liveEditorContext;
  }

  private final ILiveEditorContext m_liveEditorContext = new ILiveEditorContext() {
    public XmlObjectInfo parse(String[] sourceLines) throws Exception {
      throw new NotImplementedException();
    }

    public void dispose() throws Exception {
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDescriptionVersionsProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Installs {@link IDescriptionVersionsProvider}'s.
   */
  private void configureDescriptionVersionsProviders() throws Exception {
    List<IDescriptionVersionsProviderFactory> factories =
        ExternalFactoriesHelper.getElementsInstances(
            IDescriptionVersionsProviderFactory.class,
            "org.eclipse.wb.core.descriptionVersionsProviderFactories",
            "factory");
    for (IDescriptionVersionsProviderFactory factory : factories) {
      // versions
      addVersions(factory.getVersions(m_javaProject, m_classLoader));
      // version providers
      {
        IDescriptionVersionsProvider provider = factory.getProvider(m_javaProject, m_classLoader);
        if (provider != null) {
          addDescriptionVersionsProvider(provider);
        }
      }
    }
  }
}
