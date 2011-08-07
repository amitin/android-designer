package org.eclipse.wb.android.internal.gef.policy.layouts.table.header.selection;

import org.eclipse.wb.android.internal.gef.policy.layouts.table.header.part.RowHeaderEditPart;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link RowHeaderEditPart}.
 * 
 * @author mitin_aa
 * @coverage android.gef.policy
 */
public final class RowSelectionEditPolicy extends DimensionSelectionEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
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
        // vertical
        if (c == 'd' || c == 'D' || c == 'u' || c == 'U') {
          setAlignment(RowInfo.Alignment.UNKNOWN);
        } else if (c == 't') {
          setAlignment(RowInfo.Alignment.TOP);
        } else if (c == 'm' || c == 'M' || c == 'c' || c == 'C') {
          setAlignment(RowInfo.Alignment.MIDDLE);
        } else if (c == 'b') {
          setAlignment(RowInfo.Alignment.BOTTOM);
        }
      }*/
    }
  }
  /**
   * Sets the alignment for {@link RowInfo}.
   */
  /*private void setAlignment(final RowInfo.Alignment alignment) {
    final HTMLTableInfo panel = getPanel();
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        getDimension().setAlignment(alignment);
      }
    });
  }*/
}
