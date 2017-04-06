package com.cn.entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by cuixiaowei on 2017/2/23.
 */
@Entity
@Table(name="weChatPublic")
@GenericGenerator(name = "system-uuid", strategy = "uuid")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class weChatPublic {
    /**
     * 微信公众号表主键ID
     */
    @Id
    @GeneratedValue(generator = "system-uuid")
    @Column(name = "WECHATPUBLIC_ID", length = 32)
    private String weChatPublic_id;
    /**
     * 微信公众号appid
     */
    @Column(name = "WECHATPUBLIC_APPID", length = 200)
    private String weChatPublic_appid;
    /**
     * 微信公众号appid_secert
     */
    @Column(name = "WECHATPUBLIC_APPID_SECERT", length = 500)
    private String weChatPublic_appid_secert;
    /**
     * 微信公众号（第三方获取的）授权token
     */
    @Column(name = "WECHATPUBLIC_APPID_TOKEN", length = 1000)
    private String weChatPublic_appid_token;
    /**
     * 微信公众号（第三方获取的）授权刷新token
     */
    @Column(name = "WECHATPUBLIC_REFRESHTOKEN", length = 1000)
    private String weChatPublic_appid_refreshtoken;
    /**
     * 微信公众号（第三方获取的）授权的三方平台appid
     */
    @Column(name = "WECHATPUBLIC_OPENPLAT_APPID", length = 500)
    private String weChatPublic_openPlat_appid;

    /**
     *授权公众号授权码（有效期一小时）
     */
    @Column(name = "WECHATPUBLIC_AUTHORIZER_CODE", length = 800)
    private String wechatpublic_Authorizer_code;

    /**
     *微信服务端授权码创建日期（毫秒）存入数据库后转为正常日期
     */
    @Column(name = "WECHATPUBLIC_CODE_CREATETIME", length = 200)
    private String wechatpublic_Code_CreateTime;

    /**
     *微信服务端授权码失效日期（毫秒）存入数据库后转为正常日期
     */
    @Column(name = "WECHATPUBLIC_CODEEXPIREDTIME", length = 200)
    private String wechatpublic_CodeExpiredTime;

    /**
     *授权状态 1：授权成功 2：取消授权 3：授权更新
     */
    @Column(name = "AUTHORIZATION_STATE", length = 200)
    private String Authorization_state;



    /**
     * token 返回时间
     */
    @Column(name = "AUTHORIZER_TOKEN_TIME")
    @Type(type = "java.util.Date")
    private Date authorizer_token_time;

    public String getWechatpublic_Authorizer_code() {
        return wechatpublic_Authorizer_code;
    }

    public void setWechatpublic_Authorizer_code(String wechatpublic_Authorizer_code) {
        this.wechatpublic_Authorizer_code = wechatpublic_Authorizer_code;
    }

    public String getWechatpublic_Code_CreateTime() {
        return wechatpublic_Code_CreateTime;
    }

    public void setWechatpublic_Code_CreateTime(String wechatpublic_Code_CreateTime) {
        this.wechatpublic_Code_CreateTime = wechatpublic_Code_CreateTime;
    }

    public String getWechatpublic_CodeExpiredTime() {
        return wechatpublic_CodeExpiredTime;
    }

    public void setWechatpublic_CodeExpiredTime(String wechatpublic_CodeExpiredTime) {
        this.wechatpublic_CodeExpiredTime = wechatpublic_CodeExpiredTime;
    }

    public String getAuthorization_state() {
        return Authorization_state;
    }

    public void setAuthorization_state(String authorization_state) {
        Authorization_state = authorization_state;
    }


    public Date getAuthorizer_token_time() {
        return authorizer_token_time;
    }

    public void setAuthorizer_token_time(Date authorizer_token_time) {
        this.authorizer_token_time = authorizer_token_time;
    }

    public String getWeChatPublic_id() {
        return weChatPublic_id;
    }

    public void setWeChatPublic_id(String weChatPublic_id) {
        this.weChatPublic_id = weChatPublic_id;
    }

    public String getWeChatPublic_appid() {
        return weChatPublic_appid;
    }

    public void setWeChatPublic_appid(String weChatPublic_appid) {
        this.weChatPublic_appid = weChatPublic_appid;
    }

    public String getWeChatPublic_appid_secert() {
        return weChatPublic_appid_secert;
    }

    public void setWeChatPublic_appid_secert(String weChatPublic_appid_secert) {
        this.weChatPublic_appid_secert = weChatPublic_appid_secert;
    }

    public String getWeChatPublic_appid_token() {
        return weChatPublic_appid_token;
    }

    public void setWeChatPublic_appid_token(String weChatPublic_appid_token) {
        this.weChatPublic_appid_token = weChatPublic_appid_token;
    }

    public String getWeChatPublic_appid_refreshtoken() {
        return weChatPublic_appid_refreshtoken;
    }

    public void setWeChatPublic_appid_refreshtoken(String weChatPublic_appid_refreshtoken) {
        this.weChatPublic_appid_refreshtoken = weChatPublic_appid_refreshtoken;
    }

    public String getWeChatPublic_openPlat_appid() {
        return weChatPublic_openPlat_appid;
    }

    public void setWeChatPublic_openPlat_appid(String weChatPublic_openPlat_appid) {
        this.weChatPublic_openPlat_appid = weChatPublic_openPlat_appid;
    }
}
