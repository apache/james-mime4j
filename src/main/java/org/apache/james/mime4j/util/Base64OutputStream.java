/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.mime4j.util;

import java.io.OutputStream;
import java.io.Writer;
import java.io.IOException;

/**
 * This class has been copied from Apache MyFaces Trinidad 1.2.8
 * 
 * An OutputStream that encodes data in a base64 representation.
 * It takes a Writer as its single argument to its constructor and all
 * bytes written to the stream are correspondingly converted into Base64
 * and written out to the provided writer.
 */
public class Base64OutputStream extends OutputStream
{
  public Base64OutputStream(Writer out)
  {
    _out = out;
    _numLeftoverBytes = 0;
    _leftoverBytes = new byte[2];
  }
  
  /**
   *  Takes a byte writes it out to the writer
   * 
   * @param b   a byte
   */
  public final void write(final int b) throws IOException
  {
    _single[0] = (byte) b;
    this.write(_single, 0, 1);
  } 
  
  /**
   * Writes len bytes from the specified byte array starting at offset off 
   * to this output stream. The general contract for write(b, off, len) is 
   * that some of the bytes in the array b are written to the output stream 
   * in order; element b[off] is the first byte written and b[off+len-1] is 
   * the last byte written by this operation.
   * 
   * The write method of OutputStream calls the write method of one argument 
   * on each of the bytes to be written out. Subclasses are encouraged to 
   * override this method and provide a more efficient implementation.
   * 
   * If b is null, a NullPointerException is thrown.
   * 
   * If off is negative, or len is negative, or off+len is greater than the 
   * length of the array b, then an IndexOutOfBoundsException is thrown. 
   * 
   * 
   * @param b        the data
   * @param off      the start offset in the data
   * @param len      the number of bytes to read
   */
  public final void write(final byte[] b, final int off, final int len) 
                                      throws IOException, NullPointerException
  {
    if (b==null) 
    {
      throw new NullPointerException("BYTE_ARRAY_CANNOT_BE_NULL");
    }
      
    if (off<0 || len<0 || off+len>b.length) 
    {
      throw new IndexOutOfBoundsException("ACTUAL_LENGTH_OFFSET: " + b.length + " | " + off + " | " + len);
    }
    
    int lengthToProcess = len;
    int index = off;
    
    // base case 1: if only processing one byte from byte array
    if (lengthToProcess==1) 
    {
      if (_numLeftoverBytes==0) 
      {
        // remember this byte for next call to write
        _numLeftoverBytes = 1;
        _leftoverBytes[0] = b[index];
      }
      else if (_numLeftoverBytes==1) 
      {
        // remember this byte for next call to write
        _numLeftoverBytes = 2;
        _leftoverBytes[1] = b[index];
      }
      else if (_numLeftoverBytes==2) 
      {
        // this one byte is enough to complete a triplet
        // so convert triplet into Base64
        _writeBase64(_leftoverBytes[0], _leftoverBytes[1], b[index]);
        _numLeftoverBytes=0;
      }
      return;
    }  //end if (lengthToProcess==1)
    
    // base case 2: if only processing two bytes from byte array
    if (lengthToProcess==2) 
    {
      if (_numLeftoverBytes==0) 
      {
        // not enough to process a triplet, so remember these two bytes 
        // for next call to write
        _numLeftoverBytes = 2;
        _leftoverBytes[0] = b[index];
        _leftoverBytes[1] = b[index+1];
      }
      else if (_numLeftoverBytes==1) 
      {
        // these two bytes form triplet combined with the leftover byte
        _writeBase64(_leftoverBytes[0], b[index], b[index+1]);
        _numLeftoverBytes = 0;
      }
      else if (_numLeftoverBytes==2) 
      {
        // two leftover bytes and one new byte form a triplet
        // the second new byte is remembered for next call to write
        _writeBase64(_leftoverBytes[0], _leftoverBytes[1], b[index]);
        _leftoverBytes[0] = b[index+1];
        _numLeftoverBytes = 1;
      }
      return;
    }  // end if (lengthToProcess==2)
    
    // case involving looping
    if (lengthToProcess>2) 
    {
      if (_numLeftoverBytes==1) 
      {
        _writeBase64(_leftoverBytes[0], b[index], b[index+1]);
        _numLeftoverBytes = 0;
        lengthToProcess -= 2;
        index += 2;
        // proceed with loop
      }      
      else if (_numLeftoverBytes==2) 
      {
        _writeBase64(_leftoverBytes[0], _leftoverBytes[1], b[index]);
        _numLeftoverBytes = 0;
        lengthToProcess -= 1;
        index += 1;
        // proceed with loop
      }

      _processArray(b, index, lengthToProcess);
    }

  } //end write(byte[], int off, int len)
  
  public final void flush() throws IOException 
  {
    _out.flush();
  }

  /**
   * Call this method to indicate end of data stream 
   * and to append any padding characters if necessary.  This method should be 
   * called only if there will be no subsequent calls to a write method.  
   * Subsequent calls to the write method will result in incorrect encoding.
   * 
   * @deprecated use the close() method instead.
   */
  public void finish() throws IOException
  {
    close();
  }
  
  /**
   * Call this method to indicate end of data stream 
   * and to append any padding characters if necessary.  This method should be 
   * called only if there will be no subsequent calls to a write method.  
   * Subsequent calls to the write method will result in incorrect encoding.
   * 
   */
  public final void close() throws IOException
  {
    if (_numLeftoverBytes==1) 
    {
      // grab the one byte from the leftover array
      byte b1 = _leftoverBytes[0];
      
      // convert to two base 64 chars
      int c1, c2;
      c1 = (b1>>2)&0x3f;
      c2 = (b1<<4)&0x3f;
      
      char[] encodedChars = _fourChars;
      
      encodedChars[0] = _encode(c1);
      encodedChars[1] = _encode(c2);
      // append two padding characters
      encodedChars[2] = '=';
      encodedChars[3] = '=';
      
      _out.write(encodedChars);
    } 
    else if (_numLeftoverBytes==2)
    {
      // grab the two bytes from the leftovers array
      byte b1, b2;
      b1 = _leftoverBytes[0];
      b2 = _leftoverBytes[1];
      
      // convert the two bytes into three base64 chars
      int c1, c2, c3;
      c1 = (b1>>2)&0x3f;
      c2 = (b1<<4 | ((b2>>4)&0x0f))&0x3f;
      c3 = (b2<<2)&0x3f;
      
      char[] encodedChars = _fourChars;
      
      encodedChars[0] = _encode(c1);
      encodedChars[1] = _encode(c2);
      encodedChars[2] = _encode(c3);  
      //append one padding character
      encodedChars[3] = '=';
      
      _out.write(encodedChars);
    } 
    _out.close();
  }
  
  
  /**
   * Encodes three bytes in base64 representation and writes the corresponding 
   * four base64 characters to the output writer.
   * 
   * @param b1  the first byte
   * @param b2  the second byte
   * @param b3  the third byte
   */
  private final void _writeBase64(final byte b1, final byte b2, final byte b3) throws IOException
  {
    int c1, c2, c3, c4;
    char[] encodedChars = _fourChars;
    
    c1 = (b1>>2)&0x3f;          // b1.high6
    c2 = (b1<<4 | ((b2>>4)&0x0f) )&0x3f;  // b1.low2 + b2.high4
    c3 = (b2<<2 | ((b3>>6)&0x03) )&0x3f;  // b2.low4 + b3.high2
    c4 = b3&0x3f;               // b3.low6
    
    encodedChars[0] = _encode(c1);
    encodedChars[1] = _encode(c2);
    encodedChars[2] = _encode(c3);
    encodedChars[3] = _encode(c4);
    
    // write array of chars to writer
    _out.write(encodedChars);    
  }
  
  /**
   * Writes lengthToProcess number of bytes from byte array to writer 
   * in base64 beginning with startIndex. Assumes all leftover bytes from 
   * previous calls to write have been dealt with.  
   * 
   * @param b               the data
   * @param startIndex      the start offset in the data
   * @param lengthToProcess the number of bytes to read
   */
  private final void _processArray(byte[] b, int startIndex, int lengthToProcess) 
                                                        throws IOException
  {
    int index = startIndex;
  
    // loop through remaining length of array
    while(lengthToProcess>0) {
      // base case: only one byte
      if (lengthToProcess==1) 
      {        
        // save this byte for next call to write
        _numLeftoverBytes = 1;
        _leftoverBytes[0] = b[index];
        return;
      }
      // base case: only two bytes
      else if (lengthToProcess==2) 
      {     
        // save these two bytes for next call to write
        _numLeftoverBytes = 2;
        _leftoverBytes[0] = b[index];
        _leftoverBytes[1] = b[index+1]; 
        return;
      } 
      else 
      {
        // encode three bytes (24 bits) from input array
        _writeBase64(b[index],b[index+1],b[index+2]);
        
        // finally, make some progress in loop
        lengthToProcess -= 3;
        index +=3;
       }
    } //end while
  }
  
  

  /**
   * Encodes a six-bit pattern into a base-64 character.
   * 
   * @param c an integer whose lower 6 bits contain the base64 representation
   *          all other bits should be zero
   */
  private static final char _encode(final int c) 
  {
    if (c < 26)
      return (char)('A' + c);
    if (c < 52)
      return (char)('a' + (c-26));
    if (c < 62)
      return (char)('0' + (c-52));
    if (c == 62)
      return '+';
    if (c == 63)
      return '/';
      
    throw new AssertionError("Invalid B64 character code:"+c);
  }

  
  /** stores leftover bytes from previous call to write method **/
  private final byte[]      _leftoverBytes;
  
  /** indicates the number of bytes that were leftover after the last triplet 
   * was formed in the last call to write method  **/
  private int         _numLeftoverBytes;
  
  // cached four-character array
  private final char[]      _fourChars = new char[4];
  // cached single-byte array
  private final byte[]      _single = new byte[1];

  // Writer that will receive all completed character output
  private final Writer      _out;  
  
}
