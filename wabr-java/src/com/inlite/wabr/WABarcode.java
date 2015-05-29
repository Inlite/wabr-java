package com.inlite.wabr;
import org.w3c.dom.Document;

/** 
	Web API  Barcode class
*/
public class WABarcode
{
	public WABarcode()
	{
		setValues(new java.util.HashMap<String, String>());
	}
	/** 
	 Barcode data as a string 
	*/
	private String Text;
	public final String getText()
	{
		return Text;
	}
	public final void setText(String value)
	{
		Text = value;
	}
	/** 
	 Barcode data as a byte array 
	*/
	private byte[] Data;
	public final byte[] getData()
	{
		return Data;
	}
	public final void setData(byte[] value)
	{
		Data = value;
	}

	/** 
	 Barcode type (symbology) 
	*/
	private String Type;
	public final String getType()
	{
		return Type;
	}
	public final void setType(String value)
	{
		Type = value;
	}

	/** 
	 Length of barcode data 
	*/
	private int Length;
	public final int getLength()
	{
		return Length;
	}
	public final void setLength(int value)
	{
		Length = value;
	}

	/** 
	 Page number in the image file containing the barcode
	*/
	private int Page;
	public final int getPage()
	{
		return Page;
	}
	public final void setPage(int value)
	{
		Page = value;
	}

	/** 
	 Direction of the barcode rotation on an image 
	*/
	private String Rotation;
	public final String getRotation()
	{
		return Rotation;
	}
	public final void setRotation(String value)
	{
		Rotation = value;
	}

	/** 
	 Left coordinate of enclosing rectangle
	*/
	private int Left;
	public final int getLeft()
	{
		return Left;
	}
	public final void setLeft(int value)
	{
		Left = value;
	}

	/** 
	 Top coordinate of enclosing rectangle
	*/
	private int Top;
	public final int getTop()
	{
		return Top;
	}
	public final void setTop(int value)
	{
		Top = value;
	}

	/** 
	 Right coordinate of enclosing rectangle
	*/
	private int Right;
	public final int getRight()
	{
		return Right;
	}
	public final void setRight(int value)
	{
		Right = value;
	}

	/** 
	 Bottom coordinate of enclosing rectangle
	*/
	private int Bottom;
	public final int getBottom()
	{
		return Bottom;
	}
	public final void setBottom(int value)
	{
		Bottom = value;
	}


	/** 
	 Name of the image file containing the barcode. 
	*/
	private String File;
	public final String getFile()
	{
		return File;
	}
	public final void setFile(String value)
	{
		File = value;
	}


	/** 
	 Barcode reference information  (XML-formatted)
	*/
	private Document Meta;
	public final Document getMeta()
	{
		return Meta;
	}
	public final void setMeta(Document value)
	{
		Meta = value;
	}

	/** 
	  Decoded Values (e.g. Driver License/ID Data) 
	*/
	private java.util.HashMap<String, String> Values;
	public final java.util.HashMap<String, String> getValues()
	{
		return Values;
	}
	public final void setValues(java.util.HashMap<String, String> value)
	{
		Values = value;
	}

}