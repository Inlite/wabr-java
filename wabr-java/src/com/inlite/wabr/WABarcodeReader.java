package com.inlite.wabr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class WABarcodeReader
{
	private String _serverUrl = "wabr.inliteresearch.com";
	private String _authorization = "";

	public delReaderCallback diagCallback = null;
	public String _image = "";

	private WABarcode[] _barcodes = new WABarcode[] { };
	private String _error = "";

	class _ReaderCallback implements delReaderCallback {
		@Override
		public void procEvent(String evnt, Object value, Object obj) {
			if (value == null || obj == null)
			{
				return;
			}
			WABarcodeReader wabr = (WABarcodeReader)obj;
			if (evnt.equals("error"))
			{
				wabr._error = value.toString();
			}
			else if (evnt.equals("barcodes"))
			{
				wabr._barcodes = (WABarcode[])value;
			}
			if (diagCallback != null)
			{
				diagCallback.procEvent(evnt, value, _image);
			}
			return;			
		}
	}
	
	public WABarcodeReader(String serverUrl, String authorization)
	{
		_serverUrl = serverUrl;
		_authorization = authorization;
	}


	public String types = "";
	public static String validtypes = "1d,Code39,Code128,Code93,Codabar,Ucc128,Interleaved2of5," + "Ean13,Ean8,Upca,Upce," + "2d,Pdf417,DataMatrix,QR," + "DrvLic," + "postal,imb,bpo,aust,sing,postnet," + "Code39basic,Patch";
	public String directions = "";
	public int tbr_code= 0;
	public final WABarcode[] Read(String image)
	{
		ReadOpt(image, types, directions, tbr_code);
		if (!_error.equals(""))
		{
			throw new RuntimeException(_error);
		}
		return _barcodes;
	}


	public final void ReadAsync(String image, delReaderCallback callback, Object obj)
	{
		ReadOptAsync(image, callback, obj, types, directions, tbr_code);
	}


	public final WABarcode[] ReadOpt(String image, String types, String directions, int tbr_code)
	{
		delReaderCallback tempCallback = new _ReaderCallback(); // 
//		ReadOptAsync(image, types, directions, tbr_code, tempCallback, this);
		ReadOptAsync(image, null, null, types, directions, tbr_code);
		if (!_error.equals(""))
		{
			throw new RuntimeException(_error);
		}
		return _barcodes;

	}

	public final void ReadOptAsync(String image, delReaderCallback callback, Object obj, String types, String directions, int tbr_code)
	{
		_barcodes = new WABarcode[] { };
		_error = "";
		WACallback cb = new WACallback();
		if (callback != null)
		{
			cb.callback = callback;
			cb.obj = obj;
			cb.isAsync = true;
		}
		else
		{
			delReaderCallback tempCallback = new _ReaderCallback(); // 
			cb.callback = tempCallback;
			cb.obj = this;
			cb.isAsync = false;
		}
		_image = image;
		cb.call("image", image);

		String[] names = image.split("\\|");
		java.util.ArrayList<String> urls = new java.util.ArrayList<String>(), files = new java.util.ArrayList<String>(), images = new java.util.ArrayList<String>();
		for (String name1 : names)
		{
			String name = name1.trim();
			if (name.equals("")) continue;
			String s = name.toLowerCase();
			if (s.startsWith("http://") || s.startsWith("https://") || s.startsWith("ftp://") || s.startsWith("file://"))
			{
				urls.add(name);
			}
			else if ((new java.io.File(name)).isFile())
			{
				files.add(name);
			}
			else if (name.startsWith("data:") || WAUtils.isBase64(name))
			{
				images.add(name);
			}
			else
			{
				throw new RuntimeException("Invalid image source: " + name.substring(0, Math.min(name.length(), 256)));
			}
		}



		ReadLocal(urls, files, images, types, directions, tbr_code, cb);
	}

	public static WABarcode[] ParseResponse(String txtResponse)
	{
		java.util.ArrayList<WABarcode> barcodes = new java.util.ArrayList<WABarcode>();
		if (txtResponse.startsWith("<"))
		{
			Document doc = WAUtils.loadXMLFromString(txtResponse);
			if (doc == null) return new WABarcode[0];
			NodeList nl = WAUtils.selectNodes (doc.getDocumentElement(), "//Barcode");
			if (nl == null) return new WABarcode[0];
			for (int i = 0; i < nl.getLength(); i++) {
				Node nodeBarcode = nl.item(i);
				if (nodeBarcode.getNodeType() != Node.ELEMENT_NODE) continue; 
				WABarcode barcode = new WABarcode();
				barcode.setText(WAUtils.nodeValue(nodeBarcode, "Text", ""));
				barcode.setLeft(WAUtils.nodeValue(nodeBarcode, "Left", 0));
				barcode.setRight(WAUtils.nodeValue(nodeBarcode, "Right", 0));
				barcode.setTop(WAUtils.nodeValue(nodeBarcode, "Top", 0));
				barcode.setBottom(WAUtils.nodeValue(nodeBarcode, "Bottom", 0));
				barcode.setLength(WAUtils.nodeValue(nodeBarcode, "Length", 0));
				barcode.setData(WAUtils.decodeBase64(WAUtils.nodeValue(nodeBarcode, "Data", "")));
				barcode.setPage(WAUtils.nodeValue(nodeBarcode, "Page", 0));
				barcode.setFile(WAUtils.nodeValue(nodeBarcode, "File", ""));
				barcode.setMeta(WAUtils.nodeToXmlDocument(nodeBarcode, "Meta"));

				barcode.setType(WAUtils.nodeValue(nodeBarcode, "Type", ""));
				barcode.setRotation(WAUtils.nodeValue(nodeBarcode, "Rotation", ""));

				Document docValues = WAUtils.nodeToXmlDocument(nodeBarcode, "Values");
				if (docValues != null)
				{
					NodeList nl1 = docValues.getDocumentElement().getChildNodes();
					for (int j = 0; j < nl1.getLength(); j++) {
						Node node = nl1.item(j);
						if (node.getNodeType() != Node.ELEMENT_NODE) continue; 
						barcode.getValues().put(node.getNodeName(), node.getTextContent());
					}
				}

				barcodes.add(barcode);
			}
		}
		return barcodes.toArray(new WABarcode[0]);
	}



	private void ReadLocal(java.util.ArrayList<String> urls, java.util.ArrayList<String> files, java.util.ArrayList<String> images, 
			String types_, String directions_, int tbr_code_, WACallback cb)
	{
		if (types_ == null)  types_ = "";
		if (directions_ == null)  directions_ = "";
		String server = _serverUrl;
		if (server.equals(""))
		{
			server = "https://wabr.inliteresearch.com"; // default server
		}
		java.util.HashMap<String, String> queries = new java.util.HashMap<String, String>();

		String url = "";
		for (String s : urls)
		{
			if (!url.equals(""))
			{
				url += "|";
			}
			url += s;
		}
		if (!"url".equals(""))
		{
			queries.put("url", url);
		}

		String image = "";
		for (String s : images)
		{
			if (!image.equals(""))
			{
				image += "|";
			}
			image += s;
		}
		if (!"image".equals(""))
		{
			queries.put("image", image);
		}

		queries.put("format", "xml");
		queries.put("fields", "meta");
		if (!types_.equals(""))
		{
			queries.put("types", types_);
		}
		if (!directions_.equals(""))
		{
			queries.put("options", directions_);
		}
		if (tbr_code_ != 0)
		{
			queries.put("tbr",  Integer.toString(tbr_code_));
		}

		String serverUrl = server + "/barcodes";
		if (cb.isAsync)
		{
			Runnable http = new WAHttpRequest(serverUrl, _authorization, files, queries, cb);
			new Thread(http).start();
		}
		else 
		{
		WAHttpRequest http = new WAHttpRequest(serverUrl, _authorization, files, queries, cb);
		http.run();
		}
	}
}