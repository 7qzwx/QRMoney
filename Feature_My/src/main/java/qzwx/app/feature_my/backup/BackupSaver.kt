package qzwx.app.feature_my.backup

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

/**
 * 备份文件落盘与分享：
 * - MediaStore 保存到「下载/QRMoney」（API 29+ 无需 WRITE_EXTERNAL_STORAGE 权限）
 * - SAF Uri 写入（用户自定义保存位置，通过系统文件选择器）
 * - FileProvider + ACTION_SEND 分享
 */
object BackupSaver {

    /** 保存到系统下载目录（MediaStore.Downloads，IS_PENDING 保证写入原子性） */
    fun saveToMediaStore(context: Context, fileName: String, mimeType: String, bytes: ByteArray): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/QRMoney")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("无法创建下载文件")
        try {
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: throw IllegalStateException("无法写入下载文件")
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
    }

    /** 写入用户通过系统文件选择器（SAF / ACTION_CREATE_DOCUMENT）选择的位置 */
    fun writeToUri(context: Context, uri: Uri, bytes: ByteArray) {
        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            ?: throw IllegalStateException("无法打开所选位置")
    }

    /** 生成临时分享文件，返回 ACTION_SEND 分享 Intent（FileProvider + FLAG_GRANT_READ_URI_PERMISSION） */
    fun buildShareIntent(context: Context, fileName: String, mimeType: String, bytes: ByteArray): Intent {
        val dir = File(context.cacheDir, "backup").apply { mkdirs() }
        val file = File(dir, fileName)
        file.writeBytes(bytes)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            "分享备份文件"
        )
    }
}
