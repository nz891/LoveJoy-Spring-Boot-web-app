package com.love.joy.model;

import org.springframework.security.core.GrantedAuthority;

import jakarta.persistence.*;

@Entity
@Table(name="roles")
public class Role  implements GrantedAuthority
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable=false, unique=true)
    private String name;
    
    @Override
    public String getAuthority() {
        return name;
    }

	public Role(Integer id, String name)
	{
		super();
		this.id = id;
		this.name = name;
	}

	public Role()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
    


}
