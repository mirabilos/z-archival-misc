/*
Note - This is the Linux version of SoundClip.java
*/

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


/**
 * SoundClip is a sound clip. The sound clip is loaded and then played when requested.
 * Support will soon be added for looping the sound and stopping the sound.
 *
 * <pre>
 * SoundClip s = new SoundClip("sound.wav");
 * s.play(PLAY_ASYNC);
 * </pre>
 */
public class SoundClip
{
//Don't move this - it must be first.
public byte [] data;
//Don't move this - it must be second.
public int playStatus;
//Don't move this - it must be third.
public int fixedData;

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
	this(ewe.sys.Vm.readResource(requestor,path));
}
public SoundClip(String path)
{
	this(null,path);
}
public SoundClip(byte [] waveData)
{
	data = waveData;
	if (data != null) {
		ewe.io.File f = ewe.sys.Vm.newFileObject().createTempFile("snd","wav",null);
		if (f != null){
			ewe.io.Stream out = f.getOutputStream();
			if (out != null){
				int got = out.writeBytes(waveData,0,waveData.length);
				out.close();
				if (got == waveData.length)
					nativeInit(f.getFullPath());
			}
		}
	}
}
private native void nativeInit(String fileName);
/**
 * Plays the sound clip. Returns true if the sound starts playing and false otherwise.
* @param options Can be one of the PLAY_OPTIONS.
* @return
*/
public native boolean play(int options);
/**
 * Stop the currently playing sound.
 * @return true if successful.
 */
public native boolean stop();

public native void free();

public void finalize(){
	synchronized(ewe.sys.Vm.getSyncObject()){
		//ewe.sys.Vm.debug("Freeing:"+ewe.sys.Vm.countObjects(false));
		free();
	}
}

}
