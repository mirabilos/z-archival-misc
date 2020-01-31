/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.data;
import ewe.ui.*;

//##################################################################
public class BigPersonInfo extends PersonInfo{
//##################################################################
public String eMail = "home@hotmail.com";
public String phone = "1-868-345-4432";

//===================================================================
public void addToPanel(CellPanel cp,Editor f,int which)
//===================================================================
{
	super.addToPanel(cp,f,which);
	InputStack is = new InputStack();
	is.addInputs(f,"EMail|eMail|Phone#|phone");
	cp.addLast(is);
}

//##################################################################
}
//##################################################################
