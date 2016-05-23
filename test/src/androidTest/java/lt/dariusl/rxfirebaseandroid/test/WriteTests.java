package lt.dariusl.rxfirebaseandroid.test;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.observers.Subscribers;
import rx.subjects.BehaviorSubject;
import rx.subjects.ReplaySubject;

import static lt.dariusl.rxfirebaseandroid.RxFirebase.FirebaseChildEvent;
import static lt.dariusl.rxfirebaseandroid.RxFirebase.observe;
import static lt.dariusl.rxfirebaseandroid.RxFirebase.observeChildren;
import static lt.dariusl.rxfirebaseandroid.RxFirebase.setValue;
import static lt.dariusl.rxfirebaseandroid.RxFirebase.updateChildren;
import static lt.dariusl.rxfirebaseandroid.test.TestUtil.await;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WriteTests {

    public @Rule PlayServicesRule playServicesRule = new PlayServicesRule();
    public @Rule FirebaseRule firebaseRule = new FirebaseRule();
    private final DatabaseReference reference = firebaseRule.reference;

    @Test
    public void testObserve() throws Exception {
        ReplaySubject<String> values = ReplaySubject.create();

        observe(reference)
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
                        setValue(reference, null),
                        setValue(reference, "foo"),
                        setValue(reference, "bar"),
                        setValue(reference, null)
                )
                .subscribe(Subscribers.<Void>empty());

        List<String> observedValues = await(values.take(4).toList());

        assertThat(observedValues, contains(null, "foo", "bar", null));
    }

    @Test
    public void testObserveChildren() throws Exception {
        BehaviorSubject<FirebaseChildEvent<DataSnapshot>> events = BehaviorSubject.create();

        observeChildren(reference)
                .subscribe(events);

        await(setValue(reference.child("foo"), "bar"));
        FirebaseChildEvent<DataSnapshot> add = events.getValue();
        assertThat(add.eventType, is(FirebaseChildEvent.TYPE_ADD));
        assertThat(add.value.getValue(String.class), is("bar"));
        assertThat(add.value.getKey(), is("foo"));

        await(setValue(reference.child("foo"), "baz"));
        FirebaseChildEvent<DataSnapshot> edit = events.getValue();
        assertThat(edit.eventType, is(FirebaseChildEvent.TYPE_CHANGE));
        assertThat(edit.value.getValue(String.class), is("baz"));

        await(setValue(reference.child("foo"), null));
        FirebaseChildEvent<DataSnapshot> remove = events.getValue();
        assertThat(remove.eventType, is(FirebaseChildEvent.TYPE_REMOVE));
    }

    @Test
    public void testUpdateChildren() throws Exception {
        await(setValue(reference.child("foo"), "bar"));
        await(setValue(reference.child("baz"), "potato"));

        await(updateChildren(reference, Collections.singletonMap("baz", "tomato")));

        DataSnapshot content = await(observe(reference).take(1));
        assertThat(content.child("foo").getValue(String.class), is("bar"));
        assertThat(content.child("baz").getValue(String.class), is("tomato"));
    }
}
