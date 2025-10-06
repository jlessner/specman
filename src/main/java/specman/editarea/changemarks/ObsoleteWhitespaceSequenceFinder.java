package specman.editarea.changemarks;

import specman.editarea.document.WrappedDocumentI;
import specman.editarea.document.WrappedPosition;

import java.util.ArrayList;
import java.util.List;

class ObsoleteWhitespaceSequenceFinder {
  private final WrappedDocumentI document;
  private final WrappedPosition cutPosition;

  public ObsoleteWhitespaceSequenceFinder(WrappedDocumentI document, WrappedPosition cutPosition) {
    this.document = document;
    this.cutPosition = cutPosition;
  }

  public List<ObsoleteWhitespaceSequence> findAll() {
    WrappedPosition paragraphStart = findLeft(cutPosition, CharType.ParagraphBoundary);
    WrappedPosition paragraphEnd = findRight(cutPosition, CharType.ParagraphBoundary);
    List<ObsoleteWhitespaceSequence> sequences = findAllFullLength(paragraphStart, paragraphEnd);
    List<ObsoleteWhitespaceSequence> obsolete = new ArrayList<>();
    for (ObsoleteWhitespaceSequence seq : sequences) {
      if (isAtParagraphStart(seq, paragraphStart)
        || isAtParagraphEnd(seq, paragraphEnd)
        || includesCutPosition(seq, cutPosition)) {
        obsolete.add(seq);
      }
      else if (seq.length() > 1) {
        obsolete.add(new ObsoleteWhitespaceSequence(seq.start.inc(), seq.end));
      }
    }
    return obsolete;
  }

  private boolean includesCutPosition(ObsoleteWhitespaceSequence seq, WrappedPosition cutPosition) {
    return
      !cutPosition.less(seq.start) &&
      !cutPosition.greater(seq.end);
  }

  private boolean isAtParagraphEnd(ObsoleteWhitespaceSequence seq, WrappedPosition paragraphEnd) {
    return seq.end.equals(paragraphEnd) || seq.end.inc().equals(paragraphEnd);
  }

  private boolean isAtParagraphStart(ObsoleteWhitespaceSequence seq, WrappedPosition paragraphStart) {
    if (paragraphStart != null) {
      return paragraphStart.equals(seq.start);
    }
    return seq.start.isZero();
  }

  public List<ObsoleteWhitespaceSequence> findAllFullLength(WrappedPosition paragraphStart, WrappedPosition paragraphEnd) {
    List<ObsoleteWhitespaceSequence> sequences = new ArrayList<>();
    WrappedPosition nextSearchStart = paragraphStart;
    while (nextSearchStart.less(paragraphEnd)) {
      ObsoleteWhitespaceSequence nextSequence = findnextWhitespaceSequence(nextSearchStart, paragraphStart, paragraphEnd);
      if (nextSequence != null) {
        addUniqueSequence(sequences, nextSequence);
        nextSearchStart = nextSequence.end.inc();
      } else {
        break;
      }
    }
    return sequences;
  }

  private ObsoleteWhitespaceSequence findnextWhitespaceSequence(WrappedPosition searchStart, WrappedPosition paragraphStart, WrappedPosition paragraphEnd) {
    WrappedPosition whitestart = findRight(searchStart, CharType.Whitespace);
    if (whitestart == null || whitestart.greater(paragraphEnd)) {
      return null;
    }
    WrappedPosition whiteend = findRight(whitestart, CharType.NonWhitespace);
    if (whiteend == null) {
      whiteend = document.fromModel(document.getLength());
    }
    return new ObsoleteWhitespaceSequence(whitestart, whiteend.dec());
  }

  private void addUniqueSequence(List<ObsoleteWhitespaceSequence> sequences, ObsoleteWhitespaceSequence sequence) {
    if (sequence != null && !sequences.contains(sequence)) {
      sequences.add(sequence);
    }
  }

  private WrappedPosition findLeft(WrappedPosition from, CharType charType) {
    WrappedPosition pos = from.isZero() ? from : from.dec();
    while (!pos.isZero()) {
      if (charType.at(pos)) {
        break;
      }
      pos = pos.dec();
    }
    return pos;
  }

  private WrappedPosition findRight(WrappedPosition pos, CharType charType) {
    while (pos.exists()) {
      if (charType.at(pos)) {
        return pos;
      }
      pos = pos.inc();
    }
    return null;
  }


}
