package com.baselib.instant.zip

import java.io.*
import java.util.zip.Deflater
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.Inflater

/**
 * zip工具类
 * */
class ZipUtils {
    companion object {
        private const val sLENGTH = 256

        @Throws(Exception::class)
        fun gzip(bs: ByteArray): ByteArray {
            val bout = ByteArrayOutputStream(1000)
            var gzout: GZIPOutputStream? = null
            try {
                gzout = GZIPOutputStream(bout)
                gzout.write(bs)
                gzout.flush()
            } catch (e: Exception) {
                throw e
            } finally {
                try {
                    gzout?.close()
                } catch (e: Exception) {

                }
            }
            return bout.toByteArray()

        }

        @Throws(Exception::class)
        fun ungzip(bs: ByteArray): ByteArray {
            var gzin: GZIPInputStream? = null
            var bin: ByteArrayInputStream? = null
            try {
                bin = ByteArrayInputStream(bs)
                gzin = GZIPInputStream(bin)
                return toByteArray(gzin)
            } catch (e: Exception) {
                throw e
            } finally {
                bin?.close()
                gzin?.close()
            }
        }

        @Throws(Exception::class)
        fun toByteArray(input: InputStream): ByteArray {
            var output: ByteArrayOutputStream? = null
            try {
                output = ByteArrayOutputStream()
                copy(input, output)
                return output.toByteArray()
            } catch (e: Exception) {
                throw e
            } finally {
                try {
                    output?.close()
                } catch (e: Exception) {
                    throw e
                }
            }
        }

        @Throws(Exception::class)
        fun copy(input: InputStream, output: OutputStream): Int {
            try {
                val buffer = ByteArray(1024 * 4)
                var count = 0
                var n = 1
                while (-1 != n) {
                    output.write(buffer, 0, n)
                    count += n
                    n = input.read(buffer)
                }
                return count
            } catch (e: Exception) {
                throw e
            }

        }

        fun unzip(inStream: InputStream): String? {
            return try {
                val oldBytes = toByteArray(inStream)
                // 统计下载速度 old_bytes/time2
                val newBytes = ungzip(oldBytes)
                String(newBytes, Charsets.UTF_8)
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        }

        @Throws(Exception::class)
        fun zip(bs: ByteArray): ByteArray {

            var o: ByteArrayOutputStream? = null
            try {
                o = ByteArrayOutputStream()
                val compressor = Deflater()
                compressor.setInput(bs)
                compressor.finish()
                val output = ByteArray(1024)
                while (!compressor.finished()) {
                    val got = compressor.deflate(output)
                    o.write(output, 0, got)
                }
                o.flush()
                return o.toByteArray()
            } catch (ex: Exception) {
                throw ex

            } finally {
                try {
                    o?.close()
                } catch (e: Exception) {
                    throw e
                }
            }
        }

        @Throws(Exception::class)
        fun unzip(bs: ByteArray): ByteArray {
            var o: ByteArrayOutputStream? = null
            try {
                o = ByteArrayOutputStream()
                val decompressor = Inflater()
                decompressor.setInput(bs)
                val result = ByteArray(1024)
                while (!decompressor.finished()) {
                    val resultLength = decompressor.inflate(result)
                    o.write(result, 0, resultLength)
                }
                decompressor.end()
                o.flush()
                return o.toByteArray()
            } catch (ex: Exception) {
                throw ex

            } finally {
                try {
                    o?.close()
                } catch (e: IOException) {
                    throw e
                }
            }
        }

        /**
         * 压缩字符串
         *
         * @param str
         * @return
         * @throws IOException
         */
        @Throws(IOException::class)
        @JvmOverloads
        fun compress(str: String, bytesToStringEncode: String? = null, stringToBytesEncode: String? = null): String {
            val out = ByteArrayOutputStream()
            val gzip = GZIPOutputStream(out)
            gzip.write(if (stringToBytesEncode.isNullOrEmpty()) {
                str.toByteArray()
            } else {
                str.toByteArray(charset(stringToBytesEncode))
            })
            gzip.close()
            val result = if (bytesToStringEncode.isNullOrEmpty()) {
                out.toString()
            } else {
                out.toString(bytesToStringEncode)
            }
            out.close()
            return result
        }

        /**
         *
         * @param str
         * @param unCompressEncode 解码的编码方式
         * @param stringEncode 返回字符串的编码方式
         * @return
         * @throws IOException
         */
        @Throws(IOException::class)
        @JvmOverloads
        fun unCompress(str: String, unCompressEncode: String? = null, stringEncode: String? = null): String {
            val out = ByteArrayOutputStream()
            val inputStream = ByteArrayInputStream(
                    if (unCompressEncode.isNullOrEmpty()) {
                        str.toByteArray()
                    } else {
                        str.toByteArray(charset(unCompressEncode))
                    }
            )
            val gunzip = GZIPInputStream(inputStream)
            val buffer = ByteArray(sLENGTH)
            var n = 1
            while (n >= 0) {
                out.write(buffer, 0, n)
                n = gunzip.read(buffer)
            }
            val result = if (stringEncode.isNullOrEmpty()) {
                out.toString()
            } else {
                out.toString(stringEncode)
            }
            try {
                out.close()
                gunzip.close()
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }
    }
}