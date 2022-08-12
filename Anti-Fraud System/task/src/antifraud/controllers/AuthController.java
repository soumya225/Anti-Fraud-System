package antifraud.controllers;

import antifraud.UserDetailsServiceImpl;
import antifraud.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PostMapping("/user")
    public ResponseEntity<UserInfoReceipt> registerUser(@RequestBody @Valid UserDetailsImpl user) {

        user.setUsername(user.getUsername().toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        RoleType userRole;

        if(userDetailsService.getAllUsers().isEmpty()) {
            userRole = RoleType.ROLE_ADMINISTRATOR;
            user.setAccountNonLocked(true);
        } else {
            userRole = RoleType.ROLE_MERCHANT;
        }

        user.setRole(userRole);

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (UsernameNotFoundException e) {
            userDetailsService.save(user);
            return new ResponseEntity<>(new UserInfoReceipt(user.getId(),
                    user.getName(),
                    user.getUsername(),
                    userRole.getRoleName()), HttpStatus.CREATED);
        }

    }

    @GetMapping("/list")
    public ResponseEntity<List<UserInfoReceipt>> getUsers() {
        List<UserDetailsImpl> userDetailsList = userDetailsService.getAllUsers();

        List<UserInfoReceipt> receipts = userDetailsList.stream().map(userDetails -> {
            List<String> authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
            return new UserInfoReceipt(userDetails.getId(),
                    userDetails.getName(),
                    userDetails.getUsername(),
                    authorities.get(0).replace("ROLE_", "")
                    );
        }).collect(Collectors.toList());

        return new ResponseEntity<>(receipts, HttpStatus.OK);
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String username) {
        try {
            userDetailsService.delete(username);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Map<String, String> map = new HashMap<>();

        map.put("username", username);
        map.put("status", "Deleted successfully!");

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @PutMapping("/access")
    public ResponseEntity<Map<String, String>> changeAccess(@RequestBody @Valid ChangeAccess changeAccess) {
        String operation;
        try {
            UserDetailsImpl userDetails = userDetailsService.loadUserByUsername(changeAccess.getUsername());

            boolean accountNonLocked;

            if(changeAccess.getOperation().equals(Operation.LOCK.toString())) {
                if(userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(r -> r.equals(RoleType.ROLE_ADMINISTRATOR.toString()))) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                operation = "locked";
                accountNonLocked = false;
            } else if (changeAccess.getOperation().equals(Operation.UNLOCK.toString())) {
                operation = "unlocked";
                accountNonLocked = true;
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            userDetailsService.changeAccess(userDetails, accountNonLocked);

        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Map<String, String> map = new HashMap<>();

        map.put("status", "User " + changeAccess.getUsername() + " " + operation + "!");

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @PutMapping("/role")
    public ResponseEntity<UserInfoReceipt> changeAccess(@RequestBody @Valid ChangeRole changeRole) {
        try {
            UserDetailsImpl userDetails = userDetailsService.loadUserByUsername(changeRole.getUsername());

            RoleType roleType = null;
            for (RoleType r: RoleType.values()) {
                if(changeRole.getRole().equals(r.getRoleName())
                    && !changeRole.getRole().equals(RoleType.ROLE_ADMINISTRATOR.getRoleName())) {
                    roleType = r;
                }
            }

            if(roleType == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            try {
                userDetailsService.changeRole(userDetails, roleType);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }

            return new ResponseEntity<>(new UserInfoReceipt(userDetails.getId(),
                    userDetails.getName(),
                    userDetails.getUsername(),
                    roleType.getRoleName()), HttpStatus.OK);

        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }
 }
