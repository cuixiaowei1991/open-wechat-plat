package com.cn.service;

import java.io.IOException;

/**
 * Created by cuixiaowei on 2017/2/28.
 */
public interface ThirdTogetPubFuncService {
    /**
     * h5签名注册
     * @param appid 公众号appid
     * @param url referer 中的url
     * @return
     * @throws IOException
     */
    public String getSignPackage(String appid,String url) throws Exception;
}
