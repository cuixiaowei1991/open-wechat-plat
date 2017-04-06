package com.cn.dao.impl;

import com.cn.common.LogHelper;
import com.cn.dao.util.HibernateDAO;
import com.cn.dao.weChatPublicDao;
import com.cn.entity.weChatPublic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by cuixiaowei on 2017/2/24.
 */
@Transactional
@Repository("weChatPublicDao")
public class weChatPublicDaoImpl implements weChatPublicDao {
    @Autowired
    private HibernateDAO hibernateDAO;
    @Override
    public List<weChatPublic> getWeChatPublicByParamters(String third_appid, String public_appid) {
        StringBuilder sql=new StringBuilder();
        sql.append("from weChatPublic wp where wp.weChatPublic_openPlat_appid='"+third_appid+"'");
        sql.append(" and wp.weChatPublic_appid='"+public_appid+"'");
        LogHelper.info("根据第三方ID和公众号appid查询信息："+sql);
        //hibernateDAO.listByHQL(sql.toString());
        return (List<weChatPublic>) hibernateDAO.listByHQL(sql.toString());
    }

    @Override
    public String saveOrUpdateWeChatPublic(weChatPublic wp) {
        return hibernateDAO.saveOrUpdate(wp);
    }
}
