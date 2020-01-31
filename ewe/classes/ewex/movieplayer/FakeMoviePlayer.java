package ewex.movieplayer;
import ewe.ui.*;
import ewe.sys.*;
import ewe.io.File;
import ewe.filechooser.FileChooser;

//##################################################################
public class FakeMoviePlayer implements MoviePlayer{
//##################################################################

int size = -1;
public String open(String file)
{
	File f = new File(file);
	if (!f.exists()) return "That file does not exist!";
	size = f.getLength();
	return null;
}
public void close()
{
	size = -1;
}
//===================================================================
public String play(ewe.ui.Window window,ewe.sys.CallBack doneNotify)
//===================================================================
{
	if (size == -1) return "No file to play!";
	stopPlaying = false;
	amPlaying = true;
	new Coroutine(this,"doPlay",doneNotify);
	return null;
}
boolean stopPlaying = false, amPlaying = false;
ewe.sys.Lock playLock = new ewe.sys.Lock();
//===================================================================
public void doPlay(Object data)
//===================================================================
{
	if (playLock.hold(TimeOut.Forever)){
		if (!stopPlaying){
			ewe.sys.Vm.debug("Playing for: "+(size*2)/1000000+" seconds...");
			playLock.wait(new TimeOut((size*2)/1000)); //Sleep for two seconds for each megabyte.
		}
		ewe.sys.Vm.debug("Done!");
	}
	playLock.release();
	amPlaying = false;
	if (data instanceof CallBack){
		ewe.sys.Vm.debug("Calling!");
		ewe.sys.Vm.callInSystemQueue((CallBack)data,null);
	}
}
public String stop()
{
	if (playLock.hold(TimeOut.Forever)){
		stopPlaying = true;
		playLock.notifyAllWaiting();
	}
	playLock.release();
	return null;
}

public boolean isPlaying()
{
	return amPlaying;
}

public boolean supportsCallBack() {return true;}

//##################################################################
}
//##################################################################

