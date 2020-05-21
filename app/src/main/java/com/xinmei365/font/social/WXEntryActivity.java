package com.xinmei365.font.social;

import android.app.Activity;
import android.os.Bundle;

import com.xinmei365.font.utils.Constant;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WXAPIFactory.createWXAPI(this, Constant.WEIXIN_APP_ID).handleIntent(this.getIntent(), this);
    }

    @Override
    public void onReq(BaseReq req) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        if (baseResp instanceof SendAuth.Resp) {
            SendAuth.Resp resp = (SendAuth.Resp) baseResp;
            BindHelper.getInstance(this).weiXinFinished(resp.errCode, resp.code);
            finish();
        } else {
            finish();
        }
    }
}
