package ir.mjahanbazi.audioplayerincommingcallaudiomanager;


import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity {
    public MyAudioPlayer audioPlayer;
    private MyAudioListenerDefault listener = new MyAudioListenerDefault() {
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
