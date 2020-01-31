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
package ewe.filechooser;
import ewe.ui.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.io.File;
import ewe.sys.Time;
import ewe.data.PropertyList;

//##################################################################
public class FileListTableModel extends TableModel implements FileClipboard.FileClipboardSource{
//##################################################################

public IImage folder = TreeTableModel.closedFolder;
static IconAndText line;
public File fileCheck = ewe.sys.Vm.newFileObject();
public Time fileTime = new Time();
public boolean showDetails = false;
public boolean verticalOnly = false;
public boolean fileTips = true;
FileChooser chooser;

static final String [] defaultDetails = new String[]{"Length","Date"};
static final String [] defaultDetailWidths = new String[]{"XXXXXX","XX-XXX-XXXX"};
String [] details = defaultDetails;
int [] detailWidths = null;
int fullDetailWidths;

//===================================================================
public FileListTableModel(FileChooser chooser)
//===================================================================
{
	this.chooser = chooser;
	fileTime.format = "dd-MMM-yy";
//......................................................
// First specify how many rows and columns. Do not include the row or column
// headers in this value - they are treated separately.
// You can alwaysays change these values later and call table.update() to udpate
// the display.
//......................................................
	numRows = 0;
	numCols = 0;
	hasRowHeaders = false;
	hasColumnHeaders = true;//false;
	hasPreferredSize = false;
	clipData = true;
	canVScroll = false;
	canHScroll = false;
	allColumnsSameSize = true;
	//setShowDetails(true);
}

String [] files;
File parent;



//===================================================================
public void setShowDetails(boolean show)
//===================================================================
{
	clearCellAdjustments();
	//hasColumnHeaders =
	showDetails = show;
	if (showDetails) {
		//verticalOnly = true;
		//mode = VMode;
	}
	if (table != null) table.listMode = showDetails ? 1/*3*/ : 1;
	if (table != null) table.clearSelectedCells(null);
	horizontalScrollUnit = (showDetails) ? /*3*/1 : 1;
	if (table != null) calculateRC();
}
//===================================================================
int getColMod() {return (showDetails) ? /*3*/1 : 1;}
//===================================================================
/**
* This is called by the table to tell the model that it wants to select
* a particular cell. The model should then call table.addToSelection() to
* add it to the selection. Alternately the model can add more or different
* cells.
**/
//===================================================================
public void select(int row,int col,boolean selectOn)
//===================================================================
{
	if (selectOn){
		if (row == -1 && col == -1) return;
		Rect.buff.set((col/getColMod())*getColMod(),row,getColMod(),1);
		table.addToSelection(Rect.buff,false);
	}
}
//-------------------------------------------------------------------
void calculateRC()
//-------------------------------------------------------------------
{
	int num = (files == null) ? 0 : files.length;
	if (mode == VMode) {
		numRows = num;
		numCols = 1;
	}else{
		if (lineHeight != 0) {
			numRows = (table.getSize(Dimension.buff).height-(showDetails ? lineHeight : 3))/lineHeight; //The -1 is for the row headers.
			//if (showDetails) numRows--;
			if (numRows != 0)
				numCols = (num+numRows-1)/numRows;
			else
				numCols = 0;
		}else numRows = numCols = 0;
		/*
		if (numCols == 1 && showDetails){
			verticalOnly = true;
			updateDisplay();
			if (chooser != null) {
				chooser.iconMode = !(chooser.listMode = true);
				chooser.toControls("iconMode,listMode");
			}
		}
		*/
	}
	numCols *= getColMod();
}
//===================================================================
public boolean canSelect(int row,int col)
//===================================================================
{
	return getFileIndex(row,col) != -1;
}
//===================================================================
public void setFiles(File parent,String [] files)
//===================================================================
{
	this.files = files;
	this.parent = parent;
	table.getSize(Dimension.buff);
	//resized(Dimension.buff.width,Dimension.buff.height);
	calculateRC();
	table.scrollToVisible(0,0);
	table.update(true);
}

int lineHeight = 10, lineWidth = 10;
//int lengthWidth = 10, dateWidth = 10;

public static final int HMode = 1;
public static final int VMode = 2;

int mode = HMode;
//===================================================================
public void made()

//===================================================================
{
	super.made();
	FontMetrics fm = table.getFontMetrics();
	if (formatProperties == null) formatProperties = new PropertyList();
	formatProperties.defaultTo("dateFormat",fileTime.format);
	formatProperties.defaultTo("timeFormat",ewe.sys.Vm.getLocale().getString(ewe.sys.Locale.TIME_FORMAT,0,0));
	formatProperties.defaultTo("fontMetrics",fm);
	formatProperties.defaultTo("locale",ewe.sys.Vm.getLocale());
	Object got = fileCheck.getInfo(fileCheck.INFO_DETAIL_NAMES);
	if (got instanceof String []) details = (String [])got;
	got = fileCheck.getInfo(fileCheck.INFO_DETAIL_WIDTHS,fm,null,0);
	if (got instanceof int []) detailWidths = (int [])got;
	else{
		String [] widths = defaultDetailWidths;
		if (got instanceof String []) widths = (String [])got;
		detailWidths = new int[widths.length];
		for (int i = 0; i<widths.length; i++)
			detailWidths[i] = fm.getTextWidth(widths[i]);
	}
	fullDetailWidths = 0;
	for (int i = 0; i<detailWidths.length; i++)
		fullDetailWidths += detailWidths[i];

	if (table != null) table.listMode = showDetails ? /*3*/1 : 1;
	String longSt = "12345678901234567890";
	line = new IconAndText(folder,longSt,fm);
	lineWidth = line.getWidth()+4;
	lineHeight = line.getHeight()+4;
	/*
	FontMetrics fm = table.getFontMetrics();
	dateWidth = fm.getTextWidth(fileTime.format+"  ");
	lengthWidth = fm.getTextWidth("99999M");
	*/
}

//===================================================================
public void resized(int width,int height)
//===================================================================
{
	if (width/lineWidth >= 2 && !verticalOnly) {
		mode = HMode;
		canHScroll = true;
		canVScroll = false;
	}else{
		mode = VMode;
		canVScroll = true;
		canHScroll = false;
	}
	calculateRC();
}
//===================================================================
public void updateDisplay()
//===================================================================
{
	if (table != null){
		Dimension d = table.getSize(null);
		resized(d.width,d.height);
		table.update(true);
	}
}
//===================================================================
public int calculateColWidth(int col)
//===================================================================
{

	if (col == -1) return 0;
	else {
		col = col % getColMod();
		if (col == 0){
			if (mode == VMode) {
				int len = table.getSize(Dimension.buff).width-1;
				//if (showDetails) len -= dateWidth+lengthWidth;
				return len;
			}else return lineWidth+((showDetails) ? fullDetailWidths : 0);
		}else return 0;
	}
}
//===================================================================
public int calculateRowHeight(int row)
//===================================================================
{
	if (row == -1 && !showDetails) return 3;//0;
	return lineHeight;
}
//===================================================================
public int getFileIndex(int row,int col)
//===================================================================
{
	col /= getColMod();
	int n = numRows*col+row;
	if (n < 0 || n>=files.length) return -1;
	return n;
}
/*
static File copyDir;
static String [] copyFiles;
static boolean isCut;
static FileChooser copySource;
*/
//===================================================================
public String [] getSelectedFiles()
//===================================================================
{
	int [] got = table.getSelectedIndexes();
	String [] sel = new String[got.length];
	for (int i = 0; i<got.length; i++)
		sel[i] = files[got[i]];
	return sel;
}
//===================================================================
public boolean fileClipboardOperation(FileClipboard clip,int op)
//===================================================================
{
	chooser.setFile(parent);
	return true;
}
//===================================================================
public void setFilesToCopy(boolean isCut)
//===================================================================
{
	FileClipboard.clipboard.set(parent,getSelectedFiles(),isCut,this);
}

public PropertyList formatProperties;

private Insets myInsets = new Insets(2,2,2,2);
//===================================================================
public Insets getCellInsets(int row,int col,Insets insets)
//===================================================================
{
	return myInsets;
}

//===================================================================
public TableCellAttributes getCellAttributes(int row,int col,boolean isSelected,TableCellAttributes ta)
//===================================================================
{
	//......................................................
	// Best to call the super.getCellAttributes() first.
	//......................................................
	ta = super.getCellAttributes(row,col,isSelected,ta);
	ta.borderColor = ta.flat ? Color.Black : Color.LightGray;
	if (row == -1){
		if (showDetails){
			ta.foreground = Color.Black;
			line.clear();
			line.addColumn(" Name",getColWidth(col)-fullDetailWidths-4,Control.LEFT);
			for (int i = 0; i<details.length; i++)
				line.addColumn(details[i],detailWidths[i],Control.RIGHT);
			ta.data = line;
		}else{
			ta.fillColor = Color.White;
			ta.borderStyle = showDetails && (numCols != 1) ? Control.BDR_OUTLINE|Control.BF_RIGHT : 0;
		}
		return ta;
	}
	if (isSelected){
		ta.fillColor = Color.DarkBlue;//Black;
		ta.foreground = Color.White;
	}else{
		ta.foreground = Color.Black;
		ta.fillColor = Color.White;
	}
	ta.anchor = Control.LEFT;
	int c = col%getColMod();
	ta.borderStyle = showDetails && (numCols != 1) ? Control.BDR_OUTLINE|Control.BF_RIGHT : 0;
	ta.text = null;
	ta.data = null;
	int idx = getFileIndex(row,col);
	if (idx != -1){
		if (FileClipboard.clipboard.isCut(parent,files[idx]))
			ta.drawImageOptions = IImage.DISABLED;
		if (files[idx] == FileChooser.readingDirectory[0]){
			line.clear();
			line.set(FileChooser.getIconForFile(files[idx]),null);
			int w = line.width;
			line.addColumn(files[idx],getColWidth(col)-w-4,Control.LEFT);
			if (c == 0) ta.data = line;
			return ta;
		}
		fileCheck.set(parent,files[idx]);
		boolean isDir = fileCheck.isDirectory();
		line.clear();
		line.set(isDir ? folder : FileChooser.getIconForFile(files[idx]),null);
		int w = line.width;
		if (!showDetails || isDir){
			line.addColumn(files[idx],getColWidth(col)-w-4,Control.LEFT);
		}else{
			line.addColumn(files[idx],getColWidth(col)-w-4-fullDetailWidths,Control.LEFT);
			Object got = fileCheck.getInfo(File.INFO_DETAILS,formatProperties,null,0);
			if (!(got instanceof String [])){
				line.addColumn(FileChooser.lengthToDisplay(fileCheck.getLength()),detailWidths[0],Control.RIGHT);
				fileCheck.getModified(fileTime);
				line.addColumn(fileTime.toString(),detailWidths[1],Control.RIGHT);
			}else{
				String [] dets = (String [])got;
				for (int i = 0; i<dets.length; i++)
					line.addColumn(dets[i],detailWidths[i],Control.RIGHT);
			}
		}

		if (c == 0){
			ta.data = line;
		}
	}
	return ta;
}

//===================================================================
public int fileAt(int row,int col)
//===================================================================
{
	int idx = getFileIndex(row,col);
	if (idx == -1) return -1;
	fileCheck.set(parent,files[idx]);
	return idx;
}
//===================================================================
public Object getToolTip(int x,int y)
//===================================================================
{
	Point p = table.cellAtPoint(x,y,null);
	if (p == null || !fileTips) return null;
	if (p.y == -1) return null;
	int idx = fileAt(p.y,p.x);
	if (idx == -1) return null;
	ToolTip tt = new ToolTip();
	tt.persists = false;
	Object got = fileCheck.getInfo(fileCheck.INFO_TOOL_TIP,formatProperties,null,0);
	if (got instanceof ToolTip) return got;
	else if (got != null){
		tt.tip = got;
		return tt;
	}
	tt.tip = files[idx];
	boolean isDir = fileCheck.isDirectory();
	if (!isDir){

		fileCheck.getModified(fileTime);
		ewe.sys.Locale l = ewe.sys.Vm.getLocale();
		tt.tip = files[idx]+"\n"+l.format(l.FORMAT_PARSE_NUMBER,ewe.sys.Long.l1.set(fileCheck.getLength()),",")+" bytes\n"+fileTime+" "+fileTime.toString(fileTime,l.getString(l.TIME_FORMAT,0,0),l);
	}
	return tt;
}

//===================================================================
public int indexOf(String file)
//===================================================================
{
	for (int i = 0; i<files.length; i++)
		if (files[i].equals(file)) return i;
	return -1;
}
//===================================================================
public Point cellOf(int index)
//===================================================================
{
	if (numCols == 1) return new Point(0,index);
	else if (numRows == 0) return null;
	else return new Point(index/numRows,index%numRows);
}
//===================================================================
public Menu getMenuOutsideCells(Point p)
//===================================================================
{
	if (!chooser.noWrite)
		return new Menu(new String[]{"Paste"},"Popup");
	return null;
}
//===================================================================
public Menu getMenuFor(int row,int col)
//===================================================================
{
	if (row == -1 || !fileTips) return null;
	int idx = fileAt(row,col);
	//if (!table.multiSelect && fileCheck.isDirectory()) return null;
	String f = fileCheck.getFileExt().toLowerCase();
	Menu m = new Menu(mString.split(chooser.viewable),fileCheck.getFullPath());
	String toPut = "";
	if (idx != -1){
		if (!chooser.noWrite){
			toPut = "Rename|-|Copy|Cut|Delete";
			if (FileClipboard.clipboard.canPasteInto(fileCheck)) toPut += "|Paste In";
			else toPut += "|Paste";
		}else
			toPut = "Copy";
		Menu m2 = new Menu(mString.split(toPut),fileCheck.getFullPath());
		if (!fileCheck.isDirectory()){
			boolean added = false;
			boolean showingAView = false;

			if (f.endsWith(".bmp") || f.endsWith(".png") ||
				(f.endsWith(".jpg") && ImageCodec.canDecodeJPEG) ||
				(f.endsWith(".jpeg") && ImageCodec.canDecodeJPEG) ||
				(f.endsWith(".gif") && ImageCodec.canDecodeGIF)
			) showingAView = true;

			if (FileChooser.allowFileViewing){
				if (f.endsWith(".ewe") || f.endsWith(".zip") || f.endsWith(".txt") || f.endsWith("html") || f.endsWith(".htm"))
					showingAView = true;
			}

			if (showingAView) {
				added = true;
				if (m2.itemsSize() != 0) m2.addItem("-");
				m2.addItem("View File");
			}

			if (f.endsWith(".exe") || f.endsWith(".ewe") || f.endsWith(".class")){
				if (m2.itemsSize() != 0 && !added) m2.addItem("-");
				m2.addItem("Run");
			}

			if (FileChooser.allowFileViewing){
				if (m2.itemsSize() != 0 && !added) m2.addItem("-");
				MenuItem mi = new MenuItem("View as"); mi.subMenu = m;
				m2.addItem(mi);
			}
			m2.addItem("Properties");
		}
		return m2;
	}else{
		return getMenuOutsideCells(null);
	}
}
//##################################################################
}
//##################################################################

