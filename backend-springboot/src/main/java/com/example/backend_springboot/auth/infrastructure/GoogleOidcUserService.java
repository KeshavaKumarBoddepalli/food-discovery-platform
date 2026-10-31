package com.example.backend_springboot.auth.infrastructure;

import com.example.backend_springboot.user.application.UserProvisioningService;
import com.example.backend_springboot.user.domain.AppUser;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class GoogleOidcUserService extends OidcUserService {

	private final UserProvisioningService userProvisioningService;

	public GoogleOidcUserService(UserProvisioningService userProvisioningService) {
		this.userProvisioningService = userProvisioningService;
	}

	@Override
	public OidcUser loadUser(OidcUserRequest userRequest) {
		OidcUser identity = super.loadUser(userRequest);
		AppUser user = userProvisioningService.provisionFromGoogle(identity);

		Set<GrantedAuthority> authorities = new HashSet<>(identity.getAuthorities());
		authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
		return new DefaultOidcUser(authorities, identity.getIdToken(), identity.getUserInfo(), "sub");
	}
}
