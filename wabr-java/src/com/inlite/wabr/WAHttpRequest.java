package com.inlite.wabr;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

/** 
	Web API  HTTP Request class
 */
public class WAHttpRequest implements Runnable
{

	public static int _timeoutSec = 0;
	public static String _method = "post";  // can be "get", "post", "postenc"
	
	private String _serverUrl;
	private String _authorization;
	private java.util.ArrayList<String> _files;
	private java.util.HashMap<String, String>  _queries;
	private WACallback _cb;
	
	public WAHttpRequest (String serverUrl, String auth, java.util.ArrayList<String> files, java.util.HashMap<String, String> queries, WACallback cb)
	{
		_serverUrl = serverUrl;
		_authorization = auth;
		_files = files;
		_queries = queries;
		_cb = cb;
	}
	
	public void run()
	{
		ExecRequest (_serverUrl, _authorization, _files, _queries, _cb);
	}

	// 	http://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests
	private  void performRequest(String url, String auth, String method, String type, byte[] data, WACallback cb, int retries)
	{
		HttpURLConnection connection = null;
		try{
			String env_auth = "WABR_AUTH";
			String env = System.getenv(env_auth); 
			if (auth == null) auth = "";
			if (auth == "" && env != null)
				auth = env;
			
			URL request_url = new URL(url);
			connection = (HttpURLConnection) request_url.openConnection();
			connection.setRequestMethod(method);

			if (!auth.equals(""))
			{connection.addRequestProperty("Authorization", auth);}
			// HttpURLConnection.setFollowRedirects(true);
			if (_timeoutSec != 0) // default is 100sec
			{
				connection.setConnectTimeout(_timeoutSec * 1000);
				connection.setReadTimeout(_timeoutSec * 1000);
			}
			if (method.equals("POST"))
			{
				if (!type.equals(""))
				{
					connection.setRequestProperty("Content-Type", type); 
					connection.setRequestProperty("Content-Length", Integer.toString(data.length));
				}		
				connection.setDoOutput(true);
				OutputStream stream = connection.getOutputStream();
				try {
					stream.write(data, 0, data.length);
				} finally {
					stream.close();
				}
			}
			int code = connection.getResponseCode();
			if (code == 200)
			{
				InputStream response = connection.getInputStream();
				String txtResponse = WAUtils.inputStreamToString(response, 4096);
				WABarcode[] barcodes = WABarcodeReader.ParseResponse(txtResponse);
				cb.call("barcodes", barcodes);
			}
			else
			{
				String location = connection.getHeaderField("Location");
				if (code >= 300 && code < 400 && location != null && !location.equals("") && retries < 2)
				{ // NOTE:  Automatic redirect does not work
					cb.call("redirect", location);
					performRequest(location, auth, method, type, data, cb, retries + 1);
					return;
				}
				InputStream error = ((HttpURLConnection) connection).getErrorStream();
				String statusDescription = WAUtils.inputStreamToString(error, 1024);
				String err = "HttpError " + Integer.toString(code) + ".  " + connection.getResponseMessage();
				if (!statusDescription.startsWith("<!DOCTYPE") && !statusDescription.equals(""))
					err += ".  " + statusDescription;
				cb.call("error", err);
			}
		}
		catch (UnknownHostException ex)
		{
			cb.call("error", "Unknown Host:  " + ex.getMessage());
		}

		catch (Exception ex)
		{
			cb.call("error", "HttpRequestError:  " + ex.getMessage());
		}
	}



	private static byte[] GetMultipartFormData(java.util.HashMap<String, String> queries, java.util.ArrayList<String> files, String boundary) throws FileNotFoundException, IOException
	{
		//	Encoding encoding = Encoding.UTF8;
		ByteArrayOutputStream  formDataStream = new ByteArrayOutputStream ();
		boolean needsCLRF = false;
		String CRLF  = "\r\n";
		byte[] bCRLF = CRLF.getBytes("UTF-8");
		for (java.util.Map.Entry<String, String> param : queries.entrySet())
		{
			if (!param.getValue().equals(""))
			{
				if (needsCLRF)
				{formDataStream.write(bCRLF, 0, bCRLF.length);}
				needsCLRF = true;
				String postData = String.format("--%1$s\r\nContent-Disposition: form-data; name=\"%2$s\"\r\n\r\n%3$s", boundary, param.getKey(), param.getValue());
				byte [] b = postData.getBytes("UTF-8");	formDataStream.write(b, 0, b.length);
			}
		}

		for (String file : files)
		{
			if (needsCLRF)
			{formDataStream.write(bCRLF, 0, bCRLF.length);}
			String header = String.format("--%1$s\r\nContent-Disposition: form-data; name=\"%2$s\"; filename=\"%3$s\"\r\n\r\n", boundary, "file", (new java.io.File(file)).getName());
			byte [] b = header.getBytes("UTF-8");	formDataStream.write(b, 0, b.length);

			try (FileInputStream fileStream = new FileInputStream(file))
			{
				WAUtils.copyStream(fileStream, formDataStream, 4096);
			}
		}

		// Add the end of the request.  Start with a newline
		String footer = "\r\n--" + boundary + "--\r\n";
		byte [] b = footer.getBytes("UTF-8");	formDataStream.write(b, 0, b.length);

		{formDataStream.write(bCRLF, 0, bCRLF.length);}

		// Dump the Stream into a byte[]
		byte[] formData = formDataStream.toByteArray();

		return formData;
	}

	public final void ExecRequest(String serverUrl, String auth, java.util.ArrayList<String> files, java.util.HashMap<String, String> queries)
	{
		ExecRequest(serverUrl, auth, files, queries, null);
	}

	@SuppressWarnings("deprecation")
	public final void ExecRequest(String serverUrl, String auth, java.util.ArrayList<String> files, java.util.HashMap<String, String> queries, WACallback cb)
	{
		try
		{

			switch (WAHttpRequest._method)
			{
			case "get":
			{
				String query = "";
				for (String key : queries.keySet())
				{
					if (!key.equals("image"))
					{
						query += key + "=" + URLEncoder.encode(queries.get(key)) + "&";
					}
				}
				performRequest(serverUrl + "?" + query, auth, "GET", "", null, cb, 0);

			}
			break;
			case "post":
			{
				String formDataBoundary = "----------" + Long.toHexString(System.currentTimeMillis());
				byte[] formData = GetMultipartFormData(queries, files, formDataBoundary);
				performRequest(serverUrl, auth, "POST", "multipart/form-data; boundary=" + formDataBoundary, formData, cb, 0);
			}
			break;
			case "postenc":
			{
				String query = "";
				for (String key : queries.keySet())
				{
					query += key + "=" + URLEncoder.encode(queries.get(key)) + "&";
				}
				byte[] data = query.getBytes();
				performRequest(serverUrl, auth, "POST", "application/x-www-form-urlencoded", data, cb, 0);

			}
			break;
			default:
				String err = "Invalid HTTP method: " + WAHttpRequest._method;
				cb.call("error", err);
				break;
			}
		}
		catch (Exception ex)
		{
			cb.call("error", "ExecRequestError: " + ex.getMessage());
		}
	}

}