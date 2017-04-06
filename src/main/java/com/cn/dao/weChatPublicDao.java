package com.cn.dao;

import com.cn.entity.weChatPublic;

import java.util.List;

/**
 * Created by cuixiaowei on 2017/2/24.
 */
public interface weChatPublicDao {
    /**
     * 根据第三方ID和公众号appid获取信息
     * @param third_appid
     * @param public_appid
     * @return
     */
    public List<weChatPublic> getWeChatPublicByParamters(String third_appid,String public_appid);

    public String saveOrUpdateWeChatPublic(weChatPublic wp);
}
