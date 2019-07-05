package com.baselib.instant.zip

import android.content.Context
import java.io.*
import java.util.*
import java.util.zip.*
import kotlin.collections.ArrayList

/**
 * 文件压缩工具类
 *
 * @author wsb
 * */
class ZipFilesUtils {
    companion object {
        private const val BUFF_SIZE = 1024 * 1024

        private const val CHARSET_8859_1 = "8859_1"

        private const val CHARSET_GB2312 = "GB2312"

        private val BYTE_ARRAY = ByteArray(BUFF_SIZE)
        /**
         * 压缩单个文件（夹）
         *
         * @param resFile 需要压缩的文件（夹）
         * @param zipOut 压缩的目的文件
         * @param rootPath 压缩的文件路径
         * @throws FileNotFoundException 找不到文件时抛出
         * @throws IOException 当压缩过程出错时抛出
         */
        @Throws(FileNotFoundException::class, IOException::class)
        private fun zipFile(resFile: File, zipOut: ZipOutputStream, rootPath: String) {
            val targetPath = rootPath + (if (rootPath.trim().isEmpty()) {
                ""
            } else {
                File.separator
            }) + resFile.name

            val targetPathStr = String(targetPath.toByteArray(charset(CHARSET_8859_1)), charset(CHARSET_GB2312))
            if (resFile.isDirectory) {
                for (file in resFile.listFiles()) {
                    zipFile(file, zipOut, targetPathStr)
                }
            } else {
                val inputStream = BufferedInputStream(FileInputStream(resFile), BUFF_SIZE)
                zipOut.putNextEntry(ZipEntry(targetPathStr))

                var readLength = 1
                while (readLength != -1) {
                    zipOut.write(BYTE_ARRAY, 0, readLength)
                    readLength = inputStream.read(BYTE_ARRAY)
                }
                inputStream.close()
                zipOut.flush()

                zipOut.closeEntry()
            }
        }

        /**
         * 批量压缩文件（夹）
         *
         * @param resFileList 要压缩的文件（夹）列表
         * @param zipFile 生成的压缩文件
         * @param comment 压缩文件的注释
         * @throws IOException 当压缩过程出错时抛出
         */
        @JvmOverloads
        @Throws(IOException::class)
        fun zipFiles(resFileList: Collection<File>, zipFile: File, comment: String? = null) {
            val zipout = ZipOutputStream(BufferedOutputStream(FileOutputStream(
                    zipFile), BUFF_SIZE))
            for (resFile in resFileList) {
                zipFile(resFile, zipout, "")
            }
            comment?.let {
                zipout.setComment(it)
            }
            zipout.close()
        }


        /**
         * 解压缩一个zip文件
         *
         * @param zipFile 压缩文件
         * @param folderPath 解压缩的目标目录
         * @throws IOException 当解压缩过程出错时抛出
         */
        @Throws(ZipException::class, IOException::class)
        fun unzipFile(zipFile: File, folderPath: String) {
            val desDir = File(folderPath)
            if (!desDir.exists()) {
                desDir.mkdirs()
            }

            val zf = ZipFile(zipFile)
            var notMoreElement = false
            while (!notMoreElement) {
                val entries = zf.entries()
                val zipEntry = entries.nextElement()
                val inputStream = zf.getInputStream(zipEntry)
                val zipEntryName = folderPath + File.separator + zipEntry.name

                val zipEntryNameStr = String(zipEntryName.toByteArray(charset(CHARSET_8859_1)), charset(CHARSET_GB2312))
                val desFile = File(zipEntryNameStr)
                if (!desFile.exists()) {
                    val parentFileDir = desFile.parentFile
                    if (!parentFileDir.exists()) {
                        parentFileDir.mkdirs()
                    }
                    desFile.createNewFile()
                }
                val outputStream = FileOutputStream(desFile)
                var readLength = 1
                while (readLength != -1) {
                    outputStream.write(BYTE_ARRAY, 0, readLength)
                    readLength = inputStream.read(BYTE_ARRAY)
                }

                inputStream.close()
                outputStream.close()
            }
        }

        /**
         * 解压assets的zip压缩文件到指定目录
         * @param context 上下文对象
         * @param assetName 压缩文件名
         * @param outputDirectory 输出目录
         * @param rewrite 是否覆盖
         * @throws IOException
         */
        fun unZipAssetsFile(context: Context, assetName: String, outputDirectory: String, rewrite: Boolean) {
            val file = File(outputDirectory)
            if (!file.exists()) {
                file.mkdirs()
            }

            val inputStream = context.assets.open(assetName)
            val zipInputStream = ZipInputStream(inputStream)
            var zipEntry = zipInputStream.nextEntry

            val length = 1

            while (zipEntry != null) {
                val zipFile = File(outputDirectory + File.separator + zipEntry.name)
                parseZipEntry(rewrite, zipFile, zipEntry, length, zipInputStream)
                zipEntry = zipInputStream.nextEntry
            }
            zipInputStream.close()
        }

        private fun parseZipEntry(rewrite: Boolean, zipFile: File, zipEntry: ZipEntry, length: Int, zipInputStream: ZipInputStream) {
            var count = length
            if (rewrite || !zipFile.exists()) {
                if (zipEntry.isDirectory) {
                    zipFile.mkdir()
                } else {
                    zipFile.createNewFile()
                    val fileOutputStream = FileOutputStream(zipFile)
                    while (count != -1) {
                        fileOutputStream.write(BYTE_ARRAY, 0, count)
                        count = zipInputStream.read(BYTE_ARRAY)
                    }
                    fileOutputStream.close()
                }
            }
        }


        /**
         * 取得压缩文件对象的注释
         *
         * @param entry 压缩文件对象
         * @return 压缩文件对象的注释
         * @throws UnsupportedEncodingException
         */
        @Throws(UnsupportedEncodingException::class)
        fun getEntryComment(entry: ZipEntry): String {
            return String(entry.comment.toByteArray(charset(CHARSET_GB2312)), charset(CHARSET_8859_1))
        }

        /**
         * 取得压缩文件对象的名称
         *
         * @param entry 压缩文件对象
         * @return 压缩文件对象的名称
         * @throws UnsupportedEncodingException
         */
        @Throws(UnsupportedEncodingException::class)
        fun getEntryName(entry: ZipEntry): String {
            return String(entry.name.toByteArray(charset(CHARSET_GB2312)), charset(CHARSET_8859_1))
        }

        /**
         * 获得压缩文件内压缩文件对象以取得其属性
         *
         * @param zipFile 压缩文件
         * @return 返回一个压缩文件列表
         * @throws ZipException 压缩文件格式有误时抛出
         * @throws IOException IO操作有误时抛出
         */
        @Throws(ZipException::class, IOException::class)
        fun <T : ZipEntry> getEntriesEnumeration(zipFile: File, clz: Class<T>): Enumeration<T> {
            return ZipFile(zipFile).entries() as Enumeration<T>
        }

        /**
         * 获得压缩文件内文件列表
         *
         * @param zipFile 压缩文件
         * @return 压缩文件内文件名称
         * @throws ZipException 压缩文件格式有误时抛出
         * @throws IOException 当解压缩过程出错时抛出
         */
        @Throws(ZipException::class, IOException::class)
        fun getEntriesNames(zipFile: File): ArrayList<String> {
            val entryNames = ArrayList<String>()
            val entries = getEntriesEnumeration(zipFile, ZipEntry::class.java)
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement() as ZipEntry
                entryNames.add(String(getEntryName(entry).toByteArray(charset(CHARSET_GB2312)), charset(CHARSET_8859_1)))
            }
            return entryNames
        }

        /**
         * 解压文件名包含传入文字的文件
         *
         * @param zipFile 压缩文件
         * @param folderPath 目标文件夹
         * @param nameContains 传入的文件匹配名
         * @throws ZipException 压缩格式有误时抛出
         * @throws IOException IO错误时抛出
         */
        fun upZipSelectedFile(zipFile: File, folderPath: String, nameContains: String): ArrayList<File> {
            val fileList = ArrayList<File>()
            val desDir = File(folderPath)
            if (!desDir.exists()) {
                desDir.mkdir()
            }

            val zf = ZipFile(zipFile)
            val entries = zf.entries()
            while (entries.hasMoreElements()) {
                val zipEntry = entries.nextElement()
                if (zipEntry.name.contains(nameContains)) {
                    val inputStream = zf.getInputStream(zipEntry)
                    val entryName = folderPath + File.separator + zipEntry.name
                    val entryNameStr = String(entryName.toByteArray(charset(CHARSET_8859_1)), charset(CHARSET_GB2312))

                    val desFile = File(entryNameStr)
                    if (!desFile.exists()) {
                        val parentFile = desFile.parentFile
                        if (!parentFile.exists()) {
                            parentFile.mkdirs()
                        }
                        desFile.createNewFile()
                    }
                    val outputStream = FileOutputStream(desFile)
                    var length = 1
                    while (length != -1) {
                        outputStream.write(BYTE_ARRAY, 0, length)
                        length = inputStream.read(BYTE_ARRAY)
                    }
                    inputStream.close()
                    outputStream.close()
                    fileList.add(desFile)
                }
            }
            return fileList
        }
    }


}