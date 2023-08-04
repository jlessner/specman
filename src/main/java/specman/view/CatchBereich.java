package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import specman.Specman;
import specman.textfield.InteractiveStepFragment;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

class CatchBereich extends JPanel implements KlappbarerBereichI, ComponentListener {
	public static final String ZEILENLAYOUT_TRENNKOPF_SICHTBAR = "fill:10px";
	public static final String ZEILENLAYOUT_TRENNKOPF_VERBORGEN = AbstractSchrittView.ZEILENLAYOUT_INHALT_VERBORGEN;

	JPanel trennkopf;
	JPanel umgehung;
	JPanel catchBloeckeContainer;
	FormLayout layout;
	FormLayout bereichLayout;
	KlappButton klappen;
	final List<CatchSchrittView> catchBloecke = new ArrayList<CatchSchrittView>();
	final List<JPanel> grundlinienAnschluesse = new ArrayList<JPanel>();
	int umgehungBreite;

	public CatchBereich() {
		setBackground(Specman.schrittHintergrund());
		umgehungBreite = AbstractSchrittView.SPALTENLAYOUT_UMGEHUNG_GROESSE * 2;
		bereichLayout = new FormLayout(
				"10dlu:grow, " + AbstractSchrittView.umgehungLayout(umgehungBreite),
				"0px, " + ZEILENLAYOUT_TRENNKOPF_VERBORGEN + ", fill:pref");
		setLayout(bereichLayout);

		trennkopf = new JPanel();
		trennkopf.setBackground(Specman.schrittHintergrund());
		trennkopf.setLayout(null);
		trennkopf.addComponentListener(this);
		add(trennkopf, CC.xywh(1, 2, 2, 1));

		umgehung = new JPanel();
		umgehung.setBackground(Specman.schrittHintergrund());
		umgehung.setLayout(null);
		umgehung.setVisible(false);
		add(umgehung, CC.xy(2, 3));

		catchBloeckeContainer = new JPanel();
		catchBloeckeContainer.setBackground(Color.black);
		add(catchBloeckeContainer, CC.xy(1, 3));
		layoutInitialisieren();

		klappen = new KlappButton(this, trennkopf, bereichLayout, 3);
	}

	private void layoutInitialisieren() {
		layout = new FormLayout("10px:grow, " + AbstractSchrittView.FORMLAYOUT_GAP);
		catchBloeckeContainer.setLayout(layout);
	}

	public void catchAnhaengen(CatchSchrittView schritt, FocusListener focusListener) {
		layout.appendRow(RowSpec.decode(AbstractSchrittView.FORMLAYOUT_GAP));
		layout.appendRow(RowSpec.decode("pref:grow"));
		catchBloeckeContainer.add(schritt.getComponent(), CC.xy(1, (catchBloecke.size()+1) * 2));

		if (catchBloecke.size() > 0) {
			alleGrundlinienAnschluesseEntfernen();
			layout.appendColumn(ColumnSpec.decode(AbstractSchrittView.umgehungLayout()));
			layout.appendColumn(ColumnSpec.decode(AbstractSchrittView.FORMLAYOUT_GAP));

			for (int i = 0; i < catchBloecke.size(); i++) {
				alsVorgaengerImLayoutEinordnen(catchBloecke.get(i), i, catchBloecke.size() - i);
			}
		}
		else {
			schritt.hatNachfolger(false);
		}
		catchBloecke.add(schritt);
		trennkopfSichtbarkeitAktualisieren();
	}

	/**
	 * Einen Schritt rausnehmen ist ganz sch�n kompliziert, zumal es im schlimmsten Fall auch
	 * auf die Nachbarschritte abstrahlt. Deswegen machen wir es brute-force: Wir schmei�en
	 * die Anordnung weg und bauen sie - reduziert um den zu entfernenden Schritt - wieder
	 * ganz von vorn auf.
	 * @Return Den Index des entferntes Schritts in der Sequenz. Dient der Wiedereingliederung beim Redo
	 */
	public int catchEntfernen(CatchSchrittView zuEntfernenderSchritt) {
		FocusListener focusListener = zuEntfernenderSchritt.getText().getFocusListeners()[0];
		alleSchritteEntfernen();
		alleGrundlinienAnschluesseEntfernen();
		layoutInitialisieren();
		int schrittIndex = catchBloecke.indexOf(zuEntfernenderSchritt);
		catchBloecke.remove(schrittIndex);
		List<CatchSchrittView> restlicheSchritte = new ArrayList<CatchSchrittView>(catchBloecke);
		catchBloecke.clear();
		for (CatchSchrittView schritt: restlicheSchritte) {
			catchAnhaengen(schritt, focusListener);
		}
		trennkopfSichtbarkeitAktualisieren();
		return schrittIndex;
	}

	private void trennkopfSichtbarkeitAktualisieren() {
		if (catchBloecke.size() > 0) {
			bereichLayout.setRowSpec(1, RowSpec.decode(AbstractSchrittView.FORMLAYOUT_GAP));
			//bereichLayout.setRowSpec(2, RowSpec.decode(ZEILENLAYOUT_TRENNKOPF_SICHTBAR));
			bereichLayout.setRowSpec(2, RowSpec.decode(AbstractSchrittView.umgehungLayout()));
			umgehung.setVisible(true);
		}
		else {
			bereichLayout.setRowSpec(1, RowSpec.decode(ZEILENLAYOUT_TRENNKOPF_VERBORGEN));
			bereichLayout.setRowSpec(2, RowSpec.decode(ZEILENLAYOUT_TRENNKOPF_VERBORGEN));
			umgehung.setVisible(false);
		}


//		String zeilenlayoutTrennkopf = (catchBloecke.size() > 0) ? ZEILENLAYOUT_TRENNKOPF_SICHTBAR : ZEILENLAYOUT_TRENNKOPF_VERBORGEN;
//		String zeilenlayoutOberlinie = (catchBloecke.size() > 0) ? SchrittView.FORMLAYOUT_GAP : ZEILENLAYOUT_TRENNKOPF_VERBORGEN;
//		bereichLayout.setRowSpec(1, RowSpec.decode(zeilenlayoutOberlinie));
//		bereichLayout.setRowSpec(2, RowSpec.decode(zeilenlayoutTrennkopf));
	}

	private void alleSchritteEntfernen() {
		for (CatchSchrittView schritt: catchBloecke) {
			catchBloeckeContainer.remove(schritt.getComponent());
		}
	}

	private void alleGrundlinienAnschluesseEntfernen() {
		for (JPanel grundlinienAnschluss: grundlinienAnschluesse) {
			catchBloeckeContainer.remove(grundlinienAnschluss);
		}
		grundlinienAnschluesse.clear();
	}

	private void alsVorgaengerImLayoutEinordnen(CatchSchrittView catchSchrittView, int index, int anzahlNachfolger) {
		int spaltenbreite = anzahlNachfolger * 2 + 1;
		int zeilenposition = (index+1) * 2;
		//layout.setConstraints(catchSchrittView.getComponent(), CC.xywh(1, 2, 3, 1));
		layout.setConstraints(catchSchrittView.getComponent(), CC.xywh(1, zeilenposition, spaltenbreite, 1));

		int spaltenpositionGrundlinienAnschluss = spaltenbreite;
		int zeilenpositionGrundlinienAnschluss = zeilenposition+1;
		int zeilenhoeheGrundlinienAnschluss = anzahlNachfolger * 2;
		JPanel anschlussAnGrundlinie = new JPanel();
		anschlussAnGrundlinie.setBackground(Specman.schrittHintergrund());
		catchBloeckeContainer.add(anschlussAnGrundlinie, CC.xywh(spaltenpositionGrundlinienAnschluss, zeilenpositionGrundlinienAnschluss, 1, zeilenhoeheGrundlinienAnschluss));
		catchSchrittView.hatNachfolger(true);
		grundlinienAnschluesse.add(anschlussAnGrundlinie);
	}

	public AbstractSchrittView findeSchritt(InteractiveStepFragment fragment) {
		for (CatchSchrittView catchSchritt: catchBloecke) {
			if (catchSchritt.enthaelt(fragment)) {
				return catchSchritt;
			}
			AbstractSchrittView schritt = catchSchritt.findeSchritt(fragment);
			if (schritt != null) {
				return schritt;
			}
		}
		return null;
	}

	public AbstractSchrittView findeEigenenSchritt(JTextComponent zuletztFokussierterText) {
		for (CatchSchrittView catchSchritt: catchBloecke) {
			if (catchSchritt.getText() == zuletztFokussierterText)
				return catchSchritt;
		}
		return null;
	}

	public BreakSchrittView findeBreakSchritt(String catchText) {
		for (CatchSchrittView catchSchritt: catchBloecke) {
			BreakSchrittView schritt = catchSchritt.findeBreakSchritt(catchText);
			if (schritt != null)
				return schritt;
		}
		return null;
	}

	public void entfernen(SchrittSequenzView container) {
		for (CatchSchrittView catchSchritt: catchBloecke) {
			catchSchritt.entfernen(container);
		}
	}

	@Override
	public boolean enthaeltAenderungsmarkierungen() {
		for (CatchSchrittView catchSchritt: catchBloecke) {
			if (catchSchritt.enthaeltAenderungsmarkierungenInklName())
				return true;
		}
		return false;
	}

	@Override
	public void geklappt(boolean auf) {
		catchBloeckeContainer.setVisible(auf);
		umgehung.setVisible(auf);
	}

	public void zusammenklappenFuerReview() {
		if (!enthaeltAenderungsmarkierungen()) {
			klappen.init(true);
		}
		for (CatchSchrittView catchSchritt: catchBloecke) {
			catchSchritt.zusammenklappenFuerReview();
		}
	}

	public void skalieren(int prozentNeu, int prozentAktuell) {
		for (CatchSchrittView catchSchritt: catchBloecke) {
			catchSchritt.skalieren(prozentNeu, prozentAktuell);
		}
		trennkopfSichtbarkeitAktualisieren();
		int neueUmgehungBreite = AbstractSchrittView.groesseUmrechnen(umgehungBreite, prozentNeu, prozentAktuell);
		umgehungBreiteSetzen(neueUmgehungBreite);
		klappen.scale(prozentNeu);
	}

	public void umgehungBreiteSetzen(int angepassteUmgehungBreite) {
		umgehungBreite = angepassteUmgehungBreite;
		bereichLayout.setColumnSpec(2, ColumnSpec.decode(AbstractSchrittView.umgehungLayout(umgehungBreite)));
	}

	@Override
	public void componentResized(ComponentEvent e) {
		klappen.updateLocation(getWidth());
	}

	@Override public void componentMoved(ComponentEvent e) {
	}

	@Override public void componentShown(ComponentEvent e) {
	}

	@Override public void componentHidden(ComponentEvent e) {
	}
}