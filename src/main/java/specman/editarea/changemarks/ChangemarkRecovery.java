package specman.editarea.changemarks;

import specman.editarea.document.WrappedDocumentI;
import specman.editarea.document.WrappedPosition;
import specman.model.v001.Aenderungsmarkierung_V001;

import java.util.List;

import static specman.editarea.changemarks.CharType.NonWhitespace;

public class ChangemarkRecovery {
  final WrappedDocumentI target;
  final MarkedCharSequence source;
  final boolean[] marksPerChar;
  WrappedPosition targetProcess;
  Integer sourceProcess;

  public ChangemarkRecovery(WrappedDocumentI target, MarkedCharSequence source) {
    this.target = target;
    this.source = source;
    this.marksPerChar = new boolean[target.getLength()];
    this.targetProcess = target.fromModel(0);
    this.sourceProcess = 0;
  }

  public List<Aenderungsmarkierung_V001> recover() {
    WrappedPosition nextTargetVisibleCharSeqStart;
    Integer nextSourceVisibleCharSeqStart;
    do {
      nextTargetVisibleCharSeqStart = findRight(targetProcess, NonWhitespace);
      nextSourceVisibleCharSeqStart = source.findRight(sourceProcess, NonWhitespace);
      recoverSkippedWhitespaceMarks(nextTargetVisibleCharSeqStart, nextSourceVisibleCharSeqStart);
      recoverVisibleCharMarks(nextTargetVisibleCharSeqStart, nextSourceVisibleCharSeqStart);
    }
    while(nextTargetVisibleCharSeqStart != null);

    return assembleChangemarksFromBooleans();
  }

  private void recoverVisibleCharMarks(WrappedPosition targetPos, Integer sourcePos) {
    if (targetPos == null) {
      return;
    }
    do {
      marksPerChar[targetPos.toModel()] = source.isChanged(sourcePos);
      targetPos = targetPos.inc();
      sourcePos++;
    }
    while(targetPos.exists() && source.isVisibleChar(sourcePos));
    targetProcess = targetPos;
    sourceProcess = sourcePos;
  }

  /** TODO: Skipped whitespaces might also have changemarks, which need to be recovered. */
  private void recoverSkippedWhitespaceMarks(WrappedPosition targetVisibleCharSeqStart, Integer sourceVisibleCharSeqStart) {
    if (targetVisibleCharSeqStart == null || targetVisibleCharSeqStart.equals(targetProcess)) {
      return;
    }
    int targetWhitespaceLen = targetWhitespaceLen(targetVisibleCharSeqStart);
    int sourceWhitespaceLen = sourceWhitespaceLen(sourceVisibleCharSeqStart);
    if (targetWhitespaceLen == sourceWhitespaceLen) {
      WrappedPosition targetWhitespacePos = targetProcess;
      int sourceWhitespacePos = sourceProcess;
      for (int i = 0; i < targetWhitespaceLen; i++) {
        marksPerChar[targetWhitespacePos.toModel()] = source.isChanged(sourceWhitespacePos);
        targetWhitespacePos = targetWhitespacePos.inc();
        sourceWhitespacePos++;
      }
    }

  }

  private int sourceWhitespaceLen(Integer sourceVisibleCharSeqStart) {
    return sourceVisibleCharSeqStart - sourceProcess;
  }

  private int targetWhitespaceLen(WrappedPosition targetVisibleCharSeqStart) {
    return targetVisibleCharSeqStart.distance(targetProcess);
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

  private List<Aenderungsmarkierung_V001> assembleChangemarksFromBooleans() {
    List<Aenderungsmarkierung_V001> changemarks = new java.util.ArrayList<>();
    int markStart = -1;
    for (int i = 0; i < marksPerChar.length; i++) {
      if (marksPerChar[i]) {
        if (markStart < 0) {
          markStart = i;
        }
      }
      else {
        if (markStart >= 0) {
          changemarks.add(new Aenderungsmarkierung_V001(markStart, i - 1));
          markStart = -1;
        }
      }
    }
    if (markStart >= 0) {
      changemarks.add(new Aenderungsmarkierung_V001(markStart, marksPerChar.length - 1));
    }
    return changemarks;
  }
}
