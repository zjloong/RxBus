package com.loong.rxbus;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
import rx.subscriptions.CompositeSubscription;

/**
 * Describe the function of the class
 *
 * @author zhujinlong@ichoice.com
 * @date 2016/8/1
 * @time 11:44
 * @description Describe the place where the class needs to pay attention.
 */
public class RxBus {

    private volatile static RxBus event;

    public synchronized static RxBus getDefault() {
        if (event == null) {
            synchronized (RxBus.class) {
                if (event == null) {
                    event = new RxBus();
                }
            }
        }
        return event;
    }

    private ConcurrentHashMap<Object, List<Subject>> container = new ConcurrentHashMap<Object, List<Subject>>();

    private ConcurrentHashMap<Object, CompositeSubscription> subscriptions = new ConcurrentHashMap<Object, CompositeSubscription>();

    public <T> Observable<T> register(Object tag, Action1<T> action1){
        List<Subject> subjects = this.container.get(tag);
        if(subjects == null){
            subjects = new ArrayList<>();
            container.put(tag,subjects);
        }
        CompositeSubscription compositeSubscription = subscriptions.get(tag);
        if(compositeSubscription == null){
            compositeSubscription = new CompositeSubscription();
            subscriptions.put(tag,compositeSubscription);
        }
        Subject<T, T> subject = PublishSubject.create();
        compositeSubscription.add(subject.observeOn(AndroidSchedulers.mainThread()).subscribe(action1, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                //throwable.printStackTrace();
                Log.e("---","与注册数据类型不符合,不响应事件!");
            }
        }));
        subjects.add(subject);
        return subject;
    }

    public void unregister(Object tag){
        List<Subject> subjects = container.get(tag);
        if(subjects != null){
            subjects.clear();
            container.remove(tag);
        }
        CompositeSubscription compositeSubscription = subscriptions.get(tag);
        if(compositeSubscription != null && compositeSubscription.hasSubscriptions()){
            compositeSubscription.unsubscribe();
            compositeSubscription.clear();
            subscriptions.remove(tag);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void post(Object tag, Object content){
        List<Subject> subjects = container.get(tag);
        if(subjects != null && !subjects.isEmpty()){
            for (Subject s : subjects) {
                s.onNext(content);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void post(Object content){
        if(!container.isEmpty()){
            for (ConcurrentHashMap.Entry<Object, List<Subject>> entry : container.entrySet()) {
                List<Subject> value = entry.getValue();
                if(value != null && !value.isEmpty()){
                    for (Subject s : value) {
                        s.onNext(content);
                    }
                }
            }
        }
    }
}