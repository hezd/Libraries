友盟的一个分享工具类<br>
快速集成分享的利器<br>

使用方法<br>
1.在清单文件Menifest中配置UMENG_CHANNEL和UMENG_MESSAGE_SECRET<br>
2.在Application的oncrete方法中配置要分享的各平台的key和screte key以及重定向url，例如<br>
  PlatformConfig.setWeixin("", "");<br>
  PlatformConfig.setQQZone("", "");<br>
  PlatformConfig.setSinaWeibo("", "");<br>
  Config.REDIRECT_URL = "http://sns.ooxx.com/sina2/callback";<br>
3.分享<br>
    设置标题、副标题、显示图片、打开后的h5连接<br>
    new ShareAction(activity)
                    .setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.SINA)
                    .withTitle(")
                    .withText("")
                    .withMedia("http://ooxx.img")
                    .withTargetUrl("http://ooxx.html").setCallback(umShareListener).open();

4.结束，尽情分享吧！
