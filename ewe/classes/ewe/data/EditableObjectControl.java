package ewe.data;

//##################################################################
public abstract class EditableObjectControl extends ewe.ui.Holder{
//##################################################################

public void setData(Object data){getSetData(data,false);}
public void getData(Object data){getSetData(data,true);}

protected abstract void getSetData(Object data,boolean isGet);

//##################################################################
}
//##################################################################

