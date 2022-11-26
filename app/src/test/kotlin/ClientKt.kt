import android.os.Bundle

class ClientKt {
    companion object {
        private const val STATUS_PREPARE_DOWNLOAD = 0
        private const val STATUS_DOWNLOADING = 1
        private const val STATUS_DOWNLOADED = 2

        private const val STATUS_DOWNLOAD_CANCEL = 3
        private const val STATUS_DOWNLOAD_STOP = 4
        private const val STATUS_DOWNLOAD_RESTART = 5

        private val client = ClientKt()

        fun planA() {
            val status = STATUS_DOWNLOADING
            val percent = 50
            client.saveDownloadStatus(status)
            if (status == STATUS_DOWNLOADING) {
                client.saveDownloadPercent(percent)
            }
        }

        fun planB() {
            val status = STATUS_DOWNLOADING
            val percent = 50
            val bundle = Bundle().apply {
                if (status == STATUS_DOWNLOADING) {
                    putInt("percent", percent)
                }
            }
            client.saveDownloadStatus(bundle)
        }

        fun planC() {
            val status = STATUS_DOWNLOADING
            val percent = 50
            val downloadCount = 10
            val cancelCount = 10
            val stopCause = 404
            val restartCause = 200
            val bundle = Bundle().apply {
                when (status) {
                    STATUS_DOWNLOADING -> {
                        putInt("percent", percent)
                    }
                    STATUS_DOWNLOADED -> {
                        putInt("percent", 100)
                        putInt("downloadCount", downloadCount)
                    }
                    STATUS_DOWNLOAD_CANCEL -> {
                        putInt("percent", percent)
                        putInt("cancelCount", cancelCount)
                    }
                    STATUS_DOWNLOAD_STOP -> {
                        putInt("percent", percent)
                        putInt("stopCause", stopCause)
                    }
                    STATUS_DOWNLOAD_RESTART -> {
                        putInt("percent", percent)
                        putInt("restartCause", restartCause)
                    }
                }
            }
            client.saveDownloadStatus(bundle)
        }
    }

    /**
     * 保存下载状态
     */
    fun saveDownloadStatus(status: Int) {}

    /**
     * 本地查询结果
     * */
    fun getDownloadStatus(): Int {
        return 0
    }

    /**
     * 本地状态对应结果
     * */
    fun getDownloadStatus(status: Int): Int {
        return when (status) {
            STATUS_DOWNLOADING -> {
                50
            }
            STATUS_DOWNLOADED -> {
                100
            }
            else -> {
                0
            }
        }
    }

    /**
     * 保存下载进度
     */
    fun saveDownloadPercent(percent: Int) {}

    /**
     * 保存下载状态
     */
    fun saveDownloadStatus(bundle: Bundle) {}

    /**
     * 根据
     * */
    fun getDownloadValueByStatus(status: Int) {

    }
}