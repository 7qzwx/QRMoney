package qzwx.app.feature_my.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.first
import qzwx.app.feature_my.backup.BackupMyDateJson
import qzwx.app.module_data.QBillDatabase
import qzwx.app.module_data.dao.QBillDao
import qzwx.app.module_data.dao.QDebtDao
import qzwx.app.module_data.dao.QMyDataDao
import qzwx.app.module_data.dao.QWalletDao
import qzwx.app.module_data.dao.QWishDao
import qzwx.app.module_data.entity.QBillEntity
import qzwx.app.module_data.entity.QDebtEntity
import qzwx.app.module_data.entity.QMyDateEntity
import qzwx.app.module_data.entity.QWalletEntity
import qzwx.app.module_data.entity.QWishEntity
import javax.inject.Inject

/**
 * 数据备份数据源：
 * - 导出：从 Room 一次性读取整个数据库（账单 / 记账天数 / 钱包 / 负债 / 愿望）
 * - 导入：清空后导入（事务原子替换全部表）或覆盖导入（按 ID REPLACE），并恢复记账天数
 * Room 的 suspend 查询自动在 IO 线程执行。
 */
class DataBackUp_RT @Inject constructor(
    private val qBillDao: QBillDao,
    private val qMyDataDao: QMyDataDao,
    private val qWalletDao: QWalletDao,
    private val qDebtDao: QDebtDao,
    private val qWishDao: QWishDao,
    private val db: QBillDatabase
) {

    /** 全部账单（按创建时间倒序） */
    suspend fun getAllBillsOnce(): List<QBillEntity> = qBillDao.getAllBills().first()

    /** 指定日期范围 [startMillis, endMillis) 的账单（按创建时间倒序） */
    suspend fun getBillsBetweenOnce(startMillis: Long, endMillis: Long): List<QBillEntity> =
        qBillDao.getBillsBetweenOnce(startMillis, endMillis)

    /** 当前账单总条数 */
    suspend fun billCountOnce(): Int = qBillDao.getBillCount().first()

    /** 全部钱包 */
    suspend fun getAllWalletsOnce(): List<QWalletEntity> = qWalletDao.getAllWalletsOnce()

    /** 全部负债 */
    suspend fun getAllDebtsOnce(): List<QDebtEntity> = qDebtDao.getAllDebtsOnce()

    /** 全部愿望 */
    suspend fun getAllWishesOnce(): List<QWishEntity> = qWishDao.getAllWishesOnce()

    /** “坚持记账的第 X 天”起点（首次启动时间）；无记录时返回 null */
    suspend fun getMyDateOnce(): QMyDateEntity? = qMyDataDao.getMyDate().first()

    /**
     * 导入整个数据库：
     * - clearFirst=true：先清空账单/钱包/负债/愿望四表，再写入备份内容（整体替换，单事务保证原子性）
     * - clearFirst=false：按 ID REPLACE 覆盖写入，备份中不存在的表数据保留
     * - 备份含记账天数起点时恢复之（更新首行，无记录则插入）
     */
    suspend fun importAllData(
        bills: List<QBillEntity>,
        wallets: List<QWalletEntity>,
        debts: List<QDebtEntity>,
        wishes: List<QWishEntity>,
        myDate: BackupMyDateJson?,
        clearFirst: Boolean
    ) {
        db.withTransaction {
            if (clearFirst) {
                qBillDao.deleteAllBills()
                qWalletDao.deleteAllWallets()
                qDebtDao.deleteAllDebts()
                qWishDao.deleteAllWishes()
            }
            qBillDao.insertBills(bills)
            if (wallets.isNotEmpty()) qWalletDao.insertWallets(wallets)
            if (debts.isNotEmpty()) qDebtDao.insertDebts(debts)
            if (wishes.isNotEmpty()) qWishDao.insertWishes(wishes)

            myDate?.let { md ->
                qMyDataDao.updateMyDate(md.firstLaunchDate, md.transactionCount)
                if (qMyDataDao.getMyDate().first() == null) {
                    qMyDataDao.insertMyDate(
                        QMyDateEntity(firstLaunchDate = md.firstLaunchDate, transactionCount = md.transactionCount)
                    )
                }
            }
        }
    }
}
