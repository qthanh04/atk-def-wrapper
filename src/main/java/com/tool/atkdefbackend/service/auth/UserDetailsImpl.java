package com.tool.atkdefbackend.service.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tool.atkdefbackend.entity.TeamEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * UserDetails Implementation with enhanced fields
 * Based on NewTech.md Section 2: JWT Authentication & Authorization
 */
@Getter
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Integer id;
    private final String username;
    private final String displayName; // Team display name
    private final String teamId; // String team ID for consistency with Python Core
    private final String teamName; // Team name

    @JsonIgnore
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Integer id, String username, String displayName, String teamId,
                          String teamName, String password,
                          Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.teamId = teamId;
        this.teamName = teamName;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Build UserDetails from TeamEntity
     * Team's role field is used for authorization (ADMIN, TEACHER, or TEAM)
     */
    public static UserDetailsImpl build(TeamEntity team) {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + (team.getRole() != null ? team.getRole() : "TEAM")));

        return new UserDetailsImpl(
                team.getId() != null ? team.getId() : 0,
                team.getUsername() != null ? team.getUsername() : "",
                team.getName() != null ? team.getName() : "", // displayName = team name
                team.getId() != null ? team.getId().toString() : "0", // teamId as String
                team.getName() != null ? team.getName() : "", // teamName
                team.getPassword() != null ? team.getPassword() : "",
                authorities);
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
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserDetailsImpl))
            return false;
        UserDetailsImpl that = (UserDetailsImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
