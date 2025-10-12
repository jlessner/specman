package specman.editarea.markups;

import specman.editarea.document.WrappedDocumentI;
import specman.editarea.document.WrappedPosition;
import specman.model.v001.Markup_V001;

import java.util.List;
import java.util.Objects;

import static specman.editarea.markups.CharType.NonWhitespace;

public class MarkupRecovery {
  final WrappedDocumentI target;
  final MarkedCharSequence source;
  final MarkupType[] marksPerChar;
  WrappedPosition targetProcess;
  Integer sourceProcess;

  public MarkupRecovery(WrappedDocumentI target, MarkedCharSequence source) {
    this.target = target;
    this.source = source;
    this.marksPerChar = new MarkupType[target.getLength()];
    this.targetProcess = target.fromModel(0);
    this.sourceProcess = 0;
  }

  public List<Markup_V001> recover() {
    WrappedPosition nextTargetVisibleCharSeqStart;
    Integer nextSourceVisibleCharSeqStart;
    do {
      nextTargetVisibleCharSeqStart = findRight(targetProcess, NonWhitespace);
      nextSourceVisibleCharSeqStart = source.findRight(sourceProcess, NonWhitespace);
      recoverSkippedWhitespaceMarks(nextTargetVisibleCharSeqStart, nextSourceVisibleCharSeqStart);
      recoverVisibleCharMarks(nextTargetVisibleCharSeqStart, nextSourceVisibleCharSeqStart);
    }
    while(nextTargetVisibleCharSeqStart != null);

    return assembleMarkupsFromMarkupsPerChar();
  }

  private void recoverVisibleCharMarks(WrappedPosition targetPos, Integer sourcePos) {
    if (targetPos == null) {
      return;
    }
    do {
      marksPerChar[targetPos.toModel()] = source.type(sourcePos);
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
        marksPerChar[targetWhitespacePos.toModel()] = source.type(sourceWhitespacePos);
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

  private List<Markup_V001> assembleMarkupsFromMarkupsPerChar() {
    List<Markup_V001> changemarks = new java.util.ArrayList<>();
    Integer markStart = null;
    MarkupType lastType = null;
    for (int i = 0; i < marksPerChar.length; i++) {
      MarkupType currentType = marksPerChar[i];
      if (!Objects.equals(currentType, lastType)) {
        if (markStart != null) {
          changemarks.add(new Markup_V001(markStart, i - 1, lastType));
          markStart = null;
        }
        if (currentType != null) {
          markStart = i;
        }
        lastType = currentType;
      }
    }
    if (markStart != null) {
      changemarks.add(new Markup_V001(markStart, marksPerChar.length - 1, lastType));
    }
    return changemarks;
  }
}
