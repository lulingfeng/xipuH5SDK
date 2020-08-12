package com.xipu.h5.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;

public class OSkuDetails implements Parcelable {

    private String productId;
    private String price;
    private String price_amount_micros;
    private String price_currency_code;
    private String title;
    private String description;

    public OSkuDetails() {
    }

    protected OSkuDetails(Parcel in) {
        productId = in.readString();
        price = in.readString();
        price_amount_micros = in.readString();
        price_currency_code = in.readString();
        title = in.readString();
        description = in.readString();
    }

    public static final Creator<OSkuDetails> CREATOR = new Creator<OSkuDetails>() {
        @Override
        public OSkuDetails createFromParcel(Parcel in) {
            return new OSkuDetails(in);
        }

        @Override
        public OSkuDetails[] newArray(int size) {
            return new OSkuDetails[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productId);
        dest.writeString(price);
        dest.writeString(price_amount_micros);
        dest.writeString(price_currency_code);
        dest.writeString(title);
        dest.writeString(description);
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPrice_amount_micros() {
        return price_amount_micros;
    }

    public void setPrice_amount_micros(String price_amount_micros) {
        this.price_amount_micros = price_amount_micros;
    }

    public String getPrice_currency_code() {
        return price_currency_code;
    }

    public void setPrice_currency_code(String price_currency_code) {
        this.price_currency_code = price_currency_code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "OSkuDetails{" +
                "productId='" + productId + '\'' +
                ", price='" + price + '\'' +
                ", price_amount_micros='" + price_amount_micros + '\'' +
                ", price_currency_code='" + price_currency_code + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
