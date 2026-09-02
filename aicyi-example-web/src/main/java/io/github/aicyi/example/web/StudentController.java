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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "学生控制器")
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final BeanMapper beanMapper;
    private final StudentService studentService;

    @Operation(summary = "查询学生", description = "查询学生")
    @Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))
    @RequestMapping(value = "/get-by-id", method = RequestMethod.GET)
    public Result<StudentResp> getById(@RequestParam String id) {
        StudentBean bean = studentService.getById(Long.valueOf(id));
        StudentResp resp = beanMapper.map(bean, StudentResp.class);
        return Result.success(resp);
    }

    @Operation(summary = "按手机号查询学生", description = "按手机号查询学生")
    @Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))
    @RequestMapping(value = "/get-by-mobile", method = RequestMethod.GET)
    public Result<StudentResp> getByMobile(@RequestParam String mobile) {
        StudentBean bean = studentService.getByMobile(mobile);
        StudentResp resp = beanMapper.map(bean, StudentResp.class);
        return Result.success(resp);
    }

    @Operation(summary = "分页查询学生", description = "分页查询学生")
    @Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))
    @RequestMapping(value = "/paged-list", method = RequestMethod.GET)
    public Result<PageResponse<StudentResp>> pagedList(@Validated @ModelAttribute StudentReq req) {
        StudentQuery query = beanMapper.map(req, StudentQuery.class);
        Page<StudentBean> page = studentService.pagedList(query);
        List<StudentResp> respList = beanMapper.mapList(page.getContent(), StudentResp.class);
        return Result.success(PageResponse.build(respList, page));
    }

    @Operation(summary = "新增学生", description = "新增学生")
    @Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))
    @RequestMapping(value = "/add-student", method = RequestMethod.POST)
    public Result<Void> addStudent(@Validated @RequestBody AddStudentReq req) {
        StudentBean bean = beanMapper.map(req, StudentBean.class);
        studentService.add(bean);
        return Result.success();
    }
}
