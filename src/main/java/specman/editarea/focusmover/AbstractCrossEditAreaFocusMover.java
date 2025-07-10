package specman.editarea.focusmover;

import specman.editarea.AbstractListItemEditArea;
import specman.editarea.EditArea;
import specman.editarea.EditContainer;
import specman.editarea.TableEditArea;

import java.awt.*;

abstract class AbstractCrossEditAreaFocusMover<EDITAREATYPE extends EditArea> {
  protected final EDITAREATYPE currentFocusArea;

  public AbstractCrossEditAreaFocusMover(EDITAREATYPE currentFocusArea) {
    this.currentFocusArea = currentFocusArea;
  }

  protected void focus(EditArea editArea) {
    editArea.requestFocus();
  }

  protected AbstractListItemEditArea findNestingListItem(EDITAREATYPE editAreaInListItem) {
    Component containerParent = editAreaInListItem.getParent().getParent();
    return (containerParent instanceof AbstractListItemEditArea)
      ? (AbstractListItemEditArea) containerParent
      : null;
  }

  private TableEditArea findNestingTable(EDITAREATYPE editAreaInTableItem) {
    Component containerParent = editAreaInTableItem.getParent().getParent().getParent();
    return (containerParent instanceof TableEditArea)
      ? (TableEditArea) containerParent
      : null;
  }

  protected EditArea succeedingEditAreaInSameContainer(EditArea editArea) {
    EditContainer container = editArea.getParent();
    int areaIndex = container.indexOf(editArea);
    if (container.getLastEditArea() != editArea) {
      return container.getEditAreaAt(areaIndex + 1);
    }
    return null;
  }

  protected EditArea findSucceedingEditAreaOfListItem(EDITAREATYPE editAreaInListItem) {
    AbstractListItemEditArea listItemEditArea = findNestingListItem(editAreaInListItem);
    if (listItemEditArea != null) {
      return succeedingEditAreaInSameContainer(listItemEditArea);
    }
    return null;
  }

  protected EditArea findPreceedingEditArea() {
    EditArea preceedingEditArea = preceedingEditAreaInSameContainer(currentFocusArea);
    if (preceedingEditArea == null) {
      preceedingEditArea = findPreceedingEditAreaOfListItem(currentFocusArea);
    }
    if (preceedingEditArea == null) {
      preceedingEditArea = findPreceedingEditAreaOfTable(currentFocusArea);
    }
    return preceedingEditArea;
  }

  protected EditArea preceedingEditAreaInSameContainer(EditArea editArea) {
    EditContainer container = editArea.getParent();
    int areaIndex = container.indexOf(editArea);
    if (container.getFirstEditArea() != editArea) {
      return container.getEditAreaAt(areaIndex-1);
    }
    return null;
  }

  private EditArea findPreceedingEditAreaOfListItem(EDITAREATYPE editAreaInListItem) {
    AbstractListItemEditArea listItemEditArea = findNestingListItem(editAreaInListItem);
    if (listItemEditArea != null) {
      return preceedingEditAreaInSameContainer(listItemEditArea);
    }
    return null;
  }

  protected EditArea findSucceedingEditArea() {
    EditArea preceedingEditArea = succeedingEditAreaInSameContainer(currentFocusArea);
    if (preceedingEditArea == null) {
      preceedingEditArea = findSucceedingEditAreaOfListItem(currentFocusArea);
    }
    if (preceedingEditArea == null) {
      preceedingEditArea = findSucceedingEditAreaOfTableItem(currentFocusArea);
    }
    return preceedingEditArea;
  }

  protected EditArea findSucceedingEditAreaOfTableItem(EDITAREATYPE editAreaInTableItem) {
    TableEditArea tableEditArea = findNestingTable(editAreaInTableItem);
    if (tableEditArea != null) {
      return succeedingEditAreaInSameContainer(tableEditArea);
    }
    return null;
  }

  private EditArea findPreceedingEditAreaOfTable(EDITAREATYPE editAreaInTableItem) {
    TableEditArea tableEditArea = findNestingTable(editAreaInTableItem);
    if (tableEditArea != null) {
      return preceedingEditAreaInSameContainer(tableEditArea);
    }
    return null;
  }

  public void moveFocusToSucceedingEditArea() {
    if (caretAtBottom()) {
      EditArea succeedingEditArea = findSucceedingEditArea();
      if (succeedingEditArea != null) {
        focus(succeedingEditArea);
      }
    }
  }

  public void moveFocusToPreceedingEditArea() {
    if (caretAtTop()) {
      EditArea preceedingEditArea = findPreceedingEditArea();
      if (preceedingEditArea != null) {
        focus(preceedingEditArea);
      }
    }
  }

  abstract protected boolean caretAtTop();

  abstract protected boolean caretAtBottom();

}
