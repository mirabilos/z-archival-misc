/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.ui;
import ewe.fx.Color;
import ewe.fx.Insets;
import ewe.fx.mImage;
import ewe.ui.ButtonListSelect;
import ewe.ui.CellPanel;
import ewe.ui.Control;
import ewe.ui.ControlEvent;
import ewe.ui.Editor;
import ewe.ui.Gui;
import ewe.ui.InputStack;
import ewe.ui.Menu;
import ewe.ui.MenuBar;
import ewe.ui.MenuItem;
import ewe.ui.SoftKeyBar;
import ewe.ui.mButton;
import ewe.ui.mTextPad;
import ewe.util.Vector;
//##################################################################
public class TestForm extends Editor{
//##################################################################

private Control one;

//===================================================================
public void onControlEvent(ControlEvent ev)
//===================================================================
{
	if (ev.type == ev.PRESSED){
		ewe.sys.Vm.debug("Pressed!");
	}
	super.onControlEvent(ev);
}

//===================================================================
public boolean handleAction(String action)
//===================================================================
{
	ewe.sys.Vm.debug("Action: "+action);
	return super.handleAction(action);
}
//===================================================================
public TestForm()
//===================================================================
{
	title = "Test Toolbars";
	CellPanel [] p = addToolbar();
	p[0].defaultTags.set(INSETS,new Insets(0,1,0,1));
	//
	MenuBar mb = new MenuBar();
	Menu m = new Menu(new String[]{"Open","Save","-","Quit"},"File");
	mb.addMenu(m,"File");
	p[0].addNext(mb).setCell(DONTSTRETCH);

	mButton bb = new mButton("One","ewe/ewesmall.bmp",Color.White);
	one = bb;
	Vector bs = new Vector();
	bs.add(one);
	m = buttonsToMenu(bs,null);
	mb.addMenu(m,"Test");
	//
	// Add a toolbutton.
	//
	Control c = new mButton(new mImage("ewe/savesmall.bmp",Color.White));
	c.setToolTip("Save Document");
	p[0].addNext(c).setCell(DONTSTRETCH);
	c = new mButton(new mImage("ewe/exitsmall.bmp",Color.White));
	c.setToolTip("Quit");
	p[0].addNext(c).setCell(DONTSTRETCH);

	//
	// Add main data control, a mTextPad in this case.
	//
	InputStack is = new InputStack();
	ButtonListSelect bls = new ButtonListSelect();
	is.add(bls,"Category");
	bls.setData(
		new Vector(new String[]{"One","Two","Three","Four","Five","Six"}),null);
		//new Vector(new String[]{"Two","Three"}));
	p[1].addLast(is).setCell(HSTRETCH);
	p[1].addLast(new mTextPad(20,40));

	if (Gui.isSmartPhone){
		SoftKeyBar skb = makeSoftKeys();
		MenuItem[] mis = new MenuItem[]{
			makeMenuItemForForm("Uno","uu",tick),
			makeMenuItemForForm("Dos","dd",cross),
			makeMenuItemForForm("Tres","rr",stop)
		};
		Menu mm = skb.createMenuFor(mis);//(new String[]{"Uno|uu","-","Dos|dd","Tres|rr"});
		mm.text = "SubMenu";
		makeSoftKeys(new Object[]{"First|ff","-",mm,"Second|ss","Third|tt"},"Actions","Exit|ex",null);
	}
}

//##################################################################
}
//##################################################################
