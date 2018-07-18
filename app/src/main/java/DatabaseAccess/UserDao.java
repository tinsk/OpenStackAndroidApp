package DatabaseAccess;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Koristimo User Data Access Object za pristup podacima u bazi.
 * Svaka metoda predstavlja jedan upit ili promjenu vrijednosti u bazi.
 */
@Dao
public interface UserDao {
    @Query("SELECT * FROM user")
    List<User> getAll();

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    List<User> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM user WHERE username=:username LIMIT 1")
    User findByName(String username);

    @Query("SELECT * FROM user WHERE uid=:id LIMIT 1")
    User findById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(User... users);

    @Insert
    void insertAll(User... users);

    @Delete
    void delete(User user);

    @Update
    void update(User... users);

}
