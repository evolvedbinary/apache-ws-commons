/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ws.commons.tcpmon.core.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.ws.commons.tcpmon.TCPMonBundle;
import org.apache.ws.commons.tcpmon.core.engine.RequestResponseListener;
import org.apache.ws.commons.tcpmon.core.filter.CharsetDecoderFilter;
import org.apache.ws.commons.tcpmon.core.filter.Pipeline;
import org.apache.ws.commons.tcpmon.core.filter.RequestLineExtractor;
import org.apache.ws.commons.tcpmon.core.filter.http.HttpRequestFilter;
import org.apache.ws.commons.tcpmon.core.filter.http.HttpResponseFilter;

public abstract class AbstractRequestResponse implements RequestResponseListener {
    private static final String[] states = new String[] {
        TCPMonBundle.getMessage("active00","Active"),
        TCPMonBundle.getMessage("req00", "Req"),
        TCPMonBundle.getMessage("resp00", "Resp"),
        TCPMonBundle.getMessage("done00", "Done"),
        TCPMonBundle.getMessage("error00", "Error")
    };
    
    private static final Charset UTF8 = Charset.forName("utf-8");
    
    private final Configuration config;
    private String targetHost;
    private int targetPort;
    
    public AbstractRequestResponse(Configuration config) {
        this.config = config;
    }
    
    protected static String getTime() {
        String dateformat = TCPMonBundle.getMessage("dateformat00", "yyyy-MM-dd HH:mm:ss");
        DateFormat df = new SimpleDateFormat(dateformat);
        return df.format(new Date());
    }
    
    public void setState(int state) {
        setState(states[state]);
    }

    public String getTargetHost() {
        return targetHost;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTarget(String targetHost, int targetPort) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        setOutHost(targetHost);
    }

    public OutputStream getRequestOutputStream() {
        Pipeline pipeline = new Pipeline();
        pipeline.addFilter(new RequestLineExtractor(50) {
            protected void done(String requestLine) {
                setRequest(requestLine);
            }
        });
        if (config.isXmlFormat()) {
            HttpRequestFilter filter = new HttpRequestFilter(true);
            filter.setContentFilterFactory(new DefaultContentFilterFactory());
            pipeline.addFilter(filter);
        }
        pipeline.addFilter(new CharsetDecoderFilter(getRequestWriter(), UTF8));
        return pipeline.getOutputStream();
    }
    
    public OutputStream getResponseOutputStream() {
        Pipeline pipeline = new Pipeline();
        if (config.isXmlFormat()) {
            HttpResponseFilter filter = new HttpResponseFilter(true);
            filter.setContentFilterFactory(new DefaultContentFilterFactory());
            pipeline.addFilter(filter);
        }
        pipeline.addFilter(new CharsetDecoderFilter(getResponseWriter(), UTF8));
        return pipeline.getOutputStream();
    }
    
    public void onError(Throwable ex) {
        StringWriter st = new StringWriter();
        PrintWriter wr = new PrintWriter(st);
        ex.printStackTrace(wr);
        wr.close();
        try {
            getResponseWriter().write(st.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void setState(String state);
    protected abstract void setOutHost(String outHost);
    protected abstract void setRequest(String request);
    protected abstract Writer getRequestWriter();
    protected abstract Writer getResponseWriter();
    public abstract String getRequestAsString();
    public abstract String getResponseAsString();
}
