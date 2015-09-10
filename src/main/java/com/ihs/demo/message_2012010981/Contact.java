package com.ihs.demo.message_2012010981;

import com.ihs.contacts.api.IPhoneContact.HSContactContent;
import com.ihs.message_2012010981.managers.HSMessageManager;

import java.util.Date;
import java.util.Comparator;
public class Contact extends HSContactContent{
    String mid;
    String name;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastTimeStamp() {
        return HSMessageManager.getInstance().queryMessages(mid, 0, -1).getMessages().get(0).getTimestamp();
    }



    public Contact(String content, String label, String contactId, int type, boolean isFriend) {
        super(content, label, contactId, type, isFriend);
    }
//
//    @Override
//    public int compareTo(Object another) {
//        if this.getLastTimeStamp().after((Contact)another)
//        return 0;
//    }
}
