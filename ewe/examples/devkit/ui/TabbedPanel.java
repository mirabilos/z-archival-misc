/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.ui;
import ewe.ui.*;
import ewe.fx.*;

//##################################################################
public class TabbedPanel extends Editor{
//##################################################################

//===================================================================
public TabbedPanel()
//===================================================================
{
	title = "mTabbedPanel Demo";

	mTabbedPanel mt = new mTabbedPanel();
	addLast(mt);
	// Enable this line to see the difference it makes
	// when you shrink the display.
	//
	//mt.cardPanel.autoScroll = false;
	//
	// Make your controls and add them to the tabbed panel.
	//
	Editor ed = new ewe.database.TestData().getEditor(0);
	mt.addItem(ed,"Data Editor",null);
	//
	ed = new DateChooser(null);
	mt.addItem(ed,"Date Chooser",null);
	//
	CellPanel cp = new CellPanel();
	cp.addNext(new mButton("Button One")).setCell(HSTRETCH);
	cp.addLast(new mButton("Button Two")).setCell(HSTRETCH);
	InputStack is = new InputStack();
	is.addInput("First:","Hello");
	is.addInput("Second:","Two");
	is.addInput("Third:","Three");
	cp.addLast(is);
	mt.addItem(cp,"Random Controls",null);
	//
	// Putting Icons in.
	//
	Card c = mt.getItem(0);
	c.iconize("ewe/editsmall.bmp",Color.White);
	//
	// You can add more icons if you like.
	//
}
//##################################################################
}
//##################################################################
