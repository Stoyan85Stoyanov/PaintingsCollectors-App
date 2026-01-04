package com.paintingscollectors.service;

import com.google.gson.Gson;
import com.paintingscollectors.model.dto.UserDTO;
import com.paintingscollectors.util.UserSession;
import com.paintingscollectors.model.dto.UserLoginDto;
import com.paintingscollectors.model.dto.UserRegisterDto;
import com.paintingscollectors.model.entity.User;
import com.paintingscollectors.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final String USERS_FILE_PATH = "src/main/resources/files/users.json";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSession userSession;
    private final Gson gson;
    private final ModelMapper modelMapper;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserSession userSession, Gson gson, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userSession = userSession;
        this.gson = gson;
        this.modelMapper = modelMapper;
    }

    public String readUsersFileContent() throws IOException {
        return Files.readString(Path.of(USERS_FILE_PATH));
    }

    public void initUsers() throws IOException {
        if (userRepository.count() == 0) {
            Arrays.stream(gson.fromJson(readUsersFileContent(), UserDTO[].class))
                    .map(userDTO -> {
                        User user = modelMapper.map(userDTO, User.class);
                        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                        return user;
                    }).forEach(userRepository::save);
        }
    }

    public boolean register(UserRegisterDto userRegisterDto) {
        Optional<User> existingUser = userRepository
                .findByUsernameOrEmail(userRegisterDto.getUsername(), userRegisterDto.getEmail());

        if (existingUser.isPresent()) {
            return false;
        }

        User user = new User();
        user.setUsername(userRegisterDto.getUsername());
        user.setEmail(userRegisterDto.getEmail());
        user.setPassword(userRegisterDto.getPassword());
        user.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));

        this.userRepository.save(user);
        return true;
    }

    public boolean login(UserLoginDto data) {
        Optional<User> byUsername = userRepository.findByUsername(data.getUsername());

        if (byUsername.isEmpty()) {
            return false;
        }

        boolean passMatch = passwordEncoder.matches(data.getPassword(), byUsername.get().getPassword());

        if (!passMatch) {
            return false;
        }
        userSession.login(byUsername.get().getId(), byUsername.get().getUsername());
        return true;
    }

    public Optional<User> getByUsername(String username) {
        return userRepository.findOptionalByUsername(username);
    }

    public User getById(UUID userId) {

        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User with id [%s] does not exist.".formatted(userId)));
    }
}
