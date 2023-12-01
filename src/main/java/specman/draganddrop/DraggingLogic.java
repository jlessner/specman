package specman.draganddrop;

import specman.Aenderungsart;
import specman.EditException;
import specman.Specman;
import specman.editarea.SchrittNummerLabel;
import specman.editarea.EditContainer;
import specman.undo.UndoableSchrittVerschoben;
import specman.undo.UndoableSchrittVerschobenMarkiert;
import specman.undo.UndoableZweigHinzugefuegt;
import specman.undo.manager.UndoRecording;
import specman.view.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.List;

import static specman.view.RelativeStepPosition.Before;
import static specman.view.RelativeStepPosition.After;

public class DraggingLogic implements Serializable {
    private final Specman specman;
    private Boolean lastStep = false;

    public DraggingLogic(Specman specman) {
        this.specman = specman;
    }

    // GlassPane and add Step to sequence
    private void checkZweigHeading(ZweigSchrittSequenzView zweig, Point pos, int glassPaneHeight, InsertDecision insertDecision, MouseEvent mE) throws EditException {
        Point p = SwingUtilities.convertPoint(zweig.getUeberschrift(), 0, 0, specman);
        Rectangle r = createRectangle(p, zweig.getUeberschrift());
        if (r.contains(pos)) {
            createGlassPane(r.width, r.x,r.y + r.height - glassPaneHeight, glassPaneHeight,true);
            //mouserelease add Step at first Position in sequenz
            if (insertDecision == InsertDecision.Insert) {
                AbstractSchrittView step = zweig.schritte.get(0);
                addNeuerSchritt(Before, step, mE);
            }
        }
    }

    // GlassPane and add Step to sequence
    private void checkZweigHeading(ZweigSchrittSequenzView zweig, Point pos, int glassPaneHeight, int offsetRaute, InsertDecision insertDecision, MouseEvent mE) throws EditException {
        Point p = SwingUtilities.convertPoint(zweig.getUeberschrift(), 0, 0, specman);
        if(offsetRaute<0) {
        	p.x= p.x+offsetRaute;
        }
        Rectangle r = createRectangle(p, zweig.getUeberschrift());
        r.width= r.width+Math.abs(offsetRaute);
        if (r.contains(pos)) {
        	createGlassPane(r.width, r.x,r.y + r.height - glassPaneHeight, glassPaneHeight,true);
            //mouserelease add Step at first Position in sequenz
            if (insertDecision == InsertDecision.Insert) {
                AbstractSchrittView step = zweig.schritte.get(0);
                addNeuerSchritt(Before, step, mE);
            }
        }
    }
    
    // GlassPane over Cases
    private void checkCaseHeading(ZweigSchrittSequenzView zweig, Point pos, int glassPaneHeight,int offsetRaute, InsertDecision insertDecision) throws EditException {
        Point p = SwingUtilities.convertPoint(zweig.getUeberschrift(), 0, 0, specman);
        Rectangle r = createRectangle(p, zweig.getUeberschrift());
        if (r.contains(pos)) {
            int casehight = zweig.getContainer().getHeight() + zweig.getUeberschrift().getHeight() + 2;
            createGlassPane(glassPaneHeight, r.x + r.width - glassPaneHeight+offsetRaute, r.y+offsetRaute, casehight-offsetRaute, true);
            //mouserelease add Case right from choosen Case
            if (insertDecision == InsertDecision.Insert) addCase(zweig);
        }
    }

    //add Case withing Drag and Drop
    private void addCase(ZweigSchrittSequenzView zweigSchrittSequenz) {
        EditContainer ueberschrift = zweigSchrittSequenz.getUeberschrift();
        AbstractSchrittView step = specman.getHauptSequenz().findeSchritt(ueberschrift.asInteractiveFragment());
        CaseSchrittView caseSchritt = (CaseSchrittView) step;
        ZweigSchrittSequenzView ausgewaehlterZweig = caseSchritt.headingToBranch(ueberschrift.asInteractiveFragment());
        ZweigSchrittSequenzView neuerZweig = caseSchritt.neuenZweigHinzufuegen(specman, ausgewaehlterZweig);
        specman.addEdit(new UndoableZweigHinzugefuegt(specman, neuerZweig, caseSchritt));
        step.skalieren(specman.getZoomFactor() , 100);
        specman.diagrammAktualisieren(step);
    }

    // GlassPane and add Step to sequence
    private void checkSchleifenHeading(SchleifenSchrittView schleife, Point pos, int glassPaneHeight, InsertDecision insertDecision, MouseEvent mE) throws EditException {
        Point p = SwingUtilities.convertPoint(schleife.getTextShef(), 0, 0, specman);
        Rectangle r = createRectangle(p, schleife.getTextShef());
        if (r.contains(pos)) {
            createGlassPane(r.width, r.x, r.y + r.height - glassPaneHeight, glassPaneHeight, true);

            //mouserelease add Step at first Position in sequenz
            if (insertDecision == InsertDecision.Insert) {
                AbstractSchrittView step = schleife.getWiederholSequenz().schritte.get(0);
                addNeuerSchritt(Before, step, mE);
            }
        }
    }

    // GlassPane and add Step to sequence
    private void checkSubsequenzHeading(SubsequenzSchrittView schritt, Point pos, int glassPaneHeight, InsertDecision insertDecision, MouseEvent mE) throws EditException {
        Point p = SwingUtilities.convertPoint(schritt.getTextShef(), 0, 0, specman);
        Rectangle r = createRectangle(p, schritt.getTextShef());
        if (r.contains(pos)) {
            createGlassPane(r.width, r.x, r.y + r.height - glassPaneHeight, glassPaneHeight, true);

            //mouserelease add Step at first Position in sequenz
            if (insertDecision == InsertDecision.Insert) {
                AbstractSchrittView step = schritt.getSubsequenz().schritte.get(0);
                addNeuerSchritt(Before, step, mE);
            }
        }
    }

    // GlassPane and add Step to sequence
    private boolean checkGlassPaneforComponent(AbstractSchrittView step, Point pos, int glassPaneHeight, InsertDecision insertDecision, MouseEvent mE) throws EditException {
        Component c = null;
        Component cl = null;
        Component cu = null;
        Rectangle r = new Rectangle();
        Rectangle rl = new Rectangle();
        Rectangle ru = new Rectangle();
        Point p = null;
        Point pl;
        Point pu;

        if (step instanceof SchleifenSchrittView) {
            c = ((SchleifenSchrittView) step).getPanel();
            cl = ((SchleifenSchrittView) step).getLinkerBalken();
            cu = ((SchleifenSchrittView) step).getUntererBalken();
        } else if (step instanceof SubsequenzSchrittView) {
            c = step.getTextShef();
        } else if (step instanceof BreakSchrittView) {
            c = ((BreakSchrittView) step).getPanel();
            //Abfrage für MousePosition auf dem Panel des Breakschritts
            cl = c;
        }

        if (c != null) {
            p = SwingUtilities.convertPoint(c, 0, 0, specman);
            r = c.getBounds();
            r.setLocation(p);
        }
        if (cl != null) {
            pl = SwingUtilities.convertPoint(cl, 0, 0, specman);
            rl = cl.getBounds();
            rl.setLocation(pl);
        }
        if (cu != null) {
            pu = SwingUtilities.convertPoint(cu, 0, 0, specman);
            ru = cu.getBounds();
            ru.setLocation(pu);
        }
        //Abfrage ob der Mousezeiger über dem linken oder unteren Balken ist
        if (rl.contains(pos) || ru.contains(pos)) {
            createGlassPane(r.width, r.x, r.y + r.height - glassPaneHeight, glassPaneHeight, true);
            //bei Release hinzufügen eines Schrittes an erster Position im Zweig
            if (((insertDecision==InsertDecision.Insert) && cl != null && rl.contains(pos)) || ((insertDecision == InsertDecision.Insert) && cu != null && ru.contains(pos))) {
                addNeuerSchritt(After, step, mE);
                return true;
            }
        }
        return false;
    }

    //GlassPane
    private void checkfalseGlassPaneforComponent(EditContainer c, Point pos, int glassPaneHeight) {
        Point p = SwingUtilities.convertPoint(c, 0, 0, specman);
        Rectangle r = createRectangle(p, c);
        if (r.contains(pos)) {
            createGlassPane(r.width, r.x, r.y + r.height - glassPaneHeight, glassPaneHeight, false);
        }
    }//Recursive method to check and add Steps

    public void dragGlassPanePos(Point pos, List<AbstractSchrittView> schrittListe, InsertDecision insertDecision, MouseEvent e) throws EditException {
        int glassPaneHeight = 5;
        GlassPane glassPane = (GlassPane) specman.getGlassPane();
        Point p;
        //Abfrage,damit ein Schritt nicht auf oder in sich selbst verschoben werden kann
        //TODO Cursoranpassung funktioniert noch nicht
        if (e.getSource() instanceof SchrittNummerLabel) {
            SchrittNummerLabel label = (SchrittNummerLabel) e.getSource();
            AbstractSchrittView step = specman.getHauptSequenz().findeSchritt(label);
            Point checkPoint = SwingUtilities.convertPoint(step.getPanel(), 0, 0, specman);
            Rectangle rec = step.getPanel().getVisibleRect();
            rec.setLocation(checkPoint);
            //Letzer Schritt darf nicht verschoben werden
            if (step.getParent().schritte.size() <= 1) {
                return;
            }
            //Abfrage ob man sich auf sich selbst befindet
            if (rec.contains(pos)) {
                return;
            }
        }

        //inserts firststep to empty diagram
        insertFirstStep(schrittListe, insertDecision, e);
        for (AbstractSchrittView schritt : schrittListe) {
            p = SwingUtilities.convertPoint(schritt.getPanel(), 0, 0, specman);
            Rectangle r = createRectangle(p, schritt.getPanel());

            if(schritt.getAenderungsart()==Aenderungsart.Geloescht){
                //Auf einem Gelöschten Schritt nur verbot zeigen
                
            }else {

                //Abfrage ob es sich um den letzten Schritt einer Subsquenz gehört // Zusätzliche Abfrage da Marker beim CaseAnängen angezeigt wurde
                if (!(e.getSource().equals(specman.getCaseAnhaengen()))) {
                    if (schrittListe.get(schrittListe.size() - 1) == schritt && schritt.getId().nummern.size() > 1) {
                        if (lastPixels(pos, p, glassPaneHeight, r, glassPane, schritt, insertDecision)) {
                            break;
                        }
                    }
                }

                //Add Case
                if (e.getSource().equals(specman.getCaseAnhaengen())) {
                    if (schritt instanceof CaseSchrittView) {
                        CaseSchrittView caseSchritt = (CaseSchrittView) schritt;

                    //checkfalseGlassPaneforComponent(caseSchritt.getTextShef(), pos, glassPaneHeight);
                    checkCaseHeading(caseSchritt.getSonstSequenz(), pos, glassPaneHeight, (int)caseSchritt.breiteLayoutspalteBerechnen(), insertDecision);
                    dragGlassPanePos(pos, caseSchritt.getSonstSequenz().schritte, insertDecision, e);

                        for (ZweigSchrittSequenzView caseSequenz : caseSchritt.getCaseSequenzen()) {
                            int groesse = caseSchritt.getCaseSequenzen().size();
                            checkCaseHeading(caseSequenz, pos, glassPaneHeight, 0, insertDecision);
                            dragGlassPanePos(pos, caseSequenz.schritte, insertDecision, e);

                            if (groesse != caseSchritt.getCaseSequenzen().size()) {
                                break;
                            }
                        }
                    } else {
                        if (schritt instanceof IfElseSchrittView || schritt instanceof IfSchrittView) {
                            IfElseSchrittView ifel = (IfElseSchrittView) schritt;
                            if (schritt instanceof IfElseSchrittView) {
                                dragGlassPanePos(pos, ifel.getIfSequenz().schritte, insertDecision, e);
                            }
                            dragGlassPanePos(pos, ifel.getElseSequenz().schritte, insertDecision, e);

                        } else if (schritt instanceof WhileSchrittView || schritt instanceof WhileWhileSchrittView) {
                            SchleifenSchrittView schleife = (SchleifenSchrittView) schritt;
                            dragGlassPanePos(pos, schleife.getWiederholSequenz().schritte, insertDecision, e);

                        } else if (schritt instanceof SubsequenzSchrittView) {
                            SubsequenzSchrittView sub = (SubsequenzSchrittView) schritt;
                            dragGlassPanePos(pos, sub.getSequenz().schritte, insertDecision, e);
                        }
                    }
                    //Add Step
                } else {
                    //Abfrage einfacherSchritt
                    int groesse = schrittListe.size();
                    if (schritt instanceof EinfacherSchrittView) {
                        if (r.contains(pos)) {
                            glassPane.setInputRecBounds(r.x, r.y + r.height - glassPaneHeight, r.width, glassPaneHeight);

                            if (checkFirstStep(schritt, pos, glassPaneHeight, insertDecision, e)) {
                                break;
                            } else {
                                glassPane.setInputRecBounds(r.x, r.y + r.height - glassPaneHeight, r.width, glassPaneHeight);
                                if (insertDecision == InsertDecision.Insert) {
                                    addNeuerSchritt(After, schritt, e);
                                }
                            }
                            specman.getGlassPane().setVisible(true);
                            break;
                        }
                        //Abfrage IfElseSchritt
                    } else if (schritt instanceof IfElseSchrittView || schritt instanceof IfSchrittView) {

                        IfElseSchrittView ifel = (IfElseSchrittView) schritt;
                        if (checkFirstStep(schritt, pos, glassPaneHeight, insertDecision, e)) {
                            break;
                        }

                    
                        //checkfalseGlassPaneforComponent(ifel.getTextShef(), pos, glassPaneHeight);
                    if (schritt instanceof IfElseSchrittView) {
                        checkZweigHeading(ifel.getIfSequenz(), pos, glassPaneHeight, (int)ifel.breiteLayoutspalteBerechnen(),insertDecision, e);
                        dragGlassPanePos(pos, ifel.getIfSequenz().schritte, insertDecision, e);
                    }
                    checkZweigHeading(ifel.getElseSequenz(), pos, glassPaneHeight,- (int)ifel.breiteLayoutspalteBerechnen(), insertDecision, e);
                    dragGlassPanePos(pos, ifel.getElseSequenz().schritte, insertDecision, e);

                        //wenn letzter Step in Sequenz beenden der Rekusiven Methode und verwenden des Übergeordneten Schrittes
                        if (lastStep) {
                            addNeuerSchritt(After, ifel, e);
                            lastStep = false;
                            break;
                        }
                        //While Schritt
                    } else if (schritt instanceof WhileSchrittView || schritt instanceof WhileWhileSchrittView) {

                        SchleifenSchrittView schleife = (SchleifenSchrittView) schritt;
                        if (checkFirstStep(schritt, pos, glassPaneHeight, insertDecision, e)) {
                            break;
                        }
                        if (checkGlassPaneforComponent(schleife, pos, glassPaneHeight, insertDecision, e)) {
                            break;
                        }

                        dragGlassPanePos(pos, schleife.getWiederholSequenz().schritte, insertDecision, e);
                        checkSchleifenHeading(schleife, pos, glassPaneHeight, insertDecision, e);

                    //wenn letzter Step in Sequenz beenden der Rekusiven Methode und verwenden des Übergeordneten Schrittes
                    if (lastStep) {
                        addNeuerSchritt(After, schleife, e);
                        lastStep = false;
                        break;
                    }
                } else if (schritt instanceof CaseSchrittView) {
                    CaseSchrittView caseSchritt = (CaseSchrittView) schritt;
                    if (checkFirstStep(schritt, pos, glassPaneHeight, insertDecision, e)) {
                        break;
                    }
                    //checkfalseGlassPaneforComponent(caseSchritt.getTextShef(), pos, glassPaneHeight);
                    checkZweigHeading(caseSchritt.getSonstSequenz(), pos, glassPaneHeight,(int)caseSchritt.breiteLayoutspalteBerechnen(), insertDecision, e);
                    dragGlassPanePos(pos, caseSchritt.getSonstSequenz().schritte, insertDecision, e);
                    for (ZweigSchrittSequenzView caseSequenz : caseSchritt.getCaseSequenzen()) {
                    	if(caseSchritt.getCaseSequenzen().get(0) == caseSequenz) {
                    		 checkZweigHeading(caseSequenz, pos, glassPaneHeight,-(int)caseSchritt.breiteLayoutspalteBerechnen(), insertDecision, e);
                    	}else {
	                        checkZweigHeading(caseSequenz, pos, glassPaneHeight, insertDecision, e);
                    	}
                    	dragGlassPanePos(pos, caseSequenz.schritte, insertDecision, e);
                        //wenn letzter Step in Sequenz beenden der Rekusiven Methode und verwenden des Übergeordneten Schrittes
                        if (lastStep) {
                            addNeuerSchritt(After, caseSchritt, e);
                            lastStep = false;
                            break;
                        }
                    }
                } else if (schritt instanceof SubsequenzSchrittView) {
                    SubsequenzSchrittView sub = (SubsequenzSchrittView) schritt;
                    if (checkFirstStep(schritt, pos, glassPaneHeight, insertDecision, e)) {
                        break;
                    }
                    checkGlassPaneforComponent(sub, pos, glassPaneHeight, insertDecision, e);
                    dragGlassPanePos(pos, sub.getSequenz().schritte, insertDecision, e);
                    checkSubsequenzHeading(sub, pos, glassPaneHeight, insertDecision, e);
                    //wenn letzter Step in Sequenz beenden der Rekusiven Methode und verwenden des Übergeordneten Schrittes
                    if (lastStep) {
                        addNeuerSchritt(After, sub, e);
                        lastStep = false;
                        break;
                    }
                } else if (schritt instanceof BreakSchrittView) {
                    BreakSchrittView breakSchritt = (BreakSchrittView) schritt;
                    if (checkFirstStep(schritt, pos, glassPaneHeight, insertDecision, e)) {
                        break;
                    }
                    checkGlassPaneforComponent(breakSchritt, pos, glassPaneHeight, insertDecision, e);

                        //wenn letzter Step in Sequenz beenden der Rekusiven Methode und verwenden des Übergeordneten Schrittes
                        if (lastStep) {
                            addNeuerSchritt(After, breakSchritt, e);
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
    }

    //Creates Rectangle that refelcts a Step with Location and size
    private Rectangle createRectangle(Point p, EditContainer textShef) {
        Rectangle r = textShef.getVisibleRect();
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
        GlassPane gP = (GlassPane) specman.getGlassPane();
        gP.setInputRecBounds(i, y, glassPaneHeight, height);
        specman.getGlassPane().setVisible(b);
    }

    private boolean checkFirstStep(AbstractSchrittView schritt, Point pos, int glassPaneHeight, InsertDecision insertDecision, MouseEvent e) throws EditException {
        Point p = SwingUtilities.convertPoint(schritt.getPanel(), 0, 0, specman);
        Rectangle r = schritt.getPanel().getBounds();
        String id = "1";
        r.setLocation(p);
        if (r.contains(pos)) {
        	for( AbstractSchrittView vergleichsstep :  specman.hauptSequenz.schritte) {
        		if(!(vergleichsstep.getAenderungsart() == Aenderungsart.Geloescht || vergleichsstep.getAenderungsart() == Aenderungsart.Quellschritt )) {
        			id = vergleichsstep.getId().toString();
        			break;
        		}
        	}
        	
        	
            if (schritt.getId().toString().equals(id) && (pos.y < (r.y + glassPaneHeight))) {
                GlassPane glassPane = (GlassPane) specman.getGlassPane();
                glassPane.setInputRecBounds(r.x,r.y, r.width, glassPaneHeight);
                glassPane.setVisible(true);
                if (insertDecision == InsertDecision.Insert) {
                    addNeuerSchritt(Before, schritt, e);
                }
                return true;
            }
        }
        return false;
    }

    // Entfernt die WelcomeMessage und fügt den neuen Schritt hinzu
    private void insertFirstStep(List<AbstractSchrittView> schrittListe, InsertDecision insertDecision, MouseEvent e) {
        if (schrittListe.size() == 0 && (insertDecision == InsertDecision.Insert)) {
            SchrittSequenzView curSequenz = specman.getHauptSequenz();
            specman.dropWelcomeMessage();
            addNeuerSchritt(e, curSequenz);
        }
    }

    //Neuen Schritt zwischenschieben abhängig vom Button
    private void addNeuerSchritt(RelativeStepPosition insertionPosition, AbstractSchrittView referenceStep, MouseEvent e) throws EditException {
        SchrittSequenzView sequenz = referenceStep.getParent();

        if (e.getSource() instanceof SchrittNummerLabel) { // i.e. dragging existing step to a different position
            SchrittNummerLabel label = (SchrittNummerLabel) e.getSource();
            moveStep(insertionPosition, referenceStep, label);
        }

        else if (e.getSource().equals(specman.getEinfachenSchrittAnhaengen())) {
            referenceStep = sequenz.einfachenSchrittZwischenschieben(insertionPosition, referenceStep, specman);
            specman.newStepPostInit(referenceStep);
            specman.hauptSequenz.resyncSchrittnummerStil();
        }
        else if (e.getSource().equals(specman.getWhileSchrittAnhaengen())) {
            referenceStep = sequenz.whileSchrittZwischenschieben(insertionPosition, referenceStep, specman);
            specman.newStepPostInit(referenceStep);
            specman.hauptSequenz.resyncSchrittnummerStil();
        }
        else if (e.getSource().equals(specman.getWhileWhileSchrittAnhaengen())) {
            referenceStep = sequenz.whileWhileSchrittZwischenschieben(insertionPosition, referenceStep, specman);
            specman.newStepPostInit(referenceStep);
            specman.hauptSequenz.resyncSchrittnummerStil();
        }
        else if (e.getSource().equals(specman.getIfElseSchrittAnhaengen())) {
            referenceStep = sequenz.ifElseSchrittZwischenschieben(insertionPosition, referenceStep, specman);
            specman.newStepPostInit(referenceStep);
            specman.hauptSequenz.resyncSchrittnummerStil();
        }
        else if (e.getSource().equals(specman.getIfSchrittAnhaengen())) {
            referenceStep = sequenz.ifSchrittZwischenschieben(insertionPosition, referenceStep, specman);
            specman.newStepPostInit(referenceStep);
            specman.hauptSequenz.resyncSchrittnummerStil();
        }
        else if (e.getSource().equals(specman.getCaseSchrittAnhaengen())) {
            referenceStep = sequenz.caseSchrittZwischenschieben(insertionPosition, referenceStep, specman);
            specman.newStepPostInit(referenceStep);
            specman.hauptSequenz.resyncSchrittnummerStil();
        }
        else if (e.getSource().equals(specman.getSubsequenzSchrittAnhaengen())) {
            referenceStep = sequenz.subsequenzSchrittZwischenschieben(insertionPosition, referenceStep, specman);
            specman.newStepPostInit(referenceStep);
            specman.hauptSequenz.resyncSchrittnummerStil();
        }
        else if (e.getSource().equals(specman.getBreakSchrittAnhaengen())) {
            referenceStep = sequenz.breakSchrittZwischenschieben(insertionPosition, referenceStep, specman);
            specman.newStepPostInit(referenceStep);
            specman.hauptSequenz.resyncSchrittnummerStil();
        }
        else if (e.getSource().equals(specman.getCatchSchrittAnhaengen())) {
            referenceStep = sequenz.catchSchrittZwischenschieben(insertionPosition, referenceStep, specman);
            specman.newStepPostInit(referenceStep);
            specman.hauptSequenz.resyncSchrittnummerStil();
        }
    }

    private void moveStep(RelativeStepPosition insertionPosition, AbstractSchrittView referenceStep, SchrittNummerLabel label)
        throws EditException {
        try (UndoRecording ur = specman.composeUndo()) {
            AbstractSchrittView movingStep = specman.getHauptSequenz().findeSchritt(label);
            if (movingStep == referenceStep) {
                // Step must not be placed before or after itself. Should not be possible by the dragging logic anyway.
                return;
            }
            SchrittSequenzView sourceSequence = referenceStep.getParent();
            if(specman.aenderungenVerfolgen() && movingStep.getAenderungsart() != Aenderungsart.Hinzugefuegt) {
                QuellSchrittView quellschritt;
                sourceSequence = movingStep.getParent();
                if(movingStep.getQuellschritt() == null) {
                    quellschritt = new QuellSchrittView(specman, sourceSequence, movingStep.getId());
                    sourceSequence.schrittZwischenschieben(quellschritt, Before, movingStep);
                }
                else {
                    quellschritt = movingStep.getQuellschritt();
                }
                SchrittSequenzView originalParent = movingStep.getParent();
                int originalIndex = originalParent.schrittEntfernen(movingStep);
                movingStep.setId(referenceStep.newStepIDInSameSequence(insertionPosition));
                movingStep.setParent(referenceStep.getParent());
                sourceSequence.schrittZwischenschieben(movingStep, insertionPosition, referenceStep);
                specman.addEdit(new UndoableSchrittVerschobenMarkiert(movingStep, originalParent, originalIndex, quellschritt));
                movingStep.setQuellschrittUDBL(quellschritt);
                movingStep.setZielschrittStilUDBL();
                quellschritt.setZielschrittUDBL(movingStep);
                specman.hauptSequenz.resyncSchrittnummerStil();
            }
            else {
                SchrittSequenzView originalParent = movingStep.getParent();
                int originalIndex = originalParent.schrittEntfernen(movingStep);
                movingStep.setId(referenceStep.newStepIDInSameSequence(insertionPosition));
                movingStep.setParent(referenceStep.getParent());
                sourceSequence.schrittZwischenschieben(movingStep, insertionPosition, referenceStep);
                specman.addEdit(new UndoableSchrittVerschoben(movingStep, originalParent, originalIndex));
            }
        }
    }

    //Neuen Schritt anhängen abhängig vom Button
    private void addNeuerSchritt(MouseEvent e, SchrittSequenzView sequenz) {
        if (e.getSource().equals(specman.getEinfachenSchrittAnhaengen())) {
            sequenz.einfachenSchrittAnhaengen(specman);
        } else if (e.getSource().equals(specman.getWhileSchrittAnhaengen())) {
            sequenz.whileSchrittAnhaengen(specman);
        } else if (e.getSource().equals(specman.getWhileWhileSchrittAnhaengen())) {
            sequenz.whileWhileSchrittAnhaengen(specman);
        } else if (e.getSource().equals(specman.getIfElseSchrittAnhaengen())) {
            sequenz.ifElseSchrittAnhaengen(specman);
        } else if (e.getSource().equals(specman.getIfSchrittAnhaengen())) {
            sequenz.ifSchrittAnhaengen(specman);
        } else if (e.getSource().equals(specman.getCaseSchrittAnhaengen())) {
            sequenz.caseSchrittAnhaengen(specman);
        } else if (e.getSource().equals(specman.getSubsequenzSchrittAnhaengen())) {
            sequenz.subsequenzSchrittAnhaengen(specman);
        } else if (e.getSource().equals(specman.getBreakSchrittAnhaengen())) {
            sequenz.breakSchrittAnhaengen(specman);
        } else if (e.getSource().equals(specman.getCatchSchrittAnhaengen())) {
            sequenz.catchSchrittAnhaengen(specman);
        }
    }

    private boolean lastPixels(Point pos, Point p, int glassPaneHeight, Rectangle r, GlassPane glassPane, AbstractSchrittView schritt, InsertDecision insertDecision) {
        if (!(schritt.getParent().getParent() instanceof WhileWhileSchrittView)) {
            r.setLocation(p);
            if (r.contains(pos)) {
                if (pos.y > (r.y + r.height - glassPaneHeight)) {
                    Container container = schritt.getParent().getContainer().getParent();
                    p = SwingUtilities.convertPoint(container, 0, 0, specman);
                    glassPane.setInputRecBounds(p.x - 2, p.y + container.getHeight()+1, container.getWidth() + 4, glassPaneHeight);
                    specman.getGlassPane().setVisible(true);
                    if (insertDecision == InsertDecision.Insert) {
                        //hier wird nur festgestellt, dass es sich um den letzten Schritt in der Sequenz handelt und das Iterieren beendet -> Sprung zurück in vorherige Ebene
                        lastStep = true;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    //TODO funktioniert nicht wird überschrieben
    public void showInvalidCursor() {
        try {
            specman.setCursor(Cursor.getSystemCustomCursor("Invalid.32x32"));
        } catch (HeadlessException | AWTException e) {
            e.printStackTrace();
        }
    }
}