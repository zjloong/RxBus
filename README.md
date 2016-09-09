# RxBus
基于rxjava的事件管理

* 注册事件:

            //public <T> Observable<T> register(Object tag, Action1<T> action1)
            RxBus.getDefault().register(this, new Action1<Student>() {
                @Override
                public void call(Student s) {
                    Log.e("---","onEvent: " + s);
                }
            });

* 注销事件

            //public void unregister(Object tag)
            RxBus.getDefault().unregister(this);
            
* 发出事件

            //public void post(Object tag, Object content)
            //public void post(Object content)
            RxBus.getDefault().post(new Student("张三",33));
