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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

/**
 * Android GEF tests.
 * 
 * @author sablin_aa
 */
public class GefTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.android.gef");
		suite.addTest(createSingleSuite(ViewGroupGefTest.class));
		return suite;
	}
}
