package com.SpringBootMVC.ExpensesTracker.service;

import com.SpringBootMVC.ExpensesTracker.DTO.CustomUserDetails;
import com.SpringBootMVC.ExpensesTracker.DTO.WebUser;
import com.SpringBootMVC.ExpensesTracker.entity.Client;
import com.SpringBootMVC.ExpensesTracker.entity.Role;
import com.SpringBootMVC.ExpensesTracker.entity.User;
import com.SpringBootMVC.ExpensesTracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Implementation of the UserService interface that provides user registration
 * and authentication-related business logic.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final ClientService clientService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleService roleService,
                           ClientService clientService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.clientService = clientService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Finds a user by their username.
     *
     * @param username the username to search by.
     * @return the User object if found, or null otherwise.
     */
    @Override
    public User findUserByUserName(String username) {
        return userRepository.findByUserName(username);
    }

    /**
     * Saves a new user based on data provided by the WebUser DTO.
     * This method creates both the Client and User entities and associates them.
     *
     * @param webUser the DTO containing registration details.
     */
    @Transactional
    @Override
    public void save(WebUser webUser) {
        // Create Client entity and populate it.
        Client client = new Client();
        client.setFirstName(webUser.getFirstName());
        client.setLastName(webUser.getLastName());
        client.setEmail(webUser.getEmail());

        // Create User entity and associate it with the Client.
        User user = new User();
        user.setUserName(webUser.getUsername());
        user.setPassword(passwordEncoder.encode(webUser.getPassword()));
        user.setClient(client);
        user.setEnabled(true);
        // Assign the default role to the user.
        user.setRoles(Arrays.asList(roleService.findRoleByName("ROLE_STANDARD")));

        userRepository.save(user);
    }

    /**
     * Loads user details required for Spring Security authentication.
     *
     * @param username the username of the user to load.
     * @return a CustomUserDetails object that contains user credentials and authorities.
     * @throws UsernameNotFoundException if the user is not found.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new CustomUserDetails(
                user.getUserName(),
                user.getPassword(),
                mapRolesToAuthorities(user.getRoles()),
                user.getClient().getId()
        );
    }

    /**
     * Converts a collection of Role objects into a collection of GrantedAuthority objects.
     *
     * @param roles the collection of Role objects.
     * @return a collection of GrantedAuthority objects derived from roles.
     */
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
}
