package org.eclipse.wb.android.internal.gef.policy.layouts.table.header.action;

import com.google.common.collect.Lists;

import org.eclipse.wb.android.internal.gef.policy.layouts.table.header.part.ColumnHeaderEditPart;
import org.eclipse.wb.android.internal.gef.policy.layouts.table.header.part.DimensionHeaderEditPart;
import org.eclipse.wb.android.internal.model.layouts.table.TableLayoutInfo;
import org.eclipse.wb.android.internal.model.layouts.table.TableLayoutInfo.HeaderInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;

import org.eclipse.jface.resource.ImageDescriptor;

import java.util.List;

/**
 * Abstract action for manipulating selected 'columns'/'rows'.
 * 
 * @author mitin_aa
 * @coverage android.gef.policy
 */
public abstract class DimensionHeaderAction extends ObjectInfoAction {
  private final boolean m_horizontal;
  private final IEditPartViewer m_viewer;
  private final TableLayoutInfo m_tableLayout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionHeaderAction(DimensionHeaderEditPart editPart, String text) {
    this(editPart, text, null);
  }

  public DimensionHeaderAction(DimensionHeaderEditPart editPart,
      String text,
      ImageDescriptor imageDescriptor) {
    this(editPart, text, imageDescriptor, AS_PUSH_BUTTON);
  }

  public DimensionHeaderAction(DimensionHeaderEditPart editPart,
      String text,
      ImageDescriptor imageDescriptor,
      int style) {
    super(editPart.getTableLayout(), text, style);
    m_horizontal = editPart instanceof ColumnHeaderEditPart;
    m_viewer = editPart.getViewer();
    m_tableLayout = editPart.getTableLayout();
    setImageDescriptor(imageDescriptor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    return getClass() == obj.getClass();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Run
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final void runEx() throws Exception {
    // prepare selection
    List<HeaderInfo> dimensions = Lists.newArrayList();
    {
      List<EditPart> editParts = m_viewer.getSelectedEditParts();
      for (EditPart editPart : editParts) {
        if (editPart instanceof DimensionHeaderEditPart) {
          DimensionHeaderEditPart headerEditPart = (DimensionHeaderEditPart) editPart;
          dimensions.add(headerEditPart.getHeader());
        }
      }
    }
    // run over them
    run(dimensions);
  }

  /**
   * Does some operation on {@link List} of selected {@link HeaderInfo}s.
   */
  protected void run(List<HeaderInfo> dimensions) throws Exception {
    for (HeaderInfo dimension : dimensions) {
      run(dimension);
    }
  }

  /**
   * Does some operation on selected {@link HeaderInfo}.
   */
  protected void run(HeaderInfo dimension) throws Exception {
  }
}
