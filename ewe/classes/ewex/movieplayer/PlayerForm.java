package ewex.movieplayer;
import ewe.ui.*;
import ewe.sys.*;
import ewe.io.File;
import ewe.filechooser.FileChooser;
//##################################################################
public class PlayerForm extends Editor implements CallBack{
//##################################################################

public String curFile = "No movie chosen.";
WindowContainer wc;
MoviePlayer player = new FakeMoviePlayer();
Control playButton, openButton, closeButton;

//===================================================================
public PlayerForm()
//===================================================================
{
	title = "Ewe Movie Player";
	moveable = true;
	boolean doFake = false;
	String name = "c:\\windows\\media\\dmmintro.avi";
	if (mApp.programArguments.length >= 1)
		if (mApp.programArguments[0].equalsIgnoreCase("fake"))
			doFake = true;
		else name = mApp.programArguments[0];
	if (!doFake) player = new NativeMoviePlayer();

	CellPanel cp;
	wc = new WindowContainer();
	if (wc.isSupported()){
		cp = new CellPanel();
		cp.borderWidth = 2;
		cp.borderStyle = EDGE_SUNKEN;
		cp.addLast(wc).setPreferredSize(200,100);
		addLast(cp);
		wc.backGround = ewe.fx.Color.LightGray;
		resizable = true;
	}
	cp = new CellPanel();
	Control c = cp.addNext(addField(openButton = new mButton("Open AVI"),"openAvi"));
	Gui.iconize(c,TreeTableModel.openFolder,true,getFontMetrics());
	c = cp.addNext(addField(playButton = new mButton("Play AVI"),"playAvi"));
	c = cp.addNext(addField(new mButton("Stop AVI"),"stopAvi"));
	c = cp.addNext(addField(closeButton = new mButton("Close AVI"),"closeAvi"));
	addLast(cp).setCell(HSTRETCH);
	cp = new CellPanel();
	cp.addNext(addField(new mInput(),"curFile")).modify(NotEditable,0);
	addLast(cp).setCell(HSTRETCH);

	player.open(curFile = name);
}

//===================================================================
public boolean checkReturnError(String ret)
//===================================================================
{
	if (ret == null) return true;
	new MessageBox("Error!",ret,0).execute();
	return false;
}
//===================================================================
public void action(ewe.reflect.FieldTransfer ft,Editor e)
//===================================================================
{
	if (ft.fieldName.equals("openAvi")){
		if (player.isPlaying()) return;
		FileChooser fc = new FileChooser(FileChooser.OPEN/*|FileChooser.FILE_MUST_EXIST*/|FileChooser.QUICK_SELECT,"c:\\windows\\*.avi");
		fc.title = "Select Movie To Play";
		if (fc.execute() == IDCANCEL) return;
		curFile = fc.file;
		toControls("curFile");
		checkReturnError(player.open(curFile));
	}else if (ft.fieldName.equals("playAvi")){
		playButton.modify(Disabled,0);
		playButton.repaintNow();
		openButton.setCursor(ewe.sys.Vm.BUSY_CURSOR);
		closeButton.setCursor(ewe.sys.Vm.BUSY_CURSOR);
		if (!checkReturnError(player.play(wc.getContainedWindow(),this)))
			callBack(null);
		else{
			new playChecker();
		}
	}else if (ft.fieldName.equals("stopAvi")){
		checkReturnError(player.stop());
	}else if (ft.fieldName.equals("closeAvi")){
		if (player.isPlaying()) return;
		player.close();
		curFile = "No file chosen.";
		toControls("curFile");
	}
}

//===================================================================
public void close(int value)
//===================================================================
{
	player.close();
	super.close(value);
}
//##################################################################
public class playChecker implements Runnable{
//##################################################################

public playChecker() {new Coroutine(this,100);}
public void run()
{
	if (player.supportsCallBack()) return;
	while(player.isPlaying()){
		Coroutine.sleep(100);
	}
	callBack(null);
}
//##################################################################
}
//##################################################################

//===================================================================
public void callBack(Object data)
//===================================================================
{
	playButton.modify(0,Disabled);
	playButton.repaintNow();
	openButton.setCursor(0);
	closeButton.setCursor(0);
}
//##################################################################
}
//##################################################################


