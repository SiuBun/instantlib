package com.baselib.instant.zip

/**
 * 压缩接口
 *
 * @author  wsb
 */
interface ICompress {
    /**
     * 压缩
     * @param src    压缩源
     * @return 压缩后的比特数组
     */
    fun compress(src: ByteArray): ByteArray?

    /**
     * 解压
     * @param src 解压源
     * @return 解压出来的比特数组
     */
    fun decompress(src: ByteArray): ByteArray?
}
