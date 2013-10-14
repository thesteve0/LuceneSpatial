This is the lucene web application that uses the spatial index created by spatialindexer. We are going to build a REST web service on top of the index

It will be presented at FOSS4G 2013

To use:
1. Install the OpenShift command line tools and run *rhc setup*

2. run the following command *rhc app create lucenespatial jbosseap-6*

3. when that is finished *cd lucenespatial*

4. Add this project as a remote project to your local git repo *git remote add github -m master https://github.com/thesteve0/LuceneSpatial.git*

5. Merge this repo into your local repo *git pull -s recursive -X theirs github master*

6. finally push it back up to OpenShift *git push*

Then from your local machine you need to scp or sftp the indexDir up to your applications data directory

scp -r indexDir <uuid>@lucenespatial-<yourdomain>.rhcloud.com:app-root/data

restart your application

*rhc app restart lucenespatial*

Now you should be able to see some json if you hit:

http://lucenespatial-<yourdomain>.rhcloud.com/ws/parks



