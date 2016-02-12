Ownership Information:

Name:       Anthony Bobsin, Bilawal Sheikh
Student ID: 500506750,      500563972
Course:     CPS506, Winter 2016, Assignment #1
Due:        2016.02.11 23:59
Credit:     This is entirely our own groups work.


Rationale:
When the program is first ran, it checks if the input URL is an valid URL. If not, the program exits.
If it is a valid url, a new WebCrawler class is created passing the URL argument and path/pages argument if provided.
Once this Url is sent to WebCrawler, it uses the constructor to initialize the private variables inside the class.
In the constructor, it calls queueWebCrawlTask, that creates an new thread. The following command is used to create the new thread:

(new Thread(new WebCrawlJob(url, path))).start();

The WebCrawlJob is a class that implements runnable. Due to the runnable implementation, Java automatically calls the run method. This method calls the parse
method and the increment pages crawled method. If the page is not available, it does not increment pages crawled. Inside the parse method, if it does find an url,
it constantly checks if a new thread should be created. If not, the path and pages limit has been reached. If created, a new thread is created with an updated path
to keep count of the number of paths. While the new thread is creating, the old thread is constantly checking if the an html tag is contained in an HashMap; if not
it adds the html tag to the HashMap. If the html tag was previously added; it increments the html tag. Once this thread is finished, it updates the pages crawled.

This keeps on happening constantly until the path and pages limit is reached.
Now, to keep track of all the threads created, the following structure was created:

ConcurrentLinkedQueue<Thread> webCrawlerThreads

This structure allows the program to figure out when all the threads are finished. If all the threads are finished; it will print the global variables.
And as soon as each thread is finished crawling the page; it puts the URL and the pages stats into another HashMap. The HashMap structure is as followed:

HashMap<URL, HashMap<String, Integer>>

The URL is a key to another HashMap. Inside that HashMap, it contains the html tag and the count of the html tag. And the end, it prints out all the urls and
their representative tags/counts. When all the threads are finished; an sorted tree is created. This will alphabetically organize all the html tags and print their
respective count.
