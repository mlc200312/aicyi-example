package io.github.aicyi.example.web;

import io.github.aicyi.commons.lang.model.Result;
import io.github.aicyi.commons.core.mapper.BeanMapper;
import io.github.aicyi.example.domain.StudentQuery;
import io.github.aicyi.example.web.vo.AddStudentReq;
import io.github.aicyi.midware.web.model.PageResponse;
import io.github.aicyi.example.domain.StudentBean;
import io.github.aicyi.example.web.vo.StudentReq;
import io.github.aicyi.example.web.vo.StudentResp;
import io.github.aicyi.example.service.StudentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Mr.Min
 * @description 学生控制器
 * @date 15:45
 **/
@Validated
@Api(value = "学生控制器", tags = {"学生控制器"})
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final BeanMapper beanMapper;
    private final StudentService studentService;

    @ApiOperation(value = "查询学生", notes = "查询学生")
    @ApiImplicitParam(
            name = "Authorization",
            value = "令牌",
            required = true,
            paramType = "header",
            dataTypeClass = String.class
    )
    @RequestMapping(value = "/get-by-id", method = RequestMethod.GET)
    public Result<StudentResp> getById(@RequestParam String id) {
        StudentBean bean = studentService.getById(Long.valueOf(id));
        StudentResp resp = beanMapper.map(bean, StudentResp.class);
        return Result.success(resp);
    }

    @ApiOperation(value = "按手机号查询学生", notes = "按手机号查询学生")
    @ApiImplicitParam(
            name = "Authorization",
            value = "令牌",
            required = true,
            paramType = "header",
            dataTypeClass = String.class
    )
    @RequestMapping(value = "/get-by-mobile", method = RequestMethod.GET)
    public Result<StudentResp> getByMobile(@RequestParam String mobile) {
        StudentBean bean = studentService.getByMobile(mobile);
        StudentResp resp = beanMapper.map(bean, StudentResp.class);
        return Result.success(resp);
    }

    @ApiOperation(value = "分页查询学生", notes = "分页查询学生")
    @ApiImplicitParam(
            name = "Authorization",
            value = "令牌",
            required = true,
            paramType = "header",
            dataTypeClass = String.class
    )
    @RequestMapping(value = "/paged-list", method = RequestMethod.GET)
    public Result<PageResponse<StudentResp>> pagedList(@Validated @ModelAttribute StudentReq req) {
        StudentQuery query = beanMapper.map(req, StudentQuery.class);
        Page<StudentBean> page = studentService.pagedList(query);
        List<StudentResp> respList = beanMapper.mapList(page.getContent(), StudentResp.class);
        return Result.success(PageResponse.build(respList, page));
    }

    @ApiOperation(value = "新增学生", notes = "新增学生")
    @ApiImplicitParam(
            name = "Authorization",
            value = "令牌",
            required = true,
            paramType = "header",
            dataTypeClass = String.class
    )
    @RequestMapping(value = "/add-student", method = RequestMethod.POST)
    public Result<Void> addStudent(@Validated @RequestBody AddStudentReq req) {
        StudentBean bean = beanMapper.map(req, StudentBean.class);
        studentService.add(bean);
        return Result.success();
    }
}
