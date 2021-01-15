package database;

import model.BookModel;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

/**
 * @author JwZheng
 */
public class JdbcUtils {

    public static Connection getConnection() throws Exception {

        InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("JDBC.properties");

        Properties properties = new Properties();
        properties.load(in);

        String user = properties.getProperty("user");
        String password = properties.getProperty("password");
        String url = properties.getProperty("url");
        String driverClass = properties.getProperty("driverClass");

        Class.forName(driverClass);

        return DriverManager.getConnection(url, user, password);
    }

    public static synchronized void insertIntoTable(List<BookModel> books) {
        String sql = "REPLACE INTO jdbook(bookID,bookName,bookPrice,imgURL)VALUES (?,?,?,?)";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            // 1.获取数据库连接
            conn = getConnection();
            conn.setAutoCommit(false);
            // 2.预编译SQL语句
            ps = conn.prepareStatement(sql);

            for (BookModel book : books) {
                // 3.填充占位符
                ps.setObject(1, book.getBookID());
                ps.setObject(2, book.getBookName());
                ps.setObject(3, book.getBookPrice());
                ps.setObject(4, book.getImgUrl());
                ps.addBatch();
            }
            // 4.执行
            ps.executeBatch();

            // 5.提交
            conn.commit();

        } catch (Exception e) {
            System.out.println("线程：" + Thread.currentThread().getName() + "插入数据出错！");
            e.printStackTrace();
        } finally {
            // 6.释放资源
            closeResources(conn, ps);
        }

    }

    public static synchronized void insertIntoTableWithImage(List<BookModel> books) {
        String sql = "REPLACE INTO jdbook(bookID,bookName,bookPrice,imgURL,image)VALUES (?,?,?,?,?)";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            // 1.获取数据库连接
            conn = getConnection();
            // 2.预编译SQL语句
            ps = conn.prepareStatement(sql);

            for (BookModel book : books) {
                // 3.填充占位符
                ps.setObject(1, book.getBookID());
                ps.setObject(2, book.getBookName());
                ps.setObject(3, book.getBookPrice());
                ps.setObject(4, book.getImgUrl());

                FileInputStream fis = new FileInputStream(saveImage(book.getImgUrl(), book.getBookID()));
                ps.setBlob(5, fis);
                ps.execute();
                fis.close();
            }
        } catch (Exception e) {
            System.out.println("线程：" + Thread.currentThread().getName() + "插入数据出错！");
            e.printStackTrace();
        } finally {
            // 6.释放资源
            closeResources(conn, ps);
        }
    }

    public static String saveImage(String url, String bookID) throws IOException {
        String path = "src/main/resources/image/" + bookID;
        //获取数据流
        InputStream is = new URL(url).openStream();
        //获取后缀名
        String imagePath = path + url.substring(url.lastIndexOf("."));
        //写入数据流
        OutputStream os = new FileOutputStream(imagePath);
        byte[] buf = new byte[1024];
        int p;
        while ((p = is.read(buf)) != -1) {
            os.write(buf, 0, p);
        }
        is.close();
        os.close();
        return imagePath;
    }

    public static int getCount() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int sum = 0;
        try {
            String sql = "SELECT COUNT(*) FROM jdbook";
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery(sql);
            while (rs.next()) {
                sum = rs.getInt(1);
            }
        } catch (Exception e) {
            System.out.println("线程：" + Thread.currentThread().getName() + "查询出错！");
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return sum;
    }

    public static void closeResources(Connection conn, PreparedStatement ps) {
        try {
            if (conn != null) {
                conn.close();
            }
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeResources(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (conn != null) {
                conn.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
