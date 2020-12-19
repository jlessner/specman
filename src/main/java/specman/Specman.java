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
import specman.textfield.InsetPanel;
import specman.textfield.TextfieldShef;
import specman.undo.UndoableDiagrammSkaliert;
import specman.undo.UndoableSchrittEingefaerbt;
import specman.undo.UndoableSchrittEntfernt;
import specman.undo.UndoableSchrittHinzugefuegt;
import specman.undo.UndoableToggleStepBorder;
import specman.undo.UndoableZweigEntfernt;
import specman.undo.UndoableZweigHinzugefuegt;
import specman.view.*;

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
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static specman.view.RelativeStepPosition.After;
import static specman.view.RelativeStepPosition.Before;

/**
 * @author User #3
 */
public class Specman extends JFrame implements EditorI, SpaltenContainerI {
	public static final int INITIAL_DIAGRAMM_WIDTH = 700;
	public static final String SPECMAN_VERSION = "0.0.1";
	private static final String PROJEKTDATEI_EXTENSION = ".nsd";
	private static final BasicStroke GESTRICHELTE_LINIE =
			new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1.0f, new float[] {10.0f, 10.0f }, 0f);

	JTextComponent zuletztFokussierterText;
	public SchrittSequenzView hauptSequenz;
	JPanel arbeitsbereich;
	JPanel hauptSequenzContainer;
	SpaltenResizer breitenAnpasser;
	JScrollPane scrollPane;
	TextfieldShef intro, outro;
	FormLayout hauptlayout;
	int diagrammbreite = INITIAL_DIAGRAMM_WIDTH;
	int zoomFaktor = 100;
	Integer dragX;
	File diagrammDatei;
	List<AbstractSchrittView> postInitSchritte;
	RecentFiles recentFiles;
	private WelcomeMessagePanel welcomeMessage;

	//TODO window for dragging
	public Boolean lastStep = false;
	public final JWindow window = new JWindow();

	public Specman() throws Exception {
		setApplicationIcon();

		recentFiles = new RecentFiles(this);
		undoManager = new SpecmanUndoManager(this);

		initComponents();

		initShefController();
		//initJWebengineController();

		hauptSequenz = new SchrittSequenzView();

		scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, CC.xy(2, 3));
		// ToDo Sidebar change from getContentPane().add(scrollPane, CC.xy(1, 3));

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

		hauptlayout = new FormLayout(
				"10px, " + INITIAL_DIAGRAMM_WIDTH + "px, " + AbstractSchrittView.FORMLAYOUT_GAP,
				"10px, fill:pref, fill:default, fill:pref");
		arbeitsbereich.setLayout(hauptlayout);
		arbeitsbereich.setBackground(new Color(247, 247, 253));
		displayWelcomeMessage();

		intro = new TextfieldShef(this);
		intro.setOpaque(false);
		arbeitsbereich.add(intro.asJComponent(), CC.xy(2, 2));

		outro = new TextfieldShef(this);
		outro.setOpaque(false);
		arbeitsbereich.add(outro.asJComponent(), CC.xy(2, 4));

		scrollPane.setViewportView(arbeitsbereich);
		actionListenerHinzufuegen();
		setInitialWindowSizeAndScreenCenteredLocation();
		setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Falls jemand nicht aufgepasst hat und beim Initialisieren irgendwelche Funktionen verwendet hat,
		// die schon etwas im Undo-Manager hinterlassen.
		undoManager.discardAllEdits();
		System.out.println(SwingUtilities.convertPoint(this.getContentPane(), 0, 0,this));
		this.setGlassPane(new GlassPane((SwingUtilities.convertPoint(this.getContentPane(), 0, 0,this).y)-getJMenuBar().getHeight()));
	}

	private void setInitialWindowSizeAndScreenCenteredLocation() {
		setSize(1100, 700);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int w = this.getSize().width;
		int h = this.getSize().height;
		int x = (dim.width-w)/2;
		int y = (dim.height-h)/2;
		this.setLocation(x, y);
	}

	private void displayWelcomeMessage() {
		welcomeMessage = new WelcomeMessagePanel();
		arbeitsbereich.add(welcomeMessage, CC.xy(2, 3));
	}

	private void dropWelcomeMessage() {
		if (welcomeMessage != null) {
			arbeitsbereich.remove(welcomeMessage);
			welcomeMessage = null;
			hauptSequenzInitialisieren();
		}
	}

	private void setApplicationIcon() {
		setIconImage(readImageIcon("specman").getImage());
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
		else {
			breitenAnpasser = new SpaltenResizer(this, this);
			breitenAnpasser.setBackground(Color.BLACK);
			breitenAnpasser.setOpaque(true);
			arbeitsbereich.add(breitenAnpasser, CC.xy(3, 3));
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
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(zuletztFokussierterText);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().einfachenSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.einfachenSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
			}
		});

		whileSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(zuletztFokussierterText);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().whileSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.whileSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
			}
		});

		whileWhileSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(zuletztFokussierterText);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().whileWhileSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.whileWhileSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
			}
		});

		ifElseSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(zuletztFokussierterText);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().ifElseSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.ifElseSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
			}
		});

		ifSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(zuletztFokussierterText);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().ifSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.ifSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
			}
		});

		caseSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(zuletztFokussierterText);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().caseSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.caseSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
			}
		});

		subsequenzSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(zuletztFokussierterText);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().subsequenzSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.subsequenzSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
			}
		});

		breakSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(zuletztFokussierterText);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().breakSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.breakSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
			}
		});

		catchSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(zuletztFokussierterText);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().caseSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.caseSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
			}
		});

		caseAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
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
				schritt.skalieren(zoomFaktor, 100);
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

				SchrittSequenzView sequenz = schritt.getParent();
				int schrittIndex = sequenz.schrittEntfernen(schritt);
				undoManager.addEdit(new UndoableSchrittEntfernt(schritt, sequenz, schrittIndex));
			}
		});

		toggleBorderType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractSchrittView schritt = hauptSequenz.findeSchritt(zuletztFokussierterText);
				if (schritt != null) {
					SchrittSequenzView sequenz = schritt.getParent();
					sequenz.toggleBorderType(schritt);
					addEdit(new UndoableToggleStepBorder(Specman.this, schritt, sequenz));
					diagrammAktualisieren(schritt);
				}
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
			undoManager.discardAllEdits();
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
			dropWelcomeMessage();
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
			intro.skalieren(zoomFaktor, 0);
			outro.setPlainText(model.outro);
			outro.skalieren(zoomFaktor, 0);
			setName(model.name);
			hauptSequenz = new SchrittSequenzView(this, null, model.hauptSequenz);
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

	private void newStepPostInit(AbstractSchrittView newStep) {
		addEdit(new UndoableSchrittHinzugefuegt(newStep, newStep.getParent()));
		newStep.skalieren(zoomFaktor, 100);
		newStep.initInheritedTextFieldIndentions();
		diagrammAktualisieren(newStep);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		toolBar = new JToolBar();
		toolBar.setFloatable(false); //ToDo Sidebar added
		buttonBar = new JToolBar(JToolBar.VERTICAL); //ToDo Sidebar added

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
		toggleBorderType = new JButton();
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
		contentPane.setLayout(new FormLayout("pref, default:grow", "default, default, fill:10px:grow")); //ToDo Sidebar added "pref"

		//======== toolBar ========
		toolbarButtonHinzufuegen(schrittAnhaengen, "einfacher-schritt", "Einfachen Schritt anhängen", buttonBar);
		toolbarButtonHinzufuegen(whileSchrittAnhaengen, "while-schritt", "While-Schritt anhängen", buttonBar);
		toolbarButtonHinzufuegen(whileWhileSchrittAnhaengen, "whilewhile-schritt", "While-While-Schritt anhängen", buttonBar);
		toolbarButtonHinzufuegen(ifElseSchrittAnhaengen, "ifelse-schritt", "If-Else-Schritt anhängen", buttonBar);
		toolbarButtonHinzufuegen(ifSchrittAnhaengen, "if-schritt", "If-Schritt anhängen", buttonBar);
		toolbarButtonHinzufuegen(caseSchrittAnhaengen, "case-schritt", "Case-Schritt anhängen", buttonBar);
		toolbarButtonHinzufuegen(subsequenzSchrittAnhaengen, "subsequenz-schritt", "Subsequenz anhängen", buttonBar);
		toolbarButtonHinzufuegen(breakSchrittAnhaengen, "break-schritt", "Break anhängen", buttonBar);
		toolbarButtonHinzufuegen(catchSchrittAnhaengen, "catch-schritt", "Catchblock anhängen", buttonBar);
		toolbarButtonHinzufuegen(caseAnhaengen, "zweig", "Case anhängen", buttonBar);
		//toolBar.addSeparator();   //ToDo
		toolbarButtonHinzufuegen(einfaerben, "helligkeit", "Hintergrund schattieren", toolBar);
		toolbarButtonHinzufuegen(loeschen, "loeschen", "Schritt löschen", toolBar);
		toolbarButtonHinzufuegen(toggleBorderType, "switch-border", "Rahmen umschalten", toolBar);
		toolBar.addSeparator();
		toolbarButtonHinzufuegen(aenderungenVerfolgen, "aenderungen", "Änderungen verfolgen", toolBar);
		toolbarButtonHinzufuegen(review, "review", "Für Review zusammenklappen", toolBar);
		toolBar.addSeparator();
		toolbarButtonHinzufuegen(birdsview, "birdsview", "Bird's View", toolBar);
		toolBar.add(zoom);

		//ToDo SideBar Change from contentPane.add(toolBar, CC.xywh(1, 1, 1, 1));
		contentPane.add(toolBar, CC.xywh(1, 1, 2, 1));
		contentPane.add(buttonBar, CC.xy(1, 3));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
		//TODO
		DragButtonAdapter dragButtonAdapter = new DragButtonAdapter(this);
		addDragAdapter(schrittAnhaengen,dragButtonAdapter);
		addDragAdapter(whileSchrittAnhaengen,dragButtonAdapter);
		addDragAdapter(whileWhileSchrittAnhaengen,dragButtonAdapter);
		addDragAdapter(ifElseSchrittAnhaengen,dragButtonAdapter);
		addDragAdapter(ifSchrittAnhaengen,dragButtonAdapter);
		addDragAdapter(caseSchrittAnhaengen,dragButtonAdapter);
		addDragAdapter(subsequenzSchrittAnhaengen,dragButtonAdapter);
		addDragAdapter(breakSchrittAnhaengen,dragButtonAdapter);
		addDragAdapter(catchSchrittAnhaengen,dragButtonAdapter);
		//TODO caseAnhängen
		addDragAdapter(caseAnhaengen,dragButtonAdapter);


	}

	private void addDragAdapter(JButton button, DragButtonAdapter adapter) {
		button.addMouseListener(adapter);
		button.addMouseMotionListener(adapter);
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

	private void toolbarButtonHinzufuegen(AbstractButton button, String iconBasename, String tooltip, JToolBar tb) {
		button.setIcon(readImageIcon(iconBasename));
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setToolTipText(tooltip);
		tb.add(button);
	}

	HTMLEditorPane shefEditorPane;
	UndoManager undoManager;

	@Override
	public void instrumentWysEditor(JEditorPane ed, String initialText, Integer orientation) {
		shefEditorPane.instrumentWysEditor(ed, initialText, orientation);
	}

	private void initShefController() throws Exception {
		shefEditorPane = new HTMLEditorPane(undoManager);
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(baueDateiMenu());
		menuBar.add(shefEditorPane.getEditMenu());
		menuBar.add(shefEditorPane.getFormatMenu());
		menuBar.add(shefEditorPane.getInsertMenu());

		setJMenuBar(menuBar);
		// ToDo Sidebar Changed from getContentPane().add(shefEditorPane.getFormatToolBar(), CC.xywh(1, 2, 1, 1));
		getContentPane().add(shefEditorPane.getFormatToolBar(), CC.xywh(1, 2, 2, 1));

	}

	@Override
	public void addEdit(UndoableEdit edit) {
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
	private JToolBar buttonBar; //ToDo Sidebar ergänzt
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
	private JButton toggleBorderType;
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
		SchrittSequenzView sequenzDesCatchSchritts = fuerCatchSchritt.getParent();
		if (sequenzDesCatchSchritts == null) {
			System.err.println("Ups, das geht hier noch nicht richtig. Laden von Break-Ankoppungen");
			return null;
		}
		String catchText = fuerCatchSchritt.ersteZeileExtraieren();
		return sequenzDesCatchSchritts.findeBreakSchritt(catchText);
	}

	public int zoomFaktor() { return zoomFaktor; }

	public static boolean istHauptSequenz(SchrittSequenzView schrittSequenzView) {
		return Specman.instance.hauptSequenz == schrittSequenzView;
	}

	public static String initialtext(String text) {
		return (instance() != null && instance().aenderungenVerfolgen()) ?
				"<span style=\"background-color:" + TextfieldShef.INDIKATOR_GELB + "\">" + text + "</span>" :
				text;
	}

	public static Color schrittHintergrund() {
		return (instance() != null && instance().aenderungenVerfolgen()) ?
				TextfieldShef.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE : Color.white;
	}

	@Override public int getZoomFactor() {
		return zoomFaktor;
	}

	// GlassPane and add Step to sequence
	public void checkZweigHeading(ZweigSchrittSequenzView zweig,Point pos,int glassPaneHeight, Boolean ins, MouseEvent mE) {
		Point p = SwingUtilities.convertPoint(zweig.getUeberschrift().getInsetPanel(),0,0,Specman.this);
		Rectangle r = createRectangle(p, zweig.getUeberschrift());
		if(r.contains(pos)) {
			createGlassPane(r.width, p.x, p.y+r.height-glassPaneHeight, glassPaneHeight, true);
			//mouserelease add Step at first Position in sequenz
			if(ins) {
				AbstractSchrittView step = zweig.schritte.get(0);
				addNeuerSchritt(Before, step, instance, mE);
			}
		}
	}

	// GlassPane over Cases
	public void checkCaseHeading(ZweigSchrittSequenzView zweig,Point pos,int glassPaneHeight, Boolean ins, MouseEvent mE) {
		Point p = SwingUtilities.convertPoint(zweig.getUeberschrift().getInsetPanel(),0,0,Specman.this);
		Rectangle r = createRectangle(p, zweig.getUeberschrift());
		if(r.contains(pos)) {
			//prüfun ob rechts oder links, nur nötig wenn hinzufügen geändert wird
			/*if(pos.x < r.getX()+r.width/2){
				GlassPane gP = (GlassPane) this.getGlassPane();
				gP.setInputRecBounds(p.x,p.y, glassPaneHeight, r.height);
				this.getGlassPane().setVisible(true);
			}else{
				GlassPane gP = (GlassPane) this.getGlassPane();
				gP.setInputRecBounds(p.x+r.width-glassPaneHeight,p.y, glassPaneHeight, r.height);
				this.getGlassPane().setVisible(true);
			}*/
			createGlassPane(glassPaneHeight, p.x + r.width - glassPaneHeight, p.y, r.height, true);
			//mouserelease add Case right from choosen Case
			if(ins) {
				addCase(zweig);
			}
		}
	}

	//add Case withing Drag and Drop
	private void addCase(ZweigSchrittSequenzView zweig) {
		AbstractSchrittView step = hauptSequenz.findeSchritt(zweig.getUeberschrift().getTextComponent());
		CaseSchrittView caseSchritt = (CaseSchrittView) step;
		ZweigSchrittSequenzView ausgewaehlterZweig = caseSchritt.istZweigUeberschrift(zweig.getUeberschrift().getTextComponent());
		ZweigSchrittSequenzView neuerZweig = caseSchritt.neuenZweigHinzufuegen(this, ausgewaehlterZweig);
		addEdit(new UndoableZweigHinzugefuegt(Specman.this, neuerZweig, caseSchritt));
		step.skalieren(zoomFaktor, 100);
		diagrammAktualisieren(step);
	}

	// GlassPane and add Step to sequence
	public void checkSchleifenHeading(SchleifenSchrittView schleife,Point pos,int glassPaneHeight, Boolean ins, MouseEvent mE) {
		Point p = SwingUtilities.convertPoint(schleife.getTextShef().getInsetPanel(),0,0,Specman.this);
		Rectangle r = createRectangle(p, schleife.getTextShef());
		if(r.contains(pos)) {
			createGlassPane(r.width, p.x, p.y+r.height-glassPaneHeight, glassPaneHeight, true);

			//mouserelease add Step at first Position in sequenz
			if(ins) {
				AbstractSchrittView step = schleife.getWiederholSequenz().schritte.get(0);
				addNeuerSchritt(Before, step, instance, mE);
			}
		}
	}

	// GlassPane and add Step to sequence
	public void checkSubsequenzHeading(SubsequenzSchrittView schritt,Point pos,int glassPaneHeight, Boolean ins, MouseEvent mE) {
		Point p = SwingUtilities.convertPoint(schritt.getTextShef().getInsetPanel(),0,0,Specman.this);
		Rectangle r = createRectangle(p, schritt.getTextShef());
		if(r.contains(pos)) {
			createGlassPane(r.width, p.x, p.y+r.height-glassPaneHeight, glassPaneHeight, true);

			//mouserelease add Step at first Position in sequenz
			if(ins) {
				AbstractSchrittView step = schritt.getSubsequenz().schritte.get(0);
				addNeuerSchritt(Before, step, instance, mE);
			}
		}
	}
	

	// GlassPane and add Step to sequence
	public boolean checkGlassPaneforComponent(AbstractSchrittView step, Point pos, int glassPaneHeight,boolean ins,MouseEvent mE) {
		//Todo schauen ob berechnung mehr sinn macht
		Component c = null;
		Component cl = null;
		Component cu = null;
		Rectangle r = new Rectangle();
		Rectangle rl = new Rectangle();
		Rectangle ru = new Rectangle();
		Point p = null;
		Point pl = null;
		Point pu = null;

		if(step instanceof SchleifenSchrittView ){
			c = ((SchleifenSchrittView) step).getPanel();
			cl = ((SchleifenSchrittView) step).getLinkerBalken();
			cu = ((SchleifenSchrittView) step).getUntererBalken();
		} else if(step instanceof SubsequenzSchrittView){
			c = step.getTextShef().getInsetPanel();
		}else if(step instanceof BreakSchrittView){
			c = ((BreakSchrittView) step).getPanel();
			//Abfrage für MousePosition auf dem Panel des Breakschritts
			cl = c;
		}

		p = SwingUtilities.convertPoint(c,0,0,Specman.this);
		r = c.getBounds();
		r.setLocation(p);
		if(cl != null){
			pl = SwingUtilities.convertPoint(cl,0,0,Specman.this);
			rl = cl.getBounds();
			rl.setLocation(pl);
		}
		if(cu != null){
			pu = SwingUtilities.convertPoint(cu,0,0,Specman.this);
			ru = cu.getBounds();
			ru.setLocation(pu);
		}
		//Abfrage ob der Mousezeiger über dem linken oder unteren Balken ist
		if(rl.contains(pos)|| ru.contains(pos)) {
			createGlassPane(r.width, p.x, p.y+r.height-glassPaneHeight, glassPaneHeight, true);
			//bei Release hinzufügen eines Schrittes an erster Position im Zweig
			if((ins && cl != null && rl.contains(pos))||(ins && cu != null && ru.contains(pos))) {
				addNeuerSchritt(After, step, instance, mE);
				return true;
			}
		}
		return false;
	}

	//GlassPane
	public void checkfalseGlassPaneforComponent(TextfieldShef c, Point pos, int glassPaneHeight) {
		Point p = SwingUtilities.convertPoint(c.getInsetPanel(),0,0,Specman.this);
		Rectangle r = createRectangle(p,c);
		if(r.contains(pos)) {
			createGlassPane(r.width, p.x, p.y+r.height-glassPaneHeight, glassPaneHeight, false);
		}
	}

	//Recursive method to check and add Steps
	public void dragGlassPanePos(Point pos,List<AbstractSchrittView> schrittListe,boolean insert, MouseEvent e) {
		int glassPaneHeight = 5;
		GlassPane glassPane =  (GlassPane) this.getGlassPane();
		Point p;
		//Abfrage,damit ein Schritt nicht auf oder in sich selbst verschoben werden kann
		//TODO Cursoranpassung funktioniert noch nicht
		if(e.getSource() instanceof JLabel){
			JLabel label = (JLabel) e.getSource();
			InsetPanel ip = (InsetPanel) label.getParent().getParent();
			AbstractSchrittView step = hauptSequenz.findeSchritt(ip.getTextfeld().getTextComponent());
			Point checkPoint = SwingUtilities.convertPoint(step.getPanel(),0,0,Specman.this);
			Rectangle rec =  step.getPanel().getVisibleRect();
			rec.setLocation(checkPoint);
			//Letzer Schritt darf nicht verschoben werden
			if(step.getParent().schritte.size()<=1) {
				return;
			}
			//Abfrage ob man sich auf sich selbst befindet
			if(rec.contains(pos)) {
					showInvalidCursor();
					return;
			}
		}
		
		//inserts firststep to empty diagram
		insertFirstStep(schrittListe, insert, e);
		for(AbstractSchrittView schritt : schrittListe) {
			p = SwingUtilities.convertPoint(schritt.getPanel(), 0, 0, Specman.this);
			Rectangle r = createRectangle(p, schritt.getPanel());		
			//Abfrage ob es sich um den letzten Schritt einer Subsquenz gehört
			if (schrittListe.get(schrittListe.size() - 1) == schritt && schritt.getId().nummern.size() > 1) {
				if(lastPixels(pos, p, glassPaneHeight, r, glassPane, schritt, insert)) {
					break;
				}
			}
			//Add Case
			if(e.getSource().equals(caseAnhaengen)) {
				if (schritt instanceof CaseSchrittView) {
					CaseSchrittView caseSchritt = (CaseSchrittView) schritt;

					checkfalseGlassPaneforComponent(caseSchritt.getTextShef(), pos, glassPaneHeight);
					checkCaseHeading(caseSchritt.getSonstSequenz(), pos, glassPaneHeight, insert, e);
					dragGlassPanePos(pos, caseSchritt.getSonstSequenz().schritte, insert, e);

					for (ZweigSchrittSequenzView caseSequenz : caseSchritt.getCaseSequenzen()) {
						int groesse = caseSchritt.getCaseSequenzen().size();
						checkCaseHeading(caseSequenz, pos, glassPaneHeight, insert, e);
						dragGlassPanePos(pos, caseSequenz.schritte, insert, e);

						if (groesse != caseSchritt.getCaseSequenzen().size()) {
							break;
						}
					}
				}else{
					if(schritt instanceof IfElseSchrittView || schritt instanceof IfSchrittView){
						IfElseSchrittView ifel = (IfElseSchrittView) schritt;
						if (schritt instanceof IfElseSchrittView) {
							dragGlassPanePos(pos, ifel.getIfSequenz().schritte, insert, e);
						}
						dragGlassPanePos(pos, ifel.getElseSequenz().schritte, insert, e);

					}else if(schritt instanceof WhileSchrittView || schritt instanceof WhileWhileSchrittView){
						SchleifenSchrittView schleife = (SchleifenSchrittView) schritt;
						dragGlassPanePos(pos, schleife.getWiederholSequenz().schritte, insert, e);

					}else if(schritt instanceof SubsequenzSchrittView){
						SubsequenzSchrittView sub = (SubsequenzSchrittView) schritt;
						dragGlassPanePos(pos, sub.getSequenz().schritte, insert, e);
					}else{
						//no Action
					}
				}
			//Add Step
			}else {
				//Abfrage einfacherSchritt
				int groesse = schrittListe.size();
				if (schritt instanceof EinfacherSchrittView) {
					if (r.contains(pos)) {
						glassPane.setInputRecBounds(p.x, p.y + r.height - glassPaneHeight, r.width, glassPaneHeight);

						if (checkFirstStep(schritt, pos, glassPaneHeight, insert, e)) {
							break;
						} else {
							glassPane.setInputRecBounds(p.x, p.y + r.height - glassPaneHeight, r.width, glassPaneHeight);
							if (insert) {
								addNeuerSchritt(After, schritt, instance, e);
							}
						}
						this.getGlassPane().setVisible(true);
						break;
					}//Abfrage IfElseSchritt
				} else if (schritt.getClass().getName().equals("specman.view.IfElseSchrittView") || schritt.getClass().getName().equals("specman.view.IfSchrittView")) {
					
					IfElseSchrittView ifel = (IfElseSchrittView) schritt;
					if (checkFirstStep(schritt, pos, glassPaneHeight, insert, e)) {
						break;
					}

					checkfalseGlassPaneforComponent(ifel.getTextShef(), pos, glassPaneHeight);
					if (schritt.getClass().getName().equals("specman.view.IfElseSchrittView")) {
						checkZweigHeading(ifel.getIfSequenz(), pos, glassPaneHeight, insert, e);
						dragGlassPanePos(pos, ifel.getIfSequenz().schritte, insert, e);
					}
					checkZweigHeading(ifel.getElseSequenz(), pos, glassPaneHeight, insert, e);
					dragGlassPanePos(pos, ifel.getElseSequenz().schritte, insert, e);

					//wenn letzter Step in Sequenz beenden der Rekusiven Methode und verwenden des Übergeordneten Schrittes
					if (lastStep) {
						addNeuerSchritt(After, ifel, instance, e);
						lastStep = false;
						break;
					}
					//While SChritt
				} else if (schritt instanceof WhileSchrittView || schritt instanceof WhileWhileSchrittView) {

					SchleifenSchrittView schleife = (SchleifenSchrittView) schritt;
					if (checkFirstStep(schritt, pos, glassPaneHeight, insert, e)) {
						break;
					}
					if (checkGlassPaneforComponent(schleife, pos, glassPaneHeight, insert, e)) {
						break;
					}

					dragGlassPanePos(pos, schleife.getWiederholSequenz().schritte, insert, e);
					checkSchleifenHeading(schleife, pos, glassPaneHeight, insert, e);

					//wenn letzter Step in Sequenz beenden der Rekusiven Methode und verwenden des Übergeordneten Schrittes
					if (lastStep) {
						addNeuerSchritt(After, schleife, instance, e);
						lastStep = false;
						break;
					}
				} else if (schritt instanceof CaseSchrittView) {
					CaseSchrittView caseSchritt = (CaseSchrittView) schritt;
					if (checkFirstStep(schritt, pos, glassPaneHeight, insert, e)) {
						break;
					}
					checkfalseGlassPaneforComponent(caseSchritt.getTextShef(), pos, glassPaneHeight);
					checkZweigHeading(caseSchritt.getSonstSequenz(), pos, glassPaneHeight, insert, e);
					dragGlassPanePos(pos, caseSchritt.getSonstSequenz().schritte, insert, e);
					for (ZweigSchrittSequenzView caseSequenz : caseSchritt.getCaseSequenzen()) {
						checkZweigHeading(caseSequenz, pos, glassPaneHeight, insert, e);
						dragGlassPanePos(pos, caseSequenz.schritte, insert, e);

						//wenn letzter Step in Sequenz beenden der Rekusiven Methode und verwenden des Übergeordneten Schrittes
						if (lastStep) {
							addNeuerSchritt(After, caseSchritt, instance, e);
							lastStep = false;
							break;
						}
					}
				} else if (schritt instanceof SubsequenzSchrittView) {
					SubsequenzSchrittView sub = (SubsequenzSchrittView) schritt;
					if (checkFirstStep(schritt, pos, glassPaneHeight, insert, e)) {
						break;
					}
					checkGlassPaneforComponent(sub, pos, glassPaneHeight, insert, e);
					dragGlassPanePos(pos, sub.getSequenz().schritte, insert, e);
					checkSubsequenzHeading(sub, pos, glassPaneHeight, insert, e);
					//wenn letzter Step in Sequenz beenden der Rekusiven Methode und verwenden des Übergeordneten Schrittes
					if (lastStep) {
						addNeuerSchritt(After, sub, instance, e);
						lastStep = false;
						break;
					}
				} else if (schritt instanceof BreakSchrittView) {
					BreakSchrittView breakSchritt = (BreakSchrittView) schritt;
					if (checkFirstStep(schritt, pos, glassPaneHeight, insert, e)) {
						break;
					}
					checkGlassPaneforComponent(breakSchritt, pos, glassPaneHeight, insert, e);

					//wenn letzter Step in Sequenz beenden der Rekusiven Methode und verwenden des Übergeordneten Schrittes
					if (lastStep) {
						addNeuerSchritt(After, breakSchritt, instance, e);
						lastStep = false;
						break;
					}
				}
				if (groesse != schrittListe.size()) {
					break;
				}
			}
		}
	}
	//TODO funktioniert nicht wird überschrieben
	public void showInvalidCursor() {
		try {
			this.setCursor(Cursor.getSystemCustomCursor("Invalid.32x32"));
		} catch (HeadlessException | AWTException e) {
			e.printStackTrace();
		}
		
	}

	//Creates Rectangle that refelcts a Step with Location and size
	private Rectangle createRectangle(Point p, TextfieldShef textShef) {
		Rectangle r = textShef.getInsetPanel().getVisibleRect();
		r.setLocation(p);
		return r;
	}
	//Creates Rectangle that refelcts a Step with Location and size
	private Rectangle createRectangle(Point p, JComponent comp) {
		Rectangle r = comp.getVisibleRect();
		r.setLocation(p);
		return r;
	}
	//Creates GlassPane
	private void createGlassPane(int glassPaneHeight, int i, int y, int height, boolean b) {
		GlassPane gP = (GlassPane) this.getGlassPane();
		gP.setInputRecBounds(i, y, glassPaneHeight, height);
		this.getGlassPane().setVisible(b);
	}

	private boolean checkFirstStep(AbstractSchrittView schritt,Point pos,int glassPaneHeight,boolean insert,MouseEvent e) {
		Point p = SwingUtilities.convertPoint(schritt.getPanel(),0,0,Specman.this);
		Rectangle r = schritt.getPanel().getBounds();
		r.setLocation(p);
		if(r.contains(pos)) {
			if(schritt.getId().toString().equals("1")&&(pos.y < (r.y+glassPaneHeight))) {
				GlassPane glassPane = (GlassPane) this.getGlassPane();
				glassPane.setInputRecBounds(p.x,p.y, r.width, glassPaneHeight);
				glassPane.setVisible(true);
				if(insert) {
					addNeuerSchritt(Before, schritt, instance, e);
				}
				return true;
			}
		}
		return false;
	}

	// Entfernt die WelcomeMessage und fügt den neuen Schritt hinzu
	private void insertFirstStep(List<AbstractSchrittView> schrittListe, boolean insert, MouseEvent e) {
		if(schrittListe.size() == 0 && insert) {
			SchrittSequenzView curSequenz = hauptSequenz;
			dropWelcomeMessage();
			addNeuerSchritt(instance, e,curSequenz);
		}
	}

	//Neuen Schritt zwischenschieben abhängig vom Button
	private void addNeuerSchritt(RelativeStepPosition insertionPosition,
								 AbstractSchrittView schritt, EditorI editor,MouseEvent e) {
		SchrittSequenzView sequenz = schritt.getParent();
		//ToDo Löschen und hinzufügen beim verschieben
		if(e.getSource() instanceof JLabel){
			JLabel label = (JLabel) e.getSource();

			InsetPanel ip = (InsetPanel) label.getParent().getParent();
			AbstractSchrittView step = hauptSequenz.findeSchritt(ip.getTextfeld().getTextComponent());
			//Abfrage da der Schritt nicht vor oder nach sich selbst eingefügt werden kann
			if(step!=schritt) {

				int schrittindex = step.getParent().schrittEntfernen(step);
	
				step.setId(schritt.newStepIDInSameSequence(insertionPosition));
				undoManager.addEdit(new UndoableSchrittEntfernt(step,step.getParent(),schrittindex));
	
				step.setParent(schritt.getParent());
						sequenz.schrittZwischenschieben(step,insertionPosition,schritt,instance);
			}
		}

		if (e.getSource().equals(schrittAnhaengen)) {
			schritt = sequenz.einfachenSchrittZwischenschieben(insertionPosition,schritt,instance);
			newStepPostInit(schritt);
		}else if(e.getSource().equals(whileSchrittAnhaengen)){
			schritt = sequenz.whileSchrittZwischenschieben(insertionPosition,schritt,instance);
			newStepPostInit(schritt);
		}else if(e.getSource().equals(whileWhileSchrittAnhaengen)){
			schritt = sequenz.whileWhileSchrittZwischenschieben(insertionPosition,schritt,instance);
			newStepPostInit(schritt);
		}else if(e.getSource().equals(ifElseSchrittAnhaengen)){
			schritt = sequenz.ifElseSchrittZwischenschieben(insertionPosition,schritt,instance);
			newStepPostInit(schritt);
		}else if(e.getSource().equals(ifSchrittAnhaengen)){
			schritt = sequenz.ifSchrittZwischenschieben(insertionPosition,schritt,instance);
			newStepPostInit(schritt);
		}else if(e.getSource().equals(caseSchrittAnhaengen)){
			schritt = sequenz.caseSchrittZwischenschieben(insertionPosition,schritt,instance);
			newStepPostInit(schritt);
		}else if(e.getSource().equals(subsequenzSchrittAnhaengen)){
			schritt = sequenz.subsequenzSchrittZwischenschieben(insertionPosition,schritt,instance);
			newStepPostInit(schritt);
		}else if(e.getSource().equals(breakSchrittAnhaengen)){
			schritt = sequenz.breakSchrittZwischenschieben(insertionPosition,schritt,instance);
			newStepPostInit(schritt);
		}else if(e.getSource().equals(catchSchrittAnhaengen)){
			schritt = sequenz.catchSchrittZwischenschieben(insertionPosition,schritt,instance);
			newStepPostInit(schritt);
		}else if(e.getSource().equals(caseAnhaengen)){

		}
	}
	//Neuen Schritt anhängen abhängig vom Button
	private void addNeuerSchritt(EditorI editor,MouseEvent e,SchrittSequenzView sequenz) {
		if (e.getSource().equals(schrittAnhaengen)) {
			sequenz.einfachenSchrittAnhaengen(Specman.this);
		}else if (e.getSource().equals(whileSchrittAnhaengen)) {
			sequenz.whileSchrittAnhaengen(Specman.this);
		}else if(e.getSource().equals(whileWhileSchrittAnhaengen)) {
			sequenz.whileWhileSchrittAnhaengen(Specman.this);
		}else if(e.getSource().equals(ifElseSchrittAnhaengen)){
			sequenz.ifElseSchrittAnhaengen(Specman.this);
		}else if(e.getSource().equals(ifSchrittAnhaengen)){
			sequenz.ifSchrittAnhaengen(Specman.this);
		}else if(e.getSource().equals(caseSchrittAnhaengen)){
			sequenz.caseSchrittAnhaengen(Specman.this);
		}else if(e.getSource().equals(subsequenzSchrittAnhaengen)){
			sequenz.subsequenzSchrittAnhaengen(Specman.this);
		}else if(e.getSource().equals(breakSchrittAnhaengen)){
			sequenz.breakSchrittAnhaengen(Specman.this);
		}else if(e.getSource().equals(catchSchrittAnhaengen)){
			sequenz.catchSchrittAnhaengen(Specman.this);
		}
	}
	
	private boolean lastPixels(Point pos, Point p, int glassPaneHeight, Rectangle r, GlassPane glassPane,AbstractSchrittView schritt,Boolean insert ) {
		if(!(schritt.getParent().getParent() instanceof WhileWhileSchrittView)) {
			r.setLocation(p);
			if(r.contains(pos)) {
				if (pos.y > (p.y + r.height - glassPaneHeight)) {
					Container container = schritt.getParent().getContainer().getParent();
					p = SwingUtilities.convertPoint(container, 0, 0, Specman.this);
					glassPane.setInputRecBounds(p.x - 2, p.y + container.getHeight(), container.getWidth() + 4, glassPaneHeight);
					this.getGlassPane().setVisible(true);
					if (insert) {
						//hier wird nur festgestellt, dass es sich um den letzten Schritt in der Sequenz handelt und das Iterieren beendet -> Sprung zurück in vorherige Ebene
						lastStep = true;
					}
					return true;
				}
			}
		}
		return false;
	}
	

}
