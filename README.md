# 使用netty实现的spring基本功能
## 说明
`tiny-spring`项目主要实现了一个简单的web服务器，提供了基本的登录登出功能（session方式），同时定义了基本的拦截器、控制器接口，除此之外，本项目为了尽可能简单，免除了数据库的操作（如有需要可以自己定义实现数据库操作相应接口），而是使用配置文件形式保存基本信息，为了配置能够及时生效，还配有相应监听器，实现相应监听器，可以及时将配置文件更新到必要的地方。
## 新版本预期优化（v1.0.1）
- [] 拦截器接口优化，将原手动注入拦截器方式修改为自动寻找并注入
- [] @ComponentScan注解优化，使用自动寻找注解注入方式替代v1.0.0版本中手动注入方式
- [] 登入登出功能考虑做成配置化，可以手动选择是否开启默认登入登出
- [] 接口请求返回给调用者接口优化，自动实现接口的包装，而不是手动拼接
## 2019/08/31更新（v1.0.0）
### 目前支持的功能
* [WebSocket请求访问](#websocket请求访问)
* [基本注解，@GetMapping、@PostMapping、@Controller、@ComponentScan](#基本注解)
* [拦截器：请求拦截，返回结果拦截](#拦截器)
* [回调方法：WebSocket支持连接建立成功的回调](#websocket回调)
* [Session支持](session支持)
* [登录登出基本默认实现](#登录登出默认实现)
#### WebSocket请求访问
通过在配置文件中配置`config.websocket.uri`属性的值（该属性默认值为`/ws`），当框架针对请求的`uri`进行判断时，如果请求的`url`为配置文件中配置的值，
那么将会将该请求为`WebSocket`请求，交由相关的解析器去进行解析。
#### 基本注解
* @ComponentScan
配置相关解析路由包位置，实现扫描指定路径下类中的注解，来保持相关路由数据到缓存中。（使用该注解时需要注意该注解必须写在包含`main`方法的类上，
同时需要在`main`方法中显示写出`WebApplicationStarter.start()`；这种不优雅问题将在后续优化掉）
* @Controller
作为控制层路由，表示使用该注解的类是一个控制类，与`Spring`中的`@RestController相似`
* @GetMapping
用在解析`get`请求的方法上，与`Spring`中的`@GetMapping`类似
* @PostMapping
用在解析`post`请求的方法上，与`Spring`中的`@PostMapping`类似
#### 拦截器
实现模块中`IRequestMiddleware`接口，同时将实现的方法注入拦截管理器`MiddlewareManager`中即可，具体使用方式如下：
```java
//BaseMiddleware实现类
public class BaseMiddleware implements IRequestMiddleware{
    @Override
    public void preRequest(FullHttpRequest request) throws Exception {
    
    }
    
    @Override
    public void afterRequest(FullHttpRequest request) throws Exception {

    }
}
```
```java
//在初始化容器中增加拦截器
MiddlewareManager.addMiddleware(new BaseMiddleware());
```
#### WebSocket回调
框架提供了`IWebSocketCompleteCallback`接口，需要在项目初始化时手动将该接口的实现类注入框架中，这样在`WebSocket`
建立连接之后将会回调该接口的实现方法
#### Session支持
提供`session`支持，可以使用`SessionManager`进行`session`的管理工作，包括`session`的添加，删除，更新，验证
过期等等
#### 登录登出默认实现
默认请求`uri`路径下的`/login`和`/logout`时会直接调用框架自带的登录登出接口，如需重写需要继承框架中`BaseController`类