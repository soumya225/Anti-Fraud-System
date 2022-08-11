package antifraud;

import antifraud.models.User;
import antifraud.models.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
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
    public UserDetailsImpl loadUserByUsername(String username) {
        Optional<User> user = userRepo.findUserByUsername(username);

        if (user.isEmpty()) {
            return null;
        }

        return new UserDetailsImpl(user.get());
    }

    public void save(User user) {
        userRepo.save(user);
    }

    public void delete(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findUserByUsername(username);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("Not found: " + username);
        }

        userRepo.delete(user.get());
    }

    public List<UserDetailsImpl> getAllUsers() {
        List<UserDetailsImpl> userDetailsList = new ArrayList<>();

        userRepo.findAll().forEach(user -> {
            userDetailsList.add(new UserDetailsImpl(user));
        });

        return  userDetailsList;
    }
}