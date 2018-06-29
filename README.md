# 疯狂猜成语


###这个应用可以使用两种玩法来玩：

1、手动点击填字

2、直接点击右下角，出现语音条开始说话，自动将语音转换为文字，填充。

###下载二维码

这是一个没有压缩的包，有10M

![开始](https://github.com/bravekingzhang/chengyu/blob/master/qrcode.png)

###相关展示

![开始](https://github.com/bravekingzhang/chengyu/blob/master/splash1.jpg)

![开始](https://github.com/bravekingzhang/chengyu/blob/master/splash2.jpg)

###### 语音填字

![开始](https://github.com/bravekingzhang/chengyu/blob/master/splash4.jpg)


### 功能简介

##### 题目数据

题目数据是从相关网站上爬取的，因此，笔者没有版权，所以改项目仅供学习使用，拒绝商业用途。

##### 自己二次开发

需要自己到腾讯云去注册语音识别，然后修改这里的相关参数就ok，这个语音识别控件我封装好了，当然，感兴趣
可以自己在优化一下。温馨提示，不要用我的key，我扛不住你们那么多请求。求轻拍。

```java
 /**
     * 初始化语音转文本控件，大哥们可以用自己的id和key，setSecretKey建议在服务器端生成，别像我这样写在这里，不安全
     */
    private void initVoiceTextView() {
        mVoiceToTextView
                .setActivity(this)
                .setAppid(0)
                .setProjectid(0)
                .setSecretId("")
                .setSecretKey("")
                .setListener(new VoiceToTextListener() {
                    @Override
                    public void onText(String text) {
                        if (!TextUtils.isEmpty(text) && !TextUtils.equals(text, mLastVoiceText)) {
                            pSubject.onNext(text);
                        }
                    }
                })
                .build();
    }

```

##### 相关技术简介

1、题目是.json格式保存到服务端的，初次需要下载，（我的服务器是常年关闭的，哈哈，因此你下载不到题目，需要我可以通知我开一下）。
这里采用DownloadManager来下载，下载之后进行解析（使用moshi）。解析出来之后哦，保存到数据库中。

2、数据库使用GreenDao2.0，一开始采用realm，使用起来还是不如GreenDao顺手，为什么需要使用数据库，因为，你只需要加载一道题目到内存而已，没必要把所有的题目都加载。
有人说可以使用pref，我建议你感兴趣就试试吧。

3、因为语言识别的开启和关闭较为复杂，里面用到了锁机制。

4、语言识别会不断的向你发送识别的结果，，识别玩自动关闭等等因此，如何妥善处理，rxjava？？还是看源码，也许你还可以优化，代码有点烂哦

5、其他技术，总线、Lottle动画、弹框
