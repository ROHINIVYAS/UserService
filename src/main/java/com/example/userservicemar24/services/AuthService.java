package com.example.userservicemar24.services;

import com.example.userservicemar24.dtos.UserDto;
import com.example.userservicemar24.models.Role;
import com.example.userservicemar24.models.Session;
import com.example.userservicemar24.models.SessionStatus;
import com.example.userservicemar24.models.User;
import com.example.userservicemar24.repository.SessionRepository;
import com.example.userservicemar24.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.web.server.Ssl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;

import org.springframework.http.HttpHeaders;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import static java.net.http.HttpHeaders.*;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

@Service
public class AuthService {
    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private SessionRepository sessionRepository;
    private static final String SECRET_KEY = "your_secret_key_here"; // Replace with your actual secret key
    SecretKey keyValue;
    public AuthService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.sessionRepository = sessionRepository;
    }
    public UserDto signup(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        User savedUser = userRepository.save(user);
        return UserDto.from(savedUser);
    }

    public ResponseEntity<UserDto> login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isEmpty()){
            return null;
        }
        User user = userOptional.get();
        if(!bCryptPasswordEncoder.matches(password, userOptional.get().getPassword())){
            throw  new RuntimeException("Password is incorrect");
        }
        //session generation
        String token = RandomStringUtils.randomAlphanumeric(30);

        MacAlgorithm algorithm = Jwts.SIG.HS256;
        //SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

        SecretKey key = algorithm.key().build();
        keyValue=key;


        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles());
        claims.put("createdAt", new Date());
        claims.put("expiryAt", new Date(LocalDate.now().plusDays(3).toEpochDay()));

        token = Jwts.builder()
                .claims(claims)
                .signWith(key, algorithm)
                .compact();

        Session session = new Session();
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setUser(user);
        session.setToken(token);
        sessionRepository.save(session);

        UserDto userDto = UserDto.from(user);
        MultiValueMapAdapter<String, String> headers = new MultiValueMapAdapter<>(new HashMap<>());
        headers.add(HttpHeaders.SET_COOKIE, "auth-token:" + token);

        ResponseEntity<UserDto> response = new ResponseEntity<>(userDto,headers, HttpStatus.OK);
        return response;

    }

    public ResponseEntity<Void> logout(String token, Long userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_id(token,userId);
        if(sessionOptional.isEmpty()){
            return null;
        }
        Session session = sessionOptional.get();
        session.setSessionStatus(SessionStatus.ENDED);
        sessionRepository.save(session);
        return ResponseEntity.ok().build();
    }

    public SessionStatus validate(String token, Long userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_id(token,userId);
        if(sessionOptional.isEmpty()){
            return SessionStatus.ENDED;
        }
        Session session = sessionOptional.get();
        if(!session.getSessionStatus().equals(SessionStatus.ACTIVE)){
            return SessionStatus.ENDED;
        }
        //SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Jws<Claims> claimsJws = Jwts.parser().build().parseSignedClaims(token);
        /*Jws<Claims> claimsJws = Jwts.parser()
               .setSigningKey(keyValue) // Replace with your actual signing key
               .build().parseSignedClaims(token);*/
        String email = claimsJws.getPayload().get("email", String.class);
        List<Role> roles = claimsJws.getPayload().get("roles", List.class);
        Date createdAt = claimsJws.getPayload().get("createdAt", Date.class);

        if(createdAt.before(new Date())){
            return SessionStatus.ENDED;
        }
        return SessionStatus.ACTIVE;
    }
}
