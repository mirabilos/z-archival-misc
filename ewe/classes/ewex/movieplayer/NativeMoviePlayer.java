package ewex.movieplayer;

//##################################################################
public class NativeMoviePlayer implements MoviePlayer,ewe.sys.CallBack{
//##################################################################
{
	ewe.sys.Vm.loadLibrary("ewex_movieplayer");
}

boolean playing;
boolean stopPlaying;
int playThread;
public String openFile;

/**
* This opens the file. It returns null on success or an error String on failure.
**/
public String open(String file)
{
	if (openFile != null) close();
	if (file == null){
		return "No file specified!";
	}
	openFile = file;
	String err = nativeOpen("open "+file+" type AVIVideo alias mymovie");
	if (err == null) return null;
	openFile = null;
	return err;
}

public void close()
{
	if (openFile == null) return;
	nativeClose();
	openFile = null;
}

public native String nativeOpen(String file);
public native void nativeClose();

public native int nativePlay(ewe.ui.Window window,ewe.sys.CallBack doneNotify);

public String play(final ewe.ui.Window window,final ewe.sys.CallBack doneNotify)
{
	if (openFile == null) return "No file is open.";
	playing = true;
	new ewe.sys.Coroutine(new Runnable(){
		public void run(){
			ewe.sys.Coroutine.sleep(
			nativePlay(window,doneNotify)
			);
			playing = false;
			if (doneNotify != null){
				ewe.sys.Vm.callInSystemQueue(doneNotify,null);
			}
		}
	});
	return null;
}
public native String stop();
public boolean isPlaying(){return playing;}
//public native boolean isPlaying();
public boolean supportsCallBack() {return true;}
public void callBack(Object data)
{
	ewe.sys.Vm.debug(data.toString());
}
//##################################################################
}
//##################################################################

