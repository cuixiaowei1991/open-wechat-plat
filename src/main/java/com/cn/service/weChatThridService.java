package com.cn.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by cuixiaowei on 2017/2/23.
 */
public interface weChatThridService
{
    public void handleAuthorize(HttpServletRequest request, HttpServletResponse response) throws Exception;

    public String entranceWinXin() throws Exception;

    public String commonReturn(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
