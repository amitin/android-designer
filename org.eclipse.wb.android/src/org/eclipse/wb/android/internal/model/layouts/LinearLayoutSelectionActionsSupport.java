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
package org.eclipse.wb.android.internal.model.layouts;

import org.eclipse.wb.android.internal.Activator;
import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;

import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;


import java.util.List;

/**
 * Adds toolbar actions for LinearLayout.
 * 
 * @author mitin_aa
 * @coverage android.model
 */
public final class LinearLayoutSelectionActionsSupport extends ObjectEventListener {
  private final LinearLayoutInfo m_layout;
  private List<ObjectInfo> m_objects;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LinearLayoutSelectionActionsSupport(LinearLayoutInfo layout) {
    m_layout = layout;
    m_layout.addBroadcastListener(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObjectEventListener (selection actions)
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addSelectionActions(List<ObjectInfo> objects, List<Object> actions) throws Exception {
    if (objects.isEmpty()) {
      return;
    }
    // target is not on our container
    if (objects.get(0).getParent() != m_layout) {
      return;
    }
    m_objects = objects;
    // create gravity actions
    actions.add(new Separator());
    addGravityAction(actions, true, "left.gif", "Left.", SWT.LEFT);
    addGravityAction(actions, true, "center.gif", "Center Horizontal.", SWT.CENTER);
    addGravityAction(actions, true, "right.gif", "Right.", SWT.RIGHT);
    addGravityAction(actions, true, "fill.gif", "Fill Horizontal.", SWT.FILL);
    actions.add(new Separator());
    addGravityAction(actions, false, "top.gif", "Top.", SWT.TOP);
    addGravityAction(actions, false, "center.gif", "Center Vertical.", SWT.CENTER);
    addGravityAction(actions, false, "bottom.gif", "Bottom.", SWT.BOTTOM);
    addGravityAction(actions, false, "fill.gif", "Fill Vertical.", SWT.FILL);
  }

  private void addGravityAction(List<Object> actions,
      boolean isHorisontal,
      String imagePath,
      String tooltip,
      int position) throws Exception {
    boolean isChecked = true;
    for (ObjectInfo object : m_objects) {
      if (!m_layout.hasGravityValue((ViewInfo) object, isHorisontal, position)) {
        isChecked = false;
        break;
      }
    }
    actions.add(new GravityAction(isHorisontal, imagePath, tooltip, isChecked, position));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  private abstract class AbstractAction extends ObjectInfoAction {
    protected final boolean m_horizontal;

    public AbstractAction(int style,
        boolean horizontal,
        String iconPath,
        String tooltip,
        boolean checked) {
      super(m_layout, "", style);
      m_horizontal = horizontal;
      String path = "info/layout/LinearLayout/" + (m_horizontal ? "h" : "v") + "/menu/" + iconPath;
      setImageDescriptor(Activator.getImageDescriptor(path));
      setToolTipText(tooltip);
      setChecked(checked);
    }
  }
  private final class GravityAction extends AbstractAction {
    private final int m_position;

    public GravityAction(boolean horizontal,
        String iconPath,
        String tooltip,
        boolean checked,
        int position) {
      super(AS_RADIO_BUTTON, horizontal, iconPath, "Set Grativy to " + tooltip, checked);
      m_position = position;
    }

    @Override
    protected void runEx() throws Exception {
      for (ObjectInfo object : m_objects) {
        m_layout.setGravity((ViewInfo) object, m_horizontal, m_position);
      }
    }
  }
}
