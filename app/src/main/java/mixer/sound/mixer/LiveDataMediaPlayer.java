package mixer.sound.mixer;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.media.MediaPlayer;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

public class LiveDataMediaPlayer extends ViewModel {

    public MutableLiveData<ArrayList<MediaPlayer>> mediaPlayers;

    //LiveDataObject Not calling onCleared Method
    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public MutableLiveData<ArrayList<MediaPlayer>> getMediaPlayers(){
        if (mediaPlayers == null) {
            mediaPlayers = new MutableLiveData<>();
        }
        return mediaPlayers;
    }
}
