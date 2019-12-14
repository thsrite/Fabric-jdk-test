package com.pig4cloud.pig.fabric.api.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 *
 *
 * @author MrBird
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TTcademician implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "academician_id", type = IdType.AUTO)
    private Long academicianId;

    /**
     * 中文名
     */
    private String chineseName;

    /**
     * 姓名
     */
    private String realName;

    /**
     * 性别
     */
    private String asex;

    /**
     * 出生年月
     */
    private LocalDate abirthday;

    /**
     * 国籍
     */
    private String nationality;

    /**
     * 所属科学院
     */
    private String aos;

    /**
     * 学历
     */
    private String education;

    /**
     * 工作单位
     */
    private String workUnit;

    /**
     * 职务
     */
    private String duty;

    /**
     * 电话
     */
    private String tel;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 研究领域
     */
    private String rsf;

    /**
     * 专利
     */
    private String patent;

    /**
     * H-index
     */
    private String hindex;

    /**
     * 项目简介
     */
    private String projectProfile;

    /**
     * 主要成就
     */
    private String mjam;

    /**
     * 照片
     */
    private String photo;

    /**
     * 个人主页
     */
    private String url;

}
