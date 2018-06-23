package mixer.sound.mixer;

import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;


public class SoundControlsAdapter extends RecyclerView.Adapter<SoundControlsAdapter.ViewHolder> {

    private ArrayList<MediaPlayer> mMediaList;
    private ButtonClickListener buttonListener;
    private int MAX_VOLUME = 100;

    public SoundControlsAdapter(ArrayList<MediaPlayer> mediaList, ButtonClickListener buttonlistener) {
        mMediaList = mediaList;
        buttonListener = buttonlistener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sound_controls, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    public void setList(ArrayList<MediaPlayer> mediaPlayers){
            mMediaList = mediaPlayers;
            notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(mMediaList != null) {
            return mMediaList.size();
        }
        return 0;
    }

    public interface ButtonClickListener{
        void onButtonClick(int clickedposition);
    }

    class ViewHolder extends RecyclerView.ViewHolder
            implements SeekBar.OnSeekBarChangeListener,
            View.OnClickListener{

        SeekBar seekBarVolume;
        SeekBar seekBarSpeed;

        public ViewHolder(View itemView) {
            super(itemView);
            TextView playBtn = itemView.findViewById(R.id.button_play);
            TextView pauseBtn = itemView.findViewById(R.id.button_pause);
            TextView deleteBtn = itemView.findViewById(R.id.button_delete);
            seekBarVolume = itemView.findViewById(R.id.seekbar_volume);
            seekBarSpeed = itemView.findViewById(R.id.seekbar_speed);

            playBtn.setOnClickListener(this);
            pauseBtn.setOnClickListener(this);
            deleteBtn.setOnClickListener(this);
            seekBarVolume.setOnSeekBarChangeListener(this);
            seekBarSpeed.setOnSeekBarChangeListener(this);
        }

        @Override
        public void onClick(View view) {
            MediaPlayer music = mMediaList.get(getAdapterPosition());
            String tag = view.getTag().toString();
            if (tag.equals("play")) {
                music.setLooping(true);
                music.start();
            }
            if (tag.equals("pause")) {
                music.pause();
            }
            if (tag.equals("delete")) {
                buttonListener.onButtonClick(getAdapterPosition());
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            MediaPlayer music = mMediaList.get(getAdapterPosition());
            String tag = seekBar.getTag().toString();
            if (tag.equals("volume")) {
                if(progress > 0) {
                    final float volume = (float) (1 - (Math.log(MAX_VOLUME - progress) / Math.log(MAX_VOLUME)));
                    Log.i(MainActivity.TAG, "onProgressChanged: Volume: " + progress);
                    music.setVolume(volume, volume);
                }else {
                    music.setVolume(0,0);
                }
            }
            if (tag.equals("speed")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.i(MainActivity.TAG, "onProgressChanged: Speed: " + progress);
                    final float speed = (float) progress / 25;
                    music.setPlaybackParams(new PlaybackParams().setSpeed(speed));
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
}
