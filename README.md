#Internet Archive Downloader 
=======

This code is used for downloading all versions of pages from Internet Archive based on URLs.

##Usage

1. Compile
```mvn compile```

2. Package
```mvn clean package -Dmaven.test.skip=true```  
Skip the test because the AppTest.class is not implemented.

3. Run
```java -jar DownloadIA2.jar /Url_File_Path /Result_folder [Start Time] [End Time] [Extra Waiting Time]```

Url_File_Path: the absolute path of the file that contains the urls read from the clueweb files

Result_folder: the absolute path of the root directory which contain the download pages (each url will creat a sub dir for itself)

Start Time: Year of the start time. No earlier than 1996

End Time: Year of the end time, like 2012.

Extra Waiting Time: Milliseconds between downloading 2 pages.

##Examle 
```Java -jar DownloadIA2.jar urls_clueweb12_2 /Result 1996 2012 500```

##TODO

1. Need I to consider about the conflict of MD5? I am not sure.

##Update

###0.0.4
1. Add more output on screen.  
2. Add a parameter to set the extra waiting time (n milliseconds) between downloading 2 pages.  
3. If the particular historical pages have been downloaded before, the program will skip them.


###0.0.3

1. Change the input from a folder including many clueweb files to a particular url file.  
2. Add 1's waiting time between each request.

###0.0.2

1. FileProcess.fileNameTransform  
Use hash method to name the sub dir of each url, which may be good for search in the next step.
Method: I use MD5 to hash the url so that I can make the sub dirs have names with the same length. Besides, I record the MD5 codes in the features with the original URLs, so that I can chech the list to know which url a particular MD5 code represents.
