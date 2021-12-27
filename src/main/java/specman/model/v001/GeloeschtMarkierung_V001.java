package specman.model.v001;


public class GeloeschtMarkierung_V001 {
    final int von, bis;

    public GeloeschtMarkierung_V001(int von, int bis) {
        this.von = von;
        this.bis = bis;
    }

    public int getVon() {
        return von;
    }

    public int getBis() {
        return bis;
    }

    public int laenge() {
        return bis - von;
    }
}
