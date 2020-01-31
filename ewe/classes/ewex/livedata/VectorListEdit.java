package ewex.livedata;
import ewe.data.*;
import ewe.ui.*;
import ewe.util.*;

//##################################################################
public class VectorListEdit extends CustomListEdit{
//##################################################################

public Vector items = new Vector();

public VectorListEdit(LiveData what,int which)
{
	editor = what.getEditor(which);
	model = what;
}

//===================================================================
public LiveData getForIndex(int which) {return (LiveData)items.get(which);}
//===================================================================
public String getDisplayName(int index) {return getForIndex(index).getName();}
//===================================================================
public int getObjectCount() {return items.size();}
//===================================================================
protected ChoiceControl makeList() {return new mList(5,5,false);}
//===================================================================


//##################################################################
}
//##################################################################

