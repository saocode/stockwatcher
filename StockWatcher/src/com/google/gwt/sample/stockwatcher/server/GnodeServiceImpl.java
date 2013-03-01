package com.google.gwt.sample.stockwatcher.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gwt.sample.stockwatcher.client.GnodeService;
import com.google.gwt.sample.stockwatcher.client.NotLoggedInException;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class GnodeServiceImpl extends RemoteServiceServlet implements
GnodeService {

	private static final Logger LOG = Logger.getLogger(GnodeServiceImpl.class.getName());
	private static final PersistenceManagerFactory PMF =
			JDOHelper.getPersistenceManagerFactory("transactions-optional");

	public void addGnode(String symbol) {
		//checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.makePersistent(new Gnode("xxx"));
		} finally {
			pm.close();
		}
	}

	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}

	public void parseVnodes() { // fileName not used - parsed from URL now

		URL url = null;
		try {
			url = new URL("http://data.vancouver.ca/download/kml/graffiti.kmz");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			InputStream inStream = url.openStream();
			ZipInputStream zis = new ZipInputStream(inStream);

			// this advances stream to next file (doesn't show first file in while loop now)
			zis.getNextEntry();

			// this would go through each file in the zip stream and show its name and size
			//		ZipEntry entry;
			//		// while there are entries I process them
			//		while ((entry = zis.getNextEntry()) != null) {
			//			System.out.println("entry: " + entry.getName() + ", "
			//					+ entry.getSize());
			//			// consume all the data from this entry
			//			while (zis.available() > 0)
			//				zis.read();
			//			// I could close the entry, but getNextEntry does it
			//			// automatically
			//			// zis.closeEntry()
			//		}

			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(zis);
				Element docEle = doc.getDocumentElement();

				// Print root element of the document
				System.out.println("Root element of the document: "
						+ docEle.getNodeName());

				// Assign Placemarks to node list
				NodeList nodes = docEle
						.getElementsByTagName("Placemark");

				// Print total "Placemarks" in document
				System.out.println("Total nodes: "
						+ nodes.getLength());

				if (nodes != null && nodes.getLength() > 0) {
					for (int i = 0; i < nodes.getLength(); i++) {

						Node node = nodes.item(i);

						if (node.getNodeType() == Node.ELEMENT_NODE) {

							System.out.println("=====================");

							Element e = (Element) node;
							NodeList nodeList = e
									.getElementsByTagName("coordinates");
							System.out.println("Location: "
									+ nodeList.item(0).getChildNodes()
									.item(0).getNodeValue());
							String s = nodeList.item(0).getChildNodes()
									.item(0).getNodeValue();
							Window.alert("Gnode should be added: " + s);

						}

					}
				}
				//}  commented out "if file exists" for URL parse

			} catch (Exception e) {
				System.out.println(e);
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
