# oauth2-gemfire-sample

###Steps to Run example
* Set your JAVA_HOME env variable on a shell to a java 1.8 JDK:  
```
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_73.jdk/Contents/Home
```

* Start the Geode GFSH shell:
```
apache-geode-1.0.0-incubating.M2/bin/gfsh
```

* From the running GFSH shell start a Geode locator:
```
gfsh>start locator --name=locator1 --classpath=../lib/geode-dependencies-jar
```

* From the running GFSH shell start a Geode server with the redis adapter enabled:
```
gfsh>start server --name=server1 --classpath=../lib/geode-dependencies-jar --redis-bind-address=localhost --redis-port=6379
```

* In a new terminal clean and run the spring boot application
```
mvn clean spring-boot:run
```

* Access your application in a browser window at the following URL: http://localhost:9999/uaa/oauth/authorize?response_type=code&client_id=acme&redirect_uri=http://example.com  If prompted for authentication use user/password

* Authorize the request:
![alt text][img]

[img]: /img.png "Image"

* Once you have authenticated you will get a redirect to example.com with an authorization code attached, e.g. http://example.com/?code=jYWioI.  The code can be exchanged for an access token using the "acme" client credentials on the token endpoint. Substitute the code in the redirect for $CODE.  Your OAuth tokens will be returned
```
curl acme:acmesecret@localhost:9999/uaa/oauth/token -d grant_type=authorization_code -d client_id=acme -d redirect_uri=http://example.com -d code=$CODE

{"access_token":"e82fa2fd-980d-4de6-9b4f-9d944d0bc51c","token_type":"bearer","refresh_token":"a3cfa4ba-1869-4f53-b8fd-110cf98b3634","expires_in":43143,"scope":"openid write"}%  
```
