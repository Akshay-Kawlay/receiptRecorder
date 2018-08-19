package ak.kawlay.mail.utoronto.ca.textreader;

import java.io.Serializable;

class receiptRecord implements Serializable{

    private Double amount;
    private String name, category, date, photoPath;

    public receiptRecord(Double amount, String name, String category, String date, String photoPath) {
        this.amount = amount;
        this.name = name;
        this.category = category;
        this.date = date;
        this.photoPath = photoPath;
    }

    public Double getAmount(){
        return amount;
    }

    public String getName(){
        return name;
    }

    public String getCategory(){
        return category;
    }

    public String getDate(){
        return date;
    }

    public String getPhotoPath(){
        return photoPath;
    }

}
