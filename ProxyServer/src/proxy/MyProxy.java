package proxy;

import java.net.*;
import java.io.*;
import java.util.*;

public class MyProxy extends PrivacyProxy {

    //////////////////////////////////////////////////////////////////////////
    //
    // Enhance your proxy by implementing the following three methods:
    //   - onRequest
    //   - onResponse
    //
    //////////////////////////////////////////////////////////////////////////

    protected HashMap<String, String> onRequest(HashMap<String, String> requestHeaders, String url){
    	List<String> whitelist = new ArrayList<String>();
    	whitelist.add("wt_nbg_Q3=!H3jcRr7//qTSwyoV5Le6jgcG+zHt1KlXtOX4m8dx4UlmFfq8A5inhKw1jYtDibfAaa6Pb5cx9G6b");
    	whitelist.add("TnetID=0LgosADSZ1O78rL768MMNwZMv6OsGaG2");
    	whitelist.add("tc=1454592152%2C1454592076");
    	whitelist.add("pl=2017%3A0");
    	whitelist.add("wt3_eid=%3B318816705845986%7C2145459208300290778%232145459215700523701");
    	whitelist.add("wt3_sid=%3B318816705845986");
    	
    // This is the onRequest handler.
    // It will be executed whenever an HTTP request passes by.
    // Its arguments are a so-called HashMap containg all headers from the request, and a simple String containing the requested URL.
    // You can put code here to print the request headers, and to modify them.
    	if(requestHeaders.containsKey("Cookie") ) {
    		String[] cookies = requestHeaders.get("Cookie").split("; ");
    		for (String cookie : cookies) {
    			if (!whitelist.contains(cookie)) {
    				requestHeaders.remove("Cookie");
    			}
    		}
    	}
    	
    	if(requestHeaders.containsKey("Referer")) {
    		requestHeaders.put("Referer", "Your mom");
    	}
    	
    	if(requestHeaders.containsKey("User-Agent")) {
        	requestHeaders.put("User-Agent", "Nope Nope TryAgainBro Nope");
        } 
    	
    	if(requestHeaders.containsKey("Accept-Encoding")) {
        	requestHeaders.remove("Accept-Encoding");
        }
    	
        // let's simply print the requested URL, for a start that's enough:
        log("Request for: " + url);

        // if we want to print all the request headers , use the below code:
        // it does a for-loop over all headers
        if(url.contains("lib.min.js") || url.contains("google-analytics") || url.contains("googletagservices") || url.contains("googlesyndication") || url.contains("clients1.google")) {
        	return null;
        }
        
        if(url.contains("yolo.js") || url.contains("spy")) {
        	return null;
        }
       

        for (String header : requestHeaders.keySet()) {
        	Scanner reader = new Scanner(requestHeaders.get(header));
            // within the for loop, the variable  header  contains the name of the header
            // and you can ask for the contents of that header using requestHeaders.get() .
            log("  REQ: " + header + ": " + requestHeaders.get(header));
        }
        
        // example code to do something if a certain requestheader is present:
/*
        if (requestHeaders.containsKey("MyHeader")) ........
*/

        // example code to remove the  Creepyness  header:
/*
        requestHeaders.remove("Creepyness");
*/

        // example code to insert (or replace) the  Niceness  header:
/*
        requestHeaders.put("Niceness","high");
*/
        // return the (manipulated) headers, or
        return requestHeaders;

        // alternatively, drop this request by returning null
        // return null;
    }


    protected byte[] onResponse(byte[] originalBytes, String httpresponse){
    // This is the onResponse handler.
    // It will be executed whenever an HTTP reply passes by.
    // Its arguments are the entire HTTP response (both headers and data) as a byte array, and the line containing the response code.
    // For your convenience, the response headers are also available as a HashMap called responseHeaders , but you can't modify it.
        log("Response: "+httpresponse);

        // if you want to (safely, i.e., without binary garbage) print the entire response, uncomment the following:

        //printSafe(originalBytes);
        //log(new String(originalBytes));
        
        printSafe(originalBytes);        
        // if you want to modify the response, you can either modify the byte array directly,
        // or first convert it to a string and then modify that, _if_ you know for sure the response is in text form
        // (otherwise, a string doesn't make sense).

        if (responseHeaders.containsKey("Content-Type") && responseHeaders.get("Content-Type").startsWith("text/html")) {
             String s = new String(originalBytes);
             //String s2 = s.replaceAll("headers", " // if you want to (safely, i.e., without binary garbage) print the entire response, uncomment the following: // printSafe(originalBytes); // if you want to modify the response, you can either modify the byte array directly, // or first convert it to a string and then modify that, _if_ you know for sure the response is in text form // (otherwise, a string doesn't make sense). jaja");
             String s2 = s.replaceAll("<div id=\"ad.*</div>", "");
             String s3 = s2.replaceAll("<g:plusone></g:plusone>", "");
             String s4 = s3.replaceAll("http://www.facebook.com/plugins/like.php?","");
             String s5 = s4.replaceAll("navigator", "");
             byte [] alteredBytes = s5.getBytes();
             return alteredBytes;
        }

        // return the original, unmodified array:
        return originalBytes;
    }

    
    // Constructor, no need to touch this
    public MyProxy(Socket socket, Boolean autoFlush) {
        super(socket, autoFlush);
    }
}
