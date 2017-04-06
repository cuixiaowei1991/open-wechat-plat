package com.cn.service.impl;

import com.cn.MsgCode.MsgAndCode;
import com.cn.cache.AuthorizerInfo;
import com.cn.cache.AuthorizerInfoMap;
import com.cn.cache.JsapiticketInfo;
import com.cn.cache.JsapiticketInfoMap;
import com.cn.common.LogHelper;
import com.cn.common.WeChatCommon.commenUtil;
import com.cn.common.httpsPostMethod;
import com.cn.dao.weChatPublicDao;
import com.cn.entity.weChatPublic;
import com.cn.service.ThirdTogetPubFuncService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Created by cuixiaowei on 2017/2/28.
 */
@Service
public class ThirdTogetPubFuncServiceImpl implements ThirdTogetPubFuncService {
    @Autowired
    private weChatThridServiceImpl weChatThridService;
    @Autowired
    private weChatPublicDao wcpDao;
    @Value("${app_id}")
    private String app_id;
    @Value("${api_query_auth_url}")
    private String api_query_auth_url;
    @Value("${api_authorizer_token_url}")
    private String api_authorizer_token_url;
    @Override
    public String getSignPackage(String appid, String url) throws Exception
    {
        LogHelper.info("vvvvvvvvvvvvvvvvvvvvvvvvvvH5获取JS访问条件接口vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
        String timestamp = Long.toString(System.currentTimeMillis() / 1000);
        String nonceStr = commenUtil.createSuiJi();

        String rawstring = "jsapi_ticket=" + getJsapi_ticket(appid)
                + "&noncestr=" + nonceStr + "&timestamp=" + timestamp + "&url="
                + url + "";
        LogHelper.info("H5获取JS访问条件接口--生成的rawstring结果为：---" + rawstring);
        String signature = commenUtil.getSHA1(rawstring);
        JSONObject jj2 = new JSONObject();
        try {
            jj2.put("appId", appid);

            jj2.put("timestamp", timestamp);
            jj2.put("nonceStr", nonceStr);
            jj2.put("signature", signature);
        } catch (JSONException e) {
            LogHelper.error(e, "H5获取JS访问条件接口异常！！！！", false);
        }
        return jj2.toString();
    }

    /**
     * 调用微信JS接口的临时票据 用于生成JS请求微信的签名
     *
     * @return
     */
    public String getJsapi_ticket(String authaccess_appid) {
        JsapiticketInfo js = JsapiticketInfoMap.get(authaccess_appid);
        LogHelper.info("获取的授权token----------------》" + getAuthAccesstoken(authaccess_appid));
        LogHelper.info("AuthorizerInfo----------------》" + js);
        if (js==null || js.jsapi_ticketExpires()) {
            String rJsapi = httpsPostMethod.sendHttpsPost(
                    "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="
                            + getAuthAccesstoken(authaccess_appid), "&type=jsapi",
                    "获取jsapi_ticket");
            if (!"error".equals(rJsapi)) {
                try {
                    JSONObject jsapiJSON = new JSONObject(rJsapi);
                    int errcode = -1;
                    errcode = jsapiJSON.getInt("errcode");
                    if (errcode == 0) {
                        String jsapi_ticket = jsapiJSON.getString("ticket");
                        long expires_in = jsapiJSON.getInt("expires_in");
                        JsapiticketInfo af1=new JsapiticketInfo();
                        af1.setJsapi_ticket(jsapi_ticket);
                        af1.setJsapi_ticketExpires(expires_in);
                        JsapiticketInfoMap.put(authaccess_appid, af1);

                    } else {
                        return null;
                    }
                } catch (JSONException e) {
                    LogHelper.info("获取jsapi_ticket,authaccess_appid=" + authaccess_appid + "抛异常:"
                            + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
        return JsapiticketInfoMap.get(authaccess_appid).getJsapi_ticket();
    }
    /**
     * 根据授权码和第三方平台appid换取公众号的接口调用凭据和授权信息
     * @return
     */
    public String getAuthAccesstoken(String authaccess_appid)
    {
        LogHelper.debug("授权方的appid(getAuthAccesstoken)--------------》" + authaccess_appid);
        JSONObject accessToken_json = new JSONObject();
        AuthorizerInfo authorizerInfo= AuthorizerInfoMap.get(authaccess_appid);
        List<weChatPublic> weChatPublicList= wcpDao.getWeChatPublicByParamters(app_id, authaccess_appid);
        if(null==authorizerInfo)
        {
            authorizerInfo=new AuthorizerInfo();
        }
        if(weChatPublicList==null || weChatPublicList.size()<=0)
        {
            accessToken_json.put(MsgAndCode.CODE_001,MsgAndCode.MESSAGE_001);
            return accessToken_json.toString();
        }
        weChatPublic war=weChatPublicList.get(0);
        if( (null==war.getWeChatPublic_appid_refreshtoken()|| "".equals(war.getWeChatPublic_appid_refreshtoken())))
        {
            String auth_code="";
            try
                {
                    if(war.getAuthorization_state()==null || (war.getAuthorization_state().equals("2")))
                    {//已取消授权，重新获取授权码
                        accessToken_json.put(MsgAndCode.CODE_002, MsgAndCode.MESSAGE_002);
                        return accessToken_json.toString();
                    }
                    auth_code=war.getWechatpublic_Authorizer_code();
                    accessToken_json.put("component_appid",app_id);
                    accessToken_json.put("authorization_code", auth_code);
                    String responseStr= httpsPostMethod.sendHttpsPost(api_query_auth_url+"?component_access_token="+weChatThridService.getComponentAccessToken(),
                            accessToken_json.toString() ,"根据授权码和第三方平台appid换取公众号的接口调用凭据和授权信息");
                    LogHelper.info("根据授权码和第三方平台appid换取公众号的接口调用凭据和授权信息-----------》"+responseStr);
                    JSONObject rjson = new JSONObject(responseStr);
                    String authorization_info=rjson.get("authorization_info")+"";
                    JSONObject js_authorization = new JSONObject(authorization_info);
                    AuthorizerInfo ai=new AuthorizerInfo(app_id,authaccess_appid,rjson.getString("authorizer_access_token"),
                            rjson.getLong("expires_in"),rjson.getString("authorizer_refresh_token"));
                    AuthorizerInfoMap.put(js_authorization.getString("authorizer_appid"), ai);
                    war.setWeChatPublic_appid_refreshtoken(js_authorization.getString("authorizer_refresh_token"));
                    wcpDao.saveOrUpdateWeChatPublic(war);
                 } catch (JSONException e) {
                    LogHelper.error(e,"换取公众号的接口调用凭据和授权信息(本地authorizer_refresh_token)为空异常！！！！",false);
                    e.printStackTrace();
                }

        }
        else
        {
            if( authorizerInfo.authorizer_access_tokenExprise())
            {
                try
                {
                    //从数据库中查询出刷新令牌用的refreshtoken
                    LogHelper.debug("刷新令牌用的refreshtoken------------------>" + war.getWeChatPublic_appid_refreshtoken());
                    accessToken_json.put("component_appid",app_id);
                    accessToken_json.put("authorizer_appid",authaccess_appid);
                    accessToken_json.put("authorizer_refresh_token", war.getWeChatPublic_appid_refreshtoken());
                    String responseStr= httpsPostMethod.sendHttpsPost(api_authorizer_token_url+"?component_access_token=" + weChatThridService.getComponentAccessToken(),
                            accessToken_json.toString(), "根据刷新token重新获取授权token");
                    LogHelper.debug("(本地过期根据authorizer_refresh_token重新获取token)-----------》"+responseStr);
                    JSONObject rjson = new JSONObject(responseStr);
                    AuthorizerInfo ai=new AuthorizerInfo(app_id,authaccess_appid,rjson.getString("authorizer_access_token"),
                            rjson.getLong("expires_in"),rjson.getString("authorizer_refresh_token"));
                    AuthorizerInfoMap.put(authaccess_appid,ai);
                    war.setWeChatPublic_appid_refreshtoken(rjson.getString("authorizer_refresh_token"));
                    wcpDao.saveOrUpdateWeChatPublic(war);
                   } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
        return AuthorizerInfoMap.get(authaccess_appid).getAuthorizer_access_token();
    }
}
