package com.JwellARDB.ImagesDB.Controller;

import com.JwellARDB.ImagesDB.services.FirebaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final FirebaseService firebaseService;

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(
            @RequestHeader("Authorization") String token
    ) throws Exception {

        String uid = firebaseService.verifyToken(token);

        // TODO: delete user data from MySQL here
        // userActivityRepo.deleteByUserId(uid);
        // paymentRepo.deleteByUserId(uid);

        return ResponseEntity.ok("User deleted successfully: " + uid);
    }

    // 🔥 Test endpoint to check login working
    @GetMapping("/check")
    public ResponseEntity<?> checkLogin(
            @RequestHeader("Authorization") String token
    ) throws Exception {

        String uid = firebaseService.verifyToken(token);

        return ResponseEntity.ok("Login working. UID: " + uid);
    }
}