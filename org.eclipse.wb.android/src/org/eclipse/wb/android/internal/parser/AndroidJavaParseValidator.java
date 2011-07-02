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

import org.eclipse.wb.internal.core.parser.IParseValidator;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

/**
 * {@link IParseValidator} to prevent opening Android Java class.
 * 
 * @author mitin_aa
 * @coverage android.parser
 */
public class AndroidJavaParseValidator implements IParseValidator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IParseValidator INSTANCE = new AndroidJavaParseValidator();

  private AndroidJavaParseValidator() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IParseValidator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void validate(AstEditor editor) throws Exception {
    // TODO:
  }
}
