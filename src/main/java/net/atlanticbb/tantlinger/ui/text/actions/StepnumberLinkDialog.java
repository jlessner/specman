package net.atlanticbb.tantlinger.ui.text.actions;

import jdk.security.jarsigner.JarSigner;
import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.HeaderPanel;
import net.atlanticbb.tantlinger.ui.UIUtils;
import org.apache.commons.lang.StringUtils;
import specman.Specman;
import specman.editarea.TextEditArea;
import specman.view.AbstractSchrittView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.Serial;
import java.util.List;

public class StepnumberLinkDialog extends JDialog {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final I18n i18n = I18n.getInstance("net.atlanticbb.tantlinger.ui.text.dialogs");
    private static final Icon icon = UIUtils.getIcon("resources/images/x32/", "link3.png");
    private static final String title = "Link to Stepnumber...";
    private static final String desc = "Insert a link to a Stepnumber";
    private static final Color DEFAULT_BACKGROUND = Color.white;
    private static final Color HOVERED_BACKGROUND = Color.lightGray;
    private static final int MAXIMAL_REFERENCE_TEXTLENGTH = 60;
    private JTextComponent editor;
    private int hoveredIndex = -1;
    private final CustomListCellRenderer customListCellRenderer = new CustomListCellRenderer();

    public StepnumberLinkDialog(Frame parent, JTextComponent ed) {
        super(parent, title);
        this.editor = ed;
        this.init();
    }

    public StepnumberLinkDialog(Dialog parent, JTextComponent ed) {
        super(parent, title);
        this.editor = ed;
        this.init();
    }

    private void init() {
        JPanel headerPanel = new HeaderPanel(title, desc, icon);

        JList<AbstractSchrittView> jlist = new JList<>(getStepnumberList());
        jlist.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                hoveredIndex = -1;
                jlist.repaint();
            }
        });
        jlist.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point position = e.getPoint();
                JList<AbstractSchrittView> jlist = (JList<AbstractSchrittView>) (e.getComponent());
                hoveredIndex = jlist.locationToIndex(position);
                jlist.repaint();
            }
        });
        jlist.addListSelectionListener(this::ListValueChanged);
        jlist.setCellRenderer(customListCellRenderer);


        jlist.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new MatteBorder(1, 1, 1, 1, Color.black)));

        JButton close = new JButton(i18n.str("close"));
        close.addActionListener(e -> StepnumberLinkDialog.this.setVisible(false));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(close);
        JScrollPane scrollPane = new JScrollPane(jlist);
        this.getRootPane().setDefaultButton(close);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(headerPanel, "North");
        this.getContentPane().add(scrollPane, "Center");
        this.getContentPane().add(buttonPanel, "South");
        this.setMinimumSize(new Dimension(400, 600));
        this.pack();
        this.setResizable(false);
    }

    private DefaultListModel<AbstractSchrittView> getStepnumberList() {
        DefaultListModel<AbstractSchrittView> listModel = new DefaultListModel<>();
        final List<AbstractSchrittView> steps = Specman.instance().listAllSteps();
        listModel.addAll(steps);

        return listModel;
    }

    public void setJTextComponent(JTextComponent ed) {
        this.editor = ed;
    }

    private void ListValueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() && StepnumberLinkDialog.this.editor != null) {
            if (!StepnumberLinkDialog.this.editor.hasFocus()) {
                StepnumberLinkDialog.this.editor.requestFocusInWindow();
            }

            TextEditArea lastFocusedTextArea = (TextEditArea)Specman.instance().getLastFocusedTextArea();
            if (lastFocusedTextArea != null) {
                AbstractSchrittView selectedStep = ((JList<AbstractSchrittView>) e.getSource()).getSelectedValue();

                lastFocusedTextArea.addStepnumberLink(selectedStep);
                Specman.instance().diagrammAktualisieren(null);
            }

            StepnumberLinkDialog.this.setVisible(false);
        }
    }

    private class CustomListCellRenderer extends JLabel implements ListCellRenderer<AbstractSchrittView> {
        public CustomListCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, AbstractSchrittView step, int index, boolean isSelected, boolean cellHasFocus) {
            setText(StringUtils.left(step.toString(), MAXIMAL_REFERENCE_TEXTLENGTH));

            if (index == hoveredIndex) {
                setBackground(HOVERED_BACKGROUND);
            } else {
                setBackground(DEFAULT_BACKGROUND);
            }
            return this;
        }
    }
}