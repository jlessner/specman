package specman;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AtomicActionButton extends JButton {
  public AtomicActionButton(String text) {
    super(text);
  }

  public AtomicActionButton() { }

  @Deprecated @Override
  public void addActionListener(ActionListener listener) {
    throw new UnsupportedOperationException("This is a button for atomic operations. Use addAtomicActionListener() instead.");
  }

  public void addAtomicActionListener(ActionListener listener) {
    super.addActionListener(new AtomicActionListener() {
      @Override
      public void performAtomicAction(ActionEvent e) {
        listener.actionPerformed(e);
      }
    });
  }
}
