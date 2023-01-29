package com.driver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class WhatsappRepository {
    HashMap<String,User> usersMap=new HashMap<>();
    HashMap<Group,List<User>> GroupUserMap=new HashMap<>();
    HashMap<Group, List<Message>> GMap = new HashMap<>();
    HashMap<User,List<Message>> UMap = new HashMap<>();
    List<Message> messageList = new ArrayList<>();
    public int count=1;
    public int messageCount=0;
    public String createUser(String name,String mobile) throws Exception {
        if(usersMap.containsKey(mobile))
            throw new Exception("User already exists");
        else{
            User user = new User(name,mobile);
            usersMap.put(mobile,user);
            return "SUCCESS";
        }
    }
    public Group createGroup(List<User> users){
        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.

        //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
        //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.
        Group group = new Group();

        if(users.size()==2){
            group.setName(users.get(1).getName());
            group.setNumberOfParticipants(2);
        }
        else {
            group.setName("Group "+count);
            count++;
            group.setNumberOfParticipants(users.size());
        }
        GroupUserMap.put(group,users);
        return group;
    }

    public int createMessage(String content){
        Message message = new Message();
        message.setId(messageCount++);
        message.setTimestamp(new Date());
        message.setContent(content);
        messageList.add(message);
        return messageCount;
    }

    public int sendMessage(Message message, User sender, Group group)throws Exception{
        boolean userCheck=false;
        if(!GroupUserMap.containsKey(group))
            throw new Exception("Group does not exist");

        List<User> users = GroupUserMap.get(group);
        for(User user:users){
            if(user==sender){
                userCheck=true;
                break;
            }
        }
        if(userCheck==false)
            throw new Exception("You are not allowed to send message");

        if(GMap.containsKey(group)){
            GMap.get(group).add(message);
        }else {
            List<Message> messages = new ArrayList<>();
            messages.add(message);
            GMap.put(group,messages);
        }

        if(UMap.containsKey(sender)){
            UMap.get(sender).add(message);
        }else {
            List<Message> messages = new ArrayList<>();
            messages.add(message);
            UMap.put(sender,messages);
        }
        return GMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        if(!GroupUserMap.containsKey(group))
            throw new Exception("Group does not exist");
        if(GroupUserMap.get(group).get(0)!=approver)
            throw new Exception("Approver does not have rights");

        boolean cheakUser = false;
        int index = -1;

        for(User user1:GroupUserMap.get(group)){
            if(user1==user){
                cheakUser=true;
                index = GroupUserMap.get(group).indexOf(user1);
                break;
            }
        }

        if(cheakUser==false)
            throw new Exception("User is not a participant");

        User oldAdmin = GroupUserMap.get(group).get(0);
        GroupUserMap.get(group).add(0,user);
        GroupUserMap.get(group).add(index,oldAdmin);

        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception{
        Group group1 = null;
        boolean ckeckUser = false;

        for(Group group: GroupUserMap.keySet()){
            for(User user1: GroupUserMap.get(group)){
                if(user1==user){
                    group1 = group;
                    ckeckUser = true;
                    break;
                }
            }
            if(group1!=null)
                break;
        }

        if(!ckeckUser)
            throw new Exception("User not found");

        if(GroupUserMap.get(group1).get(0)==user)
            throw new Exception("Cannot remove admin");

        List<Message> messages = UMap.get(user);

        for(Message message:GMap.get(group1)){
            if(messages.contains(message))
                GMap.get(group1).remove(message);
        }

        for(Message message:messageList){
            if(messages.contains(message))
                messageList.remove(message);
        }

        GroupUserMap.get(group1).remove(user);
        UMap.remove(user);

        return GroupUserMap.get(group1).size()+GMap.get(group1).size()+messageList.size();
    }

    public String findMessage(Date start,Date end,int K) throws Exception {
        List<Message> messagesBetStartAndEnd=new ArrayList<>();
        int count=1;
        for(Message message:messageList){
            if(message.getTimestamp().after(start) && message.getTimestamp().before(end))
                messagesBetStartAndEnd.add(message);
        }
        if(messagesBetStartAndEnd.size()<K)
            throw new Exception("K is greater than the number of messages");
        String messageToSend="";
        for(Message message:messagesBetStartAndEnd){
            if(count==K)
                return message.getContent();
            else
                count++;
        }
        return "Message not found";
    }

}
