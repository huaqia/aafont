package com.xinmei365.font.model;

import cn.bmob.newim.bean.BmobIMExtraMessage;

public class PushMessage extends BmobIMExtraMessage {
    @Override
    public String getMsgType() {
        return "push";
    }

    @Override
    public boolean isTransient() {
        return true;
    }
}
