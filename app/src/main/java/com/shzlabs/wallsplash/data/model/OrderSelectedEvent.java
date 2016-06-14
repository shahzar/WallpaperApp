package com.shzlabs.wallsplash.data.model;

/**
 * Created by shaz on 22/5/16.
 */
public class OrderSelectedEvent {
    String order;

    public OrderSelectedEvent(String order){
        this.order = order;
    }

    public String getOrder() {
        return order;
    }

}
