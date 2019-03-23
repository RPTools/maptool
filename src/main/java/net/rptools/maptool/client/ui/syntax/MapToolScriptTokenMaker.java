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
package net.rptools.maptool.client.ui.syntax;

import java.io.*;
import javax.swing.text.Segment;
import org.fife.ui.rsyntaxtextarea.*;

/**
 * Auto-generated source created using TokenMakerMaker https://github.com/bobbylight/TokenMakerMaker
 *
 * <p>NOTE: Run JFlex using this as -skel input then removed extra methods in generated output
 */
public class MapToolScriptTokenMaker extends AbstractJFlexCTokenMaker {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;

  public static final int MLC = 1;

  /** Translates characters to character classes */
  private static final String ZZ_CMAP_PACKED =
      "\11\0\1\22\1\10\1\0\1\22\1\17\22\0\1\22\1\24\1\15"
          + "\1\20\1\1\1\41\1\41\1\7\2\42\1\41\1\31\1\41\1\25"
          + "\1\21\1\43\1\4\3\16\4\6\2\3\1\47\1\41\1\23\1\41"
          + "\1\26\1\41\1\20\1\35\1\14\2\5\1\30\1\34\1\1\1\44"
          + "\1\46\2\1\1\36\1\1\1\13\1\1\1\45\1\1\1\33\1\37"
          + "\1\32\1\12\1\1\1\50\1\27\2\1\1\42\1\11\1\42\1\17"
          + "\1\2\1\0\1\35\1\14\2\5\1\30\1\34\1\1\1\44\1\46"
          + "\2\1\1\36\1\1\1\13\1\1\1\45\1\1\1\33\1\37\1\32"
          + "\1\12\1\1\1\50\1\27\2\1\1\40\1\17\1\40\1\41\uff81\0";

  /** Translates characters to character classes */
  private static final char[] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** Translates DFA states to action switch labels. */
  private static final int[] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
      "\2\0\2\1\2\2\1\3\1\4\1\5\1\1\1\6"
          + "\3\1\1\7\1\10\1\11\4\10\1\0\1\12\1\0"
          + "\2\12\1\3\1\13\1\0\1\3\2\5\1\14\1\15"
          + "\1\0\2\1\6\0\1\15\1\0\1\16\1\3\1\17"
          + "\2\3\1\13\1\3\1\5\1\20\1\5\1\0\2\1"
          + "\1\21\5\0\1\3\1\5\1\22\1\23\2\0\1\24"
          + "\1\0\1\3\1\5\3\0\1\3\1\5";

  private static int[] zzUnpackAction() {
    int[] result = new int[79];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int[] result) {
    int i = 0; /* index in packed string */
    int j = offset; /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value;
      while (--count > 0);
    }
    return j;
  }

  /** Translates a state to a row index in the transition table */
  private static final int[] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
      "\0\0\0\51\0\122\0\173\0\244\0\315\0\366\0\122"
          + "\0\u011f\0\u0148\0\u0171\0\u019a\0\u01c3\0\u01ec\0\122\0\u0215"
          + "\0\122\0\u023e\0\u0267\0\u0290\0\u02b9\0\u02e2\0\u030b\0\u0148"
          + "\0\u0334\0\u035d\0\u0386\0\122\0\u03af\0\u03d8\0\u0401\0\u042a"
          + "\0\122\0\u0453\0\u047c\0\u04a5\0\u04ce\0\u04f7\0\u0520\0\u0549"
          + "\0\u0572\0\u059b\0\u05c4\0\u05ed\0\u0616\0\u035d\0\u063f\0\122"
          + "\0\u0668\0\u0691\0\u03af\0\u06ba\0\u06e3\0\122\0\u070c\0\u0735"
          + "\0\u075e\0\u0787\0\122\0\u07b0\0\u07d9\0\u0802\0\u082b\0\u0854"
          + "\0\u087d\0\u08a6\0\122\0\173\0\u08cf\0\u08f8\0\u0921\0\u094a"
          + "\0\u0973\0\u099c\0\u09c5\0\u0921\0\u09ee\0\u0a17\0\u0a40";

  private static int[] zzUnpackRowMap() {
    int[] result = new int[79];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int[] result) {
    int i = 0; /* index in packed string */
    int j = offset; /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** The transition table of the DFA */
  private static final int[] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
      "\1\3\2\4\1\5\1\6\1\4\1\5\1\7\1\10"
          + "\1\3\3\4\1\11\1\5\2\3\1\12\1\13\1\14"
          + "\3\3\2\4\1\3\1\15\1\4\1\16\3\4\1\17"
          + "\1\3\1\17\1\3\3\4\1\3\1\4\10\20\1\21"
          + "\14\20\1\22\6\20\1\23\7\20\1\24\3\20\1\25"
          + "\52\0\6\4\2\0\1\26\3\4\1\0\1\4\2\0"
          + "\1\4\5\0\2\4\1\0\6\4\4\0\3\4\1\0"
          + "\1\4\3\27\2\5\1\27\1\5\2\0\4\27\1\0"
          + "\1\5\1\0\1\27\1\30\5\0\1\27\1\31\1\0"
          + "\6\27\4\0\3\27\1\0\4\27\2\5\1\27\1\5"
          + "\2\0\4\27\1\0\1\5\1\0\1\27\1\30\5\0"
          + "\1\32\1\31\1\0\6\27\4\0\3\27\1\0\1\27"
          + "\7\33\1\34\1\35\1\36\37\33\10\11\1\37\1\40"
          + "\3\11\1\41\33\11\3\0\2\42\1\0\1\42\7\0"
          + "\1\42\54\0\1\13\52\0\1\43\25\0\6\4\2\0"
          + "\1\26\3\4\1\0\1\4\2\0\1\4\5\0\2\4"
          + "\1\0\1\4\1\44\4\4\4\0\3\4\1\0\1\4"
          + "\1\0\6\4\2\0\1\26\3\4\1\0\1\4\2\0"
          + "\1\4\5\0\2\4\1\0\3\4\1\45\2\4\4\0"
          + "\3\4\1\0\1\4\10\20\1\0\14\20\1\0\6\20"
          + "\1\0\7\20\1\0\3\20\26\0\1\46\55\0\1\47"
          + "\13\0\1\50\34\0\1\51\66\0\1\52\12\0\1\53"
          + "\36\0\7\27\2\0\4\27\1\0\1\27\1\0\1\27"
          + "\6\0\2\27\1\0\6\27\4\0\3\27\1\0\4\27"
          + "\2\54\1\27\1\54\2\0\4\27\1\0\1\54\1\0"
          + "\1\27\4\0\1\55\1\0\2\27\1\55\6\27\4\0"
          + "\3\27\1\0\4\27\4\56\2\0\3\27\1\56\1\0"
          + "\1\56\1\0\1\27\6\0\1\27\1\56\1\0\2\27"
          + "\2\56\2\27\4\0\3\27\1\0\1\27\7\57\1\60"
          + "\1\0\40\57\7\0\1\60\41\0\4\57\1\61\1\57"
          + "\1\62\1\63\1\0\1\33\1\64\3\33\1\61\13\57"
          + "\3\33\14\57\11\37\1\65\3\37\1\66\37\37\1\11"
          + "\1\37\2\11\1\0\1\11\1\67\4\11\13\37\3\11"
          + "\14\37\3\27\2\42\1\27\1\42\2\0\4\27\1\0"
          + "\1\42\1\0\1\27\6\0\1\27\1\31\1\0\6\27"
          + "\4\0\3\27\1\0\1\27\25\0\1\70\24\0\6\4"
          + "\2\0\1\26\1\71\2\4\1\0\1\4\2\0\1\4"
          + "\5\0\2\4\1\0\6\4\4\0\3\4\1\0\1\4"
          + "\1\0\6\4\2\0\1\26\3\4\1\0\1\4\2\0"
          + "\1\4\5\0\2\4\1\0\4\4\1\72\1\4\4\0"
          + "\3\4\1\0\1\4\26\0\1\73\67\0\1\74\41\0"
          + "\1\75\44\0\1\76\66\0\1\77\3\0\4\100\5\0"
          + "\1\100\1\0\1\100\11\0\1\100\3\0\2\100\13\0"
          + "\3\27\2\54\1\27\1\54\2\0\4\27\1\0\1\54"
          + "\1\0\1\27\6\0\2\27\1\0\6\27\4\0\3\27"
          + "\1\0\1\27\3\0\2\54\1\0\1\54\7\0\1\54"
          + "\32\0\7\57\1\34\1\0\44\57\1\62\1\57\1\62"
          + "\1\60\1\0\5\57\1\62\36\57\1\33\1\57\1\33"
          + "\1\60\1\0\5\57\1\33\35\57\4\101\1\34\1\0"
          + "\3\57\1\101\1\57\1\101\11\57\1\101\3\57\2\101"
          + "\13\57\10\37\1\0\43\37\4\102\2\37\1\65\2\37"
          + "\1\102\1\66\1\102\11\37\1\102\3\37\2\102\13\37"
          + "\25\0\1\103\24\0\6\4\2\0\1\26\3\4\1\0"
          + "\1\4\2\0\1\4\5\0\1\4\1\104\1\0\6\4"
          + "\4\0\3\4\1\0\1\4\1\0\6\4\2\0\1\26"
          + "\3\4\1\0\1\4\2\0\1\4\5\0\2\4\1\0"
          + "\5\4\1\71\4\0\3\4\1\0\1\4\47\0\1\105"
          + "\31\0\1\74\65\0\1\106\24\0\1\107\32\0\4\110"
          + "\5\0\1\110\1\0\1\110\11\0\1\110\3\0\2\110"
          + "\13\0\3\57\4\111\1\34\1\0\3\57\1\111\1\57"
          + "\1\111\11\57\1\111\3\57\2\111\13\57\3\37\4\112"
          + "\2\37\1\65\2\37\1\112\1\66\1\112\11\37\1\112"
          + "\3\37\2\112\13\37\43\0\1\113\44\0\1\74\7\0"
          + "\1\105\2\0\1\107\1\114\4\107\1\114\2\0\3\107"
          + "\1\0\1\107\1\0\2\114\2\0\2\114\1\0\2\107"
          + "\1\114\6\107\1\0\2\114\4\107\1\114\1\107\3\0"
          + "\4\115\5\0\1\115\1\0\1\115\11\0\1\115\3\0"
          + "\2\115\13\0\3\57\4\116\1\34\1\0\3\57\1\116"
          + "\1\57\1\116\11\57\1\116\3\57\2\116\13\57\3\37"
          + "\4\117\2\37\1\65\2\37\1\117\1\66\1\117\11\37"
          + "\1\117\3\37\2\117\13\37\43\0\1\107\10\0\4\4"
          + "\5\0\1\4\1\0\1\4\11\0\1\4\3\0\2\4"
          + "\13\0\3\57\4\33\1\34\1\0\3\57\1\33\1\57"
          + "\1\33\11\57\1\33\3\57\2\33\13\57\3\37\4\11"
          + "\2\37\1\65\2\37\1\11\1\66\1\11\11\37\1\11"
          + "\3\37\2\11\13\37";

  private static int[] zzUnpackTrans() {
    int[] result = new int[2665];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int[] result) {
    int i = 0; /* index in packed string */
    int j = offset; /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value;
      while (--count > 0);
    }
    return j;
  }

  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /** ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code> */
  private static final int[] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
      "\2\0\1\11\4\1\1\11\6\1\1\11\1\1\1\11"
          + "\4\1\1\0\1\1\1\0\3\1\1\11\1\0\3\1"
          + "\1\11\1\1\1\0\2\1\6\0\1\1\1\0\2\1"
          + "\1\11\5\1\1\11\1\1\1\0\2\1\1\11\5\0"
          + "\2\1\1\11\1\1\2\0\1\1\1\0\2\1\3\0"
          + "\2\1";

  private static int[] zzUnpackAttribute() {
    int[] result = new int[79];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int[] result) {
    int i = 0; /* index in packed string */
    int j = offset; /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value;
      while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /**
   * this buffer contains the current text to be matched and is the source of the yytext() string
   */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the textposition at the last state to be included in yytext */
  private int zzPushbackPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /** the number of characters from the last newline up to the start of the matched text */
  private int yycolumn;

  /** zzAtBOL == true <=> the scanner is currently at the beginning of a line */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /* user code: */

  /** Constructor. This must be here because JFlex does not generate a no-parameter constructor. */
  public MapToolScriptTokenMaker() {}

  /**
   * Adds the token specified to the current linked list of tokens.
   *
   * @param tokenType The token's type.
   * @see #addToken(int, int, int)
   */
  private void addHyperlinkToken(int start, int end, int tokenType) {
    int so = start + offsetShift;
    addToken(zzBuffer, start, end, tokenType, so, true);
  }

  /**
   * Adds the token specified to the current linked list of tokens.
   *
   * @param tokenType The token's type.
   */
  private void addToken(int tokenType) {
    addToken(zzStartRead, zzMarkedPos - 1, tokenType);
  }

  /**
   * Adds the token specified to the current linked list of tokens.
   *
   * @param tokenType The token's type.
   * @see #addHyperlinkToken(int, int, int)
   */
  private void addToken(int start, int end, int tokenType) {
    int so = start + offsetShift;
    addToken(zzBuffer, start, end, tokenType, so, false);
  }

  /**
   * Adds the token specified to the current linked list of tokens.
   *
   * @param array The character array.
   * @param start The starting offset in the array.
   * @param end The ending offset in the array.
   * @param tokenType The token's type.
   * @param startOffset The offset in the document at which this token occurs.
   * @param hyperlink Whether this token is a hyperlink.
   */
  public void addToken(
      char[] array, int start, int end, int tokenType, int startOffset, boolean hyperlink) {
    super.addToken(array, start, end, tokenType, startOffset, hyperlink);
    zzStartRead = zzMarkedPos;
  }

  /** {@inheritDoc} */
  public String[] getLineCommentStartAndEnd(int languageIndex) {
    return null;
  }

  /**
   * Returns the first token in the linked list of tokens generated from <code>text</code>. This
   * method must be implemented by subclasses so they can correctly implement syntax highlighting.
   *
   * @param text The text from which to get tokens.
   * @param initialTokenType The token type we should start with.
   * @param startOffset The offset into the document at which <code>text</code> starts.
   * @return The first <code>Token</code> in a linked list representing the syntax highlighted text.
   */
  public Token getTokenList(Segment text, int initialTokenType, int startOffset) {

    resetTokenList();
    this.offsetShift = -text.offset + startOffset;

    // Start off in the proper state.
    int state = Token.NULL;
    switch (initialTokenType) {
      case Token.COMMENT_MULTILINE:
        state = MLC;
        start = text.offset;
        break;

        /* No documentation comments */
      default:
        state = Token.NULL;
    }

    s = text;
    try {
      yyreset(zzReader);
      yybegin(state);
      return yylex();
    } catch (IOException ioe) {
      ioe.printStackTrace();
      return new TokenImpl();
    }
  }

  /**
   * Refills the input buffer.
   *
   * @return <code>true</code> if EOF was reached, otherwise <code>false</code>.
   *     <p>NOTE: Keep this method version and remove the other zzRefill() after JFlex generation.
   *     -Jamz
   */
  private boolean zzRefill() {
    return zzCurrentPos >= s.offset + s.count;
  }

  /**
   * Resets the scanner to read from a new input stream. Does not close the old reader.
   *
   * <p>All internal variables are reset, the old input stream <b>cannot</b> be reused (internal
   * buffer is discarded and lost). Lexical state is set to <tt>YY_INITIAL</tt>.
   *
   * @param reader the new input stream
   *     <p>NOTE: Keep this method version and remove the other yyreset(Reader reader) after JFlex
   *     generation. -Jamz
   */
  public final void yyreset(Reader reader) {
    // 's' has been updated.
    zzBuffer = s.array;
    /*
     * We replaced the line below with the two below it because zzRefill no longer "refills" the buffer (since the way we do it, it's always "full" the first time through, since it points to the
     * segment's array). So, we assign zzEndRead here.
     */
    // zzStartRead = zzEndRead = s.offset;
    zzStartRead = s.offset;
    zzEndRead = zzStartRead + s.count - 1;
    zzCurrentPos = zzMarkedPos = zzPushbackPos = s.offset;
    zzLexicalState = YYINITIAL;
    zzReader = reader;
    zzAtBOL = true;
    zzAtEOF = false;
  }

  /**
   * Creates a new scanner There is also a java.io.InputStream version of this constructor.
   *
   * @param in the java.io.Reader to read input from.
   */
  public MapToolScriptTokenMaker(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner. There is also java.io.Reader version of this constructor.
   *
   * @param in the java.io.Inputstream to read input from.
   */
  public MapToolScriptTokenMaker(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /**
   * Unpacks the compressed character translation table.
   *
   * @param packed the packed character translation table
   * @return the unpacked character translation table
   */
  private static char[] zzUnpackCMap(String packed) {
    char[] map = new char[0x10000];
    int i = 0; /* index in packed string */
    int j = 0; /* index in unpacked array */
    while (i < 180) {
      int count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value;
      while (--count > 0);
    }
    return map;
  }

  /** Closes the input stream. */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true; /* indicate end of file */
    zzEndRead = zzStartRead; /* invalidate buffer */

    if (zzReader != null) zzReader.close();
  }

  /** Returns the current lexical state. */
  public final int yystate() {
    return zzLexicalState;
  }

  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }

  /** Returns the text matched by the current regular expression. */
  public final String yytext() {
    return new String(zzBuffer, zzStartRead, zzMarkedPos - zzStartRead);
  }

  /**
   * Returns the character at position <tt>pos</tt> from the matched text.
   *
   * <p>It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. A value from 0 to yylength()-1.
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead + pos];
  }

  /** Returns the length of the matched text region. */
  public final int yylength() {
    return zzMarkedPos - zzStartRead;
  }

  /**
   * Reports an error that occured while scanning.
   *
   * <p>In a wellformed scanner (no or only correct usage of yypushback(int) and a match-all
   * fallback rule) this method will only be called with things that "Can't Possibly Happen". If
   * this method is called, something is seriously wrong (e.g. a JFlex bug producing a faulty
   * scanner etc.).
   *
   * <p>Usual syntax/scanner level error handling should be done in error fallback rules.
   *
   * @param errorCode the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    } catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }

  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * <p>They will be read again by then next call of the scanning method
   *
   * @param number the number of characters to be read again. This number must not be greater than
   *     yylength()!
   */
  public void yypushback(int number) {
    if (number > yylength()) zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }

  /**
   * Resumes scanning until the next regular expression is matched, the end of input is encountered
   * or an I/O-Error occurs.
   *
   * @return the next token
   * @exception java.io.IOException if any I/O-Error occurs
   */
  public org.fife.ui.rsyntaxtextarea.Token yylex() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char[] zzBufferL = zzBuffer;
    char[] zzCMapL = ZZ_CMAP;

    int[] zzTransL = ZZ_TRANS;
    int[] zzRowMapL = ZZ_ROWMAP;
    int[] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = zzLexicalState;

      zzForAction:
      {
        while (true) {

          if (zzCurrentPosL < zzEndReadL) zzInput = zzBufferL[zzCurrentPosL++];
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          } else {
            // store back cached positions
            zzCurrentPos = zzCurrentPosL;
            zzMarkedPos = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL = zzCurrentPos;
            zzMarkedPosL = zzMarkedPos;
            zzBufferL = zzBuffer;
            zzEndReadL = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            } else {
              zzInput = zzBufferL[zzCurrentPosL++];
            }
          }
          int zzNext = zzTransL[zzRowMapL[zzState] + zzCMapL[zzInput]];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ((zzAttributes & 1) == 1) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ((zzAttributes & 8) == 8) break zzForAction;
          }
        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 4:
          {
            addNullToken();
            return firstToken;
          }
        case 21:
          break;
        case 15:
          {
            addToken(Token.LITERAL_CHAR);
          }
        case 22:
          break;
        case 6:
          {
            addToken(Token.WHITESPACE);
          }
        case 23:
          break;
        case 14:
          {
            addToken(Token.LITERAL_NUMBER_HEXADECIMAL);
          }
        case 24:
          break;
        case 16:
          {
            addToken(Token.ERROR_STRING_DOUBLE);
          }
        case 25:
          break;
        case 13:
          {
            addToken(Token.LITERAL_NUMBER_FLOAT);
          }
        case 26:
          break;
        case 18:
          {
            start = zzMarkedPos - 4;
            yybegin(MLC);
          }
        case 27:
          break;
        case 7:
          {
            addToken(Token.SEPARATOR);
          }
        case 28:
          break;
        case 1:
          {
            addToken(Token.IDENTIFIER);
          }
        case 29:
          break;
        case 3:
          {
            addToken(Token.ERROR_CHAR);
            addNullToken();
            return firstToken;
          }
        case 30:
          break;
        case 5:
          {
            addToken(Token.ERROR_STRING_DOUBLE);
            addNullToken();
            return firstToken;
          }
        case 31:
          break;
        case 11:
          {
            addToken(Token.ERROR_CHAR);
          }
        case 32:
          break;
        case 19:
          {
            addToken(Token.LITERAL_BOOLEAN);
          }
        case 33:
          break;
        case 12:
          {
            addToken(Token.LITERAL_STRING_DOUBLE_QUOTE);
          }
        case 34:
          break;
        case 20:
          {
            int temp = zzStartRead;
            addToken(start, zzStartRead - 1, Token.COMMENT_MULTILINE);
            addHyperlinkToken(temp, zzMarkedPos - 1, Token.COMMENT_MULTILINE);
            start = zzMarkedPos;
          }
        case 35:
          break;
        case 10:
          {
            addToken(Token.ERROR_NUMBER_FORMAT);
          }
        case 36:
          break;
        case 17:
          {
            yybegin(YYINITIAL);
            addToken(start, zzStartRead + 3 - 1, Token.COMMENT_MULTILINE);
          }
        case 37:
          break;
        case 2:
          {
            addToken(Token.LITERAL_NUMBER_DECIMAL_INT);
          }
        case 38:
          break;
        case 8:
          {
          }
        case 39:
          break;
        case 9:
          {
            addToken(start, zzStartRead - 1, Token.COMMENT_MULTILINE);
            return firstToken;
          }
        case 40:
          break;
        default:
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
            switch (zzLexicalState) {
              case YYINITIAL:
                {
                  addNullToken();
                  return firstToken;
                }
              case 80:
                break;
              case MLC:
                {
                  addToken(start, zzStartRead - 1, Token.COMMENT_MULTILINE);
                  return firstToken;
                }
              case 81:
                break;
              default:
                return null;
            }
          } else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }
}
