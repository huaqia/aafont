package com.hanmei.aafont.filter;

import com.hanmei.aafont.ui.widget.CameraSurfaceView;
import com.hanmei.aafont.utils.MagicParams;

import java.io.File;

public class MagicEngine {
    private static MagicEngine magicEngine;

    public static MagicEngine getInstance(){
        if(magicEngine == null)
            throw new NullPointerException("MagicEngine must be built first");
        else
            return magicEngine;
    }

    private MagicEngine(Builder builder){

    }

    public void startRecord(){
        if(MagicParams.magicBaseView instanceof CameraSurfaceView)
            ((CameraSurfaceView)MagicParams.magicBaseView).changeRecordingState(true);
    }

    public void stopRecord(){
        if(MagicParams.magicBaseView instanceof CameraSurfaceView)
            ((CameraSurfaceView)MagicParams.magicBaseView).changeRecordingState(false);
    }

    public static class Builder{

        public MagicEngine build(CameraSurfaceView magicBaseView) {
            MagicParams.context = magicBaseView.getContext();
            MagicParams.magicBaseView = magicBaseView;
            return new MagicEngine(this);
        }

        public Builder setVideoPath(String path){
            MagicParams.videoPath = path;
            return this;
        }

        public Builder setVideoName(String name){
            MagicParams.videoName = name;
            return this;
        }

    }
}