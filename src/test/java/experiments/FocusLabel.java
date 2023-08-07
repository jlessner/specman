package experiments;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FocusLabel extends JFrame {

  FocusLabel() {
    setSize(400, 300);
    setVisible(true);
    Container pane = this.getContentPane();
    pane.setLayout(new FormLayout("600px:grow", "30px,fill:300px:grow,30px"));

    JTextField field = new JTextField("text1");
    pane.add(field, CC.xy(1,1));

    JPanel labelPanel = new JPanel();
    labelPanel.setBorder(new EmptyBorder(new Insets(1, 1, 1, 1)));
    labelPanel.setLayout(new FormLayout("0px:grow", "fill:0px:grow"));
    JLabel label = new JLabel("label");
    label.setBackground(Color.yellow);
    label.setOpaque(true);
    labelPanel.add(label, CC.xy(1,1));
    pane.add(labelPanel, CC.xy(1,2));
    labelPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        labelPanel.requestFocus();
      }
    });
    labelPanel.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        System.out.println("focusGained");
        labelPanel.setBorder(new LineBorder(Color.BLACK));
      }

      @Override
      public void focusLost(FocusEvent e) {
        System.out.println("focusLost");
        labelPanel.setBorder(new EmptyBorder(new Insets(1, 1, 1, 1)));
      }
    });

    JTextField field2 = new JTextField("text2");
    pane.add(field2, CC.xy(1,3));

    this.pack();
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  public static void main(String[] args) {
    new FocusLabel();
  }

}
