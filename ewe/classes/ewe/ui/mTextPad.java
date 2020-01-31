/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is free software; you can redistribute      *
 *  it and/or modify it under the terms of the Amended GNU Lesser General        *
 *  Public License distributed with this software.                               *
 *                                                                               *
 *  Under this license, linking this library or part thereof with other files to *
 *  produce an executable does not in itself require the executable to be        *
 *  covered by the GNU Lesser General Public License.                            *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.ui;
import ewe.fx.*;
import ewe.sys.Vm;
import ewe.util.*;
/**
* You should use this instead of mTextArea. This is a multi-line text editor which
* has the ability to wrap lines to fit
* the display. This control is designed to be able to be the base class for
* a full text editor (e.g. a NotePad).
**/
//##################################################################
public class mTextPad extends EditControl implements ScrollClient,Selectable{
//##################################################################

protected DisplayLine lines;
protected DisplayLineSpecs dls = new DisplayLineSpecs();
public IImage backgroundImage;// = new Image("Animal-True.png");
/**
* This is true by default - it tells the control to wrap lines to fit the
* display.
**/
public boolean wrapToScreenSize = true;
public int spacing = 4;

/**
This is false by default. Set it true if you do not want a text popup to be displayed
if the SIP covers this control.
**/
public boolean dontWantPopup = false;
/**
* This is true by default. If it is false, then pressing Enter will not cause a new line
to be inserted into the data, but rather a DataChangeEvent will be sent and the focus
will move to the next line.
**/
public boolean wantReturn = true;

/**
* A percent figure. Defaults to 30%
*/
public int minXScroll = 30;
/**
* A percent figure. Defaults to 80%
*/
public int minYScroll = 80;
/**
* If this is true the entire text is selected when it gains focus.
**/
public boolean selectAllOnFocus = false;
/**
* If this is true (which it is by default) then the user will be allowed to switch between overwrite
* and insert mode using the INS key.
**/
public boolean allowOverwrite = true;
/**
* This is used to estimate line lengths.
**/
public int charWidth = 10;
/**
Set this true to disable the cursor. With this mode the the cursor keys
will scroll the display up and down.
**/
public boolean disableCursor = false;
/**
Set this true to disable any text changes. The mTextPad will therefore just
act as a text display.
**/
public boolean disableTextChanges = false;

protected int numLines, lineHeight;
/**
* This is used to determine the state of the input. It will consist of a set of bit flags or'ed together. These
* flags include: STATE_OVERWRITE, STATE_AUTOTAB
**/
public int inputState = 0;
/**
* This is used with the inputState variable to indicate overwrite mode (as opposed to insert mode).
**/
public static final int STATE_OVERWRITE = 0x1;
/**
* This is used with the inputState variable to indicate auto-tab mode.
**/
public static final int STATE_AUTOTAB = 0x2;
/**
* This is the left margin.
**/
public int leftMargin = 0;
/**
* This is the right margin.
**/
public int rightMargin = 0;
/**
* This is extra spacing placed between lines.
**/
public int extraLineSpacing = 0;

{
	modify(KeepSIP|HasData|WantDrag|WantHoldDown|ShowSIP|TakesKeyFocus,PreferredSizeOnly);
	borderStyle = mInput.inputEdge|BF_RECT;//EDGE_SUNKEN|BDR_OUTLINE;
	borderWidth = 0;
	rows = 5;
	columns = 20;
	setCursor(ewe.sys.Vm.IBEAM_CURSOR);
	PenEvent.wantPenMoved(this,PenEvent.WANT_PEN_MOVED_INSIDE,true);
}

//===================================================================
public Menu getTextPadMenu(Menu addTo)
//===================================================================
{
	Menu men = getClipboardMenu(addTo);
	men.addSection(new String [] {"Select All$a"},true);
	return men;
}
private static Menu menu;

//-------------------------------------------------------------------
protected boolean doShowMenu(Point p)
//-------------------------------------------------------------------
{
	if (menu == null) menu = getTextPadMenu(null);
	menu.keepFrame = false;
	if (getMenu() == null) setMenu(menu);
	menuState.doShowMenu(p,false,null);
	return true;
}
//-------------------------------------------------------------------
protected void popupMenuClosed(Menu m)
//-------------------------------------------------------------------
{
	setMenu(null);
	menu = null;
	super.popupMenuClosed(m);
}

//===================================================================
public mTextPad()
//===================================================================
{
	this (5,20);
}
//==================================================================
public mTextPad(int rows,int columns)
//==================================================================
{
	this.rows = rows; this.columns = columns;
}
//===================================================================
public mTextPad(String text)
//===================================================================
{
	rows = columns = 0;
	this.text = text;
	if (this.text == null) this.text = "";
}
/**
* Returns the height in pixels of each line (all lines are the same height).
**/
//===================================================================
public int getLineHeight()
//===================================================================
{
	FontMetrics fm = getFontMetrics();
	return fm.getHeight()+2+extraLineSpacing;//+fm.getLeading();
}

/**
 * Returns the height in pixels from the top of a line down to the baseline of the font.
 */
//===================================================================
public int getBaselineHeight()
//===================================================================
{
	FontMetrics fm = getFontMetrics();
	return fm.getAscent();
}
/*
//==================================================================
protected int getTextWidth()
//==================================================================
{
	FontMetrics fm = getFontMetrics();
	int w = 0;
	for (int i = 0; i<getNumLines(); i++) {
		int ww = fm.getTextWidth(getLine(i]);
		if (ww > w) w = ww;
	}
	return w;
}
*/
/*
//==================================================================
protected void calculateSizes()
//==================================================================
{
	charWidth = getFontMetrics().getCharWidth('0');
	Rect r = Gui.getAverageSize(getFontMetrics(),rows,columns,0,0);
	preferredWidth = r.width;
	preferredHeight = r.height;
}
*/
private boolean alreadyCalculated = false;
//-------------------------------------------------------------------
protected void calculateSizes()
//-------------------------------------------------------------------
{
	if (alreadyCalculated) {
		return;
	}
	alreadyCalculated = true;
	Rect r;
 	if (rows != 0 && columns != 0) {
		r = Gui.getAverageSize(getFontMetrics(),rows,columns,spacing,spacing);
	}
	else {
		Dimension d = (Dimension)getTag(PREFERREDSIZE,new Dimension(200,-1));
		splitLines(d.width-4-spacing*2);
		r = new Rect(0,0,d.width,getLineHeight()*numLines+spacing*2);
		d = (Dimension)getTag(MAXIMUMSIZE,new Dimension(-1,200));
		r.height = Math.min(d.height,r.height);
		lines = null; numLines = 0;
	}
	preferredWidth = r.width; preferredHeight = r.height;
}

//===================================================================
//public Rect getDataRect(Rect dest)
//===================================================================
//{
//	return Rect.unNull(dest).set(x+spacing,y+spacing,width-spacing*2,height-spacing*2);
//}
//==================================================================
public int getAvailableWidth()
//==================================================================
{
	if (wrapToScreenSize) return width-4-spacing*2;
	else return Gui.getAverageSize(getFontMetrics(),1,columns,2,2).width;
}
/**
* Return the number of lines being displayed.
**/
//==================================================================
public int getNumLines()
//==================================================================
{
	if (lines == null) splitLines(getAvailableWidth());
	return numLines;
}
/**
* If wrapToScreenSize is true, this returns the width of the widest word. Otherwise it
* will return the width of the widest line.
**/
//===================================================================
public int getTextWidth()
//===================================================================
{
	if (lines == null) return 0;
	return lines.getWidth(getFontMetrics(),wrapToScreenSize ? 0 : DisplayLine.SPLIT_NO_WRAP);
}
//===================================================================
public void resizeTo(int width,int height)
//===================================================================
{
	noSelection();
	boolean needUpdate = this.width != width;
	super.resizeTo(width,height);
	if (needUpdate) update(false);
}

//===================================================================
public void displayLinesChanged()
//===================================================================
{
	if (lines == null) {
		lines = new DisplayLine();
		lines.line = "";
	}
	DisplayLine last = (DisplayLine)lines.getNext(lines,-1);
	if ((last.flags & DisplayLine.ENDS_WITH_NEWLINE) != 0){
		DisplayLine dl = new DisplayLine();
		dl.line = "";
		last.next = dl;
		dl.prev = last;
	}
	numLines = DisplayLine.countNext(lines);
}

//===================================================================
public void pushDisplayLine(String newLine,int newFlags,DisplayLine pushDown)
//===================================================================
{
	DisplayLine dl = new DisplayLine();
	dl.line = pushDown.line;
	dl.flags = pushDown.flags;
	dl.next = pushDown.next;
	if (dl.next != null) dl.next.prev = dl;
	dl.prev = pushDown;
	pushDown.line = newLine;
	pushDown.flags = newFlags;
	pushDown.next = dl;
}
//===================================================================
public DisplayLine addDisplayLine(String newLine,DisplayLine addBefore)
//===================================================================
{
	DisplayLine dl = new DisplayLine();
	dl.line = newLine;
	addDisplayLines(dl,addBefore);
	return dl;
}
//===================================================================
public void addDisplayLines(DisplayLine newSequence,DisplayLine addBefore)
//===================================================================
{
	DisplayLine dl = newSequence;
	if (addBefore != null){
		dl.addSectionBefore(addBefore,dl);
		if (lines == addBefore) lines = dl;
	}else{
		LinkedListElement lle = lines.getNext(lines,-1);
		lle.next = dl;
		dl.prev = lle;
	}
	displayLinesChanged();
}

/**
 * Insert blank lines before or after the specified data line so that there are at least
 * count number of blank lines before or after the data line.
 * @param dataLine The data line.
 * @param count The number of blank lines.
 * @param above if this is true the blank lines will be added before (above) the data line.
 * @return the number of lines that were inserted.
 */
//===================================================================
public int ensureBlankLines(DisplayLine dataLine,int count,boolean above)
//===================================================================
{
	if (count < 1) return 0;
	int current = dataLine.countBlankLines(above);
	int toAdd = count-current;
	if (toAdd < 1) return 0;
	DisplayLine blanks = DisplayLine.getBlankLines(toAdd,above);
	addDisplayLines(blanks,above ? dataLine : (DisplayLine)dataLine.next);
	return toAdd;
}
/**
* This forces a line break in the specified data line immediately before the
* specified index. The left-over data on the line is placed into another line
* placed after the data line. Note that if there are no characters after the index
* and the line does not end with a line feed then the line will not be broken and null will be returned.
* @param dataLine The dataLine to break.
* @param index The index of the character before which a break will be added.
* @return The DisplayLine containing the extra characters that was added after the
* display line.
*/
//===================================================================
public DisplayLine breakLineBefore(DisplayLine dataLine,int index)
//===================================================================
{
	String line = dataLine.line;
	if ((dataLine.flags & dataLine.ENDS_WITH_NEWLINE) != 0) line += '\n';
	int tl = line.length();
	if (index < 0 || index >= tl) return null;
	DisplayLine dl = new DisplayLine();
	dl.displayWidth = dataLine.displayWidth;
	dl.line = line.substring(index);
	dataLine.line = line.substring(0,index);
	if ((dataLine.flags & dataLine.ENDS_WITH_NEWLINE) != 0){
		dl.flags |= dataLine.ENDS_WITH_NEWLINE;
		dl.line = dl.line.substring(0,dl.line.length()-1);
	}
	dataLine.flags &= ~dataLine.ENDS_WITH_NEWLINE;
	addDisplayLines(dl,(DisplayLine)dataLine.next);
	return dl;
}
//===================================================================
public void replaceDisplayLines(DisplayLine newSequence,DisplayLine firstToReplace, DisplayLine lastToReplace)
//===================================================================
{
	if (firstToReplace == null) addDisplayLines(newSequence,null);
	else {
		firstToReplace.invalid = true;
		LinkedListElement.replaceSection(firstToReplace,lastToReplace,newSequence);
		if (lines == firstToReplace) lines = newSequence;
		displayLinesChanged();
	}
}
/**
 * Remove a single DisplayLine
 */
//===================================================================
public void removeDisplayLine(DisplayLine toRemove)
//===================================================================
{
	if (toRemove == lines) lines = (DisplayLine)toRemove.next;
	if (toRemove.next != null) toRemove.next.prev = toRemove.prev;
	if (toRemove.prev != null) toRemove.prev.next = toRemove.next;
	displayLinesChanged();
}

/**
 * This is used by TextFormatters to split text for display. Usually the lines returned are
 * used to replace lines already in the mTextPad
* @param text The text to split.
* @param forWidth The width in pixels to split for.
* @param fontMetrics The font metrics to use. If this is null then the mTextPad's metrics will be used.
* @param fts The FormattedTextSpecs to use. If this is null then the  mTextPad's specs will be used.
* @return The list of DisplayLines created.
*/
//===================================================================
public DisplayLine splitLines(String text,int forWidth,FontMetrics fontMetrics,FormattedTextSpecs fts)
//===================================================================
{
	return DisplayLine.split(text,fontMetrics == null ? getFontMetrics():fontMetrics,forWidth,wrapToScreenSize ? 0 : DisplayLine.SPLIT_NO_WRAP,fts == null ? format : fts);
}
/**
 * This is used by TextFormatters to split text for display. Usually the lines returned are
 * used to replace lines already in the mTextPad
* @param text The text to split.
* @param widthProvider An object to provide the width of each line.
* @param fontMetrics The font metrics to use. If this is null then the mTextPad's metrics will be used.
* @param fts The FormattedTextSpecs to use. If this is null then the  mTextPad's specs will be used.
* @return The list of DisplayLines created.
*/
//===================================================================
public DisplayLine splitLines(String text,DisplayLine.WidthProvider widthProvider,FontMetrics fontMetrics,FormattedTextSpecs fts)
//===================================================================
{
	return DisplayLine.split(text,fontMetrics == null ? getFontMetrics():fontMetrics,0,wrapToScreenSize ? 0 : DisplayLine.SPLIT_NO_WRAP,fts == null ? format : fts,widthProvider);
}

/**
 * Resplit and replace lines of text, possibly substituting new text. The first DisplayLine is
 * re-used with a new "line" and "displayWidth" value.
 * @param firstLine The first DisplayLine in the sequence.
 * @param numLines The number of lines in the sequence.
 * @param forWidth The width to wrap the lines to. Can be zero if widthProvider is used.
 * @param widthProvider An optional DisplayLine.WidthProvider to provide widths for each line.
 * @param fm The FontMetrics to use, can be null to use the mTextPad's FontMetrics.
 * @param newText Optional new text, if null then the original text is used.
 * @return The first DisplayLine of the newly split lines.

 */
//===================================================================
public DisplayLine resplit(DisplayLine firstLine,int numLines,int forWidth,DisplayLine.WidthProvider widthProvider, FontMetrics fm, String newText)
//===================================================================
{
	if (newText == null) newText = firstLine.concatenate(firstLine,0,numLines);
	if (fm == null) fm = getFontMetrics();

	DisplayLine dl =
		DisplayLine.split(newText,fm,forWidth,wrapToScreenSize ? 0 : DisplayLine.SPLIT_NO_WRAP,format,widthProvider);
	if (dl == null) return firstLine;
	DisplayLine dle = (DisplayLine)dl.getNext(dl,-1);
	DisplayLine lastLine = (DisplayLine)firstLine.getNext(firstLine,numLines-1);
	//
	dle.next = lastLine.next;
	//
	// dl itself will not be added.
	//
	if (dle != dl && dle.next != null) dle.next.prev = dle;
	//}
	//
	// firstLine will actually stay in place, but it will be changed to reflect the new data.
	//
	firstLine.line = dl.line;
	firstLine.flags = dl.flags;
	firstLine.width = dl.width;
	firstLine.lengthOfLine = dl.lengthOfLine;
	firstLine.displayWidth = dl.displayWidth;
	//if (dle != dl)
	firstLine.next = dl.next;
	if (dl.next != null) dl.next.prev = firstLine;
	//
	displayLinesChanged();
	return firstLine;
}

//------------------------------------------------------------------
protected void splitLines(int width)
//------------------------------------------------------------------
{
	FontMetrics fm = getFontMetrics();
	lineHeight = fm.getHeight()+2+extraLineSpacing;
	lines = DisplayLine.split(text,fm,width-leftMargin-rightMargin,wrapToScreenSize ? 0 : DisplayLine.SPLIT_NO_WRAP,format);
	//ewe.sys.Vm.debug("Split to: "+(width-leftMargin-rightMargin));
	displayLinesChanged();
}
/*
//------------------------------------------------------------------
protected void insertLine(int index)
//------------------------------------------------------------------
{
	if (lines == null) lines = new DisplayLine();
	if (index > lines.length) index = lines.length;
	DisplayLine [] nl = new DisplayLine[lines.length+1];
	for (int i = 0; i<index; i++) nl[i] = getLine(i);
	nl[index] = new DisplayLine(); nl[index].line = "";
	for (int i = index; i<lines.length; i++) nl[i+1] = getLine(i];
	lines = nl;
}
*/
private int [] posBuffer;
//------------------------------------------------------------------
protected boolean getCharRect(int ch,int ln,Rect dest)
//------------------------------------------------------------------
{
	dest.width = dest.x = 0;
	dest.height = getLineHeight();
	dest.y = dest.height*ln;
	FontMetrics fm = getFontMetrics();
//..................................................................
	if (ln >= numLines || ln < 0) return false;
	DisplayLine dl = getLine(ln);
	String s = toString(dl);
	if (ch > s.length() || ch < 0) return false;
	if (s.indexOf('\t') == -1 && getSpecialFormatCount(ln,dl) == 0 && leftMargin == 0 && rightMargin == 0){
		if (ch == 0) dest.x = 0;
		else dest.x = fm.getTextWidth(s.substring(0,ch));
		if (ch == s.length()) dest.width = 5;
		else dest.width = fm.getCharWidth(s.charAt(ch));
	}else{
		FormattedTextSpecs f = getTextPositions(ln,dl);
		posBuffer = f.calculatedPositions;
		if (ch == 0) dest.x = 0;
		else dest.x = posBuffer[ch-1];
		dest.x += f.leftMargin;
		if (ch == s.length()) dest.width = 5;
		else dest.width = posBuffer[ch]-(ch == 0 ? 0 : posBuffer[ch-1]);
	}
//..................................................................
	return true;
}
//-------------------------------------------------------------------
protected boolean getCursorRect(int ch,int ln,Rect dest)
//-------------------------------------------------------------------
{
	if (getCharRect(ch,ln,dest)){
		String s = toString(getLine(ln));
		if (ch < s.length())
			if (s.charAt(ch) == '\t')
				dest.width = 5;
		return true;
	}else
		return false;
}
//==================================================================
public void setText(String what)
//==================================================================
{
	clearSelection();
	text = what;
	if (text == null) text = "";
	splitLines(getAvailableWidth());
	fix();
	repaintDataNow();
	checkScrolls();
}
//==================================================================
public String getText()
//==================================================================
{
	if (lines == null || disableTextChanges) return text;
	else return DisplayLine.concatenate(lines,0,numLines);
	/*
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i<lines.length; i++){
		if (getLine(i] != null) {
			if (i != 0) sb.append("\n");
			sb.append(getLine(i]);
		}
	}
	return sb.toString();
	*/
}

/**
 * Get all the lines.
 * Additional verbose
 * @return
 */
//===================================================================
public String [] getLines()
//===================================================================
{
	if (lines == null) splitLines(getAvailableWidth());
	return DisplayLine.toLines(lines);
}
//==================================================================
protected void fixText()
//==================================================================
{
	setText(getText());
}
//==================================================================
public int getScreenRows()
//==================================================================
{
	int h = getLineHeight();
	if (h == 0) return 0;
	return (height-spacing*2)/h;
}
protected static ImageBuffer itemBuffer = new ImageBuffer();
protected static ImageBuffer blockBuffer = new ImageBuffer();

private static char [] singleChar = new char[1];

//==================================================================
public void paintLastChar(Graphics g) {paintLastChar(g,(char)0);}
public void paintLastChar(Graphics g,char toErase)
//==================================================================
{
	if (g == null) {
		g = getGraphics();
	}
	if (g == null) return;
	int flags = getModifiers(true);
	getColors(hasCursor,flags);
	FontMetrics fm = getFontMetrics();
	DisplayLine dl = getLine(curState.cursorLine);
	String line = toString(dl);
	int x = spacing-curState.xShift;
	int w = 0;
	int h = getLineHeight(),y = h*(curState.cursorLine-curState.firstLine)+spacing;
	g.setDrawOp(g.DRAW_OVER);
	if (line.length() >= 0) {
		char [] ln = ewe.sys.Vm.getStringChars(line);
		FormattedTextSpecs f;
		posBuffer = (f = getTextPositions(curState.cursorLine,dl)).calculatedPositions;//fm.getFormattedTextPositions(line,format,posBuffer);
		int check = toErase == 0 ? ln.length-2:ln.length-1;
		x += check >= 0 ? posBuffer[check] : 0;//fm.getTextWidth(ln,0,toErase == 0 ? ln.length-1 : ln.length);
		x += f.leftMargin;
		if (toErase == 0){
			g.setColor(colors[0]);//Color.Black);
			g.setFont(getFont());
			if (ln[ln.length-1] != '\t') g.drawText(ln,ln.length-1,1,x,y);
		}else {
			/*
			Color c = Color.White;
			if (!(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0)) || !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))) c = Color.LightGray;
			*/
			g.setColor(colors[1]);//c);
			w = fm.getCharWidth(toErase);
			g.fillRect(x,y,w,h);
		}
	}
}
//==================================================================
public boolean deleteSelection()
//==================================================================
{
	int firstLine = curState.selStartLine;
	int lastLine = curState.selEndLine;
	if (!removeSelection()) return false;
	rearrange(getLine(firstLine),firstLine,null,true);
	fix();
	return true;
}
//==================================================================
public void paintLine(Graphics g,int index) {paintLine(g,index,null);}
//==================================================================

/**
* This can be used to alter things like the tab stop width. Do not set this to null.
**/
public FormattedTextSpecs format = new FormattedTextSpecs();


//-------------------------------------------------------------------
protected int getSpecialFormatCount(int lineIndex,DisplayLine theLine)
//-------------------------------------------------------------------
{
	//if (theLine.length() >= 20) return 1;
	return 0;
}
//-------------------------------------------------------------------
protected void applySpecialFormat(int formatIndex,int lineIndex,DisplayLine theLine,FormattedTextSpecs format)
//-------------------------------------------------------------------
{
/*
	int [] ft = format.calculatedPositions;
	String part = theLine.substring(0,7);
	Font f = format.metrics.getFont();
	f = new Font(f.getName(),f.ITALIC|f.BOLD|f.UNDERLINE,f.getSize()+10);
	FontMetrics fm = format.metrics.getNewFor(f);
	int [] got = fm.getFormattedTextPositions(part,format,null);
	format.changeAndAdjustPositions(got,0,6);
	*/
}
//-------------------------------------------------------------------
protected void drawSpecialFormat(int formatIndex,int lineIndex,DisplayLine theLine,FormattedTextSpecs format,Graphics g,Color background)
//-------------------------------------------------------------------

{
/*
	int [] ft = format.calculatedPositions;
	Font f = format.metrics.getFont();
	f = new Font(f.getName(),f.ITALIC|f.BOLD|f.UNDERLINE,f.getSize()+10);
	g.setColor(background);
	g.fillRect(0,0,format.widthOf(0,6),lineHeight);
	g.setColor(new Color(0,0,255));
	g.setFont(f);
	g.drawFormattedText(theLine,0,6,0,0,format);
	*/
}

//===================================================================
public FormattedTextSpecs getTextPositions(int lineIndex,DisplayLine line,FormattedTextSpecs format,int[] posBuffer,boolean useFormatters)
//===================================================================
{
	FontMetrics fm = getFontMetrics();
	if (line == null) line = getLine(lineIndex);
	if (format == null) format = this.format;
	if (posBuffer == null) posBuffer = this.posBuffer;
	posBuffer = fm.getFormattedTextPositions(toString(line),format,posBuffer);
	format.metrics = fm;
	format.calculatedPositions = posBuffer;
	format.displayLineWidth = width-spacing*2;
	format.leftMargin = leftMargin;
	format.rightMargin = rightMargin;
	format.extraSpaceUsed = 0;
	format.firstCharPosition = 0;
	format.lineFlags = 0;
	format.backgroundColor = null;
	if (line != null && useFormatters){
		int count = getSpecialFormatCount(lineIndex,line);
		for (int i = 0; i<count; i++)
			applySpecialFormat(i,lineIndex,line,format);
	}
	return format;
}
//-------------------------------------------------------------------
protected FormattedTextSpecs getTextPositions(int lineIndex,DisplayLine line)
//-------------------------------------------------------------------
{
	return getTextPositions(lineIndex,line,format,posBuffer,true);
}

private static char [] charsToDraw = new char[0];

//-------------------------------------------------------------------
void setupCharsToDraw(String line,FormattedTextSpecs format)
//-------------------------------------------------------------------
{
	int ll = line.length();
	if (charsToDraw.length < ll) charsToDraw = new char[ll];
	mString.copyInto(line,charsToDraw,0);
	format.charsToDraw = charsToDraw;
	format.numCharsToDraw = ll;
}
private static Rect reducedClip = new Rect();
//==================================================================
public void paintLine(Graphics g,int index,DisplayLine theLine)
//==================================================================
{
	if (g == null) g = getGraphics();
	if (g == null) return;
	int num = getScreenRows();
	if (index < curState.firstLine || index >= curState.firstLine+getScreenRows()+1) return;
	if (curState.cursorLine == index && cursorOn) cursorOn = false;
	if (theLine == null) theLine = getLine(index);
	Rect wasClip = g.reduceClip(spacing,spacing,width-spacing*2,height-spacing*2,reducedClip);
	String line = toString(theLine);
	int h = getLineHeight();
	int y = spacing+(index-curState.firstLine)*h;
	int x = 0;//2-curState.xShift;
	int w = width-(spacing)*2;
	int flags = getModifiers(true);
	getColors(hasCursor,flags);
	boolean notEn = !(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0));
	boolean dis = notEn || !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0));
	int specialFormatCount = getSpecialFormatCount(index,theLine);
	boolean isFormatted = specialFormatCount != 0 || line.indexOf('\t') != -1;
	if (leftMargin != 0 || rightMargin != 0) isFormatted = true;
	Graphics gr = itemBuffer.get(w,h,true);
	Image img = itemBuffer.image;
	gr.setDrawOp(gr.DRAW_OVER);
//..................................................................
// Fill background.
//..................................................................
	/*
	Color c;
	if (dis) c = Color.LightGray;
	else c = Color.White;
	*/
	gr.setColor(colors[1]);//c);
	gr.fillRect(0,0,w,h);
//..................................................................
// Do text.
//..................................................................
	/*
	if (notEn) c = Color.DarkGray;
	else c = getForeground();
	*/
	Font f = getFont();
	gr.setColor(colors[0]);//c);
	gr.translate(-curState.xShift,0);

	if (backgroundImage != null){
		int ih = backgroundImage.getHeight(), iw = backgroundImage.getWidth();
		int iy = -(h*index)%ih;
		while(true){
			for (int ix = 0; ix <= w+curState.xShift; ix += iw){
				if (ix+iw <= curState.xShift) continue;
				backgroundImage.draw(gr,ix,iy,0);
			}
			iy += ih;
			if (iy > h) break;
		}
	}

	boolean isInSelection = curState.isInSelection(index) && curState.selectionEnabled;
	Graphics bg = null;
	int leftWidth = 0;
	int bgtx = 0;
	if (isInSelection){
		int sp = 0, ep = line.length();
		if (curState.selStartLine == index) sp = curState.selStartPos;
		if (curState.selEndLine == index) ep = curState.selEndPos;

		FontMetrics fm = getFontMetrics();
		int myWidth;
		if (!isFormatted){
			leftWidth = fm.getTextWidth(line.substring(0,sp));
			myWidth = fm.getTextWidth(line.substring(sp,ep));
		}else{
			getTextPositions(index,theLine);
			leftWidth = (sp == 0) ? 0 : posBuffer[sp-1];
			myWidth = ep == 0 ? 0 : posBuffer[ep-1]-leftWidth;
			leftWidth += format.leftMargin;
		}
		if (leftWidth+myWidth-curState.xShift > w) {
			myWidth = w-leftWidth+curState.xShift;
		}
		bg = blockBuffer.get(myWidth,h,true);
		bg.setColor(colors[3]);
		bg.fillRect(0,0,myWidth,h);
		bg.setColor(colors[2]);
		bg.translate(bgtx = /*-curState.xShift*/-leftWidth,0);
	}
	gr.setFont(f);
	if (isFormatted){
		if (!isInSelection) posBuffer = getTextPositions(index,theLine).calculatedPositions;
		if (format.backgroundColor != null){
			gr.setColor(format.backgroundColor);
			gr.fillRect(curState.xShift,0,w,h);
			gr.setColor(colors[0]);//c);
		}
		format.displayLineWidth = w;
		format.displayLineHeight = lineHeight;
		if (specialFormatCount != 0){
			setupCharsToDraw(line,format);
			for (int i = specialFormatCount-1; i >-1; i--){
				drawSpecialFormat(i,index,theLine,format,gr,colors[1]);
				gr.setFont(f);
				gr.setColor(colors[0]);
			}
			gr.drawFormattedText(format.charsToDraw,0,format.numCharsToDraw,x+format.leftMargin+format.firstCharPosition,0,format);
		}else
			gr.drawFormattedText(line,x+format.leftMargin+format.firstCharPosition,0,format);
		if (bg != null) {
			bg.setFont(f);
			if (specialFormatCount != 0){
				setupCharsToDraw(line,format);
				for (int i = specialFormatCount-1; i >-1; i--){
					drawSpecialFormat(i,index,theLine,format,bg,colors[3]);
					bg.setFont(f);
					bg.setColor(colors[2]);
				}
				bg.drawFormattedText(format.charsToDraw,0,format.numCharsToDraw,x+format.leftMargin+format.firstCharPosition,0,format);
			}else
				bg.drawFormattedText(line,x+format.leftMargin+format.firstCharPosition,0,format);
		}
	}else{
		gr.drawText(line,x,0);//+1);
		if (bg != null) {
			bg.setFont(f);
			bg.drawText(line,x,0);
		}
	}
	if (bg != null) {
		gr.drawImage(blockBuffer.image,leftWidth,0);
		bg.translate(-bgtx,0);
	}
	gr.translate(curState.xShift,0);
	g.drawImage(img,spacing,y);
	g.restoreClip(wasClip);
}
//==================================================================
public void doPaint(Graphics g,Rect area)
//==================================================================
{
	int flags = getModifiers(true);
	if (!((flags & Invisible) == 0)) return;
	getColors(hasCursor,flags);
	if ((borderStyle & BF_SOFT) != 0) doBackground(g);
/*
	Color c = Color.White;
	if (!(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0)) || !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0)))
		c = Color.LightGray;
	*/
	Color c = colors[1];
	//g.draw3DButton(getDim(null),true,c,,true);
	if (!hasModifier(PaintDataOnly,false))
		g.draw3DRect(
			getDim(Rect.buff),
			borderStyle, //Graphics.EDGE_SUNKEN|Graphics.BDR_OUTLINE,
			(flags & DrawFlat) != 0,
			c,//null,
			borderColor);
/*
	g.draw3DRect(
		getDim(Rect.buff),
		Graphics.EDGE_SUNKEN|Graphics.BDR_OUTLINE,
		(flags & DrawFlat) != 0,
		c,
		Color.DarkGray);
*/
		doPaintData(g,area);
}

private boolean inCursor = false;

//===================================================================
public void paintCursor(Graphics gr) {paintCursor(gr,false);}
//===================================================================
public void paintCursor(Graphics gr,boolean sticky)
//==================================================================
{
	if (inCursor) return;
	Graphics g = gr;
	if (g == null) g = getGraphics();
	if (g == null) return;
	boolean leave = leaveCursor;
	leaveCursor = sticky;
	int flags = getModifiers(true);
	getColors(hasCursor,flags);
	if (leave) return;
	if (hasCursor && amOnTopFrame()){
		int onScreen = curState.cursorLine-curState.firstLine;
		if (onScreen < 0 || onScreen >= getScreenRows()) return;
		Rect r = new Rect(0,0,0,0);
		if (!getCursorRect(curState.cursorPos,curState.cursorLine,r)) return;
		inCursor = true;
		int cw = (inputState & STATE_OVERWRITE) == 0 ? 2 : r.width;
		Image i = new Image(cw,r.height);
		Graphics gi = new Graphics(i);
		gi.setColor(colors[0]);//getForeground());
		gi.fillRect(0,0,r.width,r.height);
		gi.free();
		boolean cc = g.canCopyFrom();// && false;
		if (!cc){
			Image i2 = new Image(2,r.height);
			gi = new Graphics(i2);
			//gi.setColor(Color.White);
			//if (!(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0)) || !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))) gi.setColor(getBackground());
			gi.setColor(colors[1]);
			gi.fillRect(0,0,r.width,r.height);
			r.x += spacing-curState.xShift;
			r.y += spacing-curState.firstLine*getLineHeight();
			gi.translate(-r.x,-r.y);
			boolean co = cursorOn;
			//repaintNow(gi,r);
			paintLine(gi,curState.cursorLine);
			cursorOn = co;
			gi.translate(r.x,r.y);
			r.x -= spacing-curState.xShift; r.y -= spacing-curState.firstLine*getLineHeight();
			if (!cursorOn) {
				//ewe.sys.Vm.debug("Yep");
				gi.setDrawOp(g.DRAW_XOR);
				gi.drawImage(i,0,0);
			}else{
				//ewe.sys.Vm.debug("Nope");
			}
			gi.free();
			i.free();
			i = i2;
			g.setDrawOp(g.DRAW_OVER);
		}else
			g.setDrawOp(g.DRAW_XOR);
		g.drawImage(i,r.x+spacing-curState.xShift,spacing+r.y-curState.firstLine*getLineHeight());
		g.setDrawOp(g.DRAW_OVER);
		i.free();
		cursorOn = !cursorOn;
	}
	if (gr == null) g.free();
	inCursor = false;
}
//-------------------------------------------------------------------
protected void doPaintData(Graphics g)
//-------------------------------------------------------------------
{
	doPaintData(g,null);
}
//-------------------------------------------------------------------
protected void doPaintData(Graphics g,Rect area)
//-------------------------------------------------------------------
{
	//if (dragOver != null) dragOver.moveTo(null,0,0);
	//dragOver = null;
	clearCursor();
	if (doingNativeInput) return;
	if (lines == null) splitLines(getAvailableWidth());
	int from = curState.firstLine;
	int to = from+getScreenRows()+1;
	int h = getLineHeight();
	int y = spacing;
	int st = area == null ? 0 : area.y;
	int en = area == null ? height : area.y + area.height;
	DisplayLine dl = (DisplayLine)DisplayLine.getNext(lines,from);
	for (int i = from; i<= to; i++){
		if (y > en) break;
		if (y+h >= st) {
			if (dl != null) paintLine(g,i,dl);
			else paintLine(g,i);
		}
		if (dl != null) dl = (DisplayLine)dl.next;
		y += h;
	}
	/*
	for (int i = 0; i<getScreenRows()+1; i++)
		paintLine(g,i+curState.firstLine);
	*/

}
//===================================================================
public void paintLines(int from,int to)
//===================================================================
{
	if (to < from){
		int t = from;
		from = to;
		to = t;
	}
	int fl = curState.firstLine;
	if (from < fl) from = fl;
	if (from > to) return;
	if (to-fl > getScreenRows()) to = fl+getScreenRows();
	Graphics g = getGraphics();
	if (g == null) return;
	DisplayLine dl = (DisplayLine)DisplayLine.getNext(lines,from);
	for (int i = from; i<= to; i++){
		if (dl == null) break;
		paintLine(g,i,dl);
		dl = (DisplayLine)dl.next;
	}
}
//===================================================================
protected void paintLinesFrom(int index)
//===================================================================
{
	Graphics g = getGraphics();
	if (g != null)
		for (int i = 0; i<getScreenRows()+1; i++)
			if (i+curState.firstLine >= index)
				paintLine(g,i+curState.firstLine);
	cursorOn = false;
}
//==================================================================
// This is the implementation of ScrollClient. Remove this if ScrollBars
// are not needed.
//==================================================================
	//protected //ScrollServer ss;
	protected boolean amScrolling;
	//==================================================================
	public ScrollablePanel getScrollablePanel()
	//==================================================================
	{
		amScrolling = true;
		ScrollablePanel sp = new ScrollBarPanel(this);
		sp.modify(0,TakeControlEvents);
		return sp;
	}
//===================================================================
public void update() {update(true);}
//===================================================================
//===================================================================
public void update(boolean doChecks)
//===================================================================
{
	int cp = curState.cursorPos;
	int cl = curState.cursorLine;
	DisplayLine section = getLine(cl);
	if (section == null) return;
	section = section.getSection(cl);
	if (section == null) return;
	int idx = getLine(0).findSectionIndex(section);
	int pos = section.getPositionInSection(cl,cp);
	text = getText();
	splitLines(getAvailableWidth());
	section = getLine(0).getIndexedSection(idx);
	Dimension p = section.positionInSection(pos,null,false);
	curState.cursorPos = p.width;
	curState.cursorLine = p.height;
	if (fix()) {
		repaintDataNow();
	}
	if (doChecks){
		if (ss != null) ss.checkScrolls();
	}
}

/**
* Set this to be a non-zero value to have a specific document width
* (in pixels), regardless of the length of the lines being displayed.
**/
public int forcedActualWidth = 0;

//===================================================================
public int getActual(int which)
//===================================================================
{
	if (lines == null) update();
	if (which == Horizontal) return forcedActualWidth > 0 ? forcedActualWidth : getTextWidth()+leftMargin;
	else return numLines;
}
//===================================================================
public int getVisible(int which,int forSize)

//===================================================================
{
	if (lines == null) update();
	if (lineHeight == 0) return 0;
	if (which == Horizontal) return forSize-spacing*2;
	else return (forSize-spacing*2)/lineHeight;
}
//===================================================================
public int getCurrent(int which)
//===================================================================
{
	if (which == IScroll.Vertical) return curState.firstLine;
	else return curState.xShift;
}
//===================================================================
public boolean needScrollBar(int which,int forSize)
//===================================================================
{
	boolean ret = getVisible(which,forSize) < getActual(which);


	return ret;
}
public boolean canGo(int orientation,int direction,int position)
{
	return true;
}
	//public void setServer(ScrollServer server) {ss = server;}

//-------------------------------------------------------------------
private boolean vscroll(int was,int now)
//-------------------------------------------------------------------
{
	if (was == now) return false;
	int difference = now > was ? now-was : was-now;
	difference *= lineHeight;
	Rect r = new Rect(spacing,spacing,width-spacing*2,getScreenRows()*lineHeight);
	if (r.height > difference){
		//pageColor = new Color((int)(Math.random()*155)+100,(int)(Math.random()*155)+100,(int)(Math.random()*155)+100);
		if (now > was){
			scrollAndRepaint(r.x,r.y+difference,r.width,r.height-difference-lineHeight,r.x,r.y);
			paintLine(null,now+getScreenRows()-1);
			paintLine(null,now+getScreenRows());
		}else{
			scrollAndRepaint(r.x,r.y,r.width,r.height-difference,r.x,r.y+difference);
			paintLine(null,now+getScreenRows());
		}
		return false;
	}else
		return true;
}
//-------------------------------------------------------------------
private boolean hscroll(int was,int now)
//-------------------------------------------------------------------
{
	if (was == now) return false;
	int difference = now > was ? now-was : was-now;
	Rect r = new Rect(spacing,spacing,width-spacing*2,getScreenRows()*lineHeight);
	if (r.width > difference){
		//pageColor = new Color((int)(Math.random()*155)+100,(int)(Math.random()*155)+100,(int)(Math.random()*155)+100);
		if (now > was){
			scrollAndRepaint(r.x+difference,r.y,r.width-difference,r.height,r.x,r.y);
		}else
			scrollAndRepaint(r.x,r.y,r.width-difference,r.height,r.x+difference,r.y);
		paintLine(null,curState.firstLine+getScreenRows());
		return false;
	}else
		return true;
}
	//==================================================================
	public void doScroll(int which,int action,int value)
	//==================================================================
	{
		clearCursor();
		boolean repaint = true;
		if (which == IScroll.Vertical) {
			int was = curState.firstLine, dl = 0;
			if (action == IScroll.ScrollHigher) dl = 1;
			else if (action == IScroll.ScrollLower) dl = -1;
			else if (action == IScroll.PageHigher) dl = getScreenRows();
			else if (action == IScroll.PageLower) dl = -getScreenRows();
			else if (action == IScroll.TrackTo) dl = value-was;
			dl += curState.firstLine;
			if (dl > getNumLines()-getScreenRows()) dl = getNumLines()-getScreenRows();
			if (dl < 0) dl = 0;
			curState.firstLine = dl;
			repaint = vscroll(was,dl);
		}else {
			int sh = (minXScroll*width)/100;
			if (sh < 10) sh = 10;
			int was = curState.xShift, dx = 0;
			if (action == IScroll.ScrollHigher) dx = sh;

			else if (action == IScroll.ScrollLower) dx = -sh;
			else if (action == IScroll.PageHigher) dx = width-10;
			else if (action == IScroll.PageLower) dx = -(width-10);
			else if (action == IScroll.TrackTo) dx = value-was;
			dx += curState.xShift;
			int mw = getTextWidth()+leftMargin+spacing*2;
			if (forcedActualWidth <= 0)
				if (dx+width-spacing > mw) dx = mw-width;
			if (dx < 0) dx = 0;
			curState.xShift = dx;
			repaint = hscroll(was,dx);
		}
		if (disableCursor && which == IScroll.Vertical) newCursorPos(0,curState.firstLine,false);
		if (repaint) {
			repaintDataNow();
		}
		if (ss != null) ss.updateScroll(which);
	}
	/*
		public int getActual(int which)
	{
		if (which == IScroll.Vertical) return getNumLines();
		else {
			//return
			int ac = getTextWidth();
			if (ac == 0) ac = 1;
			return ac;
		}
	}
	public int getVisible(int which,int forSize)
	{
		if (which == IScroll.Vertical) {
			int h = getLineHeight();
			if (h != 0) return (forSize-spacing*2)/h;
			else return 1;//5;
		}
		else return forSize-spacing*2;
	}
	public boolean needScrollBar(int which,int forSize)
	{
		return getVisible(which,forSize) < getActual(which);
	}
public boolean canGo(int orientation,int direction,int position)
{
	return true;
}
*/

//==================================================================
//This marks the end of the implementation of ScrollClient
//==================================================================
//==================================================================
// If you don't want any scroll bar functionality, then replace
// the body of checkScrolls() and updateScrolls() with {}
//==================================================================
public void checkScrolls() {if (ss != null) ss.checkScrolls();}
//==================================================================
protected void updateScrolls()
//==================================================================
{
	if (ss == null) return;
	ss.updateScroll(Horizontal);
	ss.updateScroll(Vertical);
}

boolean hasCursor, cursorOn, leaveCursor;
//==================================================================
public void ticked(int id,int elapsed)
//==================================================================
{
	if (blinkId == id){
		if (!PenEvent.tipIsDisplayed() && Gui.requestPaint(this)) paintCursor(null);
		blinkId = mApp.requestTick(this,500);
	}
	super.ticked(id,elapsed);
}
//===================================================================
public void popupMenuEvent(Object selectedItem)
//===================================================================
{
	if (selectedItem.toString().equals("Select All")) selectAll();
	else super.popupMenuEvent(selectedItem);
}

//==================================================================
public void selectAll()
//==================================================================
{
	clearSelection();
	curState.selStartLine = curState.selStartPos = 0;
	curState.cursorLine = curState.selEndLine = numLines-1;
	curState.cursorPos = curState.selEndPos = getLine(numLines-1).length();
	if (!newCursorPos(curState.selEndPos,curState.selEndLine,true))
		repaintDataNow();
}
//===================================================================
public boolean checkSipCoverage()
//===================================================================
{
	if (dontWantPopup) return false;
	return super.checkSipCoverage();
}
//
//==================================================================
public void gotFocus(int how)
//==================================================================
{
	justGotFocus = (how == ByPen);
	boolean newFocus = !inFocus;
	inFocus = true;
	if (inputFlags == -1){
		inputFlags = 0;
		if (useNativeTextInput) inputFlags |= FLAG_USE_NATIVE|FLAG_PASSIVE;
	}
	//
	hasCursor = !disableCursor;
	//
	if ((inputFlags & FLAG_PASSIVE) != 0) {
		if (((inputFlags & FLAG_INPUT_ON_FOCUS) != 0) && newFocus)
			if (startActiveInput(selectAllOnFocus)){
				super.gotFocus(how);
				return;
			}else{
				startNativeOnPaint = true;
			}
		repaintNow();
		super.gotFocus(how);
		return;
	}
	checkScrolls();
	String text = getText();
	boolean ch = !text.equals(oldText);
	oldText = text;
	boolean sa = false;
	if (!checkSipCoverage()){
		sa = selectAllOnFocus;
		if (!sa && curState.hasSelection() && how != ByRequest && how != ByFrameChange) clearSelection();
		if (ch) notifyDataChange();
	}
	startLocalInput(sa);
	super.gotFocus(how);
	//ewe.sys.Vm.setSIP(1);
}
//==================================================================
public void lostFocus(int how)
//==================================================================
{
	inFocus = false;
	blinkId = 0;
	if (menuIsActive()) return;
	//noSelection();
	//newCursorPos(0,0,false);
	checkScrolls();
	clearCursor();
	hasCursor = false;
	if (!getText().equals(oldText))
		notifyDataChange();
	if ((inputFlags & FLAG_PASSIVE) != 0)
		repaintNow();
}
//
// This is used to start the native input if possible. If it is not possible
// then local input is used instead.
//
//-------------------------------------------------------------------
protected void setNativeInputFlags(TextInputParameters tip)
//-------------------------------------------------------------------
{
	tip.flags |= TextInputParameters.FLAG_MULTILINE;
	if (wrapToScreenSize) tip.flags |= TextInputParameters.FLAG_AUTO_WRAP;
	if (wantReturn) tip.flags |= TextInputParameters.FLAG_WANT_RETURN;
}
protected textPadState curState = new textPadState();
//==================================================================
protected boolean fix()
//==================================================================
{
	if (width == 0 || height == 0) return false;
	if (lines == null) splitLines(getAvailableWidth());
	textPadState tas = curState.getCopy();
	FontMetrics fm = getFontMetrics();
	// Temporarily removed this.
	//if (!hasCursor) tas.cursorLine = tas.cursorPos = tas.firstLine = tas.xShift = 0;
//..................................................................
	if (tas.cursorLine >= numLines) {
		tas.cursorLine = numLines-1;
		tas.cursorPos = getLine(tas.cursorLine).length();
	}
	if (tas.cursorLine < 0) tas.cursorLine = 0;
	DisplayLine dl = getLine(tas.cursorLine);
	String ln = dl.line;
	if (tas.cursorPos > ln.length()) tas.cursorPos = ln.length();
	if (tas.cursorPos < 0) tas.cursorPos = 0;
//..................................................................
	int sr = getScreenRows();
	int ys = (minYScroll*sr)/100;
	if (ys > sr) ys = sr;
	if (ys < 1) ys = 1;
	while (tas.cursorLine >= sr+tas.firstLine) tas.firstLine += ys;
	while (tas.cursorLine < tas.firstLine) tas.firstLine -= ys;
	if (tas.firstLine < 0) tas.firstLine = 0;
//..................................................................
	int w = width-spacing*2-2;
	boolean formatted = ln.indexOf('\t') != -1;
	if (forcedActualWidth <= 0){
		int cw = 0;
		if (tas.cursorPos > 0){
			if (false && !formatted)
				cw = fm.getTextWidth(ewe.sys.Vm.getStringChars(ln),0,tas.cursorPos);
			else{
				posBuffer = getTextPositions(tas.cursorLine,dl).calculatedPositions;//fm.getFormattedTextPositions(ln,format,posBuffer);
				int p = tas.cursorPos-1;
				if (p > posBuffer.length-1) p = posBuffer.length-1;
				cw = posBuffer[p];
			}
		}
		if (cw < tas.xShift) tas.xShift = 0;
		int extra = wrapToScreenSize ? w-4 : (minXScroll*width)/100;
		if (extra < 4) extra = 4;
		if (cw > tas.xShift+w-4) tas.xShift = cw-w+extra;
	}
//..................................................................
// Old version, now defunct.
//..................................................................
/*	numDisplayed = 0;
	if (leftMost > text.length()) leftMost = 0;
	if (cursorPos < leftMost) leftMost = cursorPos;
	boolean gotCursor = false;
	FontMetrics fm = getFontMetrics();
	String s = getDisplay(text);
	for (int i = leftMost; i<=text.length(); i++){
		int need = 2;
		if (i != text.length()) need = fm.getCharWidth(s.charAt(i));
		if (need+w > width){
			if (gotCursor) break;
			else if (numDisplayed != 0){
				w -= fm.getCharWidth(s.charAt(leftMost));
				leftMost++;
				numDisplayed--;
			}
		}
		if (i != text.length()) {
			numDisplayed++;
			w += need;
		}
		if (i == cursorPos) gotCursor = true;

	}
*/
	textPadState t2 = curState;
	curState = tas;
	return curState.displayChanged(t2);
}


/**
 * This calls newCursorPos(int ch,int ln,boolean takeSel,boolean leaveSel) with leaveSel set to false.
 */
//===================================================================
public boolean newCursorPos(int ch,int ln,boolean takeSel)
//===================================================================
{
	return newCursorPos(ch,ln,takeSel,false);
}
/**
* This is used to move the cursor position AND update the display accordingly.
* @param ch The character position on the line.
* @param ln The line.
* @param takeSel If this is true then the selection will be extended to the new cursor position.
If it is false AND leaveSel is also false, any selected area will be deselected. If this is false AND
leaveSel is true, the selection will be left as is.
* @param leaveSel If this is true AND takeSel is false, then the selection will not be altered.
* @return true if the display was repainted during the function call, false if it has not been repainted.
*/
//===================================================================
public boolean newCursorPos(int ch,int ln,boolean takeSel,boolean leaveSel)
//===================================================================
{
	boolean repainted = false;
	boolean cleared = false;
	if (!takeSel) clearCursor();
	if (!takeSel && curState.hasSelection() && !leaveSel) {
		clearSelection();
		cleared = true;
	}
	int was = curState.cursorLine;
	curState.newCursorPos(ch,ln,takeSel,lines,numLines);
	if (fix()) {
		repaintDataNow();
		updateScrolls();
		repainted = true;
	}else if (cleared){
		repaintDataNow();
	}else if (takeSel){
		paintLines(was,curState.cursorLine);
	}
	paintCursor(null,true);
	return repainted;
}
/*
//------------------------------------------------------------------
protected void newText(String txt,int newCp,boolean redoData)
//------------------------------------------------------------------
{
	FontMetrics fm = getFontMetrics();

	int oldw = fm.getTextWidth(toString(getLine(curState.cursorLine)));
	getLine(curState.cursorLine).line = txt;
	int w = getFontMetrics().getTextWidth(txt);
	int tw = width-spacing*2;
	if ((w > tw && oldw <= tw) || (w < tw && oldw >=tw)) checkScrolls();
	boolean rp = newCursorPos(newCp,curState.cursorLine,false);
	if (!rp) {
		clearCursor();
		paintLine(null,curState.cursorLine);
	}
}
*/



/**
 * Return the index of the top line of the display in the complete list of lines.
 */
//===================================================================
public int getTopLine()
//===================================================================
{
	return curState.firstLine;
}
/**
* Return how many pixels the display is shifted to the right.
**/
//===================================================================
public int getLeftPosition()
//===================================================================
{
	return curState.xShift;
}
/**
 * Get the line and character index of the character at the point specified (relative
 * to the origin of the mTextPad).
 * @param onControl The x and y co-ordinate within the mTextPad.
 * @param bestGuess if this is true and the point is not on a character, the location of the closest character
 * is returned.
 * @param halfWay if this is true then once the point is more than half-way through a character
 * it is considered on the next character.
 * @return The location of the character ( x = character index in line, y = line index), or
 * null if the point is not actually on a character and bestGuess is false
*/
//===================================================================
public Point getCharAt(Point onControl,boolean bestGuess,boolean halfWay)
//===================================================================
{
	FontMetrics fm = getFontMetrics();
	if (!bestGuess)
		if (onControl.y <= spacing || onControl.x <= spacing ||
			  onControl.y >= height-spacing || onControl.x >= width-spacing)
				return null;
	Point p = new Point(0,0);
	//if (lines == null) lines = new DisplayLine[0];
	int h = getLineHeight();
	int px = onControl.x-spacing+curState.xShift;
	int py = onControl.y-spacing+curState.firstLine*h;
	p.y = py/h;
	if (!bestGuess && p.y >= getNumLines()) return null;
	p.x = 0;
	DisplayLine dl = getLine(p.y);
	String s = toString(dl);
	int i = 0, w = 0;
	int last = 0;
	FormattedTextSpecs f;
	posBuffer = (f = getTextPositions(p.y,dl)).calculatedPositions;
	w = f.leftMargin+f.firstCharPosition;
	if (px < w && !bestGuess) return null;
	int len = s.length();
	for (; i<=len; i++){
		if (i == len)
			if (bestGuess) break;
			else return null;
		int fw = posBuffer[i]-last;
		if (halfWay){
			w += fw/2;
			if (w >= px) break;
			w += (-fw/2)+fw;
		}else{
			w += fw;
			if (w >= px) break;
		}
		last = posBuffer[i];
	}
	p.x = i;
	return p;
}

//-------------------------------------------------------------------
protected Point getPenChar(Point onControl)
//-------------------------------------------------------------------
{
	return getCharAt(onControl,true,true);
}
/**
 * Get the line and character index of the character at the point specified (relative
 * to the origin of the mTextPad).
 * @param onControl The x and y co-ordinate within the mTextPad.
 * @return The location of the character ( x = character index in line, y = line index), or
 * null if the point is not actually on a character.
 */
//===================================================================
public Point getCharAt(Point onControl)
//===================================================================
{
	return getCharAt(onControl,false,false);
}
//------------------------------------------------------------------
protected void clearCursor() {if (cursorOn) {leaveCursor = false; paintCursor(null);}}
//------------------------------------------------------------------
/**
 * Get the line at the specified index. Any line feeds or ending spaces are added to the end so
 * that it represents the true line.
 * @param index The index of the line.
 * @return The line text, or null if the line index is invalid.
 */
//===================================================================
public String getLineAt(int index)
//===================================================================
{
	return getLineAt(index,true);
}
/**
 * Get the line at the specified index. Any line feeds or ending spaces are added to the end so
 * that it represents the true line IF trueLine is true.
 * @param index The index of the line.
 * @param trueLine if this is true then ending line feeds or spaces are added to the line. If not,
 * then the line is returned as it is on the display.
 * @return The line text, or null if the line index is invalid.
 */
//===================================================================
public String getLineAt(int index,boolean trueLine)
//===================================================================
{
	DisplayLine dl = getLine(index);
	if (dl == null) return null;
	String ret = dl.line;
	if (trueLine){
		if ((dl.flags & dl.ENDS_WITH_SPACE) != 0) ret += " ";
		else if ((dl.flags & dl.ENDS_WITH_NEWLINE) != 0) ret += "\n";
	}
	return ret;
}
/**
 * Return the length of the line in characters, with or without the ending line feed.
 * @param trueLength if this is true, then any ending line feed is also included in the length,
 * otherwise only the number of displayed characters is counted.
 * @return the length of the line or -1 if the line index is invalid.
 */
//===================================================================
public int getLineLength(int index, boolean trueLength)
//===================================================================
{
	DisplayLine dl = getLine(index);
	if (dl == null) return -1;
	int ret = dl.line.length();
	if (trueLength)
		if ((dl.flags & dl.ENDS_WITH_SPACE) != 0) ret++;
		else if ((dl.flags & dl.ENDS_WITH_NEWLINE) != 0) ret++;
	return ret;
}
/**
 * Get the index of the character in the original text, given the line index and character index
 * on screen.
 * @param lineIndex The line it is displayed on screen.
 * @param charIndex The character index within the on-screen line.
 * @return The index of the character in the original text or -1 if the character is invalid.
 */
//===================================================================
public int getTrueCharIndex(int lineIndex, int charIndex)
//===================================================================
{
	if (lines == null || lineIndex < 0 || charIndex < 0) return -1;
	int idx = 0, line = 0;
	for (DisplayLine dl = lines;;dl = (DisplayLine)dl.next){
		if (dl == null) return -1;
		int len = dl.line.length();
		if ((dl.flags & (dl.ENDS_WITH_NEWLINE|dl.ENDS_WITH_SPACE)) != 0)
			len++;
		if (lineIndex == line){
			if (charIndex >= len) return -1;
			return idx+charIndex;
		}
		idx += len;
		line++;
	}
}
/**
 * Get the index of the character in the original text, given the line index and character index
 * on screen.
 * @param charPosition The character position on screen where y = the line it is displayed on screen,
 * and x is the character index within the on-screen line.
 * @return The index of the character in the original text or -1 if the character is invalid.
 */
//===================================================================

public int getTrueCharIndex(Point charPosition)
//===================================================================
{
	if (charPosition == null) return -1;
	return getTrueCharIndex(charPosition.y, charPosition.x);
}
/**
* Get the DisplayLine at the specified index. Once you can get following
* display lines by using the "next" member of the returned DisplayLine.
**/
//===================================================================
public DisplayLine getLine(int index)
//===================================================================
{
	if (lines == null) return null;
	if (index < 0 || index >= numLines) return null;
	return (DisplayLine) DisplayLine.getNext(lines,index);
}

protected DisplayLine getLine() {return getLine(curState.cursorLine);}


Point pressPoint = new Point(0,0);

//===================================================================
public boolean noSelection()
//===================================================================
{
	if (!curState.hasSelection()) return false;
	clearSelection();
	return true;
}
//===================================================================
public Object getSelection()
//===================================================================
{
	if (!curState.hasSelection()) return null;
	int sl = curState.selStartLine, sp = curState.selStartPos;
	int el = curState.selEndLine;
	if (sl == el) return toString(getLine(sl)).substring(curState.selStartPos,curState.selEndPos);
	String ret = "";
	DisplayLine dl = getLine(sl);
	if (dl == null) return "";
	ret += dl.line.substring(curState.selStartPos);
	if ((dl.flags & dl.ENDS_WITH_NEWLINE) != 0) ret += '\n';
	for (int i = sl+1; i<el && i<numLines; i++){
		dl = (DisplayLine)dl.next;
		if (dl == null) break;
		ret += dl.line;
		if ((dl.flags & dl.ENDS_WITH_NEWLINE) != 0) ret += '\n';
	}
	if (dl != null) dl = (DisplayLine)dl.next;
	if (dl != null) ret += dl.line.substring(0,curState.selEndPos);
	/*
	Vector v = new Vector();
	if (sl != el) {
		v.add(getLine(sl).line.substring(curState.selStartPos,getLine(sl).length()));
		for (int i = sl+1; i<el && i<numLines; i++)
			v.add(getLine(i));
		v.add(getLine(el).line.substring(0,curState.selEndPos));
	}else
		v.add(getLine(sl).line.substring(curState.selStartPos,curState.selEndPos));
	String [] ret = new String[v.size()];
	v.copyInto(ret);
	*/
	return ret;
}

/**
* This deletes any selected area and updates the cursor, but it does not do a resplitting of the
* lines nor does it do a repaint. It returns true if there was a selection to be removed.<br>The
* cursor will be positioned immediately before the text after the selection area.
**/
//-------------------------------------------------------------------
protected boolean removeSelection()
//-------------------------------------------------------------------
{
	if (!curState.hasSelection()) return false;
	DisplayLine first = getLine(curState.selStartLine);
	DisplayLine prev = (DisplayLine)first.prev;
	DisplayLine last = getLine(curState.selEndLine);
	curState.cursorLine = curState.selStartLine;
	curState.cursorPos = curState.selStartPos;
	if (first == last) {
		String nt = first.substring(0,curState.selStartPos)+first.line.substring(curState.selEndPos);
		first.line = nt;
	}else{
		first.line = first.substring(0,curState.selStartPos)+last.line.substring(curState.selEndPos);
		first.flags = last.flags;
		DisplayLine.removeSection(first.next,last);
		int deleted = curState.selEndLine-curState.selStartLine;
		numLines -= deleted;
	}
	curState.selStartLine = -1;
	return true;
}
/**
* This re-arrange the section the cursor is on and following sections up to lastSection.
* It will do a repaint if asked.
* If lastSection is null only the cursor section will be re-arranged.
**/
//-------------------------------------------------------------------
protected void rearrange(DisplayLine firstLine,int indexOfFirst,DisplayLine lastLine,boolean repaint)
//-------------------------------------------------------------------
{
	DisplayLine start = firstLine.getSection(indexOfFirst);
	indexOfFirst = start.lineIndex;
	DisplayLine end = start;
	if (lastLine != null){
		int down = 0;
		for (DisplayLine cur = start; cur != lastLine && cur != null; cur = (DisplayLine)cur.next)
			down++;
		end = lastLine.getSection(start.lineIndex+down);
	}
	String text = DisplayLine.concatenate(start,(DisplayLine)end.getNext(end,end.numberInSection-1));
	DisplayLine nl = DisplayLine.split(text,getFontMetrics(),getAvailableWidth(),wrapToScreenSize ? 0 : DisplayLine.SPLIT_NO_WRAP,format);
	replaceLines(start,(end.lineIndex+end.numberInSection)-start.lineIndex,nl);
	if (repaint) paintLinesFrom(indexOfFirst);
	/*
	int up = theLine.countToSectionStart();
	DisplayLine first = (DisplayLine)DisplayLine.getPrev(theLine,up);
	if (lastSection == null) lastSection = theLine;
	int down = lastSection.countToSectionEnd();
	DisplayLine last = (DisplayLine)DisplayLine.getNext(lastSection,down);
	String text = DisplayLine.concatenate(first,last);

	int lastPos = getPosInSection(first,up);
	replaceLines(first,up+down+1,nl);
	int index = curState.cursorLine-up;
	positionCursor(nl,lastPos,index,false);
	*/
}
/**
* This will replace the current selection with the provided Object. The Object can
* be a string or an array of strings (each representing a line). If there is no
* selection the data will be inserted at the cursor position. The cursor will always
* be moved to the end of the inserted data.
**/
//===================================================================
public boolean replaceSelection(Object with)
//===================================================================
{
	clearCursor();
	if (with == null) return deleteSelection();
	removeSelection();
	String newText = "";
	if (with instanceof Object []){
		Object [] got = (Object [])with;
		for (int i = 0; i<got.length; i++){
			if (i != 0) newText += "\n";
			if (got[i] != null)
				newText += got[i].toString();
		}
	}else
		newText = with.toString();
	if (newText == null) newText = "";
	int cl = curState.cursorLine;
	int cp = curState.cursorPos;
	DisplayLine dl = getLine(cl);
	DisplayLine ss = dl.getSection(cl);
	String was = dl.line;
	dl.line = was.substring(0,cp)+newText+was.substring(cp);
	int whichSection = 0, whichPos = ss.getPositionInSection(cl,cp);
	char [] tx = ewe.sys.Vm.getStringChars(newText);
	for (int i = 0; i<tx.length; i++){
		if (tx[i] == '\n') {
			if (i == tx.length-1) break;
			whichSection++;
			whichPos = 0;
		}else
			whichPos++;
	}
	rearrange(dl,cl,dl,true);
	dl = getLine(cl);
	for (int i = 0; i<whichSection;){
		if (dl == null) break;
		if ((dl.flags & dl.ENDS_WITH_NEWLINE) != 0) {
			i++;
		}
		dl = (DisplayLine)dl.next;
		cl++;
	}
	if (dl != null){
		dl = dl.getSection(cl);
		Dimension d = dl.positionInSection(whichPos,null,false);
		curState.cursorLine = d.height;
		curState.cursorPos = d.width;
	}
	if (fix()) repaintDataNow();
	checkScrolls();
	return true;
}

//===================================================================
public boolean hasSelection() {return curState.hasSelection();}
//===================================================================
/**
* This clears the selection but does not update the screen. Call repaintNow()
* to update the screen.
**/
//===================================================================
public void clearSelection()
//==================================================================
{
	clearCursor();
	curState.selectionEnabled = false;
	for (int i = 0; i<getScreenRows(); i++)
		if (curState.isInSelection(i+curState.firstLine)) paintLine(null,i+curState.firstLine);
	curState.selectionEnabled = true;
	curState.selStartLine = -1;
}
private boolean shouldDeselect = false;
private Point pressedChar;
//==================================================================
public void penPressed(Point where)
//==================================================================
{
	if (menuIsActive()) menuState.closeMenu();
	Point loc = pressedChar = getPenChar(where);
	if (loc == null) return;
	shouldDeselect = (!justGotFocus || !selectAllOnFocus);
	justGotFocus = false;
	newCursorPos(loc.x,loc.y,false,true);
	lastDrag = pressPoint = loc;
}
//===================================================================
public void penDoubleClicked(Point where)
//===================================================================
{
	if (curState.hasSelection()) clearSelection();
	DisplayLine line = getLine(curState.cursorLine);
	if (line == null) return;
	int cp = curState.cursorPos;
	if (cp >= line.length()) cp--;
	if (cp < 0) return;
	int i = cp-1;
	for (;i >= 0; i--)
		if (line.line.charAt(i) == ' ') break;
	cp = i+1;
	curState.cursorPos = curState.selStartPos = curState.selEndPos = cp;
	curState.selStartLine = curState.selEndLine = curState.cursorLine;
	for (;cp < line.length(); cp++)
		if (line.line.charAt(cp) == ' ') {
			cp++;
			break;
		}
	newCursorPos(cp,curState.cursorLine,true);
}
//===================================================================
public void penReleased(Point where)
//===================================================================
{
	if (menuIsActive()) return;
	if (shouldDeselect) clearSelection();
}
/*
//==================================================================
public boolean doMenu(Point p)
//==================================================================
{
	if (menuState != null && !menuIsActive()) {
		menuState.doShowMenu(p,false,null);
		return true;
	}
	return false;
}
*/
Point lastDrag;
//===================================================================
public void startDragging(DragContext dc)
//===================================================================
{
	if (menuIsActive()) return;
	if (pressedChar != null){
		if (curState.isInSelection(pressedChar.y,pressedChar.x)){
			setCursor(ewe.sys.Vm.IBEAM_CURSOR);
			startDragAndDrop(getSelection(),false,(dc.modifiers & IKeys.CONTROL) != 0);
			dc.setDropOptions(false,true);
			return;
		}
	}
	clearSelection();
	if (pressedChar != null) {
		curState.cursorLine = pressPoint.y;
		curState.cursorPos  = pressPoint.x;
		fix();
		curState.selEndLine = curState.selStartLine = curState.cursorLine;
		curState.selEndPos = curState.selStartPos = curState.cursorPos;
	}
	dragged(dc);
}
//==================================================================
public void dragged(DragContext dc)
//==================================================================
{
	if (menuIsActive()) return;
	shouldDeselect = false;
	Point now = getPenChar(dc.curPoint);
	if (now == null) return;
	if (lastDrag != null)
		if (now.x == lastDrag.x && now.y == lastDrag.y) return;
	//boolean [] wasIn = new boolean[numLines];
	//for (int i = 0; i<numLines; i++)
		//wasIn[i] = curState.isInSelection(i);
	lastDrag = now;
	/*
	if (now.y < pressPoint.y) {
		curState.selStartLine = now.y;
		curState.selEndLine = pressPoint.y;
		curState.selStartPos = now.x;
		curState.selEndPos = pressPoint.x;
	}else if (now.y > pressPoint.y) {
		curState.selStartLine = pressPoint.y;
		curState.selEndLine = now.y;
		curState.selStartPos = pressPoint.x;
		curState.selEndPos = now.x;
	}else {
		curState.selStartLine = curState.selEndLine = pressPoint.y;
		if (now.x < pressPoint.x) {
			curState.selStartPos = now.x;
			curState.selEndPos = pressPoint.x;
		}else{
			curState.selStartPos = pressPoint.x;
			curState.selEndPos = now.x;
		}
	}

	curState.fixSel(lines,numLines);
	*/
	newCursorPos(now.x,now.y,true);
	/*
	if (!newCursorPos(now.x,now.y,true)) {
		Graphics g = getGraphics();
		if (g == null) return;
		for (int i = curState.firstLine; i<curState.firstLine+getScreenRows() && i<numLines; i++) {
			if (curState.isInSelection(i) || wasIn[i])
				paintLine(g,i);
		}
	}
	*/
}

//-------------------------------------------------------------------
private String toString(DisplayLine dl)
//-------------------------------------------------------------------
{
	return dl == null ? "" : dl.line;
}

//-------------------------------------------------------------------
protected void replaceLines(DisplayLine start,int length,DisplayLine newLines)
//-------------------------------------------------------------------
{
	if (newLines == null) {
		newLines = new DisplayLine();
		newLines.line = "";
	}
	DisplayLine end = (DisplayLine)DisplayLine.getNext(start,length-1);
	DisplayLine lastToAdd = (DisplayLine)DisplayLine.getNext(newLines,-1);
	int addingLength = DisplayLine.countNext(newLines);
	if (start.prev != null) start.prev.next = newLines;
	newLines.prev = start.prev;
	if (end.next != null) end.next.prev = lastToAdd;
	lastToAdd.next = end.next;
	if (start == lines) lines = newLines;
	numLines += addingLength-length;
}
/*
//===================================================================
int getPosInSection(DisplayLine first,int down)
//===================================================================
{
	int pos = 0;
	for (int i = 0; i<down; i++){
		pos += first.length();
		first = (DisplayLine)first.next;
	}
	pos += curState.cursorPos;
	return pos;
}
//===================================================================
void positionCursor(DisplayLine first,int pos,int startLine,boolean onPrev)
//===================================================================
{
	while (pos != 0){
		if (first == null) return;
		if (pos >= first.length()){
			if (onPrev && pos == first.length()) break;
			startLine++;
			pos -= first.length();
			first = (DisplayLine)first.next;
		}else break;
	}
	curState.cursorLine = startLine;
	curState.cursorPos = pos;
}

*/
/**
* Replaces the current section with the new text. It returns the display line which
* is the start of the first section of the new text.<br>
* If newText is null or an empty string, it assumes the section is to be removed entirely.
* In this case it will return the start of the following section. If there is no following
* section, it will add on a section which will contain an empty string.<br>
* If the section being replaced is the first section, the "lines" variable will be modified
* to point to the new section.
**/
//===================================================================
DisplayLine replaceSection(DisplayLine oldStart,String newText,boolean repaint,boolean onPrev)
//===================================================================
{
	int oldPos = oldStart.getPositionInSection(curState.cursorLine,curState.cursorPos);
	if (newText == null) newText = "";
	DisplayLine newSection =
		newText.length() == 0 ? null : DisplayLine.split(newText,getFontMetrics(),getAvailableWidth(),wrapToScreenSize ? 0 : DisplayLine.SPLIT_NO_WRAP,format);
	boolean doReplace = true;
	if (newSection == null){
		newSection = (DisplayLine)DisplayLine.getNext(oldStart,oldStart.numberInSection);

		if (newSection == null) {
			doReplace = true;
			newSection = new DisplayLine();
			newSection.line = "";
		}else{
			doReplace = false;
			newSection.getSection(oldStart.lineIndex);
		}
	}
	numLines  += oldStart.replaceSection(doReplace ? newSection : null);
	if (oldStart == lines) lines = newSection;
	Dimension d = newSection.positionInSection(oldPos,null,onPrev);
 	curState.cursorLine = d.height;
	curState.cursorPos = d.width;
	if (fix() && repaint) repaintDataNow();
	else if (repaint) paintLinesFrom(oldStart.lineIndex);
	if (repaint)checkScrolls();
	return newSection;

}
//===================================================================
DisplayLine updateSection(DisplayLine theLine,int lineIndex,boolean repaint)
//===================================================================
{
	return updateSection(theLine,lineIndex,repaint,false);
}
/**
* Updates the section. The "line" element in a LineDisplay within this section
* may have been modified. It returns the start of the new section.
**/
//===================================================================
DisplayLine updateSection(DisplayLine theLine,int lineIndex,boolean repaint,boolean onPrev)
//===================================================================
{
	DisplayLine oldStart = theLine.getSection(lineIndex);
	String newText = oldStart.getSectionText();
	return replaceSection(oldStart,newText,repaint,onPrev);
}
//-------------------------------------------------------------------
protected String autoTab(String currentLine,String newLine)
//-------------------------------------------------------------------
{
	if (((inputState & STATE_AUTOTAB) == 0) || (currentLine == null)) return newLine;
	int num = 0;
	for (int i = 0; i<currentLine.length(); i++){
		if (currentLine.charAt(i) != ' ' && currentLine.charAt(i) != '\t') break;
		else num++;
	}
	return currentLine.substring(0,num)+newLine;
}
//==================================================================
public void onKeyEvent(KeyEvent ev)
//==================================================================
{
	ev.keypadToKey();
	if (ev.type != ev.KEY_PRESS) {
		super.onKeyEvent(ev);
		return;
	}
	if ((ev.modifiers & (IKeys.CONTROL|IKeys.SPECIAL)) == (IKeys.CONTROL)){
		if (ev.key != 24 && ev.key != 3 && ev.key != 22){
			super.onKeyEvent(ev);
			return;
		}else{
			ev.modifiers |= IKeys.SPECIAL;
		}
	}
	int flags = getModifiers(true);
	boolean shifted = ((ev.modifiers & IKeys.SHIFT) == IKeys.SHIFT);
	boolean ctrled = ((ev.modifiers & IKeys.CONTROL) == IKeys.CONTROL);
	boolean passive = (inputFlags & FLAG_PASSIVE) != 0;
	//
	if (!disableCursor && passive && !doingLocalInput){
		if (ev.key == IKeys.ENTER || ev.key == ' ' || ev.isActionKey()) {
			startActiveInput(selectAllOnFocus);
			return;
		}else{
			super.onKeyEvent(ev);
			return;
		}
	}
	//
	textPadState tas = curState;
	if (validator != null) {
		if (!validator.isValidKeyPress(ev)) {
			Sound.beep();
			return;
		}
	}
	if ((ev.key == IKeys.TAB) && (!shifted && !ctrled)) {
		ev.key = '\t';
		ev.modifiers &= ~IKeys.SPECIAL;
	}
	int cl = curState.cursorLine;
	int cp = curState.cursorPos;
	DisplayLine theLine = getLine();
	DisplayLine sectionStart = theLine.getSection(cl);
	String s = theLine.line;
	int sl = s.length();
	clearCursor();
	boolean shouldUpdate = false;

	if ((ev.modifiers & IKeys.SPECIAL) != 0) {
		if (ev.key == IKeys.BACKSPACE) {

			if (!(((flags & (Disabled|NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))){
				Sound.beep();
				return;
			}
			if (deleteSelection()) return;
			char toErase = 0;
			if (cp > 0){
				toErase = theLine.line.charAt(cp-1);
				theLine.line = s = s.substring(0,cp-1)+s.substring(cp);
				//..................................................................
				// Have to update the section.
				//..................................................................
				shouldUpdate = sectionStart.hasSectionChanged(theLine,getFontMetrics(),getAvailableWidth(),format) != -1;
				curState.cursorPos--;
			}else{
				shouldUpdate = true;
				DisplayLine prev = (DisplayLine)theLine.prev;
				if (prev == null) return;
				int len = prev.line.length()-1;
				if ((prev.flags & theLine.ENDS_WITH_NEWLINE) != 0) len++;
				prev.line = prev.line.substring(0,len);
				prev.line += theLine.line;
				prev.flags = theLine.flags;
				DisplayLine.removeSection(theLine,theLine);
				numLines--;
				sectionStart = prev.getSection(cl);
				theLine = prev;
				curState.cursorLine = --cl;
				curState.cursorPos = len;
			}
			if (shouldUpdate) updateSection(theLine,cl,true);
			else if (cp == sl) {
				clearCursor();
				paintLastChar(null,toErase);
			}else paintLine(null,cl);
			if (fix()) repaintDataNow();
	//..................................................................
		}else if (ev.key == IKeys.INSERT || (ev.key == 3 && ctrled)|| (ev.key == 22 && ctrled)){
	//..................................................................
			if (ctrled && curState.hasSelection() || (ev.key == 3 && ctrled)){
				toClipboard(false);
			}else if (shifted || (ev.key == 22 && ctrled)){
				fromClipboard();
			}else{
				clearCursor();
				inputState ^= STATE_OVERWRITE;
			}
			return;
	//..................................................................
		}else if (ev.key == IKeys.DELETE || (ev.key == 24 && ctrled)){
	//..................................................................
			if (!(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))){
				Sound.beep();
				return;
			}
			if ((shifted && curState.hasSelection()) || (ev.key == 24 && ctrled)){
				toClipboard(true);
				return;
			}
			if (deleteSelection()) return;
			if (cp < sl){
				theLine.line = s = s.substring(0,cp)+s.substring(cp+1);
				//..................................................................
				// Have to update the section.
				//..................................................................
				shouldUpdate = sectionStart.hasSectionChanged(theLine,getFontMetrics(),getAvailableWidth()) != -1;
			}else{
				shouldUpdate = true;
				DisplayLine next = (DisplayLine)theLine.next;
					boolean ewn = (theLine.flags & theLine.ENDS_WITH_NEWLINE) != 0;
					theLine.flags &= ~theLine.ENDS_WITH_NEWLINE;
					if (next == null) return;
					int ns = 0;
					if (next.line.length() != 0 && !ewn) ns = 1;
					next.line = theLine.line+next.line.substring(ns);

					DisplayLine.removeSection(theLine,theLine);
					numLines--;
					if (lines == theLine) lines = next;
					theLine = next;
				//}
				sectionStart = theLine.getSection(cl);
			}
			if (shouldUpdate) updateSection(theLine,cl,true);
			else paintLine(null,cl);
			fix();
		}else if (ev.key == IKeys.PAGE_UP){
			checkScrolls();
			if (disableCursor) doScroll(IScroll.Vertical,IScroll.PageLower,0);
			else newCursorPos(tas.cursorPos,tas.cursorLine-getScreenRows(),shifted);
		}else if (ev.key == IKeys.PAGE_DOWN){
			checkScrolls();
			if (disableCursor) doScroll(IScroll.Vertical,IScroll.PageHigher,0);
			else newCursorPos(tas.cursorPos,tas.cursorLine+getScreenRows(),shifted);
		}else if (ev.key == IKeys.END){
			checkScrolls();
			if (ctrled) newCursorPos(getLine(numLines-1).length(),numLines-1,shifted);
			else newCursorPos(getLine().length(),tas.cursorLine,shifted);
		}else if (ev.key == IKeys.HOME){
			checkScrolls();
			if (ctrled) newCursorPos(0,0,shifted);
			else newCursorPos(0,tas.cursorLine,shifted);
		}else if (ev.key == IKeys.LEFT){
			if (ctrled || disableCursor)
				doScroll(IScroll.Horizontal,IScroll.ScrollLower,0);
			else
				newCursorPos(tas.cursorPos-1,tas.cursorLine,shifted);
		}else if (ev.key == IKeys.RIGHT){
			if (ctrled || disableCursor)
				doScroll(IScroll.Horizontal,IScroll.ScrollHigher,0);
			else
				newCursorPos(tas.cursorPos+1,tas.cursorLine,shifted);
		}else if (ev.key == IKeys.UP){
			if (ctrled || disableCursor)
				doScroll(IScroll.Vertical,IScroll.ScrollLower,0);
			else{
				checkScrolls();
				newCursorPos(tas.cursorPos,tas.cursorLine-1,shifted);
			}
		}else if (ev.key == IKeys.DOWN){
			if (ctrled || disableCursor){
				doScroll(IScroll.Vertical,IScroll.ScrollHigher,0);
			}else{
				checkScrolls();
				newCursorPos(tas.cursorPos,tas.cursorLine+1,shifted);
			}
		}else if (ev.key == IKeys.ENTER || ev.isActionKey()) {
			if (wantReturn){
				ev.key = '\n';
				ev.modifiers &= ~IKeys.SPECIAL;
			}else{
				if (isSomeonesHotKey(ev)) return;
				if (tryNext(true)) return;
				notifyDataChange();
			}
		}else
			super.onKeyEvent(ev);
		if ((ev.modifiers & IKeys.SPECIAL) != 0) return;
	}
if ((ev.modifiers & IKeys.SPECIAL) == 0) {
	if (ev.key >= 32 || ((ev.key == '\n' || ev.key == '\t') && (((flags & (Disabled|NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0)))){
		if (!(((flags & (Disabled|NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))){
			Sound.beep();
			return;
		}
		if (ev.key == '\n') {
			if (!curState.hasSelection()){
				if (cp == 0){
					DisplayLine dl = new DisplayLine();
					dl.line = "";
					dl.flags |= dl.ENDS_WITH_NEWLINE;
					dl.next = theLine;
					dl.prev = theLine.prev;
					theLine.prev = dl;
					if (dl.prev != null) dl.prev.next = dl;
					if (lines == theLine) lines = dl;
					numLines++;
					paintLinesFrom(curState.cursorLine++);
					if (fix()) repaintDataNow();
					checkScrolls();
					return;
				}else if (theLine.next == null){
					if (cp == theLine.length() && ((theLine.flags & theLine.ENDS_WITH_NEWLINE) == 0)){
						theLine.flags |= theLine.ENDS_WITH_NEWLINE;
						DisplayLine dl = new DisplayLine();
						theLine.next = dl;
						dl.prev = theLine;
						dl.line = autoTab(s,"");
						numLines++;
						curState.cursorLine++;
						curState.cursorPos = dl.line.length();
						if (fix()) repaintDataNow();
						checkScrolls();
						return;
					}
				}
			}
		}
		char ch = (char)ev.key;
		/*
		Don't really need to do this. May leave this out some time.
		*/
		if ((ev.modifiers & IKeys.SHIFT) == IKeys.SHIFT)
			ch = Character.toUpperCase(ch);
		if (textCase == CASE_UPPER)
			ch = Character.toUpperCase(ch);
		else if (textCase == CASE_LOWER)
			ch = Character.toLowerCase(ch);
		else if (textCase == CASE_SENTENCE){
			if ((curState.cursorLine == 0 && curState.cursorPos == 0) || curState.selectionStartsFromFirstCharacter())
				ch = Character.toUpperCase(ch);
		}else if (textCase == CASE_NUMBERS && !Character.isDigit(ch)){
			Sound.beep();
			return;
		}

		if (curState.hasSelection()){
			replaceSelection(ewe.sys.Convert.toString(ch));
			return;
		}
		String add = ev.key == '\n' ? "\n"+autoTab(s,"") : (""+ch);
		if (((inputState & STATE_OVERWRITE) == 0) || (cp >= sl))
			theLine.line = s = s.substring(0,cp)+add+s.substring(cp,sl);

		else
			theLine.line = s = s.substring(0,cp)+add+s.substring(cp+1,sl);
		if (ev.key != '\n') curState.cursorPos += add.length();
		else curState.cursorPos += add.length()-1;
		//..................................................................
		// Have to update the section.
		//..................................................................
		shouldUpdate = sectionStart.hasSectionChanged(theLine,getFontMetrics(),getAvailableWidth()) != -1;
		if (shouldUpdate){
			updateSection(theLine,cl,true,ev.key != '\n' || add.length() != 1);
		}else{
			clearCursor();
			if (cp == sl) paintLastChar(null);
			else paintLine(null,cl);
			newCursorPos(cp+1,cl,false);
		}
	}else{
		super.onKeyEvent(ev);
	}
}else{
	super.onKeyEvent(ev);
}
}
//===================================================================
public boolean acceptsData(Object data,DragContext how)
//===================================================================
{
	if (!canEdit()) return false;
	return data != null;
}
Sprite dragOver = null;
//===================================================================
public void dataDraggedOver(Object data,Point p,PenEvent ev)
//===================================================================
{
	if (dragOver == null) dragOver = new Sprite(2,lineHeight,Color.DarkGray);
	if (!acceptsData(data,getDragAndDropContext())) dontAcceptDrop();
	else{
		Point where = getPenChar(p);
		if (where == null) dragOver.moveTo(null,0,0);
		else{
			Rect r = new Rect();
			getCharRect(where.x,where.y,r);
			dragOver.moveTo(this,r.x+spacing-curState.xShift,spacing+r.y-curState.firstLine*getLineHeight());

			if (curState.isInSelection(where.y,where.x)){
				dontAcceptDrop();
				return;
			}
		}
		willAcceptDrop();
	}
}
//===================================================================
public void dataDraggedOff(Object data)
//===================================================================
{
	if (dragOver != null) dragOver.moveTo(null,0,0);
	dragOver = null;
}

//===================================================================
public void onPenEvent(PenEvent ev)
//===================================================================
{
	if (ev.type == ev.PEN_MOVE){
		Point where = getPenChar(new Point(ev.x,ev.y));
		if (curState.isInSelection(where.y,where.x))
			setCursor(0);
		else
			setCursor(ewe.sys.Vm.IBEAM_CURSOR);
	}else
		super.onPenEvent(ev);
}
//===================================================================
public boolean dataDroppedOn(Object data,Point where,DragContext dc)
//===================================================================
{
	if (!canEdit()) return false;
	dataDraggedOff(data); //Remove ghost cursor.
	Point p = getPenChar(where);
	if (p == null) return false;

	boolean fromMeToMe = Window.dragAndDropSource == this;
	if (fromMeToMe){
		if (curState.isInSelection(p.y,p.x)) return false;
		if (!"copy".equals(dc.dropAction)){
			if (!curState.charPointAfterSelectionRemoved(p)) return false;
			deleteSelection();
		}
	}
	newCursorPos(p.x,p.y,false,false);
	takeData(data,dc);
	return true;
}
//===================================================================
public void dataAccepted(Control by,Object theData,String action)
//===================================================================
{
	dataDraggedOff(theData);
	if (by == this) return;
	if (!canEdit()) return;
	if (action.equals("move")) deleteSelection();
}
//-------------------------------------------------------------------
protected void dataTransferCancelled(Object data)
//-------------------------------------------------------------------
{
	dataDraggedOff(data);
	super.dataTransferCancelled(data);
}
/**
 * Get the character location of the cursor (caret).
 * @return A new Point where the x value represents the character index on the line the
	cursor is on (starting from 0) and the y value represents line index of the line the cursor is
	on.
 */
//===================================================================
public Point getCursorPosition()
//===================================================================
{
	return new Point(curState.cursorPos,curState.cursorLine);
}
/**
* This returns the selected area, or null if there is no selection. The x and y co-ordinates of the
* returned Rect mark the character and line index of the first character selected. Add the width of
* the returned Rect to the x value to find the index of the character just after the last
* selected character. Add the height of the returned Rect to the y value to find the index of the line
* of the last selected character.
**/
//===================================================================
public Rect getSelectionRange()
//===================================================================
{
	if (!curState.hasSelection()) return null;
	Rect r = new Rect(curState.selStartPos,curState.selStartLine,curState.selEndPos-curState.selStartPos,curState.selEndLine-curState.selStartLine);
	if (r.width == 0 && r.height == 0) return null;
	return r;
}
/**
 * This sets the current selection and will also move the cursor to the end of the selection.
 * @param startChar Character index of the start of the selection.
 * @param startLine Line index of the start of the selection.
 * @param endChar Character index of the end of the selection.
 * @param endLine Line index of the start of the selection.
 * @return True if the screen has been updated, false if not. If it returns false then you should
	call repaintNow() at some point to update the display correctly.
 */
//===================================================================
public boolean setSelectionRange(int startChar,int startLine,int endChar,int endLine)
//===================================================================
{
	newCursorPos(startChar,startLine,false);
	return newCursorPos(endChar,endLine,true);
}
/**
 * This locates the row and column index of a character from the original text in the
 * list of DisplayLines.
 * @param characterIndex The original index of the character.
 * @param dest a destination Dimension to hold the results. It can be null, in which case
 * a new one will be allocated and returned.
 * @return A Dimension object where the width variable denotes the index within the line and
 * the height variable donates the index of the line. It will return null if the index is not
 * in the orignal text.
 */
//===================================================================
public Dimension getIndexLocation(int characterIndex,Dimension dest)
//===================================================================
{
	getNumLines();
	return lines.locate(characterIndex,dest);
}
/**
 * This sets the current selection and will also move the cursor to the end of the selection.
 * @param originalCharacterIndex The index of the start character as it was in the original text.
 * @param numCharacters The number of characters to select.
 * @return True if the screen has been updated, false if not. If it returns false then you should
	call repaintNow() at some point to update the display correctly.
 */
//===================================================================
public boolean setSelectionRange(int originalCharacterIndex,int numCharacters)
//===================================================================
{
	Dimension st = getIndexLocation(originalCharacterIndex,null);
	Dimension ed = getIndexLocation(originalCharacterIndex+numCharacters,null);
	if (st == null || ed == null) return true;
	return setSelectionRange(st.width,st.height,ed.width,ed.height);
}
//==================================================================
public void appendText(String what,boolean moveToEnd)
//==================================================================
{
	text = text+what;
	if (text == null) text = "";
	splitLines(getAvailableWidth());
	//fix();
	if (!newCursorPos(0,numLines-1,false)) repaintDataNow();
	checkScrolls();
}
/**
* Scroll the display so that the specified line is now visible. A repaint is done.
* @param lineIndex The line to make visible.
* @param placeLineAtBottom If this is true the line will be placed at the bottom of the screen,
* otherwise it will be placed at the top.
*/
//===================================================================
public void scrollTo(int lineIndex,boolean placeLineAtBottom)
//===================================================================
{
	int num = getNumLines();
	if (lineIndex < 0 || lineIndex >= num) return;
	curState.firstLine = lineIndex;
	if (placeLineAtBottom) curState.firstLine -= getScreenRows()-1;
	if (curState.firstLine > getNumLines()-getScreenRows()) curState.firstLine = getNumLines()-getScreenRows();
	if (curState.firstLine < 0) curState.firstLine = 0;
	boolean repainted = newCursorPos(0,curState.firstLine,false);
	if (!repainted) repaintDataNow();
	if (ss != null) ss.updateScroll(Vertical);
}

/**
 * Get an Object that can be used to restore the text display to the same state later (using
	 setState(). This method essentially will return a reference to the data that is displayed
	 as the first line on-screen. Calling setState() will attempt to replace that line back on
	 top.
 */
//===================================================================
public Object getState()
//===================================================================
{
	int idx = getTrueCharIndex(curState.firstLine,0);
	return new ewe.sys.Long().set(idx);
}
/**
 * Reset the display to the state as provided by getState().
 * @param state The object returned by getState().
* @return true if successfully restored the state, false otherwise.
*/
//===================================================================
public boolean setState(Object state)
//===================================================================
{
	try{
		int idx = (int)((ewe.sys.Long)state).value;
		Dimension d = getIndexLocation(idx,null);
		if (d == null) return false;
		scrollTo(d.height,false);
		return true;
	}catch(ClassCastException c){
		throw new IllegalArgumentException();
	}
}

//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof TextEvent){
		if (ev.type == TextEvent.TEXT_ENTERED){
			doingNativeInput = false;
			TextEvent te = (TextEvent)ev;
			if ((te.flags & te.FLAG_TEXT_WAS_ENTERED) != 0){
				setText(fixCase(((TextEvent)ev).entered));
				notifyDataChange();
			}
		}
	}else
		super.onEvent(ev);
}
/**
This modifies the mTextPad to be a static text display - where the cursor is never
displayed and the cursor keys will scroll the display.<p>
The method returns the mTextPad itself if makeScrollBar is false. If makeScrollBar
is true it will return a ScrollablePanel() that contains this mTextPad. The returned
ScrollablePanel will be a VerticalScrollPanel on a SmartPhone, or a ScrollBarPanel on
any other platform.
**/
//===================================================================
public Control setAsStaticDisplay(boolean makeScrollBar)
//===================================================================
{
	modify(DisplayOnly,0);
	disableCursor = disableTextChanges = true;
	if (!makeScrollBar) return this;
	else return Gui.isSmartPhone ? new VerticalScrollPanel(this) : new ScrollBarPanel(this);
}
//-------------------------------------------------------------------
protected boolean paintConditionalChar(Graphics g, boolean highlight){return false;}
//-------------------------------------------------------------------
//##################################################################
}
//##################################################################

