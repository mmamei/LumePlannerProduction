package model;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GridCrowding {
	
	private String cell;
	private List<Double> crowdings;
	
	public GridCrowding(){}

	public GridCrowding(String cell, List<Double> crowdings) {
		super();
		this.setCell(cell);
		this.setCrowdings(crowdings);
	}

	public String getCell() {
		return cell;
	}

	public void setCell(String cell) {
		this.cell = cell;
	}

	public List<Double> getCrowdings() {
		return crowdings;
	}

	public void setCrowdings(List<Double> crowdings) {
		this.crowdings = crowdings;
	}
	
	public String toJSONString() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public String toString() {
		return "GridCrowding [cell=" + cell + ", crowdings=" + crowdings + "]";
	}
	
	
	

}
