package com.tju.elm.user.mapper;

import com.tju.elm.user.zoo.pojo.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SystemConfigMapper {
    @Select("select * from system_config where config_key = #{key};")
    SystemConfig getConfigByKey(String key);

    @Update("update system_config set config_value = #{configValue}, value_type = {valueType} ,description = #{description} where config_key = #{key}")
    void updateConfig(String key, SystemConfig systemConfig);
}
