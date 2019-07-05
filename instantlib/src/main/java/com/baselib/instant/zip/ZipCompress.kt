package com.baselib.instant.zip

/**
 * zip压缩实现
 *
 * @author wsb
 * */
class ZipCompress : ICompress {

    override fun compress(src: ByteArray): ByteArray? {
        return try {
            ZipUtils.gzip(src)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun decompress(src: ByteArray): ByteArray? {
        return try {
            ZipUtils.ungzip(src)
        }catch (e:Exception){
            e.printStackTrace()
            null
        }
    }

}