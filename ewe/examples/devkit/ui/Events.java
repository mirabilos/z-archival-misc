/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.ui;
import ewe.ui.*;

//##################################################################
public class Events extends Form{
//##################################################################

mInput firstNumber, secondNumber, result;
mChoice operation;
mButton message,quit;

//===================================================================
public Events()
//===================================================================
{
	title = "Form Demo";
	resizable = true;
	addLast(firstNumber = new mInput(),HSTRETCH,FILL);
	addLast(secondNumber = new mInput(),HSTRETCH,FILL);
	addLast(result = new mInput(),HSTRETCH,FILL);
	result.modify(DisplayOnly,0);
	addLast(operation = new mChoice(new String[]{"Multiply","Divide"},0),HSTRETCH,FILL);
	addNext(message = new mButton("Message"),HSTRETCH,FILL);
	addNext(quit = new mButton("Exit"),HSTRETCH,FILL);
}

//-------------------------------------------------------------------
private void calculate()
//-------------------------------------------------------------------
{
	try{
		double one = ewe.sys.Convert.toDouble(firstNumber.getText());
		double two = ewe.sys.Convert.toDouble(secondNumber.getText());
		double answer =
			operation.getInt() == 0 ? one*two : one/two;
		result.setText(""+answer);
	}catch(Exception e){
		result.setText(e.getMessage());
	}
}
//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof DataChangeEvent && ev.type == DataChangeEvent.DATA_CHANGED){
		calculate();
	}else if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
		if (ev.target == quit){
			mApp.mainApp.exit(0);
		}else if (ev.target == message){
			new MessageBox("Hello","Hello there!",MBOK).execute();
		}
	}
	super.onEvent(ev); //Make sure you call this.
}
//##################################################################
}
//##################################################################
