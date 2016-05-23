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

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class AuthenticationTest {
    public @Rule PlayServicesRule playServicesRule = new PlayServicesRule();
    public @Rule FirebaseRule firebaseRule = new FirebaseRule();
    private final FirebaseAuth firebaseAuth = firebaseRule.auth;

    @Test
    public void testAuthAnonymously() throws Exception {
        AuthResult result = RxFirebase.authAnonymously(firebaseAuth).toBlocking().single();
        assertThat(result, notNullValue());
    }

    @Test
    public void testObserveAuth() throws Exception {
        Observable<Boolean> isAuthenticated = RxFirebase
                .observeAuth(firebaseAuth)
                .map(new Func1<FirebaseUser, Boolean>() {
                    @Override
                    public Boolean call(FirebaseUser user) {
                        return user != null;
                    }
                });

        ReplaySubject<Boolean> userState = ReplaySubject.create();
        isAuthenticated.subscribe(userState);

        RxFirebase.authAnonymously(firebaseAuth).toBlocking().single();
        firebaseAuth.signOut();

        List<Boolean> observedState = userState.take(3).toList().toBlocking().single();
        assertThat(observedState, contains(false, true, false));
    }
}
