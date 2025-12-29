package com.tool.atkdefbackend.service.auth;

import com.tool.atkdefbackend.entity.TeamEntity;
import com.tool.atkdefbackend.repository.TeamRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final TeamRepository teamRepository;

    public UserDetailsServiceImpl(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TeamEntity team = teamRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Team Not Found with username: " + username));
        return UserDetailsImpl.build(team);
    }
}
