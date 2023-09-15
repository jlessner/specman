package specman.textfield;

public class StepnumberLink {
    public final static String STEPNUMBER_DEFECT_MARK = "?";

    public static boolean isStepnumberLinkDefect(String stepnumberLinkID) {
        return stepnumberLinkID.endsWith(STEPNUMBER_DEFECT_MARK);
    }
}