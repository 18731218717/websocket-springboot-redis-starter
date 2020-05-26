package org.dsm.trainingsystem.rest.entity;

import io.swagger.annotations.ApiModelProperty;

import java.sql.Timestamp;


public class User {

    @ApiModelProperty(value = "用户id", name = "id")
    String id;

    @ApiModelProperty(value = "英文名", name = "name")
    String name;

    @ApiModelProperty(value = "中文名", name = "namech")
    String namech;

    @ApiModelProperty(value = "性别", name = "sex")
    Integer sex;

    @ApiModelProperty(value = "电话", name = "tel")
    String tel;


    @ApiModelProperty(value = "照片", name = "image")
    String image;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamech() {
        return namech;
    }

    public void setNamech(String namech) {
        this.namech = namech;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
