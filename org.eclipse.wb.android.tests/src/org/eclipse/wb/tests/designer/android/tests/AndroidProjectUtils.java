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
package org.eclipse.wb.tests.designer.android.tests;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.wb.android.internal.support.AndroidBridge;

import com.android.ide.eclipse.adt.internal.sdk.Sdk;
import com.android.ide.eclipse.adt.internal.wizards.newproject.NewProjectCreator;
import com.android.ide.eclipse.adt.internal.wizards.newproject.NewProjectWizardState;
import com.android.ide.eclipse.adt.internal.wizards.newproject.NewProjectWizardState.Mode;
import com.android.sdklib.IAndroidTarget;

/**
 * Utils for Android test project.
 * 
 * @author sablin_aa
 */
public abstract class AndroidProjectUtils {
	public static final String PACKAGE_NAME = "wb.test";
	public static final String RES_PATH = "res/layout";
	/**
	 * @return {@link true} if new Android project successfully created.
	 */
	public static boolean createNewProject(IProject project, IAndroidTarget target, String minSdk)
			throws Exception {
		// create Android project by ADT wizard creator
		NewProjectWizardState projectValues = new NewProjectWizardState(Mode.ANY);
		{
			projectValues.useExisting = false;
			projectValues.projectName = project.getName();
			projectValues.projectNameModifiedByUser = true;
			{
				String locationString =
						ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString()
							+ project.getFullPath().toOSString();
				projectValues.projectLocation = new File(locationString);
			}
			projectValues.applicationName = projectValues.projectName;
			projectValues.packageName = PACKAGE_NAME;
			projectValues.packageNameModifiedByUser = true;
			projectValues.activityName = projectValues.projectName + "Activity";
			projectValues.createActivity = true;
			projectValues.target = target;
			{
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
		return creator.createAndroidProjects();
	}
	/**
	 * @return the {@link IAndroidTarget} with max API-level.
	 */
	public static IAndroidTarget getAndroidTarget() {
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
}