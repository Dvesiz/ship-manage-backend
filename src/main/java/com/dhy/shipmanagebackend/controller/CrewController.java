package com.dhy.shipmanagebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.entity.Crew;
import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.service.CrewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crew")
public class CrewController {

    @Autowired
    private CrewService crewService;

    // 1. 新增船员
    @PostMapping
    public Result add(@RequestBody @Validated Crew crew) {
        crewService.add(crew);
        return Result.success();
    }

    // 2. 分页查询
    // 示例: /crew?pageNum=1&pageSize=10&name=张&shipId=1
    @GetMapping
    public Result<Page<Crew>> list(@RequestParam(defaultValue = "1") int pageNum,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   @RequestParam(required = false) String name,
                                   @RequestParam(required = false) Long shipId) {

        Page<Crew> page = crewService.findPage(pageNum, pageSize, name, shipId);
        return Result.success(page);
    }

    // 3. 船员详情
    @GetMapping("/detail")
    public Result<Crew> detail(@RequestParam Long id) {
        Crew crew = crewService.findById(id);
        return Result.success(crew);
    }

    // 4. 更新船员信息
    @PutMapping
    public Result update(@RequestBody @Validated Crew crew) {
        if (crew.getId() == null) {
            return Result.error("ID不能为空");
        }
        crewService.update(crew);
        return Result.success();
    }

    // 5. 删除船员
    @DeleteMapping
    public Result delete(@RequestParam Long id) {
        crewService.delete(id);
        return Result.success();
    }
}