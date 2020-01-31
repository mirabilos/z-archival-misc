/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
package ewe.fx;

import ewe.applet.Applet;

/**
 * SoundClip is a sound clip.
 * <p>
 * Support for sound clips varies between platforms. Under Java, sound clips are
 * only supported by Java applets - not applications. This is primarily because
 * AudioClips in Java weren't supported in Java applications until the JDK 1.2
 * without using undocumented method calls. Even when using applets, some Java
 * virtual machines support .wav and .au sound files and some versions don't
 * seem to support either format.
 * <p>
 * Using a Waba Virtual Machine, .wav format sound clips are supported under
 * Win32 and WinCE. The WabaVM under PalmOS has no support for sound clips.
 * Under Win32 and WinCE, the .wav files for sound clips must exist in a file
 * outside of the programs warp (resource) file.
 * <p>
 * If you're playing a sound clip under a Windows CE device and you don't hear
 * anything, make sure that the device is set to allow programs to play sounds.
 * To check the setting, look at:
 * <p>
 * Start->Settings->Volume & Sounds
 * <p>
 * for the check box:
 * <p>
 * Enable sounds for: Programs
 * <p>
 * If it is not checked on, sound clips won't play.
 * <p>
 * Here is an example that plays a sound:
 *
 * <pre>
 * SoundClip s = new SoundClip("sound.wav");
 * s.play();
 * </pre>
 */
public class SoundClip
{

static{
	ewe.sys.Vm.loadLibrary("java_ewe");
}

String path;
boolean loaded;
java.applet.AudioClip audioClip;
//Don't move this - it must be first.
byte [] data;
//Don't move this - it must be second.
int playStatus;
//Don't move this - it must be third.
int nativeData;

/** Play synchronously (default) */
public static final int PLAY_SYNC            = 0x0000  ;
/** Play asynchronously */
public static final int PLAY_ASYNC           = 0x0001  ;
/** Loop the sound until stop() is called */
public static final int PLAY_LOOP            = 0x0008  ;
/** Don't stop any currently playing sound */
public static final int PLAY_NOSTOP          = 0x0010  ;
/**
 * Loads and constructs a sound clip from a resource.
 */
public SoundClip(Class requestor,String path)
{
	this.path = path;
	if (Applet.currentApplet.isApplication)
		data = ewe.sys.Vm.readResource(requestor,path);
	if (data == null) throw new IllegalArgumentException("SoundClip resource was not found: "+path);
}
public SoundClip(String path)
{
	this(null,path);
}
public SoundClip(byte [] waveData)
{
	data = waveData;
}
/**
 * Stop the currently playing sound.
 * @return true if successful.
 */
//===================================================================
public boolean stop() {return false;}
//===================================================================

//===================================================================
private boolean destroy(){return true;}
//===================================================================

//===================================================================
public void free()
//===================================================================
{
	audioClip = null;
	data = null;
	destroy();
}

public void finalize(){
	synchronized(ewe.sys.Vm.getSyncObject()){
		free();
	}
}

/**
 * Plays the sound clip. Returns true if the sound starts playing and false otherwise.
* @param options Can be one of the PLAY_OPTIONS.
* @return
*/
public boolean play(int options)
{
	Applet applet = Applet.currentApplet;
	if (!applet.isApplication){
		if (!loaded) try {
			audioClip = applet.getAudioClip(applet.getCodeBase(), path);
		}catch (Exception e){ e.printStackTrace(); }
		loaded = true;
		if (audioClip == null) return false;
		audioClip.play();
		return true;
	}else{
		if (data == null) return false;
		try{
			return playSound(options);
		}catch(Throwable t){
			data = null;
			return false;
		}
	}
}

private boolean playSound(int options){return false;}
}
