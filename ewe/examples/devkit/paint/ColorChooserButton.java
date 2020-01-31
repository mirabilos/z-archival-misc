/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.paint;
import ewe.fx.Color;
import ewe.ui.mButton;

//##################################################################
public class ColorChooserButton extends mButton{
//##################################################################

Color myColor = new Color(0,0,0);

//===================================================================
public ColorChooserButton()
//===================================================================
{
	super(null,"samples/paint/paletteicon.bmp",ewe.fx.Color.White);
	new ColorChooserPopup().attachTo(this);
	setToolTip("Choose Color");
}
public void setData(Object data){
	if (data instanceof Color)
		myColor.set((Color)data);
}
public void getData(Object data){
	if (data instanceof Color)
		((Color)data).set(myColor);
}
//##################################################################
}
//##################################################################
