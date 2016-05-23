package lt.dariusl.rxfirebaseandroid;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;
import android.test.ApplicationTestCase;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest{

    @Test
    public void testAuthAnonymously() throws Exception {
        /*final FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyBfivxlHBchQ4WfEr05T6PYAu21fHjcd-E")
                .setStorageBucket("rx-firebase.appspot.com")
                .setApplicationId("lt.dariusl.rxfirebaseandroid")
                .setDatabaseUrl("https://rx-firebase.firebaseio.com/")
                .build();

        FirebaseApp.initializeApp(RuntimeEnvironment.application, options);
        FirebaseDatabase.getInstance().goOnline();*/

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


        assertTrue(latch.await(30, TimeUnit.SECONDS));
    }
}