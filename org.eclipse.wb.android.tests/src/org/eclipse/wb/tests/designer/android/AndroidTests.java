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
package org.eclipse.wb.tests.designer.android;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.wb.tests.designer.android.model.TestProjectTest;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

/**
 * All Android tests.
 * 
 * @author sablin_aa
 */
public class AndroidTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.android");
		suite.addTest(createSingleSuite(TestProjectTest.class));
		//suite.addTest(GefTests.suite());
		return suite;
	}
}
