/**
* An EditableData is a simple data object that can create and utilize it's own
* Control for editing. Similar to the LiveData concept, but targeted at simpler
* data objects. An EditableObject is an implementation of this interface.
**/
package ewe.data;

//##################################################################
public interface EditableData extends DataUnit, ewe.util.Textable, Transferrable{
//##################################################################
/**
 * Get the Control for the object.
 */
public EditableObjectControl getControl();
/**
* Get a property for the object.
**/
public Object getProperty(String property,Object defaultValue);
/**
* This asks the object to set the Wrapper to be a value that can be used to save
* the state of this object. The value set in the Wrapper should be
* <ul>
* <li>Any primitive value.
* <li>A ewe.sys.Time object for date/time values.
* <li>A String object for complex data.
* <li>A ByteArray or byte[] object for more complex data.
* </ul>
* If you are calling setObject() for one of the specified types, you must NOT provide a null
* object.
**/
public void toSaveableData(ewe.reflect.Wrapper data);
/**
* This retrieves the state from the data in the wrapper. See toSaveableData() for acceptable
* data types that should be stored in the Wrapper.
**/
public void fromSaveableData(ewe.reflect.Wrapper data) throws IllegalArgumentException;

//##################################################################
}
//##################################################################

