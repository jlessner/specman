package specman.editarea.changemarks;

import specman.editarea.document.WrappedDocument;
import specman.model.v001.Aenderungsmarkierung_V001;

import java.util.List;

public class ChangemarkRestorer {
  final WrappedDocument target;
  final MarkedCharSequence source;

  public ChangemarkRestorer(WrappedDocument target, MarkedCharSequence source) {
    this.target = target;
    this.source = source;
  }

  public List<Aenderungsmarkierung_V001> restore() {
    List<Aenderungsmarkierung_V001> result = new java.util.ArrayList<>();

    return result;
  }
}
