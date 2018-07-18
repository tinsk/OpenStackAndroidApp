package DatabaseAccess;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import java.util.concurrent.ExecutionException;

/**
 * Klasa koju koristimo za pristup bazi iz Activiy klasa.
 */
public class UserViewModel extends AndroidViewModel {

    private Repository mRepository;

    public UserViewModel(@NonNull Application application) {
        super(application);
        mRepository = new Repository(application);
    }

    /**
     * Azurira korisnika koji ima isti ID kao user ili
     * dodaje novog korisnika ako korisnik s tim ID-jem ne postoji
     * @param user
     */
    public void insertOrUpdate(User user) {
        mRepository.insertOrUpdate(user);
    }

    /**
     * Dohvaca korisnika po ID-u
     * @param id
     */
    public User getUserById(Integer id) throws ExecutionException, InterruptedException {
        return mRepository.getUserById(id);
    }
}
