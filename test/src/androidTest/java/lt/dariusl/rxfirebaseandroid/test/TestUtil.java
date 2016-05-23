package lt.dariusl.rxfirebaseandroid.test;

import rx.Observable;

public class TestUtil {
    public static <T> T await(Observable<T> observable) {
        return observable.toBlocking().single();
    }
}
