package net.skup.swifty;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import net.skup.swifty.model.Pun;
import android.os.AsyncTask;

/*
 * Strict Mode produces Network On Main exception w/out this permit all
 * StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
 * StrictMode.setThreadPolicy(policy); 
 *
 */
public class DownloadFilesTask extends AsyncTask<String, Void, String> {
	
	// proxy callback
	public static interface Downloader {
		public void setData(String result);
	}
	
	private Downloader host = null;
	
    public DownloadFilesTask(Downloader host) {
    	this.host = host;
    }

    //DownloadManager API is for heavy work
    protected String doInBackground(String... urls) {
        int count = urls.length;
        String rv = null;
        for (int i = 0; i < count; i++) {
        	InputStream web_is = getDataWithURL("http://tom-swifty.appspot.com/challenges.json");
            rv = Pun.convertToString(web_is);
            // Escape early if cancel() is called
            if (isCancelled()) break;
        }
        return rv;
    }
    
    @Override
    protected void onPostExecute(String result) {
    	host.setData(result);
    }
    
 	/**
 	 * Open a URL.
 	 * http://www.vogella.com/articles/AndroidNetworking/article.html
 	 * see http://androidsnippets.com/executing-a-http-post-request-with-httpclient
 	 */
 	private InputStream getDataWithURL(String u) {
 		InputStream is = null;
 		HttpURLConnection con = null;

 		try {
 			URL url = new URL(u);
 			con = (HttpURLConnection) url.openConnection();
 			is = con.getInputStream();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 		return is;
 	}
}