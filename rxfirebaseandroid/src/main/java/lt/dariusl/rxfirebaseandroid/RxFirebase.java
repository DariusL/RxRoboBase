package lt.dariusl.rxfirebaseandroid;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.Subscriptions;

/**
 * Fork of https://gist.github.com/gsoltis/86210e3259dcc6998801
 */
public class RxFirebase {

    public static class FirebaseChildEvent <T> {
        @Retention(RetentionPolicy.SOURCE)
        @IntDef({TYPE_ADD, TYPE_CHANGE, TYPE_MOVE, TYPE_REMOVE})
        public @interface EventType{}

        public static final int TYPE_ADD = 1;
        public static final int TYPE_CHANGE = 2;
        public static final int TYPE_REMOVE = 3;
        public static final int TYPE_MOVE = 4;

        public final T value;
        public final @EventType int eventType;
        public final String prevName;

        public FirebaseChildEvent(T value, @EventType int eventType, String prevName) {
            this.value = value;
            this.eventType = eventType;
            this.prevName = prevName;
        }

        public <V> FirebaseChildEvent<V> withValue(V value){
            return new FirebaseChildEvent<>(value, eventType, prevName);
        }
    }

    public static class FirebaseException extends RuntimeException {
        private final FirebaseError error;

        public FirebaseException(FirebaseError error) {
            super("Firebase error " + error, error.toException());
            this.error = error;
        }

        public FirebaseError getError() {
            return error;
        }
    }

    public static class DatabaseException extends IOException {
        private final DatabaseError error;

        public DatabaseException(DatabaseError error) {
            super(error.getMessage() + "\n" + error.getDetails(), error.toException());
            this.error = error;
        }

        public DatabaseError getError() {
            return error;
        }
    }

    public static Observable<FirebaseChildEvent<DataSnapshot>> observeChildren(final Query ref) {
        return Observable.create(new Observable.OnSubscribe<FirebaseChildEvent<DataSnapshot>>() {

            @Override
            public void call(final Subscriber<? super FirebaseChildEvent<DataSnapshot>> subscriber) {
                final ChildEventListener listener = ref.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String prevName) {
                        subscriber.onNext(new FirebaseChildEvent<>(dataSnapshot, FirebaseChildEvent.TYPE_ADD, prevName));
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String prevName) {
                        subscriber.onNext(new FirebaseChildEvent<>(dataSnapshot, FirebaseChildEvent.TYPE_CHANGE, prevName));
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        subscriber.onNext(new FirebaseChildEvent<>(dataSnapshot, FirebaseChildEvent.TYPE_REMOVE, null));
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String prevName) {
                        subscriber.onNext(new FirebaseChildEvent<>(dataSnapshot, FirebaseChildEvent.TYPE_MOVE, prevName));
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                        subscriber.onError(new FirebaseException(error));
                    }
                });

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        ref.removeEventListener(listener);
                    }
                }));
            }
        });
    }

    private static Func1<FirebaseChildEvent, Boolean> makeEventFilter(final @FirebaseChildEvent.EventType int eventType) {
        return new Func1<FirebaseChildEvent, Boolean>() {

            @Override
            public Boolean call(FirebaseChildEvent firebaseChildEvent) {
                return firebaseChildEvent.eventType == eventType;
            }
        };
    }

    public static Observable<FirebaseChildEvent<DataSnapshot>> observeChildAdded(Query ref) {
        return observeChildren(ref).filter(makeEventFilter(FirebaseChildEvent.TYPE_ADD));
    }

    public static Observable<FirebaseChildEvent<DataSnapshot>> observeChildChanged(Query ref) {
        return observeChildren(ref).filter(makeEventFilter(FirebaseChildEvent.TYPE_CHANGE));
    }

    public static Observable<FirebaseChildEvent<DataSnapshot>> observeChildMoved(Query ref) {
        return observeChildren(ref).filter(makeEventFilter(FirebaseChildEvent.TYPE_MOVE));
    }

    public static Observable<FirebaseChildEvent<DataSnapshot>> observeChildRemoved(Query ref) {
        return observeChildren(ref).filter(makeEventFilter(FirebaseChildEvent.TYPE_REMOVE));
    }

    public static Observable<com.google.firebase.database.DataSnapshot> observe(final com.google.firebase.database.Query query) {
        return Observable.create(new Observable.OnSubscribe<com.google.firebase.database.DataSnapshot>() {
            @Override
            public void call(final Subscriber<? super com.google.firebase.database.DataSnapshot> subscriber) {
                final com.google.firebase.database.ValueEventListener listener = query.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        subscriber.onNext(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        subscriber.onError(new DatabaseException(databaseError));
                    }
                });

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        query.removeEventListener(listener);
                    }
                }));
            }
        });
    }

    public static Observable<Void> setValue(final DatabaseReference reference, final Object value) {
        return toObservable(new Func0<Task<Void>>() {
            @Override
            public Task<Void> call() {
                return reference.setValue(value);
            }
        });
    }

    public static Observable<Firebase> updateChildren(Firebase firebase, Map<String, Object> children){
        final BehaviorSubject<Firebase> subject = BehaviorSubject.create();
        firebase.updateChildren(children, new ObservableCompletionListener(subject));
        return subject;
    }

    public static Observable<AuthData> observeAuth(final Firebase firebase){
        return Observable.create(new Observable.OnSubscribe<AuthData>() {
            @Override
            public void call(final Subscriber<? super AuthData> subscriber) {
                final Firebase.AuthStateListener listener = firebase.addAuthStateListener(new Firebase.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(AuthData authData) {
                        subscriber.onNext(authData);
                    }
                });

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        firebase.removeAuthStateListener(listener);
                    }
                }));
            }
        }).startWith(firebase.getAuth()).distinctUntilChanged();
    }

    public static Observable<AuthResult> authAnonymously(final FirebaseAuth firebaseAuth){
        return Observable.create(new TaskOnSubscribe<>(new Func0<Task<AuthResult>>() {
            @Override
            public Task<AuthResult> call() {
                return firebaseAuth.signInAnonymously();
            }
        }));
    }

    public static Observable<AuthResult> authWithCredential(final FirebaseAuth firebaseAuth, final AuthCredential credential) {
        return Observable.create(new TaskOnSubscribe<>(new Func0<Task<AuthResult>>() {
            @Override
            public Task<AuthResult> call() {
                return firebaseAuth.signInWithCredential(credential);
            }
        }));
    }

    private static <T> Observable<T> toObservable(Func0<? extends Task<T>> factory) {
        return Observable.create(new TaskOnSubscribe<T>(factory));
    }

    private static class TaskOnSubscribe<T> implements Observable.OnSubscribe<T> {
        private final Func0<? extends Task<T>> taskFactory;

        private TaskOnSubscribe(Func0<? extends Task<T>> taskFactory) {
            this.taskFactory = taskFactory;
        }

        @Override
        public void call(final Subscriber<? super T> subscriber) {
            taskFactory.call().addOnCompleteListener(new OnCompleteListener<T>() {
                @Override
                public void onComplete(@NonNull Task<T> task) {
                    if (task.isSuccessful()) {
                        subscriber.onNext(task.getResult());
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(task.getException());
                    }
                }
            });
        }
    }

    private static class ObservableCompletionListener implements Firebase.CompletionListener{

        private final Observer<Firebase> observer;

        private ObservableCompletionListener(Observer<Firebase> observer) {
            this.observer = observer;
        }

        @Override
        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
            if (firebaseError == null){
                observer.onNext(firebase);
                observer.onCompleted();
            } else {
                observer.onError(new FirebaseException(firebaseError));
            }
        }
    }
}