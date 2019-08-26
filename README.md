# Spring Security Study Repo
> 인프런 스프링 시큐리티 강좌를 학습하고 정리한 내용입니다 

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
