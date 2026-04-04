package com.app.fintrack.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.app.fintrack.domain.model.FinanceCategory
import com.app.fintrack.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val amount: Double,
    val type: TransactionType,
    val category: FinanceCategory,
    val date: LocalDate,
    val notes: String,
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val targetAmount: Double,
    val currentSaved: Double,
)

class FinanceTypeConverters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? = value?.let(TransactionType::valueOf)

    @TypeConverter
    fun fromFinanceCategory(value: FinanceCategory?): String? = value?.name

    @TypeConverter
    fun toFinanceCategory(value: String?): FinanceCategory? = value?.let(FinanceCategory::valueOf)
}

@Dao
interface TransactionDao {
    @Query(
        """
        SELECT * FROM transactions
        WHERE (:searchQuery = '' OR notes LIKE '%' || :searchQuery || '%' OR category LIKE '%' || :searchQuery || '%')
        AND (:type IS NULL OR type = :type)
        AND (:category IS NULL OR category = :category)
        AND (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        ORDER BY date DESC, id DESC
        """
    )
    fun pagingSource(
        searchQuery: String,
        type: TransactionType?,
        category: FinanceCategory?,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): PagingSource<Int, TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions
        WHERE (:searchQuery = '' OR notes LIKE '%' || :searchQuery || '%' OR category LIKE '%' || :searchQuery || '%')
        AND (:type IS NULL OR type = :type)
        AND (:category IS NULL OR category = :category)
        AND (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        ORDER BY date DESC, id DESC
        """
    )
    fun observeFilteredTransactions(
        searchQuery: String,
        type: TransactionType?,
        category: FinanceCategory?,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC LIMIT :limit")
    fun observeRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY id DESC")
    fun observeAllGoals(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SavingsGoalEntity)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Database(
    entities = [TransactionEntity::class, SavingsGoalEntity::class],
    version = 3,
    exportSchema = false,
)
@TypeConverters(FinanceTypeConverters::class)
abstract class FintrackDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao
}
