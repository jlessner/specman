package specman.model.v001;

import javax.swing.text.MutableAttributeSet;

public class ZielschrittMarkierungen_V001 {
    final int von, bis;

    public ZielschrittMarkierungen_V001(int von, int bis, MutableAttributeSet attr){
        this.von = von;
        this.bis = bis;
        this.attr = attr;
    }

    MutableAttributeSet attr;

    public int getVon() {
        return von;
    }

    public int getBis() {
        return bis;
    }

    public int laenge() {
        return bis - von;
    }

    public MutableAttributeSet getAttr(){
        return attr;
    }
}
