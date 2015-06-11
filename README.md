#### Scraper Project
* Create 2 threads with different configurations (shift)
* Each thread will do the same processing (scraping, saving to csv) at their configured shift
* Developed using only Java 7. No third party libs except log4j2.

#### System requirements
* java SE 1.7.0_79 - This version will fix the timezone problem for UTC+3 where the date is reflecting as UTC+3 plus 1.
* maven 3.0.4 or higher

#### Bottlenecks encountered while developing this project. Took me whole day to fix this :(
* UTC+3 is incorrect on jdk 1.7.0_51
* Encounterd java.lang.UnsupportedClassVersionError: Bad version number in .class file when running the scraper-1.0-SNAPSHOT.jar using bash. 
  Running scraper-1.0-SNAPSHOT.jar using cmd prompt on Windows has no problem at all.

#### Deployment
* Build the project using maven | Command: mvn clean install
* Untar scraper-1.0-SNAPSHOT.tar.gz | Command: tar xzvf scraper-1.0-SNAPSHOT.tar.gz 
  (I do this using git bash. Not sure on how to do on cmd prompt. Haven't tried before.)
  This will untar the following folders and files:
    scraper-1.0-SNAPSHOT.jar
	conf/application.properties - where the shift configuration of threads are configured
	conf/log4j2.xml
	file/data.csv               - where the scraped data are appended
	lib/log4j-api-2.3.jar
	lib/log4j-core-2.3.jar
  
* Run scraper-1.0-SNAPSHOT.jar 
	In Windows: java -jar  scraper-1.0-SNAPSHOT.jar 
	In unix: nohup java -jar scraper-1.0-SNAPSHOT.jar >&/dev/null

#### Developer's note
* My first Java thread project ever!
* Took me a while to finish this project but it's quite fun to do this. As a developer, it's always fun doing something new.
* This project will be my stepping stone to develop a Heart-beat like application.
