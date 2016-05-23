package lt.dariusl.rxfirebaseandroid.test;

import android.support.test.InstrumentationRegistry;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.junit.internal.runners.statements.Fail;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class PlayServicesRule implements TestRule {
    @Override
    public Statement apply(Statement base, Description description) {
        return servicesAvailable() ? base : fail("Test class requires an emulator with Google Play Services");
    }

    private static boolean servicesAvailable(){
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(InstrumentationRegistry.getTargetContext()) == ConnectionResult.SUCCESS;
    }

    private static Statement fail(String message){
        return new Fail(new Exception(message));
    }
}
