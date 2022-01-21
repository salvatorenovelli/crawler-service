# crawler-service

This crawler will scan a list of urls and publish a series of events on PubSub with all the pages crawled. It's designed to handle big websites with million of pages but it will take a bit of memory as the list of the crawled pages is held in memory. This also means that if there is a crash or a shutdown, the crawl will have to restart from scratch as progress is not handed over or recovered in the current implementation.

Its data model is quite tied down to my application but the internals of the crawler are all well abstracted and tested so it shuold be simple to reuse.

If you need this crawler as a library, I'm happy to extract it and open source it 
