package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccountEntity
import com.example.data.model.PrivateAccountInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("SELECT * FROM user_accounts WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getAccountByEmail(email: String): AccountEntity?

    @Query("SELECT * FROM user_accounts WHERE userId = :userId LIMIT 1")
    suspend fun getAccountByUserId(userId: String): AccountEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM user_accounts WHERE LOWER(email) = LOWER(:email))")
    suspend fun isEmailRegistered(email: String): Boolean

    @Query("UPDATE user_accounts SET resetCode = :resetCode, resetCodeExpiryTimestamp = :expiry WHERE LOWER(email) = LOWER(:email)")
    suspend fun setPasswordResetCode(email: String, resetCode: String, expiry: Long)

    @Query("UPDATE user_accounts SET passwordHash = :passwordHash, passwordSalt = :passwordSalt, resetCode = NULL, resetCodeExpiryTimestamp = NULL WHERE LOWER(email) = LOWER(:email)")
    suspend fun updatePasswordByEmail(email: String, passwordHash: String, passwordSalt: String)

    @Query("UPDATE user_accounts SET passwordHash = :passwordHash, passwordSalt = :passwordSalt WHERE userId = :userId")
    suspend fun updatePasswordByUserId(userId: String, passwordHash: String, passwordSalt: String)

    @Query("SELECT userId, email, createdAtTimestamp FROM user_accounts WHERE userId = :userId LIMIT 1")
    fun getPrivateAccountInfoFlow(userId: String): Flow<PrivateAccountInfo?>

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getAccountsCount(): Int
}
