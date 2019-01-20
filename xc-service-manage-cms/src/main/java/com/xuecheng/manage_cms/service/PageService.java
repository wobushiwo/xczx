package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PageService {
    @Autowired
    CmsPageRepository cmsPageRepository;

    /**
     * 页面查询给用户看的时候为1
     *
     * @param page             页码从1开始计数
     * @param size             每页记录数
     * @param queryPageRequest 查询条件
     * @return 查询结果
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {

        //判断QueryPageRequest 是否为空
        if (queryPageRequest == null) {
            queryPageRequest = new QueryPageRequest();
        }
        //自定义条件查询
        //条件值对象
        CmsPage cmsPage = new CmsPage();
        //设置条件参数站点id
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())) {
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //设置模板id作为查询条件
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())) {
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        //设置页面别名作为查询条件
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())) {
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }

        //定义条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        //定义条件对象
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);


        if (page <= 0) {
            page = 1;
        }
        page = page - 1;
        if (size <= 0) {
            size = 10;
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());//数据列表
        queryResult.setTotal(all.getTotalElements());//数据总记录数
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }

    public CmsPageResult add(CmsPage cmsPage) {
        //根据页面名称，站点id 和访问路径
        //判断需要存入的页面是否存在
        CmsPage cp = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        //不存在存入
        if (cp != null) {
            //页面存在
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
            cmsPage.setPageId(null);
            CmsPage save = cmsPageRepository.save(cmsPage);
            return new CmsPageResult(CommonCode.SUCCESS, save);
    }

    //根据页面id查询页面信息
    public CmsPage findById(String id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        //判断optional是否为空
        return optional.orElse(null);
    }

    //修改方法
    public CmsPageResult edit(String id, CmsPage cmsPage) {
        //调用本类方法查询
        CmsPage cmsPage1 = this.findById(id);
        if (cmsPage1 != null) {
            //修改数据
            //设置修改的数据
            cmsPage1.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            cmsPage1.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            cmsPage1.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            cmsPage1.setPageName(cmsPage.getPageName());
            //更新访问路径
            cmsPage1.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            cmsPage1.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //执行更新
            cmsPage1 = cmsPageRepository.save(cmsPage1);
        }
        return new CmsPageResult(CommonCode.SUCCESS,cmsPage1);
    }

    public ResponseResult delete(String id){
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()){
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
            return new ResponseResult(CommonCode.FAIL);
    }

}
