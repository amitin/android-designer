package org.eclipse.wb.android.internal.model.property.editor;

import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;
import org.eclipse.wb.internal.core.xml.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;

/**
 * Property editor for Format.COLOR attributes.
 * 
 * @author mitin_aa
 * @coverage model.property.editor
 */
public final class ColorPropertyEditor extends PropertyEditor implements IClipboardSourceProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new ColorPropertyEditor();

  private ColorPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final int SAMPLE_SIZE = 10;
  private static final int SAMPLE_MARGIN = 3;
  private final PropertyEditorPresentation m_presentation = new ButtonPropertyEditorPresentation() {
    @Override
    protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
      openDialog(property);
    }
  };

  @Override
  public PropertyEditorPresentation getPresentation() {
    return m_presentation;
  }

  @Override
  public void paint(Property property, GC gc, int x, int y, int width, int height) throws Exception {
    Object value = property.getValue();
    if (value instanceof String) {
      RGB rgb = decodeRgb((String) value);
      if (rgb == null) {
        return;
      }
      Color color = SwtResourceManager.getColor(rgb);
      // draw color sample
      {
        Color oldBackground = gc.getBackground();
        Color oldForeground = gc.getForeground();
        try {
          int width_c = SAMPLE_SIZE;
          int height_c = SAMPLE_SIZE;
          int x_c = x;
          int y_c = y + (height - height_c) / 2;
          // update rest bounds
          {
            int delta = SAMPLE_SIZE + SAMPLE_MARGIN;
            x += delta;
            width -= delta;
          }
          // fill
          {
            gc.setBackground(color);
            gc.fillRectangle(x_c, y_c, width_c, height_c);
          }
          // draw line
          gc.setForeground(IColorConstants.gray);
          gc.drawRectangle(x_c, y_c, width_c, height_c);
        } finally {
          gc.setBackground(oldBackground);
          gc.setForeground(oldForeground);
        }
      }
      // draw color text
      {
        String text = getText(property);
        if (text != null) {
          DrawUtils.drawStringCV(gc, text, x, y, width, height);
        }
      }
    }
  }

  /**
   * @return the text for current {@link Color} value.
   */
  private String getText(Property property) throws Exception {
    // use expression
    if (property instanceof GenericProperty) {
      if (property.isModified()) {
        return ((GenericProperty) property).getExpression();
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardSourceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getClipboardSource(GenericProperty property) throws Exception {
    return getText(property);
  }

  @Override
  public boolean activate(PropertyTable propertyTable, Property property, Point location)
      throws Exception {
    // activate using keyboard
    if (location == null) {
      openDialog(property);
    }
    // don't activate
    return false;
  }

  /**
   * Opens editing dialog.
   */
  private void openDialog(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    ColorDialog colorDialog = new ColorDialog(DesignerPlugin.getShell());
    // set initial color
    RGB initial = null;
    {
      Object value = property.getValue();
      if (value instanceof String) {
        initial = decodeRgb((String) value);
        if (initial != null) {
          colorDialog.setRGB(initial);
        }
      }
    }
    // open dialog
    RGB result = colorDialog.open();
    // set result if any
    if (result != null && !result.equals(initial)) {
      String expression = String.format("#%02X%02X%02X", result.red, result.green, result.blue);
      genericProperty.setExpression(expression, Property.UNKNOWN_VALUE);
    }
  }

  private RGB decodeRgb(String colorString) {
    try {
      int color = Integer.decode(colorString);
      return new RGB(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF);
    } catch (Throwable e) {
      return null;
    }
  }
}