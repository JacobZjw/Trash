import database.JdbcUtils;

/**
 * @author JwZheng
 */
public class Test {
    public static void main(String[] args) {
        try {
            System.out.println(JdbcUtils.getConnection());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
