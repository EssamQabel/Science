package com.sphinx.science.Entities;

import com.sphinx.science.Entities.Client;

import java.util.ArrayList;

public class Profile {
         private Client client;
         private ArrayList<String> requestsUrls;

         public Profile(Client client){
                  this.client = client;
                  requestsUrls= new ArrayList<>();
         }

         public void addRequestUrl(String url){ requestsUrls.add(url); }

         public Client getClient()
         {
                  return client;
         }

         public ArrayList<String> getRequestsUrls() { return requestsUrls; }
}


