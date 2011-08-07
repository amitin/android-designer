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
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;

/**
 * {@link EditPart} for managing 'column' headers of {@link TableLayoutInfo}.
 * 
 * @author mitin_aa
 * @coverage android.gef.policy
 */
public class ColumnHeaderEditPart extends DimensionHeaderEditPart {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnHeaderEditPart(TableLayoutInfo tableLayout, HeaderInfo header, Figure containerFigure) {
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
        graphics.drawLine(r.x, r.y, r.x, r.bottom());
        graphics.drawLine(r.right() - 1, r.y, r.right() - 1, r.bottom());
        // draw column index
        int titleLeft;
        {
          String title = "" + m_header.getIndex();
          Dimension textExtents = graphics.getTextExtent(title);
          if (r.width < 3 + textExtents.width + 3) {
            return;
          }
          // draw title
          titleLeft = r.x + (r.width - textExtents.width) / 2;
          int y = r.y + (r.height - textExtents.height) / 2;
          graphics.setForegroundColor(IColorConstants.black);
          graphics.drawText(title, titleLeft, y);
        }
        // draw alignment indicator
        if (titleLeft - r.x > 3 + 7 + 3) {
          Image image = null;// TODO: m_column.getAlignment().getSmallImage();
          if (image != null) {
            int x = r.x + 2;
            drawCentered(graphics, image, x);
          }
        }
      }

      private void drawCentered(Graphics graphics, Image image, int x) {
        int y = (getBounds().height - image.getBounds().height) / 2;
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
      Interval interval = m_tableLayout.getGridInfo().getColumnIntervals()[index];
      Rectangle bounds =
          new Rectangle(interval.begin,
              0,
              interval.length + 1,
              ((GraphicalEditPart) getParent()).getFigure().getSize().height);
      bounds.translate(getOffset().x, 0);
      figure.setBounds(bounds);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IHeaderMenuProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public void buildContextMenu(IMenuManager manager) {
    // alignment
    {
      /*  manager.add(new SetAlignmentColumnAction(this, "Default", ColumnInfo.Alignment.UNKNOWN));
        manager.add(new SetAlignmentColumnAction(this, "Left", ColumnInfo.Alignment.LEFT));
        manager.add(new SetAlignmentColumnAction(this, "Center", ColumnInfo.Alignment.CENTER));
        manager.add(new SetAlignmentColumnAction(this, "Right", ColumnInfo.Alignment.RIGHT));*/
    }
    manager.add(new Separator());
    // operations
    {
      manager.add(new DimensionHeaderAction(this, "Delete Column",
          Activator.getImageDescriptor("info/layout/TableLayout/h/menu/delete.gif")) {
        @Override
        protected void run(HeaderInfo dimension) throws Exception {
          m_tableLayout.getLayoutSupport().deleteColumn(dimension.getIndex());
        }
      });
    }
  }
}
