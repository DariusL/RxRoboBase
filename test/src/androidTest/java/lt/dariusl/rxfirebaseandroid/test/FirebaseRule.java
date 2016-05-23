package lt.dariusl.rxfirebaseandroid.test;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.rules.ExternalResource;

public class FirebaseRule extends ExternalResource {
    private final FirebaseAuth auth;

    public FirebaseRule(FirebaseAuth auth) {
        this.auth = auth;
    }

    @Override
    protected void after() {
        super.after();
        auth.signOut();
    }
}
