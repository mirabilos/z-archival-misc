/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.paint;
import ewe.fx.Color;
import ewe.ui.ControlPopupForm;

//##################################################################
public class ColorChooserPopup extends ControlPopupForm{
//##################################################################
//===================================================================
public ColorChooserPopup()
//===================================================================
{
	modify(0,DrawFlat);
	ColorPalette cp;
	addLast(cp = new ColorPalette(PaintForm.colors,4){
		protected void newColorChosen(Color c){
			ColorChooserPopup.this.exit(IDOK);
			((ColorChooserButton)client).setData(c);
			client.notifyDataChange();
		}
	});
	setBorder(EDGE_ETCHED,2);
	cp.modifyAll(SendUpKeyEvents,0,true);
}

//##################################################################
}
//##################################################################
