package antifraud.controllers;

import antifraud.UserDetailsServiceImpl;
import antifraud.models.User;
import antifraud.models.UserDetailsImpl;
import antifraud.models.UserInfoReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PostMapping("/user")
    public ResponseEntity<UserInfoReceipt> registerUser(@RequestBody @Valid User user) {

        user.setUsername(user.getUsername().toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        if(userDetails != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        userDetailsService.save(user);

        return new ResponseEntity<>(new UserInfoReceipt(user.getId(),
                                                        user.getName(),
                                                        user.getUsername()), HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserInfoReceipt>> getUsers() {
        List<UserDetailsImpl> userDetailsList = userDetailsService.getAllUsers();

        List<UserInfoReceipt> receipts = userDetailsList.stream().map(userDetails ->
            new UserInfoReceipt(userDetails.getId(),
                                userDetails.getName(),
                                userDetails.getUsername())
        ).collect(Collectors.toList());

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
}
