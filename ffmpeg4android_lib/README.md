视频处理<br>
实现原理是利用ffmpeg命令来实现视频压缩，旋转等效果<br>
为了偷懒，ffmpeg命令教程在当前工程根目录 ppt文档里<br>

库工程使用方法<br>
LoadJNI vk = new LoadJNI();<br>
vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, getApplicationContext());<br>
ps:commandStr是执行的ffmpeg命令,视频操作是耗时操作记得放到子线程。<br>

    详细使用说明打开此链接 http://androidwarzone.blogspot.sg/2011/12/ffmpeg4android.html
    另外提供给大家一个Demo地址（需要翻墙）https://drive.google.com/file/d/0B9qAjo6wKhk9TUQzX0ludUJkOGc/view