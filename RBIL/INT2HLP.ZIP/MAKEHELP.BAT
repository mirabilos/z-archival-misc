@ECHO OFF
CLS
ECHO PROCESS REQUIRES MORE THAN 20Mb FREE HARD DISK
ECHO SPACE.   READ THE README FILE BEFORE STARTING.
ECHO ----------------------------------------------
ECHO  YOU CHOOSE HELPMAKE COMPRESSION FACTOR '%1'
ECHO  '' IS FOR NORMAL COMPRESSION. COMPRESSION
ECHO  GOES FROM 0 (NONE) TO 15 (MAX). NORMAL IS 3
ECHO ----------------------------------------------
ECHO PRESS CTRL+C NOW TO ABORT CONVERSION
PAUSE
INT2HM.COM
HM2HLP.BAT %1
