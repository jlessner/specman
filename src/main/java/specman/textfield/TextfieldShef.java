package specman.textfield;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import net.atlanticbb.tantlinger.shef.HTMLEditorPane;
import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.Aenderungsmarkierung_V001;
import specman.model.v001.TextMitAenderungsmarkierungen_V001;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.List;

import static specman.Specman.schrittHintergrund;
import static specman.textfield.Indentions.JEDITORPANE_DEFAULT_BORDER_THICKNESS;
import static specman.textfield.TextStyles.*;

/** Zentrales grafisches Containerpanel für einen zusammenhängenden Text mit einem Nummernlabel
 * für Schrittbeschreibungen. Normalerweise besteht diese Beschreibung aus einem einzelnen HTML
 * Text-Editorbereich (siehe Klasse {@link TextEditArea}). Sollen aber in einer Beschreibung auch
 * Bilder auftauchen, dann werden diese als separate grafische Elemente in den Container aufgenommen.
 * Im Prinzip können Bilder auch in dem HTML enthalten sein (siehe Klasse {@link HTMLEditorPane}),
 * aber das hat zwei entscheidende Limitierungen. Zum einen werden die Bilder darin ziemlich pixelig
 * skaliert, und zum anderen kann man sie dabei ausschließlich im Sinne von Referenzen auf separate
 * Dateien integrieren. Die selbstgebaute Komponente {@link ImageEditArea} sorgt für eine schönere
 * Skalierung und kann die Grafiken auch ins Diagramm <i>eingebettet</i> verwalten, wie man das
 * von Microsoft Word kennt. Das ist meistens sinnvoller, weil sich ja sonst Inhalte der Beschreibung
 * verändern können, ohne dass der User an dem Dokument gearbeitet hat.
 * <p>
 * Der Container kümmert sich auch um die abgesetzte Darstellung von Schritten und um ein damit
 * zusammenhängendes ärgerliches Grafikproblem in Swing:
 * Wenn sich ein Textfeld im Randbereich eines Schritts mit abgerundeten Ecken befinden, dann
 * muss es ein wenig eingerückt werden, damit der editierbare Bereich nicht unter der Abrundung
 * liegt. Das würde man normalerweise mit einer Border oder einem Margin für das Textfeld lösen.
 * Leider entsteht dann aber eine Unschönheit: sobald man das Textfeld anklickt und darin editiert,
 * legt Swing das Feld samt seiner Umrandung zeitweise in den Vordergrund. Die Abrundung wird also
 * für die Dauer des Editierens mit der Hintergrundfarbe der Border überdeckt. Die Klasse hier
 * löst dieses Problem. Sie ist selbst ein Panel, in dem das Textfeld über ein FormLayout auf
 * Abstand zum Rand gehalten wird. Auf diese Weise tritt der Effekt nicht auf.
 * <p>
 * Mit dem gleichen Kniff löst die Klasse das Problem, wenn sich das Textfeld am oberen oder unteren
 * Rand einer abgerundeten Umrahmung befindet. Die Rahmenlinie wird nämlich mit Antialiazing gezeichnet
 * (siehe Klasse {@link specman.view.RoundedBorderDecorator}, was zu einem leichten "Verschwimmen" der
 * Horizontallinien führt. In dem Fall muss das äußerste Pixel des oberen bzw. unteren Randabstands
 * des Textfeldes von dem Abstandspanel hier kommen und nicht von einer Border, weil sonst die Rahmenlinien
 * während des Editierens leicht angeknabbert aussehen.
 * <p>
 * Kann man dann nicht <i>alles</i> über die Klasse hier machen, statt dem Textfeld überhaupt noch
 * eine Border zu geben? Leider Nein, denn das Textfeld besitzt ja auch noch sein Schrittnummer-Label,
 * und dieses muss am Rand seiner eigenen Border platziert werden, um bündig mit den umgebenden
 * Rahmenlinien des Schrittes platziert zu werden, zu dem das Textfeld gehört. Wir müssen also
 * situationsbedingt beide Techniken mischen.
 */
public class TextfieldShef extends JPanel {
	// ACHTUNG: Das ist hier noch auf halbem Wege. Später wird es eine Liste von EditAreas geben
	private final TextEditArea editorPane;
	private ImageEditArea imagePane;
	private final SchrittNummerLabel schrittNummer;
	private final FormLayout layout;
	private EmptyBorder editorPaneBorder;
	private Indentions indentions;
	boolean schrittNummerSichtbar = true;
	TextMitAenderungsmarkierungen_V001 loeschUndoBackup;

	public TextfieldShef(EditorI editor, String initialerText, String schrittId) {
		layout = new FormLayout("0px,10px:grow,0px", "0px,fill:pref:grow,fill:pref:grow,0px");
		setLayout(layout);
		editorPane = new TextEditArea(editor, initialerText);
		add(editorPane, CC.xy(2, 2));
		updateDecorationIndentions(new Indentions());
		setBackground(schrittHintergrund());

		if (schrittId != null) {
			schrittNummer = new SchrittNummerLabel(schrittId);
			editorPane.add(schrittNummer);
			setEnabled(false);
		} else {
			schrittNummer = null;
		}

    skalieren(editor.getZoomFactor(), 0);
  }

	public TextfieldShef(EditorI editor) {
		this(editor, null, null);
	}

	public void setStandardStil(SchrittID id) {
		if (loeschUndoBackup != null) {
			setText(loeschUndoBackup);
			loeschUndoBackup = null;
		}
		else {
			editorPane.setStyle(standardStil);
		}
		setBackground(Hintergrundfarbe_Standard);
		if (schrittNummer != null) {
			schrittNummer.setStandardStil(id);
		}
	}

	public void setZielschrittStil(SchrittID quellschrittId) {
		schrittNummer.setZielschrittStil(quellschrittId);
	}

	public void setQuellStil(SchrittID zielschrittID) {
		editorPane.setStyle(quellschrittStil);
		setBackground(AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
		schrittNummer.setQuellschrittStil(zielschrittID);
	}

	public void setGeloeschtMarkiertStil(SchrittID id) {
		loeschUndoBackup = getTextMitAenderungsmarkierungen(true);
		editorPane.aenderungsmarkierungenVerwerfen();
		editorPane.setStyle(ganzerSchrittGeloeschtStil);
		setBackground(AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
		editorPane.setEditable(false);
		if (schrittNummer != null) {
			schrittNummer.setGeloeschtStil(id);
		}
	}

	public void setId(String id) {
		schrittNummer.setText(id);
	}

	public void setPlainText(String plainText) {
		setPlainText(plainText, StyleConstants.ALIGN_LEFT);
	}

	public void setPlainText(String plainText, int orientation) {
		editorPane.setPlainText(plainText, orientation);
	}

	public void setText(TextMitAenderungsmarkierungen_V001 inhalt) {
		setPlainText(inhalt.text);
	}

	public TextMitAenderungsmarkierungen_V001 getTextMitAenderungsmarkierungen(boolean formatierterText) {
		return editorPane.getTextMitAenderungsmarkierungen(formatierterText);
	}

	public String getPlainText() {
		return editorPane.getPlainText();
	}

	public void updateBounds() {
		if (schrittNummer != null) {
			if (schrittNummerSichtbar) {
				Dimension schrittnummerGroesse = schrittNummer.getPreferredSize();
				schrittNummer.setBounds(editorPane.getWidth() - schrittnummerGroesse.width, 0, schrittnummerGroesse.width,
					schrittnummerGroesse.height - 2);
			} else {
				schrittNummer.setBounds(0, 0, 0, 0);
			}
		}
		if (imagePane != null) {
			imagePane.rescale(editorPane.getWidth());
		}
	}

	public void schrittnummerAnzeigen(boolean sichtbar) {
		if (schrittNummer != null) {
			schrittNummerSichtbar = sichtbar;
			updateBounds(); // Sorgt dafür, dass der Label auch optisch sofort verschwindet
		}
	}

	public void skalieren(int prozentNeu, int prozentAktuell) {
		editorPane.setFont(font.deriveFont((float) FONTSIZE * prozentNeu / 100));
		if (schrittNummer != null) {
			schrittNummer.setFont(labelFont.deriveFont((float) SCHRITTNR_FONTSIZE * prozentNeu / 100));
		}
		// prozentAktuell = 0 ist ein Indikator für initiales Laden. Da brauchen wir nur den Font
		// anpassen. Die Bilder stehen bereits entsprechend des im Modell abgespeicherten Zoomfaktors
		// skaliert im HTML.
		if (prozentAktuell != 0 && prozentNeu != prozentAktuell) {
			ImageScaler imageScaler = new ImageScaler(prozentNeu, prozentAktuell);
			editorPane.setText(imageScaler.scaleImages(editorPane.getText()));
		}
		updateDecorationIndentions(indentions);
	}

	public static String right(String text) {
		return "<div align='right'>" + Specman.initialtext(text) + "</div>";
	}

	public static String center(String text) {
		return "<div align='center'>" + Specman.initialtext(text) + "</div>";
	}

	public String getText() { return editorPane.getText(); }
	public JTextComponent getTextComponent() { return editorPane; }
	public void addFocusListener(FocusListener focusListener) { editorPane.addFocusListener(focusListener); }
	public void requestFocus() { editorPane.requestFocus(); }

	public void setLeftInset(int px) {
		Insets insets = editorPaneBorder.getBorderInsets();
		insets.left = JEDITORPANE_DEFAULT_BORDER_THICKNESS + px;
		editorPaneBorder = new EmptyBorder(insets);
		editorPane.setBorder(editorPaneBorder);
	}

	public void setRightInset(int px) {
		Insets insets = editorPaneBorder.getBorderInsets();
		insets.right = JEDITORPANE_DEFAULT_BORDER_THICKNESS + px;
		editorPaneBorder = new EmptyBorder(insets);
		editorPane.setBorder(editorPaneBorder);
	}

	@Override
	public void setOpaque(boolean isOpaque) {
		super.setOpaque(isOpaque);
		// Null-Check ist notwendig, weil javax.swing.LookAndFeel bereits
		// im Konstruktor einen Aufruf von setOpaque vornimmt.
		if (editorPane != null) {
			editorPane.setOpaque(isOpaque);
		}
	}

	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		// Null-Check ist notwendig, weil javax.swing.LookAndFeel bereits
		// im Konstruktor einen Aufruf von setBackground vornimmt.
		if (editorPane != null) {
			editorPane.setBackground(bg);
		}
	}

	@Override
	public Color getBackground() {
		// Null-Check ist notwendig, weil javax.swing.LookAndFeel bereits
		// im Konstruktor einen Aufruf von getBackground vornimmt.
		return (editorPane != null) ? editorPane.getBackground() : super.getBackground();
	}

	public void updateDecorationIndentions(Indentions indentions) {
		this.indentions = indentions;

		layout.setRowSpec(1, indentions.topInset());
		layout.setRowSpec(4, indentions.bottomInset());
		layout.setColumnSpec(1, indentions.leftInset());
		layout.setColumnSpec(3, indentions.rightInset());

		setEditorBorder(
			indentions.topBorder(),
			indentions.leftBorder(),
			indentions.bottomBorder(),
			indentions.rightBorder());
	}

	private void setEditorBorder(int top, int left, int bottom, int right) {
		this.editorPaneBorder = new EmptyBorder(top, left, bottom, right);
		editorPane.setBorder(editorPaneBorder);
	}

	//TODO
	public JEditorPane getEditorPane() { return editorPane; }

	public Rectangle getStepNumberBounds() { return schrittNummer.getBounds(); }

	public void wrapSchrittnummerAsDeleted() { schrittNummer.wrapAsDeleted(); }
	public void wrapSchrittnummerAsZiel(SchrittID quellschrittId) { schrittNummer.wrapAsZiel(quellschrittId); }
	public void wrapSchrittnummerAsQuelle(SchrittID zielschrittID) { schrittNummer.wrapAsQuelle(zielschrittID); }

	public void aenderungsmarkierungenUebernehmen() { editorPane.aenderungsmarkierungenUebernehmen(); }
	public void aenderungsmarkierungenVerwerfen() { editorPane.aenderungsmarkierungenVerwerfen(); }
	public List<Aenderungsmarkierung_V001> findeAenderungsmarkierungen(boolean nurErste) {
		return editorPane.findeAenderungsmarkierungen(nurErste);
	}

	public boolean enthaelt(InteractiveStepFragment fragment) {
		return editorPane == fragment || schrittNummer == fragment;
	}

	public InteractiveStepFragment asInteractiveFragment() { return editorPane; }

	public void addImage(File imageFile) {
		imagePane = new ImageEditArea(imageFile);
		add(imagePane, CC.xy(2, 3));
		updateBounds();
	}
}
