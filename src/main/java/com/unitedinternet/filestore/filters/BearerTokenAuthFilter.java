package com.unitedinternet.filestore.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Class used to check that the credential (in this case a bearer token) is present and valid
 * in the required "Authorization" header.
 * As no authn/z server is available the check token endpoint is stubbed
 */
@Component
public class BearerTokenAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if(authHeader != null && authHeader.startsWith("Bearer ") && !authHeader.substring(7).isBlank()) {
            String accessToken = authHeader.substring(7);
            User user = isTokenValid(accessToken);

            if(user == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            else {
                Authentication authenticationToken = new UsernamePasswordAuthenticationToken(user.getId(), null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * This method will pass the token to the authz server and check for its validity
     * As no authz server is available the code is commented out.
     * Normally the constructor which includes the granted authorities is used.
     * This way in the session will be a user plus authorities and specific endpoints,
     * annotated with @PreAuthorize will allow access only if the authn user has the required permissions
     * As such FileStorageAdminController and FileStorageController may be segregated by user types(regular, admin)
     * @param token the authz token
     * @return the user containing the authorities that should be checked against the targeted endpoint
     */
    private User isTokenValid(String token) {
        return new User(1L);
    }

    private class User {
        private Long id;
        private List<GrantedAuthority> authorities;

        public User(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public List<GrantedAuthority> getAuthorities() {
            return authorities;
        }

    }
}
