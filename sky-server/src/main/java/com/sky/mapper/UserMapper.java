package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * ClassName: UserMapper
 *
 * @Author Mobai
 * @Create 2023/11/14 19:19
 * @Version 1.0
 * Description:
 */

@Mapper
public interface UserMapper {

    @Select("select * from user where openid = #{openId}")
    User getUser(String openId);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into user(openid, name, phone, sex, id_number, avatar, create_time) " +
            "VALUE (#{openid},#{name},#{phone},#{sex},#{idNumber},#{avatar},#{createTime})")
    void addUser(User user);

    @Select("select name from user where id = #{userId}")
    String getUserName(Long userId);

    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    @Select("select count(*) from user where create_time <= #{date}")
    Integer getTotalUserLt(LocalDateTime date);

    @Select("select count(*) from user where create_time between #{begin} and #{end}")
    Integer getNewUser(LocalDateTime begin,LocalDateTime end);


    /**
     * 根据动态条件统计用户数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}