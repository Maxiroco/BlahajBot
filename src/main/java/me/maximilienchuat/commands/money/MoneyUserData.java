package me.maximilienchuat.commands.money;

import com.google.gson.Gson;

public class MoneyUserData {
    private int balance;
    private final long userId;

    public MoneyUserData(long userId){
        this.userId = userId;
    }

    public long getUserId(){
        return userId;
    }

    public int getBalance(){
        return balance;
    }

    public void addBalance(int amount){
        balance += amount;
    }

    public void substractBalance(int amount){
        balance -= amount;
    }
}
