/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.editor;
import ewe.ui.*;
import ewe.util.Iterator;
//##################################################################
public class SmartPhoneEditPerson extends mApp{
//##################################################################

static Menu sub = new Menu(new String[]{"Uno","Dos","Tres"},"Esp");

final static boolean fullScreen = true;

//-------------------------------------------------------------------
protected void setupMainWindow()
//-------------------------------------------------------------------
{
	windowFlagsToClear |= FLAG_IS_VISIBLE;
	if (fullScreen) windowFlagsToSet |= FLAG_FULL_SCREEN;
	super.setupMainWindow();
}
//===================================================================
public void run()
//===================================================================
{
	Gui.setStyle(Gui.STYLE_SOFT);
	PersonInfo pi = new PersonInfo();
	for (int i = 0; i<1; i++){
	 	final Editor f = pi.makeEditor();
		f.setObject(pi);

		final SoftKeyBar forButtons = new SoftKeyBar();
		final SoftKeyBar forInputs = new SoftKeyBar();
		SoftKeyBar sf = forButtons;
		Menu m = new Menu(new String[]{"First","Second","Third"},"One");
		sf.setKey(1,"Done",(Menu)null);
		sf.setKey(2,"Hello There",(Menu)null);
		sf = forInputs;
		sf.setKey(1,"Done",(Menu)null);
		sf.setKey(2,"A Menu Here",Form.tick,m);
		m.addItem("-");
		m.addItem(sub);
		Form top = new Form(){
			{
				windowFlagsToSet |= Window.FLAG_MAXIMIZE_ON_PDA;
				addLast(f);
				//if (!fullScreen)
				setSoftKeyBarFor(null,forButtons);
			}
			public void make(boolean reMake)
			{
				super.make(reMake);
				for (Iterator it = getAllDescendants(false); it.hasNext();){
					Control c = (Control)it.next();
					if (c instanceof mInput) {
						//if (!fullScreen)
						setSoftKeyBarFor(c,forInputs);
					}
					//else if (c instanceof mButton) setSoftKeyBarFor(c,forButtons);
				}
			}
			public void onSoftKey(int which, String action, MenuItem menu)
			{
				if (action.equalsIgnoreCase("Done")) exit(0);
			}
		};
		top.windowTitle = top.title = "Edit Person";
		top.hasTopBar = false;
		top.resizable = f.moveable = true;//false;
		top.noBorder = true;
		top.exitSystemOnClose = true;
		top.show();
	}
	//ewe.sys.Vm.exit(0);
}

//##################################################################
}
//##################################################################
