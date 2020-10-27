package specman;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import net.atlanticbb.tantlinger.shef.HTMLEditorPane;
import specman.model.ModelEnvelope;
import specman.model.v001.SchrittSequenzModel_V001;
import specman.model.v001.StruktogrammModel_V001;
import specman.undo.UndoableDiagrammSkaliert;
import specman.undo.UndoableSchrittEingefaerbt;
import specman.undo.UndoableSchrittEntfernt;
import specman.undo.UndoableSchrittHinzugefuegt;
import specman.undo.UndoableZweigEntfernt;
import specman.undo.UndoableZweigHinzugefuegt;
import specman.view.AbstractSchrittView;
import specman.view.BreakSchrittView;
import specman.view.CaseSchrittView;
import specman.view.CatchSchrittView;
import specman.view.SchrittSequenzView;
import specman.view.ZweigSchrittSequenzView;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author User #3
 */
public class Specman extends JFrame implements EditorI, SpaltenContainerI {
	public static final String SPECMAN_VERSION = "0.0.1";
	private static final String PROJEKTDATEI_EXTENSION = ".nsd";
	private static final BasicStroke GESTRICHELTE_LINIE =
			new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1.0f, new float[] {10.0f, 10.0f }, 0f);
	
	JTextComponent zuletztFokussierterText;
	SchrittSequenzView hauptSequenz;
	JPanel arbeitsbereich;
	JPanel hauptSequenzContainer;
	SpaltenResizer breitenAnpasser;
	JScrollPane scrollPane;
	TextfeldShef intro, outro;
	FormLayout hauptlayout;
	int diagrammbreite = 400;
	int zoomFaktor = 100;
	Integer dragX;
	File diagrammDatei;
	List<AbstractSchrittView> postInitSchritte;
	RecentFiles recentFiles;
	
	public Specman() throws Exception {
		recentFiles = new RecentFiles(this);

		initComponents();
		
		initShefController();
		//initJWebengineController();

		hauptSequenz = new SchrittSequenzView();

		scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, CC.xy(1, 3));
		
		arbeitsbereich = new JPanel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				if (dragX != null) {
					Graphics2D g2 = (Graphics2D)g;
					g2.setStroke(GESTRICHELTE_LINIE);
					g.drawLine(dragX, 0, dragX, arbeitsbereich.getHeight());
				}
			}
		};
		
		hauptlayout = new FormLayout("10px, 400px, " + AbstractSchrittView.FORMLAYOUT_GAP, "10px, fill:pref, fill:default, fill:pref");
		arbeitsbereich.setLayout(hauptlayout);
		arbeitsbereich.setBackground(new Color(247, 247, 253));
		hauptSequenzInitialisieren();

		breitenAnpasser = new SpaltenResizer(this, this);
		breitenAnpasser.setBackground(Color.BLACK);
		breitenAnpasser.setOpaque(true);
		arbeitsbereich.add(breitenAnpasser, CC.xy(3, 3));

		intro = new TextfeldShef();
		intro.setOpaque(false);
		arbeitsbereich.add(intro, CC.xy(2, 2));
		
		outro = new TextfeldShef();
		outro.setOpaque(false);
		arbeitsbereich.add(outro, CC.xy(2, 4));
		
		scrollPane.setViewportView(arbeitsbereich);
		actionListenerHinzufuegen();
		setSize(800, 600);
		setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		// Falls jemand nicht aufgepasst hat und beim Initialisieren irgendwelche Funktionen verwendet hat,
		// die schon etwas im Undo-Manager hinterlassen.
		undoManager.discardAllEdits();
	}
	
	@Override
	public int spaltenbreitenAnpassenNachMausDragging(int vergroesserung, int spalte) {
		diagrammbreite += vergroesserung;
		diagrammbreiteSetzen(diagrammbreite);
		diagrammAktualisieren(null);
		dragX = null;
		arbeitsbereich.repaint();
		return vergroesserung;
	}

	@Override
	public void vertikalLinieSetzen(int x, SpaltenResizer spaltenResizer) {
		if (spaltenResizer != null) {
			Point relativePosition = SwingUtilities.convertPoint(spaltenResizer, new Point(x, 0), arbeitsbereich);
			dragX = (int)relativePosition.getX();
		}
		else {
			dragX = null;
		}
		arbeitsbereich.repaint();
	}


	private void hauptSequenzInitialisieren() {
		if (hauptSequenzContainer != null) {
			arbeitsbereich.remove(hauptSequenzContainer);
		}
		hauptSequenzContainer = hauptSequenz.getContainer();
		// Rundherum schwarze Linie au�er rechts. Da kommt stattdessen der breitenAnpasser hin
		hauptSequenzContainer.setBorder(new MatteBorder(AbstractSchrittView.LINIENBREITE, AbstractSchrittView.LINIENBREITE, AbstractSchrittView.LINIENBREITE, 0, Color.BLACK));
		arbeitsbereich.add(hauptSequenzContainer, CC.xy(2, 3));
		diagrammAktualisieren(null);
	}

	@Override public void focusGained(FocusEvent e) {
//		if (e.getSource() instanceof JWordTextPane) {
//			ctrl.setEditor((JWordTextPane)e.getSource());
//			ctrl.styleChanged();
//		}
		
//		if (e.getSource() instanceof JTextPane) {
//			toolbar.switchEditor((JTextPane)e.getSource());
//		}
		
	}
	
	@Override public void focusLost(FocusEvent e) {
		if (e.getSource() instanceof JTextComponent)
			zuletztFokussierterText = (JTextComponent)e.getSource();
	}
	
	private void setDiagrammDatei(File diagrammDatei) {
		this.diagrammDatei = diagrammDatei;
		setTitle(diagrammDatei.getName());
	}

	private void fehler(String text) {
		JOptionPane.showMessageDialog(this, text);
	}

	private void actionListenerHinzufuegen() {
		schrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractSchrittView schritt;
				SchrittSequenzView sequenz = hauptSequenz.findeSequenz(zuletztFokussierterText);
				if (sequenz != null)
					schritt = sequenz.einfachenSchrittZwischenschieben(zuletztFokussierterText, Specman.this);
				else
					schritt = hauptSequenz.einfachenSchrittAnhaengen(Specman.this);
				addEdit(new UndoableSchrittHinzugefuegt(schritt, sequenz!= null ? sequenz : hauptSequenz));
				diagrammAktualisieren(schritt);
			}
		});
		
		whileSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractSchrittView schritt;
				SchrittSequenzView sequenz = hauptSequenz.findeSequenz(zuletztFokussierterText);
				if (sequenz != null)
					schritt = sequenz.whileSchrittZwischenschieben(zuletztFokussierterText, Specman.this);
				else
					schritt = hauptSequenz.whileSchrittAnhaengen(Specman.this);
				addEdit(new UndoableSchrittHinzugefuegt(schritt, sequenz!= null ? sequenz : hauptSequenz));
				diagrammAktualisieren(schritt);
			}
		});
		
		whileWhileSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractSchrittView schritt;
				SchrittSequenzView sequenz = hauptSequenz.findeSequenz(zuletztFokussierterText);
				if (sequenz != null)
					schritt = sequenz.whileWhileSchrittZwischenschieben(zuletztFokussierterText, Specman.this);
				else
					schritt = hauptSequenz.whileWhileSchrittAnhaengen(Specman.this);
				addEdit(new UndoableSchrittHinzugefuegt(schritt, sequenz!= null ? sequenz : hauptSequenz));
				diagrammAktualisieren(schritt);
			}
		});
		
		ifElseSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractSchrittView schritt;
				SchrittSequenzView sequenz = hauptSequenz.findeSequenz(zuletztFokussierterText);
				if (sequenz != null)
					schritt = sequenz.ifElseSchrittZwischenschieben(zuletztFokussierterText, Specman.this);
				else
					schritt = hauptSequenz.ifElseSchrittAnhaengen(Specman.this);
				addEdit(new UndoableSchrittHinzugefuegt(schritt, sequenz!= null ? sequenz : hauptSequenz));
				diagrammAktualisieren(schritt);
			}
		});
		
		ifSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractSchrittView schritt;
				SchrittSequenzView sequenz = hauptSequenz.findeSequenz(zuletztFokussierterText);
				if (sequenz != null)
					schritt = sequenz.ifSchrittZwischenschieben(zuletztFokussierterText, Specman.this);
				else
					schritt = hauptSequenz.ifSchrittAnhaengen(Specman.this);
				addEdit(new UndoableSchrittHinzugefuegt(schritt, sequenz!= null ? sequenz : hauptSequenz));
				diagrammAktualisieren(schritt);
			}
		});
		
		caseSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractSchrittView schritt;
				SchrittSequenzView sequenz = hauptSequenz.findeSequenz(zuletztFokussierterText);
				if (sequenz != null)
					schritt = sequenz.caseSchrittZwischenschieben(zuletztFokussierterText, Specman.this);
				else
					schritt = hauptSequenz.caseSchrittAnhaengen(Specman.this);
				addEdit(new UndoableSchrittHinzugefuegt(schritt, sequenz!= null ? sequenz : hauptSequenz));
				diagrammAktualisieren(schritt);
			}
		});
		
		subsequenzSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractSchrittView schritt;
				SchrittSequenzView sequenz = hauptSequenz.findeSequenz(zuletztFokussierterText);
				if (sequenz != null)
					schritt = sequenz.subsequenzSchrittZwischenschieben(zuletztFokussierterText, Specman.this);
				else
					schritt = hauptSequenz.subsequenzSchrittAnhaengen(Specman.this);
				addEdit(new UndoableSchrittHinzugefuegt(schritt, sequenz!= null ? sequenz : hauptSequenz));
				diagrammAktualisieren(schritt);
			}
		});
		
		breakSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractSchrittView schritt;
				SchrittSequenzView sequenz = hauptSequenz.findeSequenz(zuletztFokussierterText);
				if (sequenz != null)
					schritt = sequenz.breakSchrittZwischenschieben(zuletztFokussierterText, Specman.this);
				else
					schritt = hauptSequenz.breakSchrittAnhaengen(Specman.this);
				addEdit(new UndoableSchrittHinzugefuegt(schritt, sequenz!= null ? sequenz : hauptSequenz));
				diagrammAktualisieren(schritt);
			}
		});
		
		catchSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractSchrittView schritt;
				SchrittSequenzView sequenz = hauptSequenz.findeSequenz(zuletztFokussierterText);
				if (sequenz != null)
					schritt = sequenz.catchSchrittZwischenschieben(zuletztFokussierterText, Specman.this);
				else
					schritt = hauptSequenz.catchSchrittAnhaengen(Specman.this);
				addEdit(new UndoableSchrittHinzugefuegt(schritt, sequenz!= null ? sequenz : hauptSequenz));
				diagrammAktualisieren(schritt);
			}
		});
		
		caseAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractSchrittView schritt = hauptSequenz.findeSchritt(zuletztFokussierterText);
				if (!(schritt instanceof CaseSchrittView)) {
					fehler("Kein Case-Schritt ausgewählt");
					return;
				}
				CaseSchrittView caseSchritt = (CaseSchrittView)schritt;
				ZweigSchrittSequenzView ausgewaehlterZweig = caseSchritt.istZweigUeberschrift(zuletztFokussierterText);
				if (ausgewaehlterZweig == null) {
					fehler("Kein Zweig ausgewählt");
					return;
				}
				ZweigSchrittSequenzView neuerZweig = caseSchritt.neuenZweigHinzufuegen(Specman.this, ausgewaehlterZweig);
				addEdit(new UndoableZweigHinzugefuegt(Specman.this, neuerZweig, caseSchritt));
				diagrammAktualisieren(schritt);
			}
		});
		
		einfaerben.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractSchrittView schritt = hauptSequenz.findeSchritt(zuletztFokussierterText);
				Color aktuelleHintergrundfarbe = schritt.getBackground();
				int farbwert = aktuelleHintergrundfarbe.getRed() == 240 ? 255 : 240;
				Color neueHintergrundfarbe = new Color(farbwert, farbwert, farbwert);
				schritt.setBackground(neueHintergrundfarbe);
				addEdit(new UndoableSchrittEingefaerbt(schritt, aktuelleHintergrundfarbe, neueHintergrundfarbe));
			}
		});
		
		loeschen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractSchrittView schritt = hauptSequenz.findeSchritt(zuletztFokussierterText);
				if (schritt == null) {
					// Sollte nur der Fall sein, wenn man den Fokus im Intro oder Outro stehen hat
					fehler("Ups - niemandem scheint das Feld zu gehören, in dem steht: " + zuletztFokussierterText.getText());
					return;
				}
				
				if (schritt instanceof CaseSchrittView) {
					CaseSchrittView caseSchritt = (CaseSchrittView)schritt;
					ZweigSchrittSequenzView zweig = caseSchritt.istZweigUeberschrift(zuletztFokussierterText);
					if (zweig != null) {
						int zweigIndex = caseSchritt.zweigEntfernen(Specman.this, zweig);
						undoManager.addEdit(new UndoableZweigEntfernt(Specman.this, zweig, caseSchritt, zweigIndex));
					}
					return;
				}
				
				SchrittSequenzView sequenz = hauptSequenz.findeElternSequenz(schritt);
				int schrittIndex = sequenz.schrittEntfernen(schritt);
				undoManager.addEdit(new UndoableSchrittEntfernt(schritt, sequenz, schrittIndex));
			}
		});

		
		speichern.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				diagrammSpeichern(false);
			}
		});
		
		speichernUnter.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				diagrammSpeichern(true);
			}
		});
		
		laden.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				diagrammLaden();
			}
		});
		
		export.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				diagrammExportieren();
			}
		});
		
		review.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				hauptSequenz.zusammenklappenFuerReview();
			}
		});
		
		zoom.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				int prozentNeu = ((ZoomFaktor)zoom.getSelectedItem()).getProzent();
				int prozentAlt = skalieren(prozentNeu);
				undoManager.addEdit(new UndoableDiagrammSkaliert(Specman.this, prozentAlt));
			}

		});
		
		birdsview.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				final int breite = hauptSequenzContainer.getBounds().width;
				final int hoehe = hauptSequenzContainer.getBounds().height;
				final Image i = createImage(breite, hoehe);
				Graphics g = i.getGraphics();
				hauptSequenzContainer.paint(g);
				final JPanel p = new JPanel();
				p.setLayout(new BorderLayout());
		        final Image scaledImage = i.getScaledInstance(breite / 5, hoehe / 5,  java.awt.Image.SCALE_SMOOTH);
				ImageIcon icon = new ImageIcon(scaledImage);
				final JLabel l = new JLabel(icon);
				p.add(l, BorderLayout.CENTER);
				final JDialog d = new JDialog();
				d.addComponentListener(new ComponentAdapter() {
					@Override
					public void componentResized(ComponentEvent e) {
						int skalierteBreite = 0;
						int skalierteHoehe = 0;
						float breitenFaktor = (float)p.getSize().width / breite;
						float hoehenFaktor = (float)p.getSize().height / hoehe;
						if (breitenFaktor > hoehenFaktor) {
							skalierteHoehe = p.getSize().height;
							skalierteBreite = (int)(breite * hoehenFaktor);
						}
						else {
							skalierteBreite = p.getSize().width;
							skalierteHoehe = (int)(hoehe * breitenFaktor);
						}
						Image neuSkaliert = i.getScaledInstance(skalierteBreite, skalierteHoehe,  java.awt.Image.SCALE_SMOOTH);
						l.setIcon(new ImageIcon(neuSkaliert));
					}
				});
				d.getContentPane().add(p);
				d.pack();
				d.setVisible(true);
			}
		});
		
	}

	public int skalieren(int prozent) {
		int bisherigerFaktor = zoomFaktor;
		zoomFaktor = prozent;
		zoomFaktorAnzeigeAktualisieren(prozent);
		float diagrammbreite100Prozent = (float)diagrammbreite / bisherigerFaktor * 100;
		int neueDiagrammbreite = (int)(diagrammbreite100Prozent * prozent / 100);
		spaltenbreitenAnpassenNachMausDragging(neueDiagrammbreite - diagrammbreite, 0);	
		hauptSequenz.skalieren(prozent, bisherigerFaktor);
		intro.skalieren(prozent, bisherigerFaktor);
		outro.skalieren(prozent, bisherigerFaktor);
		return bisherigerFaktor;
	}
	
	/** Nicht so schön, aber nötig: hier wird der Zoomfaktor in der Combobox auf einen neuen Wert aktualisiert,
	 * ohne dass dabei der AktionListener aufgerufen wird, der dann wiederum einen Eintrag im UndoManager
	 * produzieren würde. Wir brauchen die Umstellung des Werts aber grade für Undo und Redo, wo natürlich
	 * nichts neues eingetragen werden soll. Wir machen das durch Entfernen und wieder Anklemmen der Listener.
	 */
	private void zoomFaktorAnzeigeAktualisieren(int prozent) {
		ZoomFaktor faktor = ZoomFaktor.valueOf("Faktor_" + zoomFaktor);
		List<ActionListener> listeners = Arrays.asList(zoom.getActionListeners());
		listeners.forEach(l -> zoom.removeActionListener(l));
		zoom.setSelectedItem(faktor);
		listeners.forEach(l -> zoom.addActionListener(l));
	}

	private void diagrammSpeichern(boolean dateiauswahlErzwingen) {
		try {
			if (diagrammDatei == null || dateiauswahlErzwingen) {
				File verzeichnis = (diagrammDatei != null) ? diagrammDatei.getParentFile() : null;
				JFileChooser fileChooser = new JFileChooser(verzeichnis);
				fileChooser.setFileFilter(new FileNameExtensionFilter("Nassi Diagramme", "nsd"));
				if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
					return;
				String ausgewaehlterDateiname = fileChooser.getSelectedFile().getAbsolutePath();
				if (!ausgewaehlterDateiname.endsWith(PROJEKTDATEI_EXTENSION))
					ausgewaehlterDateiname += PROJEKTDATEI_EXTENSION;
				File ausgewaehlteDatei = new File(ausgewaehlterDateiname);
				if (!ausgewaehlteDatei.equals(diagrammDatei) && ausgewaehlteDatei.exists()) {
					int confirmErgebnis = JOptionPane.showConfirmDialog(this,
							"Die ausgewählte Datei existiert bereits.\nSoll die Datei überschrieben werden?",
							"Datei überschreiben?", JOptionPane.OK_CANCEL_OPTION);
					if (confirmErgebnis == JOptionPane.CANCEL_OPTION)
						return;
				}
				setDiagrammDatei(new File(ausgewaehlterDateiname));
			}
			StruktogrammModel_V001 model = generiereStruktogrammModel(true);
			ModelEnvelope wrappedModel = wrapModel(model);

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enableDefaultTyping();
			byte[] json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(wrappedModel);
			FileOutputStream fos = new FileOutputStream(diagrammDatei);
			fos.write(json);
			fos.close();

			recentFiles.add(diagrammDatei);
			undoManager.discardAllEdits(); // Kann man sich drüber streiten ;-)

		} catch (JsonProcessingException jpx) {
			jpx.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ModelEnvelope wrapModel(StruktogrammModel_V001 model) {
		ModelEnvelope envelope = new ModelEnvelope();
		envelope.model = model;
		envelope.modelType = model.getClass().getName();
		envelope.specmanVersion = SPECMAN_VERSION;
		return envelope;
	}

	private void diagrammLaden() {
		File verzeichnis = (diagrammDatei != null) ? diagrammDatei.getParentFile() : null;
		JFileChooser fileChooser = new JFileChooser(verzeichnis);
		fileChooser.setFileFilter(new FileNameExtensionFilter("Nassi Diagramme", "nsd"));
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			diagrammLaden(fileChooser.getSelectedFile());
		}
	}

	public void diagrammLaden(File diagramFile) {
		try {
			postInitSchritte = new ArrayList<AbstractSchrittView>();
			setDiagrammDatei(diagramFile);

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enableDefaultTyping();
			ModelEnvelope envelope = objectMapper.readValue(diagrammDatei, ModelEnvelope.class);
			StruktogrammModel_V001 model = (StruktogrammModel_V001)envelope.model;

			zoomFaktor = model.zoomFaktor;
			zoomFaktorAnzeigeAktualisieren(zoomFaktor);
			diagrammbreite = model.breite;
			intro.setPlainText(model.intro);
			outro.setPlainText(model.outro);
			setName(model.name);
			hauptSequenz = new SchrittSequenzView(this, model.hauptSequenz);
			hauptSequenzInitialisieren();
			neueSchritteNachinitialisieren();
			recentFiles.add(diagramFile);
			undoManager.discardAllEdits();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void schrittFuerNachinitialisierungRegistrieren(AbstractSchrittView schritt) {
		postInitSchritte.add(schritt);
	}
	
	private void neueSchritteNachinitialisieren() {
		for (AbstractSchrittView schritt: postInitSchritte) {
			schritt.nachinitialisieren();
		}
	}

	private void diagrammbreiteSetzen(int breite) {
		hauptlayout.setColumnSpec(2, ColumnSpec.decode(breite + "px"));
	}
	
	public void diagrammAktualisieren(AbstractSchrittView schrittImFokus) {
		hauptSequenzContainer.setVisible(false);
		if (schrittImFokus != null) {
			schrittImFokus.skalieren(zoomFaktor, 100);
		}
		// Folgende Zeile forciert ein Relayouting, falls z.B. nur eine manuelle Breiten�nderung
		// einer If-Else-Spaltenteilung stattgefunden hat.
		diagrammbreiteSetzen(diagrammbreite-1);
		final Point viewPosition = scrollPane.getViewport().getViewPosition(); 
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				diagrammbreiteSetzen(diagrammbreite);
				hauptSequenzContainer.setVisible(true);
				if (schrittImFokus != null)
					schrittImFokus.requestFocus();
				scrollPane.getViewport().setViewPosition(viewPosition);
			}
		});
	}
	
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		toolBar = new JToolBar();
		schrittAnhaengen = new JButton();
		whileSchrittAnhaengen = new JButton();
		whileWhileSchrittAnhaengen = new JButton();
		ifElseSchrittAnhaengen = new JButton();
		ifSchrittAnhaengen = new JButton();
		caseSchrittAnhaengen = new JButton();
		subsequenzSchrittAnhaengen = new JButton();
		breakSchrittAnhaengen = new JButton();
		catchSchrittAnhaengen = new JButton();
		caseAnhaengen = new JButton();
		einfaerben = new JButton();
		loeschen = new JButton();
		review = new JButton();
		birdsview = new JButton();
		aenderungenVerfolgen = new JToggleButton();
		zoom = new JComboBox<ZoomFaktor>();
		for (ZoomFaktor faktor: ZoomFaktor.values())
			zoom.addItem(faktor);
		zoom.setSelectedItem(ZoomFaktor.Faktor_100);
		zoom.setMaximumSize(new Dimension(65, 20));
		speichern = new JMenuItem("Speichern");
		speichernUnter = new JMenuItem("Speichern unter...");
		laden = new JMenuItem("Laden...");

		export = new JMenuItem("Exportieren");
		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new FormLayout("default:grow", "default, default, fill:10px:grow"));

		//======== toolBar ========
		toolbarButtonHinzufuegen(schrittAnhaengen, "einfacher-schritt", "Einfachen Schritt anhängen");
		toolbarButtonHinzufuegen(whileSchrittAnhaengen, "while-schritt", "While-Schritt anhängen");
		toolbarButtonHinzufuegen(whileWhileSchrittAnhaengen, "whilewhile-schritt", "While-While-Schritt anhängen");
		toolbarButtonHinzufuegen(ifElseSchrittAnhaengen, "ifelse-schritt", "If-Else-Schritt anhängen");
		toolbarButtonHinzufuegen(ifSchrittAnhaengen, "if-schritt", "If-Schritt anhängen");
		toolbarButtonHinzufuegen(caseSchrittAnhaengen, "case-schritt", "Case-Schritt anhängen");
		toolbarButtonHinzufuegen(subsequenzSchrittAnhaengen, "subsequenz-schritt", "Subsequenz anhängen");
		toolbarButtonHinzufuegen(breakSchrittAnhaengen, "break-schritt", "Break anhängen");
		toolbarButtonHinzufuegen(catchSchrittAnhaengen, "catch-schritt", "Catchblock anhängen");
		toolbarButtonHinzufuegen(caseAnhaengen, "zweig", "Case anhängen");
		toolBar.addSeparator();
		toolbarButtonHinzufuegen(einfaerben, "helligkeit", "Hintergrund schattieren");
		toolbarButtonHinzufuegen(loeschen, "loeschen", "Schritt löschen");
		toolBar.addSeparator();
		toolbarButtonHinzufuegen(aenderungenVerfolgen, "aenderungen", "Änderungen verfolgen");
		toolbarButtonHinzufuegen(review, "review", "Für Review zusammenklappen");
		toolBar.addSeparator();
		toolbarButtonHinzufuegen(birdsview, "birdsview", "Bird's View");
		toolBar.add(zoom);
		contentPane.add(toolBar, CC.xywh(1, 1, 1, 1));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	public static ImageIcon readImageIcon(String iconBasename) {
		String resource = "images/" + iconBasename + ".png";
		try {
			URL imageURL = Specman.class.getClassLoader().getResource(resource);
			Image image = ImageIO.read(imageURL);
			if (image == null) {
				throw new IllegalArgumentException("Can't load image icon " + resource);
			}
			return new ImageIcon(image);
		}
		catch(IOException iox) {
			iox.printStackTrace();
			throw new IllegalArgumentException("Error reading image icon " + resource + ": " + iox.getMessage());
		}
	}

	private void toolbarButtonHinzufuegen(AbstractButton button, String iconBasename, String tooltip) {
		button.setIcon(readImageIcon(iconBasename));
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setToolTipText(tooltip);
		toolBar.add(button);
	}
	
	static HTMLEditorPane shefEditorPane;
	static UndoManager undoManager;
	
	private void initShefController() throws Exception {
		undoManager = new UndoManager();
		shefEditorPane = new HTMLEditorPane(undoManager);
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(baueDateiMenu());
		menuBar.add(shefEditorPane.getEditMenu());
		menuBar.add(shefEditorPane.getFormatMenu());
		menuBar.add(shefEditorPane.getInsertMenu());

		setJMenuBar(menuBar);
		getContentPane().add(shefEditorPane.getFormatToolBar(), CC.xywh(1, 2, 1, 1));
	}

	static void addEdit(UndoableEdit edit) {
		undoManager.addEdit(edit);
	}

	private JMenu baueDateiMenu() {
		JMenu dateiMenu = new JMenu("Datei");
		dateiMenu.add(laden);
		dateiMenu.add(recentFiles.menu());
		dateiMenu.add(speichern);		
		dateiMenu.add(speichernUnter);		
		dateiMenu.add(export);
		return dateiMenu;
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JToolBar toolBar;
	private JButton schrittAnhaengen;
	private JButton whileSchrittAnhaengen;
	private JButton whileWhileSchrittAnhaengen;
	private JButton ifElseSchrittAnhaengen;
	private JButton ifSchrittAnhaengen;
	private JButton caseSchrittAnhaengen;
	private JButton subsequenzSchrittAnhaengen;
	private JButton breakSchrittAnhaengen;
	private JButton catchSchrittAnhaengen;
	private JButton caseAnhaengen;
	private JButton einfaerben;
	private JButton loeschen;
	private JButton review;
	private JButton birdsview;
	private JComboBox<ZoomFaktor> zoom;
	private JToggleButton aenderungenVerfolgen;
	private JMenuItem speichern;
	private JMenuItem speichernUnter;
	private JMenuItem laden;
	private JMenuItem export;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	private static Specman instance;

	@Deprecated
	/** Use the {@link EditorI} interface instead. */
	public static Specman instance() { return instance; }
	
	public boolean aenderungenVerfolgen() {
		return aenderungenVerfolgen.isSelected();
	}
	
	public static void main(String[] args) throws Exception {
		instance = new Specman();
	}

	public StruktogrammModel_V001 generiereStruktogrammModel(boolean formatierterText) {
		StruktogrammModel_V001 model = new StruktogrammModel_V001(
			getName(),
			diagrammbreite,
			zoomFaktor,
			hauptSequenz.generiereSchittSequenzModel(formatierterText),
			intro.getText(),
			outro.getText());
		return model;
	}
	
	public void diagrammExportieren() {
		SchrittSequenzModel_V001 model = hauptSequenz.generiereSchittSequenzModel(false);
		try {
			new GraphvizExporter("export.gv").export(model);
		}
		catch(IOException iox) {
			iox.printStackTrace();
		}
	}

	public BreakSchrittView findeBreakSchritt(CatchSchrittView fuerCatchSchritt) {
		SchrittSequenzView sequenzDesCatchSchritts = hauptSequenz.findeSequenz(fuerCatchSchritt.getText());
		if (sequenzDesCatchSchritts == null) {
			System.err.println("Ups, das geht hier noch nicht richtig. Laden von Break-Ankoppungen");
			return null;
		}
		String catchText = fuerCatchSchritt.ersteZeileExtraieren();
		return sequenzDesCatchSchritts.findeBreakSchritt(catchText);
	}

	@Override
	public SchrittSequenzView findeElternSequenz(AbstractSchrittView schrittView) {
		return hauptSequenz.findeElternSequenz(schrittView);
	}

	public int zoomFaktor() { return zoomFaktor; }

	public static boolean istHauptSequenz(SchrittSequenzView schrittSequenzView) {
		return Specman.instance.hauptSequenz == schrittSequenzView;
	}
	
	public static String initialtext(String text) {
		return (instance() != null && instance().aenderungenVerfolgen()) ?
				"<span style=\"background-color:" + TextfeldShef.INDIKATOR_GELB + "\">" + text + "</span>" :
				text;
	}
	
	public static Color schrittHintergrund() {
		return (instance() != null && instance().aenderungenVerfolgen()) ?
			TextfeldShef.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE : Color.white;
	}
	
}
