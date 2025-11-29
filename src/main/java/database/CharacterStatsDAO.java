package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.CharacterStats;

public class CharacterStatsDAO
{
	// Obtenemos las estadísticas de los personajes desde la base de datos
	public static CharacterStats getCharacterStats(String characterName) {
		String query = "SELECT characterName, damage, defense, speed FROM Characters WHERE characterName = ?";
		try (Connection connection = DatabaseConnection.getConnection();
			PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, characterName);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					String name = resultSet.getString("characterName");
					int damage = resultSet.getInt("damage");
					int defense = resultSet.getInt("defense");
					double speed = resultSet.getDouble("speed") * 15;
					return new CharacterStats(name, damage, defense, speed);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
