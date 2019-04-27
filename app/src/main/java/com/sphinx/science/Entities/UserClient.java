package com.sphinx.science.Entities;

import android.app.Application;
import android.content.Intent;

import java.util.ArrayList;


public class UserClient extends Application {

         public  Intent restartServiceFor26Api;

         public Boolean isAppOpen;

         private ArrayList<Request> requests = null;

         private Client client = null;

         private Boolean isAdmin = false;

         public ArrayList<Request> getRequests() {
                  return requests;
         }

         public void setRequests(ArrayList<Request> requests) {
                  this.requests = requests;
         }

         public Client getClient() {
                  return client;
         }

         public void setClient(Client client) {
                  this.client = client;
         }

         public Boolean getAdmin() {
                  return isAdmin;
         }

         public void setAdmin(Boolean admin) {
                  isAdmin = admin;
         }
}
