package com.example.cn.exoplayersample;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG";
    private SimpleExoPlayerView mPlayerView;
    private SimpleExoPlayer player;

    private DataSource.Factory mediaDataSourceFactory;
    private DefaultTrackSelector trackSelector;
    private BandwidthMeter bandwidthMeter;

    ProgressBar loadingProgressBar;//加载中进度
    private ImageView playIcon;//暂停按钮
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private DefaultTimeBar mTimeSeekBar;//时间进度条

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPlayerView();
        initListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
            trackSelector = null;
        }
    }

    //初始化播放器
    private void initPlayerView() {
        mPlayerView = findViewById(R.id.player_view);
        bandwidthMeter = new DefaultBandwidthMeter();
        mediaDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"), (TransferListener<? super DataSource>) bandwidthMeter);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        playIcon = findViewById(R.id.icon_video_play);
        mTimeSeekBar = findViewById(R.id.exo_progress);
    }

    //初始化播放器对象
    private void initializePlayer() {
        mPlayerView.requestFocus();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        //默认暂停播放
        player.setPlayWhenReady(false);

        mPlayerView.setPlayer(player);

        playIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getPlayWhenReady()) {
                    player.setPlayWhenReady(false);//暂停
                } else {
                    player.setPlayWhenReady(true);//开始播放
                }
            }
        });
        //播放地址可以是m3u8，mp4等
        String playerUrl = "https://content.jwplatform.com/manifests/IPYHGrEj.m3u8";
        //创建一个播放mediaSource,不同的源文件mediaSource不同
        MediaSource mediaSource = buildMediaSource(Uri.parse(playerUrl), null);
        //准备
        player.prepare(mediaSource);
        //时间永久不消失Controller
        mPlayerView.setControllerShowTimeoutMs(-1);
        //播放状态监听
        player.addListener(new ComponentListener());

    }

    //player.getPlayWhenReady() true是播放
    //监听事件
    private void initListener() {


        final GestureDetector gestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        if (player != null && player.getPlayWhenReady()) {
                            player.setPlayWhenReady(false);//暂停
                        } else {
                            player.setPlayWhenReady(true);//播放
                        }
                        return true;
                    }
                }
        );
        //触摸播放界面事件
        mPlayerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, @NonNull MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });


        //进度条滑动监听，
        mTimeSeekBar.setListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar, long position) {

            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {

            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                player.seekTo(position);//移动结束就开始播放
                player.setPlayWhenReady(true);
            }
        });
    }

    /**
     * 根据不同的Uri文件，生成不同的MediaSource
     *
     * @param uri
     * @param overrideExtension
     * @return
     */
    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = Util.inferContentType(!TextUtils.isEmpty(overrideExtension) ? "." + overrideExtension
                : uri.getLastPathSegment());
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), null, null);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), null, null);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, null, null);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        null, null);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }
    //

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }


    DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(getApplicationContext(), bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(getApplicationContext(), "ExoVideoView"), bandwidthMeter);
    }

    /**
     * 显示暂停按钮
     *
     * @param isPlay
     */

    private void play(boolean isPlay) {
        if (isPlay) {
            playIcon.setVisibility(View.GONE);
        } else {
            playIcon.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示加载中效果
     *
     * @param isLoading
     */
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingProgressBar.setVisibility(View.VISIBLE);
        } else {
            loadingProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * 初始到最初位置
     */
    private void playDefault() {
        playIcon.setVisibility(View.VISIBLE);
        player.seekToDefaultPosition();
        player.setPlayWhenReady(false);

    }

    /**
     * 监听播放状态
     */
    private final class ComponentListener implements ExoPlayer.EventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                /**
                 * The player is able to immediately play from its current position. The player will be playing if
                 * {@link #getPlayWhenReady()} is true, and paused otherwise.
                 */
                case Player.STATE_READY://
                    showLoading(false);
                    play(playWhenReady);
                    break;
                /**
                 * The player has finished playing the media.
                 */
                case Player.STATE_ENDED:
                    showLoading(false);
                    playDefault();
                    break;
                /**
                 * The player does not have any media to play.
                 */
                case Player.STATE_IDLE:
                    showLoading(true);
                    break;
                /**
                 * The player is not able to immediately play from its current position. This state typically
                 * occurs when more data needs to be loaded.
                 */
                case Player.STATE_BUFFERING:
                    showLoading(true);
                    break;
            }
        }


        @Override
        public void onRepeatModeChanged(int repeatMode) {
            // Do nothing.
        }

        @Override
        public void onPositionDiscontinuity() {
            // Do nothing.
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            // Do nothing.
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            // Do nothing.
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            // Do nothing.
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            // Do nothing.
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            // Do nothing.
        }


    }
}
