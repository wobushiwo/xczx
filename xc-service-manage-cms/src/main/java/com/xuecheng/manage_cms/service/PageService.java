package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService {
    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    CmsConfigRepository cmsConfigRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    RabbitTemplate rabbitTemplate;

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
            cmsPage1.setDataUrl(cmsPage.getDataUrl());
            //执行更新
            cmsPage1 = cmsPageRepository.save(cmsPage1);
        }
        return new CmsPageResult(CommonCode.SUCCESS, cmsPage1);
    }

    public ResponseResult delete(String id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()) {
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    public CmsConfig getConfigById(String id) {
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        return optional.orElse(null);
    }

    public String getPageHtml(String pageId) {
        //获取数据模型
        Map modelMap = getModelByPageId(pageId);
        if (modelMap == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //获取模板信息
        String template = getTemplateByPageId(pageId);
        if (template == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //执行静态方法
        String html = generateHtml(template, modelMap);
        return html;

    }

    //执行静态化方法
    private String generateHtml(String templateContent, Map model) {
        //创建一个配置对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        //模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", templateContent);
        //想Configuration配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        //获取模板
        try {
            Template template = configuration.getTemplate("template");
            //调用api进行静态化
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取模板信息
    private String getTemplateByPageId(String pageId) {
        CmsPage cmsPage = findById(pageId);
        //如果页面为空就抛出异常
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_FAGE_NOTEXISTS);
        }
        //获取页面的模板id
        String templateId = cmsPage.getTemplateId();
        if (templateId == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //查询模板信息
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()) {
            //获取模板对象
            CmsTemplate cmsTemplate = optional.get();
            //得到模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //从GridFS中取模本文件的内容
            //根据文件的id查询文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));

            //打开一个下载流
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建一个GridFsResource对象，用于操作了
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            //从流中取数据
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Map getModelByPageId(String pageId) {
        // 取出页面的信息
        CmsPage cmsPage = this.findById(pageId);
        //如果页面为空就抛出异常
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_FAGE_NOTEXISTS);
        }
        String dataUrl = cmsPage.getDataUrl();
        //如果路径为空就抛出路径为空
        if (dataUrl == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        ResponseEntity<Map> entity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = entity.getBody();
        System.out.println(body);
        return body;
    }

    //发布页面
    public ResponseResult post(String pageId) {
        //执行页面静态化
        String pageHtml = getPageHtml(pageId);
        //将页面静态化文件存储到GridFs中
        CmsPage cmsPage = saveHtml(pageId, pageHtml);
        //先MQ发消息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //向MQ发送消息
    private void sendPostPage(String pageId) {
        //得到页面信息
        CmsPage cmsPage = findById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        //创建消息对象
        Map<String,String> msg= new HashMap<>();
        msg.put("pageId",pageId);
        //转成json串
        String jsonString = JSON.toJSONString(msg);
        //发送给mq
        //站点id
        String siteId = cmsPage.getSiteId();
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId,jsonString);
    }

    private CmsPage saveHtml(String pageId, String htmlContent) {
        //得到页面信息
        CmsPage cmsPage = findById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        ObjectId objectId = null;
        try {
            //将htmlContent内容转换成输入流
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "utf-8");
            //将html文件内容保存到GridFs中
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //将html文件id更新到cmsPage中
        cmsPage.setHtmlFileId(objectId.toString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }
}
