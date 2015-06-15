package com.yuezhao.temporal.DownloadIA2;

import java.io.File;
import java.io.IOException;
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.security.NoSuchAlgorithmException;
import java.util.List;

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
        //	System.out.println(sourceFolder);
        System.out.println(sourceFilePath);
        System.out.println(targetFolder);
        System.out.println(startTime);
        System.out.println(endTime);
        
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
        for (String OriginalURL: OriginalURLs){
        	//	numOriginal++;
        	// flag: 1:success 0:exist before -1:fail
        	int flag = -1;
        	try {
        		flag = IADownloader.downloadAllVersions(OriginalURL, targetFolder, startTime, endTime, sleepMS);
        	} catch (Exception e)  {
        		FileProcess.addLinetoaFile("URL: " + OriginalURL + "\n" + "Exception: " + e.toString(), "ExceptionRecord");
        		System.out.println("Exception!! URL: " + OriginalURL + "\n" + e.toString());
        		flag = -1;
        		continue;
        	} finally {
            	if (flag == 1 || flag == 0){
            		numRecord++;
            		numOriginal++;
            	} else if (flag == -1) {
            		numOriginal++;
            	}
            	FileProcess.addLinetoaFile("URL: " + OriginalURL + "\n" + "Status: " + flag + ".\tThe percent of record: " + numRecord + " / " + numOriginal, urlRec.getAbsolutePath());
            	System.out.println("URL: " + OriginalURL);
            	System.out.println("Status: " + flag);
            	System.out.println("The percent of record: " + numRecord + " / " + numOriginal);
            	System.out.println();
        	}
        }
        System.out.println( "Finished!" );
    }
}
