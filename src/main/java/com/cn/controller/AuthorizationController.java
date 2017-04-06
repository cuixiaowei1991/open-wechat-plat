package com.cn.controller;

import com.cn.common.LogHelper;
import com.cn.service.ThirdTogetPubFuncService;
import com.cn.service.impl.weChatThridServiceImpl;
import com.cn.service.weChatThridService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * Created by cuixiaowei on 2017/2/23.
 */
@Controller
@RequestMapping("/authorization")
public class AuthorizationController
{

    @Autowired
    private weChatThridService wcsImpl;
    @Autowired
    private ThirdTogetPubFuncService togetPubFuncService;
    @RequestMapping(value="/componentVerifyTicket",produces="application/json;charset=UTF-8")
    public void getComponentVerifyTicket(HttpServletResponse response,HttpServletRequest request)
    {
        try {
            wcsImpl.handleAuthorize(request,response);
            PrintWriter pw = response.getWriter();
            pw.write("success");
            pw.flush();
        } catch (Exception e) {
            LogHelper.error(e,"授权异常！！！！",false);
            e.printStackTrace();
        }
    }
    @ResponseBody
    @RequestMapping(value="/authorizQrCodeUrl",produces="application/json;charset=UTF-8")
    public String Authorization_QrCodeUrl(HttpServletResponse response)
    {
        String qrCode="";
        try {
            qrCode= wcsImpl.entranceWinXin();
            PrintWriter pw = response.getWriter();
            pw.write("success");
            pw.flush();
        } catch (Exception e) {
            LogHelper.error(e,"获取授权二维码异常！！！！",false);
            e.printStackTrace();
        }

        return qrCode;
    }

    @ResponseBody
    @RequestMapping(value="/signPackage",produces="application/json;charset=UTF-8")
    public String signPackage(HttpServletResponse response,HttpServletRequest request)
    {
        String qrCode="";
        try {
            qrCode= togetPubFuncService.getSignPackage(request.getParameter("appid"),request.getHeader("referer"));
        } catch (Exception e) {
            LogHelper.error(e,"h5签名注册异常！！！！",false);
            e.printStackTrace();
        }
        return qrCode;
    }
}
