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

/**
 * Tests for project configuration.
 * 
 * @author sablin_aa
 */
public class ProjectTest extends AndroidModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_1() throws Exception {
		waitEventLoop(100000);
		/*CompositeInfo shell =
		    parse(
		        "// filler filler filler filler filler",
		        "<Shell>",
		        "  <Button wbp:name='button' bounds='10, 20, 50, 30'/>",
		        "</Shell>");
		refresh();
		ControlInfo button = getObjectByName("button");*/
	}
}