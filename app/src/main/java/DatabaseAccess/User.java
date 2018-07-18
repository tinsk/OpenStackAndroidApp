package DatabaseAccess;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 *  Sadrzi podatke o korisniku koje cuvamo u bazi.
 *  Svako polje entiteta User je jedan stupac u bazi.
 */
@Entity
public class User {
    @PrimaryKey
    private int uid;

    @ColumnInfo(name = "username")
    private String username;

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "baseUri")
    private String baseUri;

    @ColumnInfo(name = "token")
    private String token;

    @ColumnInfo(name = "identityUri")
    private String identityUri;

    @ColumnInfo(name = "computeUri")
    private String computeUri;

    @ColumnInfo(name="defaultProject")
    private String defaultProject;

    @ColumnInfo(name="domain")
    private String domain;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIdentityUri() {
        return identityUri;
    }

    public void setIdentityUri(String identityUri) {
        this.identityUri = identityUri;
    }

    public String getComputeUri() {
        return computeUri;
    }

    public void setComputeUri(String computeUri) {
        this.computeUri = computeUri;
    }

    public String getDefaultProject() {
        return defaultProject;
    }

    public void setDefaultProject(String defaultProject) {
        this.defaultProject = defaultProject;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
