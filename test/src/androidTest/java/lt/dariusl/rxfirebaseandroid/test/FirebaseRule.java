package lt.dariusl.rxfirebaseandroid.test;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.rules.ExternalResource;

import lt.dariusl.rxfirebaseandroid.RxFirebase;

public class FirebaseRule extends ExternalResource {
    public final FirebaseAuth auth;
    public final DatabaseReference reference;

    public FirebaseRule() {
        this(FirebaseDatabase.getInstance(), FirebaseAuth.getInstance());
    }

    public FirebaseRule(FirebaseDatabase database, FirebaseAuth auth){
        this.auth = auth;
        this.reference = database.getReference().push();
    }

    @Override
    protected void after() {
        super.after();
        RxFirebase.setValue(reference, null).toBlocking().single();
        auth.signOut();
    }
}
