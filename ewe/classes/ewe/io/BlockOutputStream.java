/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
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
package ewe.io;
import ewe.util.ByteArray;
import ewe.util.Utils;
import ewe.security.SecureDocument;
import ewe.security.EncryptionKey;
/**
* A BlockOutputStream is used to write out a block of data that is received
* by a BlockInputStream as a single block.<p>
* You can also provide an Encryptor for the Stream in which case the receiving BlockInputStream
* must also have a matching Decryptor.
**/
//##################################################################
public class BlockOutputStream extends BlockIO{
//##################################################################
protected OutputStream out;
private ByteArray ba;
/**
* Create a BlockOutputStream using the provided OutputStream for writing
* out data.
**/
//===================================================================
public BlockOutputStream(OutputStream out)
//===================================================================
{
	super(true);
	this.out = out;
}
/**
* This is used to set the dontUseCompression option for data encryption.
**/
//===================================================================
public void setDontUseCompression(boolean dontUseCompression)
//===================================================================
{
	getSecureDocument().dontUseCompression = dontUseCompression;
}
/**
 * Explicitly set the encryptor to use.
 * @exception IOException
 */
//===================================================================
public void setEncryptor(DataProcessor encryptor) throws IOException
//===================================================================
{
	getSecureDocument().setEncryptor(encryptor);
}
/**
 * Set the encryptor to be a new Encryptor using the specified password.
 * @exception IOException
 */
//===================================================================
public void setEncryptor(String password) throws IOException
//===================================================================
{
	getSecureDocument().setPassword(password);
}
/**
* This forces the output stream to change its encryption. This is only
* used when a remote Public Key is available. The new encryption key is
* encrypted using the receiver's Public Key and then sent along with the data
* encrypted using that new key.
**/
//===================================================================
public void changeEncryption() throws IOException
//===================================================================
{
	getSecureDocument().changeEncryption();
}
/**
* Write out a block of data.
**/
//===================================================================
public void writeBlock(ByteArray data) throws IOException
//===================================================================
{
	writeBlock(data.data,0,data.length);
}
//-------------------------------------------------------------------
private void writeDirect(byte[] source,int offset,int length) throws IOException
//-------------------------------------------------------------------
{
	Utils.writeInt(length,intBuff,0,4);
	out.write(intBuff,0,4);
	if (length < 0) length = -length;
	out.write(source,offset,length);
	out.flush();
}
/**
* Write out a block of data, encrypting it as necessary.
**/
//===================================================================
public void writeBlock(byte[] source,int offset,int length) throws IOException
//===================================================================
{
	boolean usesEncryption = false;
	if (secureDocument != null)
		usesEncryption = secureDocument.hasEncryptionParameters();
	if (!usesEncryption) writeDirect(source,offset,length);
	else{
		SecureDocument sd = getSecureDocument();
		sd.setData(source,offset,length);
		ba = sd.encode(ba);
		writeDirect(ba.data,0,-ba.length);
	}
}
/**
* Close this BlockOutputStream and the underlying OutputStream.
* @exception IOException if an error occurs closing the underlying OutputStream.
*/
//===================================================================
public void close() throws IOException
//===================================================================
{
	out.close();
}
/**
 * Set the Public/Private keys.
 * @param remotePublicKey This is the public key of the
 * receiver and the symmetric key used to encrypt each block of data will be encrypted
 * using this key.
 * @param myPrivateKey if this is not null it will be used to sign data being transmitted.
 * If you do not want to sign outgoing data, you can leave this null.
 */
//===================================================================
public void setKeys(EncryptionKey remotePublicKey,EncryptionKey myPrivateKey)
//===================================================================
{
	SecureDocument sd = getSecureDocument();
	sd.setKeys(remotePublicKey,myPrivateKey);
}

//##################################################################
}
//##################################################################

