@file:Suppress("KotlinJniMissingFunction")

package moe.fuqiuluo.mamu.driver

object WuwaDriver {
    init {
        System.loadLibrary("mamu_core")
    }

    val loaded: Boolean
        get() = nativeIsLoaded()

    val currentBindPid: Int
        get() = nativeGetCurrentBindPid()

    val isProcessBound: Boolean
        get() = nativeIsProcessBound()

    fun setMemoryAccessMode(mode: Int) = nativeSetMemoryAccessMode(mode)

    fun isProcessAlive(pid: Int) = nativeIsProcessAlive(pid)

    fun listProcesses() = nativeGetProcessList()

    fun getProcessInfo(pid: Int) = nativeGetProcessInfo(pid)

    fun listProcessesWithInfo(): Array<CProcInfo> {
        return nativeGetProcessListWithInfo()
    }

    fun bindProcess(pid: Int) = nativeBindProcess(pid)

    fun unbindProcess() = nativeUnbindProcess()

    fun queryMemRegions(pid: Int = currentBindPid) = nativeQueryMemRegions(pid)

    fun setDriverFd(fd: Int): Boolean = nativeSetDriverFd(fd)

    /**
     * 统一的内存读取方法，使用当前配置的 access_mode
     * @param addr 要读取的虚拟地址
     * @param size 读取大小
     * @return 读取的字节数组，失败返回null
     */
    fun readMemory(addr: Long, size: Int): ByteArray? = nativeReadMemory(addr, size)

    /**
     * 统一的内存写入方法，使用当前配置的 access_mode
     * @param addr 要写入的虚拟地址
     * @param data 要写入的数据
     * @return 写入是否成功
     */
    fun writeMemory(addr: Long, data: ByteArray): Boolean = nativeWriteMemory(addr, data)

    private external fun nativeIsLoaded(): Boolean
    private external fun nativeSetDriverFd(fd: Int): Boolean
    private external fun nativeSetMemoryAccessMode(mode: Int)
    private external fun nativeIsProcessAlive(pid: Int): Boolean
    private external fun nativeGetProcessList(): IntArray
    private external fun nativeGetProcessInfo(pid: Int): CProcInfo
    private external fun nativeGetProcessListWithInfo(): Array<CProcInfo>
    private external fun nativeBindProcess(pid: Int): Boolean
    private external fun nativeIsProcessBound(): Boolean
    private external fun nativeUnbindProcess(): Boolean
    private external fun nativeGetCurrentBindPid(): Int
    private external fun nativeQueryMemRegions(pid: Int): Array<MemRegionEntry>
    private external fun nativeReadMemory(addr: Long, size: Int): ByteArray?
    private external fun nativeWriteMemory(addr: Long, data: ByteArray): Boolean
}