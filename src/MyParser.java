/* CS144
 *
 * Parser skeleton for processing item-???.xml files. Must be compiled in
 * JDK 1.5 or above.
 *
 * Instructions:
 *
 * This program processes all files passed on the command line (to parse
 * an entire diectory, type "java MyParser myFiles/*.xml" at the shell).
 *
 * At the point noted below, an individual XML file has been parsed into a
 * DOM Document node. You should fill in code to process the node. Java's
 * interface for the Document Object Model (DOM) is in package
 * org.w3c.dom. The documentation is available online at
 *
 * http://java.sun.com/j2se/1.5.0/docs/api/index.html
 *
 * A tutorial of Java's XML Parsing can be found at:
 *
 * http://java.sun.com/webservices/jaxp/
 *
 * Some auxiliary methods have been written for you. You may find them
 * useful.
 */

import java.io.*;
import java.text.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;


class MyParser {
	static final String[] table = {
	    "Item", 
	    "ItemCategory", 
	    "ItemBid", 
	    "Bidder",
	    "Seller",
	};
    
    static final String columnSeparator = "|*|";
    static DocumentBuilder builder;
    static PrintWriter[] writers = new PrintWriter[5];
    
    static final String[] typeName = {
	"none",
	"Element",
	"Attr",
	"Text",
	"CDATA",
	"EntityRef",
	"Entity",
	"ProcInstr",
	"Comment",
	"Document",
	"DocType",
	"DocFragment",
	"Notation",
    };
    
    static class MyErrorHandler implements ErrorHandler {
        
        public void warning(SAXParseException exception)
        throws SAXException {
            fatalError(exception);
        }
        
        public void error(SAXParseException exception)
        throws SAXException {
            fatalError(exception);
        }
        
        public void fatalError(SAXParseException exception)
        throws SAXException {
            exception.printStackTrace();
            System.out.println("There should be no errors " +
                               "in the supplied XML files.");
            System.exit(3);
        }
        
    }
    
    /* Non-recursive (NR) version of Node.getElementsByTagName(...)
     */
    static Element[] getElementsByTagNameNR(Element root, String tagName) {
        Vector< Element > elements = new Vector< Element >();
        Node child = root.getFirstChild();
        while (child != null) {
            if (child instanceof Element && child.getNodeName().equals(tagName))
            {
                elements.add( (Element)child );
            }
            child = child.getNextSibling();
        }
        Element[] result = new Element[elements.size()];
        elements.copyInto(result);
        return result;
    }
    
    /* Returns the first subelement of e matching the given tagName, or
     * null if one does not exist. NR means Non-Recursive.
     */
    static Element getElementByTagNameNR(Element n, String tagName) {
        Node child = n.getFirstChild();
        while (child != null) {
            if (child instanceof Element && child.getNodeName().equals(tagName))
                return (Element) child;
            child = child.getNextSibling();
        }
        return null;
    }
    
    /* Returns the text associated with the given element (which must have
     * type #PCDATA) as child, or "" if it contains no text.
     */
    static String getElementText(Element n) {
        if (n.getChildNodes().getLength() == 1) {
            Text elementText = (Text) n.getFirstChild();
            return elementText.getNodeValue();
        }
        else
            return "";
    }
    
    /* Returns the text (#PCDATA) associated with the first subelement X
     * of e with the given tagName. If no such X exists or X contains no
     * text, "" is returned. NR means Non-Recursive.
     */
    static String getElementTextByTagNameNR(Element n, String tagName) {
        Element elem = getElementByTagNameNR(n, tagName);
        if (elem != null)
            return getElementText(elem);
        else
            return "";
    }
    

    /* Returns the amount (in XXXXX.xx format) denoted by a money-string
     * like $3,453.23. Returns the input if the input is an empty string.
     */
    static String strip(String money) {
        if (money.equals(""))
            return money;
        else {
            double am = 0.0;
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
            try { am = nf.parse(money).doubleValue(); }
            catch (ParseException e) {
                System.out.println("This method should work for all " +
                                   "money values you find in our data.");
                System.exit(20);
            }
            nf.setGroupingUsed(false);
            return nf.format(am).substring(1);
        }
    }
    
    /* Process one items-???.xml file.
     */
    static void processFile(File xmlFile) throws ParseException {
        Document doc = null;
        try {
            doc = builder.parse(xmlFile);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(3);
        }
        catch (SAXException e) {
            System.out.println("Parsing error on file " + xmlFile);
            System.out.println("  (not supposed to happen with supplied XML files)");
            e.printStackTrace();
            System.exit(3);
        }
        
        /* At this point 'doc' contains a DOM representation of an 'Items' XML
         * file. Use doc.getDocumentElement() to get the root Element. */
        Element root= doc.getDocumentElement();
        Element[] nlist = getElementsByTagNameNR(root,"Item");        
        for(int i=0; i<nlist.length; i++) {
            Attr itemID = nlist[i].getAttributeNode("ItemID");
            String id = itemID.getValue();
            extractData(nlist[i],id);
        }
    }
    
    public static void extractData(Element n, String id) throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    	SimpleDateFormat s = new SimpleDateFormat("MMM-dd-yy hh:mm:ss");
    	Element name = getElementByTagNameNR(n,"Name");
    	Element[] categories = getElementsByTagNameNR(n, "Category");
    	Element currently = getElementByTagNameNR(n, "Currently");
    	Element buy_Price = getElementByTagNameNR(n, "Buy_Price");
    	Element first_Bid = getElementByTagNameNR(n, "First_Bid");
    	Element number_of_Bids = getElementByTagNameNR(n, "Number_of_Bids");
    	Element bids = getElementByTagNameNR(n, "Bids");
    	Element[] bidlist = getElementsByTagNameNR(bids, "Bid");
    	Element location = getElementByTagNameNR(n, "Location");
    	Element country = getElementByTagNameNR(n, "Country");
    	Element started = getElementByTagNameNR(n, "Started");
    	Element ends = getElementByTagNameNR(n, "Ends");
    	Element seller = getElementByTagNameNR(n, "Seller");
    	Element description = getElementByTagNameNR(n, "Description");
    	
    	Date startDate = s.parse(getElementText(started));
    	Date endDate = s.parse(getElementText(ends));
    	
    	//write Item table
    	writers[0].print(id);
    	writers[0].print(columnSeparator);
    	writers[0].print(getElementText(name));
    	writers[0].print(columnSeparator);
    	writers[0].print(strip(getElementText(currently)));
    	writers[0].print(columnSeparator);    	
    	if(buy_Price != null)
    		writers[0].print(strip(getElementText(buy_Price)));
    	writers[0].print(columnSeparator);
    	writers[0].print(strip(getElementText(first_Bid)));
    	writers[0].print(columnSeparator);
    	writers[0].print(getElementText(number_of_Bids));
    	writers[0].print(columnSeparator);
    	
    	//check location
    	writers[0].print(getElementText(location));
    	writers[0].print(columnSeparator);
    	Element latitude = getElementByTagNameNR(location,"Latitude");
    	Element logitude = getElementByTagNameNR(location,"Longitude");
    	if(latitude != null)
    		writers[0].print(getElementText(latitude));
    	writers[0].print(columnSeparator);
    	
    	if(logitude != null)
    		writers[0].print(getElementText(logitude));
    	writers[0].print(columnSeparator);
    	
    	writers[0].print(getElementText(country));
    	writers[0].print(columnSeparator);
    	writers[0].print(sdf.format(startDate));
    	writers[0].print(columnSeparator);
    	writers[0].print(sdf.format(endDate));
    	writers[0].print(columnSeparator);
    	
    	//check seller
    	Attr sellerID = seller.getAttributeNode("UserID");
    	writers[0].print(sellerID.getValue());
    	writers[0].print(columnSeparator);
    	
    	writers[0].print(getElementText(description));
    	writers[0].print("\n");
    	
    	//write ItemCategory table
    	for(int i = 0; i < categories.length; i++)
    	{
    		writers[1].print(id);
    		writers[1].print(columnSeparator);
    		writers[1].print(getElementText(categories[i]));
    		writers[1].print("\n");
    	}
    	
    	//write ItemBid table & Bidder table
    	for(int i = 0; i < bidlist.length; i++)
    	{
    		Element amount = getElementByTagNameNR(bidlist[i],"Amount");
    		Element time = getElementByTagNameNR(bidlist[i],"Time");
    		Date timeDate = s.parse(getElementText(time));
    		Element bidder = getElementByTagNameNR(bidlist[i],"Bidder");
    		Attr bidderID = bidder.getAttributeNode("UserID");
    		Attr bidderRating = bidder.getAttributeNode("Rating");
    		Element bidderLoc = getElementByTagNameNR(bidder,"Location");
    		Element bidderCoun = getElementByTagNameNR(bidder,"Country");
    		
    		//ItemBid table
    		writers[2].print(id);
    		writers[2].print(columnSeparator);
    		writers[2].print(bidderID.getValue());
    		writers[2].print(columnSeparator);
    		writers[2].print(sdf.format(timeDate));
    		writers[2].print(columnSeparator);
    		writers[2].print(getElementText(amount));
    		writers[2].print(columnSeparator);
    		writers[2].print("\n");
    		
    		//Bidder table
    		writers[3].print(bidderID.getValue());
    		writers[3].print(columnSeparator);
    		writers[3].print(bidderRating.getValue());
    		writers[3].print(columnSeparator);
    		if(bidderLoc != null)
    			writers[3].print(getElementText(bidderLoc));
    		writers[3].print(columnSeparator);
    		
    		if(bidderCoun != null)
    			writers[3].print(getElementText(bidderCoun));
    		writers[3].print("\n");
    		
    	}
    	
    	//write Seller Table
    	Attr sellerRating = seller.getAttributeNode("Rating");
    	writers[4].print(sellerID.getValue());
    	writers[4].print(columnSeparator);
    	writers[4].print(sellerRating.getValue());
    	writers[4].print("\n");
    	
    }
    
    public static void main (String[] args) throws IOException, ParseException {

        /* Initialize parser. */
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringElementContentWhitespace(true);      
            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new MyErrorHandler());
        }
        catch (FactoryConfigurationError e) {
            System.out.println("unable to get a document builder factory");
            System.exit(2);
        } 
        catch (ParserConfigurationException e) {
            System.out.println("parser was unable to be configured");
            System.exit(2);
        }
        
        /* Create writer for all the output files */
        for(int i = 0; i < table.length; i++)
        {
        	String fileName = table[i] + ".del";
        	try {
      	      File file = new File(fileName);
      	      
      	      if (file.createNewFile()){
      	    	writers[i] = new PrintWriter(fileName);
      	      }else{
      	        System.out.println("File already exists.");
      	    	writers[i] = new PrintWriter(fileName);
      	      }
      	      
          	} catch (IOException e) {
      	      e.printStackTrace();
          	}        	
        }
      File currentFile = new File("items-0.xml");
      processFile(currentFile);

        for(int i = 0; i < table.length; i++)
        {
        	writers[i].close();
        }
        System.out.println("finished");
        
    }
}
