可高度ui定制播放器<br>
使用方法<br>
1.在布局中引入
    <com.android.tedcoder.wkvideoplayer.view.SuperVideoPlayer
    
                    android:layout_width="match_parent"
                    android:layout_height="200dp"
                    />
2.初始化并加载网络视频<br>
    video = new Video();<br>
    video.setVideoName(mTopicItem.getTitle());<br>
    VideoUrl videoUrl = new VideoUrl();<br>
    videoUrl.setFormatUrl(videoURL + ".f30.mp4");<br>

    ArrayList<VideoUrl> list = new ArrayList<>();
    list.add(videoUrl);

    video.setVideoUrl(list);

    mSuperVideoPlayer.loadMultipleVideo(video);

 3.ui可进行高度定制，驰骋吧！
