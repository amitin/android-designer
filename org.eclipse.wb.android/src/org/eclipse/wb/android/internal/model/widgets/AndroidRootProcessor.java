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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.android.internal.support.AndroidUtils;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.EditorActivatedListener;
import org.eclipse.wb.core.model.broadcast.EditorActivatedRequest;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.xml.model.IRootProcessor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;


import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * The root processor for gathering dependency infos to be able to reparse UI if any dependency
 * changed.
 * 
 * @author mitin_aa
 * @coverage android.model
 */
public final class AndroidRootProcessor implements IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new AndroidRootProcessor();

  private AndroidRootProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(XmlObjectInfo root) throws Exception {
    if (root instanceof ViewInfo) {
      setupDependencyChecks((ViewInfo) root);
    }
  }

  /**
   * 
   */
  private void setupDependencyChecks(final ViewInfo object) throws Exception {
    final Map<IResource, Long> dependencies = Maps.newHashMap();
    addResources(object, dependencies);
    addJavaDependencies(object, dependencies);
    object.addBroadcastListener(new EditorActivatedListener() {
      public void invoke(EditorActivatedRequest request) throws Exception {
        if (hasModifiedResource()) {
          request.requestReparse();
        }
      }

      private boolean hasModifiedResource() {
        synchronized (dependencies) {
          for (Entry<IResource, Long> entry : dependencies.entrySet()) {
            IResource resource = entry.getKey();
            if (resource.getModificationStamp() != entry.getValue()) {
              return true;
            }
          }
          return false;
        }
      }
    });
  }

  /**
   * Adds files for modification checking, ex., "attrs.xml"
   */
  private static void addResources(ViewInfo object, Map<IResource, Long> resourceStamps) {
    {
      // add attrs.xml
      IProject project = object.getContext().getJavaProject().getProject();
      IFile file = project.getFile("res/values/attrs.xml");
      if (file != null && file.isAccessible()) {
        resourceStamps.put(file, file.getModificationStamp());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Java classes dependencies
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Remembers dependency information for given java type, i.e. time stamps for referenced types.
   */
  private static void addJavaDependencies(XmlObjectInfo xmlObjectInfo,
      final Map<IResource, Long> dependencies) throws Exception {
    final Set<Class<?>> found = Sets.newHashSet();
    final IJavaProject javaProject = xmlObjectInfo.getContext().getJavaProject();
    // traverse xml hierarchy and store non-framework types used.
    xmlObjectInfo.accept(new ObjectInfoVisitor() {
      @Override
      public void endVisit(ObjectInfo objectInfo) throws Exception {
        if (objectInfo instanceof ViewInfo) {
          ViewInfo viewInfo = (ViewInfo) objectInfo;
          Class<?> componentClass = viewInfo.getDescription().getComponentClass();
          if (!AndroidUtils.isFrameworkClass(componentClass)) {
            found.add(componentClass);
          }
        }
        super.endVisit(objectInfo);
      }
    });
    // schedule thread gathering dependencies.
    if (!found.isEmpty()) {
      new Thread("WindowBuilder dependency search") {
        @Override
        public void run() {
          ExecutionUtils.runIgnore(new RunnableEx() {
            public void run() throws Exception {
              synchronized (dependencies) {
                TreeSet<String> checkedTypes = Sets.<String>newTreeSet();
                for (Class<?> componentClass : found) {
                  String typeName = componentClass.getName();
                  IType type = javaProject.findType(typeName);
                  addDependencies(dependencies, checkedTypes, type.getCompilationUnit(), 0);
                }
              }
            }
          });
        }
      }.start();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copied from JIU
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds dependencies for given {@link ICompilationUnit}.
   */
  private static void addDependencies(final Map<IResource, Long> dependencies,
      final Set<String> checkedTypes,
      final ICompilationUnit modelUnit,
      final int level) throws Exception {
    if (level < 5 && dependencies.size() < 100) {
      final IJavaProject javaProject = modelUnit.getJavaProject();
      // add current resource
      {
        IResource resource = modelUnit.getResource();
        dependencies.put(resource, resource.getModificationStamp());
      }
      // add references
      CompilationUnit astUnit = CodeUtils.parseCompilationUnit(modelUnit);
      astUnit.accept(new ASTVisitor() {
        @Override
        public void endVisit(QualifiedName node) {
          addNewType(node.resolveTypeBinding());
        }

        @Override
        public void endVisit(SimpleName node) {
          addNewType(node.resolveTypeBinding());
        }

        private void addNewType(final ITypeBinding binding) {
          if (binding == null) {
            return;
          }
          ExecutionUtils.runIgnore(new RunnableEx() {
            public void run() throws Exception {
              String typeName = AstNodeUtils.getFullyQualifiedName(binding, false);
              if (typeName.indexOf('.') != -1 && !checkedTypes.contains(typeName)) {
                checkedTypes.add(typeName);
                IType type = javaProject.findType(typeName);
                if (type != null && !type.isBinary()) {
                  addDependencies(dependencies, checkedTypes, type.getCompilationUnit(), level + 1);
                }
              }
            }
          });
        }
      });
    }
  }
}
