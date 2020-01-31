/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.editor;
import ewe.ui.*;
import ewe.sys.Time;

//##################################################################
public class PersonInfo implements FieldListener{
//##################################################################
public static final int Female = 0;
public static final int Male = 1;
//......................................................
// Make sure all uninitialized strings are "" and not null!
// Any field to be edited via an Editor MUST be public
// AND the class itself MUST be public.
// This is for running under Java but not for Waba which
// has less class security in the Reflection methods.
//......................................................
public String lastName = "";
public String firstNames = "";
public String password = "";
public String constant = "Not editable!";
public String address = "6 Evergreen Terrace, San Juan, Guatemala";
public int gender = Female;
public Time dob = new Time(1,1,1980);
public boolean smoker = false;
public int retirementAge = 65;

//===================================================================
public PersonInfo() {}
//===================================================================

//===================================================================
public void set(Object [] setup)
//===================================================================
{
	lastName = (String)setup[0];
	firstNames = (String)setup[1];
	gender = ewe.sys.Convert.toInt((String)setup[2]);
	dob = (Time)setup[3];
}

//===================================================================
public static Object []
//===================================================================
	adultMale = new Object[]{"Smith","John M",""+Male,new Time(1,6,1970)},
	childFemale = new Object[]{"Smith","Jane R",""+Female,new Time(10,8,1995)},
	homer = new Object[]{"Simpson","Homer Jay",""+Male,new Time(1,4,1960)},
	lisa = new Object[]{"Simpson","Lisa",""+Female,new Time(3,8,1993)},
	bart = new Object[]{"Simpson","Bart",""+Male,new Time(6,11,1990)};
//===================================================================


public MenuItem presetPerson;
/**
* This is how you create an editor for the object.
**/
//===================================================================
public static Editor makeEditor()
//===================================================================
{
	Editor ed = new Editor();
	ed.title = "Personal Info";
	ed.objectClass = ewe.reflect.Reflect.getForObject(new PersonInfo());
//......................................................
// An input stack is a useful panel which aligns all prompts and inputs
// nicely.
//......................................................
	InputStack is = new InputStack();
	ScrollBarPanel sp = new VerticalScrollPanel(new ScrollableHolder(is));
	//new ScrollBarPanel(new ScrollableHolder(is),ScrollablePanel.OPTION_INDICATOR_ONLY|ScrollablePanel.NeverShowHorizontalScrollers);
	//sp.setClientConstraints(ed.VEXPAND|ed.HEXPAND|ed.HCONTRACT);
	is.inputLength = 30;
	ed.addLast(sp);//,ed.HSTRETCH,ed.FILL);
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
mInput mi = is.addInput("Password:",null);
mi.textCase = mi.CASE_NUMBERS;
mi.isPassword = true;
mi.passwordCharacter = '#';
ed.addField(mi,"password");
mi = is.addInput("Constant:",null);
//mi.modify(mi.DisplayOnly,0);
mi.textCase = mi.CASE_UPPER;
ed.addField(mi,"constant");
mTextPad pad = new mTextPad(3,40);
pad.wantReturn = false;
is.add(pad,"Address:");
ed.addField(pad,"address");
pad.textCase = mi.CASE_SENTENCE;
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
DateTimeInput ddi = new DateTimeInput();
ddi.setDateFormat("ddd d-MMM-yyyy");
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
/*
Menu simpsons = new Menu(new String[]{"Homer","Lisa","Bart"},"Simpsons");
Menu preset = new Menu(new String[]{"Adult Male","Child Female"},"Presets");
preset.addItem(simpsons);
ButtonPullDownMenu bpd = new ButtonPullDownMenu("Presets",preset);
ButtonBar bb = new ButtonBar();
bb.addNext(bpd);
ed.addField(bpd,"presetPerson");
ed.addField(bb.add("Clear"),"clear");
ed.addLast(bb).setCell(ed.HSTRETCH);
*/
return ed;
}
/**
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
	}else if (fn.equals("firstNames")){
		if (firstNames.equalsIgnoreCase("michael")){
			if (!Window.inNativeInput()) new MessageBox("Nice Name","Hey, nice name you've got there!",0).execute();
		}
	//......................................................
	// Here we'll check if an item in the preset menu was selected.
	//......................................................
	}else if (fn.equals("presetPerson") && presetPerson != null){
		if (presetPerson.equals("Adult Male")) set(adultMale);
		else if (presetPerson.equals("Child Female")) set(childFemale);
		else if (presetPerson.equals("Homer")) set(homer);
		else if (presetPerson.equals("Lisa")) set(lisa);
		else if (presetPerson.equals("Bart")) set(bart);
		ed.toControls();
		ed.notifyDataChange();
	}
}
/**
* Here we respond to actions.
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
	if (fn.equals("clear")){
//......................................................
		lastName = firstNames = "";
		gender = Female;
		dob = new Time();
		smoker = false;
		retirementAge = 60;
		ed.toControls();
	}
}
//===================================================================
public void fieldEvent(ewe.reflect.FieldTransfer ft,Editor e,Object event){}
//===================================================================


//##################################################################
}
//##################################################################
