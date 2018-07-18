package OpenStackHttpClient;

/**
 * Iznimka koja se baca ako korisnik nije autoriziran
 * za zahtjev koji je poslao
 */
public class UnauthorizedException extends Exception {
    public UnauthorizedException(String message) {
        super(message);
    }
}
