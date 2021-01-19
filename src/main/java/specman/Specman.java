package specman;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import net.atlanticbb.tantlinger.shef.HTMLEditorPane;
import org.w3c.dom.Text;
import specman.draganddrop.DragButtonAdapter;
import specman.draganddrop.DraggingLogic;
import specman.draganddrop.GlassPane;
import specman.model.ModelEnvelope;
import specman.model.v001.SchrittSequenzModel_V001;
import specman.model.v001.StruktogrammModel_V001;
import specman.textfield.TextfieldShef;
import specman.undo.*;
import specman.view.AbstractSchrittView;
import specman.view.BreakSchrittView;
import specman.view.CaseSchrittView;
import specman.view.CatchSchrittView;
import specman.view.IfElseSchrittView;
import specman.view.IfSchrittView;
import specman.view.SchleifenSchrittView;
import specman.view.SchrittSequenzView;
import specman.view.SubsequenzSchrittView;
import specman.view.ZweigSchrittSequenzView;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static specman.Specman.schrittHintergrund;
import static specman.view.RelativeStepPosition.After;

/**
 * @author User #3
 */
public class Specman extends JFrame implements EditorI, SpaltenContainerI {
	public static final int INITIAL_DIAGRAMM_WIDTH = 700;
	public static final String SPECMAN_VERSION = "0.0.1";
	private static final String PROJEKTDATEI_EXTENSION = ".nsd";
	private static final BasicStroke GESTRICHELTE_LINIE =
			new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1.0f, new float[] {10.0f, 10.0f }, 0f);
	private final DraggingLogic draggingLogic = new DraggingLogic(this);

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
		//TODO
		scrollPane.addMouseWheelListener(new DragButtonAdapter(this));
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

	public void dropWelcomeMessage() {
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
				pruefeFuerSchrittnummer();
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
				pruefeFuerSchrittnummer();
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
				pruefeFuerSchrittnummer();
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
				pruefeFuerSchrittnummer();
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
				pruefeFuerSchrittnummer();
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
				pruefeFuerSchrittnummer();
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
				pruefeFuerSchrittnummer();
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
				pruefeFuerSchrittnummer();
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
				pruefeFuerSchrittnummer();
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
				pruefeFuerSchrittnummer();
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
				
				//Der Teil wird nur durchlaufen, wenn die Aenderungsverfolgung aktiviert ist
				if(instance != null && instance.aenderungenVerfolgen() && schritt.getAenderungsart() != Aenderungsart.Hinzugefuegt){

					//Muss hinzugefügt werden um zu gucken ob die Markierung schon gesetzt wurde
					if(schritt.getAenderungsart()==Aenderungsart.Geloescht)
                    	return;
                    else {

                    	//TODO einzelne Casees entfernen
						if (schritt instanceof CaseSchrittView) {
							CaseSchrittView caseSchritt = (CaseSchrittView) schritt;
							ZweigSchrittSequenzView zweig = caseSchritt.istZweigUeberschrift(zuletztFokussierterText);
							if (zweig != null) {
								if(zweig.getAenderungsart() == Aenderungsart.Hinzugefuegt){
									int zweigIndex = caseSchritt.zweigEntfernen(Specman.this, zweig);
									undoManager.addEdit(new UndoableZweigEntfernt(Specman.this, zweig, caseSchritt, zweigIndex));
								}
								zweig.setAenderungsart(Aenderungsart.Geloescht);
								zweig.getUeberschrift().setStyle(zweig.getUeberschrift().getText(), TextfieldShef.ganzerSchrittGeloeschtStil);
								zweig.getUeberschrift().setBackground(TextfieldShef.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
								recrusiv(zweig.getSchritte(), Aenderungsart.Geloescht);
							}
							else {
								schritt.setAenderungsart(Aenderungsart.Geloescht);
								//schritt.getshef().setStyle(schritt.getPlainText(), TextfieldShef.ganzerSchrittGeloeschtStil);
								//schritt.setBackground(TextfieldShef.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
								//schritt.getText().setEditable(false);
								aenderungsMarkierungenAufGeloescht(schritt);
								ohneSchleife(schritt, Aenderungsart.Geloescht);
								schritt.getshef().setGeloeschtStil(schritt.getshef().getPlainText());
								//System.out.println(undoManager.getUndoPresentationName());
								undoManager.addEdit(new UndoableSchrittnummerEntfernt(schritt,schritt.getshef().schrittNummer));
							}
						}



						else{
							schritt.setAenderungsart(Aenderungsart.Geloescht);
							schritt.getshef().setStyle(schritt.getPlainText(), TextfieldShef.ganzerSchrittGeloeschtStil);
							schritt.setBackground(TextfieldShef.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
							//schritt.getText().setEditable(false);
							aenderungsMarkierungenAufGeloescht(schritt);
							ohneSchleife(schritt, Aenderungsart.Geloescht);
						}
                    }
				}

				//Hier erfolgt das richtige Löschen, Aenderungsverfolgung nicht aktiviert
				else {
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

		//TODO
		aenderungenUebernehmen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				List<AbstractSchrittView> schritte = new CopyOnWriteArrayList<AbstractSchrittView>();
				schritte = hauptSequenz.schritte;
				uebernehmenAbfrage(schritte);

			}
		});

		//TODO
		aenderungenVerwerfen.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {

				List<AbstractSchrittView> schritte = new CopyOnWriteArrayList<AbstractSchrittView>();
				schritte = hauptSequenz.schritte;
				recrusiv(schritte, null);

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

	public void newStepPostInit(AbstractSchrittView newStep) {
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
		aenderungenUebernehmen = new JButton();
		aenderungenVerwerfen = new JButton();
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
		toolbarButtonHinzufuegen(aenderungenUebernehmen, "uebernehmen", "Änderungen übernehmen", toolBar);
		toolbarButtonHinzufuegen(aenderungenVerwerfen, "verwerfen", "Änderungen verwerfen", toolBar);
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
	private JToolBar buttonBar; // Sidebar ergänzt
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
	private JButton aenderungenUebernehmen;
	private JButton aenderungenVerwerfen;
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

	//TODO Methode um die Aenderugnsart von neuen Schritten auf hinzugefügt zu ändern, wenn die Änderungsverfolgung aktiviert ist
	public static Aenderungsart initialArt() {
		return (instance() != null && instance().aenderungenVerfolgen()) ?
				Aenderungsart.Hinzugefuegt : null;
	}


	@Override public int getZoomFactor() {
		return zoomFaktor;
	}

	public SchrittSequenzView getHauptSequenz() {
		return hauptSequenz;
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}

	public JButton getSchrittAnhaengen() {
		return schrittAnhaengen;
	}

	public JButton getWhileSchrittAnhaengen() {
		return whileSchrittAnhaengen;
	}

	public JButton getWhileWhileSchrittAnhaengen() {
		return whileWhileSchrittAnhaengen;
	}

	public JButton getIfElseSchrittAnhaengen() {
		return ifElseSchrittAnhaengen;
	}

	public JButton getIfSchrittAnhaengen() {
		return ifSchrittAnhaengen;
	}

	public JButton getCaseSchrittAnhaengen() {
		return caseSchrittAnhaengen;
	}

	public JButton getSubsequenzSchrittAnhaengen() {
		return subsequenzSchrittAnhaengen;
	}

	public JButton getBreakSchrittAnhaengen() {
		return breakSchrittAnhaengen;
	}

	public JButton getCatchSchrittAnhaengen() {
		return catchSchrittAnhaengen;
	}

	public JButton getCaseAnhaengen() {
		return caseAnhaengen;
	}



	//TODO Ich nehme mir nur einen Schritt und gehe dann durch alle unterschritte.
	//wird benötigt, wenn z.B. ein Schritt als gelöscht markiert werden soll,
	//um zu prüfen ob der Schritt unterschritt hat und diese dann auch zu markieren
	public void ohneSchleife(AbstractSchrittView schritt, Aenderungsart art) {
		if(schritt.getClass().getName().equals("specman.view.IfElseSchrittView") || schritt.getClass().getName().equals("specman.view.IfSchrittView")) {
			IfElseSchrittView ifel= (IfElseSchrittView) schritt;
			recrusiv(ifel.getElseSequenz().schritte, art);

			if(schritt.getClass().getName().equals("specman.view.IfElseSchrittView")) {
				recrusiv(ifel.getIfSequenz().schritte, art);
            }
		}

		else if(schritt.getClass().getName().equals("specman.view.WhileSchrittView") || schritt.getClass().getName().equals("specman.view.WhileWhileSchrittView") ) {
            SchleifenSchrittView schleife = (SchleifenSchrittView) schritt;
            recrusiv(schleife.getWiederholSequenz().schritte, art);
		}

		else if (schritt.getClass().getName().equals("specman.view.CaseSchrittView")) {
			CaseSchrittView caseSchritt = (CaseSchrittView) schritt;
			recrusiv(caseSchritt.getSonstSequenz().schritte, art);
			for (ZweigSchrittSequenzView caseSequenz : caseSchritt.getCaseSequenzen()) {
				recrusiv(caseSequenz.schritte, art);
			}
		}

		else if (schritt.getClass().getName().equals("specman.view.SubsequenzSchrittView")) {
			SubsequenzSchrittView sub = (SubsequenzSchrittView) schritt;
			recrusiv(sub.getSequenz().schritte, art);
		}
	}


	//TODO Ich durchlaufe alle Schritte, bis ich zum untersten Schritt des Pfades angekommen bin
	//Kann alle Schritte/Unterschritte der uebergebenen Art zuweisen
	//Wird benutzt um alle unterschritte einer Schrittliste zu durchlaufen und die gewünschte Aenderungsart hinzuzufügen
	public void recrusiv(List<AbstractSchrittView> schritte, Aenderungsart art) {
		for (AbstractSchrittView schritt: schritte) {



			//wird beim Verwerfen durchlaufen
			if (art == null) {

				//Verwerfen von Änderungen, bei Änderungen an den Zweigen, diese werden nicht in der Schritte liste durchlaufen
				if (schritt.getClass().getName().equals("specman.view.CaseSchrittView")) {
					CaseSchrittView caseSchritt = (CaseSchrittView) schritt;


					//nur für caseSequenzen möglich, da man die Sonstsequenz eh nicht löschen kann
					//caseSchritt.getCaseSequenzen() = new CopyOnWriteArrayList<ZweigSchrittSequenzView>;

					//TODO es funktioniert, ist aber glaube ich keine schöne Lösung
					//Wir spiegeln die Liste einmal auf eine CopyOnWriteArrayList um zweige während des durchlaufens bearbeiten zu können
					List<ZweigSchrittSequenzView> caseSequenzen = new CopyOnWriteArrayList<ZweigSchrittSequenzView>(caseSchritt.getCaseSequenzen());
					for (ZweigSchrittSequenzView caseSequenz : caseSequenzen) {
						if(caseSequenz.getAenderungsart() == Aenderungsart.Hinzugefuegt){
							int zweigIndex = caseSchritt.zweigEntfernen(Specman.this, caseSequenz);
							undoManager.addEdit(new UndoableZweigEntfernt(Specman.this, caseSequenz, caseSchritt, zweigIndex));
						}
						if(caseSequenz.getAenderungsart() == Aenderungsart.Geloescht){
							caseSequenz.getUeberschrift().setStyle(schritt.getPlainText(), TextfieldShef.standardStil);
							caseSequenz.getUeberschrift().setBackground(TextfieldShef.Hintergrundfarbe_Standard);
							recrusiv(caseSequenz.schritte, null);
						}
					}
				}

				//hinzugefügter Schritt muss entfernt werden, da verworfen
				if(schritt.getAenderungsart() == Aenderungsart.Hinzugefuegt) {
					SchrittSequenzView sequenz = schritt.getParent();
					int schrittIndex = sequenz.schrittEntfernen(schritt);
					undoManager.addEdit(new UndoableSchrittEntfernt(schritt, sequenz, schrittIndex));
					//continue damit er nicht versucht, die unterschritte eines gelöschten Schrittes zu finden
					continue;
				}
				schritt.setAenderungsart(art);
				//schritt.getshef().setPlainText(schritt.getshef().getPlainText());
            	schritt.getshef().setStyle(schritt.getPlainText(), TextfieldShef.standardStil);
				schritt.setBackground(TextfieldShef.Hintergrundfarbe_Standard);
				schritt.getText().setEnabled(true);
				aenderungsMarkierungenUndEnumsEntfernen(schritt);
			}

			//setzt die Unterschritte eines Schrittes auf die Aenderungsart geloescht
			if(art == Aenderungsart.Geloescht) {
            	schritt.getshef().setStyle(schritt.getPlainText(), TextfieldShef.ganzerSchrittGeloeschtStil);
            	schritt.setBackground(TextfieldShef.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
            	schritt.getText().setEnabled(false);
            	aenderungsMarkierungenAufGeloescht(schritt);
            	schritt.setAenderungsart(Aenderungsart.Geloescht);
			}

			//gibt den Schritt zur überprüfung auf Unterschritte
			ohneSchleife(schritt, art);
		}
	}

	//TODO Uebernehmen methode mit abfrage welche art zugewiesen ist
	public void uebernehmenAbfrage(List<AbstractSchrittView> schritte) {

		for (AbstractSchrittView schritt: schritte) {

			//Übernehmen von Änderungen, bei Änderungen an den Zweigen, diese werden nicht in der Schritte liste durchlaufen
			if (schritt.getClass().getName().equals("specman.view.CaseSchrittView")) {
				CaseSchrittView caseSchritt = (CaseSchrittView) schritt;
				//nur für caseSequenzen möglich, da man die Sonstsequenz eh nicht löschen kann

				//TODO es funktioniert, ist aber glaube ich keine schöne Lösung
				//Wir spiegeln die Liste einmal auf eine CopyOnWriteArrayList um zweige während des durchlaufens bearbeiten zu können
				List<ZweigSchrittSequenzView> caseSequenzen = new CopyOnWriteArrayList<ZweigSchrittSequenzView>(caseSchritt.getCaseSequenzen());
				for (ZweigSchrittSequenzView caseSequenz : caseSequenzen) {
					if(caseSequenz.getAenderungsart() == Aenderungsart.Geloescht){
						int zweigIndex = caseSchritt.zweigEntfernen(Specman.this, caseSequenz);
						undoManager.addEdit(new UndoableZweigEntfernt(Specman.this, caseSequenz, caseSchritt, zweigIndex));
					}
					if(caseSequenz.getAenderungsart() == Aenderungsart.Hinzugefuegt){
						caseSequenz.getUeberschrift().setStyle(schritt.getPlainText(), TextfieldShef.standardStil);
						caseSequenz.getUeberschrift().setBackground(TextfieldShef.Hintergrundfarbe_Standard);
						caseSequenz.setAenderungsart(null);
						recrusiv(caseSequenz.schritte, null);
					}
				}
			}

			System.out.println(schritt.getClass());

			//wird bei keiner gesetzten Änderungsart durchlaufen
			if (schritt.getAenderungsart() == null) {
				System.out.println("Es liegen keine Aenderungen vor");
			}

			//wird bei der Aenderungsart hinzugefügt durchlaufen
			//entfernt alle Text/Schrittmarkierungen
			if(schritt.getAenderungsart() == Aenderungsart.Hinzugefuegt) {
				System.out.println("Hinzugefuegt");
				schritt.setAenderungsart(null);
				schritt.getshef().setStyle(schritt.getPlainText(), TextfieldShef.standardStil);
				schritt.setBackground(TextfieldShef.Hintergrundfarbe_Standard);
				aenderungsMarkierungenUndEnumsEntfernen(schritt);
			}

			//TODO noch nicht implementiert
			if(schritt.getAenderungsart() == Aenderungsart.Bearbeitet) {
				/*System.out.println("Es wurde eine Änderung vorgenommen");
				schritt.setAenderungsart(null);
				schritt.getshef().setStyle(schritt.getPlainText(), TextfieldShef.standardStil);
				schritt.setBackground(TextfieldShef.Hintergrundfarbe_Standard);
				*/
				String neuerString="";
				for (int textstelle = 0; textstelle<schritt.getshef().getPlainText().length();textstelle++) {
					char char1 = schritt.getshef().getPlainText().charAt(textstelle);
					if (schritt.getshef().aenderungsStilCheck() == true) {
						System.out.println(schritt.getshef().getPlainText().charAt(textstelle) + " und n");
						neuerString = neuerString + String.valueOf(char1);
					} else if (schritt.getshef().geloeschtStilCheck() == true) {
						System.out.println(schritt.getshef().getPlainText().charAt(textstelle) + " und g");
						neuerString = neuerString;
					} else {
						neuerString += String.valueOf(char1);
						System.out.println(schritt.getshef().getPlainText().charAt(textstelle) + " und u");
					}
				}
				System.out.println("Neuer String lautet"+neuerString);
				schritt.getshef().setPlainText(neuerString);
				schritt.getshef().setStyle(schritt.getPlainText(),TextfieldShef.standardStil);
				schritt.setAenderungsart(null);

			}

			if(schritt.getAenderungsart() == Aenderungsart.Geloescht) {
				System.out.println("Geloescht");
				SchrittSequenzView sequenz = schritt.getParent();
				int schrittIndex = sequenz.schrittEntfernen(schritt);
				undoManager.addEdit(new UndoableSchrittEntfernt(schritt, sequenz, schrittIndex));
				//continue damit er nicht versucht, die unterschritte eines gelöschten Schrittes zu finden
				continue;
			}
			if (schritt.getClass().getName().equals("specman.view.IfElseSchrittView") || schritt.getClass().getName().equals("specman.view.IfSchrittView")) {
				IfElseSchrittView ifel = (IfElseSchrittView) schritt;
				uebernehmenAbfrage(ifel.getElseSequenz().schritte);
				if (schritt.getClass().getName().equals("specman.view.IfElseSchrittView")) {
					uebernehmenAbfrage(ifel.getIfSequenz().schritte);
				}
			}
			else if (schritt.getClass().getName().equals("specman.view.WhileSchrittView") || schritt.getClass().getName().equals("specman.view.WhileWhileSchrittView")) {
				SchleifenSchrittView schleife = (SchleifenSchrittView) schritt;
				uebernehmenAbfrage(schleife.getWiederholSequenz().schritte);
			}
			else if (schritt.getClass().getName().equals("specman.view.CaseSchrittView")) {
				CaseSchrittView caseSchritt = (CaseSchrittView) schritt;
				uebernehmenAbfrage(caseSchritt.getSonstSequenz().schritte);
				for (ZweigSchrittSequenzView caseSequenz : caseSchritt.getCaseSequenzen()) {
					uebernehmenAbfrage(caseSequenz.schritte);
				}
			}
			else if (schritt.getClass().getName().equals("specman.view.SubsequenzSchrittView")) {
				SubsequenzSchrittView sub = (SubsequenzSchrittView) schritt;
				uebernehmenAbfrage(sub.getSequenz().schritte);
			}
		}
	}

	//TODO
	//Überschriften von If/Else und Cases zurücksetzen
	//Der Teil setzt alle Änderungsmarkierungen auf den Standardwert zurück
	public void aenderungsMarkierungenUndEnumsEntfernen(AbstractSchrittView schritt) {
		if(schritt.getClass().getName().equals("specman.view.IfElseSchrittView") || schritt.getClass().getName().equals("specman.view.IfSchrittView")) {
			IfElseSchrittView ifel= (IfElseSchrittView) schritt;
			schritt.getshef().setStyle(schritt.getPlainText(), TextfieldShef.standardStil);
			//schritt.setPlainText(schritt.getshef().getPlainText(), StyleConstants.ALIGN_CENTER);
			ifel.getElseSequenz().getUeberschrift().setStyle(schritt.getPlainText(), TextfieldShef.standardStil);
			//ifel.getElseSequenz().getUeberschrift().setPlainText(ifel.getElseSequenz().getUeberschrift().getPlainText(), StyleConstants.ALIGN_RIGHT);
			ifel.getElseSequenz().getUeberschrift().getTextComponent().setEnabled(true);
			if(schritt.getClass().getName().equals("specman.view.IfElseSchrittView")) {
				ifel.getIfSequenz().getUeberschrift().setStyle(schritt.getPlainText(), TextfieldShef.standardStil);
				ifel.getIfSequenz().getUeberschrift().getTextComponent().setEnabled(true);
            }
		}
		if(schritt.getClass().getName().equals("specman.view.CaseSchrittView")) {
			CaseSchrittView caseSchritt = (CaseSchrittView) schritt;
			caseSchritt.getSonstSequenz().getUeberschrift().setStyle(schritt.getPlainText(), TextfieldShef.standardStil);
			caseSchritt.getSonstSequenz().getUeberschrift().getTextComponent().setEnabled(true);
			for (ZweigSchrittSequenzView caseSequenz : caseSchritt.getCaseSequenzen()) {
				caseSequenz.getUeberschrift().setStyle(schritt.getPlainText(), TextfieldShef.standardStil);
				caseSequenz.setBackground(TextfieldShef.Hintergrundfarbe_Standard);
				caseSequenz.getUeberschrift().getTextComponent().setEnabled(true);
			}
		}
	}

	//TODO
	public void aenderungsMarkierungenAufGeloescht(AbstractSchrittView schritt) {
		schritt.getText().setEnabled(false);
		if(schritt.getClass().getName().equals("specman.view.IfElseSchrittView") || schritt.getClass().getName().equals("specman.view.IfSchrittView")) {
			IfElseSchrittView ifel= (IfElseSchrittView) schritt;
			ifel.getElseSequenz().getUeberschrift().setStyle(ifel.getPlainText(), TextfieldShef.ganzerSchrittGeloeschtStil);
			ifel.getElseSequenz().getUeberschrift().getTextComponent().setEnabled(false);
			if(schritt.getClass().getName().equals("specman.view.IfElseSchrittView")) {
				ifel.getIfSequenz().getUeberschrift().setStyle(ifel.getPlainText(), TextfieldShef.ganzerSchrittGeloeschtStil);
				ifel.getIfSequenz().getUeberschrift().getTextComponent().setEnabled(false);
            }
		}
		if(schritt.getClass().getName().equals("specman.view.CaseSchrittView")) {
			CaseSchrittView caseSchritt = (CaseSchrittView) schritt;
			caseSchritt.getSonstSequenz().getUeberschrift().setStyle(caseSchritt.getPlainText(), TextfieldShef.ganzerSchrittGeloeschtStil);
			caseSchritt.getSonstSequenz().getUeberschrift().getTextComponent().setEnabled(false);
			for (ZweigSchrittSequenzView caseSequenz : caseSchritt.getCaseSequenzen()) {
				caseSequenz.getUeberschrift().setStyle(caseSequenz.getUeberschrift().getPlainText(), TextfieldShef.ganzerSchrittGeloeschtStil);
				caseSequenz.getUeberschrift().getTextComponent().setEnabled(false);
			}
		}
	}
	public void pruefeFuerSchrittnummer() {
		List<AbstractSchrittView> schritte = new CopyOnWriteArrayList<AbstractSchrittView>();
		schritte = hauptSequenz.schritte;
		if(instance != null && instance.aenderungenVerfolgen()) {
			for (AbstractSchrittView schritt : schritte) {

				//wird bei keiner gesetzten Änderungsart durchlaufen
				if (schritt.getAenderungsart() == Aenderungsart.Geloescht) {
					schritt.getshef().schrittNummer.setText("<html><body><span style='text-decoration: line-through;'>"+schritt.getshef().schrittNummer.getText()+"</span></body></html>");
				}
				if (schritt.getAenderungsart() == Aenderungsart.Quellschritt) {
					schritt.getshef().schrittNummer.setText("<html><body><span style='text-decoration: line-through;'>" + schritt.getshef().schrittNummer.getText() + "</span><span>&rArr</span><span>" + "B" + "</span></body></html>");
				}
				if (schritt.getAenderungsart() == Aenderungsart.Zielschritt) {
					schritt.getshef().schrittNummer.setText("<html><body><span>" + "A" + "</span><span>&lArr</span><span style='text-decoration: line-through;'>"+schritt.getshef().schrittNummer.getText()+"</span></body></html>");
				}

			}
		}
		else
			return;
	}
}
