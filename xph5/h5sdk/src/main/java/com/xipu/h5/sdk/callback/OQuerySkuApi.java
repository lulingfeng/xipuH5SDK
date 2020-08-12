package com.xipu.h5.sdk.callback;


import com.xipu.h5.sdk.model.OSkuDetails;

import java.util.List;

public interface OQuerySkuApi {

    void onSkuDetailsResponse(List<OSkuDetails> skuDetailsList);

    void onSkuDetailsFailed(String msg);
}
