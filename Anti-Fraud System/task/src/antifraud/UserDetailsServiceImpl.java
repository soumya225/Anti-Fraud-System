package antifraud;

import antifraud.models.RoleType;
import antifraud.models.UserDetailsImpl;
import antifraud.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepo;

    @Override
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserDetailsImpl> user = userRepo.findUserByUsername(username);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("Not found: " + username);
        }

        return user.get();
    }

    public void save(UserDetailsImpl user) {
        userRepo.save(user);
    }

    public void delete(String username) throws UsernameNotFoundException {
        Optional<UserDetailsImpl> user = userRepo.findUserByUsername(username);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("Not found: " + username);
        }

        userRepo.delete(user.get());
    }

    public List<UserDetailsImpl> getAllUsers() {
        List<UserDetailsImpl> userDetailsList = new ArrayList<>();

        userRepo.findAll().forEach(userDetailsList::add);

        return  userDetailsList;
    }

    public void changeAccess(UserDetailsImpl user, boolean accountNonLocked) {
        user.setAccountNonLocked(accountNonLocked);
        userRepo.save(user);
    }

    public void changeRole(UserDetailsImpl user, RoleType roleType) throws IllegalArgumentException {
        if(user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals(roleType.toString()))) {
            throw new IllegalArgumentException();
        }

        user.setRole(roleType);
        userRepo.save(user);
    }
}