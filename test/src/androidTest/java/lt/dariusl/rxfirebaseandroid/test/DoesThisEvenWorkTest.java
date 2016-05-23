package lt.dariusl.rxfirebaseandroid.test;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DoesThisEvenWorkTest {

    public @Rule PlayServicesRule playServicesRule = new PlayServicesRule();
    public @Rule FirebaseRule firebaseRule = new FirebaseRule(FirebaseAuth.getInstance());

    @Test
    public void testAuthAnonymously() throws Exception {
        AuthResult authResult = Tasks.await(FirebaseAuth.getInstance().signInAnonymously(), 5, TimeUnit.SECONDS);
        assertThat(authResult.getUser(), notNullValue());
    }
}