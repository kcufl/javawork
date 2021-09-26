package com.batch.job.model;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Alias("member")
public class Member {
    private Long id;
    private String name;
    private String country;
    private Long population;
    private Long amount;

    public Member() {
    }

    public Member(String name, String country, Long population) {
        this.name = name;
        this.country = country;
        this.population = population;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Long getPopulation() {
		return population;
	}

	public void setPopulation(Long population) {
		this.population = population;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}
}
