/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.ui;
import ewe.ui.CellPanel;
import ewe.ui.Control;
import ewe.ui.DateTimeInput;
import ewe.ui.Form;
import ewe.ui.InputMethod;
import ewe.ui.NumberEntry;
import ewe.ui.Panel;
import ewe.ui.mInput;
import ewe.ui.mTextPad;

//##################################################################
public class TestPanel extends Form{
//##################################################################

//===================================================================
mInput getPassiveInput()
//===================================================================
{
	mInput ret = new mInput();
	ret.inputFlags = ret.FLAG_PASSIVE;
	InputMethod im = new InputMethod();
	im.addCycledKeys('3',"def");
	im.addCycledKeys('2',"abc");
	ret.inputMethod = im;
	//ret.getBestPassiveFlags();
	return ret;
}
//===================================================================
public TestPanel()
//===================================================================
{
	title = "Testing Panel";
	Panel p = new CellPanel();
	p.addLast(new DateTimeInput()).setCell(HSTRETCH);
	p.addLast(getPassiveInput());
	p.addLast(new NumberEntry()).setCell(HSTRETCH);
	p.addLast(getPassiveInput());
	p.addLast(new mInput("Actually The first input!")).setCell(HSTRETCH);
	Control cc = new mTextPad(4,50);
	p.addLast(cc);
	cc.prompt = "The textpad!";
	p.addLast(new mInput("The first input!")).setCell(HSTRETCH);
	addLast(p);
}

//##################################################################
}
//##################################################################
