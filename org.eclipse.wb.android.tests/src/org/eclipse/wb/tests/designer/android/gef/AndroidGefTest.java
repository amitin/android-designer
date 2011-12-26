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
package org.eclipse.wb.tests.designer.android.gef;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wb.android.internal.Activator;
import org.eclipse.wb.android.internal.editor.AndroidEditor;
import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.android.internal.preferences.IPreferenceConstants;
import org.eclipse.wb.android.internal.support.DeviceManager;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.xml.editor.AbstractXmlEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.XML.editor.AbstractXmlGefTest;
import org.eclipse.wb.tests.designer.android.tests.AndroidProjectUtils;
import org.eclipse.wb.tests.designer.core.TestProject;

import com.android.sdklib.IAndroidTarget;

/**
 * Abstract super class for Android GEF tests.
 * 
 * @author sablin_aa
 */
public abstract class AndroidGefTest extends AbstractXmlGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		configureForTestPreferences(Activator.getToolkit().getPreferences());
	}
	@Override
	protected void tearDown() throws Exception {
		configureDefaultPreferences(Activator.getToolkit().getPreferences());
		super.tearDown();
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Project operations
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void do_projectCreate() throws Exception {
		if (m_testProject == null) {
			m_project = ResourcesPlugin.getWorkspace().getRoot().getProject("TestProject");
			assertTrue(AndroidProjectUtils.createNewProject(m_project, getTarget(), getMinSdk()));
			m_testProject = new TestProject(m_project);
			m_javaProject = m_testProject.getJavaProject();
			waitForAutoBuild();
		}
	}
	/**
	 * Configures created project.
	 */
	@Override
	protected void configureNewProject() throws Exception {
		/*BTestUtils.configure(m_testProject);
		m_testProject.addPlugin("org.eclipse.core.databinding");
		m_testProject.addBundleJars("org.eclipse.wb.xwt", "lib");*/
	}
	@SuppressWarnings("restriction")
	protected IAndroidTarget getTarget() {
		return AndroidProjectUtils.getAndroidTarget();
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
	protected void configureForTestPreferences(IPreferenceStore preferences) {
		// direct edit
		preferences.setValue(IPreferenceConstants.P_GENERAL_DIRECT_EDIT_AFTER_ADD, false);
	}
	/**
	 * Configures default values for toolkit preferences.
	 */
	protected void configureDefaultPreferences(IPreferenceStore preferences) {
		/*preferences.setToDefault(IPreferenceConstants.P_LAYOUT_DATA_NAME_TEMPLATE);
		preferences.setToDefault(IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE);*/
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Open "Design" and fetch
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Opens {@link AbstractXmlEditor} with given XML source.
	 */
	@SuppressWarnings("unchecked")
	protected <T extends XmlObjectInfo> T openEditor(String... lines) throws Exception {
		IFile file = setFileContent(AndroidProjectUtils.RES_PATH + "/test.xml", getTestSource(lines));
		file.setPersistentProperty(DeviceManager.KEY_SKIN, "false");
		openDesign(file);
		return (T) m_contentObject;
	}
	@Override
	protected final String getEditorID() {
		return AndroidEditor.ID;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Java source
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getJavaSourceToAssert() {
		return getFileContentSrc(AndroidProjectUtils.PACKAGE_NAME + "/Test.java");
	}
	@Override
	protected String[] getJavaSource_decorate(String... lines) {
		lines =
				CodeUtils.join(new String[]{
						"package " + AndroidProjectUtils.PACKAGE_NAME + ";",
						"import android.app.Activity;",
						"import android.os.Bundle;",
						"import android.view.View;",
						"import android.view.ViewGroup;",
						"import android.view.ViewParent;"}, lines);
		return lines;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// XML source
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getTestSource_namespaces() {
		return StringUtils.EMPTY;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Tool
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Loads {@link CreationTool} with {@link android.widget.Button} with text.
	 */
	protected final ViewInfo loadButton() throws Exception {
		return loadCreationTool("android.widget.Button");
	}
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
	/*protected final void prepareMyComponent(String[] javaLines, String[] descriptionLines) throws Exception {
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
	}*/
	////////////////////////////////////////////////////////////////////////////
	//
	// Box
	//
	////////////////////////////////////////////////////////////////////////////
	/*protected void prepareBox() throws Exception {
		prepareBox(100, 50);
	}
	protected void prepareBox(int width, int height) throws Exception {
		setFileContentSrc(
			"test/Box.java",
			getJavaSource(
				"public class Box extends org.eclipse.swt.widgets.Button {",
				"  public Box(Composite parent, int style) {",
				"    super(parent, style);",
				"  }",
				"  protected void checkSubclass () {",
				"  }",
				"  public Point computeSize (int wHint, int hHint, boolean changed) {",
				"    return new Point(" + width + ", " + height + ");",
				"  }",
				"}"));
		waitForAutoBuild();
	}
	protected ControlInfo loadBox() throws Exception {
		return loadCreationTool("test.Box");
	}*/
}