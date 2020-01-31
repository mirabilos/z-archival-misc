__IDSTRING(rcsid_images, "$MirOS: contrib/hosted/ewe/vm/images.cpp,v 1.3 2008/04/11 00:27:22 tg Exp $");

/*
An rgbSection is a _temporary_ section of an Image that provides read and maybe write access
to the RGB data in the image.

The class does not give any indication of the source of the data, or where within the Image
the rgb refers to.

The class either provides all of the pixel data directly, or you can get the data by using
getScanLine() - which will always work.

*/
//###################################################
typedef class rgbSection{
//###################################################
protected:
bool externalTempLine;
uint32* tempLine;

public:
	rgbSection()
	{
		externalTempLine = false; tempLine = NULL;
		usesAlpha = false; pixels = NULL; rowStride = 0;
		isValid = true;
	}
virtual ~rgbSection(){if (tempLine != NULL && !externalTempLine) delete tempLine;}
//
// This can be used in situations when a temporary line will be necessary, but
// an externally allocated one is preferred.
//
	void setTempLine(uint32* line)
	{
		tempLine = line;
		externalTempLine = true;
	}
//
// This creates a temporary line for use during getScanLine(), etc.
// It creates a line of length width 32-bit integers.
//
	void createTempLine()
	{
		tempLine = new uint32[width];
		externalTempLine = false;
	}
//
// The dimensions of the section.
//
	int width, height;
//
// If this is true then the topmost byte, the Alpha channel, should
// be considered to hold valid data. If not, then all alpha values
// should be considered to be 255 - completely opaque, regardless of
// what is stored in the scan lines.
//
	bool usesAlpha;
//
// If pixels is NULL then you must use getScanLine()
// to get the image lines. Note, however, that each call
// to getScanLine() _may_ and should be considered to,
// invalidate the previous call.
//
// If pixels is valid, then it will always point to the
// very first pixel in the section.
//
	uint32* pixels;
//
// These are the number of 32-bit values that are on a line.
// This _may_ be more than "width". You would generally only
// use this if pixels is not NULL.
//
	int rowStride;
//
// If the section is considered valid, this is true.
//
	bool isValid;
//
// Get a pointer to a scan line. This should be considered to
// invalidate previous calls to getScanLine().
//
virtual uint32* getScanLine(int line)
{
	if (pixels != NULL) return pixels+line*rowStride;
	printf("getScanLine() - not implemented!");
	return NULL;
}
//
// Set the scan line data. If it returns false then the scan line
// could not be set.
//
virtual bool setScanLine(int line,uint32* data)
{
	if (pixels != NULL) {
		memcpy(pixels+line*rowStride,data,width);
		return true;
	}
	printf("setScanLine() - not implemented!");
	return false;
}
//
// Release any used resources.
// The default implementation of this deletes the rgbSection.
//
virtual void release(){delete this;}
//
// Write back update pixels to whence it came - if appropriate.
// The default implementation of this does nothing.
//
virtual bool restore() {}
//###################################################
} RgbSection;
//###################################################
