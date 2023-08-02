package specman.textfield;

import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.Aenderungsmarkierung_V001;
import specman.model.v001.GeloeschtMarkierung_V001;
import specman.model.v001.TextMitAenderungsmarkierungen_V001;
import specman.view.AbstractSchrittView;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.text.*;
import javax.swing.text.html.CSS;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.util.ArrayList;
import java.util.List;

import static specman.Specman.schrittHintergrund;
import static specman.textfield.TextStyles.*;

public class TextfieldShef implements ComponentListener, KeyListener {
	private final InsetPanel insetPanel;
	private final JEditorPane editorPane;
	private final SchrittNummerLabel schrittNummer;
	boolean schrittNummerSichtbar = true;
	TextMitAenderungsmarkierungen_V001 loeschUndoBackup;

	public TextfieldShef(EditorI editor, String initialerText, String schrittId) {
		editorPane = new JEditorPane();
		insetPanel = new InsetPanel(editorPane, this);
		editor.instrumentWysEditor(editorPane, initialerText, 0);
		editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		editorPane.setFont(font);
		setBackground(schrittHintergrund());
		editorPane.addKeyListener(this);

		if (schrittId != null) {
			schrittNummer = new SchrittNummerLabel(schrittId);
			editorPane.add(schrittNummer);
			editorPane.addComponentListener(this);
			insetPanel.setEnabled(false);
		} else {
			schrittNummer = null;
		}

		if (editor != null) {
			skalieren(editor.getZoomFactor(), 0);
		}
	}

	public TextfieldShef(EditorI editor) {
		this(editor, null, null);
	}

	public void setStyle(String text, MutableAttributeSet attr) {
		StyledDocument doc = (StyledDocument) editorPane.getDocument();
		doc.setCharacterAttributes(0, text.length(), attr, false);
	}

	public void setStandardStil(SchrittID id) {
		if (loeschUndoBackup != null) {
			setText(loeschUndoBackup);
			loeschUndoBackup = null;
		}
		else {
			setStyle(getPlainText(), standardStil);
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
		setStyle(getPlainText(), quellschrittStil);
		setBackground(AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
		schrittNummer.setQuellschrittStil(zielschrittID);
	}

	public void setGeloeschtMarkiertStil(SchrittID id) {
		loeschUndoBackup = getTextMitAenderungsmarkierungen(true);
		aenderungsmarkierungenVerwerfen();
		setStyle(getPlainText(), ganzerSchrittGeloeschtStil);
		setBackground(AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
		getTextComponent().setEditable(false);
		if (schrittNummer != null) {
			schrittNummer.setGeloeschtStil(id);
		}
	}

	public boolean ganzerSchrittGeloeschtStilGesetzt() {
		StyledEditorKit k = (StyledEditorKit) editorPane.getEditorKit();
		MutableAttributeSet inputAttributes = k.getInputAttributes();
		Object currentTextDecoration = inputAttributes.getAttribute(CSS.Attribute.TEXT_DECORATION);
		Object currentFontColorValue  = inputAttributes.getAttribute(CSS.Attribute.COLOR);
		if (currentTextDecoration != null && currentTextDecoration.toString().equals(INDIKATOR_GELOESCHT_MARKIERT)
				&& currentFontColorValue!=null &&currentFontColorValue.toString().equals(INDIKATOR_GRAU))
			return false && currentFontColorValue!=null && currentFontColorValue.toString().equals(INDIKATOR_GRAU);
		Object currentBackgroundColorValue = inputAttributes.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
		return currentBackgroundColorValue != null
				&& currentBackgroundColorValue.toString().equalsIgnoreCase(INDIKATOR_SCHWARZ)
				&& currentTextDecoration != null
				&& currentTextDecoration.toString().equalsIgnoreCase(INDIKATOR_GELOESCHT_MARKIERT)
				&& currentFontColorValue != null && currentFontColorValue.toString().equalsIgnoreCase(INDIKATOR_GRAU);
	}

	public void ganzerSchrittStandardStilSetzenWennNochNichtVorhanden() {
		if (!ganzerSchrittGeloeschtStilGesetzt()) {
			StyledEditorKit k = (StyledEditorKit) editorPane.getEditorKit();
			MutableAttributeSet inputAttributes = k.getInputAttributes();
			inputAttributes.addAttributes(standardStil);
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
		switch (orientation) {
			case StyleConstants.ALIGN_CENTER:
				editorPane.setText("<div align='center'>" + plainText + "</div>");
				break;
			case StyleConstants.ALIGN_RIGHT:
				editorPane.setText("<div align='right'>" + plainText + "</div>");
				break;
			default:
				editorPane.setText(plainText);
		}
	}

	public void setText(TextMitAenderungsmarkierungen_V001 inhalt) {
		setPlainText(inhalt.text);
		setAenderungsmarkierungen(inhalt.aenderungen);
	}

	public TextMitAenderungsmarkierungen_V001 getTextMitAenderungsmarkierungen(boolean formatierterText) {
		String text;
		java.util.List<Aenderungsmarkierung_V001> aenderungen = null;
		if (formatierterText) {
			// Wenn wir die Zeilenumbrüche nicht rausnehmen, dann entstehen später beim Laden u.U.
			// Leerzeichen an Zeilenenden, die im ursprünglichen Text nicht drin waren. Das ist doof,
			// weil dann die separat abgespeicherten Textintervalle der Aenderungsmarkierungen nicht mehr passen.
			text = editorPane.getText().replace("\n", "");
			aenderungen = findeAenderungsmarkierungen(false);
		} else {
			text = getPlainText().replace("\n", " ").trim();
		}
		return new TextMitAenderungsmarkierungen_V001(text, aenderungen);
	}

	public String getPlainText() {
		try {
			return editorPane.getText(0, editorPane.getDocument().getLength());
		} catch (BadLocationException blx) {
			blx.printStackTrace();
			return null;
		}
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		Dimension schrittnummerGroesse = schrittNummer.getPreferredSize();
		if (schrittNummerSichtbar)
			schrittNummer.setBounds(editorPane.getWidth() - schrittnummerGroesse.width, 0, schrittnummerGroesse.width,
					schrittnummerGroesse.height - 2);
		else
			schrittNummer.setBounds(0, 0, 0, 0);
	}

	public void schrittnummerAnzeigen(boolean sichtbar) {
		if (schrittNummer != null) {
			schrittNummerSichtbar = sichtbar;
			componentResized(null); // Sorgt dafÃ¯Â¿Â½r, dass der Label auch optisch sofort verschwindet
		}
	}

	//TODO Zielschrittmarkierungen
	public java.util.List<Aenderungsmarkierung_V001> findeAenderungsmarkierungen(boolean nurErste) {
		java.util.List<Aenderungsmarkierung_V001> ergebnis = new ArrayList<Aenderungsmarkierung_V001>();
		StyledDocument doc = (StyledDocument)editorPane.getDocument();
		for (Element e: doc.getRootElements()) {
			findeAenderungsmarkierungen(e, ergebnis, nurErste);
			if (ergebnis.size() > 0 && nurErste)
				break;
		}
		return ergebnis;
	}

	private void findeAenderungsmarkierungen(Element e, java.util.List<Aenderungsmarkierung_V001> ergebnis, boolean nurErste) {
		if (elementHatAenderungshintergrund(e)) {
			ergebnis.add(new Aenderungsmarkierung_V001(e.getStartOffset(), e.getEndOffset()));
			if (ergebnis.size() > 0 && nurErste)
				return;
		}
		if (ergebnis.size() == 0 || !nurErste) {
			for (int i = 0; i < e.getElementCount(); i++) {
				findeAenderungsmarkierungen(e.getElement(i), ergebnis, nurErste);
				if (ergebnis.size() > 0 && nurErste)
					break;
			}
		}
	}

	// TODO JL: Muss mit aenderungsmarkierungenVerwerfen zusammengelegt werden
	public java.util.List<Aenderungsmarkierung_V001> aenderungsmarkierungenUebernehmen() {
		java.util.List<Aenderungsmarkierung_V001> ergebnis = new ArrayList<>();
		StyledDocument doc = (StyledDocument) editorPane.getDocument();
		List <GeloeschtMarkierung_V001> loeschungen = new ArrayList<>();
		for (Element e : doc.getRootElements()) {
			aenderungsmarkierungenUebernehmen(e, ergebnis, loeschungen);
		}
		for (int i = 0; i < loeschungen.size();i++){
			GeloeschtMarkierung_V001 loeschung = loeschungen.get((loeschungen.size()) -1 - i);
			try{
				doc.remove(loeschung.getVon(), loeschung.getBis());
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		return ergebnis;
	}

	// TODO JL: Muss mit aenderungsmarkierungenUebernehmen zusammengelegt werden
	public java.util.List<Aenderungsmarkierung_V001> aenderungsmarkierungenVerwerfen() {
		java.util.List<Aenderungsmarkierung_V001> ergebnis = new ArrayList<>();
		StyledDocument doc = (StyledDocument) editorPane.getDocument();
		List <GeloeschtMarkierung_V001> loeschungen = new ArrayList<>();
		for (Element e : doc.getRootElements()) {
			aenderungsmarkierungenVerwerfen(e, ergebnis, loeschungen);
		}
		for (int i = 0; i < loeschungen.size();i++){
			GeloeschtMarkierung_V001 loeschung = loeschungen.get((loeschungen.size()) -1 - i);
			try{
				doc.remove(loeschung.getVon(), loeschung.getBis());
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		return ergebnis;
	}

	// TODO JL: Muss mit aenderungsmarkierungenVerwerfen zusammengelegt werden
	private void aenderungsmarkierungenUebernehmen(
			Element e,
			List<Aenderungsmarkierung_V001> ergebnis,
			List <GeloeschtMarkierung_V001> loeschungen) {
		StyledDocument doc = (StyledDocument) e.getDocument();
		if (elementHatAenderungshintergrund(e)) {
			if (elementHatDurchgestrichenenText(e)){
				loeschungen.add(new GeloeschtMarkierung_V001(e.getStartOffset(), e.getEndOffset()-e.getStartOffset()));
			}
			else{
				AttributeSet attribute = e.getAttributes();
				MutableAttributeSet entfaerbt = new SimpleAttributeSet();
				entfaerbt.addAttributes(attribute);
				StyleConstants.setBackground(entfaerbt, Color.white);
				doc.setCharacterAttributes(e.getStartOffset(),e.getEndOffset()-e.getStartOffset(),entfaerbt,true);
			}
			ergebnis.add(new Aenderungsmarkierung_V001(e.getStartOffset(), e.getEndOffset()));
		}

		for (int i = 0; i < e.getElementCount(); i++) {
			aenderungsmarkierungenUebernehmen(e.getElement(i), ergebnis, loeschungen);
		}
	}

	// TODO JL: Muss mit aenderungsmarkierungenUebernehmen zusammengelegt werden
	private void aenderungsmarkierungenVerwerfen(
			Element e,
			List<Aenderungsmarkierung_V001> ergebnis,
			List <GeloeschtMarkierung_V001> loeschungen) {
		StyledDocument doc = (StyledDocument) e.getDocument();
		if (elementHatAenderungshintergrund(e)) {
			if (!elementHatDurchgestrichenenText(e)){
				loeschungen.add(new GeloeschtMarkierung_V001(e.getStartOffset(), e.getEndOffset()-e.getStartOffset()));
			}
			else{
				AttributeSet attribute = e.getAttributes();
				MutableAttributeSet entfaerbt = new SimpleAttributeSet();
				entfaerbt.addAttributes(attribute);
				StyleConstants.setBackground(entfaerbt, Color.white);
				StyleConstants.setStrikeThrough(entfaerbt, false);
				doc.setCharacterAttributes(e.getStartOffset(),e.getEndOffset()-e.getStartOffset(),entfaerbt,true);
			}
			ergebnis.add(new Aenderungsmarkierung_V001(e.getStartOffset(), e.getEndOffset()));
		}
		for (int i = 0; i < e.getElementCount(); i++) {
			aenderungsmarkierungenVerwerfen(e.getElement(i), ergebnis, loeschungen);
		}
	}

	private boolean elementHatDurchgestrichenenText (Element e){
		AttributeSet attr = e.getAttributes();
		return StyleConstants.isStrikeThrough(attr);
	}

	private boolean elementHatAenderungshintergrund(Element e) {
		AttributeSet attr = e.getAttributes();
		Object backgroundColorValue = attr.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
		return (backgroundColorValue != null && backgroundColorValue.toString().equals(INDIKATOR_GELB));
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

	@Override
	public void keyPressed(KeyEvent e) {
		if (Specman.instance().aenderungenVerfolgen() && Specman.instance().hauptSequenz.findeSchritt(editorPane).getText().isEditable()) {
			//Specman.instance().hauptSequenz.findeSchritt(editorPane).setAenderungsart(Aenderungsart.Bearbeitet);
			StyledDocument doc = (StyledDocument) editorPane.getDocument();
			int p0 = editorPane.getSelectionStart();
			int p1 = editorPane.getSelectionEnd();
			if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
				if (p0 != p1) {
					// Eigentlich muss man das hier komplizierter machen. Sind nÃƒÂ¤mlich in der
					// Selektion Zeichen enthalten, die als geÃƒÂ¤ndert markiert sind, dann muss
					// man diese entfernen statt sie als gelÃƒÂ¶scht zu markieren.
					editorPane.setCaretPosition(p0);
					doc.setCharacterAttributes(p0, p1 - p0, geloeschtStil, false);
				} else {
					if (aenderungsStilGesetzt())
						return;
					int caretPos = editorPane.getCaretPosition();
					if (caretPos > 0) {
						editorPane.setCaretPosition(caretPos - 1);
						doc.setCharacterAttributes(caretPos - 1, 1, geloeschtStil, false);
					}
				}
				e.consume();
				return;
			}
			aenderungsStilSetzenWennNochNichtVorhanden();
		} else{

			ganzerSchrittStandardStilSetzenWennNochNichtVorhanden();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (!Specman.instance().aenderungenVerfolgen()) {
			return;
		}
		AbstractSchrittView textOwner = Specman.instance().hauptSequenz.findeSchritt(editorPane);
		if (textOwner != null && textOwner.getText().isEditable()) {
			StyledDocument doc = (StyledDocument) editorPane.getDocument();
			int p0 = editorPane.getSelectionStart();
			int p1 = editorPane.getSelectionEnd();
			if (p0 != p1) {
				editorPane.setCaretPosition(p0);
				doc.setCharacterAttributes(p0, p1 - p0, geloeschtStil, false);
				editorPane.setSelectionStart(p1);
				// Jetzt ist am Ende der vorherigen Selektion noch der Geloescht-Stil gesetzt
				// D.h. die Durchstreichung muss noch weg fÃ¯Â¿Â½r das neue Zeichen, das grade
				// eingefÃ¯Â¿Â½gt werden soll
				StyledEditorKit k = (StyledEditorKit)editorPane.getEditorKit();
				MutableAttributeSet inputAttributes = k.getInputAttributes();
				StyleConstants.setStrikeThrough(inputAttributes, false);
			}
		}
	}

	private void aenderungsStilSetzenWennNochNichtVorhanden() {
		// Durch die folgende If-Abfrage verhindert man, dass die als geÃƒÂ¤ndert
		// markierten Buchstaben
		// alle einzelne Elements werden. Wenn an der aktuellen Position schon gelbe
		// Hintegrundfarbe
		// eingestellt ist, dann ÃƒÂ¤ndern wir den aktuellen Style gar nicht mehr.
		if (!aenderungsStilGesetzt()) {
			StyledEditorKit k = (StyledEditorKit) editorPane.getEditorKit();
			MutableAttributeSet inputAttributes = k.getInputAttributes();
			StyleConstants.setStrikeThrough(inputAttributes, false); // Falls noch Geloescht-Stil herrschte
			inputAttributes.addAttributes(geaendertStil);
		}
	}

	public boolean aenderungsStilGesetzt() {
		StyledEditorKit k = (StyledEditorKit) editorPane.getEditorKit();
		MutableAttributeSet inputAttributes = k.getInputAttributes();
		Object currentTextDecoration = inputAttributes.getAttribute(CSS.Attribute.TEXT_DECORATION);
		if (currentTextDecoration != null && currentTextDecoration.toString().equals(INDIKATOR_GELOESCHT_MARKIERT))
			return false;
		Object currentBackgroundColorValue = inputAttributes.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
		return currentBackgroundColorValue != null
				&& currentBackgroundColorValue.toString().equalsIgnoreCase(INDIKATOR_GELB);
	}

	@Override
	public void keyReleased(KeyEvent e) {
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

	public java.util.List<Line2D.Double> getRechteZeilenraender() {
		java.util.List<Line2D.Double> raender = new ArrayList<Line2D.Double>();
		try {
			int anzahlZeichen = getPlainText().length();
			int offset = 1;
			while (offset < anzahlZeichen) {
				System.out.println(offset);
				offset = Utilities.getRowEnd(editorPane, offset) + 1;
				Rectangle r = editorPane.modelToView(offset - 1);
				Line2D.Double randLinie = new Line2D.Double(r.getX(), r.getY(), r.getX(), r.getMaxY());
				raender.add(randLinie);
			}
		} catch (BadLocationException blx) {
			blx.printStackTrace();
		}
		return raender;
	}

	public java.util.List<Line2D.Double> getLinkeZeilenraender() {
		List<Line2D.Double> raender = new ArrayList<Double>();
		try {
			int anzahlZeichen = getPlainText().length();
			int offset = anzahlZeichen;
			while (offset > 0) {
				System.out.println(offset);
				offset = Utilities.getRowStart(editorPane, offset) - 1;
				Rectangle r = editorPane.modelToView(offset + 1);
				Line2D.Double randLinie = new Line2D.Double(r.getX(), r.getY(), r.getX(), r.getMaxY());
				raender.add(randLinie);
			}
		} catch (BadLocationException blx) {
			blx.printStackTrace();
		}
		return raender;
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

	public void wrapSchrittnummerAsDeleted() { schrittNummer.wrapAsDeleted(); }
	public void wrapSchrittnummerAsZiel(SchrittID quellschrittId) { schrittNummer.wrapAsZiel(quellschrittId); }
	public void wrapSchrittnummerAsQuelle(SchrittID zielschrittID) { schrittNummer.wrapAsQuelle(zielschrittID); }

}
