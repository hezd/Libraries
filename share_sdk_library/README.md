友盟的一个分享工具类
快速集成分享的利器

使用方法
1.在清单文件Menifest中配置UMENG_CHANNEL和UMENG_MESSAGE_SECRET
2.在Application的oncrete方法中配置要分享的各平台的key和screte key以及重定向url，例如
    PlatformConfig.setWeixin("", "");
    PlatformConfig.setQQZone("", "");
    PlatformConfig.setSinaWeibo("", "");
    Config.REDIRECT_URL = "http://sns.ooxx.com/sina2/callback";
3.分享
    设置标题、副标题、显示图片、打开后的h5连接
    new ShareAction(activity)
                    .setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.SINA)
                    .withTitle(")
                    .withText("")
                    .withMedia("http://ooxx.img")
                    .withTargetUrl("http://ooxx.html").setCallback(umShareListener).open();
4.结束，尽情分享吧！
