package com.dhy.shipmanagebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.entity.Voyage;
import com.dhy.shipmanagebackend.service.VoyageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/voyages")
public class VoyageController {

    @Autowired
    private VoyageService voyageService;

    // 1. 开航 (新增航次)
    @PostMapping
    public Result add(@RequestBody @Validated Voyage voyage) {
        voyageService.add(voyage);
        return Result.success();
    }

    // 2. 完工 (结束航次)
    // PATCH /voyages/finish?id=1
    @PatchMapping("/finish")
    public Result finish(@RequestParam Long id) {
        voyageService.finish(id);
        return Result.success();
    }

    // 3. 分页查询
    @GetMapping
    public Result<Page<Voyage>> list(@RequestParam(defaultValue = "1") int pageNum,
                                     @RequestParam(defaultValue = "10") int pageSize,
                                     @RequestParam(required = false) Long shipId) {
        Page<Voyage> page = voyageService.findPage(pageNum, pageSize, shipId);
        return Result.success(page);
    }

    // 4. 详情
    @GetMapping("/detail")
    public Result<Voyage> detail(@RequestParam Long id) {
        return Result.success(voyageService.findById(id));
    }
}