package specman;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import net.atlanticbb.tantlinger.shef.HTMLEditorPane;
import specman.draganddrop.DragMouseAdapter;
import specman.draganddrop.GlassPane;
import specman.model.ModelEnvelope;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.SchrittSequenzModel_V001;
import specman.model.v001.StruktogrammModel_V001;
import specman.pdf.PDFRenderer;
import specman.pdf.Shape;
import specman.textfield.EditContainer;
import specman.textfield.TextEditArea;
import specman.undo.UndoableDiagrammSkaliert;
import specman.undo.UndoableSchrittEingefaerbt;
import specman.undo.UndoableSchrittEntfernt;
import specman.undo.UndoableSchrittHinzugefuegt;
import specman.undo.UndoableToggleStepBorder;
import specman.undo.UndoableZweigEntfernt;
import specman.undo.UndoableZweigHinzugefuegt;
import specman.undo.manager.SpecmanUndoManager;
import specman.undo.manager.UndoRecording;
import specman.undo.manager.UndoRecordingMode;
import specman.view.AbstractSchrittView;
import specman.view.BreakSchrittView;
import specman.view.CaseSchrittView;
import specman.view.CatchSchrittView;
import specman.view.QuellSchrittView;
import specman.view.SchrittSequenzView;
import specman.view.ZweigSchrittSequenzView;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.UndoableEdit;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static specman.textfield.TextStyles.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE;
import static specman.textfield.TextStyles.INDIKATOR_GELB;
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

	TextEditArea lastFocusedTextArea;
	public SchrittSequenzView hauptSequenz;
	JPanel arbeitsbereich;
	JPanel hauptSequenzContainer;
	SpaltenResizer breitenAnpasser;
	JScrollPane scrollPane;
	EditContainer intro, outro;
	FormLayout hauptlayout;
	int diagrammbreite = INITIAL_DIAGRAMM_WIDTH;
	int zoomFaktor = 100;
	Integer dragX;
	File diagrammDatei;
	List<AbstractSchrittView> postInitSchritte;
	RecentFiles recentFiles;
	private JComponent welcomeMessage;

	//TODO window for dragging
	public final JWindow window = new JWindow();
	private final Set<Integer> pressedKeys = new HashSet<>();
	public static final String SPECMAN_TITLE = "Specman";

	public Specman() throws Exception {
		setApplicationIcon();
		setTitle(SPECMAN_TITLE);

		recentFiles = new RecentFiles(this);
		undoManager = new SpecmanUndoManager(this);

		initComponents();

		initShefController();
		//initJWebengineController();

		hauptSequenz = new SchrittSequenzView();

		scrollPane = new JScrollPane();
		//TODO
		scrollPane.addMouseWheelListener(new DragMouseAdapter(this));
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

		intro = new EditContainer(this);
		intro.setOpaque(false);
		arbeitsbereich.add(intro, CC.xy(2, 2));
		intro.addEditAreasFocusListener(this);

		outro = new EditContainer(this);
		outro.setOpaque(false);
		arbeitsbereich.add(outro, CC.xy(2, 4));
		outro.addEditAreasFocusListener(this);

		scrollPane.setViewportView(arbeitsbereich);
		actionListenerHinzufuegen();
		setInitialWindowSizeAndScreenCenteredLocation();
		setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Falls jemand nicht aufgepasst hat und beim Initialisieren irgendwelche Funktionen verwendet hat,
		// die schon etwas im Undo-Manager hinterlassen.
		undoManager.discardAllEdits();
		this.setGlassPane(new GlassPane((SwingUtilities.convertPoint(this.getContentPane(), 0, 0,this).y)-getJMenuBar().getHeight()));

		configureKeyboardManager();
		setupQuestionDialogWhenClosingWithoutSaving();
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

	/**
	 * Sets up a KeyEventDispatcher to provide a list of pressed keys used by another container.
	 */
	private void configureKeyboardManager() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				pressedKeys.add(e.getKeyCode());
			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
				pressedKeys.remove(e.getKeyCode());
			}
			return false;
		});
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

	private void setupQuestionDialogWhenClosingWithoutSaving() {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (undoManager.hasUnsavedChanges()) {
					int dialogResult = JOptionPane.showConfirmDialog(Specman.instance,
							"Änderungen am Dokument '" + getDiagramFilename() + "' vor dem Schließen speichern?" +
									"\nIhre Änderungen gehen verloren, wenn Sie diese nicht speichern.",
							"Diagramm speichern?", JOptionPane.YES_NO_CANCEL_OPTION);

					if (dialogResult == JOptionPane.CANCEL_OPTION) { // Prevent closing
						return;
					} else if (dialogResult == JOptionPane.YES_OPTION) { // Save & Close
						diagrammSpeichern(false);
					}
                }

				dispose();
				System.exit(0);
			}
		});
	}

	private String getDiagramFilename() {
		if (diagrammDatei != null) {
			return diagrammDatei.getName();
		}
		return "Unbekannt";
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
		setLastFocusedTextArea(e);
	}

	@Override public void focusLost(FocusEvent e) {
		setLastFocusedTextArea(e);
	}

	private void setLastFocusedTextArea(FocusEvent e) {
		if (e.getSource() instanceof TextEditArea) {
			lastFocusedTextArea = (TextEditArea) e.getSource();
		}
	}

	private void setDiagrammDatei(File diagrammDatei) {
		this.diagrammDatei = diagrammDatei;
		setTitle(getDiagramFilename() + " - "+ SPECMAN_TITLE);
	}

	private void fehler(String text) {
		JOptionPane.showMessageDialog(this, text);
	}

	private void actionListenerHinzufuegen() {
		schrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(lastFocusedTextArea);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().einfachenSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.einfachenSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
				hauptSequenz.resyncSchrittnummerStil();
			}
		});

		whileSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(lastFocusedTextArea);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().whileSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.whileSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
				hauptSequenz.resyncSchrittnummerStil();
			}
		});

		whileWhileSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(lastFocusedTextArea);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().whileWhileSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.whileWhileSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
				hauptSequenz.resyncSchrittnummerStil();
			}
		});

		ifElseSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(lastFocusedTextArea);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().ifElseSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.ifElseSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
				hauptSequenz.resyncSchrittnummerStil();
			}
		});

		ifSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(lastFocusedTextArea);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().ifSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.ifSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
				hauptSequenz.resyncSchrittnummerStil();
			}
		});

		caseSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(lastFocusedTextArea);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().caseSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.caseSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
				hauptSequenz.resyncSchrittnummerStil();
			}
		});

		subsequenzSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(lastFocusedTextArea);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().subsequenzSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.subsequenzSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
				hauptSequenz.resyncSchrittnummerStil();
			}
		});

		breakSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(lastFocusedTextArea);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().breakSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.breakSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
				hauptSequenz.resyncSchrittnummerStil();
			}
		});

		catchSchrittAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView referenceStep = hauptSequenz.findeSchritt(lastFocusedTextArea);
				AbstractSchrittView schritt = (referenceStep != null)
						? referenceStep.getParent().catchSchrittZwischenschieben(After, referenceStep, Specman.this)
						: hauptSequenz.catchSchrittAnhaengen(Specman.this);
				newStepPostInit(schritt);
				hauptSequenz.resyncSchrittnummerStil();
			}
		});

		caseAnhaengen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropWelcomeMessage();
				AbstractSchrittView schritt = hauptSequenz.findeSchritt(lastFocusedTextArea);
				if (!(schritt instanceof CaseSchrittView)) {
					fehler("Kein Case-Schritt ausgewählt");
					return;
				}
				CaseSchrittView caseSchritt = (CaseSchrittView)schritt;
				ZweigSchrittSequenzView ausgewaehlterZweig = caseSchritt.istZweigUeberschrift(lastFocusedTextArea);
				if (ausgewaehlterZweig == null) {
					fehler("Kein Zweig ausgewählt");
					return;
				}
				ZweigSchrittSequenzView neuerZweig = caseSchritt.neuenZweigHinzufuegen(Specman.this, ausgewaehlterZweig);
				addEdit(new UndoableZweigHinzugefuegt(Specman.this, neuerZweig, caseSchritt));
				schritt.skalieren(zoomFaktor, 100);
				diagrammAktualisieren(schritt);
				hauptSequenz.resyncSchrittnummerStil();
			}
		});

		exportPDF.addActionListener((e -> {
            exportAsPDF();
        }));

		einfaerben.addActionListener(e -> {
            if (lastFocusedTextArea != null) {
                AbstractSchrittView schritt = hauptSequenz.findeSchritt(lastFocusedTextArea);
                Color aktuelleHintergrundfarbe = schritt.getBackground();
                int farbwert = aktuelleHintergrundfarbe.getRed() == 240 ? 255 : 240;
                Color neueHintergrundfarbe = new Color(farbwert, farbwert, farbwert);
                schritt.setBackground(neueHintergrundfarbe);
                addEdit(new UndoableSchrittEingefaerbt(schritt, aktuelleHintergrundfarbe, neueHintergrundfarbe));
            }
        });

		loeschen.addActionListener(e -> {
			if (lastFocusedTextArea == null) {
				return;
			}
            try {
                AbstractSchrittView schritt = hauptSequenz.findeSchritt(lastFocusedTextArea);
                if (schritt == null) {
                    // Sollte nur der Fall sein, wenn man den Fokus im Intro oder Outro stehen hat
                    fehler("Ups - niemandem scheint das Feld zu gehören, in dem steht: " + lastFocusedTextArea.getText());
                    return;
                }

                //Der Teil wird nur durchlaufen, wenn die Aenderungsverfolgung aktiviert ist
                if (instance != null && instance.aenderungenVerfolgen() && schritt.getAenderungsart() != Aenderungsart.Hinzugefuegt) {
                    //Muss hinzugefügt werden um zu gucken ob die Markierung schon gesetzt wurde
                    if (schritt.getAenderungsart() != Aenderungsart.Geloescht) {
                        schrittAlsGeloeschtMarkieren(schritt);
						hauptSequenz.resyncSchrittnummerStil();
                    }
                } else {
					//Hier erfolgt das richtige Löschen, Aenderungsverfolgung nicht aktiviert
					try (UndoRecording ur = composeUndo()) {
						if (schritt instanceof CaseSchrittView) {
							CaseSchrittView caseSchritt = (CaseSchrittView) schritt;
							ZweigSchrittSequenzView zweig = caseSchritt.istZweigUeberschrift(lastFocusedTextArea);
							if (zweig != null) {
								int zweigIndex = caseSchritt.zweigEntfernen(Specman.this, zweig);
								undoManager.addEdit(new UndoableZweigEntfernt(Specman.this, zweig, caseSchritt, zweigIndex));
							}
							return;
						}
                        if (isStepDeletionAllowed(schritt)) {
							schritt.markStepnumberLinksAsDefect();
                            SchrittSequenzView sequenz = schritt.getParent();
                            int schrittIndex = sequenz.schrittEntfernen(schritt);
                            undoManager.addEdit(new UndoableSchrittEntfernt(schritt, sequenz, schrittIndex));
							hauptSequenz.resyncSchrittnummerStil();
                        }
					}
				}
            }
            catch (EditException ex) {
                showError(ex);
            }
        });


		toggleBorderType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractSchrittView schritt = hauptSequenz.findeSchritt(lastFocusedTextArea);
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

		exportAsPDFMenuItem.addActionListener(e -> exportAsPDF());

		exitMenuItem.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

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

		birdsview.addActionListener(e -> {
            if (hauptSequenzContainer == null) {
                return;
            }
            final int breite = hauptSequenzContainer.getBounds().width;
            final int hoehe = hauptSequenzContainer.getBounds().height;
            final Image i = createImage(breite, hoehe);
            Graphics g = i.getGraphics();
            hauptSequenzContainer.paint(g);
            final JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            final Image scaledImage = i.getScaledInstance(breite / 5, hoehe / 5,  Image.SCALE_SMOOTH);
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
                    } else {
                        skalierteBreite = p.getSize().width;
                        skalierteHoehe = (int)(hoehe * breitenFaktor);
                    }
                    if (skalierteBreite > 0 && skalierteHoehe > 0) {
                        Image neuSkaliert = i.getScaledInstance(skalierteBreite, skalierteHoehe,  Image.SCALE_SMOOTH);
                        l.setIcon(new ImageIcon(neuSkaliert));
                    }
                }
            });
            d.getContentPane().add(p);
            d.pack();
            d.setVisible(true);
        });

		aenderungenUebernehmen.addActionListener(e -> {
			try (UndoRecording ur = composeUndo()) {
				try {
					int changesMade = hauptSequenz.aenderungenUebernehmen(Specman.this);
					if (changesMade > 0) {
						diagrammAktualisieren(null);
					} else {
						JOptionPane.showMessageDialog(this, "Das Diagramm enthält keine Änderungen.");
					}
				} catch (EditException ex) {
					showError(ex);
				}
			}
        });

		aenderungenVerwerfen.addActionListener(e -> {
			try (UndoRecording ur = composeUndo()) {
				try {
					int changesReverted = hauptSequenz.aenderungenVerwerfen(Specman.this);
					if (changesReverted > 0) {
						diagrammAktualisieren(null);
					} else {
						JOptionPane.showMessageDialog(this, "Das Diagramm enthält keine Änderungen.");
					}
				} catch (EditException ex) {
					showError(ex);
				}
			}
        });

	}

	public void addImageViaFileChooser() {
		if (lastFocusedTextArea != null) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File("."));
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif", "bmp"));
			fileChooser.setAcceptAllFileFilterUsed(true);
			int result = fileChooser.showOpenDialog(arbeitsbereich);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				if (selectedFile != null && selectedFile.exists()) {
					lastFocusedTextArea.addImage(selectedFile, initialArt());
					diagrammAktualisieren(null);
				}
			}
		}
	}

	private void schrittAlsGeloeschtMarkieren(AbstractSchrittView schritt) throws EditException {
		//Es wird geschaut, ob der Schritt nur noch alleine ist und überhaupt gelöscht werden darf
		if (isStepDeletionAllowed(schritt)) {
            try (UndoRecording ur = composeUndo()) {
                addEdit(schritt.alsGeloeschtMarkieren(this));
            }
        }

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
			aenderungenVerfolgen.setSelected(false);
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
			intro.setEditorContent(this, model.intro);
			intro.skalieren(zoomFaktor, 0);
			outro.setEditorContent(this, model.outro);
			outro.skalieren(zoomFaktor, 0);
			setName(model.name);
			hauptSequenz = new SchrittSequenzView(this, null, model.hauptSequenz);

			hauptSequenzInitialisieren();
			neueSchritteNachinitialisieren();
			quellZielZuweisung(model.queryAllSteps());
			hauptSequenz.viewsNachinitialisieren();
			aenderungenVerfolgen.setSelected(model.changeModeenabled);
			recentFiles.add(diagramFile);
			undoManager.discardAllEdits();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void quellZielZuweisung(List<AbstractSchrittModel_V001> allModelSteps) {
		for(AbstractSchrittModel_V001 modelStep: allModelSteps) {
			if (modelStep.quellschrittID != null) {
				AbstractSchrittView zielschritt = hauptSequenz.findeSchrittZuId(modelStep.id);
				if(zielschritt instanceof QuellSchrittView) {
					continue;
				}
				else {
					QuellSchrittView quellSchritt = (QuellSchrittView) hauptSequenz.findeSchrittZuId(modelStep.quellschrittID);
					zielschritt.setQuellschritt(quellSchritt);
					quellSchritt.setZielschritt(zielschritt);
				}
			}
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
		// Null-Abfrage ist für den Fall, dass der User etwas im Intro oder Outro macht,
		// bevor er überhaupt das Diagramm angefangen hat. Sollte man später noch mal
		// bereinigen, dass das gar nicht geht, solange die Welcome Message noch angezeigt wird.
		if (hauptSequenzContainer != null) {
			hauptSequenzContainer.setVisible(false);
			// Folgende Zeile forciert ein Relayouting, falls z.B. nur eine manuelle Breitenänderung
			// einer If-Else-Spaltenteilung stattgefunden hat.
			diagrammbreiteSetzen(diagrammbreite-1);
			final Point viewPosition = scrollPane.getViewport().getViewPosition();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					diagrammbreiteSetzen(diagrammbreite);
					hauptSequenzContainer.setVisible(true);
					if (schrittImFokus != null) {
						schrittImFokus.requestFocus();
					}
					scrollPane.getViewport().setViewPosition(viewPosition);
				}
			});
		}
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
		exportPDF = new JButton();
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
		speichern.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		speichernUnter = new JMenuItem("Speichern unter...");
		speichernUnter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
		laden = new JMenuItem("Laden...");
		laden.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
		exportAsPDFMenuItem = new JMenuItem("Als PDF exportieren...");
		exportAsPDFMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));
		exitMenuItem = new JMenuItem("Beenden");
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
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
		buttonBar.addSeparator();
		toolbarButtonHinzufuegen(exportPDF, "pdf", "PDF exportieren", buttonBar);
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
		DragMouseAdapter dragButtonAdapter = new DragMouseAdapter(this);
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

	private void addDragAdapter(JButton button, DragMouseAdapter adapter) {
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
	SpecmanUndoManager undoManager;

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
		dateiMenu.add(exportAsPDFMenuItem);
		dateiMenu.add(exitMenuItem);
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
	private JButton exportPDF;
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
	private JMenuItem exportAsPDFMenuItem;
	private JMenuItem exitMenuItem;
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
			aenderungenVerfolgen(),
			hauptSequenz.generiereSchittSequenzModel(formatierterText),
			intro.editorContent2Model(formatierterText),
			outro.editorContent2Model(formatierterText));
		return model;
	}

	public void exportAsGraphviz() {
		SchrittSequenzModel_V001 model = hauptSequenz.generiereSchittSequenzModel(false);
		try {
			new GraphvizExporter("export.gv").export(model);
		}
		catch(IOException iox) {
			iox.printStackTrace();
		}
	}

	public void exportAsPDF() {
		final String PDF_EXTENSION = ".pdf";

//        JFileChooser fileChooser = new JFileChooser();
//        fileChooser.setCurrentDirectory(new File("."));
//        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(PDF_EXTENSION, "pdf"));
//        fileChooser.setAcceptAllFileFilterUsed(true);
//        int result = fileChooser.showSaveDialog(arbeitsbereich);
//        if (result == JFileChooser.APPROVE_OPTION) {
//            File selectedFile = fileChooser.getSelectedFile();
						File selectedFile = new File("sample.pdf");
            if (selectedFile != null) {
                Shape all = new Shape(arbeitsbereich.getBounds().getLocation())
									.add(new Shape(intro))
									.add(hauptSequenz.getShapeSequence())
									.add(breitenAnpasser.getShape())
									.add(new Shape(outro));
                new PDFRenderer(selectedFile.getAbsolutePath()).render(all);
            }
//        }
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

	public static EditorContentModel_V001 initialtext(String text) {
		String styledText = (instance() != null && instance().aenderungenVerfolgen()) ?
				"<span style=\"background-color:" + INDIKATOR_GELB + "\">" + text + "</span>" :
				text;
		return new EditorContentModel_V001(styledText);
	}

	public static Color schrittHintergrund() {
		return (instance() != null && instance().aenderungenVerfolgen()) ?
			AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE : Color.white;
	}

	//Methode um die Aenderugnsart von neuen Schritten auf hinzugefügt zu ändern, wenn die Änderungsverfolgung aktiviert ist
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

	public SpecmanUndoManager getUndoManager() {
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

	public boolean isStepDeletionAllowed(AbstractSchrittView schritt) throws EditException {
        int geloeschtzaehler = 1;
        for (AbstractSchrittView suchSchritt : schritt.getParent().schritte) {
            if (suchSchritt.getAenderungsart() == Aenderungsart.Geloescht) {
                geloeschtzaehler++;
            }
        }
        if (schritt.getParent().schritte.size() <= geloeschtzaehler) {
            throw new EditException("Der letzte Schritt kann nicht entfernt werden.");
        }
        if (schritt.hasStepnumberLinks()) {
            int dialogResult = JOptionPane.showConfirmDialog(this,
                    "Der zu löschende Schritt wird referenziert. Möchten Sie den Schritt " +
                            "wirklich löschen?\nDie Referenzen werden dann als 'Defekt' markiert.",
                    "Verknüpfte Schrittreferenzen", JOptionPane.OK_CANCEL_OPTION);
            return dialogResult == JOptionPane.OK_OPTION;
        }
        return true;
	}

	@Override public TextEditArea getLastFocusedTextArea() {
		return lastFocusedTextArea;
	}

	public void showError(EditException ex) {
		fehler(ex.getMessage());
	}

	public double getScaledLength(double length) {
		return length * getZoomFactor() * 0.01;
	}

	public UndoRecording pauseUndo() {
		return new UndoRecording(this.getUndoManager(), UndoRecordingMode.Paused);
	}

	public UndoRecording composeUndo() {
		return new UndoRecording(this.getUndoManager(), UndoRecordingMode.Composing);
	}

	public List<AbstractSchrittView> getAllSteps() {
		return getSteps(getHauptSequenz().getSchritte());
	}

	private static List<AbstractSchrittView> getSteps(final List<AbstractSchrittView> steps) {
		List<AbstractSchrittView> allSteps = new ArrayList<>();

		for (AbstractSchrittView step : steps) {
			allSteps.add(step);
			for (SchrittSequenzView unterSequenz : step.unterSequenzen()) {
				List<AbstractSchrittView> schritte = unterSequenz.getSchritte();
				if (!schritte.isEmpty()) {
					allSteps.addAll(getSteps(schritte));
				}
			}
		}

		return allSteps;
	}

	/**
	 * Finds a step by their StepID and throws an exception if it doesn't exist.
	 */
	@Override
	public AbstractSchrittView findStepByStepID(String stepID) {
		AbstractSchrittView result = findStepByStepID(getHauptSequenz().getSchritte(), stepID);
		if (result == null) {
			throw new RuntimeException("Could not find stepnumber '" + stepID + "'."
					+ " Make sure not to search for an outdated stepnumber.");
		}
		return result;
	}

	/**
	 * Returns a step if the provided stepnumber matches.
	 * This could be optimized by skipping all steps after the position where the step is supposed to be.
	 * <p>
	 * However, this case shouldn't be called since that means we are searching for a non-existing step which
	 * currently isn't possible - Except due to a bug, that's why we throw the Exception above.
	 */
	private static AbstractSchrittView findStepByStepID(final List<AbstractSchrittView> steps, String stepID) {
		for (AbstractSchrittView step : steps) {
			if (stepID.equals(step.getId().toString())) {
				return step;
			}
			for (SchrittSequenzView unterSequenz : step.unterSequenzen()) {
				List<AbstractSchrittView> schritte = unterSequenz.getSchritte();
				AbstractSchrittView result = findStepByStepID(schritte, stepID);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public boolean isKeyPressed(int keyCode) {
		return pressedKeys.contains(keyCode);
	}
}