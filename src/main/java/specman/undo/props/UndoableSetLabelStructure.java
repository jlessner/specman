package specman.undo.props;

import specman.editarea.stepnumberlabel.StepnumberLabel;
import specman.editarea.stepnumberlabel.StepnumberLabel.LabelStructure;

public class UndoableSetLabelStructure extends UndoableSetProperty<LabelStructure> {

  public UndoableSetLabelStructure(StepnumberLabel label, LabelStructure structure) {
    super(structure, label::setStructure, label::getStructure);
  }
}
