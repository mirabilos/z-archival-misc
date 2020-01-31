/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is free software; you can redistribute      *
 *  it and/or modify it under the terms of the Amended GNU Lesser General        *
 *  Public License distributed with this software.                               *
 *                                                                               *
 *  Under this license, linking this library or part thereof with other files to *
 *  produce an executable does not in itself require the executable to be        *
 *  covered by the GNU Lesser General Public License.                            *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.reflect;
import ewe.util.mString;
//##################################################################
public class FieldTransfer{
//##################################################################
/**
* This is the Control used for the field in an Editor.
**/
public Object dataInterface;
/**
* The set() method used on the data interface - this is not used on the data object.
**/
public Method setMethod;
/**
* The get() method used on the data interface - this is not used on the data object.
**/
public Method getMethod;
/**
* The combined getSet() method used on the data interface.
**/
public Method getSetMethod;
/**
* The combined getSetField() method used on the data object.
**/
public Method getSetFieldMethod;
/**
* The field in the Object (if one exists).
**/
public Field field;
/**
* The full name of the field.
**/
public String fieldName;
/**
* The type of the field.
**/
public String fieldType;
/**
* The type of the data being transfered.
**/
public String transferType;
/**
* The type of the data used by the interface object.
**/
public String ifType;
/**
* The pure name of the field without any extra specifiers.
**/
public String pureName;
/**
* The full field name.
**/
public String fullFieldName;
protected Wrapper objWrapper = new Wrapper(), ifWrapper = new Wrapper();
protected Wrapper fWrapper = new Wrapper(), retWrapper = new Wrapper(), gsWrapper = new Wrapper();
protected Wrapper [] oneParameter = new Wrapper[1], /*twoParameters = new Wrapper[2],*/ threeParameters = new Wrapper[3];
/**
* An optional DataConverter.
**/
public DataConverter converter;
/**
* The field event that occured.
**/
public Object fieldEvent;
/**
* The data object being used.
**/
public Object dataObject;

//===================================================================
public boolean setMethodsFor(String classSpec,String getMethodName,String getMethodSignature,String setMethodName,String setMethodSignature)
//===================================================================
{
	if (classSpec == null) return false;
	if (!classSpec.equals(fieldType)) return false;
	ifType = classSpec;
	getMethod = setMethod = null;
	boolean ret = true;
	Reflect dr = Reflect.getForObject(dataInterface);
	if (dr == null) return false;
	if (getMethodName != null && getMethodSignature != null)
		if ((getMethod = dr.getMethod(getMethodName,getMethodSignature,0)) == null) ret = false;
	if (setMethodName != null && setMethodSignature != null)
		if ((setMethod = dr.getMethod(setMethodName,setMethodSignature,0)) == null) ret = false;
	return ret;
}
/**
* This is only used to create a dummy field transfer.
**/
//===================================================================
public FieldTransfer(String fieldName){this.fieldName = fieldName;}
//===================================================================

//===================================================================
public FieldTransfer(Object objectOrReflect,String fieldName) {this(objectOrReflect,fieldName,null);}
//===================================================================

//===================================================================
public FieldTransfer(Object objectOrReflect,String fieldName,ewe.ui.Control dataInterface)
//===================================================================
{
	this(Reflect.toReflect(objectOrReflect),Reflect.toNonReflect(objectOrReflect),fieldName,dataInterface,null);
		//objectOrReflect instanceof Reflect ? (Reflect)objectOrReflect : Reflect.getForObject(objectOrReflect),
		//objectOrReflect instanceof Reflect ? null : objectOrReflect,

	if (dataInterface != null) dataInterface.fieldTransfer = this;
}

//===================================================================
public FieldTransfer getFor(Object data,ewe.ui.Control control)
//===================================================================
{
	FieldTransfer ft = new FieldTransfer(data,fullFieldName,control);
	control.fromField(ft);
	control.updateData();
	return ft;
}

//-------------------------------------------------------------------
private boolean getFieldType(Reflect objectClass,String theFieldType)
//-------------------------------------------------------------------
{
	if (fieldType != null) return true;
	if (theFieldType != null) fieldType = theFieldType;
	else{
		Method getFieldType = dataObject == null ? null : objectClass.getMethod("_getFieldType","(Ljava/lang/String;)Ljava/lang/String;",0);
		if (getFieldType != null){
			oneParameter[0] = new Wrapper().setObject(pureName);
			getFieldType.invoke(dataObject,oneParameter,ifWrapper);
			fieldType = (String)ifWrapper.getObject();
		}
	}
	return fieldType != null;
}
//===================================================================
public FieldTransfer(Reflect objectClass,Object sampleOrDataObject,String fieldName,Object dataInterface,DataConverter converter)
//===================================================================
{
	fullFieldName = fieldName;
	dataObject = sampleOrDataObject;
	char fspec = '$';
	String theFieldType = mString.rightOf(fieldName,fspec);
	if (theFieldType.length() == 0)
 		if (fieldName.indexOf('$') != -1)
			theFieldType = Wrapper.stringClass;
		else
			theFieldType = null;
	else if (theFieldType.charAt(0) == 'L' && !theFieldType.endsWith(";"))
		theFieldType += ";";
	fieldName = mString.leftOf(fieldName,fspec);
	pureName = mString.leftOf(fieldName,':');
	getSetFieldMethod = objectClass.getMethod("_getSetField","(Ljava/lang/String;Lewe/reflect/Wrapper;Z)Z",0);
	//
	// If we are using methods to get/set the field value then we need to know the type.
	//
	if (getSetFieldMethod != null){
		getFieldType(objectClass,theFieldType);
		if (fieldType != null){
			fWrapper = new Wrapper();
			retWrapper = new Wrapper();
		}else
			getSetFieldMethod = null;
	}
	//
	//
	//
	if (pureName.equals("this")){
		field = null;
		fieldType = "L"+objectClass.getClassName().replace('.','/')+";";
		getSetFieldMethod = null;
	}else{
		field = objectClass.getField(pureName,0);
		if (field != null) {
			getSetFieldMethod = null;
			fieldType = null;
		}
	}

	this.dataInterface = dataInterface;
	this.fieldName = fieldName;
	if (field != null && fieldType == null) fieldType = field.fullType;
	transferType = fieldType;

	if (fieldType == null) return;
	if (fieldType.charAt(0) == 'L')
		if (Reflect.isTypeOf(transferType,"ewe/data/Transferrable")){
			Object ov = getFieldValue(dataObject);
			if (ov instanceof ewe.data.Transferrable){
				try{
					if (((ewe.data.Transferrable)ov).getSetTransferData(dataInterface,objWrapper,true)){
						char c = (char)objWrapper.getType();
						transferType = ""+c;
						if (c == 'L' || c == '[') transferType = Reflect.getType(objWrapper.getObject().getClass());
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	if (transferType != null && dataInterface != null){
		Reflect dr = Reflect.getForObject(dataInterface);
		if (dr == null) return;
		String lookType = transferType;
		char c = lookType.charAt(0);
		if ((getSetMethod = dr.getMethod("_getSetValue","(Ljava/lang/String;Lewe/reflect/Wrapper;Z)Z",0)) != null){
			;
		}else if (c == Wrapper.INT || c == Wrapper.LONG || c == Wrapper.BYTE || c == Wrapper.CHAR || c == Wrapper.SHORT){
			if (c == Wrapper.LONG){
				getMethod = dr.getMethod("getLong","()J",0);
				setMethod = dr.getMethod("setLong","(J)V",0);
			}
			if (getMethod == null) getMethod = dr.getMethod("getInt","()I",0);
			if (setMethod == null) setMethod = dr.getMethod("setInt","(I)V",0);
		}else if (c == Wrapper.DOUBLE) {
			getMethod = dr.getMethod("getDouble","()D",0);
			setMethod = dr.getMethod("setDouble","(D)V",0);
		}else if (c == Wrapper.FLOAT) {
			getMethod = dr.getMethod("getFloat","()F",0);
			setMethod = dr.getMethod("setFloat","(F)V",0);
		}else if (c == Wrapper.BOOLEAN) {
			getMethod = dr.getMethod("getState","()Z",0);
			setMethod = dr.getMethod("setState","(Z)V",0);
		}else if (Reflect.isTypeOf(lookType,Wrapper.valueClass)){
			getMethod = dr.getMethod("getValue","("+Wrapper.valueClass+")V",0);
			setMethod = dr.getMethod("setValue","("+Wrapper.valueClass+")V",0);
		/*
		}else if (lookType.equals(Wrapper.doubleClass)){
			getMethod = dr.getMethod("getDouble","("+Wrapper.doubleClass+")V",0);
			setMethod = dr.getMethod("setDouble","("+Wrapper.doubleClass+")V",0);
		*/
		}else if (c == Wrapper.OBJECT || c == Wrapper.ARRAY){
			getMethod = dr.getMethod("getData","("+Wrapper.objectClass+")V",0);
			if (getMethod == null)
				getMethod = dr.getMethod("getData","()"+Wrapper.objectClass,0);
			setMethod = dr.getMethod("setData","("+Wrapper.objectClass+")V",0);
		}
		if (getMethod == null && setMethod == null && getSetMethod == null){
			lookType = Wrapper.stringClass;
		}
		if (lookType.equals(Wrapper.stringClass)){
			getMethod = dr.getMethod("getText","()"+Wrapper.stringClass,0);
			setMethod = dr.getMethod("setText","("+Wrapper.stringClass+")V",0);
		}
		ifType = lookType;
	}
}

public static final int TO_OBJECT = 1;
public static final int FROM_OBJECT = 0;


/**
 * Returns true if this FieldTransfer can actually do a transfer.
 */
//===================================================================
public boolean isValid()
//===================================================================
{
	if (field != null && fieldType != null && fieldType.length() != 0) return true;
	return getSetFieldMethod != null;
}
//===================================================================
public boolean isField(String field)
//===================================================================
{
	if (field == null) return false;
	if (!fieldName.startsWith(field)) return false;
	int l = field.length();
	if (fieldName.length() == l) return true;
	return fieldName.charAt(l) == ':';
}
//===================================================================
public Wrapper getFieldValue(Object obj,Wrapper dest)
//===================================================================
{
	if (dest == null) dest = new Wrapper();
	if (obj == null) return null;
	if (pureName.equals("this")){
			return dest.setObject(obj);
	}else if (getSetFieldMethod != null){
		threeParameters[0] = fWrapper;
		threeParameters[1] = ifWrapper;
		threeParameters[2] = gsWrapper;
		fWrapper.setObject(pureName);
		ifWrapper.setObject(dest.setType(fieldType));
		gsWrapper.setBoolean(true);
		getSetFieldMethod.invoke(obj,threeParameters,retWrapper);
		if (!retWrapper.getBoolean() && field != null)
			field.getValue(obj,dest.setType(fieldType));
	}else if (field != null){
		field.getValue(obj,dest.setType(fieldType));
	}
	return dest;
}
//===================================================================
public Object getFieldValue(Object obj)
//===================================================================
{
	Wrapper got = getFieldValue(obj,objWrapper);
	if (got == null) return null;
	if (got.getType() != 'L' && got.getType() != '[') return null;
	return got.getObject();
}
/**
* This uses the "dataObject" variable as the object for field transfer.
**/
//===================================================================
public void transfer(int direction) {transfer(dataObject,direction);}
//===================================================================
//===================================================================
public void transfer(Object obj,int direction)
//===================================================================
{
	if (fieldType == null) return;
	objWrapper.setType(fieldType);
	//===================================================================
	if (direction == FROM_OBJECT){
	//===================================================================
		if (setMethod == null && getSetMethod == null) return;
		if (obj == null) objWrapper.zero();
		if (pureName.equals("this")){
				objWrapper.setObject(obj);
		}else if (getSetFieldMethod != null){
			if (obj != null) {
				threeParameters[0] = fWrapper.setObject(pureName);
				threeParameters[1] = ifWrapper.setObject(objWrapper.setType(fieldType));
				threeParameters[2] = gsWrapper.setBoolean(true);
				getSetFieldMethod.invoke(obj,threeParameters,retWrapper);
				if (!retWrapper.getBoolean() && field != null)
					field.getValue(obj,objWrapper);
			}
		}else if (field != null){
			if (obj != null)
				field.getValue(obj,objWrapper);
		}else
			return;
		//
		//At this point, the field value is in objWrapper.
		//
		String transferType = fieldType;
		if (Reflect.isTypeOf(fieldType,"ewe/data/Transferrable")){
			ewe.data.Transferrable t = (ewe.data.Transferrable)objWrapper.getObject();
			try{
				if (t != null){
					if (t.getSetTransferData(dataInterface,objWrapper,true)){
						char c = (char)objWrapper.getType();
						transferType = ""+c;
						if (c == 'L' || c == '[') transferType = Reflect.getType(objWrapper.getObject().getClass());
					}
				}
			}catch(Exception e){}
		}
		//
		// Convert if necessary.
		//
		if (converter == null) Wrapper.doConvertData(objWrapper,transferType,ifWrapper,ifType);
		else converter.convertData(objWrapper,transferType,ifWrapper,ifType);
		objWrapper = ifWrapper.getCopy();
		//
		// Now put the data in the dataInterface.
		// The objWrapper is now a copy of the ifWrapper.
		//
		if (getSetMethod != null){
			threeParameters[0] = fWrapper.setObject(pureName);
			threeParameters[1] = ifWrapper.setObject(objWrapper);
			threeParameters[2] = gsWrapper.setBoolean(false);
			getSetMethod.invoke(dataInterface,threeParameters,retWrapper);
		}else{
			oneParameter[0] = ifWrapper;
			ifWrapper.type = ifType.charAt(0);
			setMethod.invoke(dataInterface,oneParameter,null);
		}
	//===================================================================
	}else{ // TO_OBJECT;
	//===================================================================
		//objWrapper.setType(fieldType);
		//ewe.sys.Vm.debug("XFER: "+obj+", "+getMethod+", "+getSetFieldMethod);
		if (obj == null || (getMethod == null && getSetMethod == null)) return;
		Object ov = getFieldValue(obj);
		//ewe.sys.Vm.debug("Field value is now: "+ov);
		if (getSetMethod != null){
			getFieldValue(obj,objWrapper);
			threeParameters[0] = fWrapper.setObject(pureName);
			threeParameters[1] = ifWrapper.setObject(objWrapper);
			threeParameters[2] = gsWrapper.setBoolean(true);
			getSetMethod.invoke(dataInterface,threeParameters,retWrapper);
			ifWrapper = objWrapper.getCopy();
		}else if (getMethod.returnsValue()){
			if (pureName.equals("this")) return;
			ifWrapper.type = ifType.charAt(0);
			getMethod.invoke(dataInterface,Wrapper.noParameter,ifWrapper);
		}else{ //Get method does not return.
			//This would be used for void setData(Object) and void getData(Object)
			//Note that no conversion is done here.
			if (ov != null){
				objWrapper.setObject(ov);
				oneParameter[0] = objWrapper;
				getMethod.invoke(dataInterface,oneParameter,null);
			}
			if (getSetFieldMethod == null)
				return;
			ifWrapper.type = ifType.charAt(0);
			ifWrapper.setObject(ov);
		}
		//
		//Now have the data in the ifWrapper.
		//
		if (ov instanceof ewe.data.Transferrable){
			if (((ewe.data.Transferrable)ov).getSetTransferData(dataInterface,ifWrapper,false))
				return;
		}
		if (converter == null) Wrapper.doConvertData(ifWrapper,ifType,objWrapper,fieldType);
		else converter.convertData(ifWrapper,ifType,objWrapper,fieldType);
		//
		// At this point the objWrapper contains the data.
		//
		if (getSetFieldMethod != null){
			threeParameters[0] = fWrapper.setObject(pureName);
			threeParameters[1] = ifWrapper.setObject(objWrapper.setType(fieldType));
			threeParameters[2] = gsWrapper.setBoolean(false);
			getSetFieldMethod.invoke(obj,threeParameters,retWrapper);
			if (!retWrapper.getBoolean() && field != null)
				field.setValue(obj,objWrapper);
		}else if (field != null){
			field.setValue(obj,objWrapper);
		}else
			return;
	}
}
//===================================================================
public String toString()
//===================================================================
{
	if (field == null) return "<no field>";
	String ret = fieldType+" "+field.getName()+"<=>[";
	if (getSetMethod != null) ret += getSetMethod.toString();
	else ret += mString.toString(getMethod)+","+mString.toString(setMethod);
	ret += "]";
	return ret;
}
//##################################################################
}
//##################################################################

