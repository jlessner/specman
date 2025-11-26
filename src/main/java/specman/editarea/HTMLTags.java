package specman.editarea;

import javax.swing.text.html.HTML;

public interface HTMLTags {
  String HTML_INTRO = toIntro(HTML.Tag.HTML);
  String HTML_OUTRO = toOutro(HTML.Tag.HTML);
  String HEAD_INTRO = toIntro(HTML.Tag.HEAD);
  String HEAD_OUTRO = toOutro(HTML.Tag.HEAD);
  String BODY_INTRO = toIntro(HTML.Tag.BODY);
  String BODY_OUTRO = toOutro(HTML.Tag.BODY);
  String SPAN_INTRO = toIntro(HTML.Tag.SPAN);
  String SPAN_OUTRO = toOutro(HTML.Tag.SPAN);
  String PRE_INTRO = toIntro(HTML.Tag.PRE);
  String PRE_OUTRO = toOutro(HTML.Tag.PRE);

  static String toIntro(HTML.Tag tag) {
    return "<" + tag.toString().toLowerCase() + ">";
  }

  static String toOutro(HTML.Tag tag) {
    return "</" + tag.toString().toLowerCase() + ">";
  }

}
