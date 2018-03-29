package app.beacon.com.don8;

/**
 * Created by garyc on 12/02/2018.
 */

public class User {

    public String type;
    public String email;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String type, String email) {
        this.type = type;
        this.email = email;
    }


}


