package com.love.joy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="evaluation_requests")
public class EvaluationRequest
{

	//primary key for each evaluation request
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

	//link request to user who submitted it (mto because many requests can be for one user)
    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

	// description of what the user wants feedback on (escape this in the controller for XSS)
    private String description;

	//how user prefers to be contacted (email or phoen)
    private String preferredContact;

	//where uploaded photo is stored
    private String photoPath;

	//status field so admin can track progress
    private String status = "PENDING";

    private LocalDateTime createdAt = LocalDateTime.now();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPreferredContact() {
		return preferredContact;
	}

	public void setPreferredContact(String preferredContact) {
		this.preferredContact = preferredContact;
	}

	public String getPhotoPath() {
		return photoPath;
	}

	public void setPhotoPath(String photoPath) {
		this.photoPath = photoPath;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}


}
