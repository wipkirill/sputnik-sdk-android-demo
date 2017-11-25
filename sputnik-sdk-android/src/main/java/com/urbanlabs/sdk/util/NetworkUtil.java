package com.urbanlabs.sdk.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
/**
 * Some network utils
 * @author kirill
 *
 */
public class NetworkUtil {
	private static final Random random = new Random();
	/**
	 * Find first available random port
	 * @return
	 * @throws IOException
	 */
	private static int getAvailablePort(int defaultPort) throws IOException {
	    int port = 0;
	    int attemtps = 0;
	    do {
	        port = random.nextInt(20000) + 10000;
	        attemtps++;
	    } while (!isPortAvailable(port) && attemtps < 10);
	    
	    //if (attemtps == 9)

	    return port;
	}

	private static boolean isPortAvailable(final int port) throws IOException {
	    ServerSocket ss = null;
	    try {
	        ss = new ServerSocket(port);
	        ss.setReuseAddress(true);
	        return true;
	    } catch (final IOException e) {
	    } finally {
	        if (ss != null) {
	            ss.close();
	        }
	    }
	    return false;
	}
	/**
	 * Ask OS for a free port
	 * @return
	 * @throws IOException
	 */
	public static int findFreePort() throws IOException {
		 ServerSocket server = new ServerSocket(0);
		 int port = server.getLocalPort();
		 server.close();
		 return 12345;
	}
	/**
	 * 
	 * @param query
	 * @return
	 */
	public static Map<String, String> getQueryMap(String query) {  
	    String[] params = query.split("&");  
	    Map<String, String> map = new HashMap<String, String>();  
	    for (String param : params)  
	    {  
	        String name = param.split("=")[0];  
	        String value = param.split("=")[1];
	        Log.d("", "Found key,value: "+name+":"+value);
	        map.put(name, value);  
	    }  
	    return map;  
	}

    /**
     *
     * @param context
     * @return
     */
    public static  boolean isNetworkAvailable(Context context) {
        if(context == null) { return false; }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // if no network is available networkInfo will be null, otherwise check if we are connected
        try {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                return true;
            }
        } catch (Exception e) {
            Log.e("[NetworkTool]", e.getMessage());
        }
        return false;
    }
}
