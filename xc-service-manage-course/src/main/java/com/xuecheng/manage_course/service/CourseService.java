package com.xuecheng.manage_course.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.*;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    public TeachplanNode findTeachplanList(String courseId) {
        return teachplanMapper.selectList(courseId);
    }

    //添加课程计划
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if (teachplan == null ||
                StringUtils.isEmpty(teachplan.getCourseid()) ||
                StringUtils.isEmpty(teachplan.getPname())) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //课程计划
        String courseid = teachplan.getCourseid();
        //parentId
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)) {
            //取出该课程 的根节点
            parentid = getTeachplanRoot(courseid);
        }
        assert parentid != null;
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan parentNode = optional.get();
        //新节点
        Teachplan nteachplan = new Teachplan();
        //将页面提交的teachplan信息拷贝到nteachplan对象中
        BeanUtils.copyProperties(teachplan, nteachplan);
        nteachplan.setParentid(parentid);
        nteachplan.setCourseid(courseid);
        String grade = parentNode.getId();
        if (grade.equals("1")) {
            nteachplan.setGrade("2");//级别
        } else {
            nteachplan.setGrade("3");//级别
        }
        teachplanRepository.save(nteachplan);

        //要处理parentId

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程的根节点，如果查询不到要自动添加根节点
    private String getTeachplanRoot(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            return null;
        }
        CourseBase courseBase = optional.get();
        //查询课程的根节点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");


        if (teachplanList == null || teachplanList.size() <= 0) {
            //查询不到，自己添加跟节点
            Teachplan teachplan = new Teachplan();
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            teachplan.setCourseid(courseId);
            teachplan.setStatus("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        //返回根节点id
        return teachplanList.get(0).getId();
    }


    public QueryResponseResult findCourseList(int page, int size, CourseListRequest
            courseListRequest) {
        if (courseListRequest == null) {
            courseListRequest = new CourseListRequest();
        }
        if (page <= 0) {
            page = 0;
        }
        if (size == 0) {
            size = 5;
        }
        //设置分页参数
        PageHelper.startPage(page, size);
        //分页查询
        Page<CourseInfo> courseListPage = courseBaseMapper.findCourseListPage(courseListRequest);
        List<CourseInfo> courseInfos = courseListPage.getResult();
        //总记录数
        long total = courseListPage.getTotal();
        //查询结果集
        QueryResult<CourseInfo> courseInfoQueryResult = new QueryResult<CourseInfo>();
        courseInfoQueryResult.setList(courseInfos);
        courseInfoQueryResult.setTotal(total);
        return new QueryResponseResult(CommonCode.SUCCESS, courseInfoQueryResult);
    }

    //根据id查询课程
    public CourseBase getCourseBaseById(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        return optional.orElse(null);
    }

    @Transactional
    public ResponseResult updateCourseBase(String id, CourseBase courseBase) {
        CourseBase courseB = getCourseBaseById(id);
        if (courseB == null) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        courseB.setName(courseBase.getName());
        courseB.setMt(courseBase.getMt());
        courseB.setSt(courseBase.getSt());
        courseB.setGrade(courseBase.getGrade());
        courseB.setStudymodel(courseBase.getStudymodel());
        courseB.setUsers(courseBase.getUsers());
        courseB.setDescription(courseBase.getDescription());
        CourseBase save = courseBaseRepository.save(courseB);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CourseMarket getCourseMarketById(String courseId) {
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        return optional.orElse(null);
    }

    @
            Transactional
    public CourseMarket updateCourseMarket(String id, CourseMarket courseMarket) {
        CourseMarket one = this.getCourseMarketById(id);
        if (one != null) {
            one.setCharge(courseMarket.getCharge());
            one.setStartTime(courseMarket.getStartTime());//课程有效期，开始时间
            one.setEndTime(courseMarket.getEndTime());//课程有效期，结束时间
            one.setPrice(courseMarket.getPrice());
            one.setQq(courseMarket.getQq());
            one.setValid(courseMarket.getValid());
            courseMarketRepository.save(one);
        } else {
//添加课程营销信息
            one = new CourseMarket();
            BeanUtils.copyProperties(courseMarket, one);
//设置课程id
            one.setId(id);
            courseMarketRepository.save(one);
        }
        return  one;
    }

}
