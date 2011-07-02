/*******************************************************************************
 * Copyright (c) 2011 Andrey Sablin (Sablin.Andrey@gmail.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrey Sablin (Sablin.Andrey@gmail.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.android.internal.refactoring;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.refactoring.RefactoringUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentAttribute;
import org.eclipse.wb.internal.core.utils.xml.DocumentModelVisitor;
import org.eclipse.wb.internal.core.utils.xml.FileDocumentEditContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

import com.android.ide.common.layout.LayoutConstants;

import org.apache.commons.io.FilenameUtils;

import java.util.List;

/**
 * Participates in rename UI resources.
 * 
 * @author sablin_aa
 * @coverage android.refactoring
 */
@SuppressWarnings("restriction")
public class IncludeRenameParticipant extends RenameParticipant {
  private IFile file;

  ////////////////////////////////////////////////////////////////////////////
  //
  // RefactoringParticipant
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getName() {
    return "Android UI resource rename participant";
  }

  @Override
  protected boolean initialize(Object element) {
    Assert.isTrue(
        element instanceof IFile,
        "Only IFile can be renamed, but {0} received. Check participant enablement filters.",
        element);
    file = (IFile) element;
    if ("xml".equalsIgnoreCase(file.getFileExtension())) {
      IFolder folder = (IFolder) file.getParent();
      String resLayoutFolderName = "/" + folder.getProject().getName() + "/res/layout";
      return folder.getFullPath().toPortableString().startsWith(resLayoutFolderName);
    }
    return false;
  }

  @Override
  public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) {
    return new RefactoringStatus();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Change
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final Change createChange(final IProgressMonitor pm) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Change>() {
      public Change runObject() throws Exception {
        return createChangeEx(pm);
      }
    }, null);
  }

  /**
   * Implementation of {@link #createChange(IProgressMonitor)} that can throw {@link Exception}.
   */
  public Change createChangeEx(IProgressMonitor pm) throws Exception {
    RenameArguments renameArguments = getArguments();
    if (renameArguments.getUpdateReferences()) {
      List<IFile> dependsFiles = Lists.newArrayList();
      // collect dependences
      IFolder folder = (IFolder) file.getParent();
      for (IResource resource : folder.members()) {
        if (!resource.equals(file) && resource instanceof IFile) {
          IFile dependsFile = (IFile) resource;
          if ("xml".equalsIgnoreCase(dependsFile.getFileExtension())) {
            dependsFiles.add(dependsFile);
          }
        }
      }
      final String oldValue =
          LayoutConstants.LAYOUT_PREFIX + FilenameUtils.getBaseName(file.getName());
      final String newValue =
          LayoutConstants.LAYOUT_PREFIX + FilenameUtils.getBaseName(renameArguments.getNewName());
      if (!dependsFiles.isEmpty()) {
        // create complex Change object
        CompositeChange change = new CompositeChange("IncludeRenameParticipant.change");
        for (IFile dependsFile : dependsFiles) {
          change.add(RefactoringUtils.modifyXML(dependsFile, new DocumentModelVisitor() {
            @Override
            public void visit(DocumentAttribute attribute) {
              if ("include".equalsIgnoreCase(attribute.getEnclosingElement().getTag())
                  && "layout".equalsIgnoreCase(attribute.getName())
                  && oldValue.equalsIgnoreCase(attribute.getValue())) {
                attribute.setValue(newValue);
              }
            }
          },
              new FileDocumentEditContext(dependsFile)));
        }
        return change;
      }
    }
    return null;
  }
}
