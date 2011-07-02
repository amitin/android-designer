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

import org.eclipse.wb.android.internal.parser.AndroidEditorContext;
import org.eclipse.wb.android.internal.parser.AndroidParser;
import org.eclipse.wb.internal.core.xml.editor.XmlDesignPage;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;


/**
 * {@link XmlDesignPage} for Android Designer.
 * 
 * @author mitin_aa
 * @coverage android.editor
 */
public final class AndroidDesignPage extends XmlDesignPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Render
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected XmlObjectInfo parse() throws Exception {
    AndroidEditorContext context = new AndroidEditorContext(m_file, m_document);
    return new AndroidParser(context).parse();
  }
}
