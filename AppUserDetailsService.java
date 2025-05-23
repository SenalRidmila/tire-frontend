package com.example.demo.service;

import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
public class AppUserDetailsService implements UserDetailsService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Employee not found with email: " + email));

        // Ensure the role is not null and correctly formatted
        String role = employee.getRole();
        if (role == null || role.trim().isEmpty()) {
            // This case should ideally be prevented by validation during employee creation/update
            throw new UsernameNotFoundException("Employee has no role assigned: " + email);
        }
        // Roles in Spring Security should typically start with "ROLE_"
        // The Employee model's setter should already handle this prefixing.
        
        Set<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(role));

        return new User(employee.getEmail(), employee.getPassword(), authorities);
    }
}
