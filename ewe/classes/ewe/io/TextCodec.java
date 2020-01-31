package ewe.io;
/**
* This class is used by the TextReader and TextWriter classes to convert Unicode text (Strings)
* into bytes, <b>encodeText()</b>, and to convert encoded bytes into Unicode text, <b>decodeText()</b>.
**/
//##################################################################
public interface TextCodec extends ewe.util.Copyable{
//##################################################################
/**
 * Enocde the Unicode characters into a byte Stream.
* @param text The characters to encode. This can be null if length is zero and endOfData is true. You
would use those settings to tell the codec that no more characters are to be encoded and
the final set of output bytes (if any) should be output.
* @param start The start of the characters in the text array.
* @param length The number of characters to encode.
* @param endOfData If this is true this tells the codec that no more characters are to be
* encoded and the final set of output bytes (if any) should be output.
* @param destination A ByteArray to hold the output data. The output data goes in the data member and
* and the length member holds how many bytes were output.
* @return The destination ByteArray or a new one if the destination was null.
* @exception ewe.io.IOException if an error occurs during encoding.
*/
public ewe.util.ByteArray encodeText(char[] text, int start, int length, boolean endOfData, ewe.util.ByteArray destination) throws ewe.io.IOException;
/**
 * Decode the bytes into Unicode characters.
 * @param encodedText The encoded bytes. This can be null if length is zero and endOfData is true. You
would use those settings to tell the codec that no more characters are to be decoded and
the final set of decoded characters (if any) should be output.
* @param start The start of the bytes in the byte array.
* @param length The number of bytes to decode.
* @param endOfData If this is true this tells the codec that no more characters are to be decoded and
the final set of decoded characters (if any) should be output.
* @param destination A CharArray to hold the output data. The output data goes in the data member and
* and the length member holds how many characters were output.
* @return The destination CharArray or a new one if the destination was null.
* @exception ewe.io.IOException if an error occurs during decoding, including badly formatted data.
 */
public ewe.util.CharArray decodeText(byte[] encodedText,  int start, int length, boolean endOfData, ewe.util.CharArray destination) throws ewe.io.IOException;
/**
* This aborts any on-going processing and frees resources associated with the codec.
* The codec should not be used again after this.
* An IOException should be thrown if there was an error closing the process.
**/
public void closeCodec() throws IOException;

/**
* This should return a new instance of the TextCodec, ready to begin converting a new set of
* data. This is needed because TextCodec objects are not resetable and cannot be reused.
**/
public Object getCopy();

//##################################################################
}
//##################################################################

