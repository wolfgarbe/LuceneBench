LuceneBench<br>
[![MIT License](https://img.shields.io/github/license/wolfgarbe/lucenebench.svg)](https://github.com/wolfgarbe/LuceneBench/blob/master/LICENSE)
========
[Lucene](http://lucene.apache.org/core/) is a high-performance search engine library written in Java, powering the search platforms  [Solr](http://lucene.apache.org/solr/) and [Elasticsearch](https://www.elastic.co/de/products/elasticsearch).
<br><br>
[SeekStorm](https://seekstorm.com) is a high-performance search platform written in C#, powering the SeekStorm Search as a Service.
<br><br>

> Building a search engine is easy, so there are a lot of them, and all are really fast 
> ... as long as you have only 1000 documents indexed, and only a single concurrent searcher.
> The hard part is scaling: Searching an index with billions of documents, with thousand concurrent users and still returning results within milliseconds on a single machine.

<br><br>
Performance for Indexing and Search is of paramount importance, but reliable numbers are hard to obtain. 
While there are many benchmark results published, they all vary depending on 
* number of documents, 
* size of document, 
* number of parallel users,
* number of keywords per query,
* term processing (tokenizer, stemmer, stop-words filter),
* default query operator (Lucene uses OR as default)
* implicit phrase search (are all results which satisfy a phrase are ranked on top; Lucene:no, SeekStorm:yes)
* hardware (Processor, RAM, SSD type) [Lucene Benchmark](https://home.apache.org/~mikemccand/lucenebench/) uses 2x Xeon E5 2699 with 72 cores, 256 GB RAM, 
* search software version,
* standalone vs. cloud mode (sharding)
* operating system, 
* file system,
* whether TotalHits are counted or just estimated (*seems Lucene estimates instead of a throughout search: [1.](https://issues.apache.org/jira/browse/LUCENE-8060) [2.](https://issues.apache.org/jira/browse/LUCENE-8430) [3.](https://issues.apache.org/jira/browse/LUCENE-8431)*)
* whether the **best ranked results** are returned or there is an early termination after **some results** are found.



The only way to objectively compare technologies is to run a benchmark according to **your** requirements on **your** hardware.
<br><br>
## Key Performance Indicators
Lucene Bench measures the following Key Performance Indicators (KPI):
<br>
* Indexing Throughput (million documents per day)
* Search Througput (queries per second - QPS)
* Query Latency (mean, median, maximum, percentiles)
* Concurrent Users
* Index Size

## Indexing Test data
The [English Wikipedia dump](https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2) (5,677,776 docs, 63.9 GB) original XML format has been exported to a plain text file (UTF-8, 17.6 GB).
Five consecutive lines constitute a single document: title, content, domain, url, date.

## Query Test data
[TREC 2009 Million Query Track](https://trec.nist.gov/data/million.query09.html) (40,000 queries)<br>
The test queries are stored in a plain text file (UTF-8).
<br><br>
Test data and search index are stored on different disks in order to utilize the full disk speed for indexing and searching, uncompromized by reading the test data.

## Benchmark results

![Benchmark](https://wolfgarbe.github.io/LuceneBench/img/lucenebench1.png "Benchmark")

![Benchmark](https://wolfgarbe.github.io/LuceneBench/img/lucenebench2.png "Benchmark")

|                           | [Lucene](http://lucene.apache.org/core/) v8.4.1   | [SeekStorm](https://seekstorm.com/) v0.2   | Factor |
| :--- | ---: | ---: | ---: |    
| **Search Latency** (ms, 4 concurrent users)   | 63  |  3 |  **21.0** | 
| &nbsp;&nbsp;&nbsp;mean |  63 | 3  |  |
| &nbsp;&nbsp;&nbsp;median |  61 | 3  |  |
| &nbsp;&nbsp;&nbsp;90th percentile | 81  | 8  |  |
| &nbsp;&nbsp;&nbsp;99th percentile | 110  | 17  |  |
| **Maximum Throughput** (QPS)   | 63  | 1146  | **18.2** | 
| **Maximum Concurrent Users** (latency<1s) | 4  | 1000  | **250** |
| **Indexing Speed** (million docs/day) | 900 | 550  | **0.61** |
| **Indexing Speed** (GB/hour)  | 116  | 70  |  **0.61** |
| **Index Size** (GB)           | 17  | 17  | **1.00** |
| **Write Amplification**       | 6.19 | 1.28 | **4.84** |

### Benchmark conditions
Title, content, domain, url, date fields are stored and retrieved.<br>
Full text search in all fields.<br>
KWIC summary generated from content field.<br>
Lucene SimpleAnalyzer (No stopwords, no stemming).<br>
Lucene DefaultOperator: AND<br>
Multithreaded queries: 4 Threads (>4 Lucene crash)<br>
Multithreaded indexing: 16 Threads (as [recommended](https://home.apache.org/~mikemccand/lucenebench/indexing.html))<br>
Lucene RAM buffer size: 2048 MB (as [recommended](https://home.apache.org/~mikemccand/lucenebench/indexing.html))<br>
JRE parameters: -Xmx8g -Xms8g -server (as [recommended](https://home.apache.org/~mikemccand/lucenebench/indexing.html))

### Hardware
Intel Core i7-8750H<br>
32 GB RAM<br>
Samsung 970 EVO SSD, 1TB<br>

### Software
Lucene 8.4.1<br>
Java SE 13.0.2<br>
Microsoft Windows 10 Professional<br>
