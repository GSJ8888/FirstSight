package tech.huangsh.firstsight.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import tech.huangsh.firstsight.data.local.dao.AppInfoDao
import tech.huangsh.firstsight.data.local.dao.ContactDao
import tech.huangsh.firstsight.data.model.AppInfo
import tech.huangsh.firstsight.data.model.Contact

/**
 * 应用数据库
 */
@Database(
    entities = [
        Contact::class,
        AppInfo::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun appInfoDao(): AppInfoDao
}