# ConcurrencyAndLockOnLoggerClient

1. Start a processId for given timestamp----
    void start(String processId, long timeStamp);
2. End a processId for given timestamp ---
    void end(String processId, long timestamp);
3. Poll mechanism to fetch latest message ----
    // {3} process started at {7} and ended {15}
    // {2} process started at {8} and ended {12}
    // {1} process started at {12} and ended {15}
    String poll();
    
    
