package specman.textfield;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import specman.Specman;
import specman.view.RoundedBorderDecorationStyle;

import static specman.view.RoundedBorderDecorationStyle.None;

public class Indentions {
    public static final int JEDITORPANE_DEFAULT_BORDER_THICKNESS = 3;
    private static final int LEFTRIGHT_INSET_FOR_DECORATION = 10;
    private static final int TOPBOTTOM_INSET_FOR_DECORATION = 1;

    
//    public static final int JEDITORPANE_DEFAULT_BORDER_THICKNESS = 1;
//    private static final int LEFTRIGHT_INSET_FOR_DECORATION = 20;
//    private static final int TOPBOTTOM_INSET_FOR_DECORATION = 30;
    
    
    
    final boolean top, left, bottom, right;

    public Indentions() { this(None); }

    public Indentions(RoundedBorderDecorationStyle style) {
        top = left = bottom = right = (style != None);
    }

    public Indentions(boolean top, boolean left, boolean bottom, boolean right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    private RowSpec toRowSpec(boolean indent) {
        int px = indent ? (TOPBOTTOM_INSET_FOR_DECORATION * zoomPercent() / 100) : 0;
        return RowSpec.decode(px + "px");
    }

    private ColumnSpec toColumnSpec(boolean indent) {
        int px = indent ? (LEFTRIGHT_INSET_FOR_DECORATION * zoomPercent() / 100) : 0;
        return ColumnSpec.decode(px + "px");
    }

    public RowSpec topInset() { return toRowSpec(top); }
    public RowSpec bottomInset() { return toRowSpec(bottom); }
    public ColumnSpec leftInset() { return toColumnSpec(left); }
    public ColumnSpec rightInset() { return toColumnSpec(right); }

    public int leftBorder() { return left ? 0 : JEDITORPANE_DEFAULT_BORDER_THICKNESS; }
    public int rightBorder() { return right ? 0 : JEDITORPANE_DEFAULT_BORDER_THICKNESS; }
    public int topBorder() {
        return JEDITORPANE_DEFAULT_BORDER_THICKNESS - (top ? TOPBOTTOM_INSET_FOR_DECORATION : 0);
    }
    public int bottomBorder() {
        return JEDITORPANE_DEFAULT_BORDER_THICKNESS - (bottom ? TOPBOTTOM_INSET_FOR_DECORATION : 0);
    }

    public Indentions withTop(boolean top) { return new Indentions(top, left, bottom, right); }
    public Indentions withLeft(boolean left) { return new Indentions(top, left, bottom, right); }
    public Indentions withBottom(boolean bottom) { return new Indentions(top, left, bottom, right); }
    public Indentions withRight(boolean right) { return new Indentions(top, left, bottom, right); }

    private int zoomPercent() { return Specman.instance().getZoomFactor(); }
}
