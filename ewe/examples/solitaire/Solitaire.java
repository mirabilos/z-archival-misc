import ewe.fx.Rect;
import ewe.ui.Form;
import ewe.ui.Window;

//##################################################################
public class Solitaire{
//##################################################################

//===================================================================
static void mb(String text)
//===================================================================
{
	ewe.sys.Vm.messageBox("Solitaire",text,ewe.sys.Vm.MB_OK);
}
//===================================================================
public static void main(String args[])
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	//Application.addFont(new Font("Helvetica",Font.BOLD,16),"gui");
	//mb("Main started!");
 	Rect s = (Rect)Window.getGuiInfo(Window.INFO_SCREEN_RECT,null,new Rect(),0);
	//mb("Size: "+s);
  boolean small = s.height <= 300;
	Form f = new SolitaireForm(small);
	f.execute();//mApp.appFrame,Gui.FILL_FRAME);
	ewe.sys.Vm.exit(0);
}
//##################################################################
}
//##################################################################
