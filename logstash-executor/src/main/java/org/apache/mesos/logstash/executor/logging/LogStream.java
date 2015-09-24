package org.apache.mesos.logstash.executor.logging;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Stream of logs.
 */
public interface LogStream {
    void attach(OutputStream stdout, OutputStream stderr) throws IOException;
    String readFully();
    void close();
    String getLogstashPid();
}
