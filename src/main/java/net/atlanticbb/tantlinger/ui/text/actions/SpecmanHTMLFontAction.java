package net.atlanticbb.tantlinger.ui.text.actions;

import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;
import net.atlanticbb.tantlinger.ui.text.dialogs.HTMLFontDialog;
import net.atlanticbb.tantlinger.ui.text.dialogs.SpecmanHTMLFontDialog;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.ActionEvent;

/** This class was extracted from the SHEF library and was modified to instanciate
 * {@link SpecmanHTMLFontAction} rather than {@link HTMLFontDialog}. Unfortunately
 * the class was not really designed for modification, and we didn't find an elegant
 * object-oriented approach. */
public class SpecmanHTMLFontAction extends HTMLTextEditAction {
  private static final long serialVersionUID = 1L;

  public SpecmanHTMLFontAction() {
    super(i18n.str("font_"));
  }

  protected void sourceEditPerformed(ActionEvent e, JEditorPane editor) {
    SpecmanHTMLFontDialog d = this.createDialog(editor);
    d.setLocationRelativeTo(d.getParent());
    d.setVisible(true);
    if (!d.hasUserCancelled()) {
      editor.requestFocusInWindow();
      editor.replaceSelection(d.getHTML());
    }

  }

  protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) {
    HTMLDocument doc = (HTMLDocument)editor.getDocument();
    Element chElem = doc.getCharacterElement(editor.getCaretPosition());
    AttributeSet sas = chElem.getAttributes();
    SpecmanHTMLFontDialog d = this.createDialog(editor);
    d.setBold(sas.containsAttribute(StyleConstants.Bold, Boolean.TRUE));
    d.setItalic(sas.containsAttribute(StyleConstants.Italic, Boolean.TRUE));
    d.setUnderline(sas.containsAttribute(StyleConstants.Underline, Boolean.TRUE));
    Object o = sas.getAttribute(StyleConstants.FontFamily);
    if (o != null) {
      d.setFontName(o.toString());
    }

    o = sas.getAttribute(StyleConstants.FontSize);
    if (o != null) {
      try {
        d.setFontSize(Integer.parseInt(o.toString()));
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    d.setLocationRelativeTo(d.getParent());
    d.setVisible(true);
    if (!d.hasUserCancelled()) {
      MutableAttributeSet tagAttrs = new SimpleAttributeSet();
      tagAttrs.addAttribute(StyleConstants.FontFamily, d.getFontName());
      tagAttrs.addAttribute(StyleConstants.FontSize, new Integer(d.getFontSize()));
      tagAttrs.addAttribute(StyleConstants.Bold, new Boolean(d.isBold()));
      tagAttrs.addAttribute(StyleConstants.Italic, new Boolean(d.isItalic()));
      tagAttrs.addAttribute(StyleConstants.Underline, new Boolean(d.isUnderline()));
      CompoundUndoManager.beginCompoundEdit(editor.getDocument());
      HTMLUtils.setCharacterAttributes(editor, tagAttrs);
      CompoundUndoManager.endCompoundEdit(editor.getDocument());
    }

  }

  private SpecmanHTMLFontDialog createDialog(JTextComponent ed) {
    Window w = SwingUtilities.getWindowAncestor(ed);
    String t = "";
    if (ed.getSelectedText() != null) {
      t = ed.getSelectedText();
    }

    SpecmanHTMLFontDialog d = null;
    if (w != null && w instanceof Frame) {
      d = new SpecmanHTMLFontDialog((Frame)w, t);
    } else if (w != null && w instanceof Dialog) {
      d = new SpecmanHTMLFontDialog((Dialog)w, t);
    }

    return d;
  }
}
