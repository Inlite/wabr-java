import com.inlite.wabr.WABarcodeReader;
import com.inlite.wabr.WAUtils;

public class Program
{
	
	public static void main(String[] args)
	{
		// Configure server
		String auth = "";
		String serverUrl = "";
		WABarcodeReader reader = new WABarcodeReader(serverUrl, auth);

		// Configure test
		Test test = new Test();
		reader.diagCallback = test.new DiagCallback(); // Enable display of processing status (TEST only)
		
		/*  To disable specific test set to false. Default: all tests are  enabled
		test.bTestDropBox = false;
		test.bTestBase64 = false;
		test.bTestSamplesLocal = false;
		test.bTestSamplesWeb = false;
		test.bTestUtf8 = false;
		test.bTestUtf8Names = false;
        */

		// Test synchronous API
		test.Run(reader, false);
		
		// Test asynchronous API
		test.Run(reader, true);
	}

}