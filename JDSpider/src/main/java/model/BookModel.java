package model;

/**
 * @author JwZheng
 */
public class BookModel {
    private String bookName;
    private String bookPrice;
    private String bookID;
    private String imgUrl;

    public BookModel() {
    }

    public BookModel(String bookName, String bookPrice, String bookID, String imgUrl) {
        this.bookName = bookName;
        this.bookPrice = bookPrice;
        this.bookID = bookID;
        this.imgUrl = imgUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookPrice() {
        return bookPrice;
    }

    public void setBookPrice(String bookPrice) {
        this.bookPrice = bookPrice;
    }

    public String getBookID() {
        return bookID;
    }

    public void setBookID(String bookID) {
        this.bookID = bookID;
    }

}
