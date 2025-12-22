package com.dhy.shipmanagebackend.controller;


import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.entity.ShipCategory;
import com.dhy.shipmanagebackend.service.ShipCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/ship-categories")
public class ShipCategoryController {
    @Autowired
    private ShipCategoryService shipCategoryService;

    @PostMapping
    public Result add(@RequestBody @Validated ShipCategory shipCategory) {
        shipCategoryService.add(shipCategory);
        return Result.success();
    }

    @GetMapping
    public Result<List<ShipCategory>> list() {
        return Result.success(shipCategoryService.findAll());
    }

    @GetMapping("/detail")
    public Result<ShipCategory> detail(@RequestParam Long id) {
        return Result.success(shipCategoryService.findById(id));
    }

    @PutMapping
    public Result update(@RequestBody @Validated ShipCategory shipCategory) {
        shipCategoryService.update(shipCategory);
        return Result.success();
    }

    @DeleteMapping
    public Result delete(@RequestParam Long id) {
        shipCategoryService.delete(id);
        return Result.success();
    }
}
