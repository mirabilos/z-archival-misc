/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.paint;
import ewe.fx.Dimension;
import ewe.fx.Point;
import ewe.ui.Control;
import ewe.ui.Editor;
import ewe.ui.Gui;
import ewe.ui.InputStack;
import ewe.ui.Window;

//##################################################################
public class PaintDebugger{
//##################################################################
public int repeats = 10000;
public int moves = 30;
public int pause = 1;
public int minX = 0;
public int minY = 0;
public int maxX = 0;
public int maxY = 0;
public Window window;

//===================================================================
public void go(Control forWho)
//===================================================================
{
	Point p = Gui.getPosInParent(forWho,window = forWho.getWindow());
	minX = p.x; minY = p.y;
	Dimension d = forWho.getSize(null);
	maxX = minX+d.width;
	maxY = minY+d.height;
	ewe.sys.Vm.debugObject(this,1);
}
//===================================================================
public Editor getEditor()
//===================================================================
{
	Editor ed = new Editor(this);
	ed.title = "Auto-moves";
	InputStack is = new InputStack();
	ed.addLast(is);
	is.addInputs(ed,"Repeats:|repeats|Moves:|moves|Pause:|pause");
	Gui.setOKCancel(ed);
	return ed;
}
//##################################################################
}
//##################################################################
