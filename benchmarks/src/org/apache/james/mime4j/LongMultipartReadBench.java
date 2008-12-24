package org.apache.james.mime4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.james.mime4j.parser.MimeTokenStream;

public class LongMultipartReadBench {

    public static void main(String[] args) throws Exception {
        
        ClassLoader cl = LongMultipartReadBench.class.getClassLoader();
        
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        InputStream instream = cl.getResourceAsStream("long-multipart.msg");
        if (instream == null) {
            System.out.println("Test message not found");
            return;
        }
        try {
            int l;
            byte[] tmp = new byte[2048];
            while ((l = instream.read(tmp)) != -1) {
                outstream.write(tmp, 0, l);
            }
        } finally {
            instream.close();
        }

        byte[] content = outstream.toByteArray();

        int reps = 25000;
        if (args.length > 0) {
            reps = Integer.parseInt(args[0]);
        }
        
        System.out.println("Multipart message read.");
        System.out.println("No of repetitions: " + reps);
        System.out.println("Content length: " + content.length);
        System.out.println("----------------------------");
        
        MimeTokenStream stream = new MimeTokenStream();
        long start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            stream.parse(new ByteArrayInputStream(content));
            for (int state = stream.getState(); 
                state != MimeTokenStream.T_END_OF_STREAM; 
                state = stream.next()) {
            }
        }
        long finish = System.currentTimeMillis();
        System.out.println("Execution time: " 
                + ((double)(finish - start) / 1000) + " ms" );
    }
    
}
