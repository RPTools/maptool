/*
 * Generated on 9/4/17 2:19 PM
 */
package net.rptools.maptool.client.ui.syntax;

import java.io.*;
import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.*;


/**
 * Auto-generated source created using TokenMakerMaker
https://github.com/bobbylight/TokenMakerMaker
 */
%%

%public
%class MapToolScriptTokenMaker
%extends AbstractJFlexCTokenMaker
%unicode
%ignorecase
%type org.fife.ui.rsyntaxtextarea.Token


%{


	/**
	 * Constructor.  This must be here because JFlex does not generate a
	 * no-parameter constructor.
	 */
	public MapToolScriptTokenMaker() {
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 * @see #addToken(int, int, int)
	 */
	private void addHyperlinkToken(int start, int end, int tokenType) {
		int so = start + offsetShift;
		addToken(zzBuffer, start,end, tokenType, so, true);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 */
	private void addToken(int tokenType) {
		addToken(zzStartRead, zzMarkedPos-1, tokenType);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 * @see #addHyperlinkToken(int, int, int)
	 */
	private void addToken(int start, int end, int tokenType) {
		int so = start + offsetShift;
		addToken(zzBuffer, start,end, tokenType, so, false);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param array The character array.
	 * @param start The starting offset in the array.
	 * @param end The ending offset in the array.
	 * @param tokenType The token's type.
	 * @param startOffset The offset in the document at which this token
	 *        occurs.
	 * @param hyperlink Whether this token is a hyperlink.
	 */
	public void addToken(char[] array, int start, int end, int tokenType,
						int startOffset, boolean hyperlink) {
		super.addToken(array, start,end, tokenType, startOffset, hyperlink);
		zzStartRead = zzMarkedPos;
	}


	/**
	 * {@inheritDoc}
	 */
	public String[] getLineCommentStartAndEnd(int languageIndex) {
		return null;
	}


	/**
	 * Returns the first token in the linked list of tokens generated
	 * from <code>text</code>.  This method must be implemented by
	 * subclasses so they can correctly implement syntax highlighting.
	 *
	 * @param text The text from which to get tokens.
	 * @param initialTokenType The token type we should start with.
	 * @param startOffset The offset into the document at which
	 *        <code>text</code> starts.
	 * @return The first <code>Token</code> in a linked list representing
	 *         the syntax highlighted text.
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
	 * @return      <code>true</code> if EOF was reached, otherwise
	 *              <code>false</code>.
	 */
	private boolean zzRefill() {
		return zzCurrentPos>=s.offset+s.count;
	}


	/**
	 * Resets the scanner to read from a new input stream.
	 * Does not close the old reader.
	 *
	 * All internal variables are reset, the old input stream 
	 * <b>cannot</b> be reused (internal buffer is discarded and lost).
	 * Lexical state is set to <tt>YY_INITIAL</tt>.
	 *
	 * @param reader   the new input stream 
	 */
	public final void yyreset(Reader reader) {
		// 's' has been updated.
		zzBuffer = s.array;
		/*
		 * We replaced the line below with the two below it because zzRefill
		 * no longer "refills" the buffer (since the way we do it, it's always
		 * "full" the first time through, since it points to the segment's
		 * array).  So, we assign zzEndRead here.
		 */
		//zzStartRead = zzEndRead = s.offset;
		zzStartRead = s.offset;
		zzEndRead = zzStartRead + s.count - 1;
		zzCurrentPos = zzMarkedPos = zzPushbackPos = s.offset;
		zzLexicalState = YYINITIAL;
		zzReader = reader;
		zzAtBOL  = true;
		zzAtEOF  = false;
	}


%}

Letter							= [A-Za-z]
LetterOrUnderscore				= ({Letter}|"_")
NonzeroDigit						= [1-9]
Digit							= ("0"|{NonzeroDigit})
HexDigit							= ({Digit}|[A-Fa-f])
OctalDigit						= ([0-7])
AnyCharacterButApostropheOrBackSlash	= ([^\\'])
AnyCharacterButDoubleQuoteOrBackSlash	= ([^\\\"\n])
EscapedSourceCharacter				= ("u"{HexDigit}{HexDigit}{HexDigit}{HexDigit})
Escape							= ("\\"(([btnfr\"'\\])|([0123]{OctalDigit}?{OctalDigit}?)|({OctalDigit}{OctalDigit}?)|{EscapedSourceCharacter}))
NonSeparator						= ([^\t\f\r\n\ \(\)\{\}\[\]\;\,\.\=\>\<\!\~\?\:\+\-\*\/\&\|\^\%\"\']|"#"|"\\")
IdentifierStart					= ({LetterOrUnderscore}|"$")
IdentifierPart						= ({IdentifierStart}|{Digit}|("\\"{EscapedSourceCharacter}))

LineTerminator				= (\n)
WhiteSpace				= ([ \t\f]+)

CharLiteral	= ([\']({AnyCharacterButApostropheOrBackSlash}|{Escape})[\'])
UnclosedCharLiteral			= ([\'][^\'\n]*)
ErrorCharLiteral			= ({UnclosedCharLiteral}[\'])
StringLiteral				= ([\"]({AnyCharacterButDoubleQuoteOrBackSlash}|{Escape})*[\"])
UnclosedStringLiteral		= ([\"]([\\].|[^\\\"])*[^\"]?)
ErrorStringLiteral			= ({UnclosedStringLiteral}[\"])

MLCBegin					= "<!--"
MLCEnd					= "-->"

/* No documentation comments */
/* No line comments */

IntegerLiteral			= ({Digit}+)
HexLiteral			= (0x{HexDigit}+)
FloatLiteral			= (({Digit}+)("."{Digit}+)?(e[+-]?{Digit}+)? | ({Digit}+)?("."{Digit}+)(e[+-]?{Digit}+)?)
ErrorNumberFormat			= (({IntegerLiteral}|{HexLiteral}|{FloatLiteral}){NonSeparator}+)
BooleanLiteral				= ("true"|"false")

Separator					= ([\(\)\{\}\[\]])
Separator2				= ([\;,.])

Identifier				= ({IdentifierStart}{IdentifierPart}*)

URLGenDelim				= ([:\/\?#\[\]@])
URLSubDelim				= ([\!\$&'\(\)\*\+,;=])
URLUnreserved			= ({LetterOrUnderscore}|{Digit}|[\-\.\~])
URLCharacter			= ({URLGenDelim}|{URLSubDelim}|{URLUnreserved}|[%])
URLCharacters			= ({URLCharacter}*)
URLEndCharacter			= ([\/\$]|{Letter}|{Digit})
URL						= (((https?|f(tp|ile))"://"|"www.")({URLCharacters}{URLEndCharacter})?)


/* No string state */
/* No char state */
%state MLC
/* No documentation comment state */
/* No line comment state */

%%

<YYINITIAL> {

	/* Keywords */
	"c" |
"code" |
"count" |
"dialog" |
"e" |
"expanded" |
"for" |
"foreach" |
"frame" |
"g" |
"gm" |
"gmtt" |
"gt" |
"h" |
"hidden" |
"hide" |
"if" |
"macro" |
"r" |
"result" |
"s" |
"self" |
"selftt" |
"st" |
"switch" |
"t" |
"token" |
"tooltip" |
"u" |
"unformatted" |
"w" |
"while" |
"whisper"		{ addToken(Token.RESERVED_WORD); }

	/* Keywords 2 (just an optional set of keywords colored differently) */
	"onCampaignLoad" |
"onChangeSelection" |
"onMouseOverEvent" |
"onMultipleTokensMove" |
"onTokenMove"		{ addToken(Token.RESERVED_WORD_2); }

	/* Data types */
	"bar.name" |
"macro.args" |
"macro.return" |
"roll.count" |
"roll.result" |
"state.name" |
"token.gm name" |
"token.halo" |
"token.init" |
"token.initHold" |
"token.label" |
"token.name" |
"token.visible" |
"tokens.denyMove" |
"tokens.moveCount"		{ addToken(Token.DATA_TYPE); }

	/* Functions */
	"abort" |
"abs" |
"absolutevalue" |
"add" |
"addAllNPCsToInitiative" |
"addAllPCsToInitiative" |
"addAllToInitiative" |
"addTableEntry" |
"addToInitiative" |
"arg" |
"argCount" |
"assert" |
"average" |
"avg" |
"band" |
"bitwiseand" |
"bitwisenot" |
"bitwiseor" |
"bitwisexor" |
"bnot" |
"bor" |
"bringDrawingToFront" |
"bringToFront" |
"broadcast" |
"bxor" |
"canSeeToken" |
"ceil" |
"ceiling" |
"clearLights" |
"clearTable" |
"closeDialog" |
"closeFrame" |
"concat" |
"copyMap" |
"copyTable" |
"copyToken" |
"countStrProp" |
"countsuccess" |
"createMacro" |
"createTable" |
"currentToken" |
"decode" |
"defineFunction" |
"deleteStrProp" |
"deleteTable" |
"deleteTableEntry" |
"deselectTokens" |
"dice" |
"divide" |
"drawVBL" |
"drop" |
"encode" |
"endsWith" |
"eraseVBL" |
"eval" |
"evalMacro" |
"execLink" |
"execMacro" |
"explode" |
"explodingSuccess" |
"exportData" |
"exposeAllOwnedArea" |
"exposeFOW" |
"exposeFogAtWaypoints" |
"exposePCOnlyArea" |
"findToken" |
"floor" |
"formatStrProp" |
"fudge" |
"getAllMapNames" |
"getAllPlayerNames" |
"getAllPropertyNames" |
"getAlwaysVisible" |
"getBar" |
"getCurrentInitiative" |
"getCurrentMapName" |
"getDistance" |
"getDistanceToXY" |
"getDrawingLayer" |
"getEnvironmentVariable" |
"getExposedTokenNames" |
"getExposedTokens" |
"getFindCount" |
"getGMName" |
"getGMNotes" |
"getGroup" |
"getGroupCount" |
"getGroupEnd" |
"getGroupStart" |
"getHalo" |
"getImage" |
"getImpersonated" |
"getImpersonatedName" |
"getInfo" |
"getInitiative" |
"getInitiativeHold" |
"getInitiativeList" |
"getInitiativeRound" |
"getInitiativeToken" |
"getLabel" |
"getLastPath" |
"getLayer" |
"getLibProperty" |
"getLibPropertyNames" |
"getLights" |
"getMacroButtonIndex" |
"getMacroCommand" |
"getMacroContext" |
"getMacroGroup" |
"getMacroIndexes" |
"getMacroLocation" |
"getMacroName" |
"getMacroProps" |
"getMacros" |
"getMapVisible" |
"getMatchingLibProperties" |
"getMatchingProperties" |
"getMaxLoopIterations" |
"getMaxRecursionDepth" |
"getMoveCount" |
"getNPC" |
"getNPCNames" |
"getName" |
"getNotes" |
"getOwned" |
"getOwnedNames" |
"getOwnerOnlyVisible" |
"getOwners" |
"getPC" |
"getPCNames" |
"getPlayerName" |
"getProperty" |
"getPropertyDefault" |
"getPropertyNames" |
"getPropertyNamesRaw" |
"getPropertyType" |
"getRawProperty" |
"getRecursionDepth" |
"getRequest" |
"getSelected" |
"getSelectedNames" |
"getSightType" |
"getSize" |
"getSpeech" |
"getSpeechNames" |
"getState" |
"getStateImage" |
"getStrProp" |
"getTableAccess" |
"getTableImage" |
"getTableNames" |
"getTableRoll" |
"getTableVisible" |
"getTokenDrawOrder" |
"getTokenFacing" |
"getTokenHandout" |
"getTokenHeight" |
"getTokenImage" |
"getTokenNames" |
"getTokenPortrait" |
"getTokenRotation" |
"getTokenShape" |
"getTokenStates" |
"getTokenVBL" |
"getTokenWidth" |
"getTokenX" |
"getTokenY" |
"getTokens" |
"getVBL" |
"getViewArea" |
"getVisible" |
"getVisibleMapNames" |
"getVisibleTokenNames" |
"getVisibleTokens" |
"getWithState" |
"getWithStateNames" |
"getZoom" |
"goto" |
"hasImpersonated" |
"hasLightSource" |
"hasMacro" |
"hasProperty" |
"hasSight" |
"hero" |
"herobody" |
"herolab.XPath" |
"herolab.getImage" |
"herolab.getInfo" |
"herolab.getMasterName" |
"herolab.getStatBlock" |
"herolab.hasChanged" |
"herolab.isMinion" |
"herolab.refresh" |
"herostun" |
"hex" |
"hypot" |
"hypotenuse" |
"if" |
"indexKeyStrProp" |
"indexOf" |
"indexValueStrProp" |
"initiativeSize" |
"input" |
"isBarVisible" |
"isDialogVisible" |
"isFrameVisible" |
"isFunctionDefined" |
"isGM" |
"isNPC" |
"isNumber" |
"isOwnedByAll" |
"isOwner" |
"isPC" |
"isPropertyEmpty" |
"isSnapToGrid" |
"isTrusted" |
"isVisible" |
"js.eval" |
"js.evala" |
"json.append" |
"json.contains" |
"json.count" |
"json.difference" |
"json.equals" |
"json.evaluate" |
"json.fields" |
"json.fromList" |
"json.fromStrProp" |
"json.get" |
"json.indent" |
"json.indexOf" |
"json.intersection" |
"json.isEmpty" |
"json.isSubset" |
"json.length" |
"json.merge" |
"json.objrolls" |
"json.remove" |
"json.removeAll" |
"json.removeFirst" |
"json.reverse" |
"json.rolls" |
"json.set" |
"json.shuffle" |
"json.sort" |
"json.toList" |
"json.toStrProp" |
"json.type" |
"json.union" |
"json.unique" |
"keep" |
"lastIndexOf" |
"length" |
"listAppend" |
"listContains" |
"listCount" |
"listDelete" |
"listFind" |
"listFormat" |
"listGet" |
"listInsert" |
"listReplace" |
"listSort" |
"ln" |
"log" |
"log10" |
"lower" |
"macroLink" |
"macroLinkText" |
"matches" |
"math.abs" |
"math.acos" |
"math.acos_r" |
"math.asin" |
"math.asin_r" |
"math.atan" |
"math.atan2" |
"math.atan2_r" |
"math.atan_r" |
"math.cbrt" |
"math.ceil" |
"math.cos" |
"math.cos_r" |
"math.cuberoot" |
"math.e" |
"math.floor" |
"math.hypot" |
"math.hypotenuse" |
"math.isEven" |
"math.isInt" |
"math.isOdd" |
"math.log" |
"math.log10" |
"math.max" |
"math.min" |
"math.mod" |
"math.pi" |
"math.pow" |
"math.sin" |
"math.sin_r" |
"math.sqrt" |
"math.squareroot" |
"math.tan" |
"math.tan_r" |
"math.toDegrees" |
"math.toRadians" |
"max" |
"mean" |
"median" |
"min" |
"mod" |
"moveToken" |
"moveTokenFromMap" |
"moveTokenToMap" |
"movedOverPoints" |
"movedOverToken" |
"multiply" |
"nextInitiative" |
"number" |
"oldFunction" |
"openTest" |
"pow" |
"power" |
"postRequest" |
"removeAllFromInitiative" |
"removeAllNPCsFromInitiative" |
"removeAllPCsFromInitiative" |
"removeFromInitiative" |
"removeMacro" |
"removeToken" |
"removeTokenFacing" |
"replace" |
"reroll" |
"resetFrame" |
"resetProperty" |
"restoreFoW" |
"roll" |
"round" |
"selectTokens" |
"sendDrawingToBack" |
"sendToBack" |
"set" |
"setAllStates" |
"setAlwaysVisible" |
"setBar" |
"setBarVisible" |
"setCurrentInitiative" |
"setCurrentMap" |
"setDrawingLayer" |
"setGMName" |
"setGMNotes" |
"setHalo" |
"setHasSight" |
"setInitiative" |
"setInitiativeHold" |
"setInitiativeRound" |
"setLabel" |
"setLayer" |
"setLibProperty" |
"setLight" |
"setMacroCommand" |
"setMacroProps" |
"setMapName" |
"setMapVisible" |
"setMaxLoopIterations" |
"setMaxRecursionDepth" |
"setNPC" |
"setName" |
"setNotes" |
"setOwnedByAll" |
"setOwner" |
"setOwnerOnlyVisible" |
"setPC" |
"setProperty" |
"setPropertyType" |
"setSightType" |
"setSize" |
"setSpeech" |
"setState" |
"setStrProp" |
"setTableAccess" |
"setTableEntry" |
"setTableImage" |
"setTableRoll" |
"setTableVisible" |
"setTokenDrawOrder" |
"setTokenFacing" |
"setTokenHandout" |
"setTokenImage" |
"setTokenOpacity" |
"setTokenPortrait" |
"setTokenShape" |
"setTokenSnapToGrid" |
"setTokenVBL" |
"setTokenWidth" |
"setViewArea" |
"setVisible" |
"setZoom" |
"sortInitiative" |
"sqr" |
"sqrt" |
"square" |
"squareroot" |
"sr4" |
"sr4e" |
"startsWith" |
"strPropFromVars" |
"strfind" |
"strformat" |
"string" |
"stringToList" |
"substring" |
"subtract" |
"success" |
"sum" |
"switchToken" |
"table" |
"tableImage" |
"tbl" |
"tblImage" |
"toggleFoW" |
"transferVBL" |
"trim" |
"u" |
"ubiquity" |
"upper" |
"varsFromStrProp"		{ addToken(Token.FUNCTION); }

	{BooleanLiteral}			{ addToken(Token.LITERAL_BOOLEAN); }

	{LineTerminator}				{ addNullToken(); return firstToken; }

	{Identifier}					{ addToken(Token.IDENTIFIER); }

	{WhiteSpace}					{ addToken(Token.WHITESPACE); }

	/* String/Character literals. */
	{CharLiteral}				{ addToken(Token.LITERAL_CHAR); }
{UnclosedCharLiteral}		{ addToken(Token.ERROR_CHAR); addNullToken(); return firstToken; }
{ErrorCharLiteral}			{ addToken(Token.ERROR_CHAR); }
	{StringLiteral}				{ addToken(Token.LITERAL_STRING_DOUBLE_QUOTE); }
{UnclosedStringLiteral}		{ addToken(Token.ERROR_STRING_DOUBLE); addNullToken(); return firstToken; }
{ErrorStringLiteral}			{ addToken(Token.ERROR_STRING_DOUBLE); }

	/* Comment literals. */
	{MLCBegin}	{ start = zzMarkedPos-4; yybegin(MLC); }
	/* No documentation comments */
	/* No line comments */

	/* Separators. */
	{Separator}					{ addToken(Token.SEPARATOR); }
	{Separator2}					{ addToken(Token.IDENTIFIER); }

	/* Operators. */
	"!" |
"%" |
"&" |
"&&" |
"*" |
"+" |
"," |
"-" |
"/" |
":" |
"<" |
"<=" |
"=" |
"==" |
">" |
">=" |
"|" |
"||"		{ addToken(Token.OPERATOR); }

	/* Numbers */
	{IntegerLiteral}				{ addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }
	{HexLiteral}					{ addToken(Token.LITERAL_NUMBER_HEXADECIMAL); }
	{FloatLiteral}					{ addToken(Token.LITERAL_NUMBER_FLOAT); }
	{ErrorNumberFormat}			{ addToken(Token.ERROR_NUMBER_FORMAT); }

	/* Ended with a line not in a string or comment. */
	<<EOF>>						{ addNullToken(); return firstToken; }

	/* Catch any other (unhandled) characters. */
	.							{ addToken(Token.IDENTIFIER); }

}


/* No char state */

/* No string state */

<MLC> {

	[^hwf\n-]+				{}
	{URL}					{ int temp=zzStartRead; addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); addHyperlinkToken(temp,zzMarkedPos-1, Token.COMMENT_MULTILINE); start = zzMarkedPos; }
	[hwf]					{}

	\n						{ addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); return firstToken; }
	{MLCEnd}					{ yybegin(YYINITIAL); addToken(start,zzStartRead+3-1, Token.COMMENT_MULTILINE); }
	"-"						{}
	<<EOF>>					{ addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); return firstToken; }

}


/* No documentation comment state */

/* No line comment state */
