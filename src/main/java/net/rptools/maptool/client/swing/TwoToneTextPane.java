/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.ParagraphView;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * Extension to <code>JTextPane</code> that supports 2 tone text.
 *
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author:
 *     azhrei_fje $
 */
public class TwoToneTextPane extends JTextPane {

  /*---------------------------------------------------------------------------------------------
   * Class Variables
   *-------------------------------------------------------------------------------------------*/

  /** The constant used as the attribute name for two tone colors. */
  public static final Object TwoToneColor = new ColorConstants("two-tone-color");

  /** Pattern used to parse text strings for a style */
  private static final Pattern TEXT_PATTERN = Pattern.compile("\\$\\{\\s*(\\w*)\\s*\\}");

  /*---------------------------------------------------------------------------------------------
   * Constructors
   *-------------------------------------------------------------------------------------------*/

  /** Default constructor */
  public TwoToneTextPane() {
    super();
    setEditorKit(new TwoToneStyledEditorKit());
  }

  /**
   * Create a pane for the styled document.
   *
   * @param aDoc The styled document being displayed.
   */
  public TwoToneTextPane(StyledDocument aDoc) {
    super(aDoc);
    setEditorKit(new TwoToneStyledEditorKit());
  }

  /*---------------------------------------------------------------------------------------------
   * Class Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Gets the background color setting from the attribute list.
   *
   * @param a the attribute set
   * @return the color, Color.black as the default
   */
  public static Color getTwoToneColor(AttributeSet a) {
    Color ttc = (Color) a.getAttribute(TwoToneColor);
    return ttc;
  }

  /**
   * Sets the background color.
   *
   * @param a the attribute set
   * @param fg the color
   */
  public static void setTwoToneColor(MutableAttributeSet a, Color fg) {
    a.addAttribute(TwoToneColor, fg);
  }

  /**
   * Not really support for two tone styles, but a convience method to set all of the properties for
   * a single font.
   *
   * @param style Style being modified
   * @param font Font to add to the style.
   */
  public static final void setFont(Style style, Font font) {
    StyleConstants.setFontFamily(style, font.getFamily());
    StyleConstants.setFontSize(style, font.getSize());
    StyleConstants.setBold(style, font.isBold());
    StyleConstants.setItalic(style, font.isItalic());
  }

  /**
   * A convience method to read a font from a style.
   *
   * @param style Style being modified
   * @return The font inside the passed style
   */
  public static String getFontString(Style style) {
    String font = StyleConstants.getFontFamily(style) + "-";
    if (StyleConstants.isBold(style)) font += "BOLD";
    if (StyleConstants.isItalic(style)) font += "ITALIC";
    if (!StyleConstants.isBold(style) && !StyleConstants.isItalic(style)) font += "PLAIN";
    font += "-" + StyleConstants.getFontSize(style);
    return font;
  }

  /**
   * Parse the passed text string for style names and add the styled text to the text pane's
   * document.
   *
   * @param text Text to parse
   * @param pane Pane to modify. The pane also provides the style names
   */
  public static final void parse(String text, JTextPane pane) {
    try {
      Matcher match = TEXT_PATTERN.matcher(text);
      Document doc = pane.getDocument();
      int textStart = 0; // Start of current text area
      Style style = null; // Style for next set of text
      while (match.find()) {

        // Save the current text first
        String styledText = text.substring(textStart, match.start());
        textStart = match.end() + 1;
        if (style != null && styledText != null) {
          doc.insertString(doc.getLength(), styledText, style);
        } // endif

        // Get the next style
        style = pane.getStyle(match.group(1));
        if (style == null) throw new IllegalArgumentException("Unknown style: '" + match.group(1));
      } // endwhile

      // Add the last of the text
      doc.insertString(doc.getLength(), text.substring(textStart), null);
    } catch (BadLocationException e) {
      e.printStackTrace();
      throw new IllegalStateException(
          "This should not happen since I always use the document to "
              + "determine the location to write. It might be due to synchronization problems though");
    }
  }

  /*---------------------------------------------------------------------------------------------
   * TwoToneStyledEditorKit Inner Class
   *------------------------------------------------------------------------------------------*/

  /**
   * Editor kit that provides the two tone view factory.
   *
   * @author jgorrell
   * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author:
   *     azhrei_fje $
   */
  class TwoToneStyledEditorKit extends StyledEditorKit {

    /** The view factory used by the editor kit */
    private ViewFactory defaultViewFactory = new TwoToneStyledViewFactory();

    /** @see javax.swing.text.StyledEditorKit#getViewFactory() */
    public ViewFactory getViewFactory() {
      return defaultViewFactory;
    }
  }

  /*---------------------------------------------------------------------------------------------
   * TwoToneStyledViewFactory Inner Class
   *------------------------------------------------------------------------------------------*/

  /**
   * This factory is the default view factory extended to return a view that paints two tone text.
   *
   * @author jgorrell
   * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author:
   *     azhrei_fje $
   */
  class TwoToneStyledViewFactory implements ViewFactory {

    /** @see javax.swing.text.ViewFactory#create(javax.swing.text.Element) */
    public View create(Element elem) {
      String kind = elem.getName();
      if (kind != null) {
        if (kind.equals(AbstractDocument.ContentElementName)) {
          return new TwoToneLabelView(elem);
        } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
          return new ParagraphView(elem);
        } else if (kind.equals(AbstractDocument.SectionElementName)) {
          return new BoxView(elem, View.Y_AXIS);
        } else if (kind.equals(StyleConstants.ComponentElementName)) {
          return new ComponentView(elem);
        } else if (kind.equals(StyleConstants.IconElementName)) {
          return new IconView(elem);
        } // endif
      } // endif

      // default to text display
      return new TwoToneLabelView(elem);
    }
  }

  /*---------------------------------------------------------------------------------------------
   * TwoToneLabelView Inner Class
   *-------------------------------------------------------------------------------------------*/

  /**
   * Label view that can paint two tone text.
   *
   * @author jgorrell
   * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author:
   *     azhrei_fje $
   */
  public class TwoToneLabelView extends LabelView {

    /** Painter used for two tone text. */
    private GlyphView.GlyphPainter painter = new TwoToneGlyphPainter();

    /**
     * Create a new TwoToneLabelView
     *
     * @param element
     */
    public TwoToneLabelView(Element element) {
      super(element);
      setGlyphPainter(painter);
    }
  }

  /*---------------------------------------------------------------------------------------------
   * TwoToneLabelView Inner Class
   *-------------------------------------------------------------------------------------------*/

  /**
   * Paints a black or white background text offest by a pixel both vertically and horizontally and
   * then paints the normal text. This code is just a copy of <code>javax.swing.text.GlyphPainter1
   * </code> modified to return an extra pixel for the width and height and to do the extra
   * painting.
   *
   * @author jgorrell
   * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author:
   *     azhrei_fje $
   */
  public class TwoToneGlyphPainter extends GlyphView.GlyphPainter {

    /*---------------------------------------------------------------------------------------------
     * Instance Variables
     *-------------------------------------------------------------------------------------------*/

    /** The metrics for the font. */
    FontMetrics metrics;

    /*---------------------------------------------------------------------------------------------
     * Class Variables
     *-------------------------------------------------------------------------------------------*/

    /** The amount that the text is offset horizontally */
    private static final int HORIZONTAL_OFFSET = 1;

    /** The amount that the text is offset vertially */
    private static final int VERTICAL_OFFSET = 0;

    /*---------------------------------------------------------------------------------------------
     * Instance Methods
     *-------------------------------------------------------------------------------------------*/

    /**
     * Fetch a reference to the text that occupies the given range. This is normally used by the
     * GlyphPainter to determine what characters it should render glyphs for.
     *
     * @param v Read the text from this glyph view's document.
     * @param p0 the starting document offset &gt;= 0
     * @param p1 the ending document offset &gt;= p0
     * @return the <code>Segment</code> containing the text
     */
    public Segment getText(GlyphView v, int p0, int p1) {
      Segment text = new Segment();
      try {
        Document doc = v.getDocument();
        doc.getText(p0, p1 - p0, text);
      } catch (BadLocationException bl) {
        throw new IllegalStateException("GlyphView: Stale view: " + bl);
      }
      return text;
    }

    /*---------------------------------------------------------------------------------------------
     * Abstract GlyphPainter Methods
     *-------------------------------------------------------------------------------------------*/

    /**
     * Determine the span the glyphs given a start location (for tab expansion).
     *
     * @see javax.swing.text.GlyphView.GlyphPainter#getSpan(javax.swing.text.GlyphView, int, int,
     *     javax.swing.text.TabExpander, float)
     */
    public float getSpan(GlyphView v, int p0, int p1, TabExpander e, float x) {
      sync(v);
      Segment text = getText(v, p0, p1);
      int width = Utilities.getTabbedTextWidth(text, metrics, (int) x, e, p0);
      // Do not add HORIZONTAL_OFFSET here, it affects the text selection.
      return width;
    }

    /** @see javax.swing.text.GlyphView.GlyphPainter#getHeight(javax.swing.text.GlyphView) */
    public float getHeight(GlyphView v) {
      sync(v);
      return metrics.getHeight() + VERTICAL_OFFSET;
    }

    /**
     * Fetches the ascent above the baseline for the glyphs corresponding to the given range in the
     * model.
     *
     * @see javax.swing.text.GlyphView.GlyphPainter#getAscent(javax.swing.text.GlyphView)
     */
    public float getAscent(GlyphView v) {
      sync(v);
      return metrics.getAscent();
    }

    /**
     * Fetches the descent below the baseline for the glyphs corresponding to the given range in the
     * model.
     *
     * @see javax.swing.text.GlyphView.GlyphPainter#getDescent(javax.swing.text.GlyphView)
     */
    public float getDescent(GlyphView v) {
      sync(v);
      return metrics.getDescent() + VERTICAL_OFFSET;
    }

    /**
     * @see javax.swing.text.GlyphView.GlyphPainter#modelToView(javax.swing.text.GlyphView, int,
     *     javax.swing.text.Position.Bias, java.awt.Shape)
     */
    public Shape modelToView(GlyphView v, int pos, Position.Bias bias, Shape a)
        throws BadLocationException {

      sync(v);
      Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
      int p0 = v.getStartOffset();
      int p1 = v.getEndOffset();
      TabExpander expander = v.getTabExpander();
      Segment text;

      Rectangle r = new Rectangle();
      if (pos == p1) {
        // The caller of this is left to right and borders a right to
        // left view, return our end location.
        r.setBounds(alloc.x + alloc.width, alloc.y, 0, metrics.getHeight() + VERTICAL_OFFSET);
      } else if ((pos >= p0) && (pos <= p1)) {
        // determine range to the left of the position
        text = getText(v, p0, pos);
        int width = Utilities.getTabbedTextWidth(text, metrics, alloc.x, expander, p0);
        r.setBounds(alloc.x + width, alloc.y, 0, metrics.getHeight() + VERTICAL_OFFSET);
      } else {
        throw new BadLocationException("modelToView - can't convert", p1);
      } // endif
      return r;
    }

    /**
     * Provides a mapping from the view coordinate space to the logical coordinate space of the
     * model.
     *
     * @see View#viewToModel(float, float, java.awt.Shape, javax.swing.text.Position.Bias[])
     * @see javax.swing.text.GlyphView.GlyphPainter#viewToModel(javax.swing.text.GlyphView, float,
     *     float, java.awt.Shape, javax.swing.text.Position.Bias[])
     */
    public int viewToModel(GlyphView v, float x, float y, Shape a, Position.Bias[] biasReturn) {

      sync(v);
      Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
      int p0 = v.getStartOffset();
      int p1 = v.getEndOffset();
      TabExpander expander = v.getTabExpander();
      Segment text = getText(v, p0, p1);

      int offs = Utilities.getTabbedTextOffset(text, metrics, alloc.x, (int) x, expander, p0);
      int retValue = p0 + offs;
      if (retValue == p1) {
        // No need to return backward bias as GlyphPainter1 is used for
        // ltr text only.
        retValue--;
      }
      biasReturn[0] = Position.Bias.Forward;
      return retValue;
    }

    /**
     * Determines the best location (in the model) to break the given view. This method attempts to
     * break on a whitespace location. If a whitespace location can't be found, the nearest
     * character location is returned.
     *
     * @see View#breakView
     * @see javax.swing.text.GlyphView.GlyphPainter#getBoundedPosition(javax.swing.text.GlyphView,
     *     int, float, float)
     */
    public int getBoundedPosition(GlyphView v, int p0, float x, float len) {
      sync(v);
      TabExpander expander = v.getTabExpander();
      Segment s = v.getText(p0, v.getEndOffset());
      int index =
          Utilities.getTabbedTextOffset(s, metrics, (int) x, (int) (x + len), expander, p0, false);
      int p1 = p0 + index;
      return p1;
    }

    /**
     * Synchronize this painter with the current state of the view.
     *
     * @param v Sync to this view.
     */
    @SuppressWarnings("deprecation")
    void sync(GlyphView v) {
      Font f = v.getFont();
      if ((metrics == null) || (!f.equals(metrics.getFont()))) {
        // fetch a new FontMetrics
        Toolkit kit;
        Component c = v.getContainer();
        if (c != null) {
          kit = c.getToolkit();
        } else {
          kit = Toolkit.getDefaultToolkit();
        }
        metrics = kit.getFontMetrics(f);
      }
    }

    /**
     * Much of this code is copied from GlyphPainter1's implementation.
     *
     * @see javax.swing.text.GlyphView.GlyphPainter#paint(javax.swing.text.GlyphView,
     *     java.awt.Graphics, java.awt.Shape, int, int)
     */
    public void paint(GlyphView v, Graphics g, Shape a, int p0, int p1) {
      sync(v);
      Segment text;
      TabExpander expander = v.getTabExpander();
      Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();

      // determine the x coordinate to render the glyphs
      int x = alloc.x;
      int p = v.getStartOffset();
      if (p != p0) {
        text = v.getText(p, p0);
        int width = Utilities.getTabbedTextWidth(text, metrics, x, expander, p);
        x += width;
      } // endif

      // determine the y coordinate to render the glyphs
      int y = alloc.y + metrics.getHeight() - metrics.getDescent();

      // Calculate the background highlight, it gets painted first.
      Color bg = TwoToneTextPane.getTwoToneColor(v.getElement().getAttributes());
      Color fg = g.getColor();
      if (bg == null) { // No color set, guess black or white
        float[] hsb = Color.RGBtoHSB(fg.getRed(), fg.getGreen(), fg.getBlue(), null);
        bg = hsb[2] > 0.7 ? Color.BLACK : Color.WHITE;
      } // endif
      g.setColor(bg);

      // render the glyphs, first in bg highlight, then in fg
      text = v.getText(p0, p1);
      g.setFont(metrics.getFont());
      Utilities.drawTabbedText(text, x + HORIZONTAL_OFFSET, y + VERTICAL_OFFSET, g, expander, p0);
      g.setColor(fg);
      Utilities.drawTabbedText(text, x, y, g, expander, p0);
    }
  }

  /*---------------------------------------------------------------------------------------------
   * ColorConstants Inner Class
   *-------------------------------------------------------------------------------------------*/

  /** A attribute name class that implements the proper interfaces */
  public static class ColorConstants
      implements AttributeSet.ColorAttribute, AttributeSet.CharacterAttribute {

    /** Name of the constant */
    String name;

    /**
     * Create a new ColorConstants
     *
     * @param aName The name of the new color constant.
     */
    protected ColorConstants(String aName) {
      name = aName;
    }
  }
}
