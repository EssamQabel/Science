package com.sphinx.science.Entities;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Message {

         private Boolean isAttachment = false;

         private String messageId;

         private String name;
         private String msg;
         private @ServerTimestamp
         Date timestamp;

         public Message() {
         }

         public Message(String name, String msg, Date timestamp) {
                  this.name = name;
                  this.msg = msg;
                  this.timestamp = timestamp;
         }

         public Message(String name, String msg, Boolean isAttachment) {
                  this.name = name;
                  this.msg = msg;
                  this.isAttachment = isAttachment;
         }

         public String getMsg() {
                  return msg;
         }

         public void setMsg(String msg) {
                  this.msg = msg;
         }

         public String getName() {
                  return name;
         }

         public void setName(String name) {
                  this.name = name;
         }

         public Boolean getAttachment() {
                  return isAttachment;
         }

         public void setAttachment(Boolean attachment) {
                  isAttachment = attachment;
         }

         public String getMessageId() {
                  return messageId;
         }

         public void setMessageId(String messageId) {
                  this.messageId = messageId;
         }

         public Date getTimestamp() {
                  return timestamp;
         }

         public void setTimestamp(Date timestamp) {
                  this.timestamp = timestamp;
         }
}
