package com.rwq.testnetty;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testNio() {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(new File("news.txt"), "rws");
            FileChannel channel = randomAccessFile.getChannel();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}