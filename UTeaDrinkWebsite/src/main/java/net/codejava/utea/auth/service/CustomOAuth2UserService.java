package net.codejava.utea.auth.service;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public interface CustomOAuth2UserService {

	OAuth2User loadUser(OAuth2UserRequest userRequest);
}
