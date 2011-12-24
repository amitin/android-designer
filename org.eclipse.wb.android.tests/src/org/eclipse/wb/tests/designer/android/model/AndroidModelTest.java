/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.android.model;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.wb.android.internal.support.AndroidBridge;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.model.variable.NamesManager.ComponentNameDescription;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.XML.model.AbstractXmlModelTest;
import org.eclipse.wb.tests.designer.core.TestProject;

import com.android.ide.eclipse.adt.internal.sdk.Sdk;
import com.android.ide.eclipse.adt.internal.wizards.newproject.NewProjectCreator;
import com.android.ide.eclipse.adt.internal.wizards.newproject.NewProjectWizardState;
import com.android.ide.eclipse.adt.internal.wizards.newproject.NewProjectWizardState.Mode;
import com.android.sdklib.IAndroidTarget;
import com.google.common.collect.ImmutableList;

/**
 * Abstract super class for Android tests.
 * 
 * @author sablin_aa
 */
public abstract class AndroidModelTest extends AbstractXmlModelTest {
	protected boolean m_getSource_includeStandardNamespaces;
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		//configureForTestPreferences(RcpToolkitDescription.INSTANCE);
		m_getSource_includeStandardNamespaces = true;
	}
	@Override
	protected void tearDown() throws Exception {
		//configureDefaultPreferences(RcpToolkitDescription.INSTANCE);
		super.tearDown();
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Project operations
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@SuppressWarnings("restriction")
	public void do_projectCreate() throws Exception {
		if (m_testProject == null) {
			m_project = ResourcesPlugin.getWorkspace().getRoot().getProject("TestProject");
			{
				// create Android project by ADT wizard creator
				NewProjectWizardState projectValues = new NewProjectWizardState(Mode.ANY);
				{
					projectValues.useExisting = false;
					projectValues.projectName = m_project.getName();
					projectValues.projectNameModifiedByUser = true;
					{
						String locationString =
								ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString()
									+ m_project.getFullPath().toOSString();
						projectValues.projectLocation = new File(locationString);
					}
					projectValues.applicationName = projectValues.projectName;
					projectValues.packageName = "wb.test";
					projectValues.packageNameModifiedByUser = true;
					projectValues.activityName = projectValues.projectName + "Activity";
					projectValues.createActivity = true;
					projectValues.target = getTarget();
					{
						String minSdk = getMinSdk();
						if (minSdk != null) {
							projectValues.minSdk = minSdk;
							projectValues.minSdkModifiedByUser = true;
						}
					}
				}
				IRunnableContext context = new IRunnableContext() {
					@Override
					public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable)
							throws InvocationTargetException, InterruptedException {
						runnable.run(new NullProgressMonitor());
					}
				};
				NewProjectCreator creator = new NewProjectCreator(projectValues, context);
				assertTrue(creator.createAndroidProjects());
			}
			m_testProject = new TestProject(m_project);
			m_javaProject = m_testProject.getJavaProject();
		}
	}
	/**
	 * Configures created project.
	 */
	@Override
	protected void configureNewProject() throws Exception {
		/*BTestUtils.configure(m_testProject);
		m_testProject.addPlugin("com.ibm.icu");
		m_testProject.addPlugin("org.eclipse.core.databinding");
		m_testProject.addPlugin("org.eclipse.core.databinding.beans");
		m_testProject.addPlugin("org.eclipse.core.databinding.observable");
		m_testProject.addPlugin("org.eclipse.core.databinding.property");
		m_testProject.addPlugin("org.eclipse.jface.databinding");
		m_testProject.addBundleJars("org.eclipse.wb.xwt", "lib");*/
	}
	@SuppressWarnings("restriction")
	protected IAndroidTarget getTarget() {
		IAndroidTarget usedTarget = null;
		Sdk sdk = AndroidBridge.getSdk();
		for (IAndroidTarget target : sdk.getTargets()) {
			if (usedTarget == null) {
				usedTarget = target;
			} else if (usedTarget.getVersion().getApiLevel() < target.getVersion().getApiLevel()) {
				usedTarget = target;
			}
		}
		return usedTarget;
	}
	protected String getMinSdk() {
		return null;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Preferences
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Configures test values for toolkit preferences.
	 */
	protected void configureForTestPreferences(ToolkitDescription toolkit) {
	}
	/**
	 * Configures default values for toolkit preferences.
	 */
	protected void configureDefaultPreferences(ToolkitDescription toolkit) {
		//IPreferenceStore preferences = toolkit.getPreferences();
		//preferences.setToDefault(org.eclipse.wb.internal.swt.preferences.IPreferenceConstants.P_LAYOUT_DATA_NAME_TEMPLATE);
		//preferences.setToDefault(org.eclipse.wb.internal.swt.preferences.IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE);
		NamesManager.setNameDescriptions(toolkit, ImmutableList.<ComponentNameDescription>of());
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Java source
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getJavaSourceToAssert() {
		return getFileContentSrc("test/Test.java");
	}
	@Override
	protected String[] getJavaSource_decorate(String... lines) {
		lines = CodeUtils.join(new String[]{"package test;"/*,
															"import org.eclipse.swt.SWT;",
															"import org.eclipse.swt.events.*;",
															"import org.eclipse.swt.graphics.*;",
															"import org.eclipse.swt.widgets.*;",
															"import org.eclipse.swt.layout.*;",
															"import org.eclipse.swt.custom.*;",
															"import org.eclipse.jface.layout.*;",
															"import org.eclipse.jface.viewers.*;"*/}, lines);
		return lines;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Parsing and source
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link XmlObjectInfo} for parsed XWT source, in "src/test/Text.xwt" file.
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected final <T extends XmlObjectInfo> T parse(String... lines) throws Exception {
		String source = getTestSource(lines);
		return (T) parse0("test/Test.xwt", source);
	}
	/**
	 * Parses XWT file with given path and content.
	 */
	protected final XmlObjectInfo parse0(String path, String content) throws Exception {
		/*IFile file = setFileContentSrc(path, content);
		IDocument document = new Document(content);
		XwtParser parser = new XwtParser(file, document);
		m_lastObject = parser.parse();
		m_lastContext = m_lastObject.getContext();
		m_lastLoader = m_lastContext.getClassLoader();*/
		return m_lastObject;
	}
	@Override
	protected String getTestSource_namespaces() {
		/*if (m_getSource_includeStandardNamespaces) {
		  return " xmlns:wbp='http://www.eclipse.org/wb/xwt'"
		      + " xmlns:t='clr-namespace:test'"
		      + " xmlns='http://www.eclipse.org/xwt/presentation'"
		      + " xmlns:x='http://www.eclipse.org/xwt'";
		} else */{
			return StringUtils.EMPTY;
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link ViewInfo} for {@link Button} with text.
	 */
	/*protected final ControlInfo createButtonWithText() throws Exception {
	  String componentClassName = "org.eclipse.swt.widgets.Button";
	  return createObject(componentClassName);
	}*/
	/**
	 * @return {@link ViewInfo} for {@link Button} without text.
	 */
	/*protected final ControlInfo createButton() throws Exception {
	  String componentClassName = "org.eclipse.swt.widgets.Button";
	  return createObject(componentClassName, "empty");
	}*/
	////////////////////////////////////////////////////////////////////////////
	//
	// test.MyComponent support
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Prepares empty <code>test.MyComponent</code> class with additional lines in type body.
	 */
	/*protected final void prepareMyComponent(String... lines) throws Exception {
	  prepareMyComponent(lines, ArrayUtils.EMPTY_STRING_ARRAY);
	}*/
	/**
	 * Prepares empty <code>test.MyComponent</code> class with additional lines in type body, and with special
	 * <code>wbp-component.xml</code> description.
	 */
	/*protected final void prepareMyComponent(String[] javaLines, String[] descriptionLines)
	    throws Exception {
	  // java
	  {
	    String[] lines =
	        new String[]{
	            "package test;",
	            "import org.eclipse.swt.SWT;",
	            "import org.eclipse.swt.widgets.*;",
	            "public class MyComponent extends Composite {",
	            "  public MyComponent(Composite parent, int style) {",
	            "    super(parent, style);",
	            "  }"};
	    lines = CodeUtils.join(lines, javaLines);
	    lines = CodeUtils.join(lines, new String[]{"}"});
	    setFileContentSrc("test/MyComponent.java", getSourceDQ(lines));
	  }
	  // description
	  {
	    String[] lines =
	        new String[]{
	            "<?xml version='1.0' encoding='UTF-8'?>",
	            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>"};
	    descriptionLines = removeFillerLines(descriptionLines);
	    lines = CodeUtils.join(lines, descriptionLines);
	    lines = CodeUtils.join(lines, new String[]{"</component>"});
	    setFileContentSrc("test/MyComponent.wbp-component.xml", getSourceDQ(lines));
	  }
	  waitForAutoBuild();
	}

	protected final ComponentDescription getMyDescription() throws Exception {
	  return getDescription("test.MyComponent");
	}

	protected final ComponentDescription getDescription(String componentClassName) throws Exception {
	  if (m_lastContext == null) {
	    parse("<Shell/>");
	  }
	  return ComponentDescriptionHelper.getDescription(m_lastContext, componentClassName);
	}*/
}