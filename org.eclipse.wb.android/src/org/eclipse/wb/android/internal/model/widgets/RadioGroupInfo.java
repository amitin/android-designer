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
package org.eclipse.wb.android.internal.model.widgets;

import org.eclipse.wb.android.internal.model.layouts.LinearLayoutInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAdd;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;


/**
 * Model for support RadioGroup.
 * 
 * @author mitin_aa
 * @coverage android.model
 */
public class RadioGroupInfo extends LinearLayoutInfo {
  private final RadioGroupInfo m_this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RadioGroupInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    m_this = this;
    addRadioButtonsOnAdding();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addRadioButtonsOnAdding() {
    addBroadcastListener(new XmlObjectAdd() {
      @Override
      public void after(ObjectInfo parent, XmlObjectInfo child) throws Exception {
        if (child == m_this && !isIncluded()) {
          addRadioButtons();
        }
      }
    });
  }

  /**
   * Add two radio buttons additionally.
   */
  protected void addRadioButtons() throws Exception {
    addRadioButton();
    addRadioButton();
  }

  private void addRadioButton() throws Exception {
    XmlObjectInfo radioButton =
        XmlObjectUtils.createObject(
            getContext(),
            "android.widget.RadioButton",
            new ElementCreationSupport());
    XmlObjectUtils.add(radioButton, Associations.direct(), this, null);
  }
}
