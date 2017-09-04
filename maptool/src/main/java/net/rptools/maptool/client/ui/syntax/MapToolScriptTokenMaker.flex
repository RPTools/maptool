/*
 * Generated on 9/4/17 2:14 PM
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
	"bitwisenot" |
"bitwisenot" |
"bitwiseor" |
"bitwiseor" |
"bitwisexor" |
"bitwisexor" |
"bnot" |
"bnot" |
"bor" |
"bor" |
"bringDrawingToFront" |
"bringToFront" |
"bringToFront" |
"broadcast" |
"broadcast" |
"bxor" |
"bxor" |
"canSeeToken" |
"canSeeToken" |
"ceil" |
"ceil" |
"ceiling" |
"ceiling" |
"clearLights" |
"clearLights" |
"clearTable" |
"closeDialog" |
"closeDialog" |
"closeFrame" |
"closeFrame" |
"concat" |
"concat" |
"copyMap" |
"copyTable" |
"copyToken" |
"copyToken" |
"countStrProp" |
"countStrProp" |
"countsuccess" |
"countsuccess" |
"createMacro" |
"createMacro" |
"createTable" |
"createTable" |
"currentToken" |
"currentToken" |
"d" |
"decode" |
"decode" |
"defineFunction" |
"defineFunction" |
"deleteStrProp" |
"deleteStrProp" |
"deleteTable" |
"deleteTable" |
"deleteTableEntry" |
"deselectTokens" |
"deselectTokens" |
"dice" |
"dice" |
"divide" |
"divide" |
"drawVBL" |
"drawVBL" |
"drop" |
"drop" |
"encode" |
"encode" |
"endsWith" |
"endsWith" |
"eraseVBL" |
"eraseVBL" |
"eval" |
"eval" |
"evalMacro" |
"evalMacro" |
"execLink" |
"execLink" |
"execMacro" |
"execMacro" |
"explode" |
"explode" |
"explodingSuccess" |
"explodingSuccess" |
"exportData" |
"exportData" |
"exposeAllOwnedArea" |
"exposeAllOwnedArea" |
"exposeFOW" |
"exposeFOW" |
"exposeFogAtWaypoints" |
"exposeFogAtWaypoints" |
"exposePCOnlyArea" |
"exposePCOnlyArea" |
"findToken" |
"findToken" |
"floor" |
"floor" |
"formatStrProp" |
"formatStrProp" |
"fudge" |
"fudge" |
"getAllMapNames" |
"getAllMapNames" |
"getAllPlayerNames" |
"getAllPlayerNames" |
"getAllPropertyNames" |
"getAllPropertyNames" |
"getAlwaysVisible" |
"getBar" |
"getBar" |
"getCurrentInitiative" |
"getCurrentInitiative" |
"getCurrentMapName" |
"getCurrentMapName" |
"getDistance" |
"getDistance" |
"getDistanceToXY" |
"getDistanceToXY" |
"getDrawingLayer" |
"getEnvironmentVariable" |
"getEnvironmentVariable" |
"getExposedTokenNames" |
"getExposedTokenNames" |
"getExposedTokens" |
"getExposedTokens" |
"getFindCount" |
"getFindCount" |
"getGMName" |
"getGMName" |
"getGMNotes" |
"getGMNotes" |
"getGroup" |
"getGroup" |
"getGroupCount" |
"getGroupCount" |
"getGroupEnd" |
"getGroupEnd" |
"getGroupStart" |
"getGroupStart" |
"getHalo" |
"getHalo" |
"getImage" |
"getImage" |
"getImpersonated" |
"getImpersonated" |
"getImpersonatedName" |
"getImpersonatedName" |
"getInfo" |
"getInfo" |
"getInitiative" |
"getInitiative" |
"getInitiativeHold" |
"getInitiativeHold" |
"getInitiativeList" |
"getInitiativeList" |
"getInitiativeRound" |
"getInitiativeRound" |
"getInitiativeToken" |
"getInitiativeToken" |
"getLabel" |
"getLabel" |
"getLastPath" |
"getLastPath" |
"getLayer" |
"getLayer" |
"getLibProperty" |
"getLibProperty" |
"getLibPropertyNames" |
"getLights" |
"getLights" |
"getMacroButtonIndex" |
"getMacroButtonIndex" |
"getMacroCommand" |
"getMacroCommand" |
"getMacroContext" |
"getMacroContext" |
"getMacroGroup" |
"getMacroGroup" |
"getMacroIndexes" |
"getMacroIndexes" |
"getMacroLocation" |
"getMacroLocation" |
"getMacroName" |
"getMacroName" |
"getMacroProps" |
"getMacroProps" |
"getMacros" |
"getMacros" |
"getMapVisible" |
"getMatchingLibProperties" |
"getMatchingLibProperties" |
"getMatchingProperties" |
"getMatchingProperties" |
"getMaxLoopIterations" |
"getMaxLoopIterations" |
"getMaxRecursionDepth" |
"getMaxRecursionDepth" |
"getMoveCount" |
"getMoveCount" |
"getNPC" |
"getNPC" |
"getNPCNames" |
"getNPCNames" |
"getName" |
"getName" |
"getNotes" |
"getNotes" |
"getOwned" |
"getOwned" |
"getOwnedNames" |
"getOwnedNames" |
"getOwnerOnlyVisible" |
"getOwnerOnlyVisible" |
"getOwners" |
"getOwners" |
"getPC" |
"getPC" |
"getPCNames" |
"getPCNames" |
"getPlayerName" |
"getPlayerName" |
"getProperty" |
"getProperty" |
"getPropertyDefault" |
"getPropertyDefault" |
"getPropertyNames" |
"getPropertyNames" |
"getPropertyNamesRaw" |
"getPropertyNamesRaw" |
"getPropertyType" |
"getPropertyType" |
"getRawProperty" |
"getRawProperty" |
"getRecursionDepth" |
"getRecursionDepth" |
"getSelected" |
"getSelected" |
"getSelectedNames" |
"getSelectedNames" |
"getSightType" |
"getSightType" |
"getSize" |
"getSize" |
"getSpeech" |
"getSpeech" |
"getSpeechNames" |
"getSpeechNames" |
"getState" |
"getState" |
"getStateImage" |
"getStateImage" |
"getStrProp" |
"getStrProp" |
"getTableAccess" |
"getTableImage" |
"getTableNames" |
"getTableRoll" |
"getTableVisible" |
"getTokenDrawOrder" |
"getTokenDrawOrder" |
"getTokenFacing" |
"getTokenFacing" |
"getTokenGMName" |
"getTokenHalo" |
"getTokenHandout" |
"getTokenHandout" |
"getTokenHeight" |
"getTokenHeight" |
"getTokenImage" |
"getTokenImage" |
"getTokenLabel" |
"getTokenNames" |
"getTokenNames" |
"getTokenPortrait" |
"getTokenPortrait" |
"getTokenRotation" |
"getTokenShape" |
"getTokenShape" |
"getTokenStates" |
"getTokenStates" |
"getTokenVBL" |
"getTokenWidth" |
"getTokenWidth" |
"getTokenX" |
"getTokenX" |
"getTokenY" |
"getTokenY" |
"getTokens" |
"getTokens" |
"getVBL" |
"getVBL" |
"getViewArea" |
"getVisible" |
"getVisible" |
"getVisibleMapNames" |
"getVisibleMapNames" |
"getVisibleTokenNames" |
"getVisibleTokenNames" |
"getVisibleTokens" |
"getVisibleTokens" |
"getWithState" |
"getWithState" |
"getWithStateNames" |
"getWithStateNames" |
"getZoom" |
"getZoom" |
"goto" |
"goto" |
"hasImpersonated" |
"hasImpersonated" |
"hasLightSource" |
"hasLightSource" |
"hasMacro" |
"hasMacro" |
"hasProperty" |
"hasProperty" |
"hasSight" |
"hasSight" |
"hero" |
"hero" |
"herobody" |
"herobody" |
"herolab.XPath" |
"herolab.XPath" |
"herolab.getImage" |
"herolab.getInfo" |
"herolab.getInfo" |
"herolab.getMasterName" |
"herolab.getStatBlock" |
"herolab.getStatBlock" |
"herolab.hasChanged" |
"herolab.hasChanged" |
"herolab.isMinion" |
"herolab.refresh" |
"herolab.refresh" |
"herostun" |
"herostun" |
"hex" |
"hex" |
"hypot" |
"hypot" |
"hypotenuse" |
"hypotenuse" |
"if" |
"if" |
"indexKeyStrProp" |
"indexKeyStrProp" |
"indexOf" |
"indexOf" |
"indexValueStrProp" |
"indexValueStrProp" |
"initiativeSize" |
"initiativeSize" |
"input" |
"input" |
"isBarVisible" |
"isBarVisible" |
"isDialogVisible" |
"isDialogVisible" |
"isFrameVisible" |
"isFrameVisible" |
"isFunctionDefined" |
"isFunctionDefined" |
"isGM" |
"isGM" |
"isNPC" |
"isNPC" |
"isNumber" |
"isNumber" |
"isOwnedByAll" |
"isOwnedByAll" |
"isOwner" |
"isOwner" |
"isPC" |
"isPC" |
"isPropertyEmpty" |
"isPropertyEmpty" |
"isSnapToGrid" |
"isSnapToGrid" |
"isTrusted" |
"isTrusted" |
"isVisible" |
"isVisible" |
"js.eval" |
"js.eval" |
"js.evala" |
"js.evala" |
"json.append" |
"json.append" |
"json.contains" |
"json.contains" |
"json.count" |
"json.count" |
"json.difference" |
"json.difference" |
"json.equals" |
"json.equals" |
"json.evaluate" |
"json.evaluate" |
"json.fields" |
"json.fields" |
"json.fromList" |
"json.fromList" |
"json.fromStrProp" |
"json.fromStrProp" |
"json.get" |
"json.get" |
"json.indent" |
"json.indent" |
"json.indexOf" |
"json.indexOf" |
"json.intersection" |
"json.intersection" |
"json.isEmpty" |
"json.isEmpty" |
"json.isSubset" |
"json.isSubset" |
"json.length" |
"json.length" |
"json.merge" |
"json.merge" |
"json.objrolls" |
"json.remove" |
"json.remove" |
"json.removeAll" |
"json.removeAll" |
"json.removeFirst" |
"json.reverse" |
"json.reverse" |
"json.rolls" |
"json.set" |
"json.set" |
"json.shuffle" |
"json.shuffle" |
"json.sort" |
"json.sort" |
"json.toList" |
"json.toList" |
"json.toStrProp" |
"json.toStrProp" |
"json.type" |
"json.type" |
"json.union" |
"json.union" |
"json.unique" |
"json.unique" |
"keep" |
"keep" |
"lastIndexOf" |
"lastIndexOf" |
"length" |
"length" |
"listAppend" |
"listAppend" |
"listContains" |
"listContains" |
"listCount" |
"listCount" |
"listDelete" |
"listDelete" |
"listFind" |
"listFind" |
"listFormat" |
"listFormat" |
"listGet" |
"listGet" |
"listInsert" |
"listInsert" |
"listReplace" |
"listReplace" |
"listSort" |
"listSort" |
"ln" |
"ln" |
"log" |
"log" |
"log10" |
"log10" |
"lower" |
"lower" |
"macroLink" |
"macroLink" |
"macroLinkText" |
"macroLinkText" |
"matches" |
"matches" |
"math.abs" |
"math.abs" |
"math.acos" |
"math.acos" |
"math.acos_r" |
"math.acos_r" |
"math.asin" |
"math.asin" |
"math.asin_r" |
"math.asin_r" |
"math.atan" |
"math.atan" |
"math.atan2" |
"math.atan2" |
"math.atan2_r" |
"math.atan2_r" |
"math.atan_r" |
"math.atan_r" |
"math.cbrt" |
"math.cbrt" |
"math.ceil" |
"math.ceil" |
"math.cos" |
"math.cos " |
"math.cos_r" |
"math.cos_r" |
"math.cuberoot" |
"math.cuberoot" |
"math.e" |
"math.e" |
"math.floor" |
"math.floor" |
"math.hypot" |
"math.hypot" |
"math.hypotenuse" |
"math.hypotenuse" |
"math.isEven" |
"math.isEven" |
"math.isInt" |
"math.isInt" |
"math.isOdd" |
"math.isOdd" |
"math.log" |
"math.log" |
"math.log10" |
"math.log10" |
"math.max" |
"math.max" |
"math.min" |
"math.min" |
"math.mod" |
"math.mod" |
"math.pi" |
"math.pi" |
"math.pow" |
"math.pow" |
"math.sin" |
"math.sin" |
"math.sin_r" |
"math.sin_r" |
"math.sqrt" |
"math.sqrt" |
"math.squareroot" |
"math.squareroot" |
"math.tan" |
"math.tan" |
"math.tan_r" |
"math.tan_r" |
"math.toDegrees" |
"math.toDegrees" |
"math.toRadians" |
"math.toRadians" |
"max" |
"max" |
"mean" |
"mean" |
"median" |
"median" |
"min" |
"min" |
"mod" |
"mod" |
"moveToken" |
"moveToken" |
"moveTokenFromMap" |
"moveTokenFromMap" |
"moveTokenToMap" |
"moveTokenToMap" |
"movedOverPoints" |
"movedOverPoints" |
"movedOverToken" |
"movedOverToken" |
"multiply" |
"multiply" |
"nextInitiative" |
"nextInitiative" |
"number" |
"number" |
"oldFunction" |
"oldFunction" |
"openTest" |
"openTest" |
"pow" |
"pow" |
"power" |
"power" |
"removeAllFromInitiative" |
"removeAllFromInitiative" |
"removeAllNPCsFromInitiative" |
"removeAllNPCsFromInitiative" |
"removeAllPCsFromInitiative" |
"removeAllPCsFromInitiative" |
"removeFromInitiative" |
"removeFromInitiative" |
"removeMacro" |
"removeMacro" |
"removeToken" |
"removeToken" |
"removeTokenFacing" |
"removeTokenFacing" |
"replace" |
"replace" |
"requestURL" |
"requestURL" |
"reroll" |
"reroll" |
"resetFrame" |
"resetFrame" |
"resetProperty" |
"resetProperty" |
"restoreFoW" |
"restoreFoW" |
"roll" |
"roll" |
"round" |
"round" |
"selectTokens" |
"selectTokens" |
"sendDrawingToBack" |
"sendToBack" |
"sendToBack" |
"sendURL" |
"sendURL" |
"set" |
"set" |
"setAllStates" |
"setAllStates" |
"setAlwaysVisible" |
"setBar" |
"setBar" |
"setBarVisible" |
"setBarVisible" |
"setCurrentInitiative" |
"setCurrentInitiative" |
"setCurrentMap" |
"setCurrentMap" |
"setDrawingLayer" |
"setGMName" |
"setGMName" |
"setGMNotes" |
"setGMNotes" |
"setHalo" |
"setHalo" |
"setHasSight" |
"setHasSight" |
"setInitiative" |
"setInitiative" |
"setInitiativeHold" |
"setInitiativeHold" |
"setInitiativeRound" |
"setInitiativeRound" |
"setLabel" |
"setLabel" |
"setLayer" |
"setLayer" |
"setLibProperty" |
"setLibProperty" |
"setLight" |
"setLight" |
"setMacroCommand" |
"setMacroCommand" |
"setMacroProps" |
"setMacroProps" |
"setMapName" |
"setMapVisible" |
"setMaxLoopIterations" |
"setMaxRecursionDepth" |
"setMaxRecursionDepth" |
"setNPC" |
"setNPC" |
"setName" |
"setName" |
"setNotes" |
"setNotes" |
"setOwnedByAll" |
"setOwner" |
"setOwner" |
"setOwnerOnlyVisible" |
"setOwnerOnlyVisible" |
"setPC" |
"setPC" |
"setProperty" |
"setProperty" |
"setPropertyType" |
"setPropertyType" |
"setSightType" |
"setSightType" |
"setSize" |
"setSize" |
"setSpeech" |
"setSpeech" |
"setState" |
"setState" |
"setStrProp" |
"setStrProp" |
"setTableAccess" |
"setTableEntry" |
"setTableImage" |
"setTableRoll" |
"setTableVisible" |
"setTokenDrawOrder" |
"setTokenDrawOrder" |
"setTokenFacing" |
"setTokenFacing" |
"setTokenGMName" |
"setTokenHandout" |
"setTokenHandout" |
"setTokenImage" |
"setTokenImage" |
"setTokenLabel" |
"setTokenOpacity" |
"setTokenPortrait" |
"setTokenPortrait" |
"setTokenShape" |
"setTokenSnapToGrid" |
"setTokenVBL" |
"setTokenWidth" |
"setViewArea" |
"setVisible" |
"setVisible" |
"setZoom" |
"setZoom" |
"sortInitiative" |
"sortInitiative" |
"sqr" |
"sqr" |
"sqrt" |
"sqrt" |
"square" |
"square" |
"squareroot" |
"squareroot" |
"sr4" |
"sr4" |
"sr4e" |
"sr4e" |
"startsWith" |
"startsWith" |
"strPropFromVars" |
"strPropFromVars" |
"strfind" |
"strfind" |
"strformat" |
"strformat" |
"string" |
"string" |
"stringToList" |
"stringToList" |
"substring" |
"substring" |
"subtract" |
"subtract" |
"success" |
"success" |
"sum" |
"sum" |
"switchToken" |
"switchToken" |
"table" |
"table" |
"tableImage" |
"tableImage" |
"tbl" |
"tbl" |
"tblImage" |
"tblImage" |
"toggleFoW" |
"toggleFoW" |
"transferVBL" |
"trim" |
"trim" |
"u" |
"u" |
"ubiquity" |
"ubiquity" |
"upper" |
"upper" |
"varsFromStrProp" |
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
