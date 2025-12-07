package net.atlanticbb.tantlinger.ui.text.actions;

import org.bushe.swing.action.ActionList;

/** Creates SpecmanHTMLFontSizeActions rather than plain HTMLFontSizeActions from
 * the Shef library. The Specman variant takes the current zoom factor into account. */
public class SpecmanFontSizeActionFactory {

  public static ActionList createFontSizeActionList() {
    ActionList list = new ActionList("font-size");
    int[] t = SpecmanFontSizeAction.FONT_SIZES;

    for(int i = 0; i < t.length; ++i) {
      list.add(new SpecmanFontSizeAction(i));
    }

    return list;
  }

}
