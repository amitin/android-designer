package org.eclipse.wb.android.internal.gef.policy.layouts.table.header.part;

import org.eclipse.wb.android.internal.Activator;
import org.eclipse.wb.android.internal.gef.policy.layouts.table.header.action.DimensionHeaderAction;
import org.eclipse.wb.android.internal.model.layouts.table.TableLayoutInfo;
import org.eclipse.wb.android.internal.model.layouts.table.TableLayoutInfo.HeaderInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;

/**
 * {@link EditPart} for managing 'row' headers of {@link TableLayoutInfo}.
 * 
 * @author mitin_aa
 * @coverage android.gef.policy
 */
public class RowHeaderEditPart extends DimensionHeaderEditPart {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowHeaderEditPart(TableLayoutInfo tableLayout, HeaderInfo header, Figure containerFigure) {
    super(tableLayout, header, containerFigure);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Figure createFigure() {
    Figure newFigure = new Figure() {
      @Override
      protected void paintClientArea(Graphics graphics) {
        Rectangle r = getClientArea();
        // draw rectangle
        graphics.setForegroundColor(IColorConstants.buttonDarker);
        graphics.drawLine(r.x, r.y, r.right(), r.y);
        graphics.drawLine(r.x, r.bottom() - 1, r.right(), r.bottom() - 1);
        // draw row index
        int titleTop;
        {
          String title = "" + m_header.getIndex();
          Dimension textExtents = graphics.getTextExtent(title);
          if (r.height < textExtents.height) {
            return;
          }
          // draw title
          titleTop = r.y + (r.height - textExtents.height) / 2;
          int x = r.x + (r.width - textExtents.width) / 2;
          graphics.setForegroundColor(IColorConstants.black);
          graphics.drawText(title, x, titleTop);
        }
        // draw alignment indicator
        if (titleTop - r.y > 3 + 7 + 3) {
          Image image = null; // TODO:m_row.getAlignment().getSmallImage();
          if (image != null) {
            int y = r.y + 2;
            drawCentered(graphics, image, y);
          }
        }
      }

      private void drawCentered(Graphics graphics, Image image, int y) {
        int x = (getBounds().width - image.getBounds().width) / 2;
        graphics.drawImage(image, x, y);
      }
    };
    //
    newFigure.setFont(DEFAULT_FONT);
    newFigure.setOpaque(true);
    return newFigure;
  }

  @Override
  protected void refreshVisuals() {
    super.refreshVisuals();
    Figure figure = getFigure();
    // bounds
    {
      int index = m_header.getIndex();
      Interval interval = m_tableLayout.getGridInfo().getRowIntervals()[index];
      Rectangle bounds =
          new Rectangle(0,
              interval.begin,
              ((GraphicalEditPart) getParent()).getFigure().getSize().width,
              interval.length + 1);
      bounds.translate(0, getOffset().y);
      figure.setBounds(bounds);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IHeaderMenuProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public void buildContextMenu(IMenuManager manager) {
    // operations
    {
      manager.add(new DimensionHeaderAction(this, "Delete Row",
          Activator.getImageDescriptor("info/layout/TableLayout/v/menu/delete.gif")) {
        @Override
        protected void run(HeaderInfo dimension) throws Exception {
          m_tableLayout.getLayoutSupport().deleteRow(dimension.getIndex());
        }
      });
    }
    // alignment
    {
      /* manager.add(new Separator());
       manager.add(new SetAlignmentRowAction(this, "Default", RowInfo.Alignment.UNKNOWN));
       manager.add(new SetAlignmentRowAction(this, "Top", RowInfo.Alignment.TOP));
       manager.add(new SetAlignmentRowAction(this, "Center", RowInfo.Alignment.MIDDLE));
       manager.add(new SetAlignmentRowAction(this, "Bottom", RowInfo.Alignment.BOTTOM));*/
    }
  }
}
