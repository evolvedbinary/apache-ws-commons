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

package org.apache.ws.commons.tcpmon.core.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Class that sends a byte stream through a sequence of filters.
 * Each filter receives as input the (potentially modified) output
 * of the previous filter in the chain.
 * <p>
 * The pipeline works in push
 * mode and is non blocking. This distinguishes it from a chain
 * of (Filter)InputStreams. Also, a filter is not required to process
 * all received data on each invocation (in which case it gets a chance to process
 * the unprocessed data during the next invocation). This is useful when
 * the filter requires more lookahead than is available. This
 * distinguishes a pipeline from a chain of (Filter)OutputStreams (where each
 * OutputStream is responsible itself for buffering unprocessed data).
 * <p>
 * Finally, the implementation optimizes buffer allocation and minimizes
 * array copy operations.
 */
public class Pipeline {
    private class StreamImpl implements Stream {
        private final StreamFilter filter;
        private StreamImpl next;
        private byte[] inBuffer;
        private int inOffset;
        private int inLength;
        private boolean lastBuffer;
        private boolean preserve;
        private int skipOffset;
        private int skipLength;
        private byte[] outBuffer;
        private int outLength;
        private boolean eosSignalled; // Set to true if the next filter has been invoked with eos == true
        private StreamImpl nested;
        
        public StreamImpl(StreamFilter filter) {
            this.filter = filter;
        }
        
        public void setNext(StreamImpl next) {
            this.next = next;
        }

        public void invoke(byte[] buffer, int offset, int length, boolean eos, boolean preserve) {
            do {
                if (inLength == 0) {
                    setBuffer(buffer, offset, length, preserve);
                    offset += length;
                    length = 0;
                } else if (length > 0) {
                    int c = fillBuffer(buffer, offset, length);
                    if (c == 0) {
                        throw new StreamException("Pipeline buffer overflow caused by filter " + filter.getClass().getName() + " (" + filter + ")");
                    }
                    offset += c;
                    length -= c;
                }
                this.lastBuffer = eos && length == 0;
                filter.invoke(this);
            } while (length > 0);
            flushSkip(eos);
            flushOutput(eos);
            if (inLength > 0) {
                if (eos) {
                    throw new IllegalStateException("The filter didn't consume all its input");
                }
                if (this.preserve) {
                    compactBuffer();
                }
            } else {
                if (eos && !eosSignalled) {
                    invokeNext(inBuffer, inOffset, inLength, true, true);
                }
                if (inBuffer != null) {
                    if (!this.preserve) {
                        releaseBuffer(inBuffer);
                    }
                    inBuffer = null;
                    inOffset = 0;
                    inLength = 0;
                }
            }
        }
        
        private void setBuffer(byte[] buffer, int offset, int length, boolean preserve) {
            flushSkip(false);
            if (inBuffer != null) {
                if (inLength > 0) {
                    throw new IllegalStateException();
                }
                if (!this.preserve) {
                    releaseBuffer(inBuffer);
                }
            }
            inBuffer = buffer;
            inOffset = offset;
            inLength = length;
            this.preserve = preserve;
        }
        
        private int fillBuffer(byte[] buffer, int offset, int length) {
            if (!preserve && length <= inBuffer.length-inOffset-inLength) {
                System.arraycopy(buffer, offset, inBuffer, inOffset+inLength, length);
                inLength += length;
                return length;
            } else {
                compactBuffer();
                int c = Math.min(length, inBuffer.length-inOffset-inLength);
                System.arraycopy(buffer, offset, inBuffer, inLength, c);
                inLength += c;
                return c;
            }
        }
        
        private void compactBuffer() {
            flushSkip(false);
            byte[] src = inBuffer;
            if (preserve) {
                inBuffer = allocateBuffer();
                preserve = false;
            }
            System.arraycopy(src, inOffset, inBuffer, 0, inLength);
            inOffset = 0;
        }
        
        private void invokeNext(byte[] buffer, int offset, int length, boolean eos, boolean preserve) {
            if (eos && eosSignalled) {
                throw new IllegalStateException();
            }
            StreamImpl next = nested == null ? this.next : nested;
            if (next != null) {
                next.invoke(buffer, offset, length, eos, preserve);
            } else if (!preserve) {
                releaseBuffer(buffer);
            }
            eosSignalled = eos;
        }

        private void flushSkip(boolean eos) {
            if (skipLength > 0) {
                if (outLength > 0) {
                    throw new IllegalStateException();
                }
                if (inLength == 0 && !preserve) {
                    invokeNext(inBuffer, skipOffset, skipLength, eos, false);
                    inBuffer = null;
                    inOffset = 0;
                } else {
                    invokeNext(inBuffer, skipOffset, skipLength, eos, true);
                }
                skipLength = 0;
            }
        }
        
        private void flushOutput(boolean eos) {
            if (outLength > 0) {
                if (skipLength > 0) {
                    throw new IllegalStateException();
                }
                invokeNext(outBuffer, 0, outLength, eos, false);
                outBuffer = null;
                outLength = 0;
            }
        }
        
        public int available() {
            return inLength;
        }

        public boolean isEndOfStream() {
            return lastBuffer && inLength == 0;
        }

        public int get() {
            return get(0);
        }

        public int get(int lookahead) {
            if (lookahead < 0) {
                throw new ArrayIndexOutOfBoundsException();
            } else if (lookahead >= inLength) {
                if (lastBuffer) {
                    return -1;
                } else {
                    throw new ArrayIndexOutOfBoundsException();
                }
            } else {
                return (int)inBuffer[inOffset+lookahead] & 0xFF;
            }
        }
        
        public int read(byte[] buffer, int offset, int length) {
            int c = Math.min(length, inLength);
            System.arraycopy(inBuffer, inOffset, buffer, offset, c);
            return c;
        }

        public int read(ByteBuffer buffer) {
            int c = Math.min(buffer.remaining(), inLength);
            buffer.put(inBuffer, inOffset, c);
            return c;
        }

        public void readAll(OutputStream out) throws IOException {
            out.write(inBuffer, inOffset, inLength);
        }

        public byte discard() {
            byte b = inBuffer[inOffset];
            discard(1);
            return b;
        }

        public void discard(int len) {
            if (len < 0 || len > inLength) {
                throw new ArrayIndexOutOfBoundsException();
            }
            inOffset += len;
            inLength -= len;
        }

        private void prepareOutput() {
            flushSkip(false);
            if (outLength > 0 && outLength == outBuffer.length) {
                flushOutput(false);
            }
            if (outBuffer == null) {
                outBuffer = allocateBuffer();
            }
        }

        public void insert(byte b) {
            prepareOutput();
            outBuffer[outLength++] = b;
        }
        
        public void insert(byte[] buffer, int offset, int length) {
            flushSkip(false);
            flushOutput(false);
            invokeNext(buffer, offset, length, false, true);
        }

        public void insert(ByteBuffer buffer) {
            while (buffer.hasRemaining()) {
                prepareOutput();
                int c = Math.min(outBuffer.length-outLength, buffer.remaining());
                buffer.get(outBuffer, outLength, c);
                outLength += c;
            }
        }

        public byte skip() {
            byte b = inBuffer[inOffset];
            skip(1);
            return b;
        }

        public void skip(int len) {
            if (len < 0 || len > inLength) {
                throw new ArrayIndexOutOfBoundsException();
            }
            flushOutput(false);
            if (skipLength == 0) {
                skipOffset = inOffset;
                skipLength = len;
            } else if (skipOffset+skipLength != inOffset) {
                // This means that some bytes have been discarded after
                // the last skip operation.
                flushSkip(false);
                skipOffset = inOffset;
                skipLength = len;
            } else {
                skipLength += len;
            }
            inOffset += len;
            inLength -= len;
        }

        public void skipAll() {
            skip(inLength);
        }

        public void pushFilter(StreamFilter filter) {
            flushSkip(false);
            flushOutput(false);
            StreamImpl stream = new StreamImpl(filter);
            stream.setNext(nested == null ? next : nested);
            nested = stream;
        }

        public void popFilter() {
            flushSkip(false);
            flushOutput(false);
            nested = nested.next == next ? null : nested.next;
        }
    }
    
    private class OutputStreamImpl extends OutputStream {
        public OutputStreamImpl() {}
        
        public void write(int b) throws IOException {
            write(new byte[] { (byte)b });
        }

        public void write(byte[] b, int off, int len) throws IOException {
            first.invoke(b, off, len, false, true);
        }

        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        public void close() throws IOException {
            first.invoke(new byte[0], 0, 0, true, false);
        }
    }
    
    private final int bufferSize;
    private final LinkedList buffers = new LinkedList();
    private StreamImpl first;
    private StreamImpl last;
    
    public Pipeline(int bufferSize) {
        this.bufferSize = bufferSize;
    }
    
    public Pipeline() {
        this(4096);
    }
    
    byte[] allocateBuffer() {
        return buffers.isEmpty() ? new byte[bufferSize] : (byte[])buffers.removeFirst();
    }
    
    void releaseBuffer(byte[] buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        buffers.add(buffer);
    }
    
    public void addFilter(StreamFilter filter) {
        StreamImpl node = new StreamImpl(filter);
        if (first == null) {
            first = node;
        }
        if (last != null) {
            last.setNext(node);
        }
        last = node;
    }
    
    public int readFrom(InputStream in) throws IOException {
        byte[] buffer = allocateBuffer();
        int read = in.read(buffer);
        if (read == -1) {
            first.invoke(buffer, 0, 0, true, false);
        } else {
            first.invoke(buffer, 0, read, false, false);
        }
        return read;
    }
    
    public void close() {
        first.invoke(allocateBuffer(), 0, 0, true, false);
    }
    
    public OutputStream getOutputStream() {
        return new OutputStreamImpl();
    }
}
