# File Store
This application represents a POC of a file storage service 
## Table of Contents
- [General](#General)
- [Installation](#installation)


## General
The main function of the service is to enable file storage operations.
The main logical key of the file will be considered it's full name(including path),
example "/categories/binoculars/hunting/HomeworkRESTFileService.pdf"
CRUD Operations possible are:
- upload file, a POST with Multipart and path parameters, the original filename being appended to the path for full key
- download file, a GET with variable length path segments to mimic the key
- update file, a PUT with variable length path segments to mimic the key
- delete file, a DELETE with variable length path segments to mimic the key

Other more general operations are in the admin controller:
- get a file count
- get files by regex

Both endpoints are using a redis cache(locally installed for this POC) for faster retrieval of data.
When a file is added/deleted, asynchronously a listener picks the event and does the relevant processing 
on the caching services, either adding or evicting, to keep the cache up to date, once it has been
loaded from db.
To work with redis, I preferred a jedis implementation, which is low level, but assures the functionalities that I needed
As a db, I used h2, as it's easy to bootstrap locally(also usable for tests) and mimics a rdbms

## Installation
Clone the repo at: https://github.com/hagiumarius/filestore.git
- install redis locally (for windows you can find it at https://github.com/MicrosoftArchive/redis/releases), default port
- with a maven tool, run a mvn clean package in the root folder
- with a java(jdk 17) enabled terminal run a java -jar filestore-0.0.1-SNAPSHOT.war on the resulting war in the /target subfolder
- alternatively to the above step, you can also start it from your favourite ide, by running the main method in the FilestoreApplication
- with the service started you can access it at http://localhost:8889/fs/{relevantOperation}
- A postman collection is available in the root folder with all 6 http calls working

