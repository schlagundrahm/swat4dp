package ch.srsx.swat.datapower.tools.ant.taskdefs;

/** 
 * coproc2 
 * - send stylesheet and xmlfile to a DataPower appliance;
 * - display DataPower result of applying stylesheet to xmlfile.
 *
 * Modification of SOAPClient4XG from this article:
 * http://www.ibm.com/developerworks/xml/library/x-soapcl/
 * 
 * Added sending of base64(gzip(stylesheet2Send)) as HTTP header. 
 *
 * @author  Hermann Stamm-Wilbrandt
 * @version 1.0
 * @param   stylesheet2Send  stylesheet to be executed on DataPower
 * @param   xmlFile2Send     xmlfile sent to DataPower
 * @param   url              URL of coproc2 Endpoint on DataPower
 */

import java.io.*;
import java.net.*;
import java.util.zip.*;

import org.apache.commons.codec.binary.Base64;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


public class Coproc extends Task {

	private File stylesheet;
	private File xmlFile;
	private String serviceEndpoint;

	public void setStylesheet(File stylesheet) {
		this.stylesheet = stylesheet;
	}

	public void setXmlFile(File xmlFile) {
		this.xmlFile = xmlFile;
	}

	public void setServiceEndpoint(String serviceEndpoint) {
		this.serviceEndpoint = serviceEndpoint;
	}

	@Override
	public void execute() throws BuildException {

		// Create the connection where we're going to send the file.
		URL url = null;
		try {
			url = new URL(serviceEndpoint);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		URLConnection connection = null;
		try {
			connection = url.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpURLConnection httpConn = (HttpURLConnection) connection;

		// Open the input file. After we copy it to a byte array, we can see
		// how big it is so that we can set the HTTP Cotent-Length
		// property. (See complete e-mail below for more on this.)

		FileInputStream fin = null;
		try {
			fin = new FileInputStream(xmlFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		// Copy the SOAP file to the open connection.
		try {
			copy(fin, bout);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fin.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] b = bout.toByteArray();

		// Read stylesheet2send into b2[]
		InputStream fin2 = null;
		try {
			fin2 = new FileInputStream(stylesheet);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ByteArrayOutputStream bout2 = new ByteArrayOutputStream();
		try {
			copy(fin2, bout2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fin2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte b2[] = bout2.toByteArray();

		// B[] = gzip(b2[])
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			GZIPOutputStream gz = new GZIPOutputStream(bos);
			gz.write(b2);
			gz.close();
			bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] B = bos.toByteArray();
		
		String strb64 = new String(Base64.encodeBase64(B));

		// Set the appropriate HTTP parameters.
		httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
		httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		httpConn.setRequestProperty("xsl", strb64);
		try {
			httpConn.setRequestMethod("POST");
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		httpConn.setDoOutput(true);
		httpConn.setDoInput(true);

		// Everything's set up; send the XML that was read in to b.
		try {
			OutputStream out = httpConn.getOutputStream();
			out.write(b);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Read the response and write it to standard out.

		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(httpConn.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader in = new BufferedReader(isr);

		String inputLine;

		try {
			while ((inputLine = in.readLine()) != null)
				System.out.println(inputLine);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// copy method from From E.R. Harold's book "Java I/O"
	public static void copy(InputStream in, OutputStream out) throws IOException {

		// do not allow other threads to read from the
		// input or write to the output while copying is
		// taking place

		synchronized (in) {
			synchronized (out) {

				byte[] buffer = new byte[256];
				while (true) {
					int bytesRead = in.read(buffer);
					if (bytesRead == -1)
						break;
					out.write(buffer, 0, bytesRead);
				}
			}
		}
	}
}
