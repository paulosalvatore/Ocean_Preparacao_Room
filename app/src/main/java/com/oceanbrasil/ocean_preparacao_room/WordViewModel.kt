package com.oceanbrasil.ocean_preparacao_room

import android.app.Application
import android.content.Context
import androidx.annotation.NonNull
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase

@Entity(tableName = "word_table")
data class Word(
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "word") // Opcional
    val word: String
)

class WordViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WordRepository(application)

    val allWords = repository.allWords

    fun insert(word: Word) {
        repository.insert(word)
    }
}

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(word: Word)

    @Query("DELETE FROM word_table")
    fun deleteAll()

    @Query("SELECT * FROM word_table ORDER by word ASC")
    fun getAllWords(): LiveData<List<Word>>
}

class WordRepository(application: Application) {
    private val wordDao: WordDao
    val allWords: LiveData<List<Word>>

    init {
        val db = WordRoomDatabase.getDatabase(application)
        wordDao = db.wordDao()
        allWords = wordDao.getAllWords()
    }

    fun insert(word: Word) {
        Thread(Runnable {
            wordDao.insert(word)
        }).start()
    }
}

@Database(entities = [Word::class], version = 1, exportSchema = false)
abstract class WordRoomDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao

    companion object {
        private var instance: WordRoomDatabase? = null

        private val roomDatabaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                instance?.let {
                    Thread(Runnable {
                        val dao = it.wordDao()

                        dao.deleteAll()

                        val word = Word("Samsung")
                        dao.insert(word)

                        val word2 = Word("Ocean")
                        dao.insert(word2)
                    }).start()
                }
            }
        }

        fun getDatabase(context: Context): WordRoomDatabase {
            if (instance == null) {
                synchronized(WordRoomDatabase::class.java) {
                    // Criação do DB
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        WordRoomDatabase::class.java,
                        "word_database"
                    ).addCallback(roomDatabaseCallback).build()
                }
            }

            return instance!!
        }
    }
}
