package ewex.movieplayer;

//##################################################################
public interface MoviePlayer{
//##################################################################

/**
* This opens the file. It returns null on success or an error String on failure.
**/
public String open(String file);
public void close();
public String play(ewe.ui.Window window,ewe.sys.CallBack doneNotify);
public String stop();
public boolean isPlaying();
public boolean supportsCallBack();

//##################################################################
}
//##################################################################

