package specman.view;

import javax.swing.*;

/** A bottom filler is a panel at the bottom of structured step which
 * can be folded (see class {@link KlappButton}). The filler covers the
 * space below a folded step which is located at the very end of an
 * overlong sequence. Overlength can result from sister sequences in
 * if/else and case steps which need more space. Without the filler, a
 * folded step at the sequence end would cause the black background panel
 * become visible at the bottom. */
public class BottomFiller extends JPanel {
}
