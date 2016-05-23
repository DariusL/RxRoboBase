package lt.dariusl.rxfirebaseandroid.test;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lt.dariusl.rxfirebaseandroid.RxFirebase;
import rx.Observable;
import rx.functions.Func1;
import rx.observers.Observers;
import rx.observers.Subscribers;
import rx.subjects.ReplaySubject;

import static org.junit.Assert.*;

public class WriteTests {

    public @Rule PlayServicesRule playServicesRule = new PlayServicesRule();
    public @Rule FirebaseRule firebaseRule = new FirebaseRule(FirebaseAuth.getInstance());

    @Test
    public void testObserve() throws Exception {
        ReplaySubject<String> values = ReplaySubject.create();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        RxFirebase.observe(reference)
                .map(new Func1<DataSnapshot, String>() {
                    @Override
                    public String call(DataSnapshot dataSnapshot) {
                        return dataSnapshot.getValue(String.class);
                    }
                })
                .distinctUntilChanged()
                .subscribe(values);

        Observable
                .concat(
                        RxFirebase.setValue(reference, null),
                        RxFirebase.setValue(reference, "foo"),
                        RxFirebase.setValue(reference, "bar"),
                        RxFirebase.setValue(reference, null)
                )
                .subscribe(Subscribers.<Void>empty());

        List<String> observedValues = values.take(4).toList().toBlocking().single();

        assertThat(observedValues, Matchers.contains(null, "foo", "bar", null));
    }
}
