package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */

@Api(tags = "员工相关接口")
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工注销")
    public Result<String> logout() {
        return Result.success();
    }


    /**
     * 新增员工
     *
     * @param employeeDTO
     * @return
     */
    @ApiOperation(value = "新增员工")
    @PostMapping
    public Result addEmployee(@RequestBody EmployeeDTO employeeDTO) {   //这里使用DTO对象接收值，可以缩小数据范围

        //新建员工对象
        Employee employee = new Employee();
        //进行属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);

        //新增员工
        Result result = employeeService.add(employee);

        return result;
    }


    /**
     * 分页查询
     *
     * @param pageQueryDTO
     * @return
     */
    @ApiOperation(value = "获取分页数据")
    @GetMapping("/page")
    public Result<PageResult> getPage(EmployeePageQueryDTO pageQueryDTO) {

        PageResult pageResult = employeeService.getPage(pageQueryDTO);

        return Result.success(pageResult);
    }


    /**
     * 修改员工状态
     *
     * @param status
     * @param id
     * @return
     */
    @ApiOperation(value = "修改员工状态")
    @PostMapping("/status/{status}")
    public Result changeStatus(@PathVariable int status, Long id) {

        Result result = employeeService.changeStatus(status, id);

        return result;
    }

    /**
     * 根据id查询员工数据
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "根据id查询员工数据")
    @GetMapping("/{id}")
    public Result<EmployeeDTO> getEmpById(@PathVariable Long id) {

        EmployeeDTO employeeDTO = employeeService.getEmpById(id);

        return Result.success(employeeDTO);
    }


    /**
     * 修改员工数据
     * @param employeeDTO
     * @return
     */
    @ApiOperation(value = "修改员工数据")
    @PutMapping
    public Result modifyEmp(@RequestBody EmployeeDTO employeeDTO) {

        Result result = employeeService.modifyEmp(employeeDTO);

        return result;
    }


}
