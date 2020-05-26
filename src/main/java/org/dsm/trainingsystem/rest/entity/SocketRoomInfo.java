package org.dsm.trainingsystem.rest.entity;

import io.swagger.annotations.ApiModelProperty;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @ Author     ：zhengkai.
 * @ Date       ：Created in 9:14 2020/5/22
 * @ Description：聊天室信息
 * @ Modified By：
 * @Version: 1.0$
 */
public class SocketRoomInfo {
    @ApiModelProperty(value = "聊天室成员列表", name = "members")
    ConcurrentHashMap<String,SocketUserInfo> members;
    @ApiModelProperty(value = "聊天室id", name = "roomId")
    String roomId;

    public ConcurrentHashMap<String, SocketUserInfo> getMembers() {
        return members;
    }

    public void setMembers(ConcurrentHashMap<String, SocketUserInfo> members) {
        this.members = members;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

/*    public int getMembersNum(){
        return this.members.size();
    }*/


}
