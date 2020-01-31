package ewex.livedata;
import ewe.data.*;
import ewe.ui.*;
import ewe.util.*;

//##################################################################
public abstract class SimpleListEdit extends CustomListEdit{
//##################################################################

//===================================================================
public LiveData getForIndex(int index) {return (LiveData)((SimpleList)list).getObjectAt(index);}
//===================================================================
public String getDisplayName(int index) {return ((SimpleList)list).getDisplayItem(index);}
//===================================================================
public int getObjectCount() {return ((SimpleList)list).getItemCount();}
//===================================================================
protected ChoiceControl makeList() {return createSimpleList();}
//===================================================================
protected abstract SimpleList createSimpleList();
//===================================================================
//##################################################################
}
//##################################################################

