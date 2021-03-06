# WebStats

The application is a statistical web crawler. Given an initial URL as a command line argument, visit connecting pages and gather statistics on the pages.

The following parameters will apply:

Call your main class WebStats.

1) Limit the pages you navigate to a parameter passed on the command line (default 10), and the maximum path length to another command line parameter (default 3), where the initial page has a path length of 1, and pages linked from that page have a path length of 2, etc.

Only visit each page once. Scan the page for start-tags, e.g. <div>, <html>, <body>, <a>. Ignore end-tags, e.g. </div>, </a>, </ol>.

You do not need to be sophisticated; assume every < is the start of a tag, and the next > is the end of that tag; assume attributes in tags will be properly surrounded by " characters. Case does not matter for tags or attributes.

The only attribute you care about is the href= attribute on a-tags. Links may be relative, same host, or full URL. You only need to follow http: URLs.

You don't care about whether start-tags are properly balanced with end-tags.

The final listing should list all the pages visited, with the counts of total start tags found on each. This will be followed by a sorted list of all the tag names found and the global (across all visited pages) count for each.
