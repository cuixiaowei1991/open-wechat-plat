package com.cn.service.impl;

import com.cn.cache.ComponentAccessTokenMap;
import com.cn.cache.ComponentVerifyTicket;
import com.cn.cache.PreAuthCodeMap;
import com.cn.common.LogHelper;
import com.cn.common.WeChatCommon.WXBizMsgCrypt;
import com.cn.common.WeChatCommon.WeiXinDecode;
import com.cn.common.httpsPostMethod;
import com.cn.dao.verifyTicketDao;
import com.cn.dao.weChatPublicDao;
import com.cn.entity.openPlatform;
import com.cn.entity.weChatPublic;
import com.cn.service.weChatThridService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Created by cuixiaowei on 2017/2/23.
 */
@Service
public class weChatThridServiceImpl implements weChatThridService {

    @Value("${token}")
    private String token;
    @Value("${app_key}")
    private String key;
    @Value("${app_id}")
    private String app_id;
    @Value("${app_secret}")
    private String app_secret;
    @Value("${component_access_token_url}")
    private String component_access_token_url;
    @Value("${pre_auth_code_url}")
    private String pre_auth_code_url;
    @Value("${qR_code_url}")
    private String qR_code_url;
    @Value("${redirect_uri}")
    private String redirect_uri;
    @Autowired
    private verifyTicketDao ticketDao;
    @Autowired
    private weChatPublicDao wpDao;
    public void handleAuthorize(HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
        String msg_signature=request.getParameter("msg_signature");
        String timestamp=request.getParameter("timestamp");
        String nonce=request.getParameter("nonce");
        LogHelper.info("签名串------------:" + msg_signature);
        LogHelper.info("时间戳------------:" + timestamp);
        LogHelper.info("随机数------------:" + nonce);
        BufferedReader br;
        String postStr = null;
        try {
            br = request.getReader();
            String buffer = null;
            StringBuffer buff = new StringBuffer();
            while ((buffer = br.readLine()) != null) {
                buff.append(buffer + "\n");
            }
            br.close();
            postStr = buff.toString();
            LogHelper.info("接收post发送数据:\n" + postStr);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String result_json= WeiXinDecode.decode(token, key, app_id, msg_signature, timestamp, nonce, postStr);
        JSONObject json=new JSONObject(result_json);
        String appid=json.getString("AppId");//第三方平台appid
        String createTime=json.getString("CreateTime");//时间戳
        String infoType=json.getString("InfoType");//component_verify_ticket
        List<openPlatform> mapList= ticketDao.getVerifyTicketByAppid(appid);
        if(infoType!=null && "component_verify_ticket".equalsIgnoreCase(infoType))
        {//每十分钟推一次验证ticket
            String componentVerifyTicket=json.getString("ComponentVerifyTicket");//Ticket内容

            openPlatform op=null;
            if(mapList.size()>0)
            {
                op= mapList.get(0);
            }
            else
            {
                op=new openPlatform();
            }
            op.setOpenPlatform_ticket(componentVerifyTicket);
            op.setOpenPlatform_ticket_time(createTime);
            op.setOpenPlatform_appid(appid);
            ticketDao.inserVerifyTicketAppid(op);
            LogHelper.info("每隔十分钟获取一次ticket！！！");
        }
        if(infoType!=null && ("unauthorized".equalsIgnoreCase(infoType)
                || "authorized".equalsIgnoreCase(infoType)))
        {//授权状态修改
            String result= AssemblingWeChatPublic(appid,infoType,json);
            LogHelper.info("授权状态修改："+result);
        }

    }

    /**
     * 获取第三方平台component_access_token
     * @return
     */
    public String getComponentAccessToken()
    {
        try {
            ComponentVerifyTicket cvt = ComponentAccessTokenMap.get(app_id);
            List<openPlatform> mapList= ticketDao.getVerifyTicketByAppid(app_id);
            String ticket=mapList.get(0).getOpenPlatform_ticket();
            JSONObject componentAccessToken_json = new JSONObject();
            componentAccessToken_json.put("component_appid",app_id);
            componentAccessToken_json.put("component_appsecret",app_secret);
            componentAccessToken_json.put("component_verify_ticket",ticket);
            if(null==cvt)
            {
                String responseStr= httpsPostMethod.sendHttpsPost(component_access_token_url,
                        componentAccessToken_json.toString(), "获取第三方平台component_access_token");
                LogHelper.info("获取第三方平台token微信返回-----------》" + responseStr);
                JSONObject rjson = new JSONObject(responseStr);
                if(rjson.isNull("component_access_token"))
                {
                    return null;
                }
                String component_access_token=rjson.getString("component_access_token");
                long expires_in=rjson.getLong("expires_in");
                ComponentVerifyTicket componentVerifyTicket=new ComponentVerifyTicket(app_id,component_access_token,expires_in);
                ComponentAccessTokenMap.put(app_id,componentVerifyTicket);
                LogHelper.info("第三方平台component_access_token第一次请求存入-----------》" + ComponentAccessTokenMap.get(app_id).getComponent_access_token());

            }
            else
            {
                if(cvt.component_access_tokenExprise())
                {
                   String responseStr= httpsPostMethod.sendHttpsPost(component_access_token_url,
                            componentAccessToken_json.toString(), "获取第三方平台component_access_token");
                    LogHelper.info("获取第三方平台token微信返回（本地过期）-----------》" + responseStr);
                    JSONObject rjson = new JSONObject(responseStr);
                    String component_access_token=rjson.getString("component_access_token");
                    long expires_in=rjson.getLong("expires_in");
                    ComponentVerifyTicket componentVerifyTicket=new ComponentVerifyTicket(app_id,component_access_token,expires_in);
                    ComponentAccessTokenMap.put(app_id, componentVerifyTicket);
                    LogHelper.info("第三方平台component_access_token过期存入-----------》" + ComponentAccessTokenMap.get(app_id).getComponent_access_token());
                }
            }
            return ComponentAccessTokenMap.get(app_id).getComponent_access_token();


        }catch (Exception e)
        {
            LogHelper.error(e,"获取第三方平台component_access_token异常！！！！",false);
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 获取预授权码pre_auth_code 预授权码用于公众号授权时的第三方平台方安全验证
     * @return
     */
    public String getPreAuthCode()
    {
        ComponentVerifyTicket cvt = PreAuthCodeMap.get(app_id);
        JSONObject PreAuthCode_json = new JSONObject();
        PreAuthCode_json.put("component_appid",app_id);
        try {
            if(null==cvt)
            {
               String responseStr= httpsPostMethod.sendHttpsPost(pre_auth_code_url+"?component_access_token="+getComponentAccessToken(),
                        PreAuthCode_json.toString(),"获取预授权码pre_auth_code（第一次访问）");
                LogHelper.info("获取预授权码pre_auth_code微信返回----------------》" + responseStr);
                JSONObject rjson = new JSONObject(responseStr);
                if(rjson.isNull("pre_auth_code"))
                {
                    return null;
                }
                else
                {
                    long expires_in=rjson.getLong("expires_in");//有效期20分钟
                    ComponentVerifyTicket componentVerifyTicket=new ComponentVerifyTicket(app_id,expires_in,rjson.getString("pre_auth_code"));
                    PreAuthCodeMap.put(app_id, componentVerifyTicket);
                    LogHelper.info("预授权码pre_auth_code第一次存入缓存-----------》" + PreAuthCodeMap.get(app_id).getPre_auth_code());
                }
            }
            else {
                if (cvt.preauthcode_Exprise()) {
                    String responseStr= httpsPostMethod.sendHttpsPost(pre_auth_code_url+"?component_access_token="+getComponentAccessToken(),
                            PreAuthCode_json.toString(),"获取预授权码pre_auth_code（过期）");
                    LogHelper.info("获取预授权码pre_auth_code微信返回（本地过期）----------------》" + responseStr);
                    JSONObject rjson = new JSONObject(responseStr);
                    long expires_in=rjson.getLong("expires_in");//有效期20分钟
                    ComponentVerifyTicket componentVerifyTicket=new ComponentVerifyTicket(app_id,expires_in,rjson.getString("pre_auth_code"));
                    PreAuthCodeMap.put(app_id,componentVerifyTicket);
                    LogHelper.info("预授权码pre_auth_code过期重新存入缓存-----------》" + PreAuthCodeMap.get(app_id).getPre_auth_code());
                }
            }
            return PreAuthCodeMap.get(app_id).getPre_auth_code();
        }
        catch (Exception e)
        {
            LogHelper.error(e,"预授权码pre_auth_code异常！！！！",false);
            e.printStackTrace();;
            return null;
        }
    }

    /**
     * 微信第三方平台授权的入口
     * @return
     */
    public String entranceWinXin() throws Exception
    {
        JSONObject json=new JSONObject();
        String url=qR_code_url+"?component_appid="+app_id+"&pre_auth_code="
                +getPreAuthCode()+"&redirect_uri="+redirect_uri;
        LogHelper.info("返回到前台的地址为：---------------》" + url);
        json.put("qrcode_url",url);
        return json.toString();
    }

    /**
     *微信统一回复接收接口
     * @return
     * @throws Exception
     */
    @Override
    public String commonReturn(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        String msg_signature=request.getParameter("msg_signature");
        String timestamp=request.getParameter("timestamp");
        String nonce=request.getParameter("nonce");
        LogHelper.info("签名串------------:" + msg_signature);
        LogHelper.info("时间戳------------:" + timestamp);
        LogHelper.info("随机数------------:" + nonce);
        String appid_reply="";
        try {
            appid_reply = request.getParameter("appid");
            LogHelper.info("传过来的appid为------------------》" + appid_reply);
        } catch (Exception e) {
            LogHelper.error(e,"截取appid时出现异常！！！",false);
        }
        BufferedReader br;
        String postStr = null;
        try {
            br = request.getReader();
            String buffer = null;
            StringBuffer buff = new StringBuffer();
            while ((buffer = br.readLine()) != null) {
                buff.append(buffer + "\n");
            }
            br.close();
            postStr = buff.toString();
            LogHelper.debug("接收-------------（授权后）公众号消息与事件接收---------post发送数据:\n" + postStr);
            //收到消息后，5秒内回复success
            PrintWriter pw = response.getWriter();
            pw.write("success");
            pw.flush();
            String result_json= WeiXinDecode.decode(token, key, app_id, msg_signature, timestamp, nonce, postStr);
            JSONObject json=new JSONObject(result_json);
            LogHelper.info("微信统一回复接收接口接收微信数据json---------------》" + json);
            String MsgType=json.getString("MsgType");
            String ToUserName=json.getString("ToUserName");//微信公众号
            String FromUserName=json.getString("FromUserName");//用户openid
            String event="";
            if("event".equalsIgnoreCase(MsgType))
            {
                event=json.getString("Event");
            }
            if("text".equalsIgnoreCase(MsgType))
            {
                event="text";
            }
            if("image".equalsIgnoreCase(MsgType))
            {
                event="image";
            }
            WXBizMsgCrypt pc = new WXBizMsgCrypt(token, key, app_id);

        }catch (Exception e)
        {
            LogHelper.error(e,"统一回复接收接口异常！！！",false);
        }
        return null;
    }


    public String AssemblingWeChatPublic(String appid,String infoType ,JSONObject json)
    {
        String authorizerAppid=json.getString("AuthorizerAppid");//需授权的公众号appid
        String CreateTime=json.getString("CreateTime");//微信服务端授权码创建日期（毫秒）存入数据库后转为正常日期

        String authorizationCodeExpiredTime="";//过期时间
        String authorizationCode="";
        String result="";
        if(infoType!=null && "authorized".equalsIgnoreCase(infoType))
        {
            authorizationCode=json.getString("AuthorizationCode");//需授权公众号授权码
            authorizationCodeExpiredTime=json.getString("AuthorizationCodeExpiredTime");//过期时间
        }
        List<weChatPublic> weChatPublicList= wpDao.getWeChatPublicByParamters(appid, authorizerAppid);
        weChatPublic wechat=null;
        if(weChatPublicList!=null&&weChatPublicList.size()>0)
        {
            wechat=weChatPublicList.get(0);
        }
        else
        {
            wechat=new weChatPublic();
        }
        if(infoType!=null && "unauthorized".equalsIgnoreCase(infoType))
        {
            wechat.setAuthorization_state("2");//取消授权
            wechat.setWeChatPublic_appid_refreshtoken("");
            wechat.setWechatpublic_CodeExpiredTime("");
            result=authorizerAppid+"--取消授权！";
        }
        else if(infoType!=null && "authorized".equalsIgnoreCase(infoType))
        {
            wechat.setAuthorization_state("1");//授权成功
            result=authorizerAppid+"--授权成功！";
        }
        wechat.setWechatpublic_CodeExpiredTime(authorizationCodeExpiredTime);
        wechat.setWechatpublic_Authorizer_code(authorizationCode);
        wechat.setWechatpublic_Code_CreateTime(CreateTime);
        return result+wpDao.saveOrUpdateWeChatPublic(wechat);
    }
}
