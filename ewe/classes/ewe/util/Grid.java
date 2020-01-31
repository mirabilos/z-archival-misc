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
package ewe.util;
/**
* A Grid is a way of storing a 2-D collection of objects. Objects are either
* added left to right and top to bottom OR an explicit row and column can be
* set.<p>
*
* When adding sequentially call the add(Object what,boolean lastInRow) method
* to add an object to the end of the current row. End a row either by calling
* endRow() or by setting "lastInRow" true when adding the last object to the
* row.
**/
//##################################################################
public class Grid extends Vector{
//##################################################################
/**
* This holds the number of columns in the Grid. Only read this value,
* do not set it.
**/
public int columns;
/**
* This holds the number of rows in the Grid. Only read this value,
* do not set it.
**/
public int rows;

private Vector curRow = null;

//-------------------------------------------------------------------
protected Object addToRow(Vector row,Object what)
//-------------------------------------------------------------------
{
	row.add(what);
	if (row.size() > columns) columns = row.size();
	return what;
}
/**
* End the current row - all future add() calls will add objects to a new
* row.
**/
//==================================================================
public void endRow() {curRow = null;}
//==================================================================
/**
* Add the object to the current row. If lastInRow is true, the current row
* will be ended after the object is added.
**/
//==================================================================
public Object add(Object what,boolean lastInRow)
//==================================================================
{
	if (curRow == null) {
		add(curRow = new Vector());
		rows++;
	}
	curRow.add(what);
	if (curRow.size() > columns) columns = curRow.size();
	if (lastInRow) endRow();
	return what;
}
/**
* Add all elements of the Vector parameter to the current row.
**/
//===================================================================
public void addAll(Vector what)
//===================================================================
{
	for (int i = 0; i<what.size(); i++) add(what.get(i),false);
}
/**
* Clear the entire grid.
**/
//==================================================================
public void clear()
//==================================================================
{
	super.clear();
	curRow = null;
	columns = rows = 0;
}
/**
* Return the Object stored at the index. The row for the index will be
* index/columns and the column will be index%columns.
**/
//==================================================================
public Object objectAt(int index)
//==================================================================
{
	if (rows == 0 || columns == 0) return null;
	return objectAt(index/columns,index%columns);
}
/**
* Return the Object stored at the specified row and column.
**/
//==================================================================
public Object objectAt(int row,int column)
//==================================================================
{
	if (row >= size() || row<0) return null;
	Vector v = (Vector)get(row);
 	if (column >= v.size() || column<0) return null;
	return v.get(column);
}
/**
* Return the Vector which represents the row which is currently being
* added to (you are hardly likely to use this method).
**/
//==================================================================
public Vector getMostCurrentRow()
//==================================================================
{
	if (rows == 0) return null;
	if (curRow != null) return curRow;
	return (Vector)get(rows-1);
}
/**
* Set the Object at a particluar row and column.
**/
//===================================================================
public void set(int row,int column,Object what)
//===================================================================
{
	while (row >= size()) add(new Vector());
	for (int i = 0; i<size(); i++){
		Vector v = (Vector)get(i);
		while (column >= v.size()) v.add(null);
	}
	Vector v = (Vector)get(row);
	v.set(column,what);
	if (row >= rows) rows = row+1;
	if (column >= columns) columns = column+1;
}
//##################################################################
}
//##################################################################

