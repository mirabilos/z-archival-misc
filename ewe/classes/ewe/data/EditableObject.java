package ewe.data;
/**
* This is an implementation of EditableData that is only missing the getControl() method.
**/
//##################################################################
public abstract class EditableObject extends DataObject implements EditableData, ewe.util.Encodable{
//##################################################################
/**
* By default, this returns the default value.
**/
//===================================================================
public Object getProperty(String name,Object defaultValue)
//===================================================================
{
	return defaultValue;
}
/**
* By default, this calls the ewe.util.Utils.textEncode(Encodable e) on itself.
**/
//===================================================================
public String getText()
//===================================================================
{
	return ewe.util.Utils.textEncode(this);
}
/**
* By default, this calls the ewe.util.Utils.textDecode(String text,Encodable e) on itself.
**/
//===================================================================
public void setText(String encoded)
//===================================================================
{
	ewe.util.Utils.textDecode(this,encoded);
}
//===================================================================
public void toSaveableData(ewe.reflect.Wrapper data)
//===================================================================
{
	data.setObject(getText());
}
//===================================================================
public void fromSaveableData(ewe.reflect.Wrapper data) throws IllegalArgumentException
//===================================================================
{
	setText(data.getObject().toString());
}
//===================================================================
public boolean getSetTransferData(Object transferTo,ewe.reflect.Wrapper data,boolean isGet)
//===================================================================
{
	if (transferTo instanceof ewe.ui.Control) return false;
	if (isGet) toSaveableData(data);
	else fromSaveableData(data);
	return true;
}
//##################################################################
}
//##################################################################

