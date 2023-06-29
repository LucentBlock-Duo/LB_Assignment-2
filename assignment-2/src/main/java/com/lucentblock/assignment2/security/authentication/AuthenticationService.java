package com.lucentblock.assignment2.security.authentication;

import com.lucentblock.assignment2.entity.Role;
import com.lucentblock.assignment2.entity.User;
import com.lucentblock.assignment2.repository.UserRepository;
import com.lucentblock.assignment2.security.authentication.jwt.JwtRefreshService;
import com.lucentblock.assignment2.security.authentication.jwt.JwtService;
import com.lucentblock.assignment2.security.PrincipalDetails;
import com.lucentblock.assignment2.security.model.RegisterRequest;
import com.lucentblock.assignment2.security.model.AuthenticationRequest;
import com.lucentblock.assignment2.security.model.AuthenticationResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    // OAuth 가 아닌 이 서비스에 직접 가입한 사람의 경우 이 컨트롤러가 작동한다.

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final JwtRefreshService jwtRefreshService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

//    @PostConstruct
//    public void adminSetup() {
//        User adminUser = User.builder()
//                .name("moil")
//                .email("admin@gmail.com")
//                .password(passwordEncoder.encode("admin"))
//                .phoneNumber("01012345678")
//                .role(Role.ROLE_ADMIN)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        userRepository.save(adminUser);
//    }

    public AuthenticationResponse register(RegisterRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(Role.ROLE_USER)
                .createdAt(LocalDateTime.now())
                .build();


        String accessToken = jwtService.generateToken(
                Map.of("role", Role.ROLE_USER.name()),
                new PrincipalDetails(user)
        );

        String refreshToken = jwtRefreshService.generateToken(
                Map.of("role", Role.ROLE_USER.name()),
                new PrincipalDetails(user)
        );

        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        userRepository.flush();

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())); // UserDetails (DB에 있는 유저) 정보와 일치하는지 확인
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        String accessToken = jwtService.generateToken(
                Map.of("role", user.getRole().name()),
                new PrincipalDetails(user)
        );

        String refreshToken = jwtRefreshService.generateToken(
                Map.of("role", user.getRole().name()),
                new PrincipalDetails(user)
        );

        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        userRepository.flush();

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse refresh(String accessToken, String refreshToken) {
        Claims claimsOfExpiredAccessToken = jwtService.extractClaimsFromExpiredToken(accessToken);

        String userEmail = claimsOfExpiredAccessToken.getSubject();
        User retrievedUser = userRepository.findByEmail(userEmail).orElseThrow();

        if (refreshToken.equals(retrievedUser.getRefreshToken())) {

            if (!jwtRefreshService.isTokenExpired(refreshToken)) {
                String newAccessToken = jwtService.generateToken(
                        Map.of("role", retrievedUser.getRole().name()),
                        new PrincipalDetails(retrievedUser)
                );

                String newRefreshToken = jwtRefreshService.generateToken(
                        Map.of("role", retrievedUser.getRole().name()),
                        new PrincipalDetails(retrievedUser)
                );

                retrievedUser.setRefreshToken(newRefreshToken);
                userRepository.save(retrievedUser);
                userRepository.flush();

                return AuthenticationResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .build();
            }
        }

        return null;
    }
}