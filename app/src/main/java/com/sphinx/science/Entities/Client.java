package com.sphinx.science.Entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Client implements Parcelable {

         public static final Creator<Client> CREATOR = new Creator<Client>() {
                  @Override
                  public Client createFromParcel(Parcel in) {
                           return new Client(in);
                  }

                  @Override
                  public Client[] newArray(int size) {
                           return new Client[size];
                  }
         };
         private String email;
         private String user_id;
         private String username;
         private String phone;
         private Double numberOfRequests;
         private String tokenId;

         public Client(String email, String user_id, String username, String phone, Double numberOfRequests, String tokenId) {
                  this.email = email;
                  this.user_id = user_id;
                  this.username = username;
                  this.phone = phone;
                  this.numberOfRequests = numberOfRequests;
                  this.tokenId = tokenId;
         }

         public Client() {

         }

         private Client(Parcel in) {
                  phone = in.readString();
                  user_id = in.readString();
                  username = in.readString();
                  email = in.readString();
         }

         public static Creator<Client> getCREATOR() {
                  return CREATOR;
         }

         public String getPhone() {
                  return phone;
         }

         public void setPhone(String phone) {
                  this.phone = phone;
         }

         public String getEmail() {
                  return email;
         }

         public void setEmail(String email) {
                  this.email = email;
         }

         public String getUser_id() {
                  return user_id;
         }

         public void setUser_id(String user_id) {
                  this.user_id = user_id;
         }

         public String getUsername() {
                  return username;
         }

         public void setUsername(String username) {
                  this.username = username;
         }

         public Double getNumberOfRequests() {
                  return numberOfRequests;
         }

         public String getTokenId() {
                  return tokenId;
         }

         public void setTokenId(String tokenId) {
                  this.tokenId = tokenId;
         }

         public void setNumberOfRequests(Double numberOfRequests) {
                  this.numberOfRequests = numberOfRequests;
         }

         @NonNull
         @Override
         public String toString() {
                  return "Client{" +
                          "phone='" + phone + '\'' +
                          ", username='" + username + '\'' +
                          ", email='" + email + '\'' +
                          ", token_id='" + tokenId + '\'' +
                          ", user_id='" + user_id + '\'' +
                          '}';
         }

         @Override
         public int describeContents() {
                  return 0;
         }

         @Override
         public void writeToParcel(Parcel dest, int flags) {
                  dest.writeString(email);
                  dest.writeString(user_id);
                  dest.writeString(username);
         }
}

