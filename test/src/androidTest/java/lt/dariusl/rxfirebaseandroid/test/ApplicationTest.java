package lt.dariusl.rxfirebaseandroid.test;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest {

    @Before
    public void setUp() throws Exception {
        assertThat(
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(InstrumentationRegistry.getTargetContext()),
                is(ConnectionResult.SUCCESS));
    }

    @Test
    public void testAuthAnonymously() throws Exception {
        FirebaseDatabase.getInstance().goOnline();

        final CountDownLatch latch = new CountDownLatch(1);
        FirebaseAuth.getInstance()
                .signInAnonymously()
                .addOnCompleteListener(Executors.newSingleThreadExecutor(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        assertThat(task.getResult().getUser(), notNullValue());
                        latch.countDown();
                    }
                });


        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}