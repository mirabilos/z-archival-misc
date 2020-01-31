/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.paint;
import ewe.fx.Color;
import ewe.fx.Graphics;
import ewe.fx.IImage;
import ewe.fx.Image;
import ewe.fx.Pen;
import ewe.fx.mImage;
import ewe.ui.ButtonCheckBox;
import ewe.ui.CellPanel;
import ewe.ui.CheckBoxGroup;
import ewe.ui.ControlEvent;
import ewe.ui.mCheckBox;

//##################################################################
public class PenPalette extends CellPanel{
//##################################################################

CheckBoxGroup cbg = new CheckBoxGroup();

//===================================================================
public static IImage getPenImage(int size)
//===================================================================
{
	int w,h;
	Image img = new Image(w = 10,h = 20,Image.RGB_IMAGE);
	Graphics g = new Graphics(img);
	g.setColor(Color.White);
	g.fillRect(0,0,w,h);
	g.setPen(new Pen(Color.Black,Pen.SOLID,size));
	g.drawLine(0,h/2,w-1,h/2);
	mImage mi = new mImage();
	mi.setImage(img);
	mi.transparentColor = Color.White;
	return mi;
}

//===================================================================
public ButtonCheckBox next(int size){return next(size,false);}
//===================================================================

//===================================================================
public ButtonCheckBox next(int size,boolean select)
//===================================================================
{
	ButtonCheckBox bcb = new ButtonCheckBox(getPenImage(size));
	bcb.prompt = ""+size;
	bcb.setGroup(cbg);
	if (select) bcb.setState(true);
	return bcb;
}
//===================================================================
public PenPalette() {this(false);}
//===================================================================

//===================================================================
public PenPalette(boolean vertical)
//===================================================================
{
	addNext(next(1));
	addNext(next(2));
	addNext(next(3,true)); if (vertical) endRow();
	addNext(next(4));
	addNext(next(5));
	//endRow();
	addNext(next(7)); if (vertical) endRow();
	addNext(next(10));
	addNext(next(15));
	addNext(next(20)); if (vertical) endRow();
}

//===================================================================
public int getThickness()
//===================================================================
{
	mCheckBox cb = cbg.getSelected();
	if (cb != null) return ewe.sys.Convert.toInt(cb.prompt);
	return 1;
}

//-------------------------------------------------------------------
protected void newPenChosen(int thickness){}
//-------------------------------------------------------------------

//===================================================================
public void onControlEvent(ControlEvent ev)
//===================================================================
{
	if (ev.type == ev.PRESSED){
		newPenChosen(getThickness());
	}else
		super.onControlEvent(ev);
}


//##################################################################
}
//##################################################################
