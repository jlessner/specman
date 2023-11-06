package specman.editarea.litrack;

import net.atlanticbb.tantlinger.ui.text.WysiwygHTMLEditorKit;

import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.ListView;

/** This is an extension of Shef's {@link WysiwygHTMLEditorKit} which returns a
 * view factory creating a specialized view for the rendering of HTML ordered lists
 * and unordered lists. This is required for PDF rendering. For details see class
 * {@link LIRecordingListView} */
public class LIRecordingHTMLEditorKit extends WysiwygHTMLEditorKit {
  @Override
  public ViewFactory getViewFactory() {
    return new LITrackingViewFactory(super.getViewFactory());
  }

  public static class LITrackingViewFactory implements ViewFactory {
    private ViewFactory core;

    public LITrackingViewFactory(ViewFactory core) {
      this.core = core;
    }

    @Override
    public View create(Element elem) {
      View view = core.create(elem);
      if (view instanceof ListView) {
        return new LIRecordingListView(elem);
      }
      return view;
    }
  }

}
