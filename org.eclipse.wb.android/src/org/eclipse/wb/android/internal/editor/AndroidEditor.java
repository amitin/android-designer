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
package org.eclipse.wb.android.internal.editor;

import org.eclipse.wb.internal.core.xml.editor.AbstractXmlEditor;
import org.eclipse.wb.internal.core.xml.editor.XmlDesignPage;

/**
 * Editor for Android UI.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage android.editor
 */
public final class AndroidEditor extends AbstractXmlEditor {
  public static final String ID = "org.eclipse.wb.android.editor.AndroidEditor";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected XmlDesignPage createDesignPage() {
    return new AndroidDesignPage();
  }
}
