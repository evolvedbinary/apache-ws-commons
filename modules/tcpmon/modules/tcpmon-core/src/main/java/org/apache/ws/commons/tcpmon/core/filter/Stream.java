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
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Interface used by a filter to access the stream flowing through a pipeline.
 */
public interface Stream {
    /**
     * Get the number of bytes currently available in the stream.
     * 
     * @return the number of available bytes
     */
    int available();
    
    /**
     * Check if the end of the stream has been reached.
     * 
     * @return true if the end of the stream has been reached
     */
    boolean isEndOfStream();
    
    /**
     * Get the byte at the current position in the stream.
     * Calling this method will not modify the current position in
     * the stream.
     * 
     * @return the byte at the current position in the stream
     *         or -1 if the end of the stream has been reached
     * @throws ArrayIndexOutOfBoundsException if the byte at the
     *         current position is not yet available. This is the
     *         case if {@link #available()} returns 0 and
     *         {@link #isEndOfStream()} is returns false.
     */
    int get();
    
    /**
     * Get the byte at a given distance from the current position in
     * the stream.
     * 
     * @param lookahead the distance from the current position
     * @return the byte at the given position, or -1 if the position
     *         is past the end of the stream
     * @throws ArrayIndexOutOfBoundsException if the byte at the
     *         given position is not yet available
     */
    int get(int lookahead);
    
    /**
     * Read data from the stream into a byte array, starting from the
     * current position in the stream.
     * Calling this method will not modify the current position in
     * the stream.
     * 
     * @param buffer the buffer into which the data is read
     * @param offset the start offset in array <code>buffer</code>
     *               at which the data is written
     * @param length the maximum number of bytes to read
     * @return the total number of bytes read into the buffer
     */
    int read(byte[] buffer, int offset, int length);
    
    /**
     * Read data from the stream into a byte buffer, starting from the
     * current position in the stream.
     * Calling this method will not modify the current position in
     * the stream.
     * The number of bytes read is only limited by the number of available
     * bytes in the stream and the remaining bytes in the buffer (as returned
     * by {@link ByteBuffer#remaining()}.
     * 
     * @param buffer the buffer into which the data is read
     * @return the total number of bytes read into the buffer
     */
    int read(ByteBuffer buffer);
    
    /**
     * Read all currently available data from the stream and
     * copy it to an {@link OutputStream} object.
     * Calling this method will not modify the current position in
     * the stream.
     * 
     * @param out the output stream to write the data to
     * @throws IOException if an I/O error occurred when writing
     *                     to the output stream
     */
    void readAll(OutputStream out) throws IOException;
    
    /**
     * Discard the byte at the current position in the stream.
     * This method increments the current position without copying
     * the byte to the next filter in the pipeline.
     * 
     * @return the byte at the current position in the stream
     */
    byte discard();
    
    /**
     * Discard a given number of bytes from the stream, starting
     * at the current position.
     * 
     * @param len the number of bytes to discard
     */
    void discard(int len);
    
    /**
     * Insert a byte at the current position in the stream.
     * The logical position after invocation of this method will
     * be just after the inserted byte, i.e. the inserted byte can't
     * be read, discarded or skipped.
     * 
     * @param b the byte to insert
     */
    void insert(byte b);
    
    /**
     * Insert a byte sequence at the current position in the stream.
     * The logical position after invocation of this method will
     * be just after the last inserted byte.
     * 
     * @param buffer a byte array containing the sequence to be inserted in the stream
     * @param offset the start offset in the byte array
     * @param length the number of bytes to insert
     */
    void insert(byte[] buffer, int offset, int length);
    
    /**
     * Insert the content of a byte buffer at the current position in the stream.
     * The logical position after invocation of this method will
     * be just after the last inserted byte.
     * 
     * @param buffer the byte buffer containing the sequence to be inserted in the stream
     */
    void insert(ByteBuffer buffer);
    
    /**
     * Skip the byte at the current position in the stream.
     * This will increment the current position and copy the byte
     * to the next filter.
     * 
     * @return the byte at the current position in the stream 
     */
    byte skip();
    
    /**
     * Skip a given number of bytes in the stream, starting
     * from the current position.
     * 
     * @param len the number of bytes to skip
     */
    void skip(int len);
    
    /**
     * Skip all the bytes currently available in the stream.
     * The instruction <code>s.skipAll()</code> is equivalent to
     * <code>s.skip(s.available())</code>.
     */
    void skipAll();
    
    void pushFilter(StreamFilter filter);
    
    void popFilter();
}
