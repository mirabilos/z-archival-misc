/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.paint;
import ewe.fx.Color;
import ewe.fx.Insets;
import ewe.ui.CellPanel;
import ewe.ui.CheckBoxGroup;
import ewe.ui.ControlEvent;
import ewe.ui.mCheckBox;

//##################################################################
public class ColorPalette extends CellPanel{
//##################################################################

CheckBoxGroup cbg = new CheckBoxGroup();
//===================================================================
public ColorPalette(int [] colors,int columns)
//===================================================================
{
	defaultTags.set(INSETS,new Insets(1,1,0,0));
	for (int i = 0, c = 0;i<colors.length; i++){
		mCheckBox cb;
		addNext(cb = new ColorButton().setColor(new Color((colors[i] & 0xff0000) >> 16,(colors[i] & 0xff00) >> 8,colors[i] & 0xff)));
		cb.setGroup(cbg);
		if (i == 0) cb.setState(true);
		c++;
		if (c == columns) {
			c = 0;
			endRow();
		}
	}
}

//===================================================================
public Color getChosen()
//===================================================================
{
	ColorButton cb = (ColorButton)cbg.getSelected();
	if (cb == null) return null;
	return cb.getColor();
}

//===================================================================
public void onControlEvent(ControlEvent ev)
//===================================================================
{
	if (ev.type == ev.PRESSED){
		newColorChosen(getChosen());
	}else
		super.onControlEvent(ev);
}

//-------------------------------------------------------------------
protected void newColorChosen(Color c)
//-------------------------------------------------------------------
{
}
//##################################################################
}
//##################################################################
