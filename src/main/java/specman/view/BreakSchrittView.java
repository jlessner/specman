package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;

import specman.Aenderungsart;
import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.BreakSchrittModel_V001;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.EditorContentModel_V001;
import specman.pdf.LineShape;
import specman.pdf.Shape;
import specman.undo.props.UDBL;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import java.util.List;

public class BreakSchrittView extends AbstractSchrittView {
	
	final JPanel panel;
	final FormLayout layout;
	CatchSchrittSequenzView catchSequence;

	public BreakSchrittView(EditorI editor, SchrittSequenzView parent, EditorContentModel_V001 content, SchrittID id, Aenderungsart aenderungsart) {
		super(editor, parent, content, id, aenderungsart);
		panel = new JPanel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				dreieckZeichnen((Graphics2D)g);
			}
		};
		panel.setBackground(Specman.schrittHintergrund());
		layout = new FormLayout(
				umgehungLayout() + ", 10dlu:grow",
				ZEILENLAYOUT_INHALT_SICHTBAR);
		panel.setLayout(layout);
		
		panel.add(editContainer, CC.xy(2, 1));

	}

	public BreakSchrittView(EditorI editor, SchrittSequenzView parent, BreakSchrittModel_V001 model) {
		this(editor, parent, model.inhalt, model.id, model.aenderungsart);
		setBackgroundUDBL(new Color(model.farbe));
	}

	private void dreieckZeichnen(Graphics2D g) {
		g.setStroke(new BasicStroke(1));
		g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
		for (LineShape line: buildTriangle()) {
			g.drawLine(line.start().x, line.start().y, line.end().x, line.end().y);
		}
	}

	private java.util.List<LineShape> buildTriangle() {
		int hoehe = panel.getHeight();
		int dreieckSpitzeY = hoehe / 2;
		int dreieckBasisX = editContainer.getX() - LINIENBREITE;
		return Arrays.asList(
			new LineShape(dreieckBasisX,  0,  0,  dreieckSpitzeY),
			new LineShape(0,  dreieckSpitzeY, dreieckBasisX, hoehe));
	}

	@Override
	public void setBackgroundUDBL(Color bg) {
		super.setBackgroundUDBL(bg);
		UDBL.setBackgroundUDBL(panel, bg);
	}

	@Override
	public JComponent getDecoratedComponent(){
		return decorated(panel);
	}

	@Override
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		BreakSchrittModel_V001 model = new BreakSchrittModel_V001(
			id,
			getEditorContent(formatierterText),
			getBackground().getRGB(),
			aenderungsart,
			getQuellschrittID(),
			getDecorated()
		);
		return model;
	}

	@Override
	public boolean istBreakSchrittFuer(String catchText) {
		return ersteZeileExtraieren().equals(catchText);
	}

	public void catchAnkoppeln(CatchSchrittSequenzView catchSequence) {
		this.catchSequence = catchSequence;
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (catchSequence != null) {
			catchSequence.updateHeading(editContainer.editorContent2Model(true));
		}
	}

	@Override
	public void setId(SchrittID id) {
		super.setId(id);
		if (catchSequence != null) {
			catchSequence.setId(id);
		}
	}

	@Override
	public void entfernen(SchrittSequenzView container) {
		super.entfernen(container);
		if (catchSequence != null) {
			catchSequence.removeOrMarkAsDeletedUDBL();
		}
	}

	@Override
	public void alsGeloeschtMarkierenUDBL(EditorI editor) {
		super.alsGeloeschtMarkierenUDBL(editor);
		if (catchSequence != null) {
			catchSequence.removeOrMarkAsDeletedUDBL();
		}
	}

	public void skalieren(int prozentNeu, int prozentAktuell) {
		super.skalieren(prozentNeu, prozentAktuell);
		layout.setColumnSpec(1, ColumnSpec.decode(umgehungLayout()));
	}

	@Override
	public JPanel getPanel() { return panel; }

	@Override
	public Shape getShape() {
		return super.getShape()
			.add(buildTriangle());
	}

	@Override
	public List<BreakSchrittView> queryUnlinkedBreakSteps() {
		return (catchSequence == null) ? Arrays.asList(this) : Arrays.asList();
	}

	public void updateContent(EditorContentModel_V001 content) {
		editContainer.setEditorContent(content);
	}
}
