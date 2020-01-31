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
import ewe.util.Range;
import ewe.util.Vector;
import ewe.sys.Time;
import ewe.sys.Locale;
import ewe.fx.FontMetrics;
import ewe.sys.Convert;
/**
This inputs a time of day value using a set of UpDownInputs.
**/
//##################################################################
public class DateUpDownInput extends Holder{
//##################################################################

UpDownInput day, month, century, year;
private static Range dayRange = new Range(1,31);
private static Range yearRange = new Range(0,99);
private static Vector monthRange = new Vector();
private static int monthWidth = 0;

static {
	Locale l = Locale.getDefault();
	for (int i = 0; i<12; i++){
		String r = l.getString(l.SHORT_MONTH,i+1,0);
		monthRange.add(r);
		monthWidth = Math.max(r.length(),monthWidth);
	}
}

//-------------------------------------------------------------------
mLabel getLabel(String what)
//-------------------------------------------------------------------
{
	mLabel l = new mLabel(what);
	l.spacing = 1;
	setControl(DONTFILL|CENTER);
	return l;
}

CalendarCanvas calendar;

//===================================================================
public void setLocale(Locale locale)
//===================================================================
{
	calendar.setLocale(locale);
}
//===================================================================
public DateUpDownInput(boolean showCalendar)
//===================================================================
{
	if (showCalendar) {
		calendar = new CalendarCanvas();
		calendar.setFont(mApp.findFont("small",true));
	}
	addNext(month = new UpDownInput(monthWidth));
	addNext(getLabel("/"));
	addNext(day = new UpDownInput(2));
	addNext(getLabel("/"));
	addNext(century = new UpDownInput(2));
	addNext(year = new UpDownInput(2));
	day.integerDigits = month.integerDigits = century.integerDigits = year.integerDigits = 2;
	day.dataChangeOnEachPress = month.dataChangeOnEachPress = century.dataChangeOnEachPress = year.dataChangeOnEachPress = true;
	day.integerValues = dayRange;
	month.textValues = monthRange;
	century.integerValues = year.integerValues = yearRange;
	century.borderStyle = BDR_OUTLINE|BF_LEFT|BF_TOP|BF_BOTTOM;
	year.borderStyle = BDR_OUTLINE|BF_RIGHT|BF_TOP|BF_BOTTOM;
	year.zeroFillInteger = true;
	century.anchor = EAST;
	year.anchor = WEST;
	setTime(new Time());
}


/**
Set the Time the input displays.
@param t The Time to display.
*/
//===================================================================
public void setTime(Time t)
//===================================================================
{
	day.setInt(t.day);
	month.setInt(t.month);
	century.setInt(t.year/100);
	year.setInt(t.year%100);
	day.integerValues.last = Time.numberOfDays(t.month,t.year);
	updateCalendar();
}
/**
Get the time displayed/entered.
@param dest an optional destination Time which may be null.
@return the destination Time or a new Time if dest is  null.
*/
//===================================================================
public Time getTime(Time dest)
//===================================================================
{
	if (dest == null) dest = new Time();
	dest.day = day.getInt();
	dest.month = month.getInt();
	dest.year = century.getInt()*100+year.getInt();
	int nd = Time.numberOfDays(dest.month,dest.year);
	if (dest.day > nd){
		dest.day = 1;
		day.setInt(1);
	}
	dest.update();
	return dest;
}
/*
//Locale locale;

//-------------------------------------------------------------------
private String pad(String what)
//-------------------------------------------------------------------
{
	while (what.length() < 4) what = " "+what;
	return what;
}
*/
private static Time cal;

//-------------------------------------------------------------------
private void updateCalendar()
//-------------------------------------------------------------------
{
	if (calendar == null) return;
	if (cal == null) cal = new Time();
	getTime(cal);
	calendar.update(cal);
}
//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof DataChangeEvent){
		if (ev.target == month || ev.target == year)
			day.integerValues.last = Time.numberOfDays(month.getInt(),year.getInt());
		updateCalendar();
	}else
		super.onEvent(ev);
}

private static ControlPopupForm pop, popC;

//===================================================================
public static ControlPopupForm getPopup(boolean showCalendar)
//===================================================================
{
	if (showCalendar)
			return popC == null ? (popC = new DateUpDownInputPopup(showCalendar)) : popC;
	else
			return pop == null ? (pop = new DateUpDownInputPopup(showCalendar)) : pop;
}

//##################################################################
public static class DateUpDownInputPopup extends ControlPopupForm{
//##################################################################
DateUpDownInput input;
//===================================================================
public DateUpDownInputPopup(boolean showCalendar)
//===================================================================
{
	//putByClient = false;
	setBorder(BDR_OUTLINE|BF_RECT,1);
	backGround = Color.White;
	CellPanel cp = new CellPanel();
	cp.addNext(input = new DateUpDownInput(showCalendar));
	addCloseControls(cp);
	addLast(cp);
	if (showCalendar) {
		endRow();
		addLast(input.calendar);
	}
}
//===================================================================
public void onControlEvent(ControlEvent ev)
//===================================================================
{
	if (ev.target == input){
		if (ev.type == ev.PRESSED)
			exit(IDOK);
		else if (ev.type == ev.CANCELLED)
			exit(IDCANCEL);
	}
	else super.onControlEvent(ev);
}
//-------------------------------------------------------------------
protected void transferToClient(Control client)
//-------------------------------------------------------------------
{
	if (client instanceof DateTimeInput)
		((DateTimeInput)client).setTime(input.getTime(null));
}
/**
* This is called by setFor(Control who) and gives you an opportunity to
* modify the Form based on the client control.
* @param who The new client control.
*/
//------------------------------------------------------------------
protected void startingInput(Control who)
//-------------------------------------------------------------------
{
	if (who instanceof DateTimeInput)
		input.setTime(((DateTimeInput)client).getTime(null));
}
//##################################################################
}
//##################################################################

/*
//##################################################################
public static class DateUpDownInputPopup extends InputPopup{
//##################################################################

DateUpDownInput input;

//===================================================================
public DateUpDownInputPopup(boolean showCalendar)
//===================================================================
{
	CellPanel cp = new CellPanel();
	addMainControls(cp).setControl(DONTFILL|CENTER);
	cp.addNext(input = new DateUpDownInput(showCalendar));
	addCloseControls(cp);
	endRow();
	if (input.calendar != null){
		addLast(input.calendar).setControl(DONTFILL|CENTER);
		firstFocus = input.day;
	}
}

//===================================================================
public boolean canExit(int exitCode)
//===================================================================
{
	if (exitCode != IDOK) return true;
	Time got = getTime(null);
	if (!got.isValid()){
		beep();
		Gui.flashMessage("Invalid Date",getFrame());
		Gui.takeFocus(input,ByRequest);
		return false;
	}
	return true;
}
//===================================================================
public Time getTime(Time dest)
//===================================================================
{
	return input.getTime(dest);
}
//===================================================================
public void setTime(Time time)
//===================================================================
{
	input.setTime(time);
}

//##################################################################
}
//##################################################################
*/
/*
//=================================================================
public static void main(String[] args)
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	Form f = new Form();
	DateDisplayInput ddi = new DateDisplayInput();

	InputStack is = new InputStack();
	is.add(ddi,"Date:");
	f.execute();
	ewe.sys.Vm.exit(0);
}*/
//##################################################################
}
//##################################################################

//##################################################################
class CalendarCanvas extends Canvas{
//##################################################################

static FontMetrics fontMetrics;
static int[] widths;// = new int[31];
static int[] dwidths;
static String[] dates;// = new String[]{
static String[] days;
static int maxWidth = 0, cellWidth = 0, cellHeight = 0;
Locale locale;

/*
This is meant only to be used in a popup - where only one is on-screen at a time.
So can use static values here.
*/
private static int dw[], dm[];
private int lastMonth = 0, lastYear = 0;

//===================================================================
CalendarCanvas()
//===================================================================
{
	modify(NoFocus,0);
	locale = Locale.getDefault();
	borderWidth = 1;
	borderStyle = BF_TOP;
}
//-------------------------------------------------------------------
void setLocale(Locale locale)
//-------------------------------------------------------------------
{
	if (locale == null) locale = new Locale("es",null,false);
	if (locale == null) locale = Locale.getDefault();
	this.locale = locale;
}
//-------------------------------------------------------------------
protected void calculateSizes()
//-------------------------------------------------------------------
{
	FontMetrics fm = getFontMetrics();
	if (fm != fontMetrics || widths == null){
		fontMetrics = fm;
		widths = new int[31];
		dates = new String[widths.length];
		for (int i = 0; i<widths.length; i++){
			dates[i] = Convert.toString(i+1);
			widths[i] = fm.getTextWidth(dates[i]);
			if (widths[i] > maxWidth) maxWidth = widths[i];
		}
		dwidths = new int[7];
		days = new String[7];
		for (int i = 0; i<7; i++){
			days[i] = Convert.toString(locale.getString(locale.SHORT_DAY,i+1,0).charAt(0));
			dwidths[i] = fm.getTextWidth(days[i]);
			if (dwidths[i] > maxWidth) maxWidth = dwidths[i];
		}
	}
	cellWidth = maxWidth+4;
	cellHeight = fm.getHeight();
	preferredWidth = cellWidth*7;
	preferredHeight = cellHeight*6+4;
}
int selectedDay = 0, firstDayIndex = 0;
int top = 2;
//-------------------------------------------------------------------
void update(Time cal)
//-------------------------------------------------------------------
{
	if (dw == null){
		dw = new int[7];
		dm = new int[35];
	}
	if (cal.month != lastMonth || cal.year != lastYear){
		locale.getCalendarForMonth(cal,dw,dm,true);
		for (int i = 0; i<7; i++)
			if (dm[i] == 1) firstDayIndex = i;
		/*
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i<7; i++)
			sb.append(pad(locale.getString(locale.SHORT_DAY,i,0)));
		sb.append("\n");
		for (int i = 0; i<35; i++){
			if (dm[i] == 0) sb.append("    ");
			else sb.append(pad(""+dm[i]));
			if (((i+1)%7) == 0) sb.append("\n");
		}
		System.out.println(sb);
		*/
		selectedDay = cal.day;
		repaintNow();
	}else{
		if (cal.day != selectedDay){
			int sd = selectedDay;
			selectedDay = cal.day;
			Graphics g = getGraphics();
			paintDay(sd,g);
			paintDay(selectedDay,g);
			g.free();
		}
	}
	lastMonth = cal.month;
	lastYear = cal.year;
}
//-------------------------------------------------------------------
void paintDay(int day,Graphics gr)
//-------------------------------------------------------------------
{
	if (requestPaint()){
		Graphics g = gr;
		if (g == null) g = getGraphics();
		if (g == null) return;
		int idx = (firstDayIndex+(day-1)) % 35;
		int x = (idx%7)*cellWidth, y = top+((idx/7)+1)*cellHeight;
		g.setColor(getBackground());
		g.fillRect(x,y,cellWidth,cellHeight);
		g.setFont(getFont());
		g.setColor(getForeground());
		g.drawText(dates[day-1],x+((cellWidth-widths[day-1])/2),y);
		if (day == selectedDay && selectedDay != 0)
			g.drawRect(x,y,cellWidth,cellHeight);
		if (g != gr) g.free();
	}
}
//===================================================================
public void doPaint(Graphics g, Rect Area)
//===================================================================
{
	super.doBackground(g);
	super.doBorder(g);
	g.setFont(getFont());
	g.setColor(getForeground());
	if (dw == null) return;
	int x = 0;
	for (int i = 0; i<7; i++){
		int idx = dw[i]-1;
		g.drawText(days[idx],x+((cellWidth-dwidths[idx])/2),top);
		x += cellWidth;
	}
	int v = 0;
	for (int r = 1; r<=5; r++){
		int y = top+r*cellHeight;
		x = 0;
		for (int i = 0; i<7; i++){
			int idx = dm[v++]-1;
			if (idx != -1)
				g.drawText(dates[idx],x+((cellWidth-widths[idx])/2),y);
			if (idx+1 == selectedDay && selectedDay != 0)
				g.drawRect(x,y,cellWidth,cellHeight);
			x += cellWidth;
		}
	}
}
//##################################################################
}
//##################################################################

