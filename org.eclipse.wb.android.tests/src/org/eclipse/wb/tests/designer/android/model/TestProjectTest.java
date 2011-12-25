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

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.eclipse.wb.android.internal.model.widgets.ViewGroupInfo;
import org.eclipse.wb.android.internal.model.widgets.ViewInfo;

/**
 * Tests for project configuration.
 * 
 * @author sablin_aa
 */
public class TestProjectTest extends AndroidModelTest {
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
	public void test_parse() throws Exception {
		ViewGroupInfo layout =
				parse(
					"<?xml version='1.0' encoding='utf-8'?>",
					"<LinearLayout xmlns:android='http://schemas.android.com/apk/res/android'",
					"  android:layout_width='fill_parent'",
					"  android:layout_height='fill_parent'",
					"  android:orientation='vertical'>",
					"  <TextView",
					"    android:layout_width='fill_parent'",
					"    android:layout_height='wrap_content'",
					"    android:text='@string/hello'/>",
					"</LinearLayout>");
		refresh();
		assertThat(layout).isNotNull();
		List<ViewInfo> childrenViews = layout.getChildrenViews();
		assertThat(childrenViews.size()).isEqualTo(1);
		ViewInfo text = childrenViews.get(0);
		assertThat(text).isNotNull();
		assertThat(text.getPropertyByTitle("text").getValue()).isEqualTo("@string/hello");
	}
	public void test_button() throws Exception {
		ViewGroupInfo layout =
				parse(
					"<?xml version='1.0' encoding='utf-8'?>",
					"<LinearLayout xmlns:android='http://schemas.android.com/apk/res/android'",
					"  android:layout_width='fill_parent'",
					"  android:layout_height='fill_parent'",
					"  android:orientation='vertical'>",
					"  <TextView",
					"    android:layout_width='fill_parent'",
					"    android:layout_height='wrap_content'",
					"    android:text='@string/hello'/>",
					"</LinearLayout>");
		refresh();
		assertThat(layout).isNotNull();
		ViewInfo button = createButton();
		flowContainer_CREATE(layout, button, layout.getChildrenViews().get(0));
		assertXML(
			"<?xml version='1.0' encoding='utf-8'?>",
			"<LinearLayout xmlns:android='http://schemas.android.com/apk/res/android'",
			"  android:layout_width='fill_parent'",
			"  android:layout_height='fill_parent'",
			"  android:orientation='vertical'>",
			"  <Button android:text='New Button' android:layout_width='match_parent' android:layout_height='wrap_content'/>",
			"  <TextView",
			"    android:layout_width='fill_parent'",
			"    android:layout_height='wrap_content'",
			"    android:text='@string/hello'/>",
			"</LinearLayout>");
	}
}