package com.example.Admin.Shop.Security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.Admin.Shop.Model.ShopRole;
import com.example.Admin.Shop.Model.ShopUser;
import com.example.Admin.Shop.Repository.ShopUserRepository;

@Service
public class ShopUserDetailsService implements UserDetailsService {
    private final ShopUserRepository userRepository;

    public ShopUserDetailsService(ShopUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String login = username == null ? "" : username.trim();
        ShopUser user = userRepository.findByUsernameIgnoreCase(login)
                .or(() -> userRepository.findByEmailIgnoreCase(login))
                .or(() -> userRepository.findFirstByPhoneOrderByIdAsc(login))
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));

        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .disabled(!user.isEnabled())
                .authorities(buildAuthorities(user.getRole()))
                .build();
    }

    private List<SimpleGrantedAuthority> buildAuthorities(ShopRole role) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

        if (role == ShopRole.SUPER_ADMIN) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("Admin"));
        } else if (role == ShopRole.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("Admin"));
        } else if (role == ShopRole.MANAGER) {
            authorities.add(new SimpleGrantedAuthority("Manager"));
        } else if (role == ShopRole.STAFF) {
            authorities.add(new SimpleGrantedAuthority("Staff"));
        } else if (role == ShopRole.CUSTOMER) {
            authorities.add(new SimpleGrantedAuthority("Customer"));
        }

        return authorities;
    }
}
