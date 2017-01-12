/*
 *
 * Copyright 2015 TedXiong xiong-wei@hotmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tedcoder.wkvideoplayer.view;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.android.tedcoder.wkvideoplayer.R;
import com.android.tedcoder.wkvideoplayer.model.Video;
import com.android.tedcoder.wkvideoplayer.model.VideoUrl;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ted on 2015/8/6.
 * SuperVideoPlayer
 */
public class SuperVideoPlayer extends RelativeLayout {

    private final int MSG_HIDE_CONTROLLER = 1;
    private final int MSG_UPDATE_PLAY_TIME = 2;
    private Context mContext;
    public MediaController.PageType mCurrPageType = MediaController.PageType.SHRINK;//当前是横屏还是竖屏
    private MediaController mMediaController;
    private VideoPlayCallbackImpl mVideoPlayCallback;
    private SuperVideoView mSuperVideoView;
    private View mProgressBarView;
    private Timer mUpdateTimer;
    private Video mNowPlayVideo;
    /**标题栏*/
    private View mTitleView;
    //是否自动隐藏控制栏
    private boolean mAutoHideController = true;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE_PLAY_TIME) {
                updatePlayTime();
                updatePlayProgress();
            } else if (msg.what == MSG_HIDE_CONTROLLER) {
                showOrHideController();
            }
            return false;
        }
    });

    public MediaController.MediaControlImpl mMediaControl = new MediaController.MediaControlImpl() {
        @Override
        public void alwaysShowController() {
            SuperVideoPlayer.this.alwaysShowController();
        }

        @Override
        public void onSelectFormat(int position) {
            VideoUrl videoUrl = mNowPlayVideo.getVideoUrl().get(position);
            if (mNowPlayVideo.getPlayUrl().equal(videoUrl)) return;
            mNowPlayVideo.setPlayUrl(position);
            playVideoAtLastPos();
        }

        @Override
        public void onPlayTurn() {
            if (mSuperVideoView.isPlaying()) {
                pausePlay(true);
            } else {
                goOnPlay();
            }
        }

        @Override
        public void onPageTurn() {
            mVideoPlayCallback.onSwitchPageType();
        }

        @Override
        public void onProgressTurn(MediaController.ProgressState state, int progress) {
            if (state.equals(MediaController.ProgressState.START)) {
                mHandler.removeMessages(MSG_HIDE_CONTROLLER);
            } else if (state.equals(MediaController.ProgressState.STOP)) {
                resetHideTimer();
            } else {
                int time = progress * mSuperVideoView.getDuration() / 100;
                mSuperVideoView.seekTo(time);
                Log.d("video","onProgessTurn");
                updatePlayTime();
            }
        }
    };

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START
                            || what == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
                        mProgressBarView.setVisibility(View.GONE);
                        return true;
                    }
                    return false;
                }
            });
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            stopUpdateTimer();
            stopHideTimer(true);
            mMediaController.playFinish(mSuperVideoView.getDuration());
            mVideoPlayCallback.onPlayFinish();
        }
    };

    public void setVideoPlayCallback(VideoPlayCallbackImpl videoPlayCallback) {
        mVideoPlayCallback = videoPlayCallback;
    }

    /**
     * 如果在地图页播放视频，请先调用该接口
     */
    @SuppressWarnings("unused")
    public void setSupportPlayOnSurfaceView() {
        mSuperVideoView.setZOrderMediaOverlay(true);
    }

    @SuppressWarnings("unused")
    public SuperVideoView getSuperVideoView() {
        return mSuperVideoView;
    }

    public void setPageType(MediaController.PageType pageType) {
        mMediaController.setPageType(pageType);
        mCurrPageType = pageType;
    }

    /***
     * 强制横屏模式
     */
    @SuppressWarnings("unused")
    public void forceLandscapeMode() {
        mMediaController.forceLandscapeMode();
    }

    /***
     * 播放本地视频 只支持横屏播放
     *
     * @param fileUrl fileUrl
     */
    @SuppressWarnings("unused")
    public void loadLocalVideo(String fileUrl) {
        VideoUrl videoUrl = new VideoUrl();
        videoUrl.setIsOnlineVideo(false);
        videoUrl.setFormatUrl(fileUrl);
        videoUrl.setFormatName("本地视频");
        Video video = new Video();
        ArrayList<VideoUrl> videoUrls = new ArrayList<>();
        videoUrls.add(videoUrl);
        video.setVideoUrl(videoUrls);
        video.setPlayUrl(0);

        mNowPlayVideo = video;

        /***
         * 初始化控制条的精简模式
         */
        mMediaController.initTrimmedMode();
        loadAndPlay(mNowPlayVideo.getPlayUrl(), 0);
    }

    /**
     * 播放多个视频,默认播放第一个视频，第一个格式
     *
     * @param playVideo 播放的视频
     */
    public void loadMultipleVideo(Video playVideo) {
        loadMultipleVideo(playVideo, 0);
    }

    /**
     * 播放多个视频
     *
     * @param playVideo    播放的视频
     * @param selectFormat 指定的格式
     */
    public void loadMultipleVideo(Video playVideo, int selectFormat) {
        loadMultipleVideo(playVideo, selectFormat, 0);
    }

    /***
     * @param playVideo    播放的视频
     * @param selectFormat 指定的格式
     * @param seekTime     开始进度
     */
    public void loadMultipleVideo(Video playVideo, int selectFormat, int seekTime) {
        mNowPlayVideo = playVideo;
        mNowPlayVideo.setPlayUrl(selectFormat);
        mMediaController.initPlayVideo(mNowPlayVideo);
        loadAndPlay(mNowPlayVideo.getPlayUrl(), seekTime);
    }

    /**
     * 获取当前播放视频url
     */
    public String getPlayUrl() {
        return mNowPlayVideo.getPlayUrl().getFormatUrl();
    }

    /**
     * 获取当前播放至**秒
     *
     * @return
     */
    public int getCurrentSeconds() {
        return mSuperVideoView.getCurrentPosition() / 1000;
    }

    /**
     * 获取当前播放至**毫秒
     *
     * @return
     */
    public int getCurrentMsec() {
        return mSuperVideoView.getCurrentPosition() ;
    }

    /**
     * 暂停播放
     *
     * @param isShowController 是否显示控制条
     */
    public void pausePlay(boolean isShowController) {
        mSuperVideoView.pause();
        mMediaController.setPlayState(MediaController.PlayState.PAUSE);
        stopHideTimer(isShowController);
    }

    /***
     * 继续播放
     */
    public void goOnPlay() {
        mSuperVideoView.start();
        mMediaController.setPlayState(MediaController.PlayState.PLAY);
        resetHideTimer();
        resetUpdateTimer();
    }

    public void resume() {
        mSuperVideoView.resume();
    }

    public boolean isAutoHideController() {
        return mAutoHideController;
    }

    public void setAutoHideController(boolean autoHideController) {
        mAutoHideController = autoHideController;
    }

    public SuperVideoPlayer(Context context) {
        super(context);
        initView(context);
    }

    public SuperVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public SuperVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        View.inflate(context, R.layout.super_vodeo_player_layout, this);
        mSuperVideoView = (SuperVideoView) findViewById(R.id.video_view);
        mMediaController = (MediaController) findViewById(R.id.controller);
        mProgressBarView = findViewById(R.id.progressbar);
        mMediaController.setMediaControl(mMediaControl);
    }

    /**
     * 更换清晰度地址时，续播
     */
    private void playVideoAtLastPos() {
        int playTime = mSuperVideoView.getCurrentPosition();
        mSuperVideoView.stopPlayback();
        loadAndPlay(mNowPlayVideo.getPlayUrl(), playTime);
    }

    /**
     * 加载并开始播放视频
     *
     * @param videoUrl videoUrl
     */
    private void loadAndPlay(VideoUrl videoUrl, int seekTime) {
        mProgressBarView.setVisibility(VISIBLE);
        if (TextUtils.isEmpty(videoUrl.getFormatUrl())) {
            Log.e("TAG", "videoUrl should not be null");
            return;
        }
        mSuperVideoView.setOnPreparedListener(mOnPreparedListener);
        if (videoUrl.isOnlineVideo()) {
            mSuperVideoView.setVideoPath(videoUrl.getFormatUrl());
        } else {
            Uri uri = Uri.parse(videoUrl.getFormatUrl());
            mSuperVideoView.setVideoURI(uri);
        }
        mSuperVideoView.setVisibility(VISIBLE);
        startPlayVideo(seekTime);
    }

    public void dismissProgress() {
        mProgressBarView.setVisibility(View.GONE);
    }

    /**
     * 播放视频
     * should called after setVideoPath()
     */
    public void startPlayVideo(int seekTime) {
        if (null == mUpdateTimer) resetUpdateTimer();
        resetHideTimer();
        mSuperVideoView.setOnCompletionListener(mOnCompletionListener);
        mSuperVideoView.start();
        if (seekTime > 0) {
            mSuperVideoView.seekTo(seekTime);
        }
        mMediaController.setPlayState(MediaController.PlayState.PLAY);
    }

    /**
     * 滑动屏幕更新播放的进度时间
     *
     * @param timeShow
     * @param movePercent
     */
    public void updateSeekTime(int timeShow, float movePercent) {
        int currentTime = mSuperVideoView.getCurrentPosition();
        int allTime = mSuperVideoView.getDuration();
        int playTime = currentTime;
        if (timeShow == 0) {
            playTime = currentTime;
        } else if (timeShow == 1) { //快进
            playTime = (int) (currentTime + allTime * movePercent);
            playTime = playTime > allTime ? allTime : playTime;
        } else if (timeShow == 2) { //快退
            playTime = (int) (currentTime - allTime * movePercent);
            playTime = playTime < 0 ? 0 : playTime;
        }
        mMediaController.setPlayProgressTxt(playTime, allTime);
        if (playTime != allTime && mVideoPlayCallback != null) {
            mVideoPlayCallback.onUpdateTime(timeShow, playTime, allTime);
        }
    }

    /**
     * 滑动屏幕更新播放进度条
     *
     * @param timeShow
     * @param movePercent
     */
    public void updateSeekProgress(int timeShow, float movePercent) {
        int currentTime = mSuperVideoView.getCurrentPosition();
        int allTime = mSuperVideoView.getDuration();
        int playTime = currentTime;
        if (timeShow == 0) {
            playTime = currentTime;
        } else if (timeShow == 1) { //快进
            playTime = (int) (currentTime + allTime * movePercent);
            playTime = playTime > allTime ? allTime : playTime;
        } else if (timeShow == 2) { //快退
            playTime = (int) (currentTime - allTime * movePercent);
            playTime = playTime < 0 ? 0 : playTime;
        }
        mSuperVideoView.seekTo(playTime);
        int loadProgress = mSuperVideoView.getBufferPercentage();
        int progress = playTime * 100 / allTime;
        mMediaController.setProgressBar(progress, loadProgress);
    }

    /**
     * 更新播放的进度时间
     */
    private void updatePlayTime() {
        int allTime = mSuperVideoView.getDuration();
        int playTime = mSuperVideoView.getCurrentPosition();
        mMediaController.setPlayProgressTxt(playTime, allTime);
        if (playTime != allTime && mVideoPlayCallback != null) {
            mVideoPlayCallback.onUpdateTime(0, playTime, allTime);
        }
    }

    /**
     * 更新播放进度条
     */
    private void updatePlayProgress() {
        int allTime = mSuperVideoView.getDuration();
        int playTime = mSuperVideoView.getCurrentPosition();
        int loadProgress = mSuperVideoView.getBufferPercentage();
        int progress = playTime * 100 / allTime;
        mMediaController.setProgressBar(progress, loadProgress);
    }

    /**
     * 双击切换暂停/播放
     */
    public void playTurn() {
        mMediaControl.onPlayTurn();
        mMediaController.setVisibility(VISIBLE);
    }

    /**
     * 控制栏是否显示状态
     *
     * @return
     */
    public boolean getMediaControllerVisibility() {
        return mMediaController.getVisibility() == View.VISIBLE;
    }

    public void setmTitleView(View titleView) {
        this.mTitleView = titleView;
    }

    /***
     * 是否显示控制栏
     */
    public boolean showOrHideController() {
        mMediaController.closeAllSwitchList();
        boolean visible = getMediaControllerVisibility();
        Activity activity = (Activity) mContext;
        int requestedOrientation = activity.getRequestedOrientation();
        if (visible) {
            mMediaController.clearAnimation();
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_exit_from_bottom);
            animation.setAnimationListener(new AnimationImp() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    mMediaController.setVisibility(View.GONE);
                }
            });
            mMediaController.startAnimation(animation);
            if(mTitleView!=null&&mTitleView.getVisibility()==VISIBLE&&
                    requestedOrientation== ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mTitleView.clearAnimation();
                Animation titlanimation = AnimationUtils.loadAnimation(mContext, R.anim.anim_exit_from_top);
                titlanimation.setAnimationListener(new AnimationImp() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        super.onAnimationEnd(animation);
                        mTitleView.setVisibility(View.GONE);
                    }
                });
                mTitleView.startAnimation(titlanimation);
            }
            return false;
        } else {
            mMediaController.clearAnimation();
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_enter_from_bottom);
            animation.setAnimationListener(new AnimationImp() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    mMediaController.setVisibility(View.VISIBLE);
                }
            });
            mMediaController.startAnimation(animation);
            resetHideTimer();

            if(mTitleView!=null&&mTitleView.getVisibility()!=VISIBLE&&
                    requestedOrientation== ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mTitleView.clearAnimation();
                Animation topExitanimation = AnimationUtils.loadAnimation(mContext, R.anim.anim_enter_from_top);
                topExitanimation.setAnimationListener(new AnimationImp() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        super.onAnimationEnd(animation);
                        mTitleView.setVisibility(View.VISIBLE);
                    }
                });
                mTitleView.startAnimation(topExitanimation);
            }

            return true;
        }
    }

    /**
     * 总是显示控制栏
     */
    private void alwaysShowController() {
        mHandler.removeMessages(MSG_HIDE_CONTROLLER);
        mMediaController.setVisibility(View.VISIBLE);
    }

    /**
     * 重置隐藏时间
     */
    private void resetHideTimer() {
        if (!isAutoHideController()) return;
        mHandler.removeMessages(MSG_HIDE_CONTROLLER);
        int TIME_SHOW_CONTROLLER = 2000;
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLLER, TIME_SHOW_CONTROLLER);
    }

    /**
     * 停止隐藏时间
     *
     * @param isShowController
     */
    private void stopHideTimer(boolean isShowController) {
        mHandler.removeMessages(MSG_HIDE_CONTROLLER);
        mMediaController.clearAnimation();
        mMediaController.setVisibility(isShowController ? View.VISIBLE : View.GONE);
    }

    /**
     * 重置更新时间
     */
    private void resetUpdateTimer() {
        mUpdateTimer = new Timer();
        int TIME_UPDATE_PLAY_TIME = 1000;
        mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(MSG_UPDATE_PLAY_TIME);
            }
        }, 0, TIME_UPDATE_PLAY_TIME);
    }

    /**
     * 停止更新时间
     */
    public void stopUpdateTimer() {
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
    }

    private class AnimationImp implements Animation.AnimationListener {
        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    }

    public interface VideoPlayCallbackImpl {

        void onSwitchPageType();

        void onPlayFinish();

        void onUpdateTime(int timeShow, int playTime, int allTime);
    }
}