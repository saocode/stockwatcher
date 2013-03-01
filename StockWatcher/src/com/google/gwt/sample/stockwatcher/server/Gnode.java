package com.google.gwt.sample.stockwatcher.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Gnode {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private String id;
	@Persistent
	private LatLong latlong;
	@Persistent
	private int density;

	public Gnode(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public LatLong getLatLong() {
		return latlong;
	}

	public void setLatLong(LatLong point) {
		this.latlong = point;
	}

	public int getDensity() {
		return density;
	}

	public void setDensity(int density) {
		this.density = density;
	}





}