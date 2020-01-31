package ewe.ui;
import ewe.io.*;
import ewe.util.ByteArray;
//##################################################################
public class HexTableModel extends TableModel{
//##################################################################
private int numBytes;

{
	hasRowHeaders = hasColumnHeaders = true;
	numCols = 17;
	numRows = 0;
	preferredRows = 10;
	clipData = true;
}
//-------------------------------------------------------------------
protected int calculateTextCharsInColumn(int col)
//-------------------------------------------------------------------
{
	if (col == -1) return 9;
	else if (col == numCols-1) return 17;
	else return 3;
}
//-------------------------------------------------------------------
protected int calculateTextLinesInRow(int row)
//-------------------------------------------------------------------
{
	return 1;
}
private static char [] buff = new char[2];
private static char [] addr = new char[8];
private static char [] hex =  {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
private static char [] text = new char[16];

private byte [] fetched, empty = new byte[16];
private int lastFetched;

//-------------------------------------------------------------------
protected boolean readIn16(int location,byte [] dest)
//-------------------------------------------------------------------
{
	try{
		if (stream != null){
			stream.seek((long)location);
			int toRead = (int)(stream.length()-location);
			IO.readAll(stream,dest,0,toRead < 16 && toRead > 0? toRead : 16);
			return true;
		}
		return false;
	}catch(IOException e){
		return false;
	}
}


//===================================================================
public ewe.io.RandomAccessStream stream;
//===================================================================

//===================================================================
public void closeStream() throws IOException
//===================================================================
{
	if (stream != null) stream.close();
	setStream((RandomAccessStream)null);
}

//-------------------------------------------------------------------
private byte [] fetch(int location)
//-------------------------------------------------------------------
{
	if (fetched == null || location != lastFetched){
		fetched = new byte[16];
		lastFetched = location;
		if (!readIn16(location,fetched)) {
			fetched = null;
			return empty;
		}
	}
	return fetched;
}

//===================================================================
public void setStream(RandomAccessStream stream) throws IOException
//===================================================================
{
	this.stream = stream;
	if (stream == null){
		numRows = 0;
		numBytes = 0;
	}else{
		numBytes = (int)stream.length();
		numRows = (numBytes + 15)/16;
	}
	fetched = null;
	if (table != null) {
		table.update(false);
		table.repaint();
	}
}
//===================================================================
public void setStream(ByteArray data) throws IOException
//===================================================================
{
	setStream(new MemoryFile(data));
}
//===================================================================
public String toAscii(byte [] got,int num)
//===================================================================
{
	int i = 0;
	for (; i<num; i++){
		byte g = got[i];
		text[i] = g <= 26 || g >= 0x7f ? '.' : (char)g;
	}
	for (; i<got.length; i++)
		text[i] = ' ';
	//return new String(text);
	return ewe.sys.Vm.mutateString(strbuff,text,0,16,true);
}
private String strbuff = new String();
//===================================================================
public String toHex(int value,int digits)
//===================================================================
{
	int v = value;
	char [] b = (digits == 2) ? buff : addr;
	for (int i = 0; i<digits; i++){
		b[digits-1-i] = hex[value & 0xf];
		value >>= 4;
	}
	String s = ewe.sys.Vm.mutateString(strbuff,b,0,b.length,true);//new String(b);
	return s;
}
//===================================================================
public Object getCellText(int row,int col)
//===================================================================
{
	int offset = row*(numCols-1);
	int valid = (offset+16 > numBytes ? numBytes-offset : 16);
	byte [] get = (row != -1) ? fetch(offset) : null;
	if (col == numCols-1){
		if (row == -1)
			return "Ascii";
		else
			return toAscii(get,valid);
	}else if (col == -1){
		if (row != -1){
			String s = toHex(offset,8);
			return s;
		}else
			return "Address";
	}else if (row == -1){
		return toHex(col,2);
	}else{
		int value = (col >= 0 && col < 16) ? (int)get[col] & 0xff : -1;
		return col < valid ? toHex(value,2) : "  " ;
	}
}
//===================================================================
public void gotoAddress(long address)
//===================================================================
{
	int row = (int)(address >> 4);
	if (row >= numRows) return;
	if (table != null) {
		table.changeOrigin(row,0);
		table.repaintNow();
	}
}
//===================================================================
public void promptGoTo()
//===================================================================
{
	InputBox ib = new InputBox("Goto Address");
	String where = ib.input("0",10);
	if (where == null) return;
	try{
		gotoAddress(ewe.sys.Convert.parseLong(where,16));
	}catch(NumberFormatException e){
	}
}
//===================================================================
public boolean onKeyEvent(KeyEvent ev)
//===================================================================
{
	ev.controlToLetter();
	if ((ev.type == ev.KEY_PRESS) &&
		(ev.isHotKey(ev.toKey(IKeys.CONTROL,'g')) || (ev.key == IKeys.F2))){
		promptGoTo();
		return true;
	}
	return super.onKeyEvent(ev);
}

//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if ((ev instanceof FormEvent) && (ev.type == FormEvent.CLOSED))
		if (stream != null){
			stream.close();
		}
	super.onEvent(ev);
}
//===================================================================
public Form getTableForm(Form destination)
//===================================================================
{
	if (destination == null) destination = new Form();
	TableControl tc = new TableControl();
	tc.setTableModel(this);
	tc.font = mApp.findFont("fixed",true);
	destination.addLast(new ScrollBarPanel(tc));
	destination.addListener(this);
	return destination;
}
//===================================================================
public static Form getViewOf(File f, int maxSizeForFullRead, Form destination) throws IOException
//===================================================================
{
	HexTableModel htm = new HexTableModel();
	RandomAccessStream ras = null;
	try{
		ras = f.toRandomAccessStream("r");
	}catch(IOException e){
		if (f.length() > maxSizeForFullRead) throw e;
		Stream in = f.toReadableStream();
		ras = new MemoryFile(in,"r");
		in.close();
	}
	htm.setStream(ras);
	Form ff = htm.getTableForm(destination);
	ff.title = f.getName();
	return ff;
}
//=================================================================
public static void main(String[] args) throws IOException
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	getViewOf(File.getNewFile(args[0]),10000,null).execute();
	ewe.sys.Vm.exit(0);
}

//##################################################################
}
//##################################################################

