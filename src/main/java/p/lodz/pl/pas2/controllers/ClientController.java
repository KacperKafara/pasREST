package p.lodz.pl.pas2.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import p.lodz.pl.pas2.model.User;
import p.lodz.pl.pas2.services.UserService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/client")
public class ClientController {

    private final UserService userService;

    @Autowired
    public ClientController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<User> getClientById(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUser(id));
    }

    @GetMapping
    public ResponseEntity<List<User>> getClients() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUsers());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<User> getClientByNickname(@PathVariable String username) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUser(username));
    }

    @PostMapping
    public ResponseEntity<User> addClient(@RequestBody User user) {
        User addedUser = userService.addClient(user);
        if(addedUser == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedUser);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> setActive(@PathVariable UUID id, @RequestBody Map<String, Boolean> active) {
        User updatedUser = userService.setActive(id, Boolean.parseBoolean(active.get("active").toString()));
        if(updatedUser == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateClient(@PathVariable UUID id, @RequestBody User user) {
        User updatedUser = userService.updateClient(id, user);
        if(updatedUser == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }
}