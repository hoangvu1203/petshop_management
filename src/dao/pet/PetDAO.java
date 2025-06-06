package dao.pet;

import model.pet.Pet;
import model.pet.Dog;
import model.pet.Cat;
import database.connection_provider;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class PetDAO {
    private static final String TABLE = "pets";
    
    //CREATE FROM RESULT SET
    private static Pet createPetFromResultSet(ResultSet rs) throws SQLException {
        String type = rs.getString("type");
        Pet pet;

        switch (type.toUpperCase()) {
            case "DOG" -> pet = new Dog(
                rs.getString("name"),
                rs.getString("breed"),
                rs.getInt("age"),
                rs.getBigDecimal("price")
            );
            case "CAT" -> pet = new Cat(
                rs.getString("name"),
                rs.getString("breed"),
                rs.getInt("age"),
                rs.getBigDecimal("price")
            );
            default -> throw new SQLException("Unknown pet type");
        }

        pet.setId(rs.getInt("id"));
        return pet;
    }
    
    // CREATE
    public Pet savePet(Pet pet, String type) throws SQLException {
        String sql = "INSERT INTO " + TABLE + " (name, type, breed, age, price) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connection_provider.getCon();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, pet.getName());
            ps.setString(2, type.toUpperCase());
            ps.setString(3, pet.getBreed());
            ps.setInt(4, pet.getAge());
            ps.setBigDecimal(5, pet.getPrice());
            
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) pet.setId(rs.getInt(1));
            }

            return pet;
        }
    }
    
    // SELECT ALL
    public List<Pet> getAllPets() throws SQLException {
        List<Pet> pets = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE + " WHERE status = 1";

        try (Connection conn = connection_provider.getCon();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                pets.add(createPetFromResultSet(rs));
            }
        }

        return pets;
    }
    
    // SELECT BY ID
    public Pet getById(int id) throws SQLException {
        String sql = "SELECT * FROM " + TABLE + " WHERE id = ? AND status = 1";

        try (Connection conn = connection_provider.getCon();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return createPetFromResultSet(rs);
            }
        }
        return null;
    }
    
    //SELECT BY CONDITION
    public List<Pet> getByCondition(String type, String priceOrder) throws SQLException {
        List<Pet> pets = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE + " WHERE 1=1 AND status = 1");

        if (type != null) sql.append(" AND type = ?");
        if (priceOrder != null) sql.append(" ORDER BY price ").append(priceOrder.equalsIgnoreCase("DESC") ? "DESC" : "ASC");

        try (Connection conn = connection_provider.getCon();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (type != null) ps.setString(paramIndex++, type.toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) pets.add(createPetFromResultSet(rs));
            }
        }

        return pets;
    }
    
    //UPDATE
    public boolean updatePet(Pet pet) throws SQLException {
        String sql = "UPDATE " + TABLE + " SET name = ?, breed = ?, age = ?, price = ? WHERE id = ?";

        try (Connection conn = connection_provider.getCon();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pet.getName());
            ps.setString(2, pet.getBreed());
            ps.setInt(3, pet.getAge());
            ps.setBigDecimal(4, pet.getPrice());
            ps.setInt(5, pet.getId());

            return ps.executeUpdate() > 0;
        }
    }
    
    // MARK PET THAT ALREADY SOLD
    public boolean markPetSold(int id) throws SQLException {
        String sql = "UPDATE " + TABLE + " SET status = 0 WHERE id = ?";
        try (Connection conn = connection_provider.getCon();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
    
    //DELETE
    public boolean deletePet(int id) throws SQLException {
        String sql = "DELETE FROM " + TABLE + " WHERE id = ?";

        try (Connection conn = connection_provider.getCon();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
