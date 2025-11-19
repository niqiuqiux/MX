package moe.fuqiuluo.mamu.driver

import java.nio.ByteBuffer
import java.nio.ByteOrder

object MemoryOps {

    /**
     * 分配指定大小的内存
     * @param size 内存大小（字节）
     * @return 内存地址，失败返回0
     */
    fun alloc(size: Int): ULong {
        return nativeAlloc(size).toULong()
    }

    /**
     * 释放内存
     * @param address 内存地址
     * @param size 内存大小（字节）
     */
    fun free(address: ULong, size: Int) {
        nativeFree(address.toLong(), size)
    }

    /**
     * 读取内存字节
     * @param address 内存地址
     * @param size 读取大小（字节）
     * @return 字节数组
     */
    fun read(address: ULong, size: Int): ByteArray {
        return nativeRead(address.toLong(), size)
    }

    /**
     * 写入内存字节
     * @param address 内存地址
     * @param data 要写入的字节数组
     */
    fun write(address: ULong, data: ByteArray) {
        nativeWrite(address.toLong(), data)
    }

    /**
     * 读取Int值（小端序）
     */
    fun readInt(address: ULong): Int {
        val bytes = read(address, 4)
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).int
    }

    /**
     * 写入Int值（小端序）
     */
    fun writeInt(address: ULong, value: Int) {
        val bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
        write(address, bytes)
    }

    /**
     * 获取当前进程ID
     * @return 进程ID
     */
    fun getPid(): Int {
        return nativeGetPid()
    }

    // Native methods
    private external fun nativeAlloc(size: Int): Long
    private external fun nativeFree(address: Long, size: Int)
    private external fun nativeRead(address: Long, size: Int): ByteArray
    private external fun nativeWrite(address: Long, data: ByteArray)
    private external fun nativeGetPid(): Int
}