package com.github.jtcressy.kattis.textprocessor;

import org.junit.Test;

import javax.xml.soap.Text;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class TextProcessorTest {

    @Test
    public void handleInput() {
        {
            String testcase = "acat\n" +
                    "2 3\n" +
                    "1\n" +
                    "2";
            InputStream stream = new ByteArrayInputStream(testcase.getBytes(StandardCharsets.UTF_8));
            String expected = "5\n" +
                    "6\n";
            String result = TextProcessor.handleInput(stream);
            assertEquals(expected, result);
        }
    }
    @Test
    public void timingTest() {
        {
            int caseSize = 100000;
            char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
            StringBuilder sb = new StringBuilder(caseSize);
            Random random = new Random();
            for (int i = 0; i < caseSize; i++) {
                sb.append(chars[random.nextInt(chars.length)]);
            }
            int Q = 1;
            int W = 1000;
            int I = W;
            List<Integer> questions = IntStream.rangeClosed(1, I).boxed().collect(Collectors.toList());
            sb.append("\n"+Q+" "+W+"\n");
            for (int item : questions) {
                sb.append(item + "\n");
            }
            String testcase = sb.toString();
            System.out.println("Test case: \n" + testcase + "\n");
            InputStream stream = new ByteArrayInputStream(testcase.getBytes(StandardCharsets.UTF_8));
            long startTime = System.nanoTime();
            String result = TextProcessor.handleInput(stream);
            long elapsedTime = System.nanoTime() - startTime;
            double seconds = (double)elapsedTime / 1000000000.0;
            System.out.println("Test finished in " + seconds + " seconds");
            assertTrue(seconds < 1);
        }
    }
}