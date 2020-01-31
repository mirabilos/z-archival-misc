package ewe.data;
/**
* ISelectable is an interface that represents an Object that contains some
* sort of list of which various items can be selected.
**/
//##################################################################
public interface IndexSelectable{
//##################################################################
/**
 * Get a list of all the currently selected items as an array of indexes.
 * If no items are selected this will return an integer array with zero elements.
 * @return a list of all the currently selected items as an array of indexes.
 */
public int [] getSelectedIndexes();
/**
 * Set which indexes should be selected. Any items not in this array will be deselected
	if they are currently selected. So if you call this with an int array of zero elements,
	this will have the effect of deselecting all indexes.
 * @param chosen an array of integer indexes to be set as the selection.
 */
public void setSelectedIndexes(int [] chosen);

/**
 * Select or deselect a single item.
 * @param index the index of the item.
 * @param chosen true to select the item, false to deselect it.
 */
public void select(int index,boolean chosen);
/**
 * Checks if an item is selected.
 * @param index the index of the item.
 * @return true if the item is selected, false if not.
 */
public boolean isSelected(int index);
/**
 * Returns the number of items that are currently selected.
 * @return the number of items that are currently selected.
 */
public int countSelectedIndexes();
/**
 * This returns one of the selected index. Say a list has items 2, 5 and 11 selected. Then
 * calling countSelectedIndexes() will return a value of 3 - indicating 3 items are selected.
 Calling this method with indexOfSelectedIndex being 0 will return the value 2 which is the index
	of the first selected item. Calling this method with indexOfSelectedIndex being 1 will return
	the value 5 and calling with a parameter of 2 will return the value 11.
 * @param indexOfSelectedIndex which of the selected indexes to return.
 * @return one of the selected indexes.
 */
public int getSelectedIndex(int indexOfSelectedIndex);
//##################################################################
}
//##################################################################

