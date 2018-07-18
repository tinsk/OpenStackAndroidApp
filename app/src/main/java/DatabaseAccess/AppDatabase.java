package DatabaseAccess;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Pruza direktan pristup osnovnoj bazi podataka.
 * Ovu klasu ne koristimo direktno u aplikaciji, vec upite
 * izvrsavamo putem DAO klase.
 */
@Database(entities = {User.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // Kreiramo bazu podataka naziva "stack_database"
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "stack_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

