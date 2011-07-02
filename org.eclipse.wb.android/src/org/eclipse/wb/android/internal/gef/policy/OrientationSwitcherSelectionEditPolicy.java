/*******************************************************************************
 * Copyright (c) 2011 Andrey Sablin (Sablin.Andrey@gmail.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrey Sablin (Sablin.Andrey@gmail.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.android.internal.gef.policy;

import com.google.common.collect.Lists;

import org.eclipse.wb.android.internal.Activator;
import org.eclipse.wb.android.internal.model.layouts.LinearLayoutInfo;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.RelativeLocator;
import org.eclipse.wb.draw2d.events.IMouseListener;
import org.eclipse.wb.draw2d.events.MouseEvent;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import java.util.List;

/**
 * {@link SelectionEditPolicy} that shows ...
 * 
 * @author sablin_aa
 * @coverage android.gef.policy
 */
public class OrientationSwitcherSelectionEditPolicy extends SelectionEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    final GraphicalEditPart host = getHost();
    final LinearLayoutInfo layout = (LinearLayoutInfo) host.getModel();
    RelativeLocator locator = new RelativeLocator(host.getFigure(), IPositionConstants.NORTH_WEST);
    Handle handle = new Handle(host, locator) {
      {
        setSize(10, 10);
        addMouseListener(new IMouseListener() {
          public void mouseUp(MouseEvent event) {
          }

          public void mouseDown(MouseEvent event) {
            event.consume();
            // prepare IMenuManager
            MenuManager manager = new MenuManager();
            {
              ObjectInfoAction horizontalAction =
                  new ObjectInfoAction(layout, "Horizontal", IAction.AS_RADIO_BUTTON) {
                    @Override
                    protected void runEx() throws Exception {
                      layout.setHorizontal(true);
                    }
                  };
              manager.add(horizontalAction);
              ObjectInfoAction verticalAction =
                  new ObjectInfoAction(layout, "Vertical", IAction.AS_RADIO_BUTTON) {
                    @Override
                    protected void runEx() throws Exception {
                      layout.setHorizontal(false);
                    }
                  };
              manager.add(verticalAction);
              //
              if (isHorizontal()) {
                horizontalAction.setChecked(true);
              } else {
                verticalAction.setChecked(true);
              }
            }
            // open context menu
            Control control = host.getViewer().getControl();
            Menu menu = manager.createContextMenu(control);
            menu.setVisible(true);
          }

          public void mouseDoubleClick(MouseEvent event) {
          }
        });
      }

      @Override
      protected void paintClientArea(Graphics graphics) {
        Rectangle clientArea = getClientArea();
        // draw image
        {
          Image image =
              isHorizontal()
                  ? Activator.getImage("info/layout/LinearLayout/orientation_h.png")
                  : Activator.getImage("info/layout/LinearLayout/orientation_v.png");
          if (image != null) {
            org.eclipse.swt.graphics.Rectangle imageBounds = image.getBounds();
            int x = (clientArea.width - imageBounds.width) / 2;
            int y = (clientArea.height - imageBounds.height) / 2;
            graphics.drawImage(image, x, y);
          }
        }
      }

      private boolean isHorizontal() {
        try {
          return layout.isHorizontal();
        } catch (Exception e) {
          DesignerPlugin.log(e);
          return true;
        }
      }
    };
    return Lists.newArrayList(handle);
  }
}