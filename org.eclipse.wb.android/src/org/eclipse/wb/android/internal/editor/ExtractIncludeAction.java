/*******************************************************************************
 * Copyright (c) 2011 Alexander Mitin (Alexander.Mitin@gmail.com)
 * Copyright (c) 2011 Andrey Sablin (Sablin.Andrey@gmail.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Mitin (Alexander.Mitin@gmail.com) - initial API and implementation
 *    Andrey Sablin (Sablin.Andrey@gmail.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.android.internal.editor;

import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.utils.xml.FileDocumentEditContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.TagCreationSupport;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

import com.android.ide.common.layout.LayoutConstants;
import com.android.ide.eclipse.adt.internal.resources.manager.ProjectResources;
import com.android.resources.ResourceType;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Action for extracting UI.
 * 
 * @author sablin_aa
 * @coverage android.editor
 */
@SuppressWarnings("restriction")
public final class ExtractIncludeAction extends ObjectInfoAction {
  private final ViewInfo viewInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExtractIncludeAction(ViewInfo viewInfo) {
    super(viewInfo.getParent(), "Extract include...");
    setToolTipText("Extract selected UI fragment to separate UI");
    // FIXME setImageDescriptor(DesignerPlugin.getImageDescriptor("test.png"));
    this.viewInfo = viewInfo;
  }

  @Override
  protected void runEx() throws Exception {
    String newUiName;
    IFile newUiFile;
    // create new UI file
    {
      IFile file = viewInfo.getAndroidContext().getFile();
      final IFolder folder = (IFolder) file.getParent();
      newUiName = FilenameUtils.getBaseName(file.getName()) + "_internal";
      // wizard...
      InputDialog inputDialog =
          new InputDialog(DesignerPlugin.getShell(),
              "Extract as Include",
              "New UI file name",
              newUiName,
              new IInputValidator() {
                public String isValid(String newText) {
                  if (StringUtils.isEmpty(newText)) {
                    return "Enter valid file name.";
                  }
                  if (folder.getFile(newText + ".xml").exists()) {
                    return "UI file " + newText + " already exists.";
                  }
                  return null;
                }
              });
      if (inputDialog.open() == Window.OK) {
        newUiName = inputDialog.getValue();
      } else {
        // "Cancel" pressed
        return;
      }
      newUiFile = folder.getFile(newUiName + ".xml");
    }
    // write contents to new UI file
    DocumentElement documentElement = viewInfo.getElement();
    {
      // prepare contents
      StringWriter stringWriter = new StringWriter();
      documentElement.write(new PrintWriter(stringWriter), "");
      // write to file
      IOUtils2.setFileContents(newUiFile, "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
          + stringWriter.toString());
      // some corrects
      {
        final String nsAttributeName = "xmlns:android";
        String nsAttributeValue = documentElement.getRoot().getAttribute(nsAttributeName);
        if (!StringUtils.isEmpty(nsAttributeValue)) {
          FileDocumentEditContext context = new FileDocumentEditContext(newUiFile);
          DocumentElement rootElement = context.getRoot();
          rootElement.setAttribute(nsAttributeName, nsAttributeValue);
          context.commit();
          context.disconnect();
        }
      }
      // wait for new UI resource loaded ...
      {
        // FIXME must used ProjectResources for current FolderConfiguration
        final String layoutName = newUiName;
        RunnableObjectEx<Boolean> runnableObjectEx = new RunnableObjectEx<Boolean>() {
          public Boolean runObject() throws Exception {
            ProjectResources projectResources = viewInfo.getAndroidBridge().getProjectResources();
            Integer resourceValue = projectResources.getResourceId(ResourceType.LAYOUT, layoutName);
            return resourceValue == null;
          }
        };
        try {
          long timeout = 5 * 1000;
          long startWait = System.currentTimeMillis();
          while (runnableObjectEx.runObject()) {
            ExecutionUtils.sleep(10);
            if (System.currentTimeMillis() - startWait > timeout) {
              break;
            }
          }
        } catch (Throwable e) {
          // shouldn't happen, but spit anyway
          ReflectionUtils.propagate(e);
        }
      }
    }
    // replace original UI-fragment to "include" directive
    {
      String androidNsPrefix = getAndroidNamespacePrefix(documentElement.getRoot());
      Class<?> componentClass = viewInfo.getDescription().getComponentClass();
      ViewInfo parentInfo = (ViewInfo) viewInfo.getParent();
      XmlObjectInfo nextComponent =
          GenericsUtils.getNextOrNull(parentInfo.getChildrenXML(), viewInfo);
      String layoutAttributeValue =
          LayoutConstants.LAYOUT_PREFIX + FilenameUtils.getBaseName(newUiFile.getName());
      String layoutWidthAttributeValue =
          documentElement.getAttribute(androidNsPrefix + LayoutConstants.ATTR_LAYOUT_WIDTH);
      String layoutHeightAttributeValue =
          documentElement.getAttribute(androidNsPrefix + LayoutConstants.ATTR_LAYOUT_HEIGHT);
      // remove original view
      viewInfo.delete();
      // create "include"
      CreationSupport creationSupport = new TagCreationSupport("include");
      XmlObjectInfo newViewInfo =
          XmlObjectUtils.createObject(parentInfo.getContext(), componentClass, creationSupport);
      XmlObjectUtils.add(newViewInfo, Associations.direct(), parentInfo, nextComponent);
      // set element attributes
      {
        DocumentElement newDocumentElement = newViewInfo.getElement();
        newDocumentElement.setAttribute("layout", layoutAttributeValue);
        newDocumentElement.setAttribute(
            "android:" + LayoutConstants.ATTR_LAYOUT_WIDTH,
            layoutWidthAttributeValue);
        newDocumentElement.setAttribute(
            "android:" + LayoutConstants.ATTR_LAYOUT_HEIGHT,
            layoutHeightAttributeValue);
      }
    }
  }

  private static String getAndroidNamespacePrefix(DocumentElement rootElement) {
    // TODO see com.android.ide.eclipse.adt.internal.editors.layout.refactoring.VisualRefactoring.getAndroidNamespacePrefix(Document)
    return "android:";
  }
}