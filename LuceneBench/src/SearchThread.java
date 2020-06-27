public class SearchThread implements Runnable 
{
    String queryString;

    public SearchThread(String queryString) 
    {
        this.queryString=queryString;
    }

    @Override
    public void run() 
    {
        //Measure the end to end latency of my client code
        try
        {
            long start=System.currentTimeMillis();
            long count=LuceneBenchmark.lucene.search(queryString,false);  
            long searchTime=System.currentTimeMillis()-start;
            LuceneBenchmark.resultCountSum.addAndGet(count);

            LuceneBenchmark.lock.lock();
            try {
                LuceneBenchmark.sampleSearchTime.Add(searchTime);
            } finally {
                LuceneBenchmark.lock.unlock();
            }
        } 
        finally
        {
            LuceneBenchmark.searchSemaphore.release();
        }
    }   
}