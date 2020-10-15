package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.BreakSchrittModel;
import specman.model.AbstractSchrittModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;

public class BreakSchrittView extends AbstractSchrittView {
	
	final JPanel panel;
	final FormLayout layout;
	CatchSchrittView zielSchritt;

	public BreakSchrittView(EditorI editor, String initialerText, SchrittID id) {
		super(editor, initialerText, id);
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
		
		panel.add(text, CC.xy(2, 1));
	}

	private void dreieckZeichnen(Graphics2D g) {
		int hoehe = panel.getHeight();
		int dreieckSpitzeY = hoehe / 2;
		int dreieckBasisX = text.getX() - LINIENBREITE;
		g.setStroke(new BasicStroke(1));
		g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawLine(dreieckBasisX,  0,  0,  dreieckSpitzeY);
		g.drawLine(0,  dreieckSpitzeY, dreieckBasisX, hoehe);
	}
	
	public BreakSchrittView(EditorI editor, BreakSchrittModel model) {
		this(editor, model.inhalt.text, model.id);
		setBackground(new Color(model.farbe));
	}

	public BreakSchrittView(EditorI editor, String initialerText) {
		this(editor, initialerText, (SchrittID) null);
	}
	
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		panel.setBackground(bg);
	}

	@Override
	public Component getComponent() {
		return panel;
	}

	@Override
	public AbstractSchrittModel generiereModel(boolean formatierterText) {
		BreakSchrittModel model = new BreakSchrittModel();
		model.inhalt = getTextMitAenderungsmarkierungen(formatierterText);
		model.id = id;
		model.farbe = getBackground().getRGB();
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
		super.focusLost(e);
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

	
}
