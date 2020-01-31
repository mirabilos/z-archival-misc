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
 *  produce an executableutable does not in itself require the executable to be        *
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
/**
* This holds all the information about a ButtonControl to allow it to be drawn
* and it is used by mButton objects for this purpose.
* It can also be used to draw controls which are rendered as buttons but which
* may not necessarily work the same as buttons.
**/
//##################################################################
public class ButtonObject{
//##################################################################

public Control control;
public Rect size = new Rect();
//public Rect preferredSize = new Rect();
public boolean flat;
public boolean soft;
public boolean flatInside;
public boolean small;
public boolean pressed;
public boolean enabled = true;
public boolean penIsOn = true;
public boolean transparent = false;
public String text;
public IImage image;
public int imageAnchor;
public int textPosition;
public int arrowDirection;
public Color foreground, background, border, inside;
public Font font;
public FontMetrics fm;
public boolean hasBorder = true;
public int borderStyle;
public int borderWidth;
public static ButtonObject obj = new ButtonObject();
public int alignment;
public int anchor;
//public static int standardBorder =
	//((ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS) & ewe.sys.Vm.VM_FLAG_NO_MOUSE_POINTER) != 0) ? Control.BDR_OUTLINE : 0;
//===================================================================
public ButtonObject() {}
//===================================================================

//===================================================================
public ButtonObject(Control from) {this(); update(from);}
//===================================================================

//===================================================================
public void update(Control from)
//===================================================================
{
	control = from;
	if (from != null) {
		int flags = from.getModifiers(true);
		size = from.getDim(size);
		foreground = from.getForeground();
		background = from.getBackground();
		border = from.borderColor;
		borderStyle = from.borderStyle;
		borderWidth = from.borderWidth;
		if (from instanceof mButton)
			inside = ((mButton)from).insideColor;
		else
			inside = null;
		flat = ((flags & from.DrawFlat) != 0);
		small = ((flags & from.SmallControl) != 0);
		penIsOn = ((from.penStatus & from.PenIsOn) != 0) ;
		enabled = (((flags & from.Disabled) == 0)  || ((flags & from.AlwaysEnabled) != 0));
		transparent = (flags & from.Transparent) != 0;
		text = from.makeHot(from.getDisplayText());
		font = from.getFont();
		fm = from.getFontMetrics(font);
		image = from.image;
		arrowDirection = 0;
		textPosition = mGraphics.Up;
		pressed = false;
		hasBorder = (from.borderWidth != 0);
		alignment = Control.CENTER;
		anchor = Control.CENTER;
		if (from instanceof mButton){
			mButton bb = (mButton)from;
			arrowDirection = bb.arrowDirection;
			textPosition = bb.textPosition;
		}
		if (from instanceof ButtonControl) {
			ButtonControl bc = (ButtonControl)from;
			pressed = bc.pressState;
			if (from instanceof mCheckBox) pressed |= ((mCheckBox)from).getState();
			flatInside = bc.flatInside;
			alignment = bc.alignment;
			anchor = bc.anchor;
			imageAnchor = bc.imageAnchor;
		}
		if (hasBorder){
			if ((borderStyle & (from.BF_RECT|from.BF_EXACT)) == 0) borderStyle |= from.BF_RECT|buttonEdge;
			soft = ((borderStyle & from.BF_SOFT) != 0);
			if (!penIsOn && !pressed && ((borderStyle & from.BDR_DOTTED) == 0)) {
				borderStyle = from.BDR_NOBORDER;
			}
		}
	}
}

public static int buttonEdge = Graphics.EDGE_RAISED |
(((ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS) & ewe.sys.Vm.VM_FLAG_NO_MOUSE_POINTER) != 0) ? Control.BDR_OUTLINE : 0);
public static int checkboxEdge = Graphics.EDGE_SUNKEN|Graphics.BDR_OUTLINE;

//==================================================================
public void drawButton(Graphics g)
//==================================================================
{
	Color bg = inside;//Color.LightGray;
	if (bg == null) bg = background;
	if ((flat | flatInside)&& pressed) bg = foreground;
	else if (transparent && inside == null) bg = null;
	g.setDrawOp(g.DRAW_OVER);
	//g.draw3DButton(size,pressed,bg,flat,hasBorder);
	int style = borderStyle & ~Control.BDR_DOTTED;
 	if ((style & Graphics.BF_EXACT) == 0)
		style = (pressed ?
		(Graphics.BF_BUTTON) | (Graphics.EDGE_SUNKEN & ~Graphics.BF_RECT) | (borderStyle & (Graphics.BF_RECT|Graphics.BF_SOFT|Graphics.BF_SQUARE)):
		(Graphics.BF_BUTTON) | (buttonEdge & ~Graphics.BF_RECT) | (borderStyle & (Graphics.BF_RECT|Graphics.BDR_NOBORDER|Graphics.BF_SQUARE)));
		/*
		style |=
		(hasBorder && ((borderStyle & Control.BDR_DOTTED) == 0)?
		//Graphics.BDR_OUTLINE
		borderStyle
		//Graphics
		: Graphics.BDR_OUTLINE);
		*/
	g.draw3DRect(size,style,flat,bg,border);
	if ((borderStyle & Control.BDR_DOTTED) != 0){
		size.x+=3; size.y+=3; size.width-=6; size.height-=6;
		g.draw3DRect(
			size,
			borderStyle,
			flat,
			null,
			border
			);
		size.x-=3; size.y-=3; size.width+=6; size.height+=6;
	}

}

//==================================================================
public Color getImageColor()
//==================================================================
{
	if (!enabled) return Color.DarkGray;
	if (!pressed || !flat) return foreground;
	return background;
}

//-------------------------------------------------------------------
static Rect baR = new Rect(), arrowR = new Rect(), textR = new Rect(), imageR = new Rect();
static Dimension textD = new Dimension();
static String [] txt = new String[1];
//-------------------------------------------------------------------
//==================================================================
public void paint(Graphics g)
//==================================================================
{
	//boolean db = control instanceof ButtonCheckBox;
	if (soft && control != null) control.doBackground(g);
	if (text == null) text = "";
	drawButton(g);
	Rect oldClip = g.reduceClip(baR.set(borderWidth,borderWidth,size.width-borderWidth*2,size.height-borderWidth*2));
	try{
	int inc = 0;
	if (pressed && !flat && !flatInside && !small && !soft) inc = 1;
	int aw = 9;
	if (arrowDirection == 0) aw = 0;
	Rect arrow = null;
	Rect ba = baR.set(size.x,size.y,size.width,size.height);
	ba.width -= 4; ba.height -= 4;
	ba.x = 2; ba.y = 2;

	Color fg = foreground;
	if ((flat|flatInside) && pressed) fg = background;
	if (!enabled) fg = Color.DarkGray;
	g.setColor(fg);
	if (arrowDirection == g.Left || arrowDirection == g.Right)
		arrow = arrowR.set(size.width-aw-3+(aw/2),(size.height-aw+2)/2,(aw-2)/2,aw-2);
	else if (arrowDirection == g.Up || arrowDirection == g.Down)
		arrow = arrowR.set(size.width-aw-2,(size.height-((aw-2)/2-1))/2,aw-2,(aw-2)/2);
	if (aw != 0) {
		arrow.x += inc;
		arrow.y += inc;
		ba.width -= aw;
	}
	if (arrow != null) {
		if (arrowDirection == g.Left || arrowDirection == g.Right)
			g.drawHorizontalTriangle(arrow,arrowDirection == g.Left);
		else
			g.drawVerticalTriangle(arrow,arrowDirection == g.Up);
	}//g.drawDiamond(arrow,arrowDirection);
	//......................................................
	// Calculate the text rectangle.
	//......................................................
	txt[0] = text;
	Rect textRect = textR.set(ba);
	textRect.x += 1;
	textRect.y += 1;
	textRect.width -= 2; textRect.height -= 2;
	if (image == null || textPosition == Graphics.Right){
		textRect.x += 2;
		textRect.width -= 4;
	}
	boolean hasText = text.length() != 0;
	if (!hasText) textPosition = Graphics.Down;
	//Graphics.getSize(fm,txt,0,1,textD);//Gui.getSize(fm,text,0,0);
	//textRect = textR.set(0,0,
	//if (text.length() == 0) textRect.height = textRect.width = 0;
	//else textRect.x = textRect.y = 0;
	//int dy = textRect.height;
	//......................................................
	// Calculate the image rectangle.
	//......................................................
	Rect imageRect = null;
	if (image != null) {
		imageRect = new Rect(0,0,image.getWidth(),image.getHeight());
		if (textPosition == Graphics.Up || textPosition == Graphics.Down){
			imageRect.x = (ba.width-imageRect.width)/2;
			textRect.height -= imageRect.height+2; // Only above and below.
		}else
			textRect.width -= imageRect.width+2;
		//if (db) System.out.println("IA: "+imageAnchor);
		if (hasText){
			if (textPosition == Graphics.Up){
				imageRect.y = size.height-(imageRect.height+4)-1;// += textRect.height+2;
				imageRect.x = (ba.width-imageRect.width)/2;
			}else if (textPosition == Graphics.Down){
				imageRect.x = (ba.width-imageRect.width)/2;
				imageRect.y = 3;
				textRect.y = imageRect.y+imageRect.height+2;
			}else if (textPosition == Graphics.Left){
				imageRect.y = (size.height-imageRect.height)/2;
				imageRect.x = ba.width-imageRect.width-2;
				textRect.width = ba.width-imageRect.width;
			}else{
				imageRect.y = (size.height-imageRect.height)/2;
				imageRect.x = 2;
				textRect.x = imageRect.x+imageRect.width+2;
				textRect.width = ba.width-imageRect.width;
			}
		}else if (imageAnchor != 0){
			g.anchor(imageRect,ba,imageAnchor);
			//if (db) System.out.println(imageRect+", "+ba);
			//imageRect.y = (size.height-imageRect.height)/2;
		}
	}
	//dy = (ba.height-dy)/2;
	//if (dy < 0) dy = 0;
	//textRect.y += dy;
	//if (imageRect != null) imageRect.y += dy;

	//......................................................
	// Draw the text.
	//......................................................
	if (hasText){
		txt[0] = text;
		//int tx = (ba.width-textRect.width)/2;
		g.setFont(font);
		textRect.x += inc;
		textRect.y += inc;
		//g.drawTextIn(text,ba.x+tx+inc,textRect.y+inc);
		g.drawText(fm,txt,textRect,alignment,anchor);
	}
	//......................................................
	// Draw the image.
	//......................................................
	int options = enabled ? 0 : IImage.DISABLED;
	if (image != null){
		if (hasText || imageAnchor == 0)
			image.draw(g,ba.x+imageRect.x+inc,imageRect.y+inc,options);
		else
			image.draw(g,imageRect.x+inc,imageRect.y+inc,options);
		if (image instanceof OnScreenImage)
			((OnScreenImage)image).changeRefresher(control,null);
	}

/*

	Rect ba = new Rect(
	int pw = preferredSize.width-4;
	int ph = preferredSize.height-4;
	int bx = (size.width-pw)/2;
	int by = (size.height-ph)/2;
	int inc = 0;
	int y = 0;
	if (image != null) {
		Rect d = image.getDim();
		int x = (pw-d.width)/2;
		image.draw(g,x+bx+inc,y+by+inc,0);
		y += d.height+2;
	}
	if (text.length() == 0) return;
	int w = fm.getTextWidth(text);
	int x = (pw-w)/2;
	g.setColor(getImageColor());
	g.setFont(font);
	g.drawText(text,bx+x+inc,by+y+inc);
*/
}finally{
	g.restoreClip(oldClip);
}
}

/* Old version
//==================================================================
public void paint(Graphics g)
//==================================================================
{
	if (soft && control != null) control.doBackground(g);
	if (text == null) text = "";
	drawButton(g);
	int inc = 0;
	if (pressed && !flat && !flatInside && !small && !soft) inc = 1;
	int aw = 9;
	if (arrowDirection == 0) aw = 0;
	Rect arrow = null;
	Rect ba = baR.set(size.x,size.y,size.width,size.height);
	ba.width -= 4; ba.height -= 4;
	ba.x = 2; ba.y = 2;

	Color fg = foreground;
	if ((flat|flatInside) && pressed) fg = background;
	if (!enabled) fg = Color.DarkGray;
	g.setColor(fg);
	if (arrowDirection == g.Left || arrowDirection == g.Right)
		arrow = arrowR.set(size.width-aw-3+(aw/2),(size.height-aw+2)/2,(aw-2)/2,aw-2);
	else if (arrowDirection == g.Up || arrowDirection == g.Down)
		arrow = arrowR.set(size.width-aw-2,(size.height-((aw-2)/2-1))/2,aw-2,(aw-2)/2);
	if (aw != 0) {
		arrow.x += inc;
		arrow.y += inc;
		ba.width -= aw;
	}
	if (arrow != null) {
		if (arrowDirection == g.Left || arrowDirection == g.Right)
			g.drawHorizontalTriangle(arrow,arrowDirection == g.Left);
		else
			g.drawVerticalTriangle(arrow,arrowDirection == g.Up);
	}//g.drawDiamond(arrow,arrowDirection);
	//......................................................
	// Calculate the text rectangle.
	//......................................................
	txt[0] = text;
	Rect textRect = textR.set(ba);
	textRect.x += 1;
	textRect.y += 1;
	textRect.width -= 2; textRect.height -= 2;
	if (image == null || textPosition == mGraphics.Right){
		textRect.x += 2;
		textRect.width -= 4;
	}
	boolean hasText = text.length() != 0;
	if (!hasText) textPosition = mGraphics.Down;
	//Graphics.getSize(fm,txt,0,1,textD);//Gui.getSize(fm,text,0,0);
	//textRect = textR.set(0,0,
	//if (text.length() == 0) textRect.height = textRect.width = 0;
	//else textRect.x = textRect.y = 0;
	//int dy = textRect.height;
	//......................................................
	// Calculate the image rectangle.
	//......................................................
	Rect imageRect = null;
	if (image != null) {
		imageRect = new Rect(0,0,image.getWidth(),image.getHeight());
		if (textPosition == mGraphics.Up || textPosition == mGraphics.Down){
			imageRect.x = (ba.width-imageRect.width)/2;
			textRect.height -= imageRect.height+2; // Only above and below.
		}else
			textRect.width -= imageRect.width+2;

		if (hasText){
			if (textPosition == mGraphics.Up){
				imageRect.y = size.height-(imageRect.height+4)-1;// += textRect.height+2;
				imageRect.x = (ba.width-imageRect.width)/2;
			}else if (textPosition == mGraphics.Down){
				imageRect.x = (ba.width-imageRect.width)/2;
				imageRect.y = 3;
				textRect.y = imageRect.y+imageRect.height+2;
			}else if (textPosition == mGraphics.Left){
				imageRect.y = (size.height-imageRect.height)/2;
				imageRect.x = ba.width-imageRect.width-2;
				textRect.width = ba.width-imageRect.width;
			}else{
				imageRect.y = (size.height-imageRect.height)/2;
				imageRect.x = 2;
				textRect.x = imageRect.x+imageRect.width+2;
				textRect.width = ba.width-imageRect.width;
			}
		}else{
			imageRect.y = (size.height-imageRect.height)/2;
		}
	}
	//dy = (ba.height-dy)/2;
	//if (dy < 0) dy = 0;
	//textRect.y += dy;
	//if (imageRect != null) imageRect.y += dy;

	//......................................................
	// Draw the text.
	//......................................................
	if (hasText){
		txt[0] = text;
		//int tx = (ba.width-textRect.width)/2;
		g.setFont(font);
		textRect.x += inc;
		textRect.y += inc;
		//g.drawTextIn(text,ba.x+tx+inc,textRect.y+inc);
		g.drawText(fm,txt,textRect,alignment,anchor);
	}
	//......................................................
	// Draw the image.
	//......................................................
	int options = enabled ? 0 : IImage.DISABLED;
	if (image != null){
		int tx = imageRect.x;//(ba.width-imageRect.width)/2;
		image.draw(g,ba.x+tx+inc,imageRect.y+inc,options);
	}
}
*/
//==================================================================
public Dimension calculateSize(Dimension dest)
//==================================================================
{
	dest = Dimension.unNull(dest);
	if (text == null) text = "";
	int w = 0, h = 0;
	if (image != null) {
		w = image.getWidth();
		h = image.getHeight();
	}
	if (image == null || (text.length() != 0)) {
		Rect r = Gui.getSize(fm,text,2,2);
		if (textPosition == mGraphics.Up || textPosition == mGraphics.Down){
			h += r.height;
		}else
			if (r.height > h) h = r.height;
		if (image != null) h += 2;
		if (textPosition == mGraphics.Up || textPosition == mGraphics.Down){
			if (r.width > w) w = r.width;
		}else
			w += r.width+4;
	}
	if (arrowDirection != 0) {
		w += 10;
		if (h < 10) h = 10;
	}
	dest.width = w+6;
	dest.height = h+6;
	return dest;
}
//##################################################################
}
//##################################################################

