package lt.dariusl.rxfirebase.test;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;

import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class DoesThisEvenWorkTest {

    public @Rule PlayServicesRule playServicesRule = new PlayServicesRule();
    public @Rule FirebaseRule firebaseRule = new FirebaseRule();

    @Test
    public void testAuthAnonymously() throws Exception {
        AuthResult authResult = Tasks.await(firebaseRule.auth.signInAnonymously(), 5, TimeUnit.SECONDS);
        assertThat(authResult.getUser(), notNullValue());
    }
}