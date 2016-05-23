package lt.dariusl.rxfirebaseandroid;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.functions.Func1;
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

        public static  <V> FirebaseChildEvent<V> cast(
                FirebaseChildEvent<com.google.firebase.database.DataSnapshot> event,
                Class<V> cls) {
            return event.withValue(event.value.getValue(cls));
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

    public static Observable<FirebaseChildEvent<com.google.firebase.database.DataSnapshot>> observeChildren(final com.google.firebase.database.Query query) {
        return Observable.create(new Observable.OnSubscribe<FirebaseChildEvent<com.google.firebase.database.DataSnapshot>>() {
            @Override
            public void call(final Subscriber<? super FirebaseChildEvent<com.google.firebase.database.DataSnapshot>> subscriber) {
                final com.google.firebase.database.ChildEventListener childEventListener = query.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
                    @Override
                    public void onChildAdded(com.google.firebase.database.DataSnapshot dataSnapshot, String prevName) {
                        subscriber.onNext(new FirebaseChildEvent<>(dataSnapshot, FirebaseChildEvent.TYPE_ADD, prevName));
                    }

                    @Override
                    public void onChildChanged(com.google.firebase.database.DataSnapshot dataSnapshot, String prevName) {
                        subscriber.onNext(new FirebaseChildEvent<>(dataSnapshot, FirebaseChildEvent.TYPE_CHANGE, prevName));
                    }

                    @Override
                    public void onChildRemoved(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        subscriber.onNext(new FirebaseChildEvent<>(dataSnapshot, FirebaseChildEvent.TYPE_REMOVE, null));
                    }

                    @Override
                    public void onChildMoved(com.google.firebase.database.DataSnapshot dataSnapshot, String prevName) {
                        subscriber.onNext(new FirebaseChildEvent<>(dataSnapshot, FirebaseChildEvent.TYPE_MOVE, prevName));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        subscriber.onError(new DatabaseException(databaseError));
                    }
                });
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        query.removeEventListener(childEventListener);
                    }
                }));
            }
        });
    }

    public static Func1<FirebaseChildEvent, Boolean> makeEventFilter(final @FirebaseChildEvent.EventType int eventType) {
        return new Func1<FirebaseChildEvent, Boolean>() {

            @Override
            public Boolean call(FirebaseChildEvent firebaseChildEvent) {
                return firebaseChildEvent.eventType == eventType;
            }
        };
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

    public static Observable<Void> updateChildren(final DatabaseReference reference, final Map<String, ?> children) {
        return toObservable(new Func0<Task<Void>>() {
            @Override
            public Task<Void> call() {
                //noinspection unchecked
                return reference.updateChildren((Map<String, Object>) children);
            }
        });
    }

    public static Observable<FirebaseAuth> observeAuthChange(final FirebaseAuth auth) {
        return Observable
                .create(new Observable.OnSubscribe<FirebaseAuth>() {
                    @Override
                    public void call(final Subscriber<? super FirebaseAuth> subscriber) {
                        final FirebaseAuth.AuthStateListener listener = new FirebaseAuth.AuthStateListener() {
                            @Override
                            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                                subscriber.onNext(firebaseAuth);
                            }
                        };
                        subscriber.add(Subscriptions.create(new Action0() {
                            @Override
                            public void call() {
                                auth.removeAuthStateListener(listener);
                            }
                        }));

                        auth.addAuthStateListener(listener);
                    }
                })
                .startWith(auth);
    }

    public static Observable<FirebaseUser> observeAuth(FirebaseAuth auth) {
        return observeAuthChange(auth)
                .map(new Func1<FirebaseAuth, FirebaseUser>() {
                    @Override
                    public FirebaseUser call(FirebaseAuth firebaseAuth) {
                        return firebaseAuth.getCurrentUser();
                    }
                })
                .distinctUntilChanged();
    }

    public static Observable<AuthResult> authAnonymously(final FirebaseAuth firebaseAuth){
        return toObservable(new Func0<Task<AuthResult>>() {
            @Override
            public Task<AuthResult> call() {
                return firebaseAuth.signInAnonymously();
            }
        });
    }

    public static Observable<AuthResult> authWithCredential(final FirebaseAuth firebaseAuth, final AuthCredential credential) {
        return toObservable(new Func0<Task<AuthResult>>() {
            @Override
            public Task<AuthResult> call() {
                return firebaseAuth.signInWithCredential(credential);
            }
        });
    }

    private static <T> Observable<T> toObservable(Func0<? extends Task<T>> factory) {
        return Observable.create(new TaskOnSubscribe<>(factory));
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

}