package lt.dariusl.rxfirebaseandroid.test;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Rule;
import org.junit.Test;

import lt.dariusl.rxfirebaseandroid.RxFirebase;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class AuthenticationTest {
    public @Rule PlayServicesRule playServicesRule = new PlayServicesRule();
    public @Rule FirebaseRule firebaseRule = new FirebaseRule();

    @Test
    public void testAuthAnonymously() throws Exception {
        FirebaseAuth firebaseAuth = firebaseRule.auth;

        AuthResult result = RxFirebase.authAnonymously(firebaseAuth).toBlocking().single();
        assertThat(result, notNullValue());
    }
}
