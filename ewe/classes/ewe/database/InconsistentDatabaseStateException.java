package ewe.database;
import ewe.io.IOException;
import ewe.io.RandomAccessStream;
import ewe.io.RandomAccessFile;
import ewe.util.Utils;
import ewe.util.IntArray;
import ewe.io.DataProcessor;
import ewe.io.File;
import ewe.io.FastStream;
import ewe.sys.Time;

/**
When changes are made to a RecordFile database
(or any one object that uses a RandomAccessDatabaseStream as its underlying IO stream)
these changes are done "safely". That is to say, changes are done via a idempotent
commit operation that is stored within the file. Should the application abort or crash
before the commit operation  is completely done, the RandomAccessDatabaseStream will
attempt to re-apply the commit operation the next time the database is opened.<p>
However if the database is opened in read-only mode, and the Database cannot determine
a way to re-open it in "rw" mode to re-apply the commit, then this exception will
be thrown.<p>
The correct way to handle this exception is to explicitly re-open the database in "rw"
mode - to allow the commit to be applied, and then close it and re-open it in "r" mode
as originally intended.<p>
If you absolutely cannot open the database in "rw" mode then you must open it in "r" mode
specifying that the commit operation should be ignored.
**/
//##################################################################
public class InconsistentDatabaseStateException extends IOException{
//##################################################################

public InconsistentDatabaseStateException() {}

//##################################################################
}
//##################################################################

