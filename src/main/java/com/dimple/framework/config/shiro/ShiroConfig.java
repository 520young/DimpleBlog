package com.dimple.framework.config.shiro;

import com.dimple.framework.config.shiro.realm.UserRealm;
import com.dimple.framework.filter.LogoutFilter;
import com.dimple.framework.listener.ShiroSessionListener;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @ClassName: ShiroConfig
 * @Description: Shiro配置信息
 * @Auther: Owenb
 * @Date: 11/19/18 19:21
 * @Version: 1.0
 */
@Configuration
public class ShiroConfig {


    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private String port;

    //缓存用户信息的时长
    @Value("${dimple.shiro.redis.expire}")
    private int redisExpire;

    //登录地址
    @Value("${dimple.shiro.user.loginUrl}")
    private String loginUrl;

    //未授权地址
    @Value("${dimple.shiro.user.unauthUrl}")
    private String unauthUrl;

    @Value(("${dimple.shiro.user.indexUrl}"))
    private String indexUrl;

    //RememberCookie的域名
    @Value("${dimple.shiro.cookie.rememberMeCookie.domain}")
    private String rememberMeCookieDomain;

    //rememberCookie的访问路径
    @Value("${dimple.shiro.cookie.rememberMeCookie.path}")
    private String rememberMeCookiePath;

    //RememberCache只允许Http访问
    @Value("${dimple.shiro.cookie.rememberMeCookie.httpOnly}")
    private boolean rememberMeCookieHttpOnly;

    //RememberCookie 过期时间
    @Value("${dimple.shiro.cookie.rememberMeCookie.maxAge}")
    private int rememberMeCookieMaxAge;

    //SessionCookie的域名
    @Value("${dimple.shiro.cookie.sessionCookie.domain}")
    private String sessionCookieDomain;

    //SessionCookie的访问路径
    @Value("${dimple.shiro.cookie.sessionCookie.path}")
    private String sessionCookiePath;

    //SessionCache只允许Http访问
    @Value("${dimple.shiro.cookie.sessionCookie.httpOnly}")
    private boolean sessionCookieHttpOnly;

    //SessionCookie 过期时间
    @Value("${dimple.shiro.cookie.sessionCookie.maxAge}")
    private int sessionCookieMaxAge;

    //Session的检查时间
    @Value("${dimple.shiro.session.validationInterval}")
    private int validationInterval;

    //Session的超时时间
    @Value("${dimple.shiro.session.expireTime}")
    private int sessionExpireTime;


    @Bean
    public RedisManager redisManager() {
        RedisManager redisManager = new RedisManager();
        redisManager.setHost(host);

        redisManager.setPort(Integer.valueOf(port));
        return redisManager;
    }

    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(@Qualifier("securityManager") SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        //设置Shiro核心接口SecurityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        shiroFilterFactoryBean.setLoginUrl(loginUrl);
        shiroFilterFactoryBean.setSuccessUrl(indexUrl);
        shiroFilterFactoryBean.setUnauthorizedUrl(unauthUrl);

        //拦截器
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        //静态资源不拦截
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/fonts/**", "anon");
        filterChainDefinitionMap.put("/img/**", "anon");
        filterChainDefinitionMap.put("/imgs/**", "anon");
        filterChainDefinitionMap.put("/images/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/front/**", "anon");


        filterChainDefinitionMap.put("/html/**", "anon");
        //加入plugin下面的不拦截
        filterChainDefinitionMap.put("/plugin/**", "anon");

        //前端界面不拦截
        filterChainDefinitionMap.put("/", "anon");
        filterChainDefinitionMap.put("/front/**", "anon");
        filterChainDefinitionMap.put("/about.html", "anon");
        filterChainDefinitionMap.put("/link.html", "anon");
        filterChainDefinitionMap.put("/archive.html", "anon");
        filterChainDefinitionMap.put("/technology.html", "anon");
        filterChainDefinitionMap.put("/technology/**", "anon");
        filterChainDefinitionMap.put("/leaveComment.html", "anon");
        filterChainDefinitionMap.put("/category/**", "anon");
        filterChainDefinitionMap.put("/static/**", "anon");
        //前台调用API不拦截
        filterChainDefinitionMap.put("/public/api/**", "anon");
        filterChainDefinitionMap.put("/view/**", "anon");

        //退出
        filterChainDefinitionMap.put("/logout", "logout");
        //授权验证不拦截
        filterChainDefinitionMap.put("/auth", "anon");
        //其他资源都需要认证，authc表示需要认证才能进行访问，user表示配置记住我或者认证通过就能访问
        filterChainDefinitionMap.put("/**", "user");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        Map<String, Filter> filters = new LinkedHashMap<>();

        // 注销成功，则跳转到指定页面
        filters.put("logout", logoutFilter());
        shiroFilterFactoryBean.setFilters(filters);

        return shiroFilterFactoryBean;
    }

    /**
     * 安全管理器
     *
     * @return
     */
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(userRealm());
        securityManager.setRememberMeManager(rememberMeManager());
        securityManager.setCacheManager(redisCacheManager());
        //配置自定义Session，使用Redis
        securityManager.setSessionManager(sessionManager());
        return securityManager;
    }

    /**
     * Shiro 生命周期处理器
     * 设置为static解决属性不能注入的问题
     *
     * @return
     */
    @Bean
    public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * 自定义的Realm
     *
     * @return
     */
    @Bean
    public UserRealm userRealm() {
        UserRealm userRealm = new UserRealm();
        //设置密码匹配器
        userRealm.setCredentialsMatcher(getHashedCredentialsMatcher());
        //设置身份验证缓存，缓存AuthenticationInfo的信息，默认为false
        userRealm.setCachingEnabled(true);
        //缓存AuthenticationInfo的缓存名称，在Ehcache的配置文件中有对应的缓存的配置
        userRealm.setAuthenticationCacheName("authenticationCache");
        //设置授权缓存，即缓存AuthorizationInfo的信息，默认为false
        userRealm.setAuthorizationCachingEnabled(true);
        //缓存AuthorizationInfo的信息
        userRealm.setAuthorizationCacheName("authorizationCache");
        return userRealm;
    }

    /**
     * 记住我Cookie
     *
     * @return
     */
    @Bean
    public SimpleCookie rememberMeCookie() {
        //Cookie的名称，对应前端的CheckBox的name=rememberMe
        SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
        //设置为true后只能通过Http访问，JavaScript无法访问
        simpleCookie.setHttpOnly(rememberMeCookieHttpOnly);
        simpleCookie.setPath(rememberMeCookiePath);
        //设置cookie生效时间(单位天)，setMaxAge的单位为秒
        simpleCookie.setMaxAge(rememberMeCookieMaxAge * 24 * 60 * 60);
        return simpleCookie;
    }

    /**
     * Cookie管理对象，记住我功能，RememberMe管理器
     *
     * @return
     */
    @Bean
    public CookieRememberMeManager rememberMeManager() {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMeCookie());
        cookieRememberMeManager.setCipherKey(Base64.decode("4AvVhmFLUs0KTA3Kprsdag=="));
        return cookieRememberMeManager;
    }

    /**
     * 过滤器，实现记住我
     *
     * @return
     */
    @Bean
    public FormAuthenticationFilter formAuthenticationFilter() {
        FormAuthenticationFilter formAuthenticationFilter = new FormAuthenticationFilter();
        //对应前端的CheckBox，name=rememberMe
        formAuthenticationFilter.setRememberMeParam("rememberMe");
        return formAuthenticationFilter;
    }


    @Bean
    public RedisCacheManager redisCacheManager() {
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(redisManager());
        redisCacheManager.setPrincipalIdFieldName("userId");
        //设置用户权限信息的缓存时间,单位分钟
        redisCacheManager.setExpire(redisExpire * 60 * 1000);
        return redisCacheManager;
    }

    /**
     * 让某个实例的某个方法的返回值注入为Bean的实例
     * Spring静态注入
     *
     * @return
     */
    @Bean
    public MethodInvokingFactoryBean getMethodInvokingFactoryBean() {
        MethodInvokingFactoryBean factoryBean = new MethodInvokingFactoryBean();
        factoryBean.setStaticMethod("org.apache.shiro.SecurityUtils.setSecurityManager");
        factoryBean.setArguments(new Object[]{securityManager()});
        return factoryBean;
    }


    /**
     * 设置凭证管理器
     *
     * @return
     */
    @Bean
    public HashedCredentialsMatcher getHashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName("md5");
        hashedCredentialsMatcher.setHashIterations(2);
        return hashedCredentialsMatcher;
    }


    /**
     * 开启Shiro AOP注解支持
     *
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor getAuthorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }


    /**
     * 异常后返回界面
     * 解决无权限页面不跳转的问题
     *
     * @return
     */
    @Bean
    //todo 解决下跳转问题
    public SimpleMappingExceptionResolver getSimpleMappingExceptionResolver() {
        SimpleMappingExceptionResolver simpleMappingExceptionResolver = new SimpleMappingExceptionResolver();
        Properties properties = new Properties();
        //数据库异常
        properties.setProperty("DatabaseException", "databaseError");
        properties.setProperty("UnauthorizedException", "/user/403");
        simpleMappingExceptionResolver.setExceptionMappings(properties);
        simpleMappingExceptionResolver.setDefaultErrorView("error");    // No default
        simpleMappingExceptionResolver.setExceptionAttribute("exception");     // Default is "exception"
        //r.setWarnLogCategory("example.MvcLogger");     // No default
        return simpleMappingExceptionResolver;
    }

    /**
     * 配置Session监听
     *
     * @return
     */
    @Bean("sessionListener")
    public ShiroSessionListener sessionListener() {
        ShiroSessionListener shiroSessionListener = new ShiroSessionListener();
        return shiroSessionListener;
    }


    /**
     * 配置SessionId generator
     *
     * @return
     */
    @Bean
    public SessionIdGenerator sessionIdGenerator() {
        return new JavaUuidSessionIdGenerator();
    }

    /**
     * SessionDAO的作用是为Session提供CRUD并进行持久化的一个shiro组件
     * MemorySessionDAO 直接在内存中进行会话维护
     * EnterpriseCacheSessionDAO  提供了缓存功能的会话维护，默认情况下使用MapCache实现，内部使用ConcurrentHashMap保存缓存的会话。
     *
     * @return
     */
    @Bean
    public SessionDAO sessionDAO() {
        RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
        redisSessionDAO.setRedisManager(redisManager());
        //设置session的超时时间
        redisSessionDAO.setExpire(sessionExpireTime * 60 * 1000);
        return redisSessionDAO;
    }

    /**
     * SessionId的cookie
     *
     * @return
     */
    @Bean("sessionIdCookie")
    public SimpleCookie sessionIdCookie() {
        //设置cookie的名称
        SimpleCookie simpleCookie = new SimpleCookie("sessionCookie");

        simpleCookie.setHttpOnly(sessionCookieHttpOnly);
        simpleCookie.setPath(sessionCookiePath);
       //设置session cookie的过期时间 单位min
        simpleCookie.setMaxAge(sessionCookieMaxAge * 60);
        return simpleCookie;
    }

    @Bean("sessionManager")
    public SessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        Collection<SessionListener> listeners = new ArrayList<SessionListener>();
        //配置监听
        listeners.add(sessionListener());
        sessionManager.setSessionListeners(listeners);
        sessionManager.setSessionIdCookie(sessionIdCookie());
        sessionManager.setSessionDAO(sessionDAO());
        sessionManager.setCacheManager(redisCacheManager());
        //删除过期的session
        sessionManager.setDeleteInvalidSessions(true);
        //设置全局超时时间，单位min
        sessionManager.setGlobalSessionTimeout(sessionExpireTime * 60 * 1000);
        //去掉URL后缀的Session
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        //设置Session检查时间，单位min
        sessionManager.setSessionValidationInterval(validationInterval * 60 * 1000);
        return sessionManager;
    }

    public LogoutFilter logoutFilter() {
        LogoutFilter logoutFilter = new LogoutFilter();
        logoutFilter.setLoginUrl("/");
        return logoutFilter;
    }

}
