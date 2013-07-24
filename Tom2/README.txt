

setup data
============
ls
sample.json
skupunit:assets issmith1$ pwd
/Users/issmith1/android/peppermint/tom1/assets

ls
WEB-INF		guestbook.jsp	irene.json	sample.json	stylesheets
skupunit:war issmith1$ pwd
/Users/issmith1/stash/appengine-java-sdk-1.8.1.1/demos/guestbook/war


start web server
pwd
/Users/issmith1/stash/appengine-java-sdk-1.8.1.1
skupunit:appengine-java-sdk-1.8.1.1 issmith1$ ./bin/dev_appserver.sh demos/guestbook/war


Google README

To run the local development server use:
    bin\dev_appserver.cmd <your web application>

It will begin listening on port 8080 on the local machine.

Many of the sample applications have Ant build.xml targets that start
the local runtime environment.

Uploading your App to Google
----------------------------

1) Go to http://appengine.google.com and create your application.

2) Make sure that the application identifier in your appengine-web.xml file
   matches the one you chose in step 1.

3) Run appcfg to upload your application to deploy your application to
   Google's Servers:

   From the appengine-java-sdk directory, run:
     bin\appcfg.cmd update <your web application>
     bin/appcfg.sh update demos/guestbook/war

this is the URL
http://tom-swifty.appspot.com/sample.json
http://tom-swifty.appspot.com/challenges.json

3.5)
Beginning interaction for server default...
Email: ir.smith99@gmail.com
Password for ir.smith99@gmail.com: 
Email: ir.smith99@gmail.com
Password for ir.smith99@gmail.com: 
Email: ir.smith99@gmail.com
Password for ir.smith99@gmail.com: 

com.google.appengine.tools.admin.ClientLoginServerConnection$ClientLoginException: Use an application-specific password instead of your regular account password. See http://www.google.com/support/accounts/bin/answer.py?answer=185833
Unable to update app: Use an application-specific password instead of your regular account password. See http://www.google.com/support/accounts/bin/answer.py?answer=185833
Please see the logs [/var/folders/tj/tdh3l01x633dr9j3ztmd0601_79p0f/T/appcfg558534912804139142.log] for further information.

3.6)
https://accounts.google.com/IssuedAuthSubTokens?hide_authsub=1#accesscodes
copy the yellow password
enter email then the passw to
     bin/appcfg.sh update demos/guestbook/war



4) Try your application out at:  http://<app-id>.appspot.com