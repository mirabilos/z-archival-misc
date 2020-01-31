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
import ewe.sys.*;

/**
* An mInput is a single line input.
* <p>
* Call <b>getText()</b> and <b>setText()</b> to get/set the text data.
* <p>
* Set <b>isPassword</b> true to indicate that '*' characters should be displayed
* instead of the input text.
* <p>
* Change the <b>columns</b> value to increase the preferred width of the input. This
* value is measured in characters.
**/
//##################################################################
public class mInput extends EditControl implements TimerProc,Selectable {
//##################################################################

public int spacing = 3;
{
	columns = 10;
	borderStyle = inputEdge;
	modify(HasData|WantDrag|WantHoldDown|ShowSIP|TakesKeyFocus,0);
	//setMenu(getClipboardMenu(null));
	setCursor(Vm.IBEAM_CURSOR);
}
/**
* A percent figure. Defaults to 30%
*/
public int minXScroll = 30;
/**
* Set this true so that only '*' characters are displayed.
**/
public boolean isPassword = false;
/**
* You can have the mInput fire an action event when the user single clicks the mInput by setting
* this value to 1, or double clicks by setting the value to 2. A value of 0 (the default) sets
* the mInput to not fire any actions with mouse clicks.
**/
public int clicksToFireAction = 0;
/**
* This is the maximum number of allowed characters, a value of -1 (the default) puts no limit
* on the number of characters.
**/
public int maxLength = -1;
/**
* If this is true then the mInput will fire an action event when enter is pressed, but will not
* automatically pass focus to the next control as it would usually do.
**/
public boolean wantReturn = false;
/**
* If this is true then a DataChange will be fired on each key.
**/
public boolean dataChangeOnEachKey = false;
/**
* This is the default edge to use on mInputs. By default it is EDGE_SUNKEN|BDR_OUTLINE, but
* you can change it to something different (e.g. BF_BOTTOM - to emulate a PalmOS type input).
**/
public static int inputEdge = EDGE_SUNKEN|BDR_OUTLINE;
/**
* This is a flag used with a generated DataChangeEvent to indicate that the data was changed by the
* enter key being pressed. This would indicate that an Action event will follow
* the DataChangeEvent.
**/
public static final int DATA_CHANGED_BY_ENTER = 0x1;
//==================================================================
public mInput() {text = "";}
public mInput(String txt) {text = txt;}
//==================================================================

private static Menu menu;

//-------------------------------------------------------------------
protected boolean doShowMenu(Point p)
//-------------------------------------------------------------------
{
	if (menu == null) menu = getClipboardMenu(null);
	menu.keepFrame = false;
	if (getMenu() == null) setMenu(menu);
	return super.doShowMenu(p);
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
public void setText(String txt)
//===================================================================
{
	int value = modify(Invisible,0);
	noSelection();
	super.setText(txt);
	newCursorPos(0);
	restore(value,Invisible);
	if ((modifiers & Invisible) == 0){
		repaintDataNow();
	}
}
//==================================================================
protected void calculateSizes()
//==================================================================
{
	if (columns == 0) columns = 20;
	Rect sz = Gui.getAverageSize(getFontMetrics(),1,columns,spacing,spacing);
	preferredWidth = sz.width;
	preferredHeight = sz.height;
}
//==================================================================
public void cursorToEnd(){newCursorPos(text.length());}
protected int leftMost,numDisplayed;
protected int startSel = 0, endSel = 0, cursorPos = 0;
protected boolean cursorOn = false;
protected boolean hasCursor = false;//true;
private boolean inCursor = false;
//==================================================================

//-------------------------------------------------------------------
protected Rect getDataRect(Rect dest) {return Rect.unNull(dest).set(spacing,spacing,width-spacing*2,height-spacing*2);}
//-------------------------------------------------------------------
protected boolean getCharRect(int which,Rect dest)
//-------------------------------------------------------------------
{
	dest.x = dest.y = 0;
	dest.width = dest.height = 0;
	if (which < leftMost || which > text.length()) return false;
	FontMetrics fm = getFontMetrics();
	dest.height = fm.getHeight();
	String s = getDisplay(text);
	for (int i = leftMost; i<=which; i++){
		dest.x += dest.width;
		if (i == text.length()) dest.width = 0;
		else dest.width = fm.getCharWidth(s.charAt(i));
	}
	if (dest.x >= width+spacing) return false;
	return true;
}
//===================================================================
public void ticked(int id,int elapsed)
//===================================================================
{
	//System.out.println(elapsed);
	if (blinkId == id){
		if (!PenEvent.tipIsDisplayed() && Gui.requestPaint(this)) paintCursor(null);
		blinkId = mApp.requestTick(this,500);
	}else
		super.ticked(id,elapsed);
}


//
//===================================================================
public void gotFocus(int how)
//===================================================================
{
	boolean newFocus = !inFocus;
	inFocus = true;
	justGotFocus = (how == ByPen || how == ByDeferredPen);
	if (inputFlags == -1){
		inputFlags = 0;
		if (useNativeTextInput) inputFlags |= getBestPassiveFlags();
	}
	//
	if ((modifiers & DisplayOnly) == 0)
		hasCursor =  true;
	//
	if ((inputFlags & FLAG_PASSIVE) != 0) {
		if (((inputFlags & FLAG_INPUT_ON_FOCUS) != 0) && newFocus)
			if (startActiveInput(how != ByFrameChange)){
				super.gotFocus(how);
				return;
			}else{
				startNativeOnPaint = true;
			}
		repaintNow();
		super.gotFocus(how);
		return;
	}
	//
	boolean ch = !text.equals(oldText);
	oldText = text;
	if (how != ByFrameChange)
		if (checkSipCoverage()){
			super.gotFocus(how);
			return;
		}
	if (ch) notifyDataChange();
	startLocalInput(how != ByFrameChange);
	super.gotFocus(how);
}

//-------------------------------------------------------------------
protected void stopAllInput()
//-------------------------------------------------------------------
{
	cursorOn = false;
	blinkId = 0;
	doingLocalInput = false;
	noSelection();
	newCursorPos(0);
}
//===================================================================
public void lostFocus(int how)
//===================================================================
{
	inFocus = false;
	blinkId = 0;
	cursorOn = false;
	doingLocalInput = false;
	if (menuIsActive()) return;
	hasCursor = false;
	noSelection();
	newCursorPos(0);
	if (!text.equals(oldText)){
		oldText = text;
		notifyDataChange();
	}
}
//===================================================================
public void stopActiveInput()
//===================================================================
{
	if ((inputFlags & FLAG_PASSIVE) != 0)
		stopAllInput();
}
//===================================================================
public void updateText(boolean generateEvent)
//===================================================================
{
	updateText(generateEvent,false);
}
//-------------------------------------------------------------------
protected void updateText(boolean generateEvent, boolean fromEnter)
//-------------------------------------------------------------------
{
	boolean doEvent = (!text.equals(oldText)) && generateEvent;
	oldText = text;
	if (doEvent){
		DataChangeEvent dev = new DataChangeEvent(DataChangeEvent.DATA_CHANGED,this);
		if (fromEnter) dev.flags |= DATA_CHANGED_BY_ENTER;
		notifyDataChange(dev);
	}
}

/**
This is the password character to use for the input. By default it is '*'
**/
public char passwordCharacter = PasswordDisplayMaker.defaultPasswordCharacter;
/**
* This String will be used to display characters when the isPassword option is used.
**/
//===================================================================
public static String hidden = "***********************";

private PasswordDisplayMaker myPassword;
/**
If isPassword is false, this returns 0, otherwise it will return the password character
to use for display.
**/
//===================================================================
public char getPasswordCharacter()
//===================================================================
{
	return isPassword ? passwordCharacter : 0;
}
/**
* This gets the String to represent on screen the data String provided.
* If the mInput is <b>not</b> a password, this will just return the provided
* String itself. Otherwise it returns a String of "hidden" characters equal
* in length to the data String.
* @param s The data String to display.
*/
//===================================================================
public String getDisplay(String s)
//===================================================================
{
	if (s == null) return "";
	if (!isPassword) return s;
	if (myPassword == null) myPassword = new PasswordDisplayMaker(passwordCharacter);
	return myPassword.getDisplay(s.length(),passwordCharacter);
}
protected static ImageBuffer itemBuffer = new ImageBuffer();
protected static ImageBuffer blockBuffer = new ImageBuffer();

//-------------------------------------------------------------------
protected void doPaintData(Graphics gr)
//-------------------------------------------------------------------
{
	try{
		int flags = getModifiers(true);
		boolean transparent = hasModifier(Transparent,false);
		transparent = false;
		if (hasModifier(PaintOutsideOnly,false)) return;
		fix();
		Rect _rect = getDim(null);
		if (width <= 0 || height <= 0) return;
		Graphics g = itemBuffer.get(width-4,height-4,true);
		Image img = itemBuffer.image;//new Graphics(img);

		if (transparent) g.copyRect(gr,0,0,width,height,-2,-2);

		getColors(hasCursor,flags);
		g.setColor(colors[1]);
	/*
		g.setColor(Color.White);
		if (!(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0)) || !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))) g.setColor(getBackground());
	*/
		if (!transparent) g.fillRect(0,0,width-4,height-4);
		g.setColor(colors[0]);
	/*
		g.setColor(getForeground());
		if (!(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0))) g.setColor(Color.DarkGray);
	*/
		if (!doingNativeInput){
			if (leftMost < text.length()){
				String s = text.substring(leftMost,text.length());//leftMost+numDisplayed);
				g.setFont(getFont());
				g.drawText(getDisplay(s),spacing-2,spacing-2);
			}
			int s = startSel, e = endSel;
			boolean showSelected = true;
			if (s >= e) showSelected = false;
			else if (/*s >= leftMost+numDisplayed ||*/ e-1 < leftMost) showSelected = false;
			if (showSelected){
				if (s <= leftMost) s = leftMost;
				//if (e > leftMost+numDisplayed) e = leftMost+numDisplayed;
				getCharRect(s,_rect);
				int x = _rect.x;
				getCharRect(e-1,_rect);
				int w = _rect.x-x+_rect.width;
				g.setColor(colors[3]);
				g.fillRect(x+spacing-2,spacing-2,w,_rect.height);
				g.setColor(colors[2]);
				g.setFont(getFont());
				String st = text.substring(s,e);
				g.drawText(getDisplay(st),x+spacing-2,spacing-2);
			}
			//g.setColor(new Color(0,0,0xff));
			//g.fillRect(0,0,width-4,height-4);
			//g.free();
			//gr.clearClip();
		}
		gr.drawImage(img,2,2);
		//ewe.sys.Vm.sleep(200);
		//img.free();
		/*
		if (cursorOn && hasCursor && !inCursor) {
			paintCursor(gr);
			cursorOn = false;
		}
		*/
		cursorOn = false;
		paintConditionalChar(gr,true);
	}finally{
		if (gr != null && startNativeOnPaint){
			startNativeOnPaint = false;
			startActiveInput(true);
		}
	}
}
//===================================================================
public void doPaint(Graphics g,Rect area)
//===================================================================
{
	int flags = getModifiers(true);
	if (!((flags & Invisible) == 0)) return;
	doBackground(g);
	if (!hasModifier(PaintDataOnly,false))
		g.draw3DRect(
			getDim(Rect.buff),
			borderStyle, //Graphics.EDGE_SUNKEN|Graphics.BDR_OUTLINE,
			(flags & DrawFlat) != 0,
			null,
			borderColor);
		//g.draw3DButton(_rect,true,null,((flags & DrawFlat) != 0),true);
	doPaintData(g);
	//paintCursor(g);
	//else
		//System.out.println(s+" to "+e+" Left: "+leftMost+" Displayed: "+numDisplayed);
}
//==================================================================
public void paintCursor(Graphics gr)
//==================================================================
{
	Graphics g = gr;
	if (g == null) g = getGraphics();
	if (g == null) return;
	if (inCursor) return;
	inCursor = true;
	try{
	if (hasCursor && amOnTopFrame()){
		int flags = getModifiers(true);
		getColors(hasCursor,flags);
		Rect r = new Rect(0,0,0,0);
		if (!getCharRect(cursorPos,r)){
			//ewe.sys.Vm.debug("No rect!");
			return;
		}
		Image i = new Image(2,r.height);
		Graphics gi = new Graphics(i);
		//gi.setColor(getForeground());
		gi.setColor(colors[0]);
		gi.fillRect(0,0,2,r.height);
		gi.free();
		boolean cc = g.canCopyFrom();
		if (!cc || true){
			Image i2 = new Image(2,r.height);
			gi = new Graphics(i2);
			gi.setColor(colors[1]);
			/*
			gi.setColor(Color.White);
			if (!(((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0)) || !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))) gi.setColor(getBackground());
			*/
			gi.fillRect(0,0,2,r.height);
			r.x += spacing;
			r.y += spacing;
			gi.translate(-r.x,-r.y);
			boolean co = cursorOn;
			cursorOn = false;
			if (cursorPos != text.length())  // I need this when not double buffering, but I don't know why yet.
				repaintNow(gi,r);
			cursorOn = co;
			gi.translate(r.x,r.y);
			r.x -= spacing; r.y -= spacing;
			if (!cursorOn) {
				gi.setDrawOp(g.DRAW_XOR);
				gi.drawImage(i,0,0);
			}else{
				//ewe.sys.Vm.debug("Nope");
			}
			gi.free();
			i.free();
			i = i2;
			g.setDrawOp(g.DRAW_OVER);
		}else{
			g.setDrawOp(g.DRAW_XOR);
		}
		g.drawImage(i,r.x+spacing,spacing);
		g.setDrawOp(g.DRAW_OVER);
		i.free();
		cursorOn = !cursorOn;
	}
	if (gr == null) g.free();
	}finally{
		inCursor = false;
	}

}
//==================================================================
public boolean paintLastChar(Graphics g) {return paintLastChar(g,false);}
public boolean paintLastChar(Graphics g,boolean eraseIt)
//==================================================================
{
	int l = text.length();
	if (l == 0) return true;
	Rect r = new Rect(0,0,0,0);
	getCharRect(l-1,r);
	getColors(hasCursor,0);
	if (!eraseIt) {
		g.setColor(colors[0]);
		g.setFont(getFont());
		String lc = getDisplay(text.substring(l-1,l));
		g.drawText(lc,r.x+spacing,r.y+spacing);
	}else {
		g.setColor(colors[1]);
		//if (!enabled(this) || !editable(this));
		boolean transparent = false;//hasModifier(Transparent,false);
		if (!transparent)
			g.fillRect(r.x+spacing,r.y+spacing,r.width,r.height);
		else return false;
	}
	return true;
}
private static char[] charBuff = new char[3];

//===================================================================
public boolean paintConditionalChar(Graphics gr,boolean underlined)
//===================================================================
{
	Graphics g = gr == null ? getGraphics() : gr;
	if (g == null) return false;
	try{
		if (conditionalCharIndex < 0 || conditionalCharIndex >= text.length()) return true;
		Rect r = new Rect(0,0,0,0);
		if (getCharRect(conditionalCharIndex,r)){
			getColors(hasCursor,0);
			g.setColor(colors[1]);
			g.fillRect(r.x+spacing,r.y+spacing,r.width,r.height);
			g.setColor(colors[0]);
			g.setFont(getFont());
			charBuff[0] = charBuff[1] = text.charAt(conditionalCharIndex);
			charBuff[2] = 0;
			g.drawText(charBuff,0,underlined ? 3:1,r.x+spacing,r.y+spacing);
			return true;
		}else
			return false;
	}finally{
		if (g != gr) g.free();
	}
}

//==================================================================
protected boolean fix()
//==================================================================
{
	int lastLeft = leftMost;
	if (!hasCursor) leftMost = cursorPos = 0;
	if (cursorPos < 0 || cursorPos > text.length()) cursorPos = text.length();
	int w = width-spacing*2-2;
	numDisplayed = 0;
	int cw = getFontMetrics().getCharWidth('0');
	if (cw == 0) cw = 10;
	int toAdd = ((width/cw)*minXScroll)/100;
	if (toAdd < 1) toAdd = 1;
	Rect r = new Rect(0,0,0,0);
	if (leftMost > text.length()) leftMost = 0;
	while(cursorPos < leftMost) leftMost -= toAdd;
	if (leftMost < 0) leftMost = 0;
	while(cursorPos > leftMost) {
		getCharRect(cursorPos,r);
		if (r.x+r.width < w) break;
		leftMost += toAdd;
	}
	if (cursorPos < leftMost) leftMost = cursorPos;
	/*
	boolean gotCursor = false;
	FontMetrics fm = getFontMetrics();
	String s = getDisplay(text);
	getCharRect(cursorPos,_
	*/
	/*
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
	return leftMost != lastLeft;
}
/**
* This is used to validate any new text. This implementation will first call the validator's
* isValidText() method (if a validator has been set) and then it will check that the text
* does not exceed the maxLength value (if maxLength is not negative).
**/
//===================================================================
public boolean validateText(String what)
//===================================================================
{
	if (validator != null && !validator.isValidText(what)) {
		Sound.beep();
		return false;
	}
	if (maxLength >= 0 && what.length() > maxLength){
		Sound.beep();
		return false;
	}
	return true;
}
public void newText(String what,int newCursorPos) {newText(what,newCursorPos,true);}
public void newText(String what,int newCursorPos,boolean repaintAll)
{
	int flags = getModifiers(true);
	if (!(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))) {
		Sound.beep();
		return;
	}
	if (!validateText(what)) return;
	text = what;
	if (dataChangeOnEachKey) updateText(true);
	Graphics g = getGraphics();
	if (g == null) return;
	boolean fixed = fix();
	if (cursorOn) paintCursor(g);
	cursorPos = newCursorPos;
	if (!repaintAll && cursorPos == text.length()){
		if (fixed) {
			repaintDataNow();
		}else {
			if (!paintLastChar(g)) repaintDataNow();
			//System.out.println("Last char only!");
		}
	}else
		repaintDataNow();
	if (conditionalCharIndex != -1) paintConditionalChar(g,true);
}
protected void moveCursorPos(int where,boolean takeSelection)
{
	if (cursorOn) paintCursor(getGraphics());
	boolean lostSelection = false;
	boolean wasLeft = false;
	boolean moveLeft = where < cursorPos;

	if (takeSelection) {
		if (!hasSelection()) startSel = endSel = cursorPos;
		if (cursorPos <= startSel) wasLeft = true;
	}
	if (!takeSelection) lostSelection = noSelection();
	cursorPos = where;
	if (cursorPos < 0) cursorPos = 0;
	if (cursorPos >= text.length()) cursorPos = text.length();
	if (takeSelection){
		if (wasLeft){
			if (moveLeft) startSel = cursorPos;
			else if (cursorPos > endSel) endSel = cursorPos;
			else startSel = cursorPos;
		}else{
			if (!moveLeft) endSel = cursorPos;
			else if (cursorPos < startSel) startSel = cursorPos;
			else endSel = cursorPos;
		}
	}
	if (fix() || lostSelection || takeSelection) repaintDataNow();
	else paintCursor(getGraphics());
}
protected void newCursorPos(int where,boolean takeSelection)
{
	if (cursorOn) paintCursor(getGraphics());
	boolean lostSelection = false;
	if (!takeSelection) lostSelection = noSelection();
	cursorPos = where;
	if (cursorPos < 0) cursorPos = 0;
	if (cursorPos >= text.length()) cursorPos = text.length();
	if (fix() || lostSelection || takeSelection) repaintDataNow();
	else paintCursor(getGraphics());
}
protected void newCursorPos(int where) {newCursorPos(where,true);}


//===================================================================
public boolean deleteSelection()
//===================================================================
{
	if (startSel == endSel) return false;
	String newText = text.substring(0,startSel);
	if (endSel < text.length()) newText += text.substring(endSel,text.length());
	text = newText;
	cursorPos = startSel;
	if (cursorPos > text.length()) cursorPos = text.length();
	noSelection();
	if (dataChangeOnEachKey) updateText(true);
	return true;
}
//===================================================================
public boolean replaceSelection(Object with)
//===================================================================
{
	deleteSelection();
	String in = clipboardToString(with);
	text = text.substring(0,cursorPos)+in+text.substring(cursorPos,text.length());
	cursorPos += in.length();
	if (dataChangeOnEachKey) updateText(true);
	return true;
}
//===================================================================
public Object getSelection()
//===================================================================
{
	if (!hasSelection() || isPassword) return new String();
	return text.substring(startSel,endSel);
}

//-------------------------------------------------------------------
protected void startLocalInput(boolean selectAll)
//-------------------------------------------------------------------
{
	doingLocalInput = true;
	boolean passive = (inputFlags & FLAG_PASSIVE) != 0;
	if (selectAll) this.selectAll();
	if (passive) repaintNow();
	blinkId = mApp.requestTick(this,500);
}
//
// This is used to start the native input if possible. If it is not possible
// then local input is used instead.
//
//-------------------------------------------------------------------
protected void setNativeInputFlags(TextInputParameters tip)
//-------------------------------------------------------------------
{
	if (isPassword) {
		tip.flags |= TextInputParameters.FLAG_PASSWORD;
		tip.passwordCharacter = passwordCharacter;
	}
	if (dataChangeOnEachKey) tip.flags |= tip.FLAG_EVENT_ON_EACH_KEY;
}

//===================================================================
public void onKeyEvent(KeyEvent ev)
//===================================================================
{
	ev.keypadToKey();
	if (ev.type != ev.KEY_PRESS){
		super.onKeyEvent(ev);
		return;
	}
	//
	ev = checkInputMethod(ev);
	//
	if (ev.key == IKeys.MENU && parent instanceof mComboBox){
		((mComboBox)parent).doActionKey(ev.key);
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
	boolean editable = (((flags & (Disabled|NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0));
	boolean shifted = ((ev.modifiers & IKeys.SHIFT) == IKeys.SHIFT);
	boolean ctrled = ((ev.modifiers & IKeys.CONTROL) == IKeys.CONTROL);
	if (validator != null) {
		if (!validator.isValidKeyPress(ev)) {
			Sound.beep();
			return;
		}
	}
	boolean passive = (inputFlags & FLAG_PASSIVE) != 0;
	if (passive && !doingLocalInput){
		editable = false;
		if (ev.key == IKeys.ENTER || ev.key == ' ' || ev.isActionKey()) {
			startActiveInput(true);
			return;
		}else{
			super.onKeyEvent(ev);
			return;
		}
	}
	//
	if ((ev.modifiers & IKeys.SPECIAL) != 0){
		if (ev.key == IKeys.ESCAPE){
			if (isSomeonesHotKey(ev)) return;
			text = oldText == null ? "" : oldText;
			if (!passive) selectAll();
			else stopActiveInput();
			postEvent(new ControlEvent(ControlEvent.CANCELLED,this));
			checkExitKey(ev);
			return;
		}else if (ev.key == IKeys.ENTER || ev.isActionKey()) {
			if (isSomeonesHotKey(ev)) return;
			if (!wantReturn) if (tryNext(true)) return;
			selectAll();
			updateText(true,true);
			notifyAction();
			//oldText = text;
		}else if (ev.key == IKeys.BACKSPACE && cursorPos != 0 && editable){
			if (deleteSelection()) 	repaintDataNow();
			else if (cursorPos == text.length()) {
				Graphics g = getGraphics();
				if (g == null) return;
				if (cursorOn) paintCursor(g);
				boolean didPaint = paintLastChar(g,true);
				text = text.substring(0,cursorPos-1);
				newCursorPos(cursorPos-1,false);
				if (!didPaint) repaintDataNow();
				if (dataChangeOnEachKey) updateText(true);
			}else
				newText(text.substring(0,cursorPos-1)+text.substring(cursorPos,text.length()),cursorPos-1);
		}else if (ev.key == IKeys.DELETE || ev.key == 24){
			if ((cursorPos < text.length() || startSel != endSel) && editable){
				if (shifted || ev.key == 24) {
					toClipboard(true);
					fix();
					repaintDataNow();
				}else if (deleteSelection()) 	repaintDataNow();
				else
					newText(text.substring(0,cursorPos)+text.substring(cursorPos+1,text.length()),cursorPos);
			}
		}else if (ev.key == IKeys.INSERT || ev.key == 3 || ev.key == 22){
			if ((ev.key == 3 || ctrled) && hasSelection()){
				toClipboard(false);
			}else if ((shifted || ev.key == 22) && editable){
				fromClipboard();
				fix();
				repaintDataNow();
			}
		}else if (ev.key == IKeys.END){
			moveCursorPos(text.length(),shifted);
		}else if (ev.key == IKeys.HOME){
			moveCursorPos(0,shifted);
		}else if (ev.key == IKeys.LEFT){
			moveCursorPos(cursorPos-1,shifted);
		}else if (ev.key == IKeys.RIGHT){
			moveCursorPos(cursorPos+1,shifted);
		}else{
			super.onKeyEvent(ev);
		}
	}else if (ev.key >= 32){
		if (!editable){
			Sound.beep();
			return;
		}
		char ch = (char)ev.key;
		/* Don't really need to do this - may leave out later.
		*/
		if ((ev.modifiers & IKeys.SHIFT) == IKeys.SHIFT)
			ch = Character.toUpperCase(ch);
		if (textCase == CASE_UPPER)
			ch = Character.toUpperCase(ch);
		else if (textCase == CASE_LOWER)
			ch = Character.toLowerCase(ch);
		else if (textCase == CASE_NUMBERS && !Character.isDigit(ch)){
			Sound.beep();
			return;
		}
		boolean redoData = deleteSelection();
		if (textCase == CASE_SENTENCE && cursorPos == 0)
			ch = Character.toUpperCase(ch);
		String nt = "";
		int nc = cursorPos+1;
		boolean stayHere = conditionalCharIndex != -1;
		if ((ev.modifiers & IKeys.REPLACEMENT) == 0){
			keepConditionalChar();
			nt = text.substring(0,cursorPos)+ch+text.substring(cursorPos,text.length());
			stayHere = false;
		}else{
			//int dest = conditionalCharIndex == -1 ?
			nt = text.substring(0,conditionalCharIndex)+ch+text.substring(conditionalCharIndex+1,text.length());
		}
		if ((ev.modifiers & IKeys.CONDITIONAL) == 0){
			keepConditionalChar();
		}else{
			conditionalCharIndex = stayHere ? cursorPos-1 : cursorPos;
			conditionalChar = ch;
		}
		newText(nt,cursorPos+(stayHere ? 0 : 1),redoData);
	}else{
		super.onKeyEvent(ev);
	}
}

public int findPressedChar(Point where)
{
	int ret = leftMost;
	int x = spacing;
	FontMetrics fm = getFontMetrics();
	if (where.x < x) return ret;
	String s = getDisplay(text);
	for (int i = leftMost; i<text.length(); i++){
		int fw = fm.getCharWidth(s.charAt(i));
		x += fw/2;
		if (where.x < x) return i;
		x += (-fw/2)+fw;
	}
	return text.length();
}
//==================================================================
public boolean hasSelection() {return startSel != endSel;}
public void selectAll() {startSel = 0; endSel = text.length(); newCursorPos(text.length());}
public boolean noSelection() {boolean ret = (startSel != endSel); startSel = endSel = 0; return ret;}
private boolean dontDrag = false;
private boolean shouldDeselect = false;
//==================================================================
public void penPressed(Point where)
//==================================================================
{
	if (((inputFlags & FLAG_PASSIVE) != 0)&& !doingLocalInput){
		startActiveInput(true);
		return;
	}
	//System.out.println("Pressed! JGF: "+justGotFocus);
	if (menuIsActive()) {
		//System.out.println("My menu is active");
		menuState.closeMenu();
	}
	//holdDownPause = 500;
	modify(WantDrag|WantHoldDown,0);
	shouldDeselect = true;
	if (justGotFocus) {
		shouldDeselect = false;
		modify(0,WantDrag);
		super.penPressed(where);
		justGotFocus = false;
		if (clicksToFireAction == 1) {
			selectAll();
			notifyAction();
		}
		return;
	}
	newCursorPos(findPressedChar(where));
}
//===================================================================
public void penReleased(Point where)
//===================================================================
{
	if (menuIsActive()) return;
	if (shouldDeselect && hasSelection()) {
		noSelection();
		newCursorPos(findPressedChar(where));
		repaintDataNow();
	}
}
//==================================================================
public void penDoubleClicked(Point where)
//==================================================================
{
	selectAll();
	//InputPopupForm.popupForm.popup(this,getWindow().getDim(null)); //FIX: Remove this line.
	if (hasModifier(ShowSIP,false)) ewe.sys.Vm.setSIP(1);
	if (clicksToFireAction == 2) notifyAction();
	//checkSipCoverage();
}
int startDragPos;
//==================================================================
public void startDragging(DragContext dc)
//==================================================================
{
	if (menuIsActive()) return;
	/* For testing only.
	if (true){
		startDragAndDrop(getText(),true,false);
		return;
	}
	*/
	startSel = endSel = startDragPos = cursorPos;//findPressedChar(dc.curPoint);
	shouldDeselect = false;
	repaintDataNow();
}
//==================================================================
public void dragged(DragContext dc)
//==================================================================
{
	if (menuIsActive()) return;
	doDragAt(dc.curPoint);
}
//==================================================================
protected void doDragAt(Point p)
//==================================================================
{
	Rect r = getRect();
	int which = 0;
	startSel = endSel = startDragPos;
	if (p.x < 0) {
		which = leftMost-1;
		if (which < 0) which = 0;
	}else if (p.x >= r.width){
		which = leftMost+numDisplayed+1;
		if (which > text.length()) which = text.length();
	}else which = findPressedChar(p);
	if (which > startDragPos) endSel = which;
 	else startSel = which;
	newCursorPos(which,true);
}
//==================================================================
public void resizeTo(int width,int height)
//==================================================================
{
	super.resizeTo(width,height);
	if (Gui.focusedControl() == this || true) {
		//ewe.sys.Vm.debug("Resizing: "+width+", "+height);
		leftMost = 0;
		fix();
	}
}
//===================================================================
public boolean acceptsData(Object data,DragContext how)
//===================================================================
{
	if (how != null) return false;
	return super.acceptsData(data,how);
}
//===================================================================
public void update()
//===================================================================
{
	fix();
	repaintDataNow();
}
/**
 * Use this to get the selection range. If this returns 0 then there is no selection range.
 * @return A Range which indicates the first and last index of the selection.
 */
//===================================================================
public ewe.util.Range getSelectionRange()
//===================================================================
{
	if (!hasSelection()) return null;
	return new ewe.util.Range(startSel,endSel);
}
//===================================================================
public int getCursorPosition()
//===================================================================
{
	return cursorPos;
}

//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof TextEvent){
		TextEvent te = (TextEvent)ev;
		if (te.type == TextEvent.TEXT_ENTERED){
			doingNativeInput = false;
			if ((te.flags & te.FLAG_TEXT_WAS_ENTERED) != 0){
				setText(fixCase(te.entered));
				updateText(true,true);
			}
			if ((te.flags & te.FLAG_CLOSED_BY_ENTER_KEY) != 0){
				if (wantReturn) notifyAction();
				else if ((inputFlags & FLAG_KEEP_FOCUS_AFTER_INPUT) == 0) tryNext(true);
			}
			else if ((te.flags & te.FLAG_CLOSED_BY_UP_KEY) != 0)
				tryNext(false);
			else if ((te.flags & te.FLAG_CLOSED_BY_DOWN_KEY) != 0)
				tryNext(true);
		}else if (te.type == TextEvent.TEXT_CHANGED){
			setText(fixCase(te.entered));
			updateText(true,true);
		}
	}else
		super.onEvent(ev);
}
//##################################################################
}
//##################################################################


