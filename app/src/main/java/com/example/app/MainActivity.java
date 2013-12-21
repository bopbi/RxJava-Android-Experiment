package com.example.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.concurrency.AndroidSchedulers;
import rx.concurrency.Schedulers;
import rx.util.functions.Func1;

public class MainActivity extends Activity implements Observer<Bitmap> {

    private LinearLayout linearLayout;

    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        linearLayout = (LinearLayout) findViewById(R.id.layout);

        subscription = Observable.from(1,2,3,4,5).mapMany(new LoadImage())
                .subscribeOn(Schedulers.threadPoolForIO())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);

    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        Log.i("KULINR", "Destroyed");
        super.onDestroy();

    }

    @Override
    public void onCompleted() {
        Log.i("KULINR", "onComplete");
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onNext(Bitmap bitmap) {
        if (bitmap != null) {
            Log.i("KULINR", "onNext");
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);
            linearLayout.addView(imageView);
        } else {
            Toast.makeText(this, "Error! See logs.", Toast.LENGTH_SHORT).show();
        }
    }

    private class LoadImage implements Func1<Integer, Observable<Bitmap>> {


        @Override
        public Observable<Bitmap> call(Integer integer) {
            return Observable.create(new Observable.OnSubscribeFunc<Bitmap>() {

                @Override
                public Subscription onSubscribe(final Observer<? super Bitmap> observer) {
                    final Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                HttpURLConnection connection =
                                        (HttpURLConnection) new URL("http://lorempixel.com/100/100/")
                                                .openConnection();
                                connection.connect();
                                InputStream input = connection.getInputStream();
                                observer.onNext(BitmapFactory.decodeStream(input));
                                observer.onCompleted();

                            } catch (IOException e) {
                                e.printStackTrace();

                            }
                        }
                    });
                    t.start();

                    boolean running = true;
                    if (running) {

                    }
                    return new Subscription() {
                        @Override
                        public void unsubscribe() {
                            t.interrupt();
                        }
                    };
                }
            });
        }
    }
}
