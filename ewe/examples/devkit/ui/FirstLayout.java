/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.ui;
import ewe.ui.*;

//##################################################################
public class FirstLayout {
//##################################################################

//===================================================================
public static void main(String args[])
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	Form f = new Form();
	f.title = "First layout!";
	f.exitSystemOnClose = true;
	f.resizable = true;
	f.moveable = true;
	// First Row.
	f.addNext(new mButton("One"),f.DONTSTRETCH,f.FILL);
	f.addLast(new mButton("Two-wide"),f.HSTRETCH,f.FILL);
	// New Row.
	f.addNext(new mButton("Three"),f.VSTRETCH,f.FILL);
	f.addLast(new mButton("Four"),f.STRETCH,f.FILL);
	//Try uncommenting the line below to see its effect.
	//f.equalWidths = true;
	f.execute();
	ewe.sys.Vm.exit(0);
}
//##################################################################
}
//##################################################################
