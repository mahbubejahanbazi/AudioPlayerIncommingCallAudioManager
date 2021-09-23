# Stop Playing Audio When Incoming Call Is Receieved
## audio player

Description of the application
A simple audio player that have the base player keys(play / pause and progress bar), in addition the project implemented some specific features.
- A basic audio player
- Audio manager is used in the applications to detect incoming calls.

 

## Tech Stack

Java

<p align="center">
  <img src="https://github.com/mahbubejahanbazi/audio_player_incomming_call_audio_manager/blob/main/images/default.jpg" />
</p>

<p align="center">
  <img src="https://github.com/mahbubejahanbazi/audio_player_incomming_call_audio_manager/blob/main/images/checked.jpg" />
</p>

## Source code

AudioPlayer.java
```java
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.util.concurrent.TimeUnit;

public class AudioPlayer extends androidx.constraintlayout.widget.ConstraintLayout {
    private MediaPlayer mediaPlayer;
    private ImageButton playButton;
    private ImageButton pauseButton;
    private TextView fileName;
    private TextView fileTime;
    private SeekBar seekBar;
    private boolean playing = false;
    private AudioListener listener;
    private AudioManager am;
    private int lastMode = AudioManager.MODE_NORMAL;
    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            mediaPlayer.seekTo(seekBar.getProgress());
            refreshGui();
        }
    };
    private Runnable UpdateAudioTime = new Runnable() {
        public void run() {
            while (true) {
                fileTime.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshGui();
                    }
                }, 100);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
                if (!playing) {
                    break;
                }
            }
        }
    };
    private OnClickListener onClickListenerPauseButton = new OnClickListener() {
        public void onClick(View v) {
            pauseAudio();
        }
    };
    private OnClickListener onClickListenerPlayerButton = new OnClickListener() {
        public void onClick(View arg0) {
            playAudio();
        }
    };
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {

            playButton.setVisibility(VISIBLE);
            pauseButton.setVisibility(GONE);
            mediaPlayer.seekTo(0);
            seekBar.setProgress(0);
            mediaPlayer.pause();
        }
    };

    public AudioPlayer(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AudioPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflator.inflate(R.layout.file_audio, this);
        playButton = (ImageButton) findViewById(R.id.file_audio_play);
        pauseButton = (ImageButton) findViewById(R.id.file_audio_pause);
        fileName = (TextView) findViewById(R.id.file_audio_name);
        seekBar = (SeekBar) findViewById(R.id.file_audio_seekBar);
        fileTime = (TextView) findViewById(R.id.file_audio_time);
        playButton.setOnClickListener(onClickListenerPlayerButton);
        pauseButton.setOnClickListener(onClickListenerPauseButton);
    }

    public void pauseAudio() {
        playing = false;
        pauseButton.setVisibility(INVISIBLE);
        playButton.setVisibility(VISIBLE);
        mediaPlayer.pause();
    }

    public void setListener(AudioListener listener) {
        this.listener = listener;
    }

    public void playAudio() {
        playing = true;
        new Thread(UpdateAudioTime).start();
        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
        mediaPlayer.start();
        pauseButton.setVisibility(VISIBLE);
        playButton.setVisibility(INVISIBLE);
        refreshGui();
    }

    private String getTimeStr(int time) {
        String minutes = "";
        String seconds = "";
        final long sec = TimeUnit.MILLISECONDS.toSeconds((long) time)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) time));
        if (sec < 10) {
            seconds = "0" + sec;
        } else {
            seconds = sec + "";
        }
        minutes = "" + TimeUnit.MILLISECONDS.toMinutes((long) time);
        return String.format("%s:%s", minutes, seconds);
    }


    private void refreshGui() {
        if (mediaPlayer == null) {
            return;
        }
        int length = mediaPlayer.getDuration();
        int current = mediaPlayer.getCurrentPosition();
        fileTime.setText(String.format("%s / %s",
                getTimeStr(current),
                getTimeStr(length)));
        seekBar.setProgress((int) current);
        if (listener != null) {
            if (am == null) {
                am = (AudioManager) getContext().getSystemService(getContext().AUDIO_SERVICE);
            }
            if (am.getMode() != lastMode) {
                lastMode = am.getMode();
                switch (lastMode) {
                    case AudioManager.MODE_IN_CALL:
                        listener.changeToCall();
                        break;
                    case AudioManager.MODE_IN_COMMUNICATION:
                        listener.changToCommunication();
                        break;
                    case AudioManager.MODE_NORMAL:
                        listener.changeToNormal();
                        break;
                    case AudioManager.MODE_RINGTONE:
                        listener.changeToRingtone();
                        break;
                }
            }
        }
    }


    public void setAudio(int resid) {
        fileName.setText(getResources().getResourceEntryName(resid));
        mediaPlayer = MediaPlayer.create(getContext(), resid);
        intiMedia();
    }

    public void setAudio(Uri uri) {
        fileName.setText(getFileName(uri));
        mediaPlayer = MediaPlayer.create(getContext(), uri);
        intiMedia();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void intiMedia() {
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        seekBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.setOnCompletionListener(onCompletionListener);
        fileTime.setText(String.format("0:00 / %s", getTimeStr(mediaPlayer.getDuration())));
    }

    public boolean isPlaying() {
        return playing;
    }

}
```
AudioListener.java
```java
public interface AudioListener {
     void changeToNormal();

     void changToCommunication();

     void changeToRingtone();

     void changeToCall();
}
```
AudioListenerDefault.java
```java
public class AudioListenerDefault implements AudioListener {
    public void changeToNormal(){}

    public void changToCommunication(){}

    public void changeToRingtone(){}

    public void changeToCall(){}
}
```
MainActivity.java
```java
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity {
    public AudioPlayer audioPlayer;
    private AudioListenerDefault listener = new AudioListenerDefault() {
        @Override
        public void changeToCall() {
            if (audioPlayer.isPlaying()) {
                audioPlayer.pauseAudio();
            }
        }

        @Override
        public void changeToNormal() {
        }

        @Override
        public void changeToRingtone() {
            if (audioPlayer.isPlaying()) {
                audioPlayer.pauseAudio();
            }
        }

        @Override
        public void changToCommunication() {
            if (audioPlayer.isPlaying()) {
                audioPlayer.pauseAudio();
            }
        }
    };
    private CompoundButton.OnCheckedChangeListener incommingCall = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b) {
                audioPlayer.setListener(listener);
            } else {
                audioPlayer.setListener(null);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioPlayer = findViewById(R.id.activity_main_audio_player);
        audioPlayer.setAudio(R.raw.audio_file_example);
        CheckBox c = findViewById(R.id.activity_main_stop_play_incomming_call);
        c.setOnCheckedChangeListener(incommingCall);
    }

}
```
## Contact

mjahanbazi@protonmail.com