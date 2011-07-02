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
package org.eclipse.wb.android.internal.gef;

import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.android.internal.support.DeviceManager;
import org.eclipse.wb.android.internal.support.DeviceManager.DisplaySkin;
import org.eclipse.wb.core.gef.IEditPartConfigurator;
import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.core.gef.policy.selection.EmptySelectionEditPolicy;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.events.IEditPartListener;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.swt.graphics.Image;


/**
 * {@link IEditPartConfigurator} for root {@link EditPart} to show device image.
 * 
 * @author sablin_aa
 * @coverage android.gef
 */
public final class DeviceEditPartConfigurator implements IEditPartConfigurator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartConfigurator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(EditPart context, EditPart editPart) {
    if (editPart.getModel() instanceof ViewInfo) {
      ViewInfo viewInfo = (ViewInfo) editPart.getModel();
      if (viewInfo.isRoot()) {
        new RootEditPartHandler(context.getViewer(), (GraphicalEditPart) editPart, viewInfo);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handler
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class RootEditPartHandler {
    private final GraphicalEditPart editPart;
    private final ViewInfo object;
    private EditPolicy selectionPolicy;
    private DisplaySkin deviceImage;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public RootEditPartHandler(IEditPartViewer viewer, GraphicalEditPart editPart, ViewInfo object) {
      this.editPart = editPart;
      this.object = object;
      // add/remove device figure
      viewer.getRootEditPart().addEditPartListener(new IEditPartListener() {
        public void childAdded(EditPart child, int index) {
          Layer deviceLayer = child.getViewer().getLayer(IEditPartViewer.PRIMARY_LAYER_SUB_1);
          deviceLayer.add(deviceFigure);
          // refresh now, when EditPart is added
          refresh();
        }

        public void removingChild(EditPart child, int index) {
          FigureUtils.removeFigure(deviceFigure);
        }
      });
      // update EditPart figure and selection policy
      object.addBroadcastListener(new ObjectEventListener() {
        @Override
        public void refreshed2() throws Exception {
          refresh();
        }
      });
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Notify
    //
    ////////////////////////////////////////////////////////////////////////////
    private final Figure deviceFigure = new Figure() {
      @Override
      protected void paintClientArea(final Graphics graphics) {
        ExecutionUtils.runIgnore(new RunnableEx() {
          public void run() throws Exception {
            if (deviceImage != null) {
              graphics.drawImage(deviceImage.getImage(), 0, 0);
            }
          }
        });
      }
    };

    private void refresh() {
      boolean showDeviceImage = DeviceManager.getShowDeviceImage(object);
      if (showDeviceImage) {
        deviceImage = DeviceManager.getDeviceImage(object);
      } else {
        deviceImage = null;
      }
      refreshSelectionPolicy();
      refreshVisuals();
    }

    /**
     * Removes or restores {@link EditPolicy#SELECTION_ROLE}.
     */
    private void refreshSelectionPolicy() {
      // if has device
      if (deviceImage != null) {
        // if was no device yet
        if (selectionPolicy == null) {
          selectionPolicy = editPart.getEditPolicy(EditPolicy.SELECTION_ROLE);
        }
        // can not resize on device
        editPart.installEditPolicy(EditPolicy.SELECTION_ROLE, new EmptySelectionEditPolicy());
        return;
      }
      // restore original "selection" if was device before
      if (selectionPolicy != null) {
        editPart.installEditPolicy(EditPolicy.SELECTION_ROLE, selectionPolicy);
        selectionPolicy = null;
      }
    }

    /**
     * Updates bounds of {@link #m_deviceFigure} and {@link #m_editPart}.
     */
    private void refreshVisuals() {
      ExecutionUtils.runIgnore(new RunnableEx() {
        public void run() throws Exception {
          refreshVisualsEx();
        }
      });
    }

    /**
     * Implementation for {@link #refreshVisuals()}.
     */
    private static final Point DEVICE_LOCATION = new Point(5, 5);

    private void refreshVisualsEx() throws Exception {
      Figure editPartFigure = editPart.getFigure();
      Rectangle bounds;
      if (deviceImage != null) {
        Image image = deviceImage.getImage();
        {
          deviceFigure.setLocation(DEVICE_LOCATION);
          deviceFigure.setSize(image.getBounds().width, image.getBounds().height);
        }
        // update EditPart figure
        bounds = deviceImage.getClientArea();
        bounds.translate(DEVICE_LOCATION);
      } else {
        bounds = object.getBounds();
        bounds =
            new Rectangle(AbstractComponentEditPart.TOP_LOCATION.x,
                AbstractComponentEditPart.TOP_LOCATION.y,
                bounds.width,
                bounds.height);
      }
      editPartFigure.setBounds(bounds);
    }
  }
}
