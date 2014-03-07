package no.hig.strand.lars.todoity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TaskEntity {
	@Id
	private String id;
	
	private String category;
	private String description;
	private double latitude;
	private double longitude;
	private String address;
	private boolean isActive;
	private String timeStarted;
	private String timeEnded;
	private int timeSpent;
	private boolean isFinished;
	
	private String fixedStart;
	private String fixedEnd;
	
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public String getTimeStarted() {
		return timeStarted;
	}
	
	public void setTimeStarted(String timeStarted) {
		this.timeStarted = timeStarted;
	}
	
	public String getTimeEnded() {
		return timeEnded;
	}
	
	public void setTimeEnded(String timeEnded) {
		this.timeEnded = timeEnded;
	}
	
	public int getTimeSpent() {
		return timeSpent;
	}
	
	public void setTimeSpent(int timeSpent) {
		this.timeSpent = timeSpent;
	}
	
	public boolean isFinished() {
		return isFinished;
	}
	
	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}
	
	public String getFixedStart() {
		return fixedStart;
	}
	
	public void setFixedStart(String fixedStart) {
		this.fixedStart = fixedStart;
	}
	
	public String getFixedEnd() {
		return fixedEnd;
	}
	
	public void setFixedEnd(String fixedEnd) {
		this.fixedEnd = fixedEnd;
	}
	
}
