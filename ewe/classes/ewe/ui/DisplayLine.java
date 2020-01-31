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
package ewe.ui;
import ewe.fx.*;
import ewe.util.Vector;
import ewe.util.mString;

//##################################################################
public class DisplayLine extends ewe.util.LinkedListElement{
//##################################################################
//-------------------- Don't move these variables.
public String line;
public int flags;
/**
* This is the width of the line. If it is split with wrapping then it is the width
* of the first word.
**/
public int width;
//-----------------------------------------------
/**
* This is the index of the line within the editor/display.
**/
public int lineIndex;
/**
* Only valid for the first line in a section.
**/
public int numberInSection;
/**
* This is valid immediately after a split.
**/
public int lengthOfLine;
/**
* This is the width that was used when the DisplayLine was created split.
**/
public int displayWidth;

/**
* This is used by mTextPad and TextFormatters
**/
public boolean invalid = false;

/**
* Gets the first DisplayLine in the section that this line happens
* to be in. It will also calculate the lineIndex and numberInSection
* values for that first line.
**/
//===================================================================
public DisplayLine getSection(int indexOfThisLine)
//===================================================================
{
//Count to last line in section.
	DisplayLine c = this;
	int down = 0;
	for (;;down++){
		if ((c.flags & ENDS_WITH_NEWLINE) != 0) break;
		c = (DisplayLine)c.next;
		if (c == null) break;
	}
	int back = 0;
	c = this;
	for (;;back++){
		if (c.prev == null) break;
		c = (DisplayLine)c.prev;
		if ((c.flags & ENDS_WITH_NEWLINE) != 0){
			c = (DisplayLine)c.next;
			break;
		}
	}
	c.lineIndex = indexOfThisLine-back;
	c.numberInSection = down+back+1;
	return c;
}
/**
* This finds the index of the section the line is on.
**/
//===================================================================
public int findSectionIndex(DisplayLine line)
//===================================================================
{
	int idx = 0;
	for(DisplayLine first = this; first != null; first = (DisplayLine)first.next) {
		if (first == line) return idx;
		if ((first.flags & ENDS_WITH_NEWLINE) != 0) idx++;
	}
	return -1;
}
//===================================================================
public DisplayLine getIndexedSection(int index)
//===================================================================
{
	int li = 0;
	for(DisplayLine first = this; first != null; first = (DisplayLine)first.next) {
		if (index <= 0) return first.getSection(li);
		li++;
		if ((first.flags & ENDS_WITH_NEWLINE) != 0) index--;
	}
	return null;
}
/**
* This returns how many lines are from this line (inclusive) to the end
* of this section.
* @return The number of lines from this line to the end of its section. This value
* will always be a minimum of 1, if the line is the last line in the section.
*/
//===================================================================
public int countToSectionEnd()
//===================================================================
{
	int num = 0;
	for (DisplayLine dl = this; dl != null; dl = (DisplayLine)dl.next){
		num++;
		if ((dl.flags & ENDS_WITH_NEWLINE) != 0) break;
	}
	return num;
}
/**
* This compares the lengths of the lines as given in the array. It returns
* the index of the first line which differs, or -1 if no lines differ.
**/
//===================================================================
public int compareLineLengths(DisplayLine lines)
//===================================================================
{
	DisplayLine line = this;
	for (int i = 0; lines != null; i++){
		if (line == null) return i;
		if (line.length() != lines.lengthOfLine) return i;
		line = (DisplayLine)line.next;
		lines = (DisplayLine)lines.next;
	}
	return -1;
}
/**
* This changes the text of a line within a section and then sees if
* any lines apart from that line would be changed. If so, it will
* return the index of the first line which has changed, or -1 if none
* has been changed. This must be invoked on the first line of the section.
**/
//===================================================================
public int hasSectionChanged(DisplayLine changedLine,FontMetrics fm,int width,FormattedTextSpecs fts)
//===================================================================
{
	if (changedLine == null) return -1;
	changedLine.lengthOfLine = changedLine.line.length();
	DisplayLine dl = split(getSectionText(),fm,width,SPLIT_GET_LENGTHS,fts,null);
	return compareLineLengths(dl);
}
/**
* This changes the text of a line within a section and then sees if
* any lines apart from that line would be changed. If so, it will
* return the index of the first line which has changed, or -1 if none
* has been changed. This must be invoked on the first line of the section.
**/
//===================================================================
public int hasSectionChanged(DisplayLine changedLine,FontMetrics fm,int width)
//===================================================================
{
	return hasSectionChanged(changedLine,fm,width,null);
}
/**
* This returns the position in the section of the specified cursor line and position
* in the editor/display. This must be invoked on the first line in the section as
* calculated by getSection().
**/
//===================================================================
public int getPositionInSection(int cursorLine,int cursorPos)
//===================================================================
{
	int pos = 0;
	int down = cursorLine-lineIndex;
	DisplayLine line = this;
	for (int i = 0; i<down; i++){
		pos += line.length();
		line = (DisplayLine)line.next;
	}
	pos += cursorPos;
	return pos;
}
/**
* This calculates the cursor line and position for the given offset position
* from the start of the section. The width of the returned Dimension gives the
* "cursorPos" element and the height gives the "cursorLine" element.
**/
//===================================================================
public Dimension positionInSection(int position,Dimension dest,boolean onPrev)
//===================================================================
{
	if (dest == null) dest = new Dimension();
	DisplayLine line = this;
	int index = lineIndex;
	while (position != 0){
		if (line == null) break;
		int len = line.length();
		if (position >= len){
			if (onPrev && position == len) break;
			index++;
			position -= len;
			line = (DisplayLine)line.next;
		}else break;
	}
	dest.width = position;
	dest.height = index;
	return dest;
}
/**
* This replaces the section with the provided section (which can actually have more than
* one section in it). It must be called on
* the first line of a section only. Returns the difference in the number of lines
* after the section is replaced. The provided section may be null.
**/
//===================================================================
public int replaceSection(DisplayLine newSection)
//===================================================================
{
//Get the new section to calculate its lineIndex and numberInSection.
	if (newSection != null) newSection.getSection(lineIndex);
	int removing = numberInSection;
	int adding = countNext(newSection); //In case the newSection actually has more than one section.
	replaceSection(this,getNext(this,numberInSection-1),newSection);
	return adding-removing;
}
/**
* This can only be called on a section start.
**/
//===================================================================
public String getSectionText()
//===================================================================
{
	return concatenate(this,0,numberInSection);
}
//===================================================================
/**
* This indicates that this line actually has a '\n' character at the end. However it will
* not be present in the String given by the line member. Its trueLength() will be equal to
* its length() plus one.
**/
public static final int ENDS_WITH_NEWLINE = 0x1;
/**
* This is not used.
**/
public static final int ENDS_WITH_SPACE = 0x2;
/**
* This indicates that this line is used to provide blank lines above a non-blank line.
* It is used with empty lines (line == "" and no ENDS_WITH_NEWLINE) to indicate that it was
* placed there to provide space above a data line. There may be a number of these in sequence.
**/
public static final int PROVIDES_SPACE_ABOVE_LINE = 0x4;
/**
* This indicates that this line is used to provide blank lines below a non-blank line.
* It is used with empty lines (line == "" and no ENDS_WITH_NEWLINE) to indicate that it was
* placed there to provide space below a data line. There may be a number of these in sequence.
**/
public static final int PROVIDES_SPACE_BELOW_LINE = 0x8;
/**
* This is only valid immediately after doing a splitLines() call. It
* gives the width of the widest word.
**/
//===================================================================
public static int widest = 0;
//===================================================================

//===================================================================
public static String [] toLines(DisplayLine all)
//===================================================================
{
	if (all == null) return new String [0];
	int num = DisplayLine.countNext(all);
	String [] ret = new String[num];
	for (int i = 0;all != null; all = (DisplayLine)all.next)
		ret[i++] = all.line;
	return ret;
}
public static final int SPLIT_NO_WRAP = 0x1;
public static final int FIRST_WIDTH_ONLY = 0x2;
public static final int SPLIT_GET_LENGTHS = 0x4;


/**
 * Split the lines.
 * @param text The text to be split.
 * @param fm The FontMetrics for the Font and surface to be used for display.
 * @param width The width in pixels of the display area.
 * @param options This is a combination of SPLIT_GET_LENGTHS, SPLIT_NO_WRAP and FIRST_WIDTH_ONLY
 * @return An array of DisplayLine objects.
 */
//===================================================================
public static DisplayLine [] splitLines(String text,FontMetrics fm,int width,int options)
//===================================================================
{
	DisplayLine list = split(text,fm,width,options);
	int num = DisplayLine.countNext(list);
	DisplayLine [] ret = new DisplayLine[num];
	DisplayLine.toArray(list,num,ret);
	return ret;
}
//===================================================================
public int getWidth(FontMetrics fm,int options)
//===================================================================
{
	return DisplayLineSpecs.getWidth(this,fm,options);
}
//===================================================================
public static DisplayLine split(String text,FontMetrics fm,int width,int options)
//===================================================================
{
	return split(text,fm,width,options,null,null);
}
//===================================================================
public static DisplayLine split(String text,FontMetrics fm,int width,int options,FormattedTextSpecs ft)
//===================================================================
{
	return split(text,fm,width,options,ft,null);
}
//===================================================================
public static DisplayLine split(String text,FontMetrics fm,int width,int options,FormattedTextSpecs fts,WidthProvider wp)
//===================================================================
{
	/*
	DisplayLine ret[] = null;
	if (width <= 0 || (options & SPLIT_NO_WRAP) != 0){
		String [] ln = ewe.util.mString.split(text,'\n');
		ret = new DisplayLine[ln.length];
		for (int i = 0; i<ln.length; i++) {
			int lw = fm.getTextWidth(ln[i]);
			if (lw > widest) widest = lw;
			ret[i] = new DisplayLine();
			ret[i].line = ln[i];
			ret[i].flags |= ENDS_WITH_NEWLINE;
		}
		return ret;
	}
	*/
	DisplayLineSpecs dls = new DisplayLineSpecs();
	dls.source = ewe.sys.Vm.getStringChars(text);
	dls.start = 0;
	dls.length = dls.source.length;
	dls.fm = fm;
	dls.displayWidth = width;
	dls.format = fts;
	dls.widthProvider = wp;
	DisplayLine ret = dls.split(options);
	widest = dls.widest;
	return ret;
}
//===================================================================
public String toString()
//===================================================================
{
	return line;
}
//===================================================================
public int length() {return line.length();}
//===================================================================


//===================================================================
public int trueLength() {return line.length()+(((flags & ENDS_WITH_NEWLINE) != 0) ? 1 : 0);}
//===================================================================

//===================================================================
public String substring(int start,int end)
//===================================================================
{
	return line.substring(start,end);
}

//===================================================================
public static String concatenate(DisplayLine lines,int start,int length)
//===================================================================
{
	StringBuffer sb = new StringBuffer();
	lines = (DisplayLine)DisplayLine.getNext(lines,start);
	for (int i = 0; i<length && lines != null; i++){
		if (lines.line != null)
			sb.append(lines.line);
		if ((lines.flags & lines.ENDS_WITH_NEWLINE) != 0) sb.append('\n');
		lines = (DisplayLine)lines.next;
	}
	return sb.toString();
}
//===================================================================
public static String concatenate(DisplayLine from,DisplayLine to)
//===================================================================
{
	StringBuffer sb = new StringBuffer();
	while (from != null){
		if (from.line != null) sb.append(from.line);
		if ((from.flags & ENDS_WITH_NEWLINE) != 0) sb.append('\n');
		if (from == to) break;
		from = (DisplayLine)from.next;
	}
	return sb.toString();
}
//===================================================================
public static DisplayLine [] replaceLines(DisplayLine [] original,int start,DisplayLine [] newLines)
//===================================================================
{
	if (original == null || start == 0) return newLines;
	DisplayLine [] ret = new DisplayLine[start+newLines.length];
	ewe.sys.Vm.copyArray(original,0,ret,0,start);
	ewe.sys.Vm.copyArray(newLines,0,ret,start,newLines.length);
	return ret;
}
/**
* Counts how many lines after this one are in the same section.
**/
/*
//===================================================================
public int countToSectionEnd()
//===================================================================
{
	DisplayLine c = this;
	for (int i = 0;;i++){
		if ((c.flags & ENDS_WITH_NEWLINE) != 0) return i;
		c = (DisplayLine)c.next;
		if (c == null) return i;
	}
}
//===================================================================
public int countToSectionStart()
//===================================================================
{
	DisplayLine c = (DisplayLine)prev;
	for (int i = 0;;i++){
		if (c == null) return i;
		if ((c.flags & ENDS_WITH_NEWLINE) != 0) return i;
		c = (DisplayLine)c.prev;
	}
}
*/


/**
 * This locates the row and column index of a character from the original text in the
 * list of DisplayLines.
 * @param characterIndex The original index of the character.
 * @param dest a destination Dimension to hold the results. It can be null, in which case
 * a new one will be allocated and returned.
 * @return A Dimension object where the width variable denotes the index within the line and
 * the height variable donates the index of the line. It will return null if the index is not
 * in the orignal text.
 */
//===================================================================
public Dimension locate(int characterIndex,Dimension dest)
//===================================================================
{
	if (dest == null) dest = new Dimension();
	int idx = characterIndex;
	int h = 0, w = 0;
	for (DisplayLine l = this; l != null; l = (DisplayLine)l.next){
		int len = l.line.length();
		if ((l.flags & (ENDS_WITH_NEWLINE|ENDS_WITH_SPACE)) != 0) len++;
		if (len >= idx+1) {
			dest.width = idx;
			dest.height = h;
			return dest;
		}
		h++;
		idx -= len;
	}
	return null;
}
/**
 * Get a sequence of blank (empty) lines to be placed either above or below a data line.
 * @param count The number of lines to get.
 * @param forAbove if this is true then the lines are meant to be above a data line,
	otherwise it is for below a data line.
 * @return a sequence of blank lines to be placed either above or below a data line.
 */
//===================================================================
public static DisplayLine getBlankLines(int count, boolean forAbove)
//===================================================================
{
	if (count == 0) return null;
	int flag = forAbove ? PROVIDES_SPACE_ABOVE_LINE : PROVIDES_SPACE_BELOW_LINE;
	DisplayLine ret = null;
	for (int i = 0; i<count; i++){
		DisplayLine dl = new DisplayLine();
		dl.line = "";
		dl.next = ret;
		dl.flags |= flag;
		if (ret != null) ret.prev = dl;
		ret = dl;
	}
	return ret;
}
/**
* Count how many blank lines are above or below this line.
* @param above if this is true then you are counting the blank lines above, otherwise
* you are counting those below.
* @return the number of blank lines above or below this one.
*/
//===================================================================
public int countBlankLines(boolean above)
//===================================================================
{
	int flag = above ? PROVIDES_SPACE_ABOVE_LINE : PROVIDES_SPACE_BELOW_LINE;
	int num = 0;
	for(DisplayLine d = this;;num++){
		d = above ? (DisplayLine)d.prev : (DisplayLine)d.next;
		if ((d == null) || ((d.flags & flag) == 0)) return num;
	}
}
/*
//===================================================================
public String getPrevNext()
//===================================================================
{
	return "This: "+ewe.sys.Vm.identityHashCode(this)+", Next: "+ewe.sys.Vm.identityHashCode(next)+", Prev: "+ewe.sys.Vm.identityHashCode(prev);
}
*/
//##################################################################
public interface WidthProvider{
//##################################################################
/**
 * This should return the display width for the specified line index.
 * @param specs The DisplayLineSpecs that is doing the splitting.
* @param lineIndex The index of the line being created (starting from 0).
* @param startingCharacterIndex The index of the character that will come first on this line.
* @param splitSoFar The linked list of DisplayLines that have been split so far.
* @return The display width for the line.
*/
public int getWidthFor(DisplayLineSpecs specs, int lineIndex, int startingCharacterIndex, DisplayLine splitSoFar);

//##################################################################
}
//##################################################################

//##################################################################
}
//##################################################################

