package mixer.sound.mixer;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SoundControlsAdapter.ButtonClickListener {
    private static final int samplingRates[] = {16000, 11025, 11000, 8000, 6000};
    private AudioRecord mRecorder;
    private File mRecording;
    public static boolean mIsRecording = false;
    private String RECORD_WAV_PATH = Environment.getExternalStorageDirectory() + File.separator + "AudioRecord";
    private List<Integer> mSound = new ArrayList<>();
    private SoundPool mSoundPool;
    private SoundControlsAdapter adapter;
    public static final String TAG = "TAG";
    private boolean multi;
    private RecordAudio recordAudio;
    public ArrayList<MediaPlayer> mediaPlayerList = new ArrayList<>();
    private ArrayList<String> uRIS = new ArrayList<>();
    public static final String URIs = "uris";
    LiveDataMediaPlayer mediaPlayers;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissions();

        initRecorder();
        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .build();
        loadSoundPool();
        mediaPlayers = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(LiveDataMediaPlayer.class);
        if(savedInstanceState != null){
            uRIS = savedInstanceState.getStringArrayList(URIs);
            for(String uri: uRIS){
                mediaPlayerList.add(MediaPlayer.create(this, Uri.fromFile(new File(uri))));
                mediaPlayerList.get(mediaPlayerList.size() - 1).start();
                mediaPlayerList.get(mediaPlayerList.size() - 1).pause();
            }
        }
        getSoundControls();
        mediaPlayers.getMediaPlayers().observe(this, mediaObserver);
    }

    Observer<ArrayList<MediaPlayer>> mediaObserver = new Observer<ArrayList<MediaPlayer>>() {
        @Override
        public void onChanged(@Nullable ArrayList<MediaPlayer> mediaPlayers) {
            adapter.setList(mediaPlayers);
        }
    };

        public void getPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 100);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            finish();
        }
    }

    public void loadSoundPool() {
        mSound.add(mSoundPool.load(this, R.raw.boom_kick, 1));
        mSound.add(mSoundPool.load(this, R.raw.gubbler_drum, 1));
        mSound.add(mSoundPool.load(this, R.raw.nice_one, 1));
        mSound.add(mSoundPool.load(this, R.raw.ready, 1));
        mSound.add(mSoundPool.load(this, R.raw.robot_intro, 1));
        mSound.add(mSoundPool.load(this, R.raw.thunder, 1));
        mSound.add(mSoundPool.load(this, R.raw.whoosh, 1));
    }

    public void playSoundPool(View v) {
        System.out.println(mSound);
        Random x = new Random();
        int y = x.nextInt(7);
        mSoundPool.play(mSound.get(y), 1, 1, 1, 0, 1);
    }

    public void muteSystemSounds(int level) {
        AudioManager manage = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        manage.setStreamVolume(AudioManager.STREAM_ALARM, level, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        manage.setStreamVolume(AudioManager.STREAM_RING, level, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        manage.setStreamVolume(AudioManager.STREAM_SYSTEM, level, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        manage.setStreamVolume(AudioManager.STREAM_NOTIFICATION, level, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    public void vibratePhone(){
        Vibrator vibe = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        long[] waveForm = {25, 50, 25, 50};
        if(Build.VERSION.SDK_INT >= 26){
            vibe.vibrate(VibrationEffect.createWaveform(waveForm, 4));
        }else {
            vibe.vibrate(150);
        }
    }

    public void recordWavStart(View v) {
        vibratePhone();
        if(v.getTag().toString().equals("Multi")){
            multi = true;
        }
        mIsRecording = true;
        if(!multi) {
            for (MediaPlayer media : mediaPlayerList) {
                media.pause();
            }
        }
        muteSystemSounds(0);
        mRecorder.startRecording();
        mRecording = getFile("raw");
        recordAudio.startBufferedWrite(mRecording);
    }

    public void recordWavStop(View v) {
        if(mIsRecording) {
            try {
                mIsRecording = false;
                mRecorder.stop();
                File waveFile;
                if (multi) {
                    multi = false;
                }
                waveFile = getFile("wav");
                recordAudio.rawToWave(mRecording, waveFile);
                mediaPlayerList.add(MediaPlayer.create(this, Uri.fromFile(waveFile)));
                uRIS.add(waveFile.toString());
                mediaPlayerList.get(mediaPlayerList.size() - 1).start();
                mediaPlayerList.get(mediaPlayerList.size() - 1).pause();
                mediaPlayers.getMediaPlayers().postValue(mediaPlayerList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            muteSystemSounds(10);
        }
    }

    private File getFile(final String suffix) {
        Time time = new Time();
        time.setToNow();
        String timeString;
        String recordWavPath;
        if(multi) {
            recordWavPath = Environment.getExternalStorageDirectory() + File.separator + "Music";
            timeString = time.format("%Y %m %d") + "." + suffix;
        }else {
            recordWavPath = Environment.getExternalStorageDirectory() + File.separator + "AudioRecord";
            timeString = time.format("%Y%m%d%H%M%S") + "." + suffix;
        }
        return new File(recordWavPath, timeString);
    }

    public void getSoundControls(){
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        adapter = new SoundControlsAdapter(mediaPlayerList, this);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void initRecorder() {
        int sampleRate = getValidSampleRates();
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        short[] buffer = new short[bufferSize];
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        recordAudio = new RecordAudio(mRecorder, buffer, sampleRate);
    }

    private int getValidSampleRates() {
        for (int rate : samplingRates) {
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize > 0) {
                return rate;
            }
        }
        return 0;
    }

    @Override
    public void onButtonClick(int clickedposition) {
        mediaPlayerList.remove(clickedposition);
        uRIS.remove(clickedposition);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(uRIS != null) {
            outState.putStringArrayList(URIs, uRIS);
        }
        super.onSaveInstanceState(outState);
    }
}




