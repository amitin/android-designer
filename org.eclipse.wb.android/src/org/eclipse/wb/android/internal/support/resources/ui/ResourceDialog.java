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
package org.eclipse.wb.android.internal.support.resources.ui;

import com.google.common.collect.Sets;

import org.eclipse.wb.android.internal.parser.AndroidEditorContext;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.android.ide.common.resources.ResourceItem;
import com.android.ide.common.resources.ResourceRepository;
import com.android.ide.eclipse.adt.internal.resources.manager.ResourceManager;
import com.android.ide.eclipse.adt.internal.sdk.AndroidTargetData;
import com.android.resources.ResourceType;

import org.apache.commons.lang.StringUtils;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The dialog allowing to choose any resource reference into a Reference property.
 * 
 * @author mitin_aa
 * @coverage android.support.resources
 */
@SuppressWarnings("restriction")
public final class ResourceDialog extends ResizableDialog {
  private static final String ANDROID_PKG = "android";
  // fields
  private TreeViewer m_resourcesTreeViewer;
  private ResourceType m_primaryResourceType;
  private final AndroidEditorContext m_context;
  private final ResourceViewerFilter m_filter = new ResourceViewerFilter();
  private final Set<ResourceType> m_resourceTypeSet = Sets.newHashSet();
  private boolean m_isFrameworkResource;
  private String m_referenceValue;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ResourceDialog(Shell parentShell, AbstractUIPlugin plugin, AndroidEditorContext context) {
    super(parentShell, plugin);
    m_context = context;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void create() {
    setupDialog();
    super.create();
  }

  /**
   * Create contents of the dialog.
   * 
   * @param parent
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    GridLayoutFactory.create(container).columns(2);
    // resource source 
    createSourceGroup(container);
    // resources tree
    createResourceTreeGroup(container);
    // resource types
    createResourceTypesGroup(container);
    // populate
    populateResourceViewer();
    return container;
  }

  private void setupDialog() {
    if (StringUtils.isEmpty(m_referenceValue)) {
      // use sdk resources
      m_isFrameworkResource = true;
    } else {
      boolean matches = m_referenceValue.regionMatches(1, ANDROID_PKG, 0, ANDROID_PKG.length());
      if (matches) {
        m_isFrameworkResource = true;
      }
    }
    // setup type
    if (!StringUtils.isEmpty(m_referenceValue)) {
      String typeName = null;
      Matcher m = Pattern.compile(".*?([a-z]+)/.*").matcher(m_referenceValue);
      if (m.matches()) {
        typeName = m.group(1);
      }
      if (typeName != null) {
        ResourceType resType = ResourceType.getEnum(typeName);
        if (resType != null) {
          m_primaryResourceType = resType;
        }
      }
    }
  }

  private void populateResourceViewer() {
    ResourceRepository repository;
    // get the resource repository for this project and the system resources.
    if (m_isFrameworkResource) {
      // get the Target Data to get the system resources
      AndroidTargetData data = m_context.getAndroidBridge().getTargetData();
      repository = data.getFrameworkResources();
    } else {
      // open a resource chooser dialog for specified resource type.
      repository =
          ResourceManager.getInstance().getProjectResources(m_context.getJavaProject().getProject());
    }
    m_filter.setRepository(repository);
    m_filter.setResourceTypes(m_resourceTypeSet);
    m_resourcesTreeViewer.setInput(repository);
  }

  private void createResourceTreeGroup(Composite container) {
    {
      Group grpResources = new Group(container, SWT.NONE);
      GridLayoutFactory.create(grpResources);
      GridDataFactory.create(grpResources).fill().spanV(2).grabH();
      grpResources.setText("Resources:");
      Composite composite = new Composite(grpResources, SWT.NONE);
      GridLayoutFactory.create(composite).noMargins().noSpacing().columns(2);
      GridDataFactory.create(composite).fill().grabH();
      //
      Label lblNewLabel = new Label(composite, SWT.NONE);
      GridDataFactory.create(lblNewLabel).alignHR().alignVM();
      lblNewLabel.setText("Filter:");
      // search text
      final Text filterText = new Text(composite, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
      GridDataFactory.create(filterText).alignHC().grabH().fill();
      filterText.addListener(SWT.FocusIn, new Listener() {
        public void handleEvent(Event event) {
          filterText.selectAll();
        }
      });
      filterText.addModifyListener(new ModifyListener() {
        private Timer timer;

        public void modifyText(ModifyEvent e) {
          final String filterString = filterText.getText();
          m_filter.setResourcePrefix(filterString);
          // schedule update
          if (timer != null) {
            timer.cancel();
          }
          timer = new Timer();
          timer.schedule(new TimerTask() {
            @Override
            public void run() {
              DesignerPlugin.getStandardDisplay().asyncExec(new Runnable() {
                public void run() {
                  m_resourcesTreeViewer.refresh();
                  if (!StringUtils.isEmpty(filterString)) {
                    m_resourcesTreeViewer.expandAll();
                  }
                }
              });
            }
          }, 250);
        }
      });
      filterText.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
          if (e.detail == SWT.ICON_CANCEL) {
            filterText.setText("");
          }
        }
      });
      filterText.addKeyListener(new KeyListener() {
        public void keyReleased(KeyEvent e) {
          if (e.keyCode == SWT.ARROW_DOWN) {
            m_resourcesTreeViewer.getTree().setFocus();
          }
        }

        public void keyPressed(KeyEvent e) {
        }
      });
      if (!StringUtils.isEmpty(m_referenceValue)) {
        String resourceName = StringUtils.substringAfterLast(m_referenceValue, "/");
        filterText.setText(resourceName);
      }
      // resource tree
      m_resourcesTreeViewer = new TreeViewer(grpResources, SWT.BORDER | SWT.SINGLE);
      Tree tree = m_resourcesTreeViewer.getTree();
      GridDataFactory.create(tree).fill().grab().hintHC(40).hintVC(20);
      // all listeners
      m_resourcesTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
          m_referenceValue =
              getReferenceValue((TreeSelection) m_resourcesTreeViewer.getSelection());
          // update OK button
          getButton(IDialogConstants.OK_ID).setEnabled(m_referenceValue != null);
        }
      });
      m_resourcesTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
        public void doubleClick(DoubleClickEvent event) {
          if (m_referenceValue != null) {
            okPressed();
          }
        }
      });
      // setup
      m_resourcesTreeViewer.setContentProvider(new ResourceContentProvider());
      m_resourcesTreeViewer.setLabelProvider(new ResourceLabelProvider());
      m_resourcesTreeViewer.addFilter(m_filter);
      // set focus to filter
      filterText.setFocus();
    }
  }

  private void createSourceGroup(Composite container) {
    Group grpSource = new Group(container, SWT.NONE);
    GridLayoutFactory.create(grpSource);
    GridDataFactory.create(grpSource).fill();
    grpSource.setText("Source:");
    {
      Button btnProject = new Button(grpSource, SWT.RADIO);
      btnProject.setText("Project");
      btnProject.setSelection(!m_isFrameworkResource);
      btnProject.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          m_isFrameworkResource = false;
          populateResourceViewer();
        }
      });
    }
    {
      Button btnFramework = new Button(grpSource, SWT.RADIO);
      btnFramework.setText("Framework");
      btnFramework.setSelection(m_isFrameworkResource);
      btnFramework.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          m_isFrameworkResource = true;
          populateResourceViewer();
        }
      });
    }
  }

  private void createResourceTypesGroup(Composite container) {
    Group grpTypes = new Group(container, SWT.NONE);
    GridLayoutFactory.create(grpTypes);
    GridDataFactory.create(grpTypes).fill().grabV();
    grpTypes.setText("Types:");
    // fill
    String[] names = ResourceType.getNames();
    for (String name : names) {
      final Button resourceNameButton = new Button(grpTypes, SWT.CHECK);
      ResourceType resourceType = ResourceType.getEnum(name);
      resourceNameButton.setText(resourceType.getDisplayName());
      resourceNameButton.setData(resourceType);
      // set selection: select all if no primary type passed.
      if (resourceType.equals(m_primaryResourceType) || m_primaryResourceType == null) {
        resourceNameButton.setSelection(true);
        m_resourceTypeSet.add(resourceType);
      }
      // add listeners managing resource type filter
      resourceNameButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          boolean selection = resourceNameButton.getSelection();
          ResourceType resourceType = (ResourceType) resourceNameButton.getData();
          if (selection) {
            m_resourceTypeSet.add(resourceType);
          } else {
            m_resourceTypeSet.remove(resourceType);
          }
          m_resourcesTreeViewer.refresh();
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the resource type which is selected by default.
   */
  public void setPrimaryResourceType(ResourceType resourceType) {
    m_primaryResourceType = resourceType;
  }

  /**
   * @param value
   *          the current property value.
   */
  public void setReference(String value) {
    m_referenceValue = value;
  }

  private String getReferenceValue(TreeSelection selection) {
    TreePath treePath = getTreePathSelected(selection);
    if (treePath != null) {
      ResourceType resourceType = (ResourceType) treePath.getFirstSegment();
      ResourceItem resourceItem = (ResourceItem) treePath.getLastSegment();
      return getReferenceValue(resourceType, resourceItem);
    }
    return null;
  }

  private TreePath getTreePathSelected(TreeSelection selection) {
    if (selection != null) {
      TreePath[] treePaths = selection.getPaths();
      if (treePaths.length > 0 && treePaths[0] != null) {
        if (treePaths[0].getSegmentCount() == 2) {
          return treePaths[0];
        }
      }
    }
    return null;
  }

  private String getReferenceValue(ResourceType resourceType, ResourceItem resourceItem) {
    return (m_isFrameworkResource ? "@android:" : "@")
        + resourceType.getName()
        + "/"
        + resourceItem.getName();
  }

  /**
   * @return resulting value.
   */
  public String getReferenceValue() {
    return m_referenceValue;
  }
}
