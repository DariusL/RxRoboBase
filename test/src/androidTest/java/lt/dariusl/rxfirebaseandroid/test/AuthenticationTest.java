package lt.dariusl.rxfirebaseandroid.test;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import lt.dariusl.rxfirebaseandroid.RxFirebase;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.ReplaySubject;

import static lt.dariusl.rxfirebaseandroid.RxFirebase.*;
import static lt.dariusl.rxfirebaseandroid.test.TestUtil.await;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class AuthenticationTest {
    public @Rule PlayServicesRule playServicesRule = new PlayServicesRule();
    public @Rule FirebaseRule firebaseRule = new FirebaseRule();
    private final FirebaseAuth firebaseAuth = firebaseRule.auth;

    @Test
    public void testAuthAnonymously() throws Exception {
        AuthResult result = await(authAnonymously(firebaseAuth));
        assertThat(result, notNullValue());
    }

    @Test
    public void testObserveAuth() throws Exception {
        Observable<Boolean> isAuthenticated =
                observeAuth(firebaseAuth)
                .map(new Func1<FirebaseUser, Boolean>() {
                    @Override
                    public Boolean call(FirebaseUser user) {
                        return user != null;
                    }
                });

        ReplaySubject<Boolean> userState = ReplaySubject.create();
        isAuthenticated.subscribe(userState);

        await(authAnonymously(firebaseAuth));
        firebaseAuth.signOut();

        List<Boolean> observedState = await(userState.take(3).toList());
        assertThat(observedState, contains(false, true, false));
    }
}
