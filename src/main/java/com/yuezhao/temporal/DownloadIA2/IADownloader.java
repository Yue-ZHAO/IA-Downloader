package com.yuezhao.temporal.DownloadIA2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

public class IADownloader {
	
	
	/**
	 * 
	 * @param inputURL
	 * @return JSONArray urlList
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	public static JSONArray wayback(String inputURL, String startTime, String endTime) {
		String cdxURL = "http://web.archive.org/cdx/search/cdx?url=";
		String originURL = inputURL;
		JSONArray urlList = null;		
		//	Add some figures about our requests
		String filterURL = "&from=" + startTime.trim() + "&to=" + endTime.trim() + "&fl=timestamp,original,digest&output=json";
		String formatURL = cdxURL + originURL + filterURL;
		//	format URL
		URL url = null;
		URI uri = null;
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		try{		
			url = new URL(formatURL);
			String nullFragment = null;
			uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), nullFragment);
			//	System.out.println("URI " + uri.toString() + " is OK");
			//	Use http Get to get feedback from Internet Archive
			httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(uri);
			httpResponse = httpClient.execute(httpGet);
		    HttpEntity httpEntity = httpResponse.getEntity();	    
		    InputStream inSm = httpEntity.getContent();  
	        Scanner inScn = new Scanner(inSm);
	        String responseString = "";
	        while (inScn.hasNextLine()) {  
	        	responseString = responseString + inScn.nextLine();	        	
	        }
	        inScn.close();	        
	        //	If the response is error information, or no information
	        //	Change it to 0 content.
	        if (!responseString.startsWith("["))
	        	responseString = "[]";
	        urlList = new JSONArray(responseString);		    
		    EntityUtils.consume(httpEntity);			
		} catch (Exception e) {
			FileProcess.addLinetoaFile("URL: " + inputURL + "\n" + "Exception: " + e, "ExceptionRecord");
		} finally {
			try {
				httpResponse.close();
				httpClient.close();
			} catch(Exception e) {
				FileProcess.addLinetoaFile("URL: " + inputURL + "\n" + "Exception: " + e, "ExceptionRecord");
			}
		}
		return urlList;
	}
	
	
	public static List<String> generateIAurls(JSONArray feedback){
		String preURL = "https://web.archive.org/web/";
		List<String> urls = new ArrayList<String>();
		
		if (feedback.length() < 1 || feedback == null)
			return null;
		
		// 	For the JSON array we get from Internet Archive
		// 	The first element is the field name of the later data.
		// 	So we use it to initialize our indexes
		int index_Timestamp = 0;
		int index_Original = 0;
		int index_Digest = 0;
		for(int i=0; i<feedback.getJSONArray(0).length(); i++){
			String temp = feedback.getJSONArray(0).get(i).toString();
			if (temp.equals("timestamp")){
				index_Timestamp = i;
			} else if (temp.equals("original")){
				index_Original = i;
			} else if (temp.equals("digest")){
				index_Digest = i;
			}
		}
		
		// 	For the rest elements of JSON array
		//	If the current element has different content (digest) with the former one
		//	We transform them into URLs which we will use to get content from Internet Archive
		//	And put them in the list called urls
		String tempDigest = "";
		for(int j=1; j<feedback.length(); j++){
			String currentTimestamp = feedback.getJSONArray(j).get(index_Timestamp).toString();
			String currentOriginal = feedback.getJSONArray(j).get(index_Original).toString();
			String currentDigest = feedback.getJSONArray(j).get(index_Digest).toString();
			
			if (!currentDigest.equals(tempDigest)){
				String tempURL = preURL + currentTimestamp + "/" + currentOriginal;
				urls.add(tempURL);
			}
			tempDigest = currentDigest;
		}				
		return urls;		
	}
	
	public static int downloadPages(List<String> urls, String subTargetFolder, long sleepMS) {
		int i = 0;
		for (String url: urls){
			
			//	If the file has been Downloaded, skip to the next one.
			String fileName = url.substring(28, 40);
		    //	Now it runs well, but I am not sure is the path like subTargetFolder + fileName + ".html" is good enough.
		    fileName = fileName + ".html";
		    File file = new File(subTargetFolder, fileName);
		    if (file.exists()) {
		    	System.out.println("Historical version: " + url.substring(28, 40) + " exist. ");
				i++;
				System.out.println(i + "/" + urls.size() + " Completed.");
		    	continue;
		    }
		    
		    long begintime = System.currentTimeMillis();		    
		    //	format URL
			URL url1 = null;
			URI uri = null;
			try {
				url1 = new URL(url);
				// System.out.println(url1);
				String nullFragment = null;
				uri = new URI(url1.getProtocol(), url1.getHost(), url1.getPath(), url1.getQuery(), nullFragment);
				// URI uri = new URI(url1.getProtocol(), url1.getUserInfo(), url1.getHost(), url1.getPort(), url1.getPath(), url1.getQuery(), url1.getRef());
				// System.out.println(uri);
			} catch (Exception e) {
				FileProcess.addLinetoaFile("URL: " + url + "\n" + "Exception: " + e, "ExceptionRecord");
				return -1;
			}
			//	http Client
			CloseableHttpClient httpClient = HttpClients.createDefault();			
			//CloseableHttpClient httpClient = HttpClients.custom().setRedirectStrategy(urlSanitizeRedirectStrategy);
			RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
			//RequestConfig config = RequestConfig.custom().setRedirectsEnabled(false).setCircularRedirectsAllowed(true).build();

			//	httpGet.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; QQDownload 1.7; .NET CLR 1.1.4322; CIBA; .NET CLR 2.0.50727)");
			CloseableHttpResponse httpResponse = null;			
			//	int statusCode = httpResponse.getStatusLine().getStatusCode();
			try {
				HttpGet httpGet = new HttpGet(uri);
				httpGet.setConfig(config);				
				httpResponse = httpClient.execute(httpGet);
				HttpEntity httpEntity = httpResponse.getEntity();
			    InputStream inSm = httpEntity.getContent(); 
			    // 	Read the input stream of the entity
			    //	Transfer it in to the file with html format			    
			    // String htmlFilePath = subTargetFolder + fileName + ".html";
			   	BufferedInputStream bis = new BufferedInputStream(inSm);
			   	// BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(htmlFilePath)));
			   	BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			   	int inByte;
			   	while((inByte = bis.read()) != -1) 
			   		bos.write(inByte);
			    bis.close();
			    bos.close();
				System.out.print("Historical version: " + url.substring(28, 40) + ". ");
				long endtime = System.currentTimeMillis();
				System.out.print("Time cost is: " + (endtime - begintime) + ". ");
				i++;
				System.out.println(i + "/" + urls.size() + " Completed.");
			} catch(Exception e) {
				FileProcess.addLinetoaFile("URL: " + url + "\n" + "Exception: " + e, "ExceptionRecord");
				return -1;
			} finally {
				try {
					httpResponse.close();
					TimeUnit.MILLISECONDS.sleep(sleepMS);
				} catch (Exception e) {
					FileProcess.addLinetoaFile("URL: " + url + "\n" + "Exception: " + e, "ExceptionRecord");
					return -1;
				}
			}
			try {
				httpClient.close();
			} catch (Exception e) {
				FileProcess.addLinetoaFile("URL: " + url + "\n" + "Exception: " + e, "ExceptionRecord");
				return -1;
			}
		}
		return 1;
	}
	
	public static int downloadAllVersions(String originalURL,
			String targetFolder, String startTime, String endTime, long sleepMS) {
		// 	Use wayback machine get feedback from IA
		JSONArray feedback = null;
		try {
			feedback = wayback(originalURL, startTime, endTime);
		} catch (Exception e) {
			FileProcess.addLinetoaFile("URL: " + originalURL + "\n" + "Exception: " + e, "ExceptionRecord");
			return -1;
		}
		
		//	If there is no feedback, return
		if (feedback.length() < 1 || feedback == null) {
			return -1;
		} else {
			System.out.println("Downloading the historical pages of " + originalURL);
			//	Generate urls belongs to IA based on the feedback
			List<String> urlsIA = generateIAurls(feedback);
			
			//	Generate the sub-folder of this url in the target folder
			File subTargetFolder = null;
			
			try {
				subTargetFolder = FileProcess.generateSubFolder(originalURL, targetFolder);
				if (subTargetFolder == null) {
					System.out.println("Historical File existed: " + originalURL);
					return 0;
				}
				String subTargetFolderPath = subTargetFolder.getAbsolutePath();
				//	Download Pages from IA based on the URLs we generate.
				int flag = downloadPages(urlsIA, subTargetFolderPath, sleepMS);
				if (flag == -1)
					return -1;
			} catch (Exception e) {
				System.out.println("Exception: " + e);
				FileProcess.addLinetoaFile("URL: " + originalURL + "\n" + "Exception: " + e, "ExceptionRecord");
				return -1;
			}
			
			System.out.println("Downloaded ALL: " + originalURL);
			//	Write Down the features of the URL
			writeDownFeatures(subTargetFolder.getName(), originalURL, feedback.length(), urlsIA.size(), feedback, targetFolder);
			return 1;
		}
	}


	private static void writeDownFeatures(String folderName, String originalURL, int length,
			int size, JSONArray feedback, String targetFolder) {
		// 	For the JSON array we get from Internet Archive
		// 	The first element is the field name of the later data.
		// 	So we use it to initialize our indexes
		int index_Timestamp = 0;

		for(int i=0; i<feedback.getJSONArray(0).length(); i++){
			String temp = feedback.getJSONArray(0).get(i).toString();
			if (temp.equals("timestamp")){
				index_Timestamp = i;
			} 
		}		
		
		// Generate features
		String features = 
				folderName + ", " + 
				length + ", " + 
				size + ", " + 
				feedback.getJSONArray(1).get(index_Timestamp).toString() + ", " + 
				feedback.getJSONArray(length-1).get(index_Timestamp).toString() + ", " + 
				originalURL;
		
		//	Write Features to the targetFolder
		File featureFile = new File(targetFolder, "Features");
		FileProcess.addLinetoaFile(features, featureFile.getAbsolutePath());
	}
	
	


	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
