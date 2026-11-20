package com.love.joy.controller;

import com.love.joy.model.EvaluationRequest;
import com.love.joy.model.User;
import com.love.joy.repository.EvaluationRequestRepository;
import com.love.joy.repository.UserRepository;
import com.love.joy.service.FileStorageService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Controller
public class EvaluationController
{

    private final EvaluationRequestRepository evalRepo;
    private final UserRepository userRepo;
    private final FileStorageService fileStorage;

    public EvaluationController(EvaluationRequestRepository evalRepo, UserRepository userRepo, FileStorageService fileStorage)
    {
        this.evalRepo = evalRepo; this.userRepo = userRepo; this.fileStorage = fileStorage;
    }

    //show evaluation request form
    @GetMapping("/evaluation")
    public String evalForm() { return "request_evaluation"; }

    //for evaluation form submission
    @PostMapping("/evaluation")
    public String submitEval(@RequestParam String description,
                             @RequestParam String preferredContact,
                             @RequestParam("photo") MultipartFile photo,
                             Authentication auth,
                             Model model) {
        try {
            String email = auth.getName();
            Optional<User> userOpt = userRepo.findByEmail(email);
            if (userOpt.isEmpty())
            {
                //if there is no user for this emailthen show error and stay on the form
                model.addAttribute("error", "User not found"); return "request_evaluation";
            }
            User user = userOpt.get();

            // to store the filename if the user uploads a photo
            String storedFile = null;
            // making sure to only store if the user actually picked a file
            if (photo != null && !photo.isEmpty())
            {
                storedFile = fileStorage.storeFile(photo);
            }
            // create a new EvaluationRequest entity in db
            EvaluationRequest r = new EvaluationRequest();
            r.setUser(user);
            // i escape the description so that if an attacker tries to inject HTML/JS
            // then it will be displayed as text and not executed for XSS protection
            r.setDescription(org.apache.commons.text.StringEscapeUtils.escapeHtml4(description)); // simple escape
            //how user would like to be contacted either phone or email
            r.setPreferredContact(preferredContact);
            //if a file was uploaded and storedthen keep only the safe filename/path here
            r.setPhotoPath(storedFile);
            // save everything into the evaluation_requests table
            evalRepo.save(r);
            model.addAttribute("message", "Request submitted");
            return "message";
        } catch (Exception ex) {
            // if error then send user back to the form with error message
            model.addAttribute("error", ex.getMessage());
            return "request_evaluation";
        }
    }

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> serveFile(@PathVariable String filename)
    {
        try {
            //instead of doing something like "uploads/" + filename, i pass the filename into my FileStorageService,to load from safe directory
            //this helps protect against path traversal attacks
            Path p = fileStorage.load(filename);
            // make Path into a spring resource so it can be streamed to the browser
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(p.toUri());

            // if file doesnt exist then return 404 instead of crashing
            if (!resource.exists())
            {
                return ResponseEntity.notFound().build();
            }

            //try to figure out the content type of the file (image/jpeg, image/png, pdf, etc.)
            String contentType = Files.probeContentType(p);
            if (contentType == null)
            {
                //else it falls back to generic binary type
                contentType = "application/octet-stream";
            }

            //  return the file with the detected content type, content-Disposition set to "inline" so images open in the browser instead of forcing download
            // i control exactly WHICH file is being returned using the safe path from FileStorageService
            // and tjis reduces the risk of exposing random files on the server
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            // If error then send a generic 500
            return ResponseEntity.internalServerError().build();
        }
    }
}
