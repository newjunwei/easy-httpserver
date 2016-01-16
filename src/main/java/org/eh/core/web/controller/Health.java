package org.eh.core.web.controller;

import org.eh.core.annotation.RequestMapping;
import org.eh.core.model.ResultInfo;

import java.util.Map;

/**
 * Created by zhangjw on 16/01/16.
 */
@org.eh.core.annotation.Controller(name = "test1", url = "/health/")
public class Health implements Controller{

    @RequestMapping
    public ResultInfo isGood(Map<String, Object> map) {
        ResultInfo info = new ResultInfo();
        info.setView("json:good");
        return info;
    }
}
