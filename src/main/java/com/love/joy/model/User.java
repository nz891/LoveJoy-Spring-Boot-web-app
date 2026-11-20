package com.love.joy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
public class User
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(nullable = false)
	private String name;

	private String phone;

	// security question and hashed answer
	@Column(name = "security_question")
	private String securityQuestion;

	@Column(name = "security_answer_hash")
	private String securityAnswerHash;

	// email verified flag
	private boolean enabled = false;

	// login lockout fields
	private int failedAttempts = 0;
	private LocalDateTime lockedUntil;

	// two-factor authentication fields
	@Column(name = "two_factor_code")
	private String twoFactorCode;

	@Column(name = "two_factor_expires_at")
	private LocalDateTime twoFactorExpiresAt;

	private LocalDateTime createdAt = LocalDateTime.now();
	private LocalDateTime updatedAt = LocalDateTime.now();

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "user_roles",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id")
	)
	private Set<Role> roles;

	public User()
	{

	}

	// ---------- getters & setters ----------

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getPasswordHash()
	{
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash)
	{
		this.passwordHash = passwordHash;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getPhone()
	{
		return phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getSecurityQuestion()
	{
		return securityQuestion;
	}

	public void setSecurityQuestion(String securityQuestion)
	{
		this.securityQuestion = securityQuestion;
	}

	public String getSecurityAnswerHash()
	{
		return securityAnswerHash;
	}

	public void setSecurityAnswerHash(String securityAnswerHash)
	{
		this.securityAnswerHash = securityAnswerHash;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public int getFailedAttempts()
	{
		return failedAttempts;
	}

	public void setFailedAttempts(int failedAttempts)
	{
		this.failedAttempts = failedAttempts;
	}

	public LocalDateTime getLockedUntil()
	{
		return lockedUntil;
	}

	public void setLockedUntil(LocalDateTime lockedUntil)
	{
		this.lockedUntil = lockedUntil;
	}

	public String getTwoFactorCode()
	{
		return twoFactorCode;
	}

	public void setTwoFactorCode(String twoFactorCode)
	{
		this.twoFactorCode = twoFactorCode;
	}

	public LocalDateTime getTwoFactorExpiresAt()
	{
		return twoFactorExpiresAt;
	}

	public void setTwoFactorExpiresAt(LocalDateTime twoFactorExpiresAt)
	{
		this.twoFactorExpiresAt = twoFactorExpiresAt;
	}

	public LocalDateTime getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt)
	{
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt()
	{
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt)
	{
		this.updatedAt = updatedAt;
	}

	public Set<Role> getRoles()
	{
		return roles;
	}

	public void setRoles(Set<Role> roles)
	{
		this.roles = roles;
	}


	// true if user is currently locked
	public boolean isAccountLocked()
	{
		return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
	}

	//increase failed attempts and lock if limit reached
	public void incrementFailedAttempts(int maxAttempts)
	{
		this.failedAttempts++;
		if (this.failedAttempts >= maxAttempts)
		{
			// lock for 15 minutes
			this.lockedUntil = LocalDateTime.now().plusMinutes(15);
		}
	}

	//reset counter and unlock
	public void resetFailedAttempts()
	{
		this.failedAttempts = 0;
		this.lockedUntil = null;
	}
}