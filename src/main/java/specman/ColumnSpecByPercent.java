package specman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static specman.view.AbstractSchrittView.FORMLAYOUT_GAP;

public class ColumnSpecByPercent {
  private static final String TABLELAYOUT_COLSPEC = "pref:grow";

  public static String percents2specs(int columns, List<Integer> percents) {
    String colSpecs = "";
    for (int c = 0; c < columns; c++) {
      String colSpec = percents != null
        ? "pref:grow(" + (float)percents.get(c) / 100 + ")"
        : TABLELAYOUT_COLSPEC;
      if (colSpecs.length() > 0) {
        colSpecs += ",";
      }
      colSpecs += colSpec;
      if (c < columns-1) {
        colSpecs += "," + FORMLAYOUT_GAP;
      }
    }
    return colSpecs;
  }

  public static List<Integer> copyOf(List<Integer> l) {
    return l != null ? new ArrayList<>(l) : l;
  }

  public static List<Integer> releasePercent(int column, List<Integer> percents) {
    if (percents != null) {
      List<Integer> result = copyOf(percents);
      int widthEaterColumn = column == percents.size() - 1 ? column - 1 : column + 1;
      int fromColumnWidth = percents.get(column);
      int eaterWidth = percents.get(widthEaterColumn);
      result.set(widthEaterColumn, eaterWidth + fromColumnWidth);
      result.remove(column);
      return result;
    }
    return null;
  }

  public static List<Integer> allocPercent(int column, List<Integer> percents) {
    if (percents != null) {
      List<Integer> originalPercents = copyOf(percents);
      int stealFromColumn = Math.min(column, percents.size() - 1);
      int fromColumnWidth = percents.get(stealFromColumn);
      int newColumnWidth = fromColumnWidth / 2;
      percents.set(stealFromColumn, newColumnWidth);
      percents.add(column, newColumnWidth);
      return originalPercents;
    }
    return null;
  }

  public static List<Integer> recomputePercents(Integer[] absoluteWidths, int vergroesserung, int spalte) {
    if (absoluteWidths[spalte] + vergroesserung < 0) {
      // Dragging too far to the left is ignored
      return null;
    }
    if (vergroesserung > absoluteWidths[spalte+1]) {
      // Dragging to the right exceeding the following sequence's width is ignored
      return null;
    }
    absoluteWidths[spalte] += vergroesserung;
    absoluteWidths[spalte+1] -= vergroesserung;
    int columnsWidthSum = Arrays.stream(absoluteWidths).mapToInt(cw -> cw).sum();
    return Arrays.stream(absoluteWidths)
      .map(cw -> (int)((float)cw / columnsWidthSum * 100))
      .collect(Collectors.toList());
  }
}
