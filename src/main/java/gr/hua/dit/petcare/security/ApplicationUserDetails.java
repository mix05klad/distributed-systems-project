package gr.hua.dit.petcare.security;

import gr.hua.dit.petcare.core.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class ApplicationUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public ApplicationUserDetails(Long id,
                                  String username,
                                  String password,
                                  Collection<? extends GrantedAuthority> authorities,
                                  boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    public static ApplicationUserDetails fromUser(User user) {
        Set<String> roles = user.getRoles();

        Collection<? extends GrantedAuthority> authorities = roles.stream()
                .map(roleName -> "ROLE_" + roleName) // "OWNER" -> "ROLE_OWNER"
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return new ApplicationUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities,
                true
        );
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
