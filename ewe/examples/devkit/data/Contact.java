/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.data;
import ewe.data.*;
import ewe.ui.*;
//##################################################################
public class Contact extends LiveObject{
//##################################################################

public String lastName = "Brereton";
public String firstNames = "Michael";
public int age = 30;

//===================================================================
public String getName()
//===================================================================
{
	return lastName+", "+firstNames;
}
//===================================================================
public void firstNames_changed(Editor ed)
//===================================================================
{
	if (firstNames.equalsIgnoreCase("micheal")){
		new MessageBox("Error","Learn to spell!",0).execute();
		firstNames = "Michael";
		if (ed != null) ed.toControls("firstNames");
	}
}
//===================================================================
public void addToPanel(CellPanel cp,Editor ed,int which)
//===================================================================
{
	UIBuilder ub = addMeToPanel(cp,ed,"Contact");
	ub.close(true);

	ub.open("Actions");
	ub.add("hello",new mButton("Hello"));
	ub.add("neil",new mButton("Neil"));
	ub.close();
}

//===================================================================
public void neil_action(Editor ed)
//===================================================================
{
	lastName = "Peart";
	firstNames = "Neil";
	age = 50;
	if (ed != null) ed.dataChanged();
}
//##################################################################
}
//##################################################################
