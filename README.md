# Spring Security Study Repo
> 인프런 스프링 시큐리티 강좌를 학습하고 정리한 내용입니다 

## Account Info
- Normal User : user / 123
- Admin User : admin / !@#


## Password Encoder
비밀번호는 평문이 아닌 단방향 알고리즘으로 인코딩해서 저장해야 한다.
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