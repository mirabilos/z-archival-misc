/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.paint;
import ewe.ui.ControlPopupForm;

//##################################################################
public class PenChooserPopup extends ControlPopupForm{
//##################################################################

//===================================================================
public PenChooserPopup()
//===================================================================
{
	modify(0,DrawFlat);
	PenPalette cp;
	addLast(cp = new PenPalette(true){
		protected void newPenChosen(int thickness){
			PenChooserPopup.this.exit(IDOK);
			((PenChooserButton)client).setInt(thickness);
			client.notifyDataChange();
		}
	});
	cp.modifyAll(SendUpKeyEvents,0,true);
	setBorder(EDGE_ETCHED,2);
}

//##################################################################
}
//##################################################################
