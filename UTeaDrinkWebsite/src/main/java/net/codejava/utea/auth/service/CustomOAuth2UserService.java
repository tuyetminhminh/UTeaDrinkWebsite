// net/codejava/utea/auth/service/CustomOAuth2UserService.java
package net.codejava.utea.auth.service;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

/** Optional helper interface for your own references. Not used by Spring directly. */
@FunctionalInterface
public interface CustomOAuth2UserService {
    OAuth2User loadUser(OAuth2UserRequest userRequest);
}
