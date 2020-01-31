/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.paint;
import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.ui.ButtonCheckBox;

//##################################################################
public class ColorButton extends ButtonCheckBox{
//##################################################################

public static Dimension buttonSize = new Dimension(10,10);

//===================================================================
public ColorButton()
//===================================================================
{
	super("");
	modify(MouseSensitive,0);
	setPreferredSize(12,12);
}

//===================================================================
public Color getColor() {return backGround;}
//===================================================================

//===================================================================
public ColorButton setColor(Color c)
//===================================================================
{
	backGround = c;
	/*
	Image i = new Image(buttonSize.width,buttonSize.height);
	Graphics g = new Graphics(i);
	g.setColor(c);
	g.fillRect(0,0,buttonSize.width,buttonSize.height);
	g.free();
	image = new mImage(i);
	*/
	return this;
}
//##################################################################
}
//##################################################################
