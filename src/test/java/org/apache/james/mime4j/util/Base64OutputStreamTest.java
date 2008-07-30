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

import java.io.IOException;
import java.io.StringWriter;
import java.io.BufferedWriter;
import java.util.zip.GZIPOutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This class has been copied from Apache MyFaces Trinidad 1.2.8
 *
 * Unit tests for Base64OutputStream.
 */
public class Base64OutputStreamTest extends TestCase 
{   
 /**
   * Creates a new Base64OutputStreamTest.
   * 
   * @param testName  the unit test name
   */
  public Base64OutputStreamTest(String testName)
  {
    super(testName);
  }
  
  protected void setUp() throws Exception
  {
    super.setUp();
  }
  
  protected void tearDown() throws Exception
  {
    super.tearDown();
  }
  
  public static Test suite()
  {
    return new TestSuite(Base64OutputStreamTest.class);
  }
  
  /**
   * Tests decoding of stream that contains no trailing padding characters.
   */ 	
  public void testNoPaddingChar() throws IOException
  {
	 	
    String	str = "abcdefghijklmnopqrstuvwxBase64 Encoding is a popular way to convert the 8bit and the binary to the 7bit for network trans using Socket, and a security method to handle text or file, often used in Authentical Login and Mail Attachment, also stored in text file or database. Most SMTP server will handle the login UserName and Password in this way. 1";
    String str_encoded = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4QmFzZTY0IEVuY29kaW5nIGlzIGEgcG9wdWxhciB3YXkgdG8gY29udmVydCB0aGUgOGJpdCBhbmQgdGhlIGJpbmFyeSB0byB0aGUgN2JpdCBmb3IgbmV0d29yayB0cmFucyB1c2luZyBTb2NrZXQsIGFuZCBhIHNlY3VyaXR5IG1ldGhvZCB0byBoYW5kbGUgdGV4dCBvciBmaWxlLCBvZnRlbiB1c2VkIGluIEF1dGhlbnRpY2FsIExvZ2luIGFuZCBNYWlsIEF0dGFjaG1lbnQsIGFsc28gc3RvcmVkIGluIHRleHQgZmlsZSBvciBkYXRhYmFzZS4gTW9zdCBTTVRQIHNlcnZlciB3aWxsIGhhbmRsZSB0aGUgbG9naW4gVXNlck5hbWUgYW5kIFBhc3N3b3JkIGluIHRoaXMgd2F5LiAx";
   
    _testWriteChar(str, str_encoded);
    
    _testWriteArray(str, str_encoded);    	
  }
 	
  /**
   * Tests decoding of stream that contains exactly one trailing padding character.
   */ 	
  public void testOnePaddingChar() throws IOException
  {
    String str = "Base64 Encoding is a popular way to convert the 8bit and the binary to the 7bit for network trans using Socket, and a security method to handle text or file, often used in Authentical Login and Mail Attachment, also stored in text file or database. Most SMTP server will handle the login UserName and Password in this way.9";
    String str_encoded = "QmFzZTY0IEVuY29kaW5nIGlzIGEgcG9wdWxhciB3YXkgdG8gY29udmVydCB0aGUgOGJpdCBhbmQgdGhlIGJpbmFyeSB0byB0aGUgN2JpdCBmb3IgbmV0d29yayB0cmFucyB1c2luZyBTb2NrZXQsIGFuZCBhIHNlY3VyaXR5IG1ldGhvZCB0byBoYW5kbGUgdGV4dCBvciBmaWxlLCBvZnRlbiB1c2VkIGluIEF1dGhlbnRpY2FsIExvZ2luIGFuZCBNYWlsIEF0dGFjaG1lbnQsIGFsc28gc3RvcmVkIGluIHRleHQgZmlsZSBvciBkYXRhYmFzZS4gTW9zdCBTTVRQIHNlcnZlciB3aWxsIGhhbmRsZSB0aGUgbG9naW4gVXNlck5hbWUgYW5kIFBhc3N3b3JkIGluIHRoaXMgd2F5Ljk="; 
    
    _testWriteChar(str, str_encoded);
    
    _testWriteArray(str, str_encoded);    	
    
  }
 	
  /**
   * Tests decoding of stream that contains exactly two trailing padding characters.
   */ 	 	
  public void testTwoPaddingChars() throws IOException
  {
    String str = "Base64 Encoding is a popular way to convert the 8bit and the binary to the 7bit for network trans using Socket, and a security method to handle text or file, often used in Authentical Login and Mail Attachment, also stored in text file or database. Most SMTP server will handle the login UserName and Password in this way.";
    String str_encoded = "QmFzZTY0IEVuY29kaW5nIGlzIGEgcG9wdWxhciB3YXkgdG8gY29udmVydCB0aGUgOGJpdCBhbmQgdGhlIGJpbmFyeSB0byB0aGUgN2JpdCBmb3IgbmV0d29yayB0cmFucyB1c2luZyBTb2NrZXQsIGFuZCBhIHNlY3VyaXR5IG1ldGhvZCB0byBoYW5kbGUgdGV4dCBvciBmaWxlLCBvZnRlbiB1c2VkIGluIEF1dGhlbnRpY2FsIExvZ2luIGFuZCBNYWlsIEF0dGFjaG1lbnQsIGFsc28gc3RvcmVkIGluIHRleHQgZmlsZSBvciBkYXRhYmFzZS4gTW9zdCBTTVRQIHNlcnZlciB3aWxsIGhhbmRsZSB0aGUgbG9naW4gVXNlck5hbWUgYW5kIFBhc3N3b3JkIGluIHRoaXMgd2F5Lg==";
    
    _testWriteChar(str, str_encoded);
    
    _testWriteArray(str, str_encoded);    	
    
  }	
  
  /**
   * Tests decoding of stream by writing out to stream in different intervals to test whether
   * whehther leftover data is persisted across calls to write
   */ 	 	
  public void testMultipleWrites() throws IOException
  {
    String str = "Base64 Encoding is a popular way to convert the 8bit and the binary to the 7bit for network trans using Socket, and a security method to handle text or file, often used in Authentical Login and Mail Attachment, also stored in text file or database. Most SMTP server will handle the login UserName and Password in this way.";
    String str_encoded = "QmFzZTY0IEVuY29kaW5nIGlzIGEgcG9wdWxhciB3YXkgdG8gY29udmVydCB0aGUgOGJpdCBhbmQgdGhlIGJpbmFyeSB0byB0aGUgN2JpdCBmb3IgbmV0d29yayB0cmFucyB1c2luZyBTb2NrZXQsIGFuZCBhIHNlY3VyaXR5IG1ldGhvZCB0byBoYW5kbGUgdGV4dCBvciBmaWxlLCBvZnRlbiB1c2VkIGluIEF1dGhlbnRpY2FsIExvZ2luIGFuZCBNYWlsIEF0dGFjaG1lbnQsIGFsc28gc3RvcmVkIGluIHRleHQgZmlsZSBvciBkYXRhYmFzZS4gTW9zdCBTTVRQIHNlcnZlciB3aWxsIGhhbmRsZSB0aGUgbG9naW4gVXNlck5hbWUgYW5kIFBhc3N3b3JkIGluIHRoaXMgd2F5Lg==";
    
    // create a Base64OutputStream 
    StringWriter strwriter = new StringWriter(); 
    BufferedWriter buffwriter = new BufferedWriter(strwriter);
    Base64OutputStream b64_out = new Base64OutputStream(buffwriter);
    
    byte[] b = str.getBytes();
    
    // write the bytes in different lengths to test whether leftover
    // data is persisted across calls to write
    b64_out.write(b, 0, 33);
    b64_out.write(b, 33, 1);
    b64_out.write(b, 34, 1);
    b64_out.write(b, 35, 2);
    b64_out.write(b, 37, 3);
    b64_out.write(b, 40, 3);
    b64_out.write(b, 43, 3);
    b64_out.write(b, 46, 5);
    b64_out.write(b, 51, 4);
    b64_out.write(b, 55, 5);
    b64_out.write(b, 60, 6);
    b64_out.write(b, 66, 10);
    b64_out.write(b, 76, 51);
    b64_out.write(b, 127, 150);
    b64_out.write(b, 277, 22);
    b64_out.write(b, 299, 21);
    b64_out.write(b, 320, 2);
    
    // remember to add padding characters (if necessary)
    b64_out.close();
    
    // compare the contents of the outputstream with the expected encoded string
    assertEquals(strwriter.toString(), str_encoded);	
  }	
  
  
  /**
   * Testing writing binary data whose byte values range from -128 to 128.
   * We create such data by GZIP-ing the string before writing out to the Base64OutputStream.
   * 
   */ 	 	
  public void testWriteBinaryData() throws IOException
  {
    String str = "Base64 Encoding is a popular way to convert the 8bit and the binary to the 7bit for network trans using Socket, and a security method to handle text or file, often used in Authentical Login and Mail Attachment, also stored in text file or database. Most SMTP server will handle the login UserName and Password in this way.";
    String str_encoded = "H4sIAAAAAAAAAD2QQW7DMAwEv7IPCHIK2l4ToLe6CJD2AbREx0JkMpDouvl9KQXokVxylssTVX454F2CxiRXpArCXe9rpoKNHjBFUPnhYrCZ8TYmA0nsxZiESh9p1WuTJi0Qtk3LDVZIKtbauBcNN7ZdXyVUDmtJ9sDCNmtshNmVzDD+NThjSpl30MlYnMARSXBc3UYsBcr40Kt3Gm2glHE0ozAvrrpFropqWp5bndhwDRvJaPTIewxaDZfh6+zHFI+HLeX8f4XHyd3h29VPWrhbnalWT/bEzv4qf9D+DzA2UNlCAQAA";
    
    byte[] bytes = str.getBytes();
    
    StringWriter strwriter = new StringWriter(); 
    BufferedWriter buffwriter = new BufferedWriter(strwriter);
    Base64OutputStream b64_out = new Base64OutputStream(buffwriter);
    
    GZIPOutputStream zip = new GZIPOutputStream(b64_out);
    
    zip.write(bytes, 0, bytes.length);
    zip.finish();
    buffwriter.flush();
    b64_out.close();
    
    assertEquals(str_encoded, strwriter.toString());
    
  }
  
  
  ////////// private	methods ////////////
  
  /**
   *
   *	Writes each individual char from str to a Base64OutputStream and compares
   *	the resulting output stream contents with the expected encoded string.  
   *
   *	@param	str			the decoded string
   *	@param	str_encoded	the encoded string
   *
   **/ 	
  
  private void _testWriteChar(String str, String str_encoded) throws IOException
  {
    // create a Base64OutputStream 
    StringWriter strwriter = new StringWriter(); 
    BufferedWriter buffwriter = new BufferedWriter(strwriter);
    Base64OutputStream b64_out = new Base64OutputStream(buffwriter);
    
    // write out each char in str to the stream
    for (int i = 0; i<str.length(); i++) 
    {
      b64_out.write(str.charAt(i));
    }
    // remember to add padding characters (if necessary)
    b64_out.close();
        
    // compare the contents of the outputstream with the expected encoded string
    assertEquals(strwriter.toString(), str_encoded);	
  }
  
  /**
   *
   *	Writes str (after converting to an array of bytes) to a Base64OutputStream 
   *	and compares the resulting output stream contents with the expected encoded 
   *	string.  
   *
   *	@param	str			the decoded string
   *	@param	str_encoded	the encoded string
   *
   **/ 	
  private void _testWriteArray(String str, String str_encoded) throws IOException
  {	
    // create a Base64OutputStream 
    StringWriter strwriter = new StringWriter(); 
    BufferedWriter buffwriter = new BufferedWriter(strwriter);
    Base64OutputStream b64_out = new Base64OutputStream(buffwriter);
    
    // convert str into an array of bytes
    byte[] b = str.getBytes();
    
    // write out the array to the output stream
    b64_out.write(b, 0, b.length);
    // append padding chars if necessary
    b64_out.close();
    
    // 		System.out.println("testwriteArray,  expected encoding:" + str_encoded);    
    //       	System.out.println("testwriteArray, output of encoding:" + strwriter.toString());
    
    // compare the contents of the outputstream with the expected encoded string
    assertEquals(strwriter.toString(), str_encoded);
  }
} // end Base64OutputStreamTest class
