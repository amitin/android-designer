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
package org.eclipse.wb.android.internal.gef;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.gef.MatchingEditPartFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;

/**
 * {@link IEditPartFactory} for Android.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage android.gef
 */
public final class EditPartFactory implements IEditPartFactory {
  private final static IEditPartFactory MATCHING_FACTORY =
      new MatchingEditPartFactory(ImmutableList.of(
          "org.eclipse.wb.android.internal.model.widgets",
          "org.eclipse.wb.android.internal.model.layouts",
          "org.eclipse.wb.android.internal.model.layouts.table"), ImmutableList.of(
          "org.eclipse.wb.android.internal.gef.part",
          "org.eclipse.wb.android.internal.gef.part",
          "org.eclipse.wb.android.internal.gef.part"));

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditPart createEditPart(EditPart context, Object model) {
    // most EditPart's can be created using matching
    return MATCHING_FACTORY.createEditPart(context, model);
  }
}