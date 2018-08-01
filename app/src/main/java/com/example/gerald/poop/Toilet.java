package com.example.gerald.poop;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DataSnapshot;

public class Toilet implements Parcelable {
    private String toilet_id;
    private String toilet_title;
    private String toilet_address;
    private String toilet_description;
    private int toilet_rating;
    private Double toilet_lng;
    private Double toilet_lat;
    private int toilet_thumb_up;
    private int toilet_thumb_down;

    public static final Parcelable.Creator<Toilet> CREATOR = new Parcelable.Creator<Toilet>(){

        @Override
        public Toilet createFromParcel(Parcel parcel) {
            return new Toilet(parcel);
        }

        @Override
        public Toilet[] newArray(int i) {
            return new Toilet[0];
        }
    };

    public Toilet() {

    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(toilet_id);
        parcel.writeString(toilet_title);
        parcel.writeString(toilet_address);
        parcel.writeString(toilet_description);
        parcel.writeInt(toilet_rating);
        parcel.writeDouble(toilet_lat);
        parcel.writeDouble(toilet_lng);
        parcel.writeInt(toilet_thumb_up);
        parcel.writeInt(toilet_thumb_down);
    }

    public Toilet(Parcel parcel){
        toilet_id = parcel.readString();
        toilet_title = parcel.readString();
        toilet_address = parcel.readString();
        toilet_description = parcel.readString();
        toilet_rating = parcel.readInt();
        toilet_lat = parcel.readDouble();
        toilet_lng = parcel.readDouble();
        toilet_thumb_up = parcel.readInt();
        toilet_thumb_down = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    public Toilet(String toilet_id, String toilet_title, String toilet_address, String toilet_description, int toilet_rating, Double toilet_lng, Double toilet_lat, int toilet_thumb_up, int toilet_thumb_down) {
        this.toilet_id = toilet_id;
        this.toilet_title = toilet_title;
        this.toilet_address = toilet_address;
        this.toilet_description = toilet_description;
        this.toilet_rating = toilet_rating;
        this.toilet_lng = toilet_lng;
        this.toilet_lat = toilet_lat;
        this.toilet_thumb_up = toilet_thumb_up;
        this.toilet_thumb_down = toilet_thumb_down;
    }

    public String getToilet_id() {
        return toilet_id;
    }

    public void setToilet_id(String toilet_id) {
        this.toilet_id = toilet_id;
    }

    public String getToilet_title() {
        return toilet_title;
    }

    public void setToilet_title(String toilet_title) {
        this.toilet_title = toilet_title;
    }

    public String getToilet_address() {
        return toilet_address;
    }

    public void setToilet_address(String toilet_address) {
        this.toilet_address = toilet_address;
    }

    public String getToilet_description() {
        return toilet_description;
    }

    public void setToilet_description(String toilet_description) {
        this.toilet_description = toilet_description;
    }

    public int getToilet_rating() {
        return toilet_rating;
    }

    public void setToilet_rating(int toilet_rating) {
        this.toilet_rating = toilet_rating;
    }

    public Double getToilet_lng() {
        return toilet_lng;
    }

    public void setToilet_lng(Double toilet_lng) {
        this.toilet_lng = toilet_lng;
    }

    public Double getToilet_lat() {
        return toilet_lat;
    }

    public void setToilet_lat(Double toilet_lat) {
        this.toilet_lat = toilet_lat;
    }

    public int getToilet_thumb_up() {
        return toilet_thumb_up;
    }

    public void setToilet_thumb_up(int toilet_thumb_up) {
        this.toilet_thumb_up = toilet_thumb_up;
    }

    public int getToilet_thumb_down() {
        return toilet_thumb_down;
    }

    public void setToilet_thumb_down(int toilet_thumb_down) {
        this.toilet_thumb_down = toilet_thumb_down;
    }

    public void add_1_to_thumb_up(){
        this.toilet_thumb_up++;
    }

    public void add_1_to_thumb_down(){
        this.toilet_thumb_down++;
    }

    public void setToilet(Toilet toilet){
        this.setToilet_title(toilet.getToilet_title());
        this.setToilet_address(toilet.getToilet_address());
        this.setToilet_description(toilet.getToilet_description());
        this.setToilet_id(toilet.getToilet_id());
        this.setToilet_lng(toilet.getToilet_lng());
        this.setToilet_lat(toilet.getToilet_lat());
        this.setToilet_rating(toilet.getToilet_rating());
        this.setToilet_thumb_up(toilet.getToilet_thumb_up());
        this.setToilet_thumb_down(toilet.getToilet_thumb_down());
    }
}
