/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.paint;
import ewe.ui.mButton;

//##################################################################
public class PenChooserButton extends mButton{
//##################################################################

public int thickness = 1;

//===================================================================
public PenChooserButton()
//===================================================================
{
	super(null,"samples/paint/penpalette.bmp",ewe.fx.Color.White);
	new PenChooserPopup().attachTo(this);
	setToolTip("Choose Pencil");
}
public void setInt(int value){
	thickness = value;
}
public int getInt(){
	return thickness;
}
//##################################################################
}
//##################################################################
