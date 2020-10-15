package specman;

import org.apache.commons.lang.StringUtils;
import specman.model.IfElseSchrittModel;
import specman.model.IfSchrittModel;
import specman.model.AbstractSchrittModel;
import specman.model.SchrittSequenzModel;
import specman.model.SubsequenzSchrittModel;
import specman.model.WhileSchrittModel;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GraphvizExporter {
	PrintStream out;

	public GraphvizExporter(String exportDateiname) throws IOException {
		out = new PrintStream(exportDateiname);
	}

	public void export(SchrittSequenzModel model) throws IOException {
		out.println(
				"digraph G {\n" +
				"  graph [fontname = \"Helvetica-Oblique\", fontsize = 10];\n" +
				"  node [fontsize=10, height=0.1, shape=box, style=\"rounded,filled\", fillcolor=\"gray94:grey82\", gradientangle=94];\n" +
				"  edge [fontsize=10];\n\n" +
				"  begin [shape = circle, label=\"\", fillcolor=black, height=0.2];\n" +
				"  end [shape = doublecircle, label=\"\", fillcolor=black]");

		List<Haken> letzteSchritte = sequenzExportieren(model, new Haken("begin"));
		verbinden(letzteSchritte, "end");
			
		out.println("}");
		out.close();
	}

	private void verbinden(List<Haken> vons, Haken nach) {
		verbinden(vons, nach.schritt);
	}

	private void verbinden(List<Haken> vons, String nach) {
		for (Haken von: vons) {
			out.println(von + " -> " + nach + von.verbindungsProperties());
		}
	}

	static String propertyString(String... properties) {
		String s = "[";
		for (String property: properties) {
			if (property != null) {
				s += property + ",";
			}
		}
		return (s.length() == 1) ? "" : s.substring(0, s.length()-1) + "]";
	}

	private List<Haken> sequenzExportieren(SchrittSequenzModel sequenz, Haken obererAnschluss) throws IOException {
		return sequenzExportieren(sequenz, hakenliste(obererAnschluss));
	}
	
	private List<Haken> sequenzExportieren(SchrittSequenzModel sequenz, List<Haken> obereAnschluesse) throws IOException {
		for (AbstractSchrittModel schritt: sequenz.schritte) {
			String schrittExportName = "schritt_" + schritt.id.toString().replace(".", "_");
			if (schritt instanceof WhileSchrittModel) {
				WhileSchrittModel whileSchritt = (WhileSchrittModel)schritt;
				Haken whileBeginnAnschluss = new Haken(schrittExportName + "_begin");
				Haken whileEndAnschluss = new Haken(schrittExportName + "_end");
				out.println(
						"  subgraph cluster_" + schrittExportName + " {\n" +
						"    fontsize=10; shape=box; style=\"rounded,filled\"; fillcolor=\"gray94:grey82\"; gradientangle=94;\n" +
						"    label=\"" + textFuerAktivitaetsboxAufbereiten(whileSchritt) + "\"\n" +
						"    " + whileBeginnAnschluss + " [shape = circle, label=\"\", fillcolor=black, height=0.1]\n" +
						"    " + whileEndAnschluss + " [shape = doublecircle, label=\"\", fillcolor=black, height=0.1]");
				List<Haken> letzteUnterschritte = sequenzExportieren(whileSchritt.wiederholSequenz, whileBeginnAnschluss);
				verbinden(letzteUnterschritte, whileEndAnschluss);
				out.println("}");
				verbinden(obereAnschluesse, whileBeginnAnschluss);
				obereAnschluesse = hakenliste(whileEndAnschluss);
			}
			if (schritt instanceof SubsequenzSchrittModel) {
				// Machen wir grafisch erst mal genau wie einen While-Schritt
				SubsequenzSchrittModel subsequenzSchritt = (SubsequenzSchrittModel)schritt;
				Haken subBeginnAnschluss = new Haken(schrittExportName + "_begin");
				Haken subEndAnschluss = new Haken(schrittExportName + "_end");
				out.println(
						"  subgraph cluster_" + schrittExportName + " {\n" +
						"    fontsize=10; shape=box; style=\"rounded,filled\"; fillcolor=\"gray94:grey82\"; gradientangle=94;\n" +
						"    label=\"" + textFuerAktivitaetsboxAufbereiten(subsequenzSchritt) + "\"\n" +
						"    " + subBeginnAnschluss + " [shape = circle, label=\"\", fillcolor=black, height=0.1]\n" +
						"    " + subEndAnschluss + " [shape = doublecircle, label=\"\", fillcolor=black, height=0.1]");
				List<Haken> letzteUnterschritte = sequenzExportieren(subsequenzSchritt.subsequenz, subBeginnAnschluss);
				verbinden(letzteUnterschritte, subEndAnschluss);
				out.println("}");
				verbinden(obereAnschluesse, subBeginnAnschluss);
				obereAnschluesse = hakenliste(subEndAnschluss);
			}
			else if (schritt instanceof IfElseSchrittModel) {
				IfElseSchrittModel ifSchritt = (IfElseSchrittModel)schritt;
				out.println(
						schrittExportName + "[label=\"" + textFuerBedingungAufbereiten(schritt) + "\"\n" +
						"shape = diamond; style=filled; fillcolor=gray94; fixedsize=shape; height=0.3; width=0.3 ]");
				verbinden(obereAnschluesse, schrittExportName);
				Haken ifHaken = new Haken(schrittExportName, ifSchritt.ifSequenz.ueberschrift.text, ifSchritt.ifBreitenanteil > 0.6 ? 5 : 1);
				Haken elseHaken = new Haken(schrittExportName, ifSchritt.elseSequenz.ueberschrift.text, ifSchritt.ifBreitenanteil < 0.4 ? 5 : 1);
				List<Haken> letzteUnterschritteIfSequenz = sequenzExportieren(ifSchritt.ifSequenz, ifHaken);
				gewichtUebernehmen(ifHaken, letzteUnterschritteIfSequenz);
				List<Haken> letzteUnterschritteElseSequenz = sequenzExportieren(ifSchritt.elseSequenz, elseHaken);
				gewichtUebernehmen(elseHaken, letzteUnterschritteElseSequenz);
				obereAnschluesse = hakenliste(letzteUnterschritteIfSequenz, letzteUnterschritteElseSequenz);
			}
			else if (schritt instanceof IfSchrittModel) {
				IfSchrittModel ifSchritt = (IfSchrittModel)schritt;
				out.println(
						schrittExportName + "[label=\"" + textFuerBedingungAufbereiten(schritt) + "\"\n" +
						"shape = diamond; style=filled; fillcolor=gray94; fixedsize=shape; height=0.3; width=0.3 ]");
				verbinden(obereAnschluesse, schrittExportName);
				Haken ifHaken = new Haken(schrittExportName, ifSchritt.ifSequenz.ueberschrift.text, 5);
				Haken umgehungHaken = new Haken(schrittExportName, "", 1);
				List<Haken> umgehungHakenAlsListe = new ArrayList<Haken>();
				umgehungHakenAlsListe.add(umgehungHaken);
				List<Haken> letzteUnterschritteIfSequenz = sequenzExportieren(ifSchritt.ifSequenz, ifHaken);
				gewichtUebernehmen(ifHaken, letzteUnterschritteIfSequenz);
				obereAnschluesse = hakenliste(letzteUnterschritteIfSequenz, umgehungHakenAlsListe);
			}
			else {
				String aufbereiteterText = textFuerAktivitaetsboxAufbereiten(schritt);
				if (!StringUtils.isEmpty(aufbereiteterText.trim())) { // Eigentlich unsch�n. Ist nur, weil If noch als If-Else mit leerem Schritt modelliert wird.
					out.println(schrittExportName + "[label=\"" + textFuerAktivitaetsboxAufbereiten(schritt) + "\"];");
					verbinden(obereAnschluesse, schrittExportName);
					obereAnschluesse = hakenliste(new Haken(schrittExportName));
				}
			}
		}
		return obereAnschluesse;
	}

	private void gewichtUebernehmen(Haken von, List<Haken> nachs) {
		for (Haken nach: nachs)
			nach.verbindungsGewicht = von.verbindungsGewicht;
	}

	private List<Haken> hakenliste(Haken... aufhaenger) {
		return Arrays.asList(aufhaenger);
	}

	private List<Haken> hakenliste(List<Haken>... hakenListen) {
		List<Haken> liste = new ArrayList<Haken>();
		for (List<Haken> hakenListe: hakenListen) {
			liste.addAll(hakenListe);
		}
		return liste;
	}

	private String textFuerAktivitaetsboxAufbereiten(AbstractSchrittModel schritt ) {
		return textFuerAktivitaetsboxAufbereiten(schritt.inhalt.text, "");
	}

	private String textFuerBedingungAufbereiten(AbstractSchrittModel schritt) {
		return textFuerAktivitaetsboxAufbereiten(schritt.inhalt.text, "\t\t\t\t\t\t");
	}

	private static String textFuerAktivitaetsboxAufbereiten(String rohtext, String zeilenTrailer) {
		String[] tokens = rohtext.split(" ");
		int t = 0;
		String ergebnis = "";
		for (int z = 0; z < 3; z++) {
			String zeile = "";
			while(t < tokens.length) {
				if (zeile.length()  + tokens[t].length() > 40)
					break;
				zeile = zeile + tokens[t++] + " ";
			}
			if (zeile.endsWith(" "))
				zeile = zeile.substring(0, zeile.length()-1);
			ergebnis += zeile + zeilenTrailer + "\n";
		}
		if (ergebnis.endsWith("\n"))
			ergebnis = ergebnis.substring(0, ergebnis.length()-2);
		if (t < tokens.length)
			ergebnis += "...";
		return ergebnis;
	}
	
	public static void main(String[] args) {
		System.out.println("'" + textFuerAktivitaetsboxAufbereiten("In Rom ist es im Sommer entschieden zu hei�.", "") + "'");
		System.out.println("'" + textFuerAktivitaetsboxAufbereiten("Solange ich noch nicht richtig wach bin und noch wenigstens 5 Minuten Zeit bleiben", "") + "'");
	}
	
	/**
	 * Ein Haken ist ein Knoten im Graphen, der einen Schritt repr�sentiert, erg�nzt um Label-
	 * und Gewichtsinformation f�r den n�chsten sich anschlie�enenden Knoten im Graphen.
	 * Aus Haken und Folge(schritt)knoten kann man dann die Edges herstellen
	 */
	static class Haken {
		final String schritt;
		final String verbindungsLabel;
		Integer verbindungsGewicht;
		
		public Haken(String schritt, String verbindungsLabel, Integer verbindungsGewicht) {
			this.schritt = schritt;
			this.verbindungsLabel = verbindungsLabel;
			this.verbindungsGewicht = verbindungsGewicht;
		}

		public String verbindungsProperties() {
			return propertyString(
					(verbindungsLabel == null) ? null : "label=\"" + verbindungsLabel + "\"",
					(verbindungsGewicht == null) ? null : "weight=" + verbindungsGewicht	
					);
		}

		public Haken(String schritt, String verbindungsLabel) {
			this(schritt, verbindungsLabel, 3);
		}

		public Haken(String schritt) {
			this(schritt, null, 3);
		}

		public String toString() { return schritt; }
	}
}
