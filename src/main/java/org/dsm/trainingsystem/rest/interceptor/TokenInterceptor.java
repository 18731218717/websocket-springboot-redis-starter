
package org.dsm.trainingsystem.rest.interceptor;

import org.dsm.trainingsystem.rest.redis.RedisTools;
import org.dsm.trainingsystem.util.JWTHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Resource
    RedisTools redisTools;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String token = request.getHeader("X-Token");
        //System.out.println(request.getRequestURI());

        if (StringUtils.isEmpty(token)){
            //返回401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        //认证token
        if (!JWTHelper.verify(token)){
            response.setStatus( HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        //取用户名
        String userName = JWTHelper.getUserName(token);
        System.out.println(userName);
        if (StringUtils.isEmpty(userName)){
            response.setStatus( HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        //认证Redis中token是否有效(目前Redis中的token存储时间与JWT中的过期时间是一致的,策略也一样,只要过期了就得重新登录系统)
        Object redisToken=redisTools.get(userName+"_token");
        if (!token.equals(redisToken)){
            response.setStatus( HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        //取用户角色
        Map<String, List<String>> userInterfaceListMap = (Map<String, List<String>>) redisTools.get("userInterfaceList");
        if(userInterfaceListMap!=null){
            if (userInterfaceListMap.containsKey(userName)){
                List<String> interfaceList = userInterfaceListMap.get(userName);
                if (interfaceList.contains(request.getRequestURI())){
                    return true;
                }else{
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return false;
                }
            }
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
