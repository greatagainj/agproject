package com.atguigu.gmall.index.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("index")
public class IndexController {

    @Autowired
    IndexService indexService;

    @GetMapping("cates")
    public Resp<List<CategoryEntity>> queryLevel1Categories() {
        List<CategoryEntity> categoryEntities = this.indexService.queryLevel1Categories();
        return Resp.ok(categoryEntities);
    }

    @GetMapping("cates/{pid}")
    public Resp<List<CategoryVo>> queryLevel23Categories(@PathVariable("pid") Long pId) {
        List<CategoryVo> categoryVos = this.indexService.queryLevel23Categories(pId);
        return Resp.ok(categoryVos);
    }
}
