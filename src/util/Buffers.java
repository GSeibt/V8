package util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Contains static convenience methods for allocating <code>FloatBuffer</code>, <code>IntBuffer</code> and
 * <code>ByteBuffer</code> instances.
 */
public class Buffers {

    /**
     * Allocates a new <code>IntBuffer</code> of the given size.
     *
     * @param size
     *         the size in ints
     *
     * @return the allocated <code>IntBuffer</code>
     */
    public static IntBuffer allocateIntBuffer(int size) {
        return allocateByteBuffer(size * 4).asIntBuffer();
    }

    /**
     * Allocates a new <code>FloatBuffer</code> of the given size.
     *
     * @param size
     *         the size in floats
     *
     * @return the allocated <code>FloatBuffer</code>
     */
    public static FloatBuffer allocateFloatBuffer(int size) {
        return allocateByteBuffer(size * 4).asFloatBuffer();
    }

    /**
     * Allocates a new <code>ByteBuffer</code> of the given size.
     *
     * @param size
     *         the size in bytes
     *
     * @return the allocated <code>ByteBuffer</code>
     */
    public static ByteBuffer allocateByteBuffer(int size) {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }
}
