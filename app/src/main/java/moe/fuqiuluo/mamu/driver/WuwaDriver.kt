@file:Suppress("KotlinJniMissingFunction")

package moe.fuqiuluo.mamu.driver

import moe.fuqiuluo.mamu.data.model.DriverInfo
import moe.fuqiuluo.mamu.data.model.DriverInstallResult

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

    fun queryMemRegionsWithRetry(
        pid: Int = currentBindPid,
        retryCount: Int = 3
    ): Array<MemRegionEntry> {
        repeat(retryCount) {
            runCatching {
                queryMemRegions(pid)
            }.onSuccess {
                return it
            }.onFailure {
                it.printStackTrace()
            }
        }
        throw RuntimeException("failed to queryMemRegions")
    }

    fun setDriverFd(fd: Int): Boolean = nativeSetDriverFd(fd)

    /**
     * 统一的内存读取方法，使用当前配置的 access_mode
     * @param addr 要读取的虚拟地址
     * @param size 读取大小
     * @return 读取的字节数组，失败返回null
     */
    fun readMemory(addr: Long, size: Int): ByteArray? = nativeReadMemory(addr, size)

    /**
     * 批量读取内存
     * @param addrs 要读取的地址数组
     * @param sizes 每个地址对应的读取大小
     * @return 读取的字节数组，失败的位置为null
     */
    fun batchReadMemory(addrs: LongArray, sizes: IntArray): Array<ByteArray?> =
        nativeBatchReadMemory(addrs, sizes)

    /**
     * 统一的内存写入方法，使用当前配置的 access_mode
     * @param addr 要写入的虚拟地址
     * @param data 要写入的数据
     * @return 写入是否成功
     */
    fun writeMemory(addr: Long, data: ByteArray): Boolean = nativeWriteMemory(addr, data)

    /**
     * 批量写入内存
     * @param addrs 要写入的地址数组
     * @param dataArray 每个地址对应的数据
     * @return 每个地址写入是否成功的结果数组
     */
    fun batchWriteMemory(addrs: LongArray, dataArray: Array<ByteArray>): BooleanArray =
        nativeBatchWriteMemory(addrs, dataArray)

    /**
     * 获取可用的驱动列表
     * @return 可用驱动信息数组
     */
    fun getAvailableDrivers(): Array<DriverInfo> = nativeGetAvailableDrivers()

    /**
     * 下载并安装驱动（Rust层通过JNI调用Java的RootFileSystem和Shell）
     * @param driverName 驱动名称
     * @return 安装结果
     */
    fun downloadAndInstallDriver(driverName: String): DriverInstallResult =
        nativeDownloadAndInstallDriver(driverName)

    /**
     * 检查驱动是否已安装
     * @return 是否已安装
     */
    fun isDriverInstalled(): Boolean = nativeIsDriverInstalled()

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
    private external fun nativeBatchReadMemory(addrs: LongArray, sizes: IntArray): Array<ByteArray?>
    private external fun nativeWriteMemory(addr: Long, data: ByteArray): Boolean
    private external fun nativeBatchWriteMemory(
        addrs: LongArray,
        dataArray: Array<ByteArray>
    ): BooleanArray

    private external fun nativeGetAvailableDrivers(): Array<DriverInfo>
    private external fun nativeDownloadAndInstallDriver(driverName: String): DriverInstallResult
    private external fun nativeIsDriverInstalled(): Boolean
}