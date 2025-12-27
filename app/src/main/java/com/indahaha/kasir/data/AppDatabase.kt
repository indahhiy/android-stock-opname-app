package com.indahaha.kasir.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.indahaha.kasir.data.dao.*
import com.indahaha.kasir.data.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MasterKategori::class,
        MasterBarang::class,
        TrafficMasuk::class,
        TrafficKeluar::class,
        StockOpnameHarian::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun masterKategoriDao(): MasterKategoriDao
    abstract fun masterBarangDao(): MasterBarangDao
    abstract fun trafficDao(): TrafficDao
    abstract fun stockOpnameDao(): StockOpnameDao
    abstract fun itemReportDao(): ItemReportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE stock_opname_harian ADD COLUMN catatan TEXT DEFAULT NULL"
                )
            }
        }

        // ✅ FIX: HANYA 1 PARAMETER (context)
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kasir_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    populateDatabase(
                                        database.masterKategoriDao(),
                                        database.masterBarangDao()
                                    )
                                }
                            }
                        }
                    })
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDatabase(
            kategoriDao: MasterKategoriDao,
            barangDao: MasterBarangDao
        ) {
            val kategori1 = MasterKategori(namaKategori = "Bahan Dapur")
            val kategori2 = MasterKategori(namaKategori = "Minuman")
            val kategori3 = MasterKategori(namaKategori = "Kemasan")

            val id1 = kategoriDao.insert(kategori1).toInt()
            val id2 = kategoriDao.insert(kategori2).toInt()
            val id3 = kategoriDao.insert(kategori3).toInt()

            barangDao.insert(MasterBarang(
                namaBarang = "Gula",
                satuan = "kg",
                idKategoriDefault = id1,
                stokMinimum = 5.0,
                stokAwal = 10.0
            ))
            barangDao.insert(MasterBarang(
                namaBarang = "Tepung",
                satuan = "kg",
                idKategoriDefault = id1,
                stokMinimum = 3.0,
                stokAwal = 8.0
            ))
            barangDao.insert(MasterBarang(
                namaBarang = "Air Mineral",
                satuan = "galon",
                idKategoriDefault = id2,
                stokMinimum = 2.0,
                stokAwal = 5.0
            ))
        }
    }
}
