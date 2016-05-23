package lt.dariusl.rxfirebase.test;

import rx.Observable;

public class TestUtil {
    public static <T> T await(Observable<T> observable) {
        return observable.toBlocking().single();
    }
}
