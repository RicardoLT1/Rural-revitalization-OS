package com.xiangyun.os.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiangyun.os.entity.TodoItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoItemMapper extends BaseMapper<TodoItem> {
}
