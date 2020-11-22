package specman.textfield;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class InsetPanel extends JPanel {
    private FormLayout layout;
    private EmptyBorder border;
    private JEditorPane editorPane;

    InsetPanel(JEditorPane editorPane) {
        this.editorPane = editorPane;
        this.layout = new FormLayout("0px,10px:grow,0px", "0px,fill:pref:grow,0px");
        this.border = new EmptyBorder(3, 3, 3, 3);

        setLayout(layout);
        editorPane.setBorder(border);

//    setLayout(new FormLayout("10px,10px:grow,10px", "1px,fill:pref:grow,1px"));
//    setBorder(new EmptyBorder(2, 0, 2, 0));

        add(editorPane, CC.xy(2, 2));
    }

    public void setLeftInset(int px) {
        Insets insets = border.getBorderInsets();
        insets.left += px;
        border = new EmptyBorder(insets);
        editorPane.setBorder(border);
    }

    public void setRightInset(int px) {
        Insets insets = border.getBorderInsets();
        insets.right += px;
        border = new EmptyBorder(insets);
        editorPane.setBorder(border);
    }
}
