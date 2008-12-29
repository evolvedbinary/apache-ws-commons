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

package org.apache.ws.commons.tcpmon.eclipse.ui;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.apache.ws.commons.tcpmon.SlowLinkSimulator;
import org.apache.ws.commons.tcpmon.TCPMonBundle;

/**
 * a connection listens to a single current connection
 */
class Connection extends Thread {
    public TableItem item;

    /**
     * Field listener
     */
    Listener listener;

    /**
     * Field active
     */
    boolean active;

    /**
     * Field fromHost
     */
    String fromHost;

    /**
     * Field time
     */
    String time;

    /**
     * Field elapsed time
     */
    long elapsedTime;
    
    /**
     * Field inputText
     */
    Text inputText = null;

    /**
     * Field outputText
     */
    Text outputText = null;

    /**
     * Field inSocket
     */
    Socket inSocket = null;

    /**
     * Field outSocket
     */
    Socket outSocket = null;

    /**
     * Field clientThread
     */
    Thread clientThread = null;

    /**
     * Field serverThread
     */
    Thread serverThread = null;

    /**
     * Field rr1
     */
    SocketRR rr1 = null;

    /**
     * Field rr2
     */
    SocketRR rr2 = null;

    /**
     * Field inputStream
     */
    InputStream inputStream = null;

    /**
     * Field HTTPProxyHost
     */
    String HTTPProxyHost = null;

    /**
     * Field HTTPProxyPort
     */
    int HTTPProxyPort = 80;

    /**
     * Field slowLink
     */
    private SlowLinkSimulator slowLink;

    /**
     * Constructor Connection
     *
     * @param l
     */
    public Connection(Listener l) {
        listener = l;
        HTTPProxyHost = l.HTTPProxyHost;
        HTTPProxyPort = l.HTTPProxyPort;
        slowLink = l.slowLink;
    }

    /**
     * Constructor Connection
     *
     * @param l
     * @param s
     */
    public Connection(Listener l, Socket s) {
        this(l);
        inSocket = s;
        start();
    }

    /**
     * Constructor Connection
     *
     * @param l
     * @param in
     */
    public Connection(Listener l, InputStream in) {
        this(l);
        inputStream = in;
        start();
    }

    /**
     * Method run
     */
    public void run() {
        try {
            active = true;
            HTTPProxyHost = System.getProperty("http.proxyHost");
            if ((HTTPProxyHost != null) && HTTPProxyHost.equals("")) {
                HTTPProxyHost = null;
            }
            if (HTTPProxyHost != null) {
                String tmp = System.getProperty("http.proxyPort");
                if ((tmp != null) && tmp.equals("")) {
                    tmp = null;
                }
                if (tmp == null) {
                    HTTPProxyPort = 80;
                } else {
                    HTTPProxyPort = Integer.parseInt(tmp);
                }
            }
            if (inSocket != null) {
                fromHost = (inSocket.getInetAddress()).getHostName();
            } else {
                fromHost = "resend";
            }
            String dateformat = TCPMonBundle.getMessage("dateformat00", "yyyy-MM-dd HH:mm:ss");
            DateFormat df = new SimpleDateFormat(dateformat);
            time = df.format(new Date());
            final int count = listener.connections.size();

            MainView.display.syncExec(new Runnable() {
                public void run() {
                    item = new TableItem(listener.connectionTable, SWT.BORDER, count + 1);
                    item.setText(new String[]{TCPMonBundle.getMessage("active00", "Active"),
                            time,
                            fromHost,
                            listener.hostField.getText(),
                            "", ""});
                    listener.tableEnhancer.setSelectionInterval(0, 0);
                }
            });


            listener.connections.add(this);
            TableEnhancer te = listener.tableEnhancer;
            if ((count == 0) || (te.getLeadSelectionIndex() == 0)) {

                MainView.display.syncExec(new Runnable() {
                    public void run() {
                        inputText = (Text) listener.setLeft(MainView.SWT_TEXT, "");
                        outputText = (Text) listener.setRight(MainView.SWT_TEXT, "");
                        listener.removeButton.setEnabled(false);
                        listener.removeAllButton.setEnabled(true);
                        listener.saveButton.setEnabled(true);
                        listener.resendButton.setEnabled(true);
                    }
                });

            }

            final ArrayList outValues = new ArrayList();
            MainView.display.syncExec(new Runnable() {
                public void run() {
                    outValues.add(listener.hostField.getText());
                    outValues.add(listener.tPortField.getText());
                    outValues.add(listener.portField.getText());

                }
            });

            String targetHost = (String) outValues.get(0);
            int targetPort = Integer.parseInt((String) outValues.get(1));
            int listenPort = Integer.parseInt((String) outValues.get(2));


            InputStream tmpIn1 = inputStream;
            OutputStream tmpOut1 = null;
            InputStream tmpIn2 = null;
            OutputStream tmpOut2 = null;
            if (tmpIn1 == null) {
                tmpIn1 = inSocket.getInputStream();
            }
            if (inSocket != null) {
                tmpOut1 = inSocket.getOutputStream();
            }
            String bufferedData = null;
            StringBuffer buf = null;
            int index = listener.connections.indexOf(this);

            final ArrayList outValues2 = new ArrayList();
            MainView.display.syncExec(new Runnable() {
                public void run() {
                    outValues2.add(listener.isProxyBox.getSelection() ? "true" : "false");
                    outValues2.add((HTTPProxyHost != null) ? "true" : "false");
                }
            });

            if ("true".equals(outValues2.get(0)) || "true".equals(outValues2.get(1))) {

                // Check if we're a proxy
                byte[] b = new byte[1];
                buf = new StringBuffer();
                String s;
                for (; ;) {
                    int len;
                    len = tmpIn1.read(b, 0, 1);
                    if (len == -1) {
                        break;
                    }
                    s = new String(b);
                    buf.append(s);
                    if (b[0] != '\n') {
                        continue;
                    }
                    break;
                }
                bufferedData = buf.toString();
                final String inputString = bufferedData;
                MainView.display.syncExec(new Runnable() {
                    public void run() {
                        inputText.append(inputString);
                    }
                });
                if (bufferedData.startsWith("GET ")
                        || bufferedData.startsWith("POST ")
                        || bufferedData.startsWith("PUT ")
                        || bufferedData.startsWith("DELETE ")) {
                    int start, end;
                    URL url;
                    start = bufferedData.indexOf(' ') + 1;
                    while (bufferedData.charAt(start) == ' ') {
                        start++;
                    }
                    end = bufferedData.indexOf(' ', start);
                    String urlString = bufferedData.substring(start, end);
                    if (urlString.charAt(0) == '/') {
                        urlString = urlString.substring(1);
                    }

                    final boolean[] out = new boolean[1];
                    MainView.display.syncExec(new Runnable() {
                        public void run() {
                            out[0] = listener.isProxyBox.getSelection();
                        }
                    });

                    if (out[0]) {
                        url = new URL(urlString);
                        targetHost = url.getHost();
                        targetPort = url.getPort();
                        if (targetPort == -1) {
                            targetPort = 80;
                        }

                        final int inputInt = index;
                        final String inputString2 = targetHost;
                        MainView.display.syncExec(new Runnable() {
                            public void run() {
                                listener.tableEnhancer.setValueAt(inputString2,
                                        inputInt + 1,
                                        MainView.OUTHOST_COLUMN);
                            }
                        });

                        bufferedData = bufferedData.substring(0, start)
                                + url.getFile()
                                + bufferedData.substring(end);
                    } else {
                        url = new URL("http://" + targetHost + ":"
                                + targetPort + "/" + urlString);

                        final int inputInt = index;
                        final String inputString2 = targetHost;
                        MainView.display.syncExec(new Runnable() {
                            public void run() {
                                listener.tableEnhancer.setValueAt(inputString2,
                                        inputInt + 1,
                                        MainView.OUTHOST_COLUMN);
                            }
                        });
                        bufferedData = bufferedData.substring(0, start)
                                + url.toExternalForm()
                                + bufferedData.substring(end);
                        targetHost = HTTPProxyHost;
                        targetPort = HTTPProxyPort;
                    }
                }
            } else {
                //
                // Change Host: header to point to correct host
                //
                byte[] b1 = new byte[1];
                buf = new StringBuffer();
                String s1;
                String lastLine = null;
                for (; ;) {
                    int len;
                    len = tmpIn1.read(b1, 0, 1);
                    if (len == -1) {
                        break;
                    }
                    s1 = new String(b1);
                    buf.append(s1);
                    if (b1[0] != '\n') {
                        continue;
                    }

                    // we have a complete line
                    String line = buf.toString();
                    buf.setLength(0);

                    // check to see if we have found Host: header
                    if (line.startsWith("Host: ")) {

                        // we need to update the hostname to target host
                        String newHost = "Host: " + targetHost + ":"
                                + listenPort + "\r\n";
                        bufferedData = bufferedData.concat(newHost);
                        break;
                    }

                    // add it to our headers so far
                    if (bufferedData == null) {
                        bufferedData = line;
                    } else {
                        bufferedData = bufferedData.concat(line);
                    }

                    // failsafe
                    if (line.equals("\r\n")) {
                        break;
                    }
                    if ("\n".equals(lastLine) && line.equals("\n")) {
                        break;
                    }
                    lastLine = line;
                }
                if (bufferedData != null) {

                    final String inputString = bufferedData;
                    MainView.display.syncExec(new Runnable() {
                        public void run() {
                            inputText.append(inputString);
                        }
                    });
                    int idx = (bufferedData.length() < 50)
                            ? bufferedData.length()
                            : 50;
                    s1 = bufferedData.substring(0, idx);
                    int i = s1.indexOf('\n');
                    if (i > 0) {
                        s1 = s1.substring(0, i - 1);
                    }
                    s1 = s1 + "                           "
                            + "                       ";
                    s1 = s1.substring(0, 51);

                    final int inputInt = index;
                    final String inputString2 = s1;
                    MainView.display.syncExec(new Runnable() {
                        public void run() {
                            listener.tableEnhancer.setValueAt(inputString2,
                                    inputInt + 1,
                                    MainView.REQ_COLUMN);
                        }
                    });
                }
            }
            if (targetPort == -1) {
                targetPort = 80;
            }
            outSocket = new Socket(targetHost, targetPort);
            tmpIn2 = outSocket.getInputStream();
            tmpOut2 = outSocket.getOutputStream();
            if (bufferedData != null) {
                byte[] b = bufferedData.getBytes();
                tmpOut2.write(b);
                slowLink.pump(b.length);
            }

            final boolean[] out = new boolean[1];
            MainView.display.syncExec(new Runnable() {
                public void run() {
                    out[0] = listener.xmlFormatBox.getSelection();
                }
            });
            boolean format = out[0];

            // this is the channel to the endpoint
            rr1 = new SocketRR(this, inSocket, tmpIn1, outSocket, tmpOut2,
                    inputText, format, listener.connectionTable,
                    index + 1, "request:", slowLink);

            // create the response slow link from the inbound slow link
            SlowLinkSimulator responseLink =
                    new SlowLinkSimulator(slowLink);

            // this is the channel from the endpoint
            rr2 = new SocketRR(this, outSocket, tmpIn2, inSocket, tmpOut1,
                    outputText, format, null, 0, "response:",
                    responseLink);
            
            while ((rr1 != null) || (rr2 != null)) {

                if (rr2 != null) {
                    final int inputInt = index;
                    MainView.display.syncExec(new Runnable() {
                        public void run() {
                            listener.tableEnhancer.setValueAt(rr2.getElapsed(), 1 + inputInt, MainView.ELAPSED_COLUMN);
                        }
                    });
                }
                
                // Only loop as long as the connection to the target
                // machine is available - once that's gone we can stop.
                // The old way, loop until both are closed, left us
                // looping forever since no one closed the 1st one.
                
                if ((null != rr1) && rr1.isDone()) {
                    if ((index >= 0) && (rr2 != null)) {
                        final int inputInt = index;
                        MainView.display.syncExec(new Runnable() {
                            public void run() {
                                listener.tableEnhancer.setValueAt(
                                        TCPMonBundle.getMessage("resp00", "Resp"), 1 + inputInt,
                                        MainView.STATE_COLUMN);
                            }
                        });
                    }
                    rr1 = null;
                }

                if ((null != rr2) && rr2.isDone()) {
                    if ((index >= 0) && (rr1 != null)) {
                        final int inputInt = index;
                        MainView.display.syncExec(new Runnable() {
                            public void run() {
                                listener.tableEnhancer.setValueAt(
                                        TCPMonBundle.getMessage("req00", "Req"), 1 + inputInt,
                                        MainView.STATE_COLUMN);
                            }
                        });
                    }
                    rr2 = null;
                }

                synchronized (this) {
                    this.wait(100);    // Safety just incase we're not told to wake up.
                }
            }

            active = false;

            if (index >= 0) {
                final int inputInt = index;
                MainView.display.syncExec(new Runnable() {
                    public void run() {
                        listener.tableEnhancer.setValueAt(
                                TCPMonBundle.getMessage("done00", "Done"),
                                1 + inputInt, MainView.STATE_COLUMN);
                    }
                });
            }

        } catch (Exception e) {
            StringWriter st = new StringWriter();
            PrintWriter wr = new PrintWriter(st);
            int index = listener.connections.indexOf(this);
            if (index >= 0) {
                final int inputInt = index;
                MainView.display.syncExec(new Runnable() {
                    public void run() {
                        listener.tableEnhancer.setValueAt(
                                TCPMonBundle.getMessage("error00", "Error"), 1 + inputInt,
                                MainView.STATE_COLUMN);

                    }
                });
            }
            e.printStackTrace(wr);
            wr.close();

            final String inputString = st.toString();
            MainView.display.syncExec(new Runnable() {
                public void run() {
                    if (outputText != null) {
                        outputText.append(inputString);
                    } else {
                        // something went wrong before we had the output area
                        System.out.println(inputString);
                    }

                }
            });

            halt();
        }
    }

    /**
     * Method wakeUp
     */
    synchronized void wakeUp() {
        this.notifyAll();
    }

    /**
     * Method halt
     */
    public void halt() {
        try {
            if (rr1 != null) {
                rr1.halt();
            }
            if (rr2 != null) {
                rr2.halt();
            }
            if (inSocket != null) {
                inSocket.close();
            }
            inSocket = null;
            if (outSocket != null) {
                outSocket.close();
            }
            outSocket = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
