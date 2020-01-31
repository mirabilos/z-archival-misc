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
package ewe.database;
/**
This class contains constants used in the Database package.
**/
//##################################################################
public interface DatabaseTypes{
//##################################################################

/**
* This is the Integer (32-bit) field type.
In a Record object, this will be represented by an ewe.sys.Long object.
**/
public static final int INTEGER = 1;
/**
* This is the Long Integer (64-bit) field type.
In a Record object, this will be represented by an ewe.sys.Long object.
**/
public static final int LONG = 2;
/**
* This is the Boolean field type.
In a Record object, this will be represented by an ewe.sys.Long object.
**/
public static final int BOOLEAN = 3;
/**
* This is the String field type.
In a Record object, this will be represented by a ewe.util.CharArray object.
**/
public static final int STRING = 4;
/**
* This is the double precision floating point (64-bit) type. No single-precision
* floating point type is provided.
In a Record object, this will be represented by a ewe.sys.Double object.
**/
public static final int DOUBLE = 5;
/**
* This is the byte array type.
In a Record object, this will be represented by a ewe.sys.ByteArray object.
**/
public static final int BYTE_ARRAY = 6;
/**
* This is for a date and time (represented by ewe.sys.Time) value (saved as a 64-bit integer).
In a Record object, this will be represented by a ewe.sys.Time object.
**/
public static final int DATE_TIME = 7;
/**
* This is for an arbitrary sized decimal number stored as an ewe.sys.Decimal.
In a Record object, this will be represented by a ewe.sys.Decimal object.
**/
public static final int DECIMAL = 8;
/**
* This is for a date value (represented by ewe.sys.DayOfYear) value (saved as a 64-bit integer).
In a Record object, this will be represented by a ewe.sys.DayOfYear value.
**/
public static final int DATE = 9;
/**
* This is for a date value (represented by ewe.sys.TimeOfDay) value (saved as a 64-bit integer).
In a Record object, this will be represented by a ewe.sys.TimeOfDay value.
**/
public static final int TIME = 10;
/**
* This is for a timestamp value that measures time to nanosecond precision (saved as a 64-bit integer).
In a Record object, this will be represented by a ewe.sys.Long value.
**/
public static final int TIMESTAMP = 11;
/**
* This is for an arbitrary Java Object encoded somehow as bytes.
In a Record object, this will be represented by the decoded Java object.
**/
public static final int JAVA_OBJECT = 12;
/**
* This is an option for a Sort.
**/
//public static final int SORT_DESCENDING = 0x1;
/**
* This is an option for a Sort.
**/
public static final int SORT_IGNORE_CASE = 0x2;
/**
* This is an option for a Sort.
**/
//public static final int SORT_UNKNOWN_FIRST = 0x4;
/**
* This options indicates that an unknown field value (ie the field value is not set)
* is considered less than a known field value.
**/
public static final int SORT_UNKNOWN_IS_LESS_THAN_KNOWN = 0x4;
/**
* This options indicates that an unknown field value (ie the field value is not set)
* is considered greater than a known field value.
**/
public static final int SORT_UNKNOWN_IS_GREATER_THAN_KNOWN = 0x0;
/**
* This is an option for a Sort.
**/
public static final int SORT_DATE_ONLY = 0x8;
/**
* This is an option for a Sort.
**/
public static final int SORT_TIME_ONLY = 0x10;
/**
All fields equal to and above this value are considered "special" fields,
used for synchronization and other such tasks - ie the XXX_FIELD values.
**/
public static final int FIRST_SPECIAL_FIELD = 0xfff1;
/**
This is the maximum value a Field or Sort ID can be.
**/
public static final int MAX_ID = 0xfff0;
/**
* This is the reserved "EntryName" field. It is of type String. It is not added unless
* you request it.
**/
public static final int NAME_FIELD = FIRST_SPECIAL_FIELD+0;
/**
* This is the reserved "EntryOID" field. It is a 64-bit value (LONG) which
* should be unique to the entry. This is not added unless you request it.
**/
public static final int OID_FIELD = FIRST_SPECIAL_FIELD+1;
/**
* This is the reserved "CreatedDate" field. It is a 64-bit value representing
* when the table entry was created. This is not added unless you request it.
**/
public static final int CREATED_FIELD = FIRST_SPECIAL_FIELD+2;
/**
* This is the reserved "ModifiedDate" field. It is a 64-bit value representing
* when the table entry was created. This is not added unless you request it.
**/
public static final int MODIFIED_FIELD = FIRST_SPECIAL_FIELD+3;

/**
* This is the reserved "EntryFlags" field. It is a 32-bit value representing
* flags pertaining to the entry.
**/
public static final int FLAGS_FIELD = FIRST_SPECIAL_FIELD+4;
/*
Note! Do not set a flag value as 0x80000000 otherwise this will set flags with
that bit set to be negative. Also, make sure that FLAG_SYNCHRONIZED stays as
0x40000000 so that they can be sorted by their synchronized state.
*/
/**
* This is used with the FLAGS_FIELD and will specify that the entry has not been
* modified since last synchronized.
**/
public static final int FLAG_SYNCHRONIZED = 0x4000000;
/**
* This is the reserved "ObjectText" field. It is a String representing the
* text encoded data of a stored object.
**/
public static final int OBJECT_TEXT_FIELD = FIRST_SPECIAL_FIELD+5;
/**
* This is the reserved "ObjectBytes" field. It is a byte array representing the byte
* encoded data of a stored object.
**/
public static final int OBJECT_BYTES_FIELD = FIRST_SPECIAL_FIELD+6;
/**
* This is the reserved "ModifiedByWho" field. It is a 32-bit value representing
* the ID of the database which modified the entry. This is not added unless you request it.
**/
public static final int MODIFIED_BY_FIELD = FIRST_SPECIAL_FIELD+7;

public static final String [] reservedFieldNames =
{"EntryName","EntryOID","CreatedDate","ModifiedDate","EntryFlags","ObjectText","ObjectBytes","ModifiedBy"};
public static final String [] reservedFieldHeaders =
{"Entry Name","Entry OID","Created Date","Modified Date","Entry Flags","Object Text","Object Bytes","Modified By"};
public static final int [] reservedFieldIDs =
{NAME_FIELD,OID_FIELD,CREATED_FIELD,MODIFIED_FIELD,FLAGS_FIELD,OBJECT_TEXT_FIELD,OBJECT_BYTES_FIELD,MODIFIED_BY_FIELD};
public static final int [] reservedFieldTypes =
{STRING,LONG,DATE_TIME,DATE_TIME,INTEGER,STRING,BYTE_ARRAY,INTEGER};

/**
 * Use with Database.setOptions() - it indicates that if, when opening a DB,
 * the NEED_REINDEX flag is set (usually caused by an incomplete operation or error)
 * then instead of re-indexing, it will throw an IO exception instead.
 */
public static final int OPTION_ERROR_ON_NEED_REINDEX = 1;
//##################################################################
}
//##################################################################

