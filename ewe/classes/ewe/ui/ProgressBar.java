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
import ewe.sys.*;
import ewe.fx.*;
//##################################################################
public class ProgressBar extends Control{
//##################################################################

int drawWidth = 0;
float progress = 0;
int percent = 0;
public boolean showPercent = true;
public boolean showTimeLeft = false;
/**
* If you set this to a non null value, then a message will be displayed along
* with the percentage value.
**/
public String doing = null;
public long timeStarted;

public Color incompleteColor = Color.White;
public Color barColor = new Color(0,0,0x80);

{
	borderStyle = mInput.inputEdge|BF_RECT;
}

//-------------------------------------------------------------------
protected void calculateSizes()
//-------------------------------------------------------------------
{
	preferredWidth = 100; preferredHeight = 20;
}
//===================================================================
public void reset()
//===================================================================
{
	drawWidth = 0;
	doing = null;
	repaintNow();
}

private long lastSet = ewe.sys.Vm.getTimeStampLong();

public int bounceSteps = 10;
public int bounceTimeMillis = 2000;


/**
 * Set the bar progress to the specified value.
 * @param progress A value between 0.0 and 1.0 or -1.
 */
//===================================================================
public void set(float progress)
//===================================================================
{
	set(progress,0);
}
/**
 * Set the bar progress to the specified value.
 * @param progress A value between 0.0 and 1.0 or -1.
* @param startTime A value indicating when the operation started. This
* may be zero if it is unknown.
*/
//===================================================================
public void set(float progress,long startTime)
//===================================================================
{
	timeStarted = startTime;
	long now = ewe.sys.Vm.getTimeStampLong();
	float lastProgress = this.progress;
	this.progress = progress;
	float value = progress;
	if (value < 0){
		if (now-lastSet >= bounceTimeMillis/bounceSteps){
			lastSet = now;
			if (percent >= 0) percent = -1;
			else percent--;
			if (percent <= (-bounceSteps*2)) percent = -2;
			repaintNow();
		}else if (percent >= 0) percent = -1;
		return;
	}
	percent = (int)(value*100);
	int newWidth = (int)((float)(width-4)*value);
	if (newWidth != drawWidth || lastProgress < 0){
		drawWidth = newWidth;
		repaintNow();
		lastSet = now;
	}else if ((now-lastSet >= 1000) && showTimeLeft){
		repaintNow();
		lastSet = now;
	}
}
static ImageBuffer buffer;

Time now;
StringBuffer sb;
//-------------------------------------------------------------------
String timeLeft()
//-------------------------------------------------------------------
{
	if (timeStarted == 0 || progress <= 0) return null;
	if (now == null) now = new Time();
	now.setToCurrentTime();
	long taken = now.getTime()-timeStarted;
	int need = (int)(((taken/progress)-taken)/1000);
	if (need < 0) return null;
	int hours = need/(60*60);
	need = need%(60*60);
	int minutes = need/60;
	need = need%60;
	int seconds = need;
	if (sb == null) sb = new StringBuffer();
	sb.setLength(0);
	if (hours != 0){
		sb.append(hours); sb.append(" hr. ");
	}
	if (minutes != 0 || hours != 0){
		sb.append(minutes); sb.append(" min. ");
	}
	if (hours == 0){
		sb.append(seconds); sb.append(" sec. ");
	}
	sb.append("left.");
	return sb.toString();
}
//===================================================================
public void doPaint(Graphics g,Rect area)
//===================================================================
{
	Rect r = getDim(Rect.buff);
	int flags = getModifiers(true);
	int blockWidth = 0, blockPos = 0;
	if ((borderStyle & BF_SOFT) != 0) doBackground(g);
	g.draw3DRect(
			getDim(Rect.buff),
			borderStyle, //Graphics.EDGE_SUNKEN|Graphics.BDR_OUTLINE,
			(flags & DrawFlat) != 0,
			incompleteColor,
			borderColor);

	if (percent < 0){
		double w = (r.width-4)/(double)(bounceSteps);
		int p = (-percent)-1;
		blockWidth = (int)(r.width-4-((bounceSteps-1)*w)-1);
		if (p >= bounceSteps) p = bounceSteps*2-p-2;
		if (p < 0) p = 0;
		blockPos = (int)(p*w+2);
		g.setColor(barColor);
		g.fillRect(blockPos,2,blockWidth,r.height-4);
	}else{
		if (drawWidth > r.width-4) drawWidth = r.width-4;
		else if (drawWidth < 0) drawWidth = 0;
		if (false && ((borderStyle & BF_SOFT) != 0) && !showPercent && doing == null){
	/*
			r = new Rect().set(Rect.buff);
			if (buffer == null) buffer = new ImageBuffer();
			Graphics gi = buffer.get(drawWidth,r.height-4,true);
			gi.setColor(incompleteColor);
			gi.fillRect(0,0,drawWidth,r.height-4);
			int rw = 12;
			int num = drawWidth/rw;
			int h = r.height-4, w = rw-1;
			for (int i = 0; i<num; i++)
					gi.draw3DRect(Rect.buff.set(rw*i,0,w,h),borderStyle,false,barColor,null);
			g.drawImage(buffer.image,2,2);
	*/
		}else{
			g.setColor(barColor);
			g.fillRect(2,2,drawWidth,r.height-4);
		}
	}
	String toPrint = new String();
	if (showPercent && percent >= 0) toPrint += percent+"%"+ (doing == null ? "" : " - ");
	if (doing != null) toPrint += doing;
	if (showTimeLeft){
		String left = timeLeft();
		if (left != null){
			if (toPrint.length() != 0) toPrint += " - ";
			toPrint += left;
		}
	}
	if (toPrint.length() != 0) {
		toPrint = toPrint.trim();
		Rect cr = g.getClip(new Rect());
		FontMetrics fm = getFontMetrics();
		String s = toPrint;
		int w = fm.getTextWidth(s);
		int h = fm.getAscent()+fm.getDescent();
		int xpos = (doing == null ? (r.width-w)/2 : 4);
		g.setFont(fm.getFont());
		if (percent >= 0){
			g.setColor(incompleteColor);
			g.setClip(0,0,drawWidth+2,r.height);
			g.drawText(s,xpos,((r.height-h)/2)/*+fm.getAscent()*/);//,(d.height-h)/2);
			g.setClip(drawWidth+2,0,r.width-drawWidth-4,r.height);
			g.setColor(barColor);
			g.drawText(s,xpos,((r.height-h)/2)/*+fm.getAscent()*/);//,(d.height-h)/2);
		}else{
			g.setColor(barColor);
			g.drawText(s,xpos,((r.height-h)/2));
			g.setColor(incompleteColor);
			g.reduceClip(new Rect(blockPos,0,blockWidth,r.height));
			g.drawText(s,xpos,((r.height-h)/2));
		}
		if (cr != null) g.setClip(cr.x,cr.y,cr.width,cr.height);
	}
}
//##################################################################
}
//##################################################################

