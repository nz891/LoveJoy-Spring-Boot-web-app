package com.love.joy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="password_reset_tokens")
public class PasswordResetToken
{

    @Id
    private String token;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    private LocalDateTime expiresAt;

    private boolean used = false;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}


}
