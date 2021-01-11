package specman.textfield;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import specman.EditorI;
import specman.Specman;
import specman.Aenderungsart;
import specman.draganddrop.DragButtonAdapter;
import specman.model.v001.Aenderungsmarkierung_V001;
import specman.model.v001.TextMitAenderungsmarkierungen_V001;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.text.*;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.util.ArrayList;
import java.util.List;

import static specman.Specman.schrittHintergrund;

public class TextfieldShef implements ComponentListener, KeyListener {
	public static final Color Schriftfarbe_Geloescht = Color.LIGHT_GRAY;
	public static final Color Hintergrundfarbe_Geloescht = Color.BLACK;
	public static final Color Schriftfarbe_Standard = Color.BLACK;
	public static final Color Hintergrundfarbe_Standard = Color.WHITE;
	public static final Color AENDERUNGSMARKIERUNG_FARBE = Color.yellow;
	public static final Color AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE = new Color(255, 255, 200);
	public static final String INDIKATOR_GELB = getHTMLColor(AENDERUNGSMARKIERUNG_FARBE);
	public static final String INDIKATOR_AENDERUNGSMARKIERUNG = CSS.Attribute.BACKGROUND_COLOR + ": " + INDIKATOR_GELB;
	public static final String INDIKATOR_GELOESCHT_MARKIERT = "line-through";
	public static final String INDIKATOR_WEISS = getHTMLColor(Hintergrundfarbe_Standard);
	public static final String INDIKATOR_KEINEMARKIERUNG = CSS.Attribute.BACKGROUND_COLOR + ": " + INDIKATOR_WEISS;
	public static final String INDIKATOR_STANDARD = "none";
	public static final int FONTSIZE = 15;
	public static final int SCHRITTNR_FONTSIZE = 10;

	public static MutableAttributeSet geaendertStil = new SimpleAttributeSet();
	public static MutableAttributeSet geloeschtStil = new SimpleAttributeSet();
	public static MutableAttributeSet ganzerSchrittGeloeschtStil = new SimpleAttributeSet();
	public static MutableAttributeSet standardStil = new SimpleAttributeSet();
	public static MutableAttributeSet quellschrittStil = new SimpleAttributeSet();
	public static MutableAttributeSet zielschrittStil  =new SimpleAttributeSet();

	public static Font font = new Font(Font.SERIF, Font.PLAIN, FONTSIZE);
	public static Font labelFont = new Font(Font.SANS_SERIF, Font.BOLD, SCHRITTNR_FONTSIZE);

	public static final Color SCHRITTNUMMER_HINTERGRUNDFARBE = Color.LIGHT_GRAY;
	public static final Color SCHRITTNUMMER_HINTERGRUNDFARBE2 = Color.BLACK;
	public static final Color NEUMARKIERUNG_HINTERGRUNDFARBE = new Color(255, 255, 153);
	public static final String INDIKATOR_GRAU = getHTMLColor(SCHRITTNUMMER_HINTERGRUNDFARBE);
	public static final String INDIKATOR_SCHWARZ = getHTMLColor(SCHRITTNUMMER_HINTERGRUNDFARBE2);

	static {
		// Das hier ist ein bisschen tricky:
		// Die Zeile mit StyleConstants.setBackground sorgt dafÃƒÂ¼r, dass man die
		// Hintergrundfarbe unmittelbar
		// beim Editieren in der Oberflche sieht. Allerdings taucht sie dann nicht im
		// abgespeicherten HTML
		// auf und geht auch verloren, sobald man einen Zeilenumbruch im Text
		// einfÃƒÂ¼gt. Also braucht man noch
		// ein weiteres, persistentes Styling ÃƒÂ¼ber ein Span-Tag, wie ich es hier
		// gefunden habe:
		// https://stackoverflow.com/questions/13285526/jtextpane-text-background-color-does-not-work
		String htmlStyle = "background-color:" + getHTMLColor(Color.yellow);
		String htmlStyleSchwarz = "background-color:" + getHTMLColor(Color.black);
		String htmlStyleStandard = "background-color:" + getHTMLColor(Color.white);

		SimpleAttributeSet htmlHintergrundStyle = new SimpleAttributeSet();
		SimpleAttributeSet htmlHintergrundStyleSchwarz = new SimpleAttributeSet();
		SimpleAttributeSet htmlHintergrundStyleStandard = new SimpleAttributeSet();

		htmlHintergrundStyle.addAttribute(HTML.Attribute.STYLE, htmlStyle);
		geaendertStil.addAttribute(HTML.Tag.SPAN, htmlHintergrundStyle);
		StyleConstants.setBackground(geaendertStil, AENDERUNGSMARKIERUNG_FARBE);

		// neue Stile (null, hinzugefuegt, geloescht)
		geloeschtStil.addAttribute(HTML.Tag.SPAN, htmlHintergrundStyle);
		StyleConstants.setBackground(geloeschtStil, AENDERUNGSMARKIERUNG_FARBE);
		StyleConstants.setStrikeThrough(geloeschtStil, true);

		// neuer Static Stil
		/**
		 * Geloescht Stil - Foreground (RGB (0,0,0)) - Background (RGB(255,255,153)) -
		 * Fontcolor (RGB(166,166,166)) - StrikeThrough true
		 */
		htmlHintergrundStyleSchwarz.addAttribute(HTML.Attribute.STYLE, htmlStyleSchwarz);
		ganzerSchrittGeloeschtStil.addAttribute(HTML.Tag.SPAN, htmlHintergrundStyleSchwarz);
		StyleConstants.setBackground(ganzerSchrittGeloeschtStil, Hintergrundfarbe_Geloescht);
		StyleConstants.setStrikeThrough(ganzerSchrittGeloeschtStil, true);
		StyleConstants.setForeground(ganzerSchrittGeloeschtStil, Schriftfarbe_Geloescht);

		// Standard Style
		htmlHintergrundStyleStandard.addAttribute(HTML.Attribute.STYLE, htmlStyleStandard);
		standardStil.addAttribute(HTML.Tag.SPAN, htmlHintergrundStyleStandard);
		StyleConstants.setBackground(standardStil, Hintergrundfarbe_Standard);
		StyleConstants.setStrikeThrough(standardStil, false);
		StyleConstants.setForeground(standardStil, Schriftfarbe_Standard);

		//Quellschritt  || der verschobene Schritt ist im StandardStil!!!
		quellschrittStil.addAttribute(HTML.Tag.SPAN, htmlHintergrundStyle);
		StyleConstants.setBackground(quellschrittStil, AENDERUNGSMARKIERUNG_FARBE);
		StyleConstants.setStrikeThrough(quellschrittStil, true);
		StyleConstants.setForeground(quellschrittStil, Schriftfarbe_Geloescht);
		StyleConstants.setFontSize(quellschrittStil, 5);
	}

	public void setStyle(String text, MutableAttributeSet attr) {
		StyledDocument doc = (StyledDocument) editorPane.getDocument();
		doc.setCharacterAttributes(0, text.length(), attr, false);
		if (standardStil.equals(attr)) {
			ganzerSchrittStandardStilGesetzt();
		} else if (geloeschtStil.equals(attr)) {
			geloeschtStilGesetzt();
		} else if (geaendertStil.equals(attr)) {
			aenderungsStilGesetzt();
		}
	}

	public void setGeloeschtStil(String text) {
		setStyle(text, ganzerSchrittGeloeschtStil);
		setBackground(AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
		schrittNummer.setText("<html><body><span style='text-decoration: line-through;'>"+schrittNummer.getText()+"</span></body></html>");
		schrittNummer.setBorder(new MatteBorder(0, 2, 1, 1, TextfieldShef.Hintergrundfarbe_Geloescht));
		schrittNummer.setBackground(TextfieldShef.Hintergrundfarbe_Geloescht);
		schrittNummer.setForeground(TextfieldShef.Schriftfarbe_Geloescht);
	}
	public void setZielschrittStil(String text) {
		setStyle(text, standardStil);
		setBackground(Hintergrundfarbe_Standard);
		//Text noch editieren
		//schrittNummer.setText("<html><body><span style='text-decoration: line-through;'>"+schrittNummer.getText()+"</span></body></html>");
		schrittNummer.setBorder(new MatteBorder(0, 2, 1, 1, TextfieldShef.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE));
		schrittNummer.setBackground(TextfieldShef.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
		schrittNummer.setForeground(TextfieldShef.Hintergrundfarbe_Geloescht);
	}
	public void setQuellStil(String text) {
		setStyle(text, quellschrittStil);
		setBackground(AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
		//schrittNummer.setText("<html><body><span style='text-decoration: line-through;'>"+schrittNummer.getText()+"</span></body></html>");
		schrittNummer.setBorder(new MatteBorder(0, 2, 1, 1, TextfieldShef.Hintergrundfarbe_Geloescht));
		schrittNummer.setBackground(TextfieldShef.Hintergrundfarbe_Geloescht);
		schrittNummer.setForeground(TextfieldShef.Schriftfarbe_Geloescht);
	}

	// public void removeStyle(String text) throws BadLocationException {
	// StyledDocument doc = (StyledDocument)editorPane.getDocument();
	// doc.remove, arg1);;
	// }

	public void setStyleSchrittnummer(String text, MutableAttributeSet attr) {
		StyledDocument doc = (StyledDocument) editorPane.getDocument();
		// String labelText = label.getText();
		doc.setCharacterAttributes(0, text.length(), attr, true);
		ganzerSchrittGeloeschtStilSetzenWennNochNichtVorhanden();
	}

	//06.12.2020
	public void ganzerSchrittGeloeschtStilSetzenWennNochNichtVorhanden() {
		if (!ganzerSchrittGeloeschtStilGesetzt()) {
			StyledEditorKit k = (StyledEditorKit) editorPane.getEditorKit();
			MutableAttributeSet inputAttributes = k.getInputAttributes();
			inputAttributes.addAttributes(geloeschtStil);
		}

	}
	// 06.12.2020
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

	public boolean ganzerSchrittStandardStilGesetzt() {
		StyledEditorKit k = (StyledEditorKit) editorPane.getEditorKit();
		MutableAttributeSet inputAttributes = k.getInputAttributes();
		Object currentFontColorValue = inputAttributes.getAttribute(CSS.Attribute.COLOR);
		Object currentBackgroundColorValue = inputAttributes.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
		if (currentFontColorValue!=null && currentFontColorValue.toString().equals(INDIKATOR_SCHWARZ) && currentBackgroundColorValue.toString().equals(INDIKATOR_WEISS)) {
			return false && currentFontColorValue!=null && currentFontColorValue.toString().equalsIgnoreCase(INDIKATOR_SCHWARZ) && currentBackgroundColorValue.toString().equals(INDIKATOR_WEISS);
		}
		return currentBackgroundColorValue != null
				&& currentBackgroundColorValue.toString().equalsIgnoreCase(INDIKATOR_WEISS)
				&& currentFontColorValue != null && currentFontColorValue.toString().equalsIgnoreCase(INDIKATOR_SCHWARZ);
	}

	//29.12.2020
	public void ganzerSchrittStandardStilSetzenWennNochNichtVorhanden() {
		if (!ganzerSchrittGeloeschtStilGesetzt()) {
			StyledEditorKit k = (StyledEditorKit) editorPane.getEditorKit();
			MutableAttributeSet inputAttributes = k.getInputAttributes();
			inputAttributes.addAttributes(standardStil);
		}

	}

	public static String getHTMLColor(Color color) {
		if (color == null) {
			return "#000000";
		}
		return "#" + Integer.toHexString(color.getRGB()).substring(2).toLowerCase();
	}

	private final InsetPanel insetPanel;
	private final JEditorPane editorPane;
	public final JLabel schrittNummer;
	boolean schrittNummerSichtbar = true;

	public TextfieldShef(EditorI editor, String initialerText, String schrittId) {
		editorPane = new JEditorPane();
		insetPanel = new InsetPanel(editorPane, this);
		editor.instrumentWysEditor(editorPane, initialerText, 0);
		editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		editorPane.setFont(font);
		setBackground(schrittHintergrund());
		editorPane.addKeyListener(this);

		if (schrittId != null) {
			schrittNummer = new JLabel(schrittId);
			schrittNummer.setFont(labelFont);
			schrittNummer.setBackground(Schriftfarbe_Geloescht);
			schrittNummer.setBorder(new MatteBorder(0, 2, 1, 1, Schriftfarbe_Geloescht));
			schrittNummer.setForeground(Color.WHITE);
			schrittNummer.setOpaque(true);

			DragButtonAdapter ada = new DragButtonAdapter(Specman.instance());
			schrittNummer.addMouseListener(ada);
			schrittNummer.addMouseMotionListener(ada);

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
	// public String getId() {
	// return schrittNummer.getText();
	// }

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
			// Wenn wir die ZeilenumbrÃƒÂ¼che nicht rausnehmen, dann entstehen spÃƒÂ¤ter
			// beim Laden u.U.
			// Leerzeichen an Zeilenenden, die im ursprÃƒÂ¼nglichen Text nicht drin waren.
			// Das ist doof,
			// weil dann die separat abgespeicherten Textintervalle der
			// Aenderungsmarkierungen
			// nicht mehr passen.
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

	public void repaintSchrittId() {
		if (schrittNummer != null)
			schrittNummer.repaint();
	}

	public java.util.List<Aenderungsmarkierung_V001> findeAenderungsmarkierungen(boolean nurErste) {
		java.util.List<Aenderungsmarkierung_V001> ergebnis = new ArrayList<Aenderungsmarkierung_V001>();
		StyledDocument doc = (StyledDocument) editorPane.getDocument();
		for (Element e : doc.getRootElements()) {
			findeAenderungsmarkierungen(e, ergebnis, nurErste);
			if (ergebnis.size() > 0 && nurErste)
				break;
		}
		return ergebnis;
	}

	private void findeAenderungsmarkierungen(Element e, java.util.List<Aenderungsmarkierung_V001> ergebnis,
											 boolean nurErste) {
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

	private boolean elementHatAenderungshintergrund(Element e) {
		AttributeSet attr = e.getAttributes();
		Object backgroundColorValue = attr.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
		return (backgroundColorValue != null && backgroundColorValue.toString().equals(INDIKATOR_GELB));
	}

	public void setAenderungsmarkierungen(java.util.List<Aenderungsmarkierung_V001> aenderungen) {
		// TODO JL: Brauchen wir aktuell nicht mehr. Das war nÃƒÂ¶tig, weil die
		// Hintergrundfarbe nicht
		// im abgespeicherten HTML erhalten blieb. Das ist jetzt dank des Tricks aus
		// https://stackoverflow.com/questions/13285526/jtextpane-text-background-color-does-not-work
		// der Fall. Die Funktion kann also evt. weg, sofern wir aus den
		// HTML-Formatierungen allein
		// alle die Ãƒâ€žnderungsinformationen vollstÃƒÂ¤ndig wieder auslesen kÃƒÂ¶nnen.
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
		if (Specman.instance().aenderungenVerfolgen()) {
			Specman.instance().hauptSequenz.findeSchritt(editorPane).setAenderungsart(Aenderungsart.Bearbeitet);
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
						//test ob ich damit das geloeschtStil Event triggern kann
						StyledEditorKit k = (StyledEditorKit) editorPane.getEditorKit();
						MutableAttributeSet inputAttributes = k.getInputAttributes();
						StyleConstants.setStrikeThrough(inputAttributes, true);
						geloeschtStilGesetzt();
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
		if (Specman.instance().aenderungenVerfolgen()) {
			Specman.instance().hauptSequenz.findeSchritt(editorPane).setAenderungsart(Aenderungsart.Bearbeitet);
			StyledDocument doc = (StyledDocument) editorPane.getDocument();
			int p0 = editorPane.getSelectionStart();
			int p1 = editorPane.getSelectionEnd();
			if (p0 != p1) {
				doc.setCharacterAttributes(p0, p1 - p0, geloeschtStil, false);
				editorPane.setSelectionStart(p1);
				// Jetzt ist am Ende der vorherigen Selektion noch der Geloescht-Stil gesetzt
				// D.h. die Durchstreichung muss noch weg fÃ¯Â¿Â½r das neue Zeichen, das grade
				// eingefÃ¯Â¿Â½gt werden soll
			}
		} else {
			DefaultStyledDocument doc = (DefaultStyledDocument) editorPane.getDocument();
			int p0 = editorPane.getSelectionStart();
			int p1 = editorPane.getSelectionEnd();
			if (p0 != p1) {
				doc.setCharacterAttributes(p0, p1 - p0, standardStil, false);
				editorPane.setSelectionStart(p1);
				// Jetzt ist am Ende der vorherigen Selektion noch der Geloescht-Stil gesetzt
				// D.h. die Durchstreichung muss noch weg fÃ¯Â¿Â½r das neue Zeichen, das grade
				// eingefÃ¯Â¿Â½gt werden soll
				StyledEditorKit k = (StyledEditorKit) editorPane.getEditorKit();
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
				&& currentBackgroundColorValue.toString().equalsIgnoreCase(INDIKATOR_GELB) && true;
	}

	//überarbeiten!!!
	public boolean aenderungsStilCheck() {
		StyledEditorKit k = (StyledEditorKit) editorPane.getEditorKit();
		MutableAttributeSet inputAttributes = k.getInputAttributes();
		Object currentTextDecoration = inputAttributes.getAttribute(CSS.Attribute.TEXT_DECORATION);
		Object currentBackgroundColorValue = inputAttributes.getAttribute((CSS.Attribute.BACKGROUND_COLOR));
		if (currentTextDecoration != null && currentTextDecoration.toString().equals(INDIKATOR_GELOESCHT_MARKIERT) && currentBackgroundColorValue.toString().equals(INDIKATOR_GELB)) {
			return false;
		}
		if (currentBackgroundColorValue !=null && currentBackgroundColorValue.toString().equals(INDIKATOR_GELB)){
			return
					//currentBackgroundColorValue!=null
					//&& currentBackgroundColorValue.toString().equalsIgnoreCase(INDIKATOR_GELB)
					//&&
			true;
		}
		else
			return false;
	}

	public boolean geloeschtStilGesetzt() {
		StyledEditorKit k = (StyledEditorKit) editorPane.getEditorKit();
		MutableAttributeSet inputAttributes = k.getInputAttributes();
		Object currentTextDecoration = inputAttributes.getAttribute(CSS.Attribute.TEXT_DECORATION);
		Object currentBackgroundColorValue = inputAttributes.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
		if (currentTextDecoration != null && currentTextDecoration.toString().equals(INDIKATOR_GELOESCHT_MARKIERT)) {
			return currentTextDecoration !=null
					&& currentBackgroundColorValue !=null
					&& currentTextDecoration.toString().equalsIgnoreCase(INDIKATOR_GELOESCHT_MARKIERT)
					&& currentBackgroundColorValue.toString().equalsIgnoreCase(INDIKATOR_GELB);
		}else {
			return false;
		}
	}

	public boolean geloeschtStilCheck() {
		StyledEditorKit k = (StyledEditorKit) editorPane.getEditorKit();
		MutableAttributeSet inputAttributes = k.getInputAttributes();
		Object currentTextDecoration = inputAttributes.getAttribute(CSS.Attribute.TEXT_DECORATION);
		Object currentBackgroundColorValue = inputAttributes.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
		if (currentTextDecoration !=null && currentTextDecoration.toString().equals(INDIKATOR_GELOESCHT_MARKIERT) &&
				currentBackgroundColorValue!=null &&currentBackgroundColorValue.toString().equals(INDIKATOR_GELB)){
			return true;
		}
		else
			return false;
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	public void skalieren(int prozentNeu, int prozentAktuell) {
		editorPane.setFont(font.deriveFont((float) FONTSIZE * prozentNeu / 100));
		if (schrittNummer != null) {
			schrittNummer.setFont(labelFont.deriveFont((float) SCHRITTNR_FONTSIZE * prozentNeu / 100));
		}
		// prozentAktuell = 0 ist ein Indikator fÃƒÂ¼r initiales Laden. Da brauchen wir
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

	public Rectangle getBounds() {
		return insetPanel.getBounds();
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

}
