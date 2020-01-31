/****************************************************************

Sample code for Ewe Application Development.

Updated October 2003, for 1.30 release.

****************************************************************/

package samples.data;
import ewe.data.LiveTreeNode;
import ewe.sys.Time;
import ewe.ui.ButtonBar;
import ewe.ui.CellPanel;
import ewe.ui.DateDisplayInput;
import ewe.ui.Editor;
import ewe.ui.InputStack;
import ewe.ui.IntChoice;
import ewe.ui.MessageBox;
import ewe.ui.mCheckBox;
import ewe.ui.mChoice;

//##################################################################
public class PersonInfo extends LiveTreeNode{//LiveObject{
//##################################################################
public static final int Female = 0;
public static final int Male = 1;
//......................................................
// Make sure all uninitialized strings are "" and not null!
// Any field to be edited via an Editor MUST be public
// AND the class itself MUST be public.
//......................................................
public String lastName = "";
public String firstNames = "";
public int gender = Female;
public Time dob = new Time(1,1,1980);
public boolean smoker = false;
public int retirementAge = 65;
//......................................................
// Defining the variable "_fields" allows specific fields to be copied/encoded,
// otherwise it will copy/encode ALL public non-static fields in this object.
//
// Again _fields MUST be public.
//......................................................
//public String _fields = "lastName,firstNames,gender,dob,smoker,retirementAge";

/**
* This is how you create an editor for the object. A live object must provide
* an Editor for itself using Editor LiveObject.getEditor(int editorOption).
* This is already mostly implemented in LiveObject, all you have to do is
* override this method below. Add your controls to the "cp" CellPanel and
* your fields to the "ed" editor.
**/
//===================================================================
public void addToPanel(CellPanel cp,Editor ed,int editorOption)
//===================================================================
{
	ed.title = "Personal Info";
//......................................................
// An input stack is a useful panel which aligns all prompts and inputs
// nicely.
//......................................................
	InputStack is = new InputStack();
	is.inputLength = 30;
	cp.addLast(is,cp.HSTRETCH,cp.FILL);
//......................................................
// This is the long way of adding a field to the input panel.
//......................................................
	//mInput mi = new mInput();
	//ed.addField(mi,"lastName"); // This associates the "lastName" variable with the mInput.
	//is.add(mi,"Last Name:");
//......................................................
// Here is a shorter way.
//......................................................
	//is.add(ed.addField(new mInput(),"firstName"),"First Name:");
//......................................................
// This is the absolute shortest way for adding multiple Inputs.
// It only adds mInputs though.
//......................................................
is.addInputs(ed,"Last Name:|lastName|First Name:|firstNames");
//......................................................
// Use an mChoice with no drop-down button for the gender. Clicking it will
// cycle through the choices.
//......................................................
mChoice mc = new mChoice("Female|Male",0);
mc.dropDownButton = false;
mc.modify(mc.PreferredSizeOnly,0);
is.add(ed.addField(mc,"gender"),"Gender:");
//......................................................
// Use a DateDisplayInput for the DOB.
//......................................................
DateDisplayInput ddi = new DateDisplayInput();
ddi.time.format = "ddd d-MMM-yyyy";
is.add(ed.addField(ddi,"dob"),"D.O.B.:");
//......................................................
// Use a checkbox for smoker.
//......................................................
is.add(ed.addField(new mCheckBox(""),"smoker"),"Smoker:");
//......................................................
// Use an IntChoice for the retirement age.
//......................................................
is.add(ed.addField(new IntChoice("60|61|62|63|64|65|66|67|68|69|70",60),"retirementAge"),"Ret. Age:");
//......................................................
// Here we'll add some action buttons. Note that the field names used for actions should not
// correspond to any fields in our object.
//......................................................
ButtonBar bb = new ButtonBar();
ed.addField(bb.add("Lisa"),"lisa");
ed.addField(bb.add("Homer"),"homer");
ed.addField(bb.add("Encode"),"encode");
cp.addLast(bb).setCell(cp.HSTRETCH);
}

/**
* Since this class inherits from LiveObject, I can respond to field changes
* by putting the field name and adding "_changed(Editor ed)" to it to
* form a method that will respond directly to the field change.
**/
//===================================================================
public void firstNames_changed(Editor ed)
//===================================================================
{
	if (firstNames.equalsIgnoreCase("michael"))
		new MessageBox("Nice Name","Hey, nice name you've got there!",MessageBox.MBOK).execute();
}
/**
* Here we respond to field changes by directly overriding the fieldChanged() FieldListener method.
* This method gets called whenever the data in a UI has changed and this
* change has been sent to the object. Remember that at this point, the
* change has already been made in the object and there is no need to call
* "fromControls()" on the editor.
* Remember that modifications to the object data is not automatically
* reflected in the Editor. You have to call toControls() to update
* the editor.
* You can call toControls() with no arguments to update all the controls.
* You can call toControls("field1,field2,field3") with field names separated
* by commas. The same goes for fromControls().
**/
//===================================================================
public void fieldChanged(ewe.reflect.FieldTransfer ft,Editor ed)
//===================================================================
{
	String fn = ft.fieldName;
	//......................................................
	// Just for kicks, we'll limit the retirement age to no later than 68.
	//......................................................
	if (fn.equals("retirementAge")){
		if (retirementAge > 68){
			retirementAge = 68;
			ed.toControls(fn);
			ewe.fx.Sound.beep();
		}
	//......................................................
	// Here we'll check the value of firstNames.
	//......................................................
	}
	super.fieldChanged(ft,ed);
}
//-------------------------------------------------------------------
//Create a static person.
//-------------------------------------------------------------------
static PersonInfo homerInfo;
static
{
	homerInfo = new PersonInfo();
	homerInfo.lastName = "Simpson";
	homerInfo.firstNames = "Homer Jay";
	homerInfo.gender = Male;
	homerInfo.dob = new Time(1,4,1960);
	homerInfo.smoker = true;
	homerInfo.retirementAge = 60;
}
/**
* Since this class inherits from LiveObject, I can respond to actions
* by putting the field name and adding "_action(Editor ed)" to it to
* form a method that will respond directly to the action.
**/
//===================================================================
public void homer_action(Editor ed)
//===================================================================
{
//......................................................
// Here we'll copy from a pre-created object. Note that
// nowhere do we define the copyFrom() method. That is
// inherited from LiveObject. It looks at the "_fields"
// variable and copies all fields listed there.
// Convenient eh!
//......................................................
	copyFrom(homerInfo);
	ed.toControls();
	ed.notifyDataChange();
}
/**
* Here we respond to actions by directly overriding the action() FieldListener method.
* Remember that modifications to the object data is not automatically
* reflected in the Editor. You have to call toControls() to update
* the editor.
**/
//===================================================================
public void action(ewe.reflect.FieldTransfer ft,Editor ed)
//===================================================================
{
	String fn = ft.fieldName;
//......................................................
// Here we'll set the fields explicitly.
//......................................................
	if (fn.equals("lisa")){
		lastName = "Simpson";
		firstNames = "Lisa";
		gender = Female;
		dob = new Time(3,2,1992);
		smoker = false;
		retirementAge = 67;
		ed.toControls();
		ed.notifyDataChange();
	}else if (fn.equals("encode")){
		//ewe.sys.Vm.debug(textEncode());
	}else
		super.action(ft,ed);
}
/**
* To encode your object to text override this method and be sure
* to call the superclass version.
*
* We do not need to override this method since we want ALL the fields
* encoded/decoded
**/
/*
//-------------------------------------------------------------------
protected TextEncoder encode(TextEncoder te)
//-------------------------------------------------------------------
{
	encodeFields(_fields,te,"PersonInfo");
	return super.encode(te);
}
*/
/**
* To decode your object to text override this method and be sure
* to call the superclass version.
*
* We do not need to override this method since we want ALL the fields
* encoded/decoded
**/
/*
//-------------------------------------------------------------------
protected TextDecoder decode(TextDecoder td)
//-------------------------------------------------------------------
{
	decodeFields(_fields,td,"PersonInfo");
	return super.decode(td);
}
*/
//===================================================================
public String getName()
//===================================================================
{
	return lastName+", "+firstNames;
}
//===================================================================
public String toString(){return ewe.reflect.Reflect.getForObject(this).getClassName()+" = "+getName();}
//===================================================================

//===================================================================
public PersonInfo(){}
//===================================================================

//===================================================================
public PersonInfo(String lastName, String firstNames,int day,int month,int year,int gender)
//===================================================================
{
	this.lastName = lastName;
	this.firstNames = firstNames;
	dob = new Time(day,month,year);
	this.gender = gender;
}
//##################################################################
}
//##################################################################
