package specman.textfield;

import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.Aenderungsmarkierung_V001;
import specman.model.v001.TextMitAenderungsmarkierungen_V001;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.FocusListener;
import java.util.List;

import static specman.Specman.schrittHintergrund;
import static specman.textfield.TextStyles.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE;
import static specman.textfield.TextStyles.FONTSIZE;
import static specman.textfield.TextStyles.Hintergrundfarbe_Standard;
import static specman.textfield.TextStyles.SCHRITTNR_FONTSIZE;
import static specman.textfield.TextStyles.font;
import static specman.textfield.TextStyles.ganzerSchrittGeloeschtStil;
import static specman.textfield.TextStyles.labelFont;
import static specman.textfield.TextStyles.quellschrittStil;
import static specman.textfield.TextStyles.standardStil;

public class TextfieldShef {
	private final InsetPanel insetPanel;
	private final TextEditArea editorPane;
	private final SchrittNummerLabel schrittNummer;
	boolean schrittNummerSichtbar = true;
	TextMitAenderungsmarkierungen_V001 loeschUndoBackup;

	public TextfieldShef(EditorI editor, String initialerText, String schrittId) {
		editorPane = new TextEditArea(editor, initialerText);
		insetPanel = new InsetPanel(editorPane, this);
		setBackground(schrittHintergrund());

		if (schrittId != null) {
			schrittNummer = new SchrittNummerLabel(schrittId);
			editorPane.add(schrittNummer);
			insetPanel.setEnabled(false);
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
		getTextComponent().setEditable(false);
		if (schrittNummer != null) {
			schrittNummer.setGeloeschtStil(id);
		}
	}

	public void setBackground(Color bg) {
		insetPanel.setBackground(bg);
		// Null-Check ist notwendig, weil javax.swing.LookAndFeel bereits
		// im Konstruktor einen Aufruf von setBackground vornimmt.
		if (editorPane != null) {
			editorPane.setBackground(bg);
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
		setAenderungsmarkierungen(inhalt.aenderungen);
	}

	public TextMitAenderungsmarkierungen_V001 getTextMitAenderungsmarkierungen(boolean formatierterText) {
		return editorPane.getTextMitAenderungsmarkierungen(formatierterText);
	}

	public String getPlainText() {
		return editorPane.getPlainText();
	}

	public void updateBounds() {
		Dimension schrittnummerGroesse = schrittNummer.getPreferredSize();
		if (schrittNummerSichtbar) {
      schrittNummer.setBounds(editorPane.getWidth() - schrittnummerGroesse.width, 0, schrittnummerGroesse.width,
          schrittnummerGroesse.height - 2);
    } else {
      schrittNummer.setBounds(0, 0, 0, 0);
    }
	}

	public void schrittnummerAnzeigen(boolean sichtbar) {
		if (schrittNummer != null) {
			schrittNummerSichtbar = sichtbar;
			updateBounds(); // Sorgt dafür, dass der Label auch optisch sofort verschwindet
		}
	}

	public void setAenderungsmarkierungen(java.util.List<Aenderungsmarkierung_V001> aenderungen) {
		// TODO JL: Brauchen wir aktuell nicht mehr. Das war nötig, weil die Hintergrundfarbe nicht
		// im abgespeicherten HTML erhalten blieb. Das ist jetzt dank des Tricks aus
		// https://stackoverflow.com/questions/13285526/jtextpane-text-background-color-does-not-work
		// der Fall. Die Funktion kann also evt. weg, sofern wir aus den HTML-Formatierungen allein
		// alle die Ãnderungsinformationen vollständig wieder auslesen können.
		// StyledDocument doc = (StyledDocument)getDocument();
		// MutableAttributeSet attr = new SimpleAttributeSet();
		// StyleConstants.setBackground(attr, AENDERUNGSMARKIERUNG_FARBE);
		// for (Aenderungsmarkierung aenderung: aenderungen) {
		// doc.setCharacterAttributes(aenderung.getVon(), aenderung.laenge(), attr,
		// false);
		// }
	}

	public void skalieren(int prozentNeu, int prozentAktuell) {
		editorPane.setFont(font.deriveFont((float) FONTSIZE * prozentNeu / 100));
		if (schrittNummer != null) {
			schrittNummer.setFont(labelFont.deriveFont((float) SCHRITTNR_FONTSIZE * prozentNeu / 100));
		}
		// prozentAktuell = 0 ist ein Indikator für initiales Laden. Da brauchen wir
		// nur den Font
		// anpassen. Die Bilder stehen bereits entsprechend des im Modell
		// abgespeicherten Zoomfaktors
		// skaliert im HTML.
		if (prozentAktuell != 0 && prozentNeu != prozentAktuell) {
			ImageScaler imageScaler = new ImageScaler(prozentNeu, prozentAktuell);
			editorPane.setText(imageScaler.scaleImages(editorPane.getText()));
		}
		insetPanel.skalieren(prozentNeu);
	}

	public static String right(String text) {
		return "<div align='right'>" + Specman.initialtext(text) + "</div>";
	}

	public static String center(String text) {
		return "<div align='center'>" + Specman.initialtext(text) + "</div>";
	}

	public String getText() {
		return editorPane.getText();
	}

	public JTextComponent getTextComponent() {
		return editorPane;
	}

	public void setLeftInset(int px) {
		insetPanel.setLeftInset(px);
	}

	public void setRightInset(int px) {
		insetPanel.setRightInset(px);
	}

	public Color getBackground() {
		return editorPane.getBackground();
	}

	public JComponent asJComponent() {
		return insetPanel;
	}

	public int getX() {
		return insetPanel.getX();
	}

	public int getY() {
		return insetPanel.getY();
	}

	public int getHeight() {
		return insetPanel.getHeight();
	}

	public int getWidth() {
		return insetPanel.getWidth();
	}

	public void addFocusListener(FocusListener focusListener) {
		editorPane.addFocusListener(focusListener);
	}

	public void requestFocus() {
		editorPane.requestFocus();
	}

	public void setOpaque(boolean isOpaque) {
		editorPane.setOpaque(isOpaque);
		insetPanel.setOpaque(isOpaque);
	}

	public void updateDecorationIndentions(Indentions indentForDecoration) {
		insetPanel.updateDecorationIndentions(indentForDecoration);
	}


	//TODO
	public InsetPanel getInsetPanel() {
		return insetPanel;
	}

	public JEditorPane getEditorPane() {
		return editorPane;
	}

	public Rectangle getStepNumberBounds() {
		return schrittNummer.getBounds();
	}

	public void wrapSchrittnummerAsDeleted() { schrittNummer.wrapAsDeleted(); }
	public void wrapSchrittnummerAsZiel(SchrittID quellschrittId) { schrittNummer.wrapAsZiel(quellschrittId); }
	public void wrapSchrittnummerAsQuelle(SchrittID zielschrittID) { schrittNummer.wrapAsQuelle(zielschrittID); }

	public void aenderungsmarkierungenUebernehmen() { editorPane.aenderungsmarkierungenUebernehmen(); }
	public void aenderungsmarkierungenVerwerfen() { editorPane.aenderungsmarkierungenVerwerfen(); }
	public List<Aenderungsmarkierung_V001> findeAenderungsmarkierungen(boolean nurErste) {
		return editorPane.findeAenderungsmarkierungen(nurErste);
	}
}
