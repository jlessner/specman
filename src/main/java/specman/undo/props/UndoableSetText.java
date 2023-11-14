package specman.undo.props;

import javax.swing.*;

public class UndoableSetText extends UndoableSetProperty<String> {

  public UndoableSetText(JLabel label, String undoText) {
    super(undoText, label::setText, label::getText);
  }
}
