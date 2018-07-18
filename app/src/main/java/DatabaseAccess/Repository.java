package DatabaseAccess;

import android.app.Application;
import android.os.AsyncTask;

import java.util.concurrent.ExecutionException;

/**
 * Indirektni pristup bazi podataka koristenjem DAO klase.
 * Ovu klasu koristimo u UserViewModel-u za pristup podacima
 * iz baze u aktivnostima
 */
public class Repository {
    private UserDao mUserDao;

    Repository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mUserDao = db.userDao();
    }

    /**
     * Izvrsava dodavanje ili azuriranje korisnika na pozadinskoj dretvi
     * @param user
     */
    public void insertOrUpdate (User user) {
        new insertAsyncTask(mUserDao).execute(user);
    }

    /**
     * Izvrsava dohvacanje korisnika po ID-ju na pozadinskoj dretvi
     * @param uid
     */
    public User getUserById(int uid) throws ExecutionException, InterruptedException {
        return (new getUsersAsyncTask(mUserDao).execute(uid)).get();
    }

    /**
     * Zadatak koji dodaje ili azurira korisnika
     */
    private static class insertAsyncTask extends AsyncTask<User, Void, Void> {

        private UserDao mAsyncTaskDao;

        insertAsyncTask(UserDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final User... users) {
            mAsyncTaskDao.insertOrUpdate(users);
            return null;
        }
    }

    /**
     * Zadatak koji dohvaca korisnika
     */
    private static class getUsersAsyncTask extends AsyncTask<Integer, Void, User> {
        private UserDao mAsyncTaskDao;

        public getUsersAsyncTask(UserDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected User doInBackground(Integer... params) {
            return mAsyncTaskDao.findById(params[0]);
        }
    }
}
