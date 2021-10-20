package com.baselib.instant.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.text.TextUtils
import java.io.*
import java.nio.charset.Charset
import java.util.*


class FileUtils {
    companion object {
        /**
         * 将图片数据存入sd卡文件中
         *
         * @param iconByte 待存入的数据
         * @param iconType 图片类型,传入null,则默认以png为后缀
         * @param dirPath 存放路径,传入null,则默认以png为后缀
         * @return 存入后的文件路径,存入失败,则返回null
         */
        fun saveIconToSdFile(iconByte: ByteArray, iconType: String?, dirPath: String): String? {
            val randomStr = Random().nextInt().toString()
            val type: String = if (iconType.isNullOrEmpty()) ".png" else iconType
            val pathStr = dirPath + randomStr + type
            return if (saveByteToSdFile(iconByte, pathStr)) pathStr else null
        }

        /**
         * 保存数据到指定文件
         *
         * @param iconByte 待存入的数据
         * @param pathStr  存放数据的目标文件路径
         * @return true for save successful, false for save failed.
         */
        fun saveByteToSdFile(iconByte: ByteArray, pathStr: String): Boolean {
            return try {
                val newFile = createNewFile(pathStr, false)
                val fileOutputStream = FileOutputStream(newFile)
                fileOutputStream.write(iconByte)
                fileOutputStream.flush()
                fileOutputStream.close()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        fun saveByteToCommonIconSdFile(byteData: ByteArray, fileName: String, dirPath: String): Boolean {
            return saveByteToSdFile(byteData, dirPath + fileName)
        }

        /**
         * 保存位图到通用图片库中
         *
         * @param bitmap 位图资源
         * @param fileName 待保存文件名
         * @param iconFormat 图片格式
         * @return true for 保存成功，false for 保存失败。
         */
        fun saveBitmapToCommonIconSDFile(bitmap: Bitmap, fileName: String,
                                         iconFormat: Bitmap.CompressFormat, dirPath: String): Boolean {
            return saveBitmapToSdFile(bitmap, dirPath + fileName, iconFormat)

        }

        /**
         * 保存位图到sd卡目录下
         *
         * @param bitmap 位图资源
         * @param filePathName 待保存的文件完整路径名
         * @param iconFormat 图片格式
         * @return true for 保存成功，false for 保存失败。
         */
        fun saveBitmapToSdFile(bitmap: Bitmap?, filePathName: String, iconFormat: Bitmap.CompressFormat): Boolean {
            return if (bitmap == null || bitmap.isRecycled) {
                false
            } else {
                try {
                    createNewFile(filePathName, false)
                    val fileOutputStream = FileOutputStream(filePathName)
                    val compress = bitmap.compress(iconFormat, 100, fileOutputStream)
                    fileOutputStream.close()
                    compress
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        }

        /**
         * 保存输入流内容到SD卡文件
         * @param inputStream
         * @param filePathName 待保存的文件完整路径名
         * @return
         */
        fun saveInputStreamToSDFile(inputStream: InputStream?,
                                    filePathName: String): Boolean {
            return if (null == inputStream) {
                false
            } else {
                var result: Boolean
                var os: OutputStream? = null
                try {
                    val file = createNewFile(filePathName, false)
                    os = FileOutputStream(file)
                    val buffer = ByteArray(4 * 1024)
                    var len: Int
                    while ((inputStream.read(buffer).also { len = it }) != -1) {
                        os.write(buffer, 0, len)
                    }
                    os.flush()
                    result = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    result = false
                } finally {
                    try {
                        os!!.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
                result
            }

        }

        /**
         * 安全地（使用临时文件）保存输入流内容到SD卡文件
         * @param inputStream
         * @param filePathName 待保存的文件完整路径名
         * @return
         */
        fun saveInputStreamToSDFileSafely(inputStream: InputStream?,
                                          filePathName: String): Boolean {
            return if (null == inputStream) {
                false
            } else {
                var result: Boolean
                var os: OutputStream? = null
                try {
                    val tempFilePathName = "$filePathName-temp"
                    val file = createNewFile(tempFilePathName, false)
                    os = FileOutputStream(file)
                    val buffer = ByteArray(4 * 1024)
                    var len: Int
                    while ((inputStream.read(buffer).also { len = it }) != -1) {
                        os.write(buffer, 0, len)
                    }
                    os.flush()
                    file!!.renameTo(File(filePathName))
                    result = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    result = false
                } finally {
                    try {
                        os!!.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
                result
            }

        }

        /**
         *
         * @param path 文件路径
         * @param append 若存在是否插入原文件
         * @return 成功返回file文件，失败返回null
         */
        fun createNewFile(path: String?, append: Boolean): File? {
            return if (path.isNullOrEmpty()) {
                null
            } else {
                val file = File(path)
                if (!append) {
                    if (file.exists()) {
                        file.delete()
                    }
                }
                if (!file.exists()) {
                    val parentFile = file.parentFile
                    if (parentFile != null && !parentFile.exists()) {
                        parentFile.mkdirs()
                    }
                    file.createNewFile()
                }

                if (file.exists() && file.isFile) file else null
            }
        }

        /**
         * 获取文件比特数组
         * @param filePathName 文件路径
         * @return 比特数组形式
         * */
        fun getByteFromSdFile(filePathName: String): ByteArray? {
            val byteArray: ByteArray
            return try {
                val file = File(filePathName)
                val fileInputStream = FileInputStream(file)
                val dataInputStream = DataInputStream(fileInputStream)
                val bufferedInputStream = BufferedInputStream(dataInputStream)
                byteArray = ByteArray(file.length().toInt())
                bufferedInputStream.read(byteArray)
                fileInputStream.close()
                byteArray
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        /**
         * 创建文件夹
         * @param directoryPath 文件夹路径
         * @return 创建文件夹后返回该文件对象
         */
        fun createDirectory(directoryPath: String): File? {
            val directoryFile = File(directoryPath)
            directoryFile.mkdirs()
            return if (directoryFile.exists() && directoryFile.isDirectory) directoryFile else null
        }

        /**
         * sd卡是否可读写
         *
         * @return true代表可用
         */
        fun sdCardAvaiable(): Boolean {
            return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
        }

        /**
         * 指定路径文件对象是否存在
         *
         * @param filePath 文件绝对路径
         * @return true代表存在
         */
        fun fileExist(filePath: String?): Boolean {
            return if (filePath.isNullOrEmpty()) {
                false
            } else {
                val file = File(filePath)
                file.exists()
            }
        }

        /**
         * 删除指定路径的文件对象
         *
         * 如果filePath表示的是一个文件，则删除文件。
         * 如果filePath表示的是一个目录，则删除目录下的子目录再删除文件
         *
         * @param filePath 文件路径
         */
        fun delFile(filePath: String?) {
            filePath?.let {
                val file = File(filePath)
                if (file.exists()) {
                    if (file.isDirectory) {
                        file.listFiles()?.apply {
                            for (fileItem in this) {
                                if (fileItem.isDirectory) {
                                    delFile(fileItem.absolutePath)
                                }
                                fileItem.delete()
                            }
                        }
                    }
                    file.delete()
                }
            }
        }


        /**
         * 获取文件属性
         *
         * 获取指定路径下,默认获取当前目录下的文件名
         *
         * @param fileName 文件名
         * @return
         */
        fun getFileOption(fileName: String): String {
            val command = "ls -l $fileName"
            val stringBuffer = StringBuffer()
            try {
                val process = Runtime.getRuntime().exec(command)
                val inputStream = process.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))

                var tmpStr: String?

                while (bufferedReader.readLine().also { tmpStr = it } != null) {
                    stringBuffer.append(tmpStr)
                }

                inputStream?.close()
                bufferedReader.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return stringBuffer.toString()
        }


        /**
         * 拷贝文件到指定目录
         * @param src 文件路径
         * @param dstDir 目标目录， 尾部带路径分隔符
         */
        fun copyFile2Dir(src: String, dstDir: String) {
            if (!fileExist(src)) {
                return
            }
            val srcFile = File(src)
            val fileName = srcFile.name
            copyFile(src, dstDir + fileName)
        }

        /**
         * 拷贝文件
         * @param src
         * @param dst
         */
        fun copyFile(src: String, dst: String) {
            if (!fileExist(src)) {
                return
            }

            var fis: FileInputStream? = null
            var fos: FileOutputStream? = null

            val buffer = ByteArray(1024)
            var len: Int
            try {
                fis = FileInputStream(src)
                fos = FileOutputStream(createNewFile(dst, false))

                while ((fis.read(buffer).also { len = it }) > 0) {
                    fos.write(buffer, 0, len)
                }
                fos.flush()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fis?.close()
                } catch (e2: Exception) {
                }

                try {
                    fos?.close()
                } catch (e2: Exception) {
                }
            }
        }

        /**
         * 读取res/raw目录下的txt文件
         *
         * @param context
         * @param rawResId     raw文件资源id
         * @param defaultValue 默认值
         * @return
         */
        fun readRawTxt(context: Context?, rawResId: Int, defaultValue: String): String {
            var rawTxtString = defaultValue
            return if (null == context) {
                defaultValue
            } else {
                // 从资源获取流
                var inputStream: InputStream? = null
                try {
                    inputStream = context.resources.openRawResource(rawResId)

                    val buffer = ByteArray(64)
                    // 读取流内容
                    val len = inputStream.read(buffer)
                    if (len > 0) {
                        // 生成字符串
                        rawTxtString = String(buffer, 0, len).trim { it <= ' ' }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        inputStream!!.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                rawTxtString
            }
        }


        /**
         * 从sdcard读取文件
         *
         * @param filePathName 文件路径
         * @return
         */
        fun readByteFromSDFile(filePathName: String): ByteArray? {
            var bs: ByteArray? = null
            try {
                val newFile = File(filePathName)
                val fileInputStream = FileInputStream(newFile)
                val dataInputStream = DataInputStream(fileInputStream)
                val inPutStream = BufferedInputStream(dataInputStream)
                bs = ByteArray(newFile.length().toInt())
                inPutStream.read(bs)
                fileInputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return bs
        }


        /**
         * 从sdcard读取文件
         *
         * @param filePath 文件路径
         * @return
         */
        fun readFileToString(filePath: String): String? {
            return if (TextUtils.isEmpty(filePath)) {
                null
            } else {
                val file = File(filePath)
                if (!file.exists()) {
                    null
                } else {
                    try {
                        val inputStream = FileInputStream(file)
                        readInputStream(inputStream, "UTF-8")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }

            }
        }

        /**
         * 读取输入流,转为字符串
         *
         * @param inputStream
         * @param charset 字符格式
         * @return
         * @throws IOException
         */
        @Throws(IOException::class)
        fun readInputStream(inputStream: InputStream?, charset: String): String? {
            return if (inputStream == null) {
                ""
            } else {
                val out = ByteArrayOutputStream()
                val bufferLength = 1024
                val data: ByteArray
                try {
                    val buf = ByteArray(bufferLength)
                    var len: Int
                    while ((inputStream.read(buf).also { len = it }) > 0) {
                        out.write(buf, 0, len)
                    }
                    data = out.toByteArray()

                    String(data, if (TextUtils.isEmpty(charset)) Charsets.UTF_8 else Charset.forName(charset))
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                } finally {
                    inputStream.close()
                    out.close()
                }
            }
        }


        /**
         * 读取stream并返回一个前length长的string
         *
         * @param in 输入流
         * @param charset 编码格式
         * @param length 长度
         * @return
         * @throws IOException
         */
        @Throws(IOException::class)
        fun readInputStreamWithLength(inputStream: InputStream?, charset: String, length: Int): String? {
            return if (inputStream == null) {
                ""
            } else {
                val out = ByteArrayOutputStream()
                val bufferLength = 1024
                val data: ByteArray
                try {
                    val buf = ByteArray(bufferLength)
                    var len: Int
                    var i = 0
                    while ((inputStream.read(buf).also { len = it }) > 0 && i < length) {
                        out.write(buf, 0, len)
                        i++
                    }
                    data = out.toByteArray()
                    String(data, if (TextUtils.isEmpty(charset)) Charsets.UTF_8 else Charset.forName(charset))
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                } finally {
                    inputStream.close()
                    out.close()
                }
            }
        }

        /**
         * 追加到文件末尾
         *
         * @param fileName 文件路径名
         * @param content 文件写入内容
         */
        fun append2File(fileName: String, content: String) {
            try {
                // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
                val writer = FileWriter(fileName, true)
                writer.write(content)
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }
}