# GuessTheCelebrity
A Quiz to test your knowledge of famous celebrities.


# What's in this project?
- In this project I parse a webpage to extract all the needed data using <b>regex pattern matching</b>. The class <b>WebParser</b> contains two methods <code>extractImages(String)</code> and <code>extractCelebrities(String)</code> that takes the html code as input and returns a list of images/celebrities.

- The html code for the celebrities data is obtained from <a href="http://www.posh24.se/kandisar">here</a>. The html is downloaded using a method <code>getUrlBytes()</code> from the class <b>NetworkUtils</b>
This method uses an <code>HttpURLConnection</code> object to make a connection and returns a stream of data. Other third party libraries like <code>OkHttp</code> & <code>Retrofit</code> maybe used as well, however i only used <b>java.net</b> and <b>java.io</b> packages.


- <b>AsyncTask</b> is used to run 2 background tasks for downloading. The first task <code>WebDownloader</code> is for downloading the html code as mentione before, and the second task <code>ImageDownloader</code> is to return a bitmap corresponding to a certain celebrity.
Finally, i make use of the <b>LruCache</b> class from the <b>support library</b> to cache the bitmaps. 

- <b>Fragments</b> are also used in this project to make use of <code>setRetainInstance(true)</code> which will retain our data (LruCache and answers) across device configuration changes such as screen orientation.

- A landscape variation is also added for the default layout.


# Screenshots
<img src="portrait1.png" width="300" style="margin:20px"/>        <img src="portrait2.png" width="300"/>
<br/>
<img src="landscape.png" width="600"/>
