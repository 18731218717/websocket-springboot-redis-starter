package org.dsm.trainingsystem.rest.websocket;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dsm.trainingsystem.rest.entity.SocketRoomInfo;
import org.dsm.trainingsystem.rest.entity.SocketUserInfo;
import org.dsm.trainingsystem.rest.entity.User;
import org.dsm.trainingsystem.rest.redis.RedisTools;
import org.dsm.trainingsystem.util.JWTHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ Author     ：zhengkai.
 * @ Date       ：Created in 16:02 2020/5/19
 * @ Description：WebSocket服务端
 * @ Modified By：
 * @Version: 1.0$
 */
@Component
@ServerEndpoint(value = "/WebSocketServer/{roomId}/{userId}", configurator = EndpointConfigure.class)
public class WebSocketServer {

    private  ConcurrentHashMap<String, SocketRoomInfo> roomList = null;

    /*
    存儲本地session集合
     */
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String,Session>> roomSessionList = new ConcurrentHashMap<String, ConcurrentHashMap<String,Session>>();


    private static final String normal_msg="normal_msg";                        //正常会话消息 (消息)
    private static final String membersInfo_notice="membersInfo_notice";        //在线人员列表 (通知)
    private static final String num_notice="num_notice";                        //在线人数通知 (通知)


    @Autowired
    RedisTools redisTools;

    private Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    /**
     * 接收Redis中发布的消息
     *
     * @param message
     * @return
     */
    public void receiveMessage(String message) {
        //序列化对象（特别注意：发布的时候需要设置序列化；订阅方也需要设置序列化）
        Jackson2JsonRedisSerializer seria = new Jackson2JsonRedisSerializer(HashMap.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        seria.setObjectMapper(objectMapper);
        HashMap<String, String> data = (HashMap<String, String>) seria.deserialize(message.getBytes());
        String roomId = data.get("roomId");
        String userId = data.get("userId");
        Object msg = data.get("msg");
        String msgType = data.get("msgType");
        String broadcastIndex = data.get("broadcastIndex");

        /*
        将接收到的消息推送给本机存储的用户
         */
        broadcastMessage(roomId, userId, getStandardMessage(msg,userId,msgType,null), broadcastIndex);              //将其他机器发来的消息发送给本机的用户
    }

    /**
     * 连接建立成功调用的方法(此处的userId即是用户名)
     */
    @OnOpen
    public void onOpen(@PathParam("roomId") String roomId, @PathParam("userId") String userId, Session session) {
        log.info("新客户端连入，房间号:" + roomId + " 用户id：" + userId);
        if(StringUtils.isEmpty(roomId)||StringUtils.isEmpty(userId)){
            return;
        }

        roomList=redisTools.get("roomList")!=null? (ConcurrentHashMap<String, SocketRoomInfo>) redisTools.get("roomList") :new ConcurrentHashMap<String, SocketRoomInfo>();

        String token = redisTools.get(userId + "_token") != null ? redisTools.get(userId + "_token").toString() : "";
        Map<String, Object> userInfoMap= JWTHelper.getUserInfo(token);
        User user = new User();
        try {
            if(userInfoMap!=null){
                user.setId(userInfoMap.get("id").toString());           //id
                user.setName(userInfoMap.get("name").toString());       //用户名
                user.setNamech(userInfoMap.get("namech").toString());   //姓名
                user.setSex(Integer.parseInt(userInfoMap.get("sex").toString()));   //性别
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        /*
        创建一个新成员(存储用户信息)
         */
        SocketUserInfo socketUserInfo = new SocketUserInfo();
        socketUserInfo.setRoomId(roomId);
        socketUserInfo.setUserId(userId);
        //socketUserInfo.setSession(session);
        socketUserInfo.setUser(user);

        /*
        如果该聊天室已经存在,那么直接让该成员加入,反之先创建聊天室再加入
         */
        ConcurrentHashMap<String, SocketUserInfo> members = null;
        ConcurrentHashMap<String,Session> sessions=null;
        if (roomList.containsKey(roomId)&&roomSessionList.containsKey(roomId)) {
            members = roomList.get(roomId).getMembers();
            members.put(userId, socketUserInfo);        //存用户常规信息于Redis
            sessions = roomSessionList.get(roomId);
            sessions.put(userId,session);               //存用户session信息于本地
        } else {
            /*
            创建新房间存用户信息
             */
            members = new ConcurrentHashMap<String, SocketUserInfo>();
            members.put(userId, socketUserInfo);
            SocketRoomInfo socketRoomInfo = new SocketRoomInfo();
            socketRoomInfo.setMembers(members);
            socketRoomInfo.setRoomId(roomId);
            roomList.put(roomId, socketRoomInfo);

            /*
            创建新房间存用户session信息
             */
            sessions = new ConcurrentHashMap<String,Session>();
            sessions.put(userId,session);
            roomSessionList.put(roomId,sessions);
        }


        if (roomId!=null&&userId != null) {
            /*
            将消息发布到Redis供其他机器订阅(包括本机)
             */
            String msg=userId + "连接成功-" + "-当前在线人数为：" + roomList.get(roomId).getMembers().size();
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("roomId",roomId);
            data.put("userId",userId);
            data.put("msg",msg);
            data.put("msgType",num_notice);
            data.put("broadcastIndex","all");
            redisTools.sendMessage(data);

            /*
            将该消息发送给本机人员(由于自身监听了自身,因此此处不用再给本机人员单独发送消息,不然会重复)
             */
            //broadcastMessage(roomId,userId,getStandardMessage(msg,userId,num_notice,null),"all");



            /*
            发送在线人员列表信息
             */
            sendMembersInfo(roomId, userId, session, members);

            /*
            更新Redis缓存
             */
            redisTools.set("roomList",roomList);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("roomId") String roomId, @PathParam("userId") String userId, Session session) {
        log.info("成员" + userId + "退出房间" + roomId);

        roomList=(ConcurrentHashMap<String, SocketRoomInfo>) redisTools.get("roomList");

        if(roomList!=null){
            if (roomList.get(roomId) != null) {
                roomList.get(roomId).getMembers().remove(userId);
            }
            if(roomSessionList.get(roomId)!=null){
                roomSessionList.get(roomId).remove(userId);
            }
        /*
        将消息发布到Redis供其他机器订阅(包括本机)
         */
            String msg=userId + "退出-" + "-当前在线人数为：" + roomList.get(roomId).getMembers().size();
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("roomId",roomId);
            data.put("userId",userId);
            data.put("msg",msg);
            data.put("msgType",num_notice);
            data.put("broadcastIndex","all");
            redisTools.sendMessage(data);

         /*
        将该消息发送给本机人员(由于自身监听了自身,因此此处不用再给本机人员单独发送消息,不然会重复)
         */
            //broadcastMessage(roomId,userId,getStandardMessage(msg,userId,num_notice,null),"all");

         /*
        发送在线人员列表信息
         */
            sendMembersInfo(roomId,userId,session,roomList.get(roomId).getMembers());

        /*
        更新Redis缓存
        */
            redisTools.set("roomList",roomList);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(@PathParam("roomId") String roomId, @PathParam("userId") String userId, Session session, String message) throws IOException {
        log.info(roomId + "-" + userId + ":发送过来的消息为：" + message);

        /*
        将消息发布到Redis供其他机器订阅(包括本机)
         */
        String msg=message;
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("roomId",roomId);
        data.put("userId",userId);
        data.put("msg",msg);
        data.put("msgType",normal_msg);
        data.put("broadcastIndex","other");
        redisTools.sendMessage(data);

        /*
        将该消息发送给本机人员(由于自身监听了自身,因此此处不用再给本机人员单独发送消息,不然会重复)
         */
        //broadcastMessage(roomId, userId, getStandardMessage(msg,userId,normal_msg,null),"other");
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("websocket出现错误");
        error.printStackTrace();
    }

    /**
     * 发送普通消息
     *
     * @param message
     * @param session
     */
    public void sendMessage(String message, Session session) {
        try {
            session.getBasicRemote().sendText(message);
            log.info("推送消息成功，消息为：" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送二进制消息(文件、图片)
     */
    public void sendBinaryMessage(ByteBuffer message, Session session) {
        try {
            session.getBasicRemote().sendBinary(message);
            log.info("推送流消息成功!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送消息对象
     *
     * @param message
     * @param session
     */
    public void sendObjectMessage(Object message, Session session) {
        try {
            session.getBasicRemote().sendObject(message);
            log.info("推送对象消息成功!");
        } catch (IOException | EncodeException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送在线人员列表信息
     *
     * @param members
     * @param session
     */
    public void  sendMembersInfo(String roomId,String userId,Session session,ConcurrentHashMap<String, SocketUserInfo> members){
        /**
         * 指定排除属性过滤器和包含属性过滤器
         * 指定排除属性过滤器：转换成JSON字符串时，排除哪些属性
         * 指定包含属性过滤器：转换成JSON字符串时，包含哪些属性
         * 此处排除session,首先session属性传递到前端无意义,其次session属性转json会异常
         */
        String[] excludeProperties = {"session"};   //20200525 郑凯去除session,加与不加不影响,因此暂不做修改
        // String[] includeProperties = {"id", "username", "mobile"};
        PropertyPreFilters filters = new PropertyPreFilters();
        PropertyPreFilters.MySimplePropertyPreFilter excludefilter = filters.addFilter();
        excludefilter.addExcludes(excludeProperties);
/*          PropertyPreFilters.MySimplePropertyPreFilter includefilter = filters.addFilter();
            includefilter.addIncludes(includeProperties);*/

        /*
        将消息发布到Redis供其他机器订阅(包括本机)
         */
        String msg=userId + "连接成功-" + "-当前在线人数为：" + roomList.get(roomId).getMembers().size();
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("roomId",roomId);
        data.put("userId",userId);
        data.put("msg",members);
        data.put("msgType",membersInfo_notice);
        data.put("broadcastIndex","all");
        redisTools.sendMessage(data);

       // broadcastMessage(roomId,userId,getStandardMessage(members,userId,membersInfo_notice,excludefilter),"all");
    }


    /**
     * 得到标准的信息
     * @param message
     * @param msgType
     * @param excludefilter
     * @return
     */
    public String getStandardMessage(Object message,String userId,String msgType,PropertyPreFilters.MySimplePropertyPreFilter excludefilter){
        Map<String, Object> retInfo = new HashMap<>();
        retInfo.put("msg",message);
        retInfo.put("msg_type",msgType);
        retInfo.put("send_id",userId);
        String jsonStr="";
        if(excludefilter!=null){
            jsonStr=JSONObject.toJSONString(retInfo,excludefilter);
        }else{
            jsonStr=JSONObject.toJSONString(retInfo);
        }
        return jsonStr;
    }


    /**
     * 广播自定义消息
     * @param roomId
     * @param userId
     * @param message
     * @param broadcastIndex 广播标识(all[包括自己],other)
     */
    public void broadcastMessage(String roomId, String userId, String message,String broadcastIndex) {
        if(roomList!=null){
            if (roomList.get(roomId) != null&&roomSessionList.get(roomId)!=null) {
                ConcurrentHashMap<String, SocketUserInfo> members = roomList.get(roomId).getMembers();
                ConcurrentHashMap<String, Session> sessions = roomSessionList.get(roomId);
                for (String key: members.keySet()) {
                    if (members.get(key) != null) {
                        Session session1 = sessions.get(key);
                        String userId1=members.get(key).getUserId();
                        if(session1!=null&&userId!=null){
                            if("all".equalsIgnoreCase(broadcastIndex)){
                                sendMessage(message, session1);
                            }else if("other".equalsIgnoreCase(broadcastIndex)){
                                if (!userId1.equals(userId)) {            //不同服务器的session ID值可能会出现重复的情况,因此此处使用人员ID
                                    sendMessage(message, session1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
