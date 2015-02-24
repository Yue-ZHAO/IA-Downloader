This is the 2nd program of sigir2015 for downloading all versions of pages based on URLs extracted by CluewebURLReader.

--Usage--

1. mvn compile

2. mvn clean package -Dmaven.test.skip=true

Skip the test because the AppTest.class is not implemented.


3. java -jar DownloadIA2-0.0.3-SNAPSHOT.jar [Source File Path] [Target Dir] [Start Time] [End Time]

Souce File Path: the absolute path of the file that contains the urls read from the clueweb files

Target Dir: the absolute path of the root directory which contain the download pages (each url will creat the sub dir for itself)

Start Time: Year of the start time. No earlier than 1996

End Time: Year of the end time, like 2012.

Examle: 
java -jar DownloadIA2-0.0.3-SNAPSHOT.jar D:\workspace\DownloadIA2\urls_clueweb12_2 D:\temp\testHtml 1996 2012

--VERSIONS--

0.0.3

1. Change the input from a folder including many clueweb files to a particular url file.
2. Add 1's waiting time between each request. 

0.0.2

1. FileProcess.fileNameTransform 
Use hash method to name the sub dir of each url, which may be good for search in the next step.
Method: I use MD5 to hash the url so that I can make the sub dirs have names with the same length. Besides, I record the MD5 codes in the features with the original URLs, so that I can chech the list to know which url a particular MD5 code represents.
