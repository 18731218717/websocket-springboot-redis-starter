package org.dsm.trainingsystem.rest.entity;

import io.swagger.annotations.ApiModelProperty;

import javax.websocket.Session;

/**
 * @ Author     ：zhengkai.
 * @ Date       ：Created in 8:44 2020/5/22
 * @ Description：存放多人会话人员信息
 * @ Modified By：
 * @Version: 1.0$
 */
public class SocketUserInfo {
    @ApiModelProperty(value = "用户信息", name = "userInfo")
    User user;
    @ApiModelProperty(value = "聊天室id", name = "roomId")
    String roomId;
    @ApiModelProperty(value = "用户id", name = "userId")
    String userId;

    /*
    20200525郑凯修改,session单独存于本地缓存,其余信息存于Redis做分布式共享数据
     */
/*    @ApiModelProperty(value = "用户Session", name = "session")
    Session session;*/

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

/*    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }*/
}
