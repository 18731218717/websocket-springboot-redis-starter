package org.dsm.trainingsystem.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.dsm.trainingsystem.rest.entity.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTHelper {

    private static final long EXPIRE_TIME = 30*60*1000;     //过期时间
    static Algorithm algorithm = Algorithm.HMAC256("secret");

    public static String getToken(Map<String, Object> map){

        Date expiresTime = new Date(System.currentTimeMillis() + EXPIRE_TIME);

        String token = JWT.create()
                .withIssuer("auth0")   //发布者
                //.withSubject("test")    //主题
                //.withAudience(audience)     //观众，相当于接受者
                //.withIssuedAt(new Date())   // 生成签名的时间
                .withExpiresAt(expiresTime)    // 生成签名的有效期,分钟
                .withClaim("username", map.get("name").toString()) //20200521 郑凯修改,暂时不删了,防止前端在用
                .withClaim("userinfo", map)
                //.withNotBefore(new Date())  //生效时间
                //.withJWTId(UUID.randomUUID().toString())    //编号
                .sign(algorithm);

        return token;
    }

    public static boolean verify(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();
            DecodedJWT jwt = verifier.verify(token);
            System.out.println("认证通过：");
            //System.out.println("issuer: " + jwt.getIssuer());
            System.out.println("username: " + jwt.getClaim("username").asString());
            System.out.println("过期时间： " + jwt.getExpiresAt());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getUserName(String token){
        try{
            JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("username").asString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取存储的用户信息(后期可能得对JWT中存储的信息进行加密)
     * @param token
     * @return
     */
    public static Map<String, Object> getUserInfo(String token){
        try{
            JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("userinfo").asMap();
        } catch (Exception e) {
            return null;
        }
    }
}
