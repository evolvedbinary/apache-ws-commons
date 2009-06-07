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

package org.apache.ws.commons.tcpmon.core.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;

public class TestHttpHandler extends AbstractHttpHandler {
    public void handle(String pathInContext, String pathParams,
            HttpRequest request, HttpResponse response) throws HttpException,
            IOException {
        
        if (pathInContext.startsWith("/")) {
            try {
                String name = pathInContext.substring(1);
                Method method = getClass().getMethod(name, new Class[] { HttpRequest.class, HttpResponse.class });
                method.invoke(this, new Object[] { request, response });
                request.setHandled(true);
            } catch (SecurityException ex) {
                // Do nothing
            } catch (NoSuchMethodException ex) {
                // Do nothing
            } catch (IllegalAccessException ex) {
                // Do nothing
            } catch (InvocationTargetException ex) {
                // Do nothing
            }
        }
    }
    
    public void test(HttpRequest request, HttpResponse response) throws IOException {
        response.setContentType("text/plain");
        Writer out = new OutputStreamWriter(response.getOutputStream());
        out.write("test");
        out.flush();
    }
    
    public void echo(HttpRequest request, HttpResponse response) throws IOException {
        InputStream in = request.getInputStream();
        if ("gzip".equals(request.getField("Content-Encoding"))) {
            in = new GZIPInputStream(in);
        }
        IOUtils.copy(in, response.getOutputStream());
    }
}
