package com.example.insuranceprototype.Auth.controllers;

import javax.validation.Valid;

import com.example.insuranceprototype.Auth.exception.TokenRefreshException;
import com.example.insuranceprototype.Auth.models.RefreshToken;
import com.example.insuranceprototype.Auth.models.User;
import com.example.insuranceprototype.Auth.payload.request.LogOutRequest;
import com.example.insuranceprototype.Auth.payload.request.LoginRequest;
import com.example.insuranceprototype.Auth.payload.request.SignupRequest;
import com.example.insuranceprototype.Auth.payload.request.TokenRefreshRequest;
import com.example.insuranceprototype.Auth.payload.response.JwtResponse;
import com.example.insuranceprototype.Auth.payload.response.MessageResponse;
import com.example.insuranceprototype.Auth.payload.response.TokenRefreshResponse;
import com.example.insuranceprototype.Auth.repository.UserRepository;
import com.example.insuranceprototype.Auth.security.jwt.JwtUtils;
import com.example.insuranceprototype.Auth.security.services.RefreshTokenService;
import com.example.insuranceprototype.Auth.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  @Autowired
  RefreshTokenService refreshTokenService;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    String jwt = jwtUtils.generateJwtToken(userDetails);

    RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

    return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(),
        userDetails.getUsername(), userDetails.getEmail()));
  }

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
    }

    // Create new user's account
    User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),encoder.encode(signUpRequest.getPassword()));

    userRepository.save(user);
    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

  @PostMapping("/refreshtoken")
  public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
    String requestRefreshToken = request.getRefreshToken();

    return refreshTokenService.findByToken(requestRefreshToken)
        .map(refreshTokenService::verifyExpiration)
        .map(RefreshToken::getUser)
        .map(user -> {
          String token = jwtUtils.generateTokenFromUsername(user.getUsername());
          return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
        })
        .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
            "Refresh token is not in database!"));
  }
  
  @PostMapping("/logout")
  public ResponseEntity<?> logoutUser(@Valid @RequestBody LogOutRequest logOutRequest) {
    
    System.out.println(refreshTokenService.findByToken(logOutRequest.getRefreshToken()).isPresent());

    if(refreshTokenService.findByToken(logOutRequest.getRefreshToken()).isPresent()){
      refreshTokenService.deleteByUserId(logOutRequest.getUserId());
      return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }
    else{
      return ResponseEntity.ok(new MessageResponse("Cannot Log out ...No Login Record Found"));
    }

  }

//  @PostMapping("TokenCheck")
//  public ResponseEntity<?> TokenCheck(@RequestBody String jwt){
//
//    if(jwtUtils.validateJwtToken(jwt)) return ResponseEntity.ok(new MessageResponse("Valid access token")) ;
//    return ResponseEntity.ok(new MessageResponse("Invalid access token"));
//
//  }

}
