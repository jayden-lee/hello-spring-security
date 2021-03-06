# Spring Security Study Repo
> 인프런 스프링 시큐리티 강좌를 학습하고 정리한 내용입니다 

## Prerequisites
- Installing MySQL 5.7

## Account Info
- Normal User : user / 123
- Admin User : admin / !@#

## Password Encoder
비밀번호는 평문이 아닌 단방향 알고리즘으로 인코딩해서 저장해야 한다
- {id}encodePassword
```java
PasswordEncoder passwordEncoder = 
        PasswordEncoderFactories.createDelegatingPasswordEncoder();
```

### Password Encoder 종류
- BCryptPasswordEncoder
- NoOpPasswordEncoder
- Pbkdf2PasswordEncoder
- ScryptPasswordEncoder
- StandardPasswordEncoder

## Spring Web Mock Mvc Test
<code>@AutoConfigureMockMvc</code> 를 사용하면 MockMvc 테스트를 진행할 수 있다
```java
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SpringBootTest {

}
```

### Anonymous, User, Admin Test

#### Anonymous
```java
@Test
@WithAnonymousUser
public void index_anonymous() throws Exception {
    mockMvc.perform(get(INDEX_PAGE))
        .andDo(print())
        .andExpect(status().isOk());
}
```

#### User
```java
@Retention(RetentionPolicy.RUNTIME)
@WithMockUser(username = "user", roles="USER")
public @interface WithNormalUser {
}

@Test
@WithNormalUser
public void index_user() throws Exception {
    mockMvc.perform(get(INDEX_PAGE))
        .andDo(print())
        .andExpect(status().isOk());
}
```

#### Admin
```java
@Retention(RetentionPolicy.RUNTIME)
@WithMockUser(username = "admin", roles="ADMIN")
public @interface WithAdminUser {
}

@Test
@WithAdminUser
public void admin_admin() throws Exception {
    mockMvc.perform(get(ADMIN_PAGE))
        .andDo(print())
        .andExpect(status().isOk());
}
```

## SecurityContextHolder와 Authentication
- SecurityContext 제공
- 하나의 Thread에서 Authentication 공유하기 위해서 ThreadLocal 사용
- <code>Authentication</code>는 <b>Principal</b>과 <b>GrantAuthority</b> 제공
    - Principal은 사용자에 대한 정보
    - GrantAuthority는 권한 정보 (인가 및 권한 확인할 때 사용)
    
```java
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

// 사용자 정보
Object principal = authentication.getPrincipal();

// 사용자 권한
Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

// 인증 여부
boolean authenticated = authentication.isAuthenticated();
```

> UserDetailsService 클래스는 DAO로 사용자 정보를 가져오는 작업을 수행한다. 실제 인증은 AuthenticationManager 인터페이스가
수행한다.

## AuthenticationManager와 Authentication
- 스프링 시큐리티에서 인증은 [AuthenticationManager](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/authentication/AuthenticationManager.html)가 수행
- SecurityContext는 인증 정보를 갖고 있음
- 대부분 AuthenticationManager 인터페이스를 구현한 <code>ProviderManager</code> 구현체 클래스를 사용한다 

```java
public interface AuthenticationManager {

	Authentication authenticate(Authentication authentication)
			throws AuthenticationException;
}
```

### DaoAuthenticationProvider
- UsernamePasswordAuthenticationToken은 DaoAuthenticationProvider가 인증하는 작업을 처리
- UserDetailsService 인터페이스를 구현한 클래스의 <code>loadUserByUsername</code> 메서드를 호출
- AccountService 클래스의 loadUserByUsername 메서드는 <b>User 객체를 반환</b>
- User 클래스는 UserDetails 인터페이스를 구현한 구체 클래스

![DaoAuthenticationProvider](https://user-images.githubusercontent.com/43853352/63638601-7aa4fa00-c6c5-11e9-84f4-aeed7b5a2205.png)

```java
public class AccountService implements UserDetailsService {

    @Autowired
    AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            throw new UsernameNotFoundException(username);
        }

        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }
}
```

## ThreadLocal
- <code>java.lang</code> 패키지에서 제공하는 쓰레드 범위 변수
- 쓰레드 수준의 데이터 저장소
- 같은 쓰레드 내에서만 공유
- 같은 쓰레드라면 해당 데이터를 메서드의 매개변수로 넘겨줄 필요 없음

```java
public class AccountContext {

    private static final ThreadLocal<Account> ACCOUNT_THREAD_LOCAL
            = new ThreadLocal<>();

    public static void setAccount(Account account) {
        ACCOUNT_THREAD_LOCAL.set(account);
    }

    public static Account getAccount() {
        return ACCOUNT_THREAD_LOCAL.get();
    }

}
```

## SecurityContextHolder에 Authentication 정보를 제공하는 필터
1. UsernamePasswordAuthenticationFilter
    - <code>AuthenticationManager</code>를 이용해서 사용자가 입력한 로그인 정보(이름, 비밀번호)를 인증
    ![UsernamePasswordAuthenticationFilter](https://user-images.githubusercontent.com/43853352/63639189-7d0a5280-c6cb-11e9-9424-2faab365de40.png)
    - 인증에 성공하면 <code>successfulAuthentication</code> 메서드를 호출
    - SecurityContextHolder의 SecurityContext에 인증 정보를 저장
    ![AbstractAuthenticationProcessingFilter](https://user-images.githubusercontent.com/43853352/63639251-f609aa00-c6cb-11e9-8e25-e1b8acd4adac.png)
    
2. SecurityContextPersistenceFilter
    - <code>HttpSessionSecurityContextRepository</code> 저장소를 통해 SecurityContext 정보를 가져온다
    - 기본 전략으로 Http 세션에 저장하고 복원한다
    - Repository에서 가져온 SecurityContext 정보를 다시 SecurityContextHolder에 넣어 준다
    
## 스프링 시큐리티 Filter와 FilterChainProxy
- FilterChainProxy는 요청(HttpServletRequest)에 따라 적합한 <code>SecurityFilterChain</code>을 사용
- 기본 전략으로 <code>DefaultSecurityFilterChain</code>을 사용
- <code>DefaultSecurityFilterChain</code>는 Filter 리스트를 가지고 있다
- SecurityFilterChain을 여러개 만들고 싶으면 SecurityConfig 클래스를 여러개 만든다
    - 이 때 SecurityConfig가 상충할 수 있으니 Order 어노테이션을 통해 우선순위를 지정한다
- Filter 개수는 SecurityConfig 설정에 따라 달라진다   
- FilterChainProxy는 필터를 호출하고 실행한다

1. WebAsyncManagerIntergrationFilter
2. <b>SecurityContextPersistenceFilter</b>
3. HeaderWriterFilter
4. CsrfFilter
5. LogoutFilter
6. <b>UsernamePasswordAuthenticationFilter</b>
7. DefaultLoginPageGeneratingFilter
8. DefaultLogoutPageGeneratingFilter
9. BasicAuthenticationFilter
10. RequestCacheAwareFilter
11. SecurityContextHolderAwareReqeustFilter
12. AnonymouseAuthenticationFilter
13. SessionManagementFilter
14. ExeptionTranslationFilter
15. FilterSecurityInterceptor

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .mvcMatchers("/", "/info").permitAll()
            .mvcMatchers("/admin").hasRole("ADMIN")
            .anyRequest().authenticated();
        http.formLogin();
        http.httpBasic();
    }

}
```

## DelegatingFilterProxy
- 일반적인 서블릿 필터
- 서블릿 필터 처리를 스프링에 들어있는 빈으로 위임하고 싶을 때 사용하는 서블릿 필터
- 타겟 빈 이름을 설정
- 스프링 부트(자동 설정) 없이 스프링 시큐리티 설정할 때는 <code>AbstractSecurityWebApplicationInitializer</code>를
사용해서 등록
- 스프링 부트를 사용할 때는 자동으로 등록 (<code>SecurityFilterAutoConfiguration</code>)
- <code>FilterChainProxy</code>는 <b>springSecurityFilterChain</b> 이름으로 빈 등록

```java
public abstract class AbstractSecurityWebApplicationInitializer
		implements WebApplicationInitializer {

	private static final String SERVLET_CONTEXT_PREFIX = "org.springframework.web.servlet.FrameworkServlet.CONTEXT.";

	public static final String DEFAULT_FILTER_NAME = "springSecurityFilterChain";

    ...
}
```

![SecurityFilterAutoConfiguration](https://user-images.githubusercontent.com/43853352/63649912-9c15ec80-c77e-11e9-82c7-70af9c04ba91.png)

## AccessDecisionManager
Access Control 결정을 내리는 인터페이스, 구현체 3가지를 기본으로 제공한다
- AffirmativeBased : 여러 Voter 중에 한 명이라도 허용하면 인가 (기본 전략) 
- ConsensusBased : 다수결
- UnanimousBased : 만장일치

```java
public interface AccessDecisionManager {

	void decide(Authentication authentication, Object object,
			Collection<ConfigAttribute> configAttributes) throws AccessDeniedException,
			InsufficientAuthenticationException;

	boolean supports(ConfigAttribute attribute);


	boolean supports(Class<?> clazz);
}
```

## AccessDecisionVoter
- Authentication이 특정한 Object에 접근할 때 필요한 ConfigAttribute를 만족하는지 확인
- WebExpressionVoter : 웹 시큐리티에서 사용하는 기본 구현체, ROLE_XXX 일치하는지 확인
- RoleHierarchyVoter : 계층형 Role 지원

## Custom AccessDecisionManager
- <code>RoleHierarchyImpl</code> 객체에 Role 계층을 설정

```java
public AccessDecisionManager accessDecisionManager() {
    RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
    roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");

    DefaultWebSecurityExpressionHandler handler = new DefaultWebSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);

    WebExpressionVoter webExpressionVoter = new WebExpressionVoter();
    webExpressionVoter.setExpressionHandler(handler);

    List<AccessDecisionVoter<? extends Object>> voters = Arrays.asList(webExpressionVoter);
    return new AffirmativeBased(voters);
}
```

## FilterSecurityInterceptor
- FilterChainProxy가 호출하는 시큐리티 필터 목록 중에 하나이며, 대부분 가장 마지막에 위치함
- 인증이 된 상태에서 특정 리소스에 접근할 수 있는지 Role을 확인함
- <code>AccessDecisionManager</code>를 사용해서 Access Control 또는 예외 처리하는 필터 

### AbstractSecurityInterceptor
- FilterSecurityInterceptor 클래스의 부모 클래스
 
![AbstractSecurityInterceptor](https://user-images.githubusercontent.com/43853352/63650312-404e6200-c784-11e9-9d42-cfd02d6e840e.png)

## ExceptionTranslationFilter
- 필터 체인에서 발생하는 <code>AccessDeniedException</code>과 <code>AuthenticationException</code>을 처리하는 필터

![ExceptionTranslationFilter](https://user-images.githubusercontent.com/43853352/63650539-8b697480-c786-11e9-86eb-c6d870bf83e4.png)

### AuthenticationException
- 인증에 실패할 때 발생하는 예외
- AbstractSecurityInterceptor 하위 클래스에서 발생하는 예외만 처리

### AccessDeniedException
- 익명 사용자라면 AuthenticationEntryPoint 실행 (로그인 페이지로 이동)
- 익명 사용자가 아니라면 AccessDeniedHandler에게 위임

## 스프링 시큐리티 적용 무시하기 (ignoring)
인증이 필요없는 페이지를 접속할 때 favicon.ico와 같은 정적 자원을 요청하는 경우에 FilterChainProxy 리스트의 필터를 타게 된다. 아래 이미지에서
favicon.ico를 요청하면 DefaultLoginPageGeneratingFilter 필터가 인증을 위해서 login 요청을 다시 하게된다.

![before_ignoring](https://user-images.githubusercontent.com/43853352/63650945-2fedb580-c78b-11e9-84b2-7421c344025e.png)

이러한 정적 자원을 필터에서 제외하기 위해서는 다음과 같이 WebSecurity에 <code>ignoring</code>을 설정해야 한다. CommonLocations은 5개의 자원에
대해 필터를 무시하도록 한다.

```java
@Override
public void configure(WebSecurity web) {
    web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
}
```

![StaticResourceLocation](https://user-images.githubusercontent.com/43853352/63651001-c7530880-c78b-11e9-8d89-dfe20b5ea380.png)

<code>WebSecurityConfigurerAdapter</code> 상속 받은 클래스에서 정적 자원을 무시하도록 설정하고, 다시 인증이 필요없는 페이지를
접속하게 되면 다음과 같이 스프링 필터를 적용하지 않고 바로 정적 자원을 전달한다.

![after_ignoring](https://user-images.githubusercontent.com/43853352/63650953-4b58c080-c78b-11e9-829e-ca42925c6add.png)

## WebAsyncManagerIntegrationFilter
스프링 MVC의 Async 기능을 사용할 때도 SecurityContext를 공유하도록 도와주는 필터

- PreProcess: SecurityContext를 설정한다.
- Callable: 비록 다른 쓰레드지만 그 안에서는 동일한 SecurityContext를 참조할 수 있다.
- PostProcess: SecurityContext를 정리(clean up)한다.

MVC 요청이 들어오는 쓰레드 작업을 완료하고 나서도 <code>SecurityContextHolder</code>에서는 사용자 정보를 동일하게 얻을 수 있다. 그 역할을
<b>WebAsyncManagerIntegrationFilter</b>가 수행한다.

```java
@Controller
public class SampleController {

    @GetMapping("/async-handler")
    @ResponseBody
    public Callable<String> asyncHandler() {
        // http-nio-8080-exec 쓰레드
        SecurityLogger.log("MVC");

        return () -> {
            // task-1 쓰레드
            SecurityLogger.log("Callable");
            return "Async Handler";
        };
    }
    
}
```

### SecurityContextCallableProcessingInterceptor
WebAsyncManagerIntegrationFilter는 <code>SecurityContextCallableProcessingInterceptor</code>를 사용해서 SecurityContextHolder에
SecurityContext 정보를 저장한다.

![SecurityContextCallableProcessingInterceptor](https://user-images.githubusercontent.com/43853352/63701300-a8be4180-c85f-11e9-90a7-d086333d13eb.png)

## @Async 서비스에서 SecurityContextHolder 공유
- SecurityContextHolder 기본 전략은 <code>ThreadLocal</code>
- @Async 서비스에서 SecurityContextHolder가 공유 되지 않는 문제가 발생함
- SecurityContextHolder 전략을 다음 코드와 같이 바꾸면 쓰레드 계층 사이에서도 SecurityContextHolder 정보가
공유된다

```java
SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
```

## SecurityContextPersistenceFilter
<code>SecurityContextRepository</code>를 사용해서 기존의 SecurityContext 정보를 읽어오거나 초기화한다
- 기본으로 사용하는 전략은 <b>HTTP Session</b> 사용 (HttpSessionSecurityContextRepository)
- Spring-Session과 연동하여 세션 클러스터를 구현할 수 있다

## HeaderWriterFilter
응답 헤더에 시큐리티 관련 헤더를 추가해주는 필터
- XContentTypeOptionsHeaderWriter : 마임 타입 스니핑 방어.
- XXssProtectionHeaderWriter : 브라우저에 내장된 XSS 필터 적용.
- CacheControlHeadersWriter : 캐시 히스토리 취약점 방어.
- HstsHeaderWriter : HTTPS로만 소통하도록 강제.
- XFrameOptionsHeaderWriter : clickjacking 방어.

![response-headers](https://user-images.githubusercontent.com/43853352/63869293-d687bf80-c9f2-11e9-9eb7-f98f0eb341dc.png)

## CsrfFilter
CSRF 어택 방지 필터
- 인증된 유저의 계정을 사용해서 악의적인 변경 요청을 만들어 보내는 기법
- 의도한 사용자만 리소스를 변경할 수 있도록 허용하는 필터
- CSRF 토큰을 사용하여 체크

### CsrfFilter Token
form 형식에 hidden 타입으로 csrf 토큰 값이 포함되어 있다

![csrf-token](https://user-images.githubusercontent.com/43853352/63873583-d5f32700-c9fa-11e9-9e60-065040ce5112.png)

Postman을 이용해서 <code>/signup</code> POST 요청을 보내면, <b>401 Unauthorized</b> 에러가 발생한다. 이유는 csrf 토큰 값이 없어서 폼 인증이 되지 않기
때문에 발생한다.

![csrf-401-code](https://user-images.githubusercontent.com/43853352/63873845-4c902480-c9fb-11e9-9055-11b725c73b24.png)

### CsrfFilter Test
```java
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SignUpControllerTest {

    @Autowired
    MockMvc mockMvc;

    // SignUp Get 요청
    @Test
    public void signUpForm() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("_csrf")));
    }

    // SignUp Post 요청, csrf 토큰을 포함 
    @Test
    public void processSignUp() throws Exception {
        mockMvc.perform(post("/signup")
                .param("username", "jayden")
                .param("password", "123")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }
}
```

### CsrfFilter 비활성화
```java
http.csrf().disable();
```

## LogoutFilter
여러 LogoutHanlder를 사용하여 로그아웃시 필요한 작업을 수행한다. 그리고 <code>LogoutSuccessHandler</code>를 사용해서
로그아웃 후처리를 한다.

### Default LogoutHanlder
- CsrfLogoutHandler
- SecurityContextLogoutHandler

### Default LogoutSuccessHandler
- SimpleUrlLogoutSuccessHandler

## UsernamePasswordAuthenticationFilter
폼 로그인을 처리하는 인증 필터

- 사용자가 폼에 입력한 정보를 토대로 Authentication 객체를 생성하고 <code>AuthenticationManager</code>를 사용하여 인증을 시도한다
- AuthenticationManager(ProviderManager)는 여러 <code>AuthenticationProvider</code>를 사용하여 인증을 시도하는데, 그 중 <code>DaoAuthenticationProvider</code>는
UserDetailsService를 사용하여 UserDetails 정보를 가져와서 사용자가 입력한 정보와 동일한지 비교한다

## DefaultLoginPageGeneratingFilter
기본 로그인 페이지를 생성하는 필터

### 사용자 이름과 비밀번호 파라미터 이름 변경

```java
http.formLogin()
        .usernameParameter("app_username")
        .passwordParameter("app_password");
```

![DefaultLoginPageGeneratingFilter](https://user-images.githubusercontent.com/43853352/64032534-a15fa680-cb85-11e9-9187-6220150cb1f2.png)

### 커스텀 로그인 페이지
커스텀 로그인 페이지를 등록하면 FilterChainProxy에서 <code>DefaultLoginPageGeneratingFilter</code>와 <code>DefaultLogoutPageGeneratingFilter</code> 두 필터가 제외됨

```java
http.formLogin()
        .loginPage("/login");
```
 
## DefaultLogoutPageGeneratingFilter
기본 로그아웃 페이지를 생성하는 필터

## 로그인/로그아웃 폼 커스터마이징
로그인/로그아웃 폼 페이지를 커스터마이징 하기 위해서 <code>LogInOutController</code>를 생성한다. 이 컨트롤러는 Get 요청으로 로그인/로그아웃
페이지를 반환한다.

```java
@Controller
public class LogInOutController {

    @GetMapping("/login")
    public String loginForm() {
        return "/login";
    }

    @GetMapping("/logout")
    public String logoutForm() {
        return "/logout";
    }

}
```

<code>SpirngSecurity</code> 설정에서 로그인 폼 페이지 URL과 로그아웃 URL을 설정한다.

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
            .mvcMatchers("/", "/info", "/signup").permitAll()
            .mvcMatchers("/admin").hasRole("ADMIN")
            .mvcMatchers("/user").hasRole("USER")
            .anyRequest().authenticated()
            .accessDecisionManager(accessDecisionManager());


    http.httpBasic();

    http.formLogin()
            .loginPage("/login")
            .permitAll();

    http.logout()
            .logoutUrl("/logout")
            .logoutSuccessUrl("/");

    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
}
```

## BasicAuthenticationFilter
- Http Basic 인증을 지원하는 필터
- 요청 헤더에 아이디와 패스워드를 보내면 브라우저 또는 서버가 그 값을 읽어서 인증하는 방식
- 정보는 Base64 인코딩 되어 보내지고 읽을 때 다시 디코딩해서 값을 읽는다
- 스니핑하면 요청 정보를 쉽게 취득하는 위험이 있기 때문에 HTTPS를 사용할 것을 권장

```java
http.httpBasic();
```

## RequestCacheAwareFilter
현재 요청과 관련 있는 캐시된 요청이 있는지 찾아서 적용하는 필터
- 캐시된 요청이 없다면, 현재 요청 처리
- 캐시된 요청이 있다면, 캐시된 요청 처리

> 대시보드(로그인이 필요한 페이지) 페이지를 접속하려고 하면 로그인 페이지로 이동한다. 로그인 페이지에서 로그인 인증을 수행하고 나면,
<code>RequestCacheAwareFilter</code>에서 캐시한 요청(대시보드 페이지로 이동하려는 요청)을 수행한다.  

```java
public class RequestCacheAwareFilter extends GenericFilterBean {

	private RequestCache requestCache;

	public RequestCacheAwareFilter() {
		this(new HttpSessionRequestCache());
	}

	public RequestCacheAwareFilter(RequestCache requestCache) {
		Assert.notNull(requestCache, "requestCache cannot be null");
		this.requestCache = requestCache;
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest wrappedSavedRequest = requestCache.getMatchingRequest(
				(HttpServletRequest) request, (HttpServletResponse) response);

        // 캐시된 요청이 있는지 체크하고 현재 요청을 처리할지 캐시된 요청을 처리할지 결정
		chain.doFilter(wrappedSavedRequest == null ? request : wrappedSavedRequest,
				response);
	}

}
```

## SecurityContextHolderAwareRequestFilter
시큐리티 관련 서블릿 API를 구현해주는 필터

- HttpServletRequest#authenticate(HttpServletResponse)
- HttpServletRequest#login(String, String)
- HttpServletRequest#logout()
- AsyncContext#start(Runnable)

## AnonymousAuthenticationFilter
SecurityContext에 Authentication이 null 값이면, 익명 Authentication을 생성해서 넣어준다. Authentication이 null 값이 아니면,
아무일도 하지 않는 필터이다. ([null object pattern](https://www.geeksforgeeks.org/null-object-design-pattern/))

![AnonymousAuthenticationFilter_dofilter](https://user-images.githubusercontent.com/43853352/64066511-be63aa80-cc55-11e9-8350-318e4890474f.png)

스프링 시큐리티는 별도의 설정이 없어도 AnonymousUser를 기본적으로 생성한다. Principal은 <b>anonymousUser</b>이고 권한은 <b>ROLE_ANONYMOUS</b>로 설정한다.

![AnonymousAuthenticationFilter](https://user-images.githubusercontent.com/43853352/64066496-7775b500-cc55-11e9-9efa-333157e58e7b.png)

## SessionManagementFilter
- 세션 변조 방지 전략 설정
    - 세션 변조 방지 전략으로 <b>changeSessionId</b>로 설정
    ```java
    http.sessionManagement()
            .sessionFixation()
            .changeSessionId();
    ```
- 유효하지 않은 세션을 리다이렉트 시킬 URL 설정
- 동시성 제어
    - 세션 개수 제어
    - 추가 로그인을 막을지 여부 (기본값은 false)
    ```java
    http.sessionManagement()
            .maximumSessions(1)
            .maxSessionsPreventsLogin(true);
    ```
- 세션 생성 전략
    1. ALWAYS
    2. NEVER
    3. IF_REQUIRED
    4. STATELESS
    
## ExceptionTranslationFilter
- <code>try-catch</code> 구문으로 감싸고 <code>FilterSecurityInterceptor</code>를 처리한다
- FilterSecurityInterceptor는 <code>AccessDecisionManager</code>를 이용해서 인가 처리를 함
- AuthenticationEntryPoint, AccessDeniedException 예외를 처리함

## FilterSecurityInterceptor
- Http 리소스 시큐리티 처리를 담당하는 필터
- <code>AccessDecisionManager</code>를 사용하여 인가를 처리

```java
http.authorizeRequests()
        .mvcMatchers("/", "/info", "/signup").permitAll()
        .mvcMatchers("/admin").hasRole("ADMIN")
        .mvcMatchers("/user").hasRole("USER")
        .anyRequest().authenticated()
        .accessDecisionManager(accessDecisionManager());
```

![security-filter-list](https://user-images.githubusercontent.com/43853352/64179431-1f7dbf00-ce9e-11e9-8a1d-a8265300ab56.png)

## RememberMeAuthenticationFilter
- 세션이 사라지거나 만료가 되더라도 쿠키 또는 DB를 사용하여 저장된 토큰 기반으로 인증을 지원하는 필터

### RememberMe 설정
페이지에 접속하면 서버에서 세션이 생성되고 웹 브라우저 쿠키에 세션 아이디 정보가 담긴다. 로그인 하고 나면 서버는 해당 세션을 인증된 세션으로 취급한다.

사용자가 웹 브라우저 쿠키에서 세션 아이디를 삭제하게 되면, 인증된 세션이 아니기 때문에 서버는 다시 로그인 창으로 리다이렉트 된다.

세션 아이디를 삭제하면 <code>SecurityContextHolder</code>에서 인증 정보를 가져올 수 없기 때문에 서버는 인증되지 않은 사용자로 판단하고 인증이 필요한 페이지의 접속을 막는다.

![session-id](https://user-images.githubusercontent.com/43853352/64179984-fe699e00-ce9e-11e9-9545-ab9d357ff7bf.png)

다음과 같이 <code>rememberMe</code> 설정을 하고 로그인할 때, remember-me 파라미터를 넘기면 remember-me 쿠키 정보가 생기게 된다.
remember-me 쿠키에는 사용자 이름과 유효 기간 정보를 포함하고 있다.

```java
http.rememberMe()
        .userDetailsService(accountService)
        .key("remember-me");
```

![remember-me](https://user-images.githubusercontent.com/43853352/64180534-e8a8a880-ce9f-11e9-81cd-def073030818.png)

앞에서 한 것처럼 다시 세션 아이디를 삭제하고 나서 다시 인증이 필요한 페이지를 요청하면 로그인 페이지로 리다이렉트 하지 않는다. 필터 체인 목록에서
<code>RememberMeAuthenticationFilter</code>가 <b>RememberMeAuthenticationToken</b> 정보를 이용해서 인증하고, 인증된 정보를 다시
<code>SecurityContextHolder</code>에 넣어준다.

![RememberMeAuthenticationFilter](https://user-images.githubusercontent.com/43853352/64181175-fb6fad00-cea0-11e9-8319-24823498f3c1.png)

> 크롬 웹 브라우저에서 현재 접속한 페이지의 쿠키 정보를 쉽게 확인할 수 있는 플러그인으로 [EditThisCookie](https://chrome.google.com/webstore/detail/editthiscookie/fngmhnnpilhplaeedifhccceomclgfbg?hl=ko)를
설치해서 사용했다.

## 커스텀 필터 추가하기
Filter를 생성하는 것은 여러 방법이 있지만 이번에 추가하는 LoggingFilter는 <code>GenericFilterBean</code> 클래스를 상속 받아서 구현하도록 한다. <code>GenericFilterBean</code> 클래스에는 기본적인 설정이 되어 있기 때문에
상속 받은 클래스가 <b>doFilter</b> 메서드만 오버라이드 하면 된다.

```java
public class LoggingFilter extends GenericFilterBean {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        chain.doFilter(request, response);

        stopWatch.stop();
        logger.info(stopWatch.prettyPrint());
    }
}
```

새로 생성한 <code>LoggingFilter</code> 필터를 필터 목록에서 원하는 위치로 설정할 수 있다. <code>WebAsyncManagerIntegrationFilter</code> 필터는 필터 목록에서 가장 첫 번째에 위치하는 필터이다.

```java
http.addFilterBefore(new LoggingFilter(), WebAsyncManagerIntegrationFilter.class);
```

![loggingfilter](https://user-images.githubusercontent.com/43853352/64186008-134b2f00-cea9-11e9-84af-e423886d04e9.png)

## 메서드 시큐리티
- 스프링 시큐리티 기능을 웹 또는 데스크탑 애플리케이션에서도 사용할 수 있도록 도와주는 기능
- 메서드 시큐리티를 사용하기 위해서는 다음과 같은 설정 클래스를 생성해야 한다

```java
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, jsr250Enabled = true)
public class MethodSecurity {
    
}
```

- 권한에 따라 특정 메서드를 실행 유무를 설정하고 싶으면 <code>@Secured</code> 애노테이션과 함께 <b>ROLE_USER</b> 이름을 추가한다 
- <code>@Secured</code>, <code>@RolesAllowed</code>, <code>PreAuthorize</code> 애노테이션들은 dashboard 메서드를 호출하기 전에 권한 검사를 수행한다
- <codE>@PostAuthorize</code> 애노테이션은 dashboard 메서드를 실행한 이후에 권한을 체크한다
 
```java
@Secured("ROLE_USER")
@RolesAllowed("ROLE_USER")
@PreAuthorize("hasRole(USER)")
public void dashboard() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    System.out.println("================");
    System.out.println(authentication);
    System.out.println(authentication.getName());
}
```

## @AuthenticationPrincipal
> 기존에 Principal 정보를 얻으려면 다음과 같은 두 가지 방법을 사용했다. 첫번째 방법을 통해 얻은 Principal 객체에서는
사용자 이름 정보만 가져올 수 있는 단점이 있다. 두 번째 방법은 우리가 선언한 도메인 타입의 클래스로 변환하면 이름, 역할, 비밀번호 정보를
얻을 수 있다.

1. Argument에 Principal를 추가
    ```java
    @GetMapping(value = "/dashboard")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("message", "Hello " + principal.getName());
        AccountContext.setAccount(accountRepository.findByUsername(principal.getName()));
        sampleService.dashboard();
        return "dashboard";
    }
    ```

2. SecurityContextHolder에서 가져오는 방법
    ```java
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    ```

이번에 살펴볼 <code>@AuthenticationPrincipal</code> 애노테이션을 사용하면 우리가 선언한 도메인 타입의 Principal 정보를
매개변수로 받을 수 있다. 애노테이션을 확인하고 <code>ArgumentResolver</code>가 현재 로그인한 사용자 정보를 만들어서 넣어준다.

```java
@GetMapping(value = "/")
public String index(Model model, @AuthenticationPrincipal UserAccount userAccount) {
    if (userAccount == null) {
        model.addAttribute("message", "Hello Spring Security");
    } else {
        model.addAttribute("message", "Hello " + userAccount.getUsername());
    }
    return "index";
}
```

UserAccount 클래스는 User 클래스를 상속 받아 구현한다.
```java
@Getter
public class UserAccount extends User {

    private Account account;

    public UserAccount(Account account) {
        super(account.getUsername(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole())));
        this.account = account;
    }

}
```

UserAccount 객체에서 Account 정보만 가져오고 싶을 때는 다음과 같은 방법을 사용한다

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account")
public @interface CurrentUser {
}
```

```java
@GetMapping(value = "/")
public String index(Model model, @CurrentUser Account account) {
    if (account == null) {
        model.addAttribute("message", "Hello Spring Security");
    } else {
        model.addAttribute("message", "Hello " + account.getUsername());
    }
    return "index";
}
```

