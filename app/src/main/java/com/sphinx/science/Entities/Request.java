package com.sphinx.science.Entities;

public class Request {
         private String name;
         private String url;
         private Boolean isDone;

         public Request(){}
         public Request(String name, String url, Boolean isDone) {
                  this.name = name;
                  this.url = url;
                  this.isDone = isDone;
         }

         public String getName() {
                  return name;
         }

         public void setName(String name) {
                  this.name = name;
         }

         public String getUrl() {
                  return url;
         }

         public void setUrl(String url) {
                  this.url = url;
         }

         public Boolean getIsDone() {
                  return isDone;
         }

         public void setIsDone(Boolean isDone) {
                  this.isDone = isDone;
         }
}
