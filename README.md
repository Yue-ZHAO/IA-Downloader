This code is used for downloading all versions of pages based on URL.

--Usage--

Java -jar DownloadIA2-0.0.1-SNAPSHOT.jar [Source Dir] [Target Dir] [Start Time] [End Time]

Souce Dir: the absolute path of the directory clueweb12-docids-relevant-output

Target Dir: the absolute path of the root directory which contain the download pages (each url will creat the sub dir for itself)

Start Time: Year of the start time. No earlier than 1996

End Time: Year of the end time, like 2012.

--TODO--
1. FileProcess.fileNameTransform 
Use hash method to name the sub dir of each url, which may be good for search in the next step.