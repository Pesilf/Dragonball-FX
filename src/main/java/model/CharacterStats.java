package model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CharacterStats {

	private String name;
	private int damage;
	private int defense;
	private double speed;
}
