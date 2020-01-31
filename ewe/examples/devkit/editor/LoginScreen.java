package samples.editor;
import ewe.reflect.FieldTransfer;
import ewe.sys.Handle;
import ewe.sys.Task;
import ewe.sys.mThread;
import ewe.ui.ButtonBar;
import ewe.ui.Control;
import ewe.ui.Editor;
import ewe.ui.Gui;
import ewe.ui.InputStack;
import ewe.ui.Menu;
import ewe.ui.MenuItem;
import ewe.ui.MessageBox;
import ewe.ui.ProgressBarForm;
import ewe.ui.mCheckBox;
import ewe.ui.mInput;
//##################################################################
public class LoginScreen extends Editor{
//##################################################################
public String userName = "mike";
public String password = "";
public boolean remember = false;
public MenuItem loginHow;

//===================================================================
public LoginScreen()
//===================================================================
{
	title = "Please Login";
	//......................................................
	// Now add the data fields.
	//......................................................
	InputStack is = new InputStack();
	mInput in;
	is.add(addField(in = new mInput(),"userName"),"User Name:");
	in.columns = 15;
	is.add(addField(in = new mInput(),"password"),"Password:");
	in.columns = 15;
	in.isPassword = true;
	//......................................................
	// Add a checkbox with no prompt since the checkbox already
	// has a prompt within it.
	//......................................................
	is.add(addField(new mCheckBox("Remember password"),"remember"),null);
	addLast(is).setCell(HSTRETCH);
	//......................................................
	// Now add the action buttons.
	//......................................................
	ButtonBar bb = new ButtonBar();
	Control c;
	bb.modify(MouseSensitive,0);
	c = addField(bb.add("Login"),"login");
	//......................................................
	// Add an optional menu to the login button.
	// Note that I am calling addField TWICE for the same control. The second
	// time I am assigning it to a field which is of type MenuItem. This field
	// will be updated when the optional menu for the button is displayed. This menu
	// will be displayed when the button is pressed for more than 1/4 second or if
	// the right mouse button is pressed on it.
	//
	// Note that action() will be called when the button is pressed normally and
	// fieldChanged() will be called when the menu for the button is used.
	//......................................................
	Menu m = new Menu(new String[]{"Quick Login","Medium Login","Slow Login"},"Login Type");
	c.setMenu(m);
	addField(c,"loginHow");
	c.setToolTip("Login to server.\n(Hold down button for options.)");
	//......................................................
	// This is a useful routine which gives an icon to a control if one
	// exists. Check the API for it's usage.
	//......................................................
	Gui.iconize(c,tick,true,null);
	//......................................................
	// I can't use the field name "cancel" here because there is a field
	// with that name in the Form class which Editor inherits from.
	//......................................................
	c = addField(bb.add("Cancel"),"cancelLogin");
	Gui.iconize(c,cross,true,null);
	addLast(bb).setCell(HSTRETCH);
	//......................................................
	// This tells the editor NOT to add an OK button in the title.
	//......................................................
	exitButtonDefined = true;
}
//===================================================================
public void doLogin(int time)
//===================================================================
{
		//......................................................
		// Here I am going to fake a login process. I'll just countdown for 2.5 seconds.
		// If cancel is pressed on the progress bar I will abort the login.
		//......................................................
		Handle h = new Handle();
		Task to = h;
		ProgressBarForm pbf = new ProgressBarForm();
		pbf.title = "Logging In";
		pbf.hasTopBar = false;
		pbf.showStop = true;
		pbf.exec();
		pbf.setTask(h,"Logging In");
		//......................................................
		// Now just loop for a while, updating the progress field so that the
		// progress bar will change.
		//......................................................
		for (int i = 0; i<time && ((h.check() & h.Stopped) == 0); i++){
			h.progress = (float)i/(float)time;
			h.changed();
			mThread.nap(250);
		}
		if ((h.check() & h.Stopped) == 0) h.set(h.Succeeded);
		pbf.exit(0);
		//......................................................
		// See if the user pressed the cancel button on the progress bar.
		//......................................................
		if ((h.check() & h.Success) == 0){
			new MessageBox("Login Cancelled","You cancelled your login.",0).execute();
			return;
		}
		//......................................................
		// Completed login, show a message.
		//......................................................
		String message = "You are now logged in: "+userName+"\n\n";
		if (remember) message += "I will remember your password\nfor your next login.";
		else message += "You will have to re-enter your passowrd\nfor your next login.";
		new MessageBox("Logged In",message,0).execute();
		exit(1);
}
//===================================================================
public void fieldChanged(FieldTransfer ft,Editor ed)
//===================================================================
{
	if (ft.fieldName.equals("loginHow") && loginHow != null){
		if (loginHow.label.startsWith("Quick")){
			doLogin(5);
		}else if (loginHow.label.startsWith("Medium")){
			doLogin(10);
		}else if (loginHow.label.startsWith("Slow")){
			doLogin(20);
		}
	}
}
//===================================================================
public void action(FieldTransfer ft,Editor ed)
//===================================================================
{
	if (ft.fieldName.equals("cancelLogin")){
		MessageBox mb = new MessageBox("Cancel Login?","Are you sure you want\nto cancel your login?",MessageBox.MBYESNO);
		//......................................................
		// The execute method only works for Coroutine threads.
		// It does not return until the form being executed has
		// closed.
		//......................................................
		if (mb.execute() == IDYES){
			exit(0);
		}
	}else if (ft.fieldName.equals("login")){
		doLogin(10);
	}
}
//##################################################################
}
//##################################################################
