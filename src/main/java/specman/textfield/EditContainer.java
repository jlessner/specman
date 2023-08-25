package specman.textfield;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import net.atlanticbb.tantlinger.shef.HTMLEditorPane;
import specman.Aenderungsart;
import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.AbstractEditAreaModel_V001;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.ImageEditAreaModel_V001;
import specman.model.v001.TextEditAreaModel_V001;
import specman.pdf.FormatedShapeText;
import specman.pdf.LabelShapeText;
import specman.pdf.Shape;
import specman.undo.UndoableImageAdded;
import specman.undo.UndoableImageRemoved;
import specman.undo.manager.UndoRecording;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
public class EditContainer extends JPanel {
	private final static RowSpec EDITAREA_LAYOUT_ROWSPEC = RowSpec.decode("fill:pref:grow");

	// ACHTUNG: Das ist hier noch auf halbem Wege. Später wird es eine Liste von EditAreas geben
	private final List<EditArea> editAreas = new ArrayList<>();
	private final List<FocusListener> editAreasFocusListeners = new ArrayList<>();
	private final List<ComponentListener> editAreasComponentListeners = new ArrayList<>();
	private final SchrittNummerLabel schrittNummer;
	private FormLayout layout;
	private EmptyBorder editorPaneBorder;
	private Indentions indentions;
	private boolean schrittNummerSichtbar = true;

	public EditContainer(EditorI editor, String initialContent, String schrittId) {
		this(editor, new EditorContentModel_V001(initialContent), schrittId);
	}

	public EditContainer(EditorI editor, EditorContentModel_V001 initialContent, String schrittId) {
		if (schrittId != null) {
			schrittNummer = new SchrittNummerLabel(schrittId);
			setEnabled(false);
		} else {
			schrittNummer = null;
		}

		initLayoutAndEditAreas(editor, initialContent);
		updateDecorationIndentions(new Indentions());
		setBackground(schrittHintergrund());

    skalieren(editor.getZoomFactor(), 0);
  }

	private void initLayout(EditorContentModel_V001 content) {
		List<RowSpec> rowSpecs = new ArrayList<>();
		rowSpecs.add(RowSpec.decode("0px"));
		rowSpecs.add(RowSpec.decode("0px"));
		layout = new FormLayout("0px,10px:grow,0px", "0px,0px");
		setLayout(layout);
	}

	private void initLayoutAndEditAreas(EditorI editor, EditorContentModel_V001 content) {
		editAreas.stream().forEach(ea -> remove(ea.asComponent()));
		editAreas.clear();
		initLayout(content);
		int index = 0;
		for (AbstractEditAreaModel_V001 editAreaModel: content.areas) {
			EditArea editArea;
			if (editAreaModel instanceof TextEditAreaModel_V001) {
				TextEditAreaModel_V001 textEditAreaModel = (TextEditAreaModel_V001)editAreaModel;
				editArea = new TextEditArea(editor, textEditAreaModel.text, schrittHintergrund());
			}
			else if (editAreaModel instanceof ImageEditAreaModel_V001) {
				ImageEditAreaModel_V001 imageEditAreaModel = (ImageEditAreaModel_V001)editAreaModel;
				editArea = new ImageEditArea(imageEditAreaModel);
			}
			else {
				throw new RuntimeException("Noch nicht fertig: " + editAreaModel);
			}
			addEditArea(editArea, index++);
		}
		if (schrittNummer != null) {
			editAreas.get(0).addSchrittnummer(schrittNummer);
		}
	}

	public EditContainer(EditorI editor) {
		this(editor, new EditorContentModel_V001(""), null);
	}

	public void aenderungsmarkierungenEntfernen(SchrittID id) {
		editAreas.stream().forEach(ea -> ea.setStandardStil());
		setBackground(Hintergrundfarbe_Standard);
		if (schrittNummer != null) {
			schrittNummer.setStandardStil(id);
		}
	}

	public void setZielschrittStil(SchrittID quellschrittId) {
		schrittNummer.setZielschrittStil(quellschrittId);
	}

	public void setQuellStil(SchrittID zielschrittID) {
		editAreas.forEach(ea -> ea.setQuellStil());
		setBackground(AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
		schrittNummer.setQuellschrittStil(zielschrittID);
	}

	public void setGeloeschtMarkiertStil(SchrittID id) {
		setBackground(AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
		modifyableEditAreas().forEach(ea -> ea.markAsDeleted());
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
		EditorContentModel_V001 content = switch (orientation) {
			case StyleConstants.ALIGN_CENTER -> center(plainText);
			case StyleConstants.ALIGN_RIGHT -> right(plainText);
			default -> new EditorContentModel_V001(plainText);
		};
		initLayoutAndEditAreas(Specman.instance(), content);
	}

	public EditorContentModel_V001 editorContent2Model(boolean formatierterText) {
		List<AbstractEditAreaModel_V001> areaModels = editAreas.stream().map(ea -> ea.toModel(formatierterText)).collect(Collectors.toList());
		return new EditorContentModel_V001(areaModels);
	}

	public String getPlainText() {
		StringBuilder sb = new StringBuilder();
		editAreas.forEach(ea -> sb.append(ea.getPlainText()));
		return sb.toString();
	}

	public void updateBounds() {
		int maxEditWidth = getMaxEditAreasWidth();
		if (schrittNummer != null) {
			if (schrittNummerSichtbar) {
				Dimension schrittnummerGroesse = schrittNummer.getPreferredSize();
				schrittNummer.setBounds(maxEditWidth - schrittnummerGroesse.width,
					0,
					schrittnummerGroesse.width,
					schrittnummerGroesse.height - 2);
			} else {
				schrittNummer.setBounds(0, 0, 0, 0);
			}
		}
		editAreas.stream().forEach(ea -> ea.pack(maxEditWidth));
	}

	private int getMaxEditAreasWidth() {
		int maxWidth = 0;
		for (EditArea area: editAreas) {
			maxWidth = Math.max(maxWidth, area.getWidth());
		}
		return maxWidth;
	}

	public void schrittnummerAnzeigen(boolean sichtbar) {
		if (schrittNummer != null) {
			schrittNummerSichtbar = sichtbar;
			updateBounds(); // Sorgt dafür, dass der Label auch optisch sofort verschwindet
		}
	}

	public void skalieren(int prozentNeu, int prozentAktuell) {
		editAreas.forEach(ea -> ea.skalieren(prozentNeu, prozentAktuell));
		if (schrittNummer != null) {
			schrittNummer.setFont(labelFont.deriveFont((float) SCHRITTNR_FONTSIZE * prozentNeu / 100));
		}
		updateDecorationIndentions(indentions);
	}

	public static EditorContentModel_V001 right(String text) {
		return Specman.initialtext("<div align='right'>" + text + "</div>");
	}

	public static EditorContentModel_V001 center(String text) {
		return Specman.initialtext("<div align='center'>" + text + "</div>");
	}

	public Container getKlappButtonParent() { return schrittNummer.getParent(); }

	public void addEditAreasFocusListener(FocusListener focusListener) {
		editAreasFocusListeners.add(focusListener);
		editAreas.forEach(ea -> ea.addFocusListener(focusListener));
	}

	public void requestFocus() {
		editAreas.get(0).requestFocus();
	}

	public void setLeftInset(int px) {
		// TODO JL: Das ist hier noch nicht sauber. Die Insets von Text- und Imagebereichen sind nicht gleich, und
		// der oberste und unterste Editbereich haben unterschiedliche Top- und Bottom-Insets
		Insets insets = editorPaneBorder.getBorderInsets();
		insets.left = JEDITORPANE_DEFAULT_BORDER_THICKNESS + px;
		editorPaneBorder = new EmptyBorder(insets);
		editAreas.forEach(ea -> ea.setBorder(editorPaneBorder));
	}

	public void setRightInset(int px) {
		Insets insets = editorPaneBorder.getBorderInsets();
		insets.right = JEDITORPANE_DEFAULT_BORDER_THICKNESS + px;
		editorPaneBorder = new EmptyBorder(insets);
		editAreas.forEach(ea -> ea.setBorder(editorPaneBorder));
	}

	@Override
	public void setOpaque(boolean isOpaque) {
		super.setOpaque(isOpaque);
		// Null-Check ist notwendig, weil javax.swing.LookAndFeel bereits
		// im Konstruktor einen Aufruf von setBackground vornimmt.
		if (editAreas != null) {
			editAreas.forEach(ea -> ea.setOpaque(isOpaque));
		}
	}

	@Override
	public Color getBackground() {
		// Null- und Size-Check ist notwendig, weil javax.swing.LookAndFeel bereits
		// im Konstruktor einen Aufruf von getBackground vornimmt.
		return editAreas != null && editAreas.size() > 0 ? editAreas.get(0).getBackground() : super.getBackground();
	}

	public void updateDecorationIndentions(Indentions indentions) {
		this.indentions = indentions;

		layout.setRowSpec(1, indentions.topInset());
		layout.setRowSpec(editAreas.size()+2, indentions.bottomInset());
		layout.setColumnSpec(1, indentions.leftInset());
		layout.setColumnSpec(3, indentions.rightInset());

		setEditorBorder(
			indentions.topBorder(),
			indentions.leftBorder(),
			indentions.bottomBorder(),
			indentions.rightBorder());
	}

	private void setEditorBorder(int top, int left, int bottom, int right) {
		// TODO JL: Das ist hier noch nicht sauber. Die Insets von Text- und Imagebereichen sind nicht gleich, und
		// der oberste und unterste Editbereich haben unterschiedliche Top- und Bottom-Insets
		this.editorPaneBorder = new EmptyBorder(top, left, bottom, right);
		editAreas.forEach(ea -> ea.setBorder(editorPaneBorder));
	}

	public Rectangle getStepNumberBounds() { return schrittNummer.getBounds(); }

	public void wrapSchrittnummerAsDeleted() { schrittNummer.wrapAsDeleted(); }
	public void wrapSchrittnummerAsZiel(SchrittID quellschrittId) { schrittNummer.wrapAsZiel(quellschrittId); }
	public void wrapSchrittnummerAsQuelle(SchrittID zielschrittID) { schrittNummer.wrapAsQuelle(zielschrittID); }

	/** Required for iterations that may modify the list of edit areas. Working directly on the
	 * list whould cause concurrent operation exceptions in these cases. The usage of this method
	 * is sometimes unintuitive. E.g. removing change marks actually may cause an {@link ImageEditArea}
	 * to be removed from the list. */
	private List<EditArea> modifyableEditAreas() { return new ArrayList<>(editAreas); }

	public void aenderungsmarkierungenUebernehmen() {
		modifyableEditAreas().forEach(ea -> ea.aenderungsmarkierungenUebernehmen());
	}

	public void aenderungsmarkierungenVerwerfen() {
		modifyableEditAreas().forEach(ea -> ea.aenderungsmarkierungenVerwerfen());
	}

	public boolean enthaelt(InteractiveStepFragment fragment) {
		return editAreas.stream().anyMatch(ea -> ea == fragment)
			|| schrittNummer == fragment;
	}

	public InteractiveStepFragment asInteractiveFragment() { return editAreas.get(0); }

	public void addImage(File imageFile, TextEditArea initiatingTextArea, Aenderungsart aenderungsart) {
		EditorI editor = Specman.instance();
		try (UndoRecording ur = editor.composeUndo()) {
			int initiatingTextAreaIndex = editAreas.indexOf(initiatingTextArea);
			int initiatingCaretPosition = initiatingTextArea.getCaretPosition();
			ImageEditArea imageEditArea = new ImageEditArea(imageFile, aenderungsart);
			addEditArea(imageEditArea, initiatingTextAreaIndex+1);
			TextEditArea cutOffTextArea = initiatingTextArea.split(initiatingCaretPosition);
			if (cutOffTextArea != null) {
				addEditArea(cutOffTextArea, initiatingTextAreaIndex+2);
			}
			editor.addEdit(new UndoableImageAdded(this, initiatingTextArea, imageEditArea, cutOffTextArea));
		}
		updateBounds();
	}

	public void addImageByUndoRedo(TextEditArea initiatingTextArea, ImageEditArea imageEditArea, TextEditArea cutOffTextArea) {
		try (UndoRecording ur = Specman.instance().pauseUndo()) {
			int initiatingTextAreaIndex = editAreas.indexOf(initiatingTextArea);
			addEditArea(imageEditArea, initiatingTextAreaIndex+1);
			if (cutOffTextArea != null) {
				addEditArea(cutOffTextArea, initiatingTextAreaIndex+2);
			}
		}
		updateBounds();
	}

	private void addEditArea(EditArea editArea, int index) {
		editAreasFocusListeners.forEach(fl -> editArea.asComponent().addFocusListener(fl));
		editAreasComponentListeners.forEach(cl -> editArea.asComponent().addComponentListener(cl));
		editAreas.add(index, editArea);
		layout.setRowSpec(index+2, EDITAREA_LAYOUT_ROWSPEC);
		add(editArea.asComponent(), CC.xy(2, index+2));
		for (int followerIndex = index+1; followerIndex < editAreas.size(); followerIndex++) {
			EditArea followerArea = editAreas.get(followerIndex);
			layout.setRowSpec(followerIndex+2, EDITAREA_LAYOUT_ROWSPEC);
			layout.setConstraints(followerArea.asComponent(), CC.xy(2, followerIndex + 2));
		}
		layout.appendRow(RowSpec.decode("0px"));
	}

	public void removeImageByUndoRedo(ImageEditArea imageEditArea, TextEditArea cutOffTextArea) {
		try (UndoRecording ur = Specman.instance().pauseUndo()) {
			removeEditArea(imageEditArea);
			if (cutOffTextArea != null) {
				removeEditArea(cutOffTextArea);
			}
		}
		updateBounds();
	}

	public void removeImage(ImageEditArea image) {
		EditorI editor = Specman.instance();
		try (UndoRecording ur = editor.composeUndo()) {
			TextEditArea leadingTextArea = null;
			TextEditArea trailingTextArea = null;
			int imageIndex = removeEditArea(image);
			if (imageIndex > 0) {
				leadingTextArea = editAreas.get(imageIndex-1).asTextArea();
				if (editAreas.size() > imageIndex) {
					trailingTextArea = editAreas.get(imageIndex).asTextArea();
					if (leadingTextArea != null && trailingTextArea != null) {
						removeEditArea(trailingTextArea);
						leadingTextArea.appendText(trailingTextArea.getText());
						leadingTextArea.requestFocus();
					}
				}
			}
			editor.addEdit(new UndoableImageRemoved(this, leadingTextArea, image, trailingTextArea));
		}
		updateBounds();
	}

	private int removeEditArea(EditArea area) {
		int index = editAreas.indexOf(area);
		editAreas.remove(area);
		remove(area.asComponent());
		for (int followerIndex = index; followerIndex < editAreas.size(); followerIndex++) {
			EditArea followerArea = editAreas.get(followerIndex);
			layout.setConstraints(followerArea.asComponent(), CC.xy(2, followerIndex + 2));
		}
		layout.removeRow(editAreas.size()+3);
		return index;
	}

	public void setEditorContent(EditorI editor, EditorContentModel_V001 intro) {
		initLayoutAndEditAreas(editor, intro);
	}

	public void addEditComponentListener(ComponentListener componentListener) {
		editAreasComponentListeners.add(componentListener);
		editAreas.forEach(ea -> addComponentListener(componentListener));
	}

	public boolean enthaeltAenderungsmarkierungen() {
		return editAreas.stream().anyMatch(ea -> ea.enthaeltAenderungsmarkierungen());
	}

	public specman.pdf.Shape getShape() {
		TextEditArea edit1 = (TextEditArea) editAreas.get(0);
		Shape shape = new Shape(this).withText(new FormatedShapeText(edit1));
		if (schrittNummer != null && schrittNummerSichtbar) {
			shape.add(schrittNummer.getShape());
		}
		return shape;
	}
}
