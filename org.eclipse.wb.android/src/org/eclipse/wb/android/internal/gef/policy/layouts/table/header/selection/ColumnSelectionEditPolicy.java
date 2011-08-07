package org.eclipse.wb.android.internal.gef.policy.layouts.table.header.selection;

import org.eclipse.wb.android.internal.gef.policy.layouts.table.header.part.ColumnHeaderEditPart;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link ColumnHeaderEditPart}.
 * 
 * @author mitin_aa
 * @coverage android.gef.policy
 */
public final class ColumnSelectionEditPolicy extends DimensionSelectionEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
    super(mainPolicy);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Keyboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void performRequest(Request request) {
    super.performRequest(request);
    if (request instanceof KeyRequest) {
      KeyRequest keyRequest = (KeyRequest) request;
      /*if (keyRequest.isPressed()) {
        char c = keyRequest.getCharacter();
        if (c == 'd' || c == 'u') {
          setAlignment(ColumnInfo.Alignment.UNKNOWN);
        } else if (c == 'l') {
          setAlignment(ColumnInfo.Alignment.LEFT);
        } else if (c == 'c') {
          setAlignment(ColumnInfo.Alignment.CENTER);
        } else if (c == 'r') {
          setAlignment(ColumnInfo.Alignment.RIGHT);
        } else if (c == 'f') {
        }
      }*/
    }
  }
  /**
   * Sets the alignment for {@link ColumnInfo}.
   */
  /*private void setAlignment(final ColumnInfo.Alignment alignment) {
    final HTMLTableInfo panel = getPanel();
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        getDimension().setAlignment(alignment);
      }
    });
  }*/
}
