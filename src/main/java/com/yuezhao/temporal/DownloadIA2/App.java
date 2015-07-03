package com.yuezhao.temporal.DownloadIA2;

import java.io.File;
import java.io.IOException;
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;



/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args ) throws IOException 
    {
        System.out.println( "Hello World!" );
        //	Read args[]
        //	String sourceFolder = args[0];
        String sourceFilePath = args[0];
        String targetFolder = args[1];
        String startTime = args[2];
        String endTime = args[3];
        long sleepMS = Long.parseLong(args[4]);
        int timeoutMS = Integer.parseInt(args[5]);
        //	System.out.println(sourceFolder);
        System.out.println(sourceFilePath);
        System.out.println(targetFolder);
        System.out.println(startTime);
        System.out.println(endTime);
        System.out.println(sleepMS);
        System.out.println(timeoutMS);
        
        //	Read Original URLs from Clueweb Files
        //	List<String> OriginalURLs = FileProcess.readURLFromCluewebFiles(sourceFolder);
        List<String> OriginalURLs = FileProcess.readURLFromURLFile(sourceFilePath);
        
        
        //	Open or create a target directory  
        File targetFolderFile = new File(targetFolder);
        if (!targetFolderFile.exists())
        	targetFolderFile.mkdir();
        
        //	Download pages based on the URLs
        int numRecord = 0;
        int numOriginal = 0;
    	File urlRec = new File(targetFolder, "URLs Record");
    	int errCount = 0;
        for (String OriginalURL: OriginalURLs){
        	//	numOriginal++;
        	// flag: 1:success 0:exist before -1:fail
        	int intFlag = -1;
        	String stringFlag = "NoRecord";        	
        	try {
        		stringFlag = IADownloader.downloadAllVersions(OriginalURL, targetFolder, startTime, endTime, sleepMS, timeoutMS);
        		errCount = 0;
        	} catch (ClientProtocolException e)  {
        		FileProcess.addLinetoaFile("URL: " + OriginalURL + "\n" + "Exception: " + e.getStackTrace().toString(), "ExceptionRecord");
        		System.out.println("Client Protocol Exception!! URL: " + OriginalURL + "\n" + e.getStackTrace().toString());
        		intFlag = -1;
        		errCount++;
        		System.out.println(errCount);
        		if (errCount > 50)
        			return;
        		else
        			continue;
        	} catch (IOException e)  {
        		FileProcess.addLinetoaFile("URL: " + OriginalURL + "\n" + "Exception: " + e.getStackTrace().toString(), "ExceptionRecord");
        		System.out.println("IO Exception!! URL: " + OriginalURL + "\n" + e.getStackTrace().toString());
        		intFlag = -1;
        		errCount++;
        		System.out.println(errCount);
        		if (errCount > 50)
        			return;
        		else
        			continue;
        	} finally {
            	if (!stringFlag.equals("NoRecord") && !stringFlag.equals("Existed")){
            		// gzip the folder and delete the original folder
            		FileProcess.fileToGzip(stringFlag);
            		FileProcess.fileDelete(stringFlag);
            		intFlag = 1;
            		numRecord++;
            		numOriginal++;
            	} else if (stringFlag.equals("Existed")){
            		intFlag = 0;
            		numRecord++;
            		numOriginal++;
            	} else {
            		intFlag = -1;
            		numOriginal++;
            	}
            	FileProcess.addLinetoaFile("URL: " + OriginalURL + "\n" + "Status: " + intFlag + ".\tThe percent of record: " + numRecord + " / " + numOriginal, urlRec.getAbsolutePath());
            	System.out.println("URL: " + OriginalURL);
            	System.out.println("Status: " + intFlag);
            	System.out.println("The percent of record: " + numRecord + " / " + numOriginal);
            	System.out.println();
        	}
        }
        System.out.println( "Finished!" );
    }
}
