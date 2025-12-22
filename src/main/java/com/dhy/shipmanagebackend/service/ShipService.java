package com.dhy.shipmanagebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.entity.Ship;

public interface ShipService {
    // 添加
    void add(Ship ship);
    
    void update(Ship ship);

    void delete(Long id);

    Ship findById(Long id);

    Page<Ship> findPage(int pageNum, int pageSize, Long categoryId, String name, String status);
}
