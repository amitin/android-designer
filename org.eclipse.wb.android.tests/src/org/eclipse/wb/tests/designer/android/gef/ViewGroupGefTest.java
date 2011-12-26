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

import org.eclipse.wb.android.internal.model.widgets.ViewGroupInfo;

/**
 * Test for {@link ViewGroupInfo} in GEF.
 * 
 * @author sablin_aa
 */
public class ViewGroupGefTest extends AndroidGefTest {
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
	// Drop Layout
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_linearLayout() throws Exception {
		ViewGroupInfo layout =
				openEditor(
					"<?xml version='1.0' encoding='utf-8'?>",
					"<LinearLayout xmlns:android='http://schemas.android.com/apk/res/android'",
					"  android:layout_width='fill_parent'",
					"  android:layout_height='fill_parent'",
					"  android:orientation='vertical'>",
					"</LinearLayout>");
		loadButton();
		//
		canvas.moveTo(layout, 100, 100);
		// FIXME canvas.assertFeedbacks(canvas.getTargetPredicate(layout));
		canvas.click();
		assertXML(
			"<?xml version='1.0' encoding='utf-8'?>",
			"<LinearLayout xmlns:android='http://schemas.android.com/apk/res/android'",
			"  android:layout_width='fill_parent'",
			"  android:layout_height='fill_parent'",
			"  android:orientation='vertical'>",
			"  <Button android:text='New Button' android:layout_width='match_parent' android:layout_height='wrap_content'/>",
			"</LinearLayout>");
	}
}
