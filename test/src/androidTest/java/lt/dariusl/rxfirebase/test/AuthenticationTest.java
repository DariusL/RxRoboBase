package lt.dariusl.rxfirebase.test;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.ReplaySubject;

import static lt.dariusl.rxfirebase.RxFirebase.authAnonymously;
import static lt.dariusl.rxfirebase.RxFirebase.observeAuth;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class AuthenticationTest {
    public @Rule PlayServicesRule playServicesRule = new PlayServicesRule();
    public @Rule FirebaseRule firebaseRule = new FirebaseRule();
    private final FirebaseAuth firebaseAuth = firebaseRule.auth;

    @Test
    public void testAuthAnonymously() throws Exception {
        AuthResult result = TestUtil.await(authAnonymously(firebaseAuth));
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

        TestUtil.await(authAnonymously(firebaseAuth));
        firebaseAuth.signOut();

        List<Boolean> observedState = TestUtil.await(userState.take(3).toList());
        assertThat(observedState, contains(false, true, false));
    }
}
