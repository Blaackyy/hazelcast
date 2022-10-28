package com.hazelcast.internal.tpc.util;

import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class})
public class UtilTest {

    @Test
    public void test_put_exactlyEnoughSpace(){
        ByteBuffer src = ByteBuffer.allocate(8);
        src.putInt(1);
        src.putInt(2);
        src.flip();
        int srcPos = src.position();
        int srcLimit = src.limit();

        ByteBuffer dst  = ByteBuffer.allocate(8);
        Util.put(dst,src);
        dst.flip();
        assertEquals(8, dst.remaining());
        assertEquals(1, dst.getInt());
        assertEquals(2, dst.getInt());

        assertEquals(srcPos + 8, src.position());
        assertEquals(srcLimit, src.limit());
    }

    @Test
    public void test_put_moreThanEnoughSpace(){
        ByteBuffer src = ByteBuffer.allocate(8);
        src.putInt(1);
        src.putInt(2);
        src.flip();
        int srcPos = src.position();
        int srcLimit = src.limit();

        ByteBuffer dst  = ByteBuffer.allocate(12);
        Util.put(dst,src);
        dst.flip();
        assertEquals(8, dst.remaining());
        assertEquals(1, dst.getInt());
        assertEquals(2, dst.getInt());
        assertEquals(srcPos + 8, src.position());
        assertEquals(srcLimit, src.limit());
    }

    @Test
    public void test_put_notEnoughSpace(){
        ByteBuffer src = ByteBuffer.allocate(8);
        src.putInt(1);
        src.putInt(2);
        src.flip();
        int srcPos = src.position();
        int srcLimit = src.limit();

        ByteBuffer dst  = ByteBuffer.allocate(4);
        Util.put(dst,src);
        dst.flip();
        assertEquals(4, dst.remaining());
        assertEquals(1, dst.getInt());

        assertEquals(srcPos + 4, src.position());
        assertEquals(srcLimit, src.limit());
    }
}