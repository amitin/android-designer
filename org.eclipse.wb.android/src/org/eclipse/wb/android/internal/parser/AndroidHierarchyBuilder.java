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
package org.eclipse.wb.android.internal.parser;

import org.eclipse.wb.core.model.ObjectInfo;

import com.android.ide.common.rendering.api.ViewInfo;

/**
 * Visitor for traversing Android native {@link ViewInfo} objects.
 * 
 * @author mitin_aa
 * @coverage android.parser
 */
public abstract class AndroidHierarchyBuilder {
  @SuppressWarnings("unused")
  public ObjectInfo visit(ViewInfo nativeView, ObjectInfo parent) throws Exception {
    return null;
  }
}
