package com.nahudev.ToDoListApplication.controller;

import com.nahudev.ToDoListApplication.dto.CreateUserDTO;
import com.nahudev.ToDoListApplication.model.ERole;
import com.nahudev.ToDoListApplication.model.RoleEntity;
import com.nahudev.ToDoListApplication.model.UserEntity;
import com.nahudev.ToDoListApplication.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) throws URISyntaxException {

        if (createUserDTO.getEmail().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Set<RoleEntity> roles = createUserDTO.getRoles().stream()
                .map(role -> RoleEntity.builder()
                        .name(ERole.valueOf(role))
                        .build())
                .collect(Collectors.toSet());

        UserEntity userEntity = UserEntity.builder()
                .name(createUserDTO.getName())
                .lastName(createUserDTO.getLastName())
                .birthdate(createUserDTO.getBirthdate())
                .username(createUserDTO.getUsername())
                .email(createUserDTO.getEmail())
                .password(passwordEncoder.encode(createUserDTO.getPassword()))
                .roles(roles)
                .build();

        userService.createdUser(userEntity);
        return ResponseEntity.ok(userEntity);

    }

    @PutMapping("/edit/{id_user}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> editUser(@PathVariable Long id_user,@RequestBody UserEntity userEntity) {

        Optional<UserEntity> userFound = userService.getUser(id_user);
        if (userFound.isPresent()) {
            userService.editUser(id_user, userEntity);
            return ResponseEntity.ok("Usuario editado exitosamente!");
        }

        return ResponseEntity.notFound().build();

    }

    @DeleteMapping("/delete/{id_user}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id_user) {

        if (id_user != null) {
            userService.deleteUser(id_user);
            return ResponseEntity.ok("Usuario eliminado exitosamente!");
        }

        return ResponseEntity.notFound().build();

    }

    @GetMapping("/getUser/{id_user}")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUser(@PathVariable Long id_user) {

        Optional<UserEntity> userFound = userService.getUser(id_user);

        if (userFound.isPresent()) {
            return ResponseEntity.ok(userFound);
        }

        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/getAllUsers")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {

        List<UserEntity> userEntityList = userService.getAllUsers();

        if (userEntityList != null) {
            return ResponseEntity.ok(userEntityList);
        }

        return ResponseEntity.badRequest().build();
    }

}
