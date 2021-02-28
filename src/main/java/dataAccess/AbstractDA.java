package dataAccess;

import com.itextpdf.text.pdf.PdfPTable;
import connection.ConnectionFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbstractDA<T> {
    protected static final Logger LOGGER = Logger.getLogger(AbstractDA.class.getName());

    private final Class<T> type;

    @SuppressWarnings("unchecked")
    public AbstractDA() {
        this.type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

    }

    /**
     * creates select instruction
     *
     * @param field field to check condition
     * @return instruction
     */
    private String createSelectQuery(String field) {
        return "SELECT * FROM " + type.getSimpleName() + " WHERE " + field + " =?";
    }

    /**
     * creates insert instruction
     *
     * @param data to be inserted
     * @return insert instruction
     */
    private String createInsertInstruction(T data) {
        StringBuilder insertInstruction = new StringBuilder();
        insertInstruction.append("INSERT INTO ").append(type.getSimpleName()).append("(");//.append(values.get(0));//(id, name, address, email, age) VALUES");
        for (Field field : data.getClass().getDeclaredFields()) {
            insertInstruction.append(field.getName()).append(", ");
        }
        insertInstruction.replace(insertInstruction.lastIndexOf(", "), insertInstruction.lastIndexOf(", ") + 10, ") VALUES (");
        for (int i = 0; i < data.getClass().getDeclaredFields().length; i++) {
            insertInstruction.append("?, ");
        }
        insertInstruction.replace(insertInstruction.lastIndexOf(", "), insertInstruction.lastIndexOf(", ") + 26, ") ON DUPLICATE KEY UPDATE ");
        for (Field field : data.getClass().getDeclaredFields()) {
            if (field.getName().equals("id")) continue;
            insertInstruction.append(field.getName()).append(" = VALUES(").append(field.getName()).append("), ");
        }
        insertInstruction.replace(insertInstruction.lastIndexOf(", "), insertInstruction.lastIndexOf(", ") + 2, ";");
        //System.out.println(insertInstruction);
        return insertInstruction.toString();
    }

    /**
     * creates delete instruction
     *
     * @param field to be looked for
     * @return instruction
     */
    private String createDeleteInstruction(String field) {
        return "DELETE FROM " + type.getSimpleName() + " WHERE " + field + " =?";
    }

    private String createUpdateInstruction(T data, String condition) {
        StringBuilder insertInstruction = new StringBuilder();
        insertInstruction.append("UPDATE ").append(type.getSimpleName()).append(" SET ");
        for (Field field : data.getClass().getDeclaredFields()) {
            insertInstruction.append(field.getName()).append(" = ?, ");
        }
        insertInstruction.replace(insertInstruction.lastIndexOf(", "), insertInstruction.lastIndexOf(", ") + 2, " WHERE " + condition + " = ?");
        System.out.println(insertInstruction);
        return insertInstruction.toString();
    }

    public List<T> findAll() {
        // TODO:
        return null;
    }

    /**
     * looks for object in database by name
     *
     * @param name to be looked for
     * @return object found or null
     */
    public T findByName(String name) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String query = createSelectQuery("name");
        try {
            connection = ConnectionFactory.getConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, name);
            resultSet = statement.executeQuery();

            return createObjects(resultSet).get(0);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, type.getName() + "DA:findByName " + e.getMessage());
        } finally {
            ConnectionFactory.close(resultSet);
            ConnectionFactory.close(statement);
            ConnectionFactory.close(connection);
        }
        return null;
    }

    /**
     * map database info to java object
     *
     * @param resultSet result set from database
     * @return java object
     */
    private List<T> createObjects(ResultSet resultSet) {
        List<T> list = new ArrayList<T>();

        try {
            while (resultSet.next()) {
                T instance = type.newInstance();
                for (Field field : type.getDeclaredFields()) {
                    Object value = resultSet.getObject(field.getName());
                    PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), type);
                    Method method = propertyDescriptor.getWriteMethod();
                    method.invoke(instance, value);
                }
                list.add(instance);
            }
        } catch (InstantiationException | IllegalAccessException | SecurityException | IllegalArgumentException | InvocationTargetException | SQLException | IntrospectionException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * generate report
     *
     * @param table to be generated
     * @return pdf data for report
     */
    public PdfPTable select(String table) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        ResultSetMetaData rsmd = null;
        String query = "SELECT * FROM " + table;
        try {
            connection = ConnectionFactory.getConnection();
            statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery();
            rsmd = resultSet.getMetaData();
            PdfPTable resultTable = new PdfPTable(rsmd.getColumnCount());

            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                resultTable.addCell(rsmd.getColumnName(i));
            }
            int rowCount = 0;
            while (resultSet.next()) {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    resultTable.addCell(resultSet.getString(rsmd.getColumnName(i)));
                }
                rowCount++;
            }

            return resultTable;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, type.getName() + "DA:findByName " + e.getMessage());
        } finally {
            ConnectionFactory.close(resultSet);
            ConnectionFactory.close(statement);
            ConnectionFactory.close(connection);
        }
        return null;
    }

    /**
     * insert object into database
     *
     * @param data to be inserted
     */
    public void insert(T data) {
        Connection connection = null;
        PreparedStatement statement = null;
        String query = createInsertInstruction(data);
        try {
            connection = ConnectionFactory.getConnection();
            statement = connection.prepareStatement(query);
            int rang = 1;
            for (Field field : data.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                statement.setObject(rang, field.get(data));
                rang++;
            }
            statement.execute();
        } catch (SQLException | IllegalAccessException e) {
            LOGGER.log(Level.WARNING, type.getName() + "DA:insert " + e.getMessage());
        } finally {
            ConnectionFactory.close(statement);
            ConnectionFactory.close(connection);
        }
    }

    /**
     * delete object from database
     *
     * @param name to be looked for
     */
    public void deleteByName(String name) {
        Connection connection = null;
        PreparedStatement statement = null;
        String query = createDeleteInstruction("name");
        try {
            connection = ConnectionFactory.getConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, type.getName() + "DA:deleteByName " + e.getMessage());
        } finally {
            ConnectionFactory.close(statement);
            ConnectionFactory.close(connection);
        }
    }

    /**
     * update object in database
     *
     * @param data      to be updated
     * @param condition to be looked for
     */
    public void updateByName(T data, String condition) {
        Connection connection = null;
        PreparedStatement statement = null;
        String query = createUpdateInstruction(data, condition);
        try {
            connection = ConnectionFactory.getConnection();
            statement = connection.prepareStatement(query);
            int rang = 1;
            for (Field field : data.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                statement.setObject(rang, field.get(data));
                rang++;
            }
            Field f = data.getClass().getDeclaredField(condition);
            f.setAccessible(true);
            statement.setObject(rang, f.get(data));
            statement.execute();
        } catch (SQLException | NoSuchFieldException | IllegalAccessException e) {
            LOGGER.log(Level.WARNING, type.getName() + "DA:updateById " + e.getMessage());
        } finally {
            ConnectionFactory.close(statement);
            ConnectionFactory.close(connection);
        }
    }
}
