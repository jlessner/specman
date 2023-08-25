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

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.util.Arrays;

public class BreakSchrittView extends AbstractSchrittView {
	
	final JPanel panel;
	final FormLayout layout;
	CatchSchrittView zielSchritt;

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
		setBackground(new Color(model.farbe));
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
	public void setBackground(Color bg) {
		super.setBackground(bg);
		panel.setBackground(bg);
	}

	@Override
	public JComponent getComponent(){
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

	public void zielAnkoppeln(CatchSchrittView zielSchritt) {
		this.zielSchritt = zielSchritt;
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (zielSchritt != null) {
			zielSchritt.catchTextAktualisieren(ersteZeileExtraieren());
		}
	}

	@Override
	public void setId(SchrittID id) {
		super.setId(id);
		if (zielSchritt != null) {
			zielSchritt.setId(id);
		}
	}

	@Override
	public void entfernen(SchrittSequenzView container) {
		super.entfernen(container);
		if (zielSchritt != null) {
			zielSchritt.breakAbkoppeln(this);
		}
	}
	
	public void skalieren(int prozentNeu, int prozentAktuell) {
		super.skalieren(prozentNeu, prozentAktuell);
		layout.setColumnSpec(1, ColumnSpec.decode(umgehungLayout()));
	}

	public JPanel getPanel() {
		return panel;
	}

	@Override
	public Shape getShape() {
		return new Shape(getComponent(), this)
			.add(editContainer.getShape())
			.add(buildTriangle());
	}

}
