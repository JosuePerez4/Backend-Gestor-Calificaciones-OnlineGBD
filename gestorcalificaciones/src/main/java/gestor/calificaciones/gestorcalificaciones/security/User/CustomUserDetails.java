package gestor.calificaciones.gestorcalificaciones.security.User;

import lombok.Data;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import gestor.calificaciones.gestorcalificaciones.entities.User;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Data
public class CustomUserDetails implements UserDetails {

    private final User user;
    private final UUID id;
    private final String email;

    public CustomUserDetails(User user) {
        this.user = user;
        this.id = user.getId();
        this.email = user.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = "ROLE_" + user.getRole().name();
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // make sure User has this field
    }

    @Override
    public String getUsername() {
        return user.getId().toString(); // Return user ID instead of email
    }

    public User getUser() {
        return user;
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
        return true;
    }
}