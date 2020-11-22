package specman.textfield;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Diese Klasse löst ein ärgerliches Grafikproblem in Swing:
 * Wenn sich ein Textfeld im Randbereich eines Schritts mit abgerundeten Ecke befinden, dann
 * muss es ein wenig eingerückt werden, damit der editierbare Bereich nicht unter der Abrundung
 * liegt. Das würde man normalerweise mit einer Border oder einem Margin für das Textfeld lösen.
 * Leider entsteht dann aber eine Unschönheit: sobald man das Textfeld anklickt und darin editiert,
 * legt Swing das Feld samt seiner Umrandung zeitweise in den Vordergrund. Die Abrundung wird also
 * für die Dauer des Editierens mit der Hintergrundfarbe der Border überdeckt. Die Klasse hier
 * löst dieses Problem. Sie ist selbst ein Panel, in dem das Textfeld über ein FormLayout auf
 * Abstand zum Rand gehalten wird. Auf diese Weise tritt der Effekt nicht auf.
 * <p>
 * Mit dem gleichen Kniff löst die Klasse das Problem, wenn sich das Textfeld am oberen oder unteren
 * Rand einer abgerundeten Umrahmung befindet. Die Rahmenlinie wird nämlich mit Antializing gezeichnet
 * (siehe Klasse {@link specman.view.RoundedBorderDecorator}, was zu einem leichten "Verschwimmen" der
 * Horizontallinien führt. In dem Fall muss das außerste Pixel des oberen bzw. unteren Randabstands
 * des Textfeldes von dem Abstandspanel hier kommen und nicht von einer Border, wei sonst die Rahmenlinien
 * während des Editierens leicht angeknabbert aussehen.
 * <p>
 * Kann man dann nicht <i>alles</i> über die Klasse hier machen, statt dem Textfeld überhaupt noch
 * eine Border zu geben? Leider Nein, denn das Textfeld besitzt ja auch noch sein Schrittnummer-Label,
 * und dieses muss am Rand seiner eigenen Border platziert werden, um bündig mit den umgebenden
 * Rahmenlinien des Schrittes platziert zu werden, zu dem das Textfeld gehört. Wir müssen also
 * situationsbedingt beide Techniken mischen.
 */
public class InsetPanel extends JPanel {
    private static final int JEDITORPANE_DEFAULT_BORDER_THICKNESS = 3;

    private FormLayout layout;
    private EmptyBorder border;
    private JEditorPane editorPane;

    InsetPanel(JEditorPane editorPane) {
        this.editorPane = editorPane;
        this.layout = new FormLayout("0px,10px:grow,0px", "0px,fill:pref:grow,0px");
        this.border = new EmptyBorder(
                JEDITORPANE_DEFAULT_BORDER_THICKNESS,
                JEDITORPANE_DEFAULT_BORDER_THICKNESS,
                JEDITORPANE_DEFAULT_BORDER_THICKNESS,
                JEDITORPANE_DEFAULT_BORDER_THICKNESS);
        setLayout(layout);
        editorPane.setBorder(border);

//    setLayout(new FormLayout("10px,10px:grow,10px", "1px,fill:pref:grow,1px"));
//    setBorder(new EmptyBorder(2, 0, 2, 0));

        add(editorPane, CC.xy(2, 2));
    }

    public void setLeftInset(int px) {
        Insets insets = border.getBorderInsets();
        insets.left = JEDITORPANE_DEFAULT_BORDER_THICKNESS + px;
        border = new EmptyBorder(insets);
        editorPane.setBorder(border);
    }

    public void setRightInset(int px) {
        Insets insets = border.getBorderInsets();
        insets.right = JEDITORPANE_DEFAULT_BORDER_THICKNESS + px;
        border = new EmptyBorder(insets);
        editorPane.setBorder(border);
    }
}
