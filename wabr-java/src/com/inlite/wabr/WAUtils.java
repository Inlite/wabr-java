package com.inlite.wabr;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.*;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import javax.xml.bind.DatatypeConverter;


public class WAUtils
{
	public static delPrintLine fncPrintLine = null;
	
	public static void copyStream(InputStream in, OutputStream out,   int bufferSize)
			   throws IOException
			{
			   // Read bytes and write to destination until eof

			   byte[] buf = new byte[bufferSize];
			   int len = 0;
			   while ((len = in.read(buf)) >= 0)
			   {
			      out.write(buf, 0, len);
			   }
			}
	
	public static String inputStreamToString (final InputStream is, final int bufferSize)
	{
	  final char[] buffer = new char[bufferSize];
	  final StringBuilder out = new StringBuilder();
	  try (Reader in = new InputStreamReader(is, "UTF-8")) {
	    for (;;) {
	      int rsz = in.read(buffer, 0, buffer.length);
	      if (rsz < 0)
	        break;
	      out.append(buffer, 0, rsz);
	    }
	  }
	  catch (UnsupportedEncodingException ex) {
		  return "";  /* ... */
	  }
	  catch (IOException ex) {
	      return ""; /* ... */
	  }
	  return out.toString();
	}

	public static String xmlDocumentToString(Document doc) {
		try {
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (Exception ex) {
			return "";
		}
	}

	public static String fileToString (String file)
	{
		try {
			File fileDir = new File(file);

			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(fileDir), "UTF8"));

			String str, strOut = "";

			while ((str = in.readLine()) != null) {
				str = str.replace("\uFEFF", "");
				strOut += str + "\r\n";
			}

			in.close();
			return strOut;
		} 
   
		catch (Exception e)
		{
			return "";
		}
	}

	public static String signature(String image)
	{
		if (image == null || image.equals(""))
		{return "";	}
		return " [" + image.substring(0, Math.min(80, image.length())) + "] ";
	}


	public static void printLine()
	{printLine("");}

	public static void printLine(String msg)
	{
		if (fncPrintLine != null)
			{fncPrintLine.printLine(msg);}
		else
			System.out.println(msg);
	}

	public static String stringAppend(String str, String add, String sep)
	{
		if (sep.equals(""))
		{sep = " ,";}
		if (add.equals(""))
		{return str;}
		else
		{
			String sout = str;
			if (!sout.equals(""))
			{sout += sep;}
			sout += add;
			return sout;
		}
	}

	private static Node selectSingleNode (Node nodeParent, String name)
	{
		try{
			XPath xpath = XPathFactory.newInstance().newXPath();
			Node node =  (Node) xpath.evaluate(name, nodeParent, XPathConstants.NODE);
			return node;
		}
		catch (Exception e)  
		{return null;}
	}

	public static NodeList selectNodes (Node nodeParent, String name)
	{
		try{
			XPath xpath = XPathFactory.newInstance().newXPath();
			return   (NodeList) xpath.evaluate(name, nodeParent, XPathConstants.NODESET);
		}
		catch (Exception e)  
		{return null;}
	}

	private static String innerXml(Node node) {
		DOMImplementationLS lsImpl = (DOMImplementationLS)node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
		LSSerializer lsSerializer = lsImpl.createLSSerializer();
		lsSerializer.getDomConfig().setParameter("xml-declaration", false);
		NodeList childNodes = node.getChildNodes();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < childNodes.getLength(); i++) {
			sb.append(lsSerializer.writeToString(childNodes.item(i)));
		}
		return sb.toString(); 
	}

	public static String nodeValue(Node nodeParent, String name, String def)
	{
		String sout = def;
		Node node =  selectSingleNode(nodeParent, name);
		if (node != null)
		{sout = node.getTextContent();}
		return sout;
	}

	public static String nodeValueXml(Node nodeParent, String name, String def)
	{
		String sout = def;
		Node node =  selectSingleNode(nodeParent, name);
		if (node != null)
		{sout = innerXml(node);	}
		return sout;
	}

	public static int nodeValue(Node nodeParent, String name, int def)
	{
		int nout = def;
		Node node =  selectSingleNode(nodeParent, name);
		if (node != null)
		{
			try  
			{nout = Integer.parseInt(node.getTextContent()); } 
			catch(NumberFormatException nfe)   
			{;} 
		}
		return nout;
	}

	public static byte[] decodeBase64(String base64)
	{
		if (base64 == null || base64.equals(""))
		{return null;}
		try
		{return DatatypeConverter.parseBase64Binary(base64);}
		catch (Exception e)
		{return null;}
	}

	public static Document loadXMLFromString(String xml) 
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		}
		catch (Exception e)
		{return null;}
	}

	public static Document nodeToXmlDocument(Node nodeParent, String name)
	{
		Node node =  selectSingleNode(nodeParent, name);
		if (node == null)
		{return null;}
		String xml = innerXml(node); 
		if (xml == null || xml.equals(""))
		{return null;}
		return loadXMLFromString(xml); 
	}

	private static String getFileExtension(String fileName)
	{
		String extension = "";

		int i = fileName.lastIndexOf('.');
		int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

		if (i > p) {
			extension = fileName.substring(i+1);
		}
		return extension;
	}

	public static String FileToBase64(String file)
	{
		try
		{
			RandomAccessFile f = new RandomAccessFile(file, "r");
			byte[] filebytes = new byte[(int)f.length()];
			f.readFully(filebytes);

			String base64 = DatatypeConverter.printBase64Binary(filebytes);
			String ext = getFileExtension(file);

			base64 = "data:image/" + ext + ";base64," + base64;
			// Optionally attach suffix with reference file name to be placed in Barcode.File property
			base64 = base64 + ":::" + (new java.io.File(file)).getName();
			return base64;
		}
		catch (Exception ex)
		{
			WAUtils.printLine();
			WAUtils.printLine(ex.getMessage());
			return "";
		}
	}

	public static boolean isBase64(String value) // IsBase64String
	{
		String v = value;
		// replace formating characters
		v = v.replace("\r\n", "");
		v = v.replace("\r", "");
		// remove reference file name, if  present
		int ind = v.indexOf(":::");
		if (ind > 0)
		{
			v = v.substring(0, ind);
		}

		if (v == null || v.length() == 0 || (v.length() % 4) != 0)
		{
			return false;
		}
		int index = v.length() - 1;
		if (v.charAt(index) == '=')
		{
			index--;
		}
		if (v.charAt(index) == '=')
		{
			index--;
		}
		for (int i = 0; i <= index; i++)
		{
			if (IsInvalidBase64char(v.charAt(i)))
			{
				return false;
			}
		}
		return true;
	}

	private static boolean IsInvalidBase64char(char value)
	{
		int intValue = (int)value;
		if (intValue >= 48 && intValue <= 57)
		{
			return false;
		}
		if (intValue >= 65 && intValue <= 90)
		{
			return false;
		}
		if (intValue >= 97 && intValue <= 122)
		{
			return false;
		}
		return intValue != 43 && intValue != 47;
	}
}