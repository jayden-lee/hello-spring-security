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