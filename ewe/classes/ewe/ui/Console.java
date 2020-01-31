/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
import ewe.util.*;
import ewe.io.*;
import ewe.reflect.FieldTransfer;
/**
* A Console is a non-editable text display. It is optimized for fast display and so:<br>
* <nl><li>It is not-editable.<li>It only works with fixed-width (monospaced) fonts.</nl>
* <p>By default the Console will set its font to the font returned by mApp.findFont("fixed")
* and there is a convenience method to change the size of the font: setFontSize()<p>
* The <b>wrapLength</b> variable controls the wrap length of the Console. It should be 0 for
* no wrapping, -1 for wrapping to the screen size or a positive value for an explicit maximum line width.
**/
//##################################################################
public class Console extends EditControl implements ScrollClient{
//##################################################################
// Some important constants. getNumLines() must never return 0 and so
// the number of items in the lines vector must always be 1,
// even if it is an empty char array.

Vector lines;// = new Vector();
int charWidth, charHeight;
int firstLine = 0, firstChar = 0;
int lineLength = 100;
static char [] singleChar = new char[1];


{
	modify(HasData|TakesKeyFocus|WantHoldDown,0);
	setFont(mApp.findFont("fixed",true));
	clear(false);
	setMenu(new Menu(new MenuItem[]{
	new MenuItem("Clear","ewe/newsmall.bmp",Color.White),
	new MenuItem("Copy","ewe/copysmall.bmp",Color.White),
	new MenuItem("Append","ewe/pastesmall.bmp",Color.White),
},"Menu"));
	menuState.outsideOfControl = false;
}

public int spacing = 2;
/**
* This is the maximum number of lines to store in the Console. By default it is 0 indicates
* that there is no maximum.
**/
public int maxLines = 0;

/**
* This is true by default. Set it false to have no cursor displayed.
**/
public boolean showCursor = true;

/**
* This specifies the number of characters allowed per line, or a value of -1 indicates
* that incoming data should wrap to the screen size, or a value of 0 indicates no line wrapping.
**/
public int wrapLength = 0;

/**
 * Call this to set the Font for the Console, which should be a fixed-width font.
 * @param f The new Font.
 * @return itself.
 */
//===================================================================
public Control setFont(Font f)
//===================================================================
{
	font = f;
	FontMetrics fm = new FontMetrics(f,mApp.mainApp);
	charWidth = fm.getCharWidth('X');
	charHeight = fm.getHeight()+2;
	return this;
}
/**
* Change the size of the font.
* @param size The new size of the font.
*/
//===================================================================
public void setFontSize(int size)
//===================================================================
{
	setFont(new Font(font.getName(),font.getStyle(),size));
}

protected static ImageBuffer itemBuffer = new ImageBuffer();
protected static ImageBuffer blockBuffer = new ImageBuffer();
protected static char [] emptyLine = new char[0];

//===================================================================
public void gotFocus(int how)
//===================================================================
{
	super.gotFocus(how);
	paintCursor();
}
//===================================================================
public void lostFocus(int how)
//===================================================================
{
	super.lostFocus(how);
	paintCursor();
}
//===================================================================
public void selectAll()
//===================================================================
{

}
//===================================================================
public boolean hasSelection()
//===================================================================
{
	return false;
}
//===================================================================
public boolean noSelection()
//===================================================================
{
	return true;
}
//===================================================================
public boolean deleteSelection()
//===================================================================
{
	return false;
}
//===================================================================
public boolean replaceSelection(Object with)
//===================================================================
{
	return false;
}
//===================================================================
public Object getSelection()
//===================================================================
{
	return null;
}
//===================================================================
public void popupMenuEvent(Object selectedItem)
//===================================================================
{
	String name = selectedItem instanceof MenuItem ? ((MenuItem)selectedItem).action : selectedItem.toString();
	if (name.equals("Clear")) clear(true);
	else if (name.equals("Copy"))
		setClipObject(getText());
	else if (name.equals("Append")){
		Object obj = getClipObject();
		String newText = "";
		if (obj instanceof Object []){
			Object [] got = (Object [])obj;
			for (int i = 0; i<got.length; i++){
				if (i != 0) newText += "\n";
				if (got[i] != null)
					newText += got[i].toString();
			}
		}else
			newText = obj.toString();
		append(newText,true);
	}
}
/**
* Get the number of lines that can be partially or fully displayed.
* @param fullyDisplayed if this is true then only the number of fully displayed lines are returned.
* @return The number of lines that can be displayed.
*/
//===================================================================
public int getScreenRows(boolean fullyDisplayed)
//===================================================================
{
	if (height <= 0 || charHeight == 0) return 0;
	int d = height-spacing*2;
	int num = d/charHeight;
	if (!fullyDisplayed && (d%charHeight != 0)) num++;
	return num;
}
/**
* Get the number of characters that can be partially or fully displayed on a line.
* @param fullyDisplayed if this is true then only the number of fully displayed characters are returned.
* @return The number of lines that can be displayed.
*/
//===================================================================
public int getScreenCols(boolean fullyDisplayed)
//===================================================================
{
	if (width <= 0 || charWidth == 0) return 0;
	int d = width-spacing*2;
	int num = d/charWidth;
	if (!fullyDisplayed && (d%charWidth != 0)) num++;
	return num;
}

//===================================================================
public char [] getLine(int line)
//===================================================================
{
	Object got = null;
	//got = line >= getNumLines() ? null : "I am the: "+line+" line!"; //Fixme remove this.
	if (lines == null) return null;
	if (line < lines.size() && line >= 0) got = lines.get(line);
	if (got == null) return null;
	else if (got instanceof char []) return (char [])got;
	else return got.toString().toCharArray();
}

//-------------------------------------------------------------------
boolean setLine(char [] data,int line)
//-------------------------------------------------------------------
{
	if (lines == null) return false;
	if (lines.size() > line) lines.set(line,data);
	else lines.add(data);
	if (data.length+1 > lineLength) lineLength = data.length+1;
	if (data.length != 0 && data[data.length-1] == '\n')
		lines.add(new char[0]);
	int toDelete = (maxLines > 0) ? lines.size()-(maxLines+1) : 0;
	for (int i = 0; i<toDelete; i++) lines.del(0);
	return toDelete > 0;
}
/**
* This does not shift the position to the end of the text, use refresh() that
* instead.
**/
//-------------------------------------------------------------------
protected void updateDisplay()
//-------------------------------------------------------------------
{
	updateDisplay(-1);
}

//===================================================================
public void updateDisplay(int changedLines)
//===================================================================
{
	int was = firstLine;
	int num = getNumLines();
	int show = getScreenRows(true);
	if (firstLine >= num) firstLine = num-show;
	if (num-firstLine > show) firstLine = num-show;
	if (firstLine < 0) firstLine = 0;
	if (was != firstLine || changedLines <0) repaintDataNow();
	else {
		for (int i = 0; i<changedLines; i++){
			int ln = num-changedLines+i;
			paintLine(null,ln);
		}
	}
	if (ss != null) ss.checkScrolls();
	paintCursor();
}

/**
 * Clear the console text.
 * @param updateDisplay if this is true a repaint will be done.
 */
//===================================================================
public void clear(boolean updateDisplay)
//===================================================================
{
	if (lines == null) text = "";
	else{
		lines.clear();
		lines.add(new char[0]);
		lineLength = 1;
		firstLine = firstChar = 0;
	}
	if (updateDisplay)
		updateDisplay(-1);
}
//===================================================================
public String getText()
//===================================================================
{
	if (lines == null) return text;
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i<lines.size(); i++){
		sb.append(new String((char [])lines.get(i)));
	}
	return sb.toString();
}
/**
* This will reposition the display to the end of the text and then do a
* full display refresh.
**/
//===================================================================
public void refresh()
//===================================================================
{
	shiftToShowEnd(false);
	updateDisplay(-1);
}
/**
* This will shift the display to show the end of the data (where the
* cursor whould generally be), but a repaint is not done. If this returns
* true, then a full screen repaint is necessary otherwise it is not.
**/
//-------------------------------------------------------------------
boolean shiftToShowEnd(boolean largeShift)
//-------------------------------------------------------------------
{
	boolean repaint = false;
	int num = getNumLines();
	int was = firstLine;
	if (num<firstLine-1 || firstLine+getScreenRows(true)<num){
		firstLine = num-getScreenRows(true);
		if (firstLine < 0) firstLine = 0;
		if (firstLine != was) repaint = vscroll(was,firstLine);//true;
	}
	char [] ln = getLine(num-1);
	if (ln == null) ln = emptyLine;
	was = firstChar;
	int idx = ln.length+1;
	int cols = getScreenCols(true);
	if (idx<firstChar-1 || firstChar+cols<idx){
		firstChar = ln.length+1-cols;
		if (largeShift) firstChar += cols/4;
	}
	if (firstChar < 0) firstChar = 0;
	if (firstChar != was) repaint = hscroll(was,firstChar);//true;
	return repaint;
}
/**
 * Append a single character to the console.
 * @param data The character to append.
 * @param updateDisplay true to update the display after.
 */
//===================================================================
public void append(char data, boolean updateDisplay)
//===================================================================
{
	if (lines == null) {
		text += data;
		return;
	}
	int soFar = getNumLines();
	char [] line = getLine(soFar-1);
	if (line == null) line = emptyLine;
	int wl = wrapLength;
	if (wl < 0){
		wl = (width <= 0 || charWidth <= 0) ? 0 : getScreenCols(true)-1;
		if (wl < 0) wl = 0;
	}
	boolean didNewLine = false;
	if (wl != 0 && line.length >= wl && data != '\n'){
		didNewLine = true;
		lines.add(line = new char[0]);
		soFar++;
	}
	singleChar[0] = data;
	line = (char [])Utils.appendArray(line,singleChar);
	boolean maxed = setLine(line,soFar-1);
	if (updateDisplay){
		boolean repaint = shiftToShowEnd(true);
 		if (maxed || didNewLine || repaint || data == '\n') updateDisplay(-1);
		else {
			paintChar(null,soFar-1,line.length-1,data);
			paintCursor();
		}
	}
}
//-------------------------------------------------------------------
int appendStraight(char [] data,int start,int length,int lineTotal)
//-------------------------------------------------------------------
{
	if (lines == null){
		text += new String(data,start,length);
		return lineTotal;
	}
	if (length == 0) return lineTotal;
	int wl = wrapLength;
	if (wl < 0){
		wl = (width <= 0 || charWidth <= 0) ? 0 : getScreenCols(true)-1;
		if (wl < 0) wl = 0;
	}
	int soFar = getNumLines();
	char [] line = getLine(soFar-1);
	if (line == null) line = emptyLine;
	if (wl != 0 && length+line.length >wl){
		lineTotal = appendStraight(data,start,wl-line.length,lineTotal);
		lines.add(new char[0]);
		return appendStraight(data,start+wl-line.length,length-wl+line.length,lineTotal);
	}
	char [] nl = new char[line.length+length];
	ewe.sys.Vm.copyArray(line,0,nl,0,line.length);
	ewe.sys.Vm.copyArray(data,start,nl,line.length,length);
	if (setLine(nl,soFar-1)) return -1;
	if (lineTotal == -1) return -1;
	return lineTotal+1;
}
//===================================================================
public void append(char [] data,int start,int length,boolean updateDisplay)
//===================================================================
{
	if (length == 1) append(data[start],updateDisplay);
	else{
		int sa = start;
		int newLines = 0;
		for (int i = 0; i<length; i++){
			if (data[start+i] == '\n'){
				newLines = appendStraight(data,sa,start+i+1-sa,newLines);
				sa = start+i+1;
			}
		}
		if (sa != start+length) newLines = appendStraight(data,sa,start+length-sa,newLines);
		if (updateDisplay && lines != null){
			boolean repaint = shiftToShowEnd(true);
			updateDisplay(repaint || newLines == -1 ? -1 : newLines+1);
		}
	}
}
//===================================================================
public void append(String data,boolean updateDisplay)
//===================================================================
{
	append(ewe.sys.Vm.getStringChars(data),0,data.length(),updateDisplay);
}

/**
 * Paint a single character or cursor usually at the end of the line.
 * @param gr The graphics context. Can be null.
 * @param line Which line.
 * @param whichChar the index of the char on the line.
 * @param theChar should be 0 to clear the char, -1 for the cursor, or any other char.
 * @return
 */
//===================================================================
void paintChar(Graphics gr,int line,int whichChar,char theChar)
//===================================================================
{
	Graphics g = gr;
	if (g == null) g = getGraphics();
	if (g == null) return;
	try{
		int flags = getModifiers(true);
		getColors(false,flags);
		int num = getScreenRows(false);
		if (line < firstLine || line-firstLine >= num) return;
		if (whichChar < firstChar) return;
		int w = (width-spacing*2)-(whichChar-firstChar)*charWidth;
		if (w > charWidth) w = charWidth;
		else if (w <= 0) return; //Is not on screen.
		int h = height-((line-firstLine)*charHeight)-(spacing*2);
		int mh = Gui.focusedControl() == this ? charHeight : 4;
		if (h > charHeight) h = charHeight;

		Graphics im = itemBuffer.get(w,h,true);
		im.setColor(colors[1]);
		im.fillRect(0,0,w,h);
		im.setColor(colors[0]);
		if (theChar == (char)-1){
			im.fillRect(0,h-mh,w,mh);
		}else if (theChar != 0){
			im.setFont(font);
			im.drawChar(theChar,0,0,0);
		}
		g.drawImage(itemBuffer.image,spacing+(whichChar-firstChar)*charWidth,spacing+(line-firstLine)*charHeight);
		//im.free();
	}finally{
		if (g != gr) g.free();
	}

}
//-------------------------------------------------------------------
void paintCursor()
//-------------------------------------------------------------------
{
	paintCursor(null,false);
}
//-------------------------------------------------------------------
void paintCursor(Graphics g,boolean clear)
//-------------------------------------------------------------------
{
	if (!showCursor) return;
	int line = getNumLines()-1;
	char [] ln = getLine(line);
	int pos = ln == null ? 0 : ln.length;
	paintChar(g,line,pos,clear ? (char)0 : (char)-1);
}
//===================================================================
public void paintLine(Graphics gr,int line)
//===================================================================
{
	Graphics g = gr;
	if (g == null) g = getGraphics();
	if (g == null) return;
	try{
		int flags = getModifiers(true);
		getColors(false,flags);
		int num = getScreenRows(false);
		if (line < firstLine || line-firstLine >= num) return;
		int w = width-spacing*2;
		int h = height-((line-firstLine)*charHeight)-(spacing*2);
		if (h > charHeight) h = charHeight;
		Graphics im = itemBuffer.get(w,h,true);
		im.setColor(colors[1]);
		im.fillRect(0,0,w,h);
		char [] l = getLine(line);
		if (l != null){
			int len = l.length;
			if (len > 0 && l[len-1] == '\n') len--;
			if (firstChar < len){
				im.setColor(colors[0]);
				im.setFont(font);
				im.drawText(l,firstChar,len-firstChar,0,0);
			}
		}
		g.drawImage(itemBuffer.image,spacing,spacing+(line-firstLine)*charHeight);
		//im.free();
	}finally{
		if (g != gr) g.free();
	}
}

//===================================================================
public void doPaint(Graphics g,Rect area)
//===================================================================
{
	int flags = getModifiers(true);
	if (!((flags & Invisible) == 0)) return;
	getColors(false,flags);
	if ((borderStyle & BF_SOFT) != 0) doBackground(g);
	Color c = colors[1];
	if (!hasModifier(PaintDataOnly,false))
		g.draw3DRect(
			getDim(Rect.buff),
			borderStyle, //Graphics.EDGE_SUNKEN|Graphics.BDR_OUTLINE,
			(flags & DrawFlat) != 0,
			c,//null,
			borderColor);
		doPaintData(g,area);
}

//===================================================================
public void doPaintData(Graphics g,Rect area)
//===================================================================
{
	getNumLines();
	int num = getScreenRows(false);
	if (area == null)
		for (int i = 0; i<num; i++)
			paintLine(g,firstLine+i);
	else{
		int first = (area.y-spacing*2)/charHeight;
		int toDo = ((area.height+charHeight-1)/charHeight)+1;
		if (first+toDo > 0){
			toDo += first;
			for (int i = first; i<toDo; i++)
				if (i >= 0){
					paintLine(g,firstLine+i);
				}
		}
	}
	paintCursor(g,false);
}
//===================================================================
public void setText(String text)
//===================================================================
{
	clear(true);
	append(text,true);
}
//-------------------------------------------------------------------
protected void doPaintData(Graphics g)
//-------------------------------------------------------------------
{
	doPaintData(g,null);
}
//-------------------------------------------------------------------
private boolean vscroll(int was,int now)
//-------------------------------------------------------------------
{
	if (was == now) return false;
	int difference = now > was ? now-was : was-now;
	difference *= charHeight;
	int sr = getScreenRows(true), sr2 = getScreenRows(false);
	Rect r = new Rect(spacing,spacing,width-spacing*2,sr*charHeight);
	if (r.height > difference){
		//pageColor = new Color((int)(java.lang.Math.random()*155)+100,(int)(java.lang.Math.random()*155)+100,(int)(java.lang.Math.random()*155)+100);
		if (now > was){
			scrollAndRepaint(r.x,r.y+difference,r.width,r.height-difference,r.x,r.y);
			paintLine(null,now+sr2-1); // Paint the last piece of line at the bottom.
		}else{
			scrollAndRepaint(r.x,r.y,r.width,r.height-difference,r.x,r.y+difference);
			paintLine(null,now+sr2-1); // Paint the last piece of line at the bottom.
		}
		return false;
	}else
		return true;
}

//-------------------------------------------------------------------
private boolean hscroll(int was,int now)
//-------------------------------------------------------------------
{
/*
	if (was == now) return false;
	int difference = now > was ? now-was : was-now;
	difference *= charWidth;
	Rect r = new Rect(spacing,spacing,width-spacing*2,getScreenRows(true)*charHeight);
	if (r.width > difference){
		//pageColor = new Color((int)(Math.random()*155)+100,(int)(Math.random()*155)+100,(int)(Math.random()*155)+100);
		if (now > was){
			scrollAndRepaint(r.x+difference,r.y,r.width-difference,r.height,r.x,r.y);
		}else

			scrollAndRepaint(r.x,r.y,r.width-difference,r.height,r.x+difference,r.y);
		return false;
	}else
	*/
		return true;
}
//==================================================================
public void doScroll(int which,int action,int value)
//==================================================================
{
	boolean repaint = false;
	if (which == IScroll.Vertical) {
		int was = firstLine, dl = 0;
		if (action == IScroll.ScrollHigher) dl = 1;
		else if (action == IScroll.ScrollLower) dl = -1;
		else if (action == IScroll.PageHigher) dl = getScreenRows(true);
		else if (action == IScroll.PageLower) dl = -getScreenRows(true);
		else if (action == IScroll.TrackTo) dl = value-was;
		dl += firstLine;
		if (dl > getNumLines()-getScreenRows(true)) dl = getNumLines()-getScreenRows(true);
		if (dl < 0) dl = 0;
		if (was != dl) paintCursor(null,true);
		firstLine = dl;
		repaint = vscroll(was,dl);
	}else {
		int sh = 1;
		int was = firstChar, dx = 0;
		if (action == IScroll.ScrollHigher) dx = sh;
		else if (action == IScroll.ScrollLower) dx = -sh;
		else if (action == IScroll.PageHigher) dx = width/charWidth-1;
		else if (action == IScroll.PageLower) dx = -(width/charWidth)+1;
		else if (action == IScroll.TrackTo) dx = value-was;
		dx += firstChar;
		if (dx+getScreenCols(true) > getLineLength()) dx = getLineLength()-getScreenCols(true);
		if (dx < 0) dx = 0;
		firstChar = dx;
		repaint = hscroll(was,dx);
	}
	if (repaint) repaintDataNow();
	if (ss != null) ss.updateScroll(which);
	paintCursor();
}
//===================================================================
public int getLineLength()
//===================================================================
{
	return lineLength;
}
//===================================================================
public int getNumLines()
//===================================================================
{
	if (lines == null){
		if (width > 0 && height > 0){
			lines = new Vector();
			lines.add(new char[0]);
			append(text,false);
		}else
			return 0;
	}
	return lines.size();
}
//===================================================================
public int getActual(int direction)
//===================================================================
{
	if (direction == Horizontal) return getLineLength();
	else return getNumLines();
}
//===================================================================
public int getCurrent(int direction)
//===================================================================
{
	if (direction == Horizontal) return firstChar;
	else return firstLine;
}
//===================================================================
public int getVisible(int direction,int forSize)
//===================================================================
{
	if (direction == Horizontal) {
		if (charWidth == 0) return 0;
		return (forSize-spacing*2)/charWidth;
	}else {
		if (charHeight == 0) return 0;
		return (forSize-spacing*2)/charHeight;
	}
}
public boolean needScrollBar(int direction,int forSize)
{
	return true;
}
public boolean canGo(int orientation, int direction, int position)
{
	return true;
}
/**
* If this is true then keys pressed are echoed (displayed) on the console.
**/
public boolean localEcho = false;

private Writer writer;
private Reader reader;
/**
* Get a Writer that you can use to write to the console.
**/
//===================================================================
public Writer getWriter()
//===================================================================
{
	if (writer != null) return writer;
	return writer = new Writer(){
		protected boolean closed = false;
		public void close(){writer = null; closed = true; writer = null;}
		public void write(char[] value, int offset, int length) throws IOException{
			if (closed) throw new IOException("Stream closed.");
			if (amDebug) reappear();
			append(value,offset,length,true);
		}
		public void flush(){}
	};
}
Lock readLock;
CharArray typed;
/**
* Get a Reader that you can use to read from the console.
**/
//===================================================================
public Reader getReader()
//===================================================================
{
	if (reader != null) return reader;
	if (readLock == null) readLock = new Lock();
	if (typed == null) typed = new CharArray();
	return reader = new Reader(){
		protected boolean closed = false;
		public void close(){
			closed = true;
			reader = null;
			if (readLock != null && readLock.grab())try{
				readLock.notifyAllWaiting();
			}finally{
				readLock.release();
			}
		}
		public int read(char[] dest,int offset, int length){
			if (length <= 0) return 0;
			if (amDebug) reappear();
			while(true){
				if (closed) return -1;
				readLock.synchronize(); try{
					if (typed.length == 0){
						try{
							readLock.waitOn();
						}catch(Exception e){}
					}else{
						if (length > typed.length) length = typed.length;
						System.arraycopy(typed.data,0,dest,offset,length);
						typed.length -= length;
						if (typed.length != 0)
							System.arraycopy(typed.data,length,typed.data,0,typed.length);
						return length;
					}
				}finally{
					readLock.release();
				}
			}
		}
	};
}


/**
 * If there are open Writers and Readers for this Console, this call will close them.
 */
//===================================================================
public void closeIO()
//===================================================================
{
	if (reader != null){
		readLock.synchronize(); try{
			reader.close();
			readLock.notifyAllWaiting();
			reader = null;
		}catch(Exception e){
		}finally{readLock.release();}
	}
	if (writer != null) try{
		writer.close();
		writer = null;
	}catch(Exception e){
	}
}
private char[] kb;


/**
 Connect this console to a Reader and a Writer. Two background threads are started, one
 to read from the reader and display all read characters on the console, and another
 that takes characters typed on the console and sends it to the Writer.
 * @param reader the reader to read characters from to output on the console. If this
	is null then no reading is done.
 * @param writer the writer to send characters typed on the console to. If this is null
	then no writing is done.
 * @return an array of two IOHandles. The IOHandle at index 0 is the handle that
	indicates the state of the transfer of characters from the provided reader to the
	console display. The IOHandle at index 1 is the handle that indicates the state of
	the transfer of characters typed on the console to the provided writer. If either
	reader or writer is null, the corresponding IOHandle will be null.
 */
//===================================================================
public IOHandle[] connectTo(Reader reader,Writer writer)
//===================================================================
{
	IOHandle[] ret = new IOHandle[2];
	ret[0] = reader != null ? StreamUtils.transfer(reader,getWriter()) : null;
	ret[1] = writer != null ? StreamUtils.transfer(getReader(),writer) : null;
	return ret;
}
/**
 Connect this console to a bi-directional Stream. This method creates an InputStreamReader() and
	and OutputStreamWriter() and then calls the connectTo(Reader,Writer) method. Please
	see that method to see how the method works.
 * @param s the stream to read from and write to.
 * @return an array of two IOHandles. The IOHandle at index 0 is the handle that
	indicates the state of the transfer of characters from the stream to the
	console display. The IOHandle at index 1 is the handle that indicates the state of
	the transfer of characters typed on the console to the stream. If either
	reader or writer is null, the corresponding IOHandle will be null.
 */
//===================================================================
public IOHandle[] connectTo(Stream s)
//===================================================================
{
	return connectTo(new InputStreamReader(s.toInputStream()),new OutputStreamWriter(s.toOutputStream()));
}
/**
 * This gets called by the default sendChar() method and can also be called directly.
 * By default, if a Reader has been opened to the Console, then this method will put the
 * characters in the Reader's buffer.
 * @param keys a set of characters.
 * @param offset the start of the characters to send.
 * @param length the number of characters to send.
 */
//===================================================================
public void sendChars(char[] keys,int offset,int length)
//===================================================================
{
	if (typed != null){
		typed.append(keys,offset,length);
		if (readLock != null)
			if (readLock.grab()) try{
				readLock.notifyAllWaiting();
			}finally{
				readLock.release();
			}
	}
}
/**
 * This gets called when a key was pressed by the default sendKey() method.
 * By default this method will call the sendChars(char[] keys,int offset,int length)
 * @param key The pressed key.
 */
//===================================================================
public void sendChar(char key)
//===================================================================
{
	if (kb == null) kb = new char[1];
	kb[0] = key;
	sendChars(kb,0,1);
}
/**
 * This converts a key press to a Unicode character for sending or displaying.
 * You can override this to provide more or better key mappings.
 * @param key The pressed key.
 * @return The character to convert to.
 */
//-------------------------------------------------------------------
protected char keyToChar(KeyEvent key)
//-------------------------------------------------------------------
{
	switch(key.key){
		case IKeys.ENTER: return '\n';
		case IKeys.TAB: return '\t';
		case IKeys.BACKSPACE: return (char)8;
		default: return (char)key.key;
	}
}
/**
 * This gets called when a key was pressed by the default keyPressed() method.
 * By default this method calls keyToChar() on the key and calls sendChar().
 * @param key The pressed key event.
 */
//-------------------------------------------------------------------
protected void sendKey(KeyEvent key)
//-------------------------------------------------------------------
{
	sendChar(keyToChar(key));
}
/**
* This gets called when a valid key is pressed.
* By default this will echo the local character if localEcho is true and
* then call sendKey(). sendKey() by default will then call sendChar().
* @param ev The detected key event.
*/
//-------------------------------------------------------------------
protected void keyPressed(KeyEvent ev)
//-------------------------------------------------------------------
{
	if (localEcho)
		append(keyToChar(ev),true);
	sendKey(ev);
}
/**
 * This is used to decide if you wish to process a key press. If this method returns true, then
 * the key will be passed to keyPressed(KeyEvent ev)
 * By default, this will return false only for keys considered SPECIAL except ENTER, TAB and BACKSPACE.
 * All other keys will return true.
 * @param ev The key event.
 * @return true if you want to keep and process the key, false if not. If you return false
 * the key will be passed to the super class implementation of onKeyEvent.
 */
//-------------------------------------------------------------------
protected boolean wantKey(KeyEvent ev)
//-------------------------------------------------------------------
{
	if ((ev.modifiers & IKeys.SPECIAL) == 0) return true;
	if (ev.key == IKeys.ENTER || ev.key == IKeys.TAB || ev.key == IKeys.BACKSPACE)
		return true;
	return false;
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
	if (!wantKey(ev)) {
		super.onKeyEvent(ev);
		return;
	}
	/*
	if ((ev.modifiers & (IKeys.CONTROL|IKeys.SPECIAL)) == (IKeys.CONTROL)){
		if (ev.key != 24 && ev.key != 3 && ev.key != 22){
			super.onKeyEvent(ev);
			return;
		}else{
			ev.modifiers |= IKeys.SPECIAL;
		}
	}
	if ((ev.modifiers & IKeys.SPECIAL) != 0){
		if (ev.key == IKeys.ENTER || ev.key == IKeys.TAB) keyPressed(ev);
		else
			super.onKeyEvent(ev);
		return;
	}
	*/
	//int flags = getModifiers(true);
	//boolean shifted = ((ev.modifiers & IKeys.SHIFT) == IKeys.SHIFT);
	//boolean ctrled = ((ev.modifiers & IKeys.CONTROL) == IKeys.CONTROL);
	keyPressed(ev);
}

//-------------------------------------------------------------------
void reappear()
//-------------------------------------------------------------------
{
	if (getWindow() == null){
		Console dc = this;
		dc.wrapLength = -1;
		Form f = new Form();
		f.title = "Console";
		f.addLast(new ScrollBarPanel(dc));
		f.windowFlagsToSet |= Window.FLAG_MAXIMIZE_ON_PDA;
		f.show();
	}
}

//-------------------------------------------------------------------
static Console debugConsole = null;
//-------------------------------------------------------------------
boolean amDebug = false;
//-------------------------------------------------------------------
/**
* Get a special console used for debugging or standard input/output.
* This Console will appear the first time data is sent to it (or read from it) and will re-appear
* should it be closed and data is sent to it (or read from it).
*/
//===================================================================
public static Console getAppConsole()
//===================================================================
{
	if (debugConsole == null){
 		debugConsole = new Console();
		debugConsole.amDebug = true;
		debugConsole.localEcho = true;
		debugConsole.setPreferredSize(480,320);
	}
	return debugConsole;
}
//-------------------------------------------------------------------
protected boolean paintConditionalChar(Graphics g, boolean b)
//-------------------------------------------------------------------
{
	return false;
}
/**
* Send a line of data to the special debugging/standard IO console. See getAppConsole().
**/
//===================================================================
public static void debug(String data)
//===================================================================
{
	Console c = getAppConsole();
	c.reappear();
	c.append(data+"\n",true);
}
//===================================================================
public boolean checkSipCoverage()
//===================================================================
{
	return false;
}

/*
//=================================================================
public static void main(String[] args) throws IOException
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	ewe.sys.Vm.useConsoleForIO();
	ewe.sys.Vm.out().println("Hello there!");
	mThread.nap(3000);
	ewe.sys.Vm.err().println("Hello again!");
	mThread.nap(3000);
	String got = ewe.sys.Vm.in().readLine();
	ewe.sys.Vm.out().println(got);
	mThread.nap(3000);
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################



