package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GridFsTest {
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;

    @Test
    public void testStore() throws FileNotFoundException {
        File file = new File("E:/xczx/scdym/index_banner.ftl");
        //定义file
        FileInputStream fis = new FileInputStream(file);
        ObjectId objectId = gridFsTemplate.store(fis, "index_banner.ftl");
        System.out.println(objectId);
    }

    @Test
    public void queryFile() throws IOException {
        //根据文件的id查询文件
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("5c45305ee7f2581d503ca5df")));

        //打开一个下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //创建一个GridFsResource对象，用于操作了
        GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
        //从流中取数据
        String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
        System.out.println(content);
    }

    @Test
    public void test() {
        List<CmsTemplate> list = cmsTemplateRepository.findAll();
        for (CmsTemplate cmsTemplate : list) {
            System.out.println(cmsTemplate.getTemplateFileId());
        }
    }
}
