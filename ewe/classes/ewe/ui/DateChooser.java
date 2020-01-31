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
import ewe.util.*;
import ewe.reflect.*;

//##################################################################
public class DateChooser extends Editor{
//##################################################################

MultiPanel panels = new CardPanel();

public int year;
public String monthName;
public int month;
public int day;

public boolean autoAdvance = true;
public boolean didAll = false;

/**
* Set this true to select the day first.
**/
public static boolean dayFirst = false;

TableControl dayChooser, monthChooser, yearChooser;
public Locale locale = Vm.getLocale();

Control dayDisplay;
Control monthDisplay;
Control yearDisplay;

Time getTime()
{
	Time t = (Time)dateSet.getCopy();
	if (showingSmall && singleInput != null) {
		Time.fromString(singleInput.getText(),t,t instanceof TimeOfDay ? t.format : locale.getString(Locale.SHORT_DATE_FORMAT,0,0),locale);
		t.update();
		//ewe.sys.Vm.debug(t.toString());
		return t;
	}
	t.day = day;
	t.month = month;
	t.year = year;
	t.update();
	return t;
	//return new Time(day,month,year);
}

//===================================================================
void addTable(TableControl tc,TableModel tm,String name)
//===================================================================
{
	tc.setTableModel(tm);
	tc.setClickMode(true);
	panels.addItem(tc,name,null);
	//tc.addListener(this);
}

boolean added = false;
//-------------------------------------------------------------------
Control addTopData(CellPanel cp,String field)
//-------------------------------------------------------------------
{
	Control dl = new mButton();//DumbLabel(1,10);
	cp.addNext(addField(dl,field),HSTRETCH,HFILL);
	dl.addListener(this);
	//dl.anchor = CENTER;
	dl.modify(DrawFlat,0);
	dl.borderStyle = BDR_OUTLINE|BF_TOP|BF_RIGHT|BF_SQUARE;
	//if (!added) dl.borderStyle |= BF_LEFT;
	added = true;
	dl.borderColor = Color.Black;
	return dl;
}

public String firstPanel = "monthName";

//===================================================================
public void reset(Time t)
//===================================================================
{
	setDate(t);
	didAll = false;
	newDate();
	panels.select(firstPanel);
}
//===================================================================
public CellPanel addTopSection(CellPanel addTo,Control cp)
//===================================================================
{
	addTo.modify(DrawFlat,0);
	addTo.defaultTags.set(INSETS,new Insets(0,0,0,0));
	mButton b = new mButton();
	b.borderStyle = BDR_OUTLINE|BF_LEFT|BF_TOP|BF_RIGHT|BF_SQUARE;
	b.image = new DrawnIcon(DrawnIcon.CROSS,10,10,new Color(0x80,0,0));
	addTo.addNext(addField(b,"reject")).setCell(DONTSTRETCH);
	addTo.addNext(cp,HSTRETCH,HFILL);
	b = new mButton();
	b.borderStyle = BDR_OUTLINE|BF_TOP|BF_RIGHT|BF_SQUARE;
	b.image = new DrawnIcon(DrawnIcon.TICK,10,10,new Color(0,0x80,0));
	addTo.addNext(addField(b,"accept")).setCell(DONTSTRETCH);
	return addTo;
}

CardPanel cards = new CardPanel();
mInput singleInput;

//===================================================================
public DateChooser(Locale l)
//===================================================================
{
	if (l != null) locale = l;
	setDate(new Time());
	addLast(cards);
	CellPanel addTo = new CellPanel();
	cards.addItem(addTo,"full",null);

	CellPanel cp = new CellPanel();
	CellPanel top = new CellPanel();
	cp.equalWidths = true;
	if (dayFirst){
		firstPanel = "day";
		dayDisplay = addTopData(cp,"day");
		monthDisplay = addTopData(cp,"monthName");
	}else{
		firstPanel = "monthName";
		monthDisplay = addTopData(cp,"monthName");
		dayDisplay = addTopData(cp,"day");
	}
	yearDisplay = addTopData(cp,"year");
	cp.endRow();


	addTopSection(top,cp);
	addTo.addLast(top).setCell(HSTRETCH);
	addTo.addLast((Control)panels);

	if (dayFirst){
		addTable(dayChooser = new TableControl(),new dayChooserTableModel(locale),"day");
		addTable(monthChooser = new TableControl(),new monthChooserTableModel(locale),"monthName");
	}else{
		addTable(monthChooser = new TableControl(),new monthChooserTableModel(locale),"monthName");
		addTable(dayChooser = new TableControl(),new dayChooserTableModel(locale),"day");
	}
	addTable(yearChooser = new TableControl(),new yearChooserTableModel(),"year");

	Form mini = new Form();
	mini.firstFocus = singleInput = new mInput();
	singleInput.wantReturn = true;
	addField(singleInput,"entered");
	cards.addItem(mini,"mini",null);

	mButton b = new mButton();
	//b.borderStyle = BDR_OUTLINE|BF_LEFT|BF_TOP|BF_RIGHT|BF_SQUARE;
	b.image = new DrawnIcon(DrawnIcon.CROSS,10,10,new Color(0x80,0,0));
	addField(b,"reject");
	b.image = new DrawnIcon(DrawnIcon.CROSS,10,10,new Color(0x80,0,0));
	mini.addNext(b).setCell(VSTRETCH);
	mini.addNext(singleInput);
	b = new mButton();
	b.image = new DrawnIcon(DrawnIcon.TICK,10,10,new Color(0,0x80,0));
	addField(b,"accept");
	mini.addNext(b).setCell(VSTRETCH);
	mini.borderStyle = BDR_OUTLINE|BF_RECT;
	newDate();

}
boolean showingSmall = false;

//-------------------------------------------------------------------
void setSize(boolean isSmall)
//-------------------------------------------------------------------
{
	cards.select(isSmall ? "mini" : "full");
	showingSmall = isSmall;
}
//===================================================================
public void fieldChanged(FieldTransfer ft,Editor ed)
//===================================================================
{/*
	if (ft.fieldName.equals("monthName")){
		month = locale.fromString(locale.MONTH,monthName,0);
	}
	*/
	newDate();
}
//===================================================================
public void action(FieldTransfer ft,Editor ed)
//===================================================================
{
	String n = ft.fieldName;
	if (n.equals("day")||n.equals("monthName")||n.equals("year"))
		panels.select(n);
	if (n.equals("accept") || n.equals("entered"))
		exit(IDOK);
	if (n.equals("reject")) exit(IDCANCEL);
}
//===================================================================
public void onControlEvent(ControlEvent ev)
//===================================================================
{
	if (ev instanceof TableEvent && ev.type == TableEvent.CELL_CLICKED){
		if (ev.target == dayChooser){
			day = Convert.toInt((String)((TableEvent)ev).cellData);
			newDate();
			toControls("day");
			if (autoAdvance && !didAll) {
				if (dayFirst) panels.select("monthName");
				else {
					panels.select("year");
					didAll = true;
				}
			}
		}else if (ev.target == monthChooser){
			month = (int)((ewe.sys.Long)(((TableEvent)ev).cellData)).value;
			newDate();
			monthName = locale.getString(locale.SHORT_MONTH,month,0);
			toControls("monthName");
			if (autoAdvance && !didAll) {
				if (dayFirst) {
					panels.select("year");
					didAll = true;
				}else
					panels.select("day");
			}
		}else if (ev.target == yearChooser){
			String p = (String)((TableEvent)ev).cellData;
			int dec = year % 100;
			year -= dec;
			if (p.charAt(0) == 'C'){
				if (p.charAt(1) == '+') year = year+100+dec;
				else year = year-100+dec;
			}else{
				int val = Convert.toInt(p);
				if (val > 9) year = val*100;
				else {
					dec = dec*10+val;
					year += dec%100;
				}
			}
			newDate();
			toControls("year");
			//if (autoAdvance && !didAll) panels.select("monthName");
		}
	}
	if (ev.type == ev.CANCELLED) exit(IDCANCEL);
	else super.onControlEvent(ev);
}

Time dateSet;
//===================================================================
public void setDate(Time t)
//===================================================================
{
	dateSet = t;
	if (!t.isValid()) t = new Time();
	String s = Time.toString(t,t instanceof TimeOfDay ? t.getFormat() : locale.getString(Locale.SHORT_DATE_FORMAT,0,0),locale);
	if (singleInput != null) singleInput.setText(s);
	day = t.day; month = t.month; year = t.year;
	monthName = locale.getString(locale.SHORT_MONTH,t.month,0);
	toControls("day,month,year,monthName");
}
//===================================================================
public void newDate()
//===================================================================
{
	Time t = new Time(day,month,year);
	Time t2 = t;
	if (!t2.isValid()) t2 = new Time(day = 1,month,year);
	if (!t2.isValid()) t2 = new Time();
	if (t2 != t){
		setDate(t2);
	}
	dayChooserTableModel dcm = (dayChooserTableModel)dayChooser.getTableModel();
	dcm.set(day,month,year);
	monthChooserTableModel mcm = (monthChooserTableModel)monthChooser.getTableModel();
	mcm.set(day,month,year);
	//dayChooser.repaintNow();
	//monthChooser.repaintNow();
}
//##################################################################
}
//##################################################################
//##################################################################
class monthChooserTableModel extends InputPanelTableModel{
//##################################################################
int chosenMonth;
Locale locale = Vm.getLocale();
//-------------------------------------------------------------------
monthChooserTableModel(Locale locale)
//-------------------------------------------------------------------
{
	this.locale = locale;
	numRows = 3;
	numCols = 4;
	hasColumnHeaders = false;
	hasRowHeaders = false;
	fillToEqualHeights = fillToEqualWidths = true;
}

//-------------------------------------------------------------------
void set(int day,int month,int year)
//-------------------------------------------------------------------
{
	int old = chosenMonth;
	chosenMonth = month;
	if (old != chosenMonth) refreshMonth(old);
}
//-------------------------------------------------------------------
int getMonthFor(int row,int cell)
//-------------------------------------------------------------------
{
	if (row == -1 || cell == -1) return 0;
	int idx = row*4+cell;
	return idx+1;
}
//-------------------------------------------------------------------
Point getCellFor(int month,Point dest)
//-------------------------------------------------------------------
{
	month--;
	int row = (month/4)%3;
	int col = month%4;
	return Point.unNull(dest).set(col,row);
}
//===================================================================
void refreshMonth(int month)
//===================================================================
{
	Point p = getCellFor(month,null);
	table.repaintCell(p.y,p.x);
}
//===================================================================
public TableCellAttributes getCellAttributes(int row,int col,boolean isSelected,TableCellAttributes ta)
//===================================================================
{
	ta.flat = true;
	ta = super.getCellAttributes(row,col,isSelected,ta);
	ta.borderStyle = fixBorder(table.BDR_OUTLINE|table.BF_BOTTOM|table.BF_RIGHT,row,col,true);
	if (getMonthFor(row,col) == chosenMonth && !isSelected){
		ta.fillColor = new Color(0x80,0x80,0xff);
	}
	return ta;
}
//===================================================================
public Object getCellText(int row,int col)
//===================================================================
{
	return locale.getString(locale.SHORT_MONTH,getMonthFor(row,col),0);
}
//===================================================================
public Object getCellData(int row,int col)
//===================================================================
{
	int month = getMonthFor(row,col);
	return new ewe.sys.Long().set(month);
}
//##################################################################
}
//##################################################################

//##################################################################
class dayChooserTableModel extends InputPanelTableModel{
//##################################################################

Vector days = new Vector();
Locale locale = Vm.getLocale();
int firstDayIndex = 0;
int numDays = 28;

int chosenDay = 0;

//-------------------------------------------------------------------
void set(int day,int month,int year)
//-------------------------------------------------------------------
{
	//ewe.sys.Vm.debug(month+","+year);
	numDays = Time.numberOfDays(month,year);
	Time t = new Time(1,month,year);
	firstDayIndex = t.indexOfDayInWeek(t.dayOfWeek,locale)-1;
	//ewe.sys.Vm.debug(""+firstDayIndex+", "+numDays);
	int oldDay = chosenDay;
	chosenDay = day;
	if (oldDay != chosenDay) refreshDay(oldDay);
}
//-------------------------------------------------------------------
int getDayFor(int row,int cell)
//-------------------------------------------------------------------
{
	if (row == -1 || cell == -1) return 0;
	int idx = row*7+cell;
	if (idx >= firstDayIndex && idx < firstDayIndex+numDays) return idx-firstDayIndex+1;
	idx = idx+35;
	if (idx >= firstDayIndex && idx < firstDayIndex+numDays) return idx-firstDayIndex+1;
	return 0;
}
//===================================================================
Point getCellFor(int day,Point dest)
//===================================================================
{
	day += firstDayIndex-1;
	int row = (day/7)%5;
	int col = day%7;
	return Point.unNull(dest).set(col,row);
}
//===================================================================
void refreshDay(int day)
//===================================================================
{
	Point p = getCellFor(day,null);
	table.repaintCell(p.y,p.x);
}
//===================================================================
public TableCellAttributes getCellAttributes(int row,int col,boolean isSelected,TableCellAttributes ta)
//===================================================================
{
	ta.flat = true;
	ta = super.getCellAttributes(row,col,isSelected,ta);
	ta.borderStyle = fixBorder(table.BDR_OUTLINE|table.BF_BOTTOM|table.BF_RIGHT,row,col,true);
	if (getDayFor(row,col) == chosenDay && !isSelected){
		ta.fillColor = new Color(0x80,0x80,0xff);
	}
	return ta;
}
//-------------------------------------------------------------------
dayChooserTableModel(Locale l)
//-------------------------------------------------------------------
{
	this.locale = l;
	numRows = 5;
	numCols = 7;
	hasColumnHeaders = true;
	hasRowHeaders = false;
	for (int i = 1; i<=7; i++) days.add(l.getString(l.SHORT_DAY_OF_WEEK,i,0));

	fillToEqualHeights = fillToEqualWidths = true;
}
//===================================================================
public boolean canSelect(int row,int col)
//===================================================================
{
	return (getDayFor(row,col) != 0);
}
//===================================================================
public Object getCellData(int row,int col)
//===================================================================
{
	if (row == -1) return days.get(col);
	else {
		int val = getDayFor(row,col);
		if (val == 0) return null;
		return Convert.toString(val);
	}
}
//##################################################################
}
//##################################################################
//##################################################################
class yearChooserTableModel extends InputPanelTableModel{
//##################################################################
//-------------------------------------------------------------------
yearChooserTableModel()
//-------------------------------------------------------------------
{
	numRows = 4;
	numCols = 4;
	hasColumnHeaders = false;
	hasRowHeaders = false;
}
String [] all = mString.split("19xx|7|8|9|20xx|4|5|6|21xx|1|2|3|18xx|0|C+|C-");

//===================================================================
public Object getCellText(int row,int col)
//===================================================================
{
	if (row >= 0 && col >= 0)
		return all[col+row*4];
	return null;
}
//===================================================================
public Object getCellData(int row,int col)
//===================================================================
{
	String str = (String)getCellText(row,col);
	if (str.length() > 2) str = str.substring(0,2);
	return str;
}
//===================================================================
public TableCellAttributes getCellAttributes(int row,int col,boolean isSelected,TableCellAttributes ta)
//===================================================================
{
	ta.flat = true;
	ta = super.getCellAttributes(row,col,isSelected,ta);
	return ta;
}
//##################################################################
}
//##################################################################

