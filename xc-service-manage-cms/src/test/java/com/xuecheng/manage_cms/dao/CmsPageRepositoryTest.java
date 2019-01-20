package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsPageParam;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {
    @Autowired
    CmsPageRepository cmsPageRepository;
    @Test
    public void testFindAll() {
        List<CmsPage> all = cmsPageRepository.findAll();
        System.out.println(all);
    }

    //分页查询
    @Test
    public void testFindPage() {
        int page = 1; //从0开始
        int size = 10; //查10条
        Pageable  pageable = PageRequest.of(page,size);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        System.out.println(all);
    }

    @Test
    public void testInsert() {
        //定义实体类
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId("s01");
        cmsPage.setTemplateId("t01");
        cmsPage.setPageName("测试页面");
        cmsPage.setPageCreateTime(new Date());
        List<CmsPageParam> cmsPageParams = new ArrayList<>();
        CmsPageParam cmsPageParam = new CmsPageParam();
        cmsPageParam.setPageParamName("param1");
        cmsPageParam.setPageParamValue("value1");
        cmsPageParams.add(cmsPageParam);
        cmsPage.setPageParams(cmsPageParams);
        cmsPageRepository.save(cmsPage);
        System.out.println(cmsPage);
    }

    //删除
    @Test
    public void testDelete() {
        cmsPageRepository.deleteById("5c3edc12e7f258545c0d944f");
    }
    //更新
    @Test
    public void testUpdate() {
        //查询对象
        Optional<CmsPage> optional = cmsPageRepository.findById("5c3edcdae7f258445809ac72");
        if (optional.isPresent()){
            CmsPage cmsPage = optional.get();
            //修改属性
            cmsPage.setPageName("测试测试页面");
            //..
            //设置修改
            CmsPage save = cmsPageRepository.save(cmsPage);
            System.out.println(save);
        }
    }
    //根据页面名称查询
    @Test
    public void testFindByPageName() {
        CmsPage cs = cmsPageRepository.findByPageName("测试测试页面");
        System.out.println(cs);
    }

    //自定义条件查询
    @Test
    public void testFindAllByExample(){
        //分页参数
        int page = 0; //从0开始
        int size = 10; //查10条
        Pageable  pageable = PageRequest.of(page,size);
        //条件值对象
        CmsPage cmsPage = new CmsPage();
        //要查询5a795ac7dd573c04508f3a56站点的页面
//        cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
//        cmsPage.setTemplateId("5a962b52b00ffc514038faf7");
        cmsPage.setPageAliase("预览");
        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        exampleMatcher = exampleMatcher.withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
        //定义Example
        Example<CmsPage> example =  Example.of(cmsPage,exampleMatcher);

        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        List<CmsPage> content = all.getContent();
        System.out.println(content);
    }
}
