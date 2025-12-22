package com.dhy.shipmanagebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.entity.Ship;
import com.dhy.shipmanagebackend.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/ship")
public class ShipController {
    @Autowired
    private ShipService shipService;

    @PostMapping
    public Result add(@RequestBody @Validated Ship ship) {
        shipService.add(ship);
        return Result.success();
    }

    // 2. 分页查询
    // URL 示例: /ships?pageNum=1&pageSize=10&name=远洋&status=IN_SERVICE
    @GetMapping
    public Result<Page<Ship>> list(@RequestParam(defaultValue = "1") int pageNum,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   @RequestParam(required = false) Long categoryId,
                                   @RequestParam(required = false) String name,
                                   @RequestParam(required = false) String status) {

        Page<Ship> page = shipService.findPage(pageNum, pageSize, categoryId, name, status);
        return Result.success(page);
    }

    // 3. 详情
    @GetMapping("/detail")
    public Result<Ship> detail(@RequestParam Long id) {
        return Result.success(shipService.findById(id));
    }

    // 4. 更新
    @PutMapping
    public Result update(@RequestBody @Validated Ship ship) {
        if (ship.getId() == null) {
            return Result.error("ID不能为空");
        }
        shipService.update(ship);
        return Result.success();
    }

    // 5. 删除
    @DeleteMapping
    public Result delete(@RequestParam Long id) {
        shipService.delete(id);
        return Result.success();
    }
}
